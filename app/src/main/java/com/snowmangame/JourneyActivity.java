package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class JourneyActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(236,247,252));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new JourneyView(this));
    }

    static class JourneyView extends View {
        static final int WALK_TO_CASHIER=0, CHOOSE_TICKET=1, VALIDATE_TICKET=2,
                FIND_WAGON=3, BOARDING=4, TRAIN=5, ARRIVAL=6;

        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final SharedPreferences prefs;
        final float density,textScale;
        final RectF cashierZone=new RectF(),validatorZone=new RectF(),actionBtn=new RectF(),ticketRect=new RectF();
        final RectF[] ticketChoices={new RectF(),new RectF(),new RectF()};
        final RectF[] wagonDoors={new RectF(),new RectF(),new RectF()};

        float safeTop,safeBottom;
        int stage=WALK_TO_CASHIER,year,wallet,ticketCost,nextYear,wagonTarget,platformTarget,mistakes;
        boolean draggingSnowman,draggingTicket,ticketOwned,yearAdvanced;
        float snowX,snowY,ticketX,ticketY;
        long stageStart=SystemClock.elapsedRealtime(),stationStart=SystemClock.elapsedRealtime();
        String hint="Проведи сніговика до каси";

        JourneyView(Context c){
            super(c);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            year=Math.max(1,Math.min(7,prefs.getInt("life_year",1)));
            wallet=Math.max(0,prefs.getInt("coins",0));
            nextYear=Math.min(7,year+1);
            ticketCost=year==1?0:Math.min(12,(year-1)*2);
            wagonTarget=2+((year+1)%3);
            platformTarget=1+(nextYear%2);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);
            setContentDescription("Залізничний вокзал — міні-рівень подорожі сніговика");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());safeTop=bars.top;safeBottom=bars.bottom;}
                    else{safeTop=insets.getSystemWindowInsetTop();safeBottom=insets.getSystemWindowInsetBottom();}
                    invalidate();return insets;
                }
            });
            requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;}
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        float smooth(float v){v=clamp(v,0,1);return v*v*(3f-2f*v);} float mix(float a,float b,float t){return a+(b-a)*t;}
        int stationSeconds(){return(int)((SystemClock.elapsedRealtime()-stationStart)/1000L);} String timeText(int s){return String.format("%d:%02d",s/60,s%60);}
        String ageName(int y){switch(y){case 1:return"Малюк";case 2:return"Малюк-дослідник";case 3:return"Пустун";case 4:return"Помічник";case 5:return"Майстер снігу";case 6:return"Майбутній школяр";default:return"Школяр";}}
        int yearGoal(int y){return 1100+(y-1)*320;}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float t=(SystemClock.elapsedRealtime()-stageStart)/1000f;drawBackground(c,t);layoutObjects();
            if(year>=7&&stage==WALK_TO_CASHIER){drawSchoolFinish(c);return;}
            if(stage==WALK_TO_CASHIER)drawWalkToCashier(c,t);else if(stage==CHOOSE_TICKET)drawChooseTicket(c,t);else if(stage==VALIDATE_TICKET)drawValidateTicket(c,t);else if(stage==FIND_WAGON)drawFindWagon(c,t);else if(stage==BOARDING)drawBoarding(c,t);else if(stage==TRAIN)drawTrainRide(c,t);else drawArrival(c,t);
            if(stage==BOARDING&&t>3.7f)switchStage(TRAIN);if(stage==TRAIN&&t>6.0f)switchStage(ARRIVAL);postInvalidateOnAnimation();
        }

        void layoutObjects(){
            float w=getWidth(),bottom=getHeight()-safeBottom,stationTop=safeTop+dp(132),platformY=bottom-dp(170);
            cashierZone.set(dp(18),stationTop+dp(80),w*.42f,platformY-dp(8));validatorZone.set(w-dp(108),stationTop+dp(56),w-dp(22),stationTop+dp(138));
            if(snowX==0){snowX=w*.72f;snowY=platformY-dp(5);}else snowY=platformY-dp(5);
            if(ticketX==0){ticketX=w*.30f;ticketY=bottom-dp(145);}
        }

        void drawBackground(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;LinearGradient g=new LinearGradient(0,0,0,bottom*.78f,Color.rgb(157,213,240),Color.rgb(226,245,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(239,248,252));c.drawRect(0,bottom*.70f,w,h,p);
            for(int i=0;i<26;i++){float x=(i*97f+21f+(float)Math.sin(t*.6f+i)*dp(7))%Math.max(1,w),y=safeTop+((i*61f+t*dp(14+i%4))%Math.max(dp(120),bottom*.63f));p.setColor(Color.argb(135+(i%3)*35,255,255,255));c.drawCircle(x,y,dp(1+(i%3)*.45f),p);}
        }

        void drawHeader(Canvas c,String title,String sub){
            float w=getWidth(),top=safeTop+dp(8);RectF card=new RectF(dp(12),top,w-dp(12),top+dp(104));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(28,72,101));text.setTextSize(tx(17));c.drawText(title,card.left+dp(16),card.top+dp(31),text);text.setTextSize(tx(8.2f));text.setColor(Color.rgb(91,128,149));c.drawText(sub,card.left+dp(16),card.top+dp(57),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(9.5f));text.setColor(Color.rgb(34,104,146));c.drawText("● "+wallet+" монет",card.right-dp(16),card.top+dp(31),text);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(108,140,157));c.drawText("РІК "+year+" • "+ageName(year),card.right-dp(16),card.top+dp(57),text);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.1f));text.setColor(Color.rgb(120,145,158));c.drawText("Вокзал  "+timeText(stationSeconds())+"   •   помилки "+mistakes,card.left+dp(16),card.bottom-dp(14),text);text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(6.2f));c.drawText("ІГРОВА СЦЕНА • НЕ ОФІЦІЙНИЙ СЕРВІС",card.right-dp(16),card.bottom-dp(14),text);
        }

        String stationSpeech(){return ticketCost==0?"Мій перший переїзд: квиток для Малюка — 0 монет.":"Я вже "+ageName(year)+". До року "+nextYear+" квиток коштує "+ticketCost+" монет.";}

        void drawWalkToCashier(Canvas c,float t){
            float bottom=getHeight()-safeBottom,platformY=bottom-dp(170);drawHeader(c,"Залізничний вокзал","Крок 1/4 • сам дійди до каси");drawStationBuilding(c,cashierZone);drawPlatform(c,platformY);drawTrainSet(c,platformY,false);drawSnowKid(c,snowX,snowY,dp(29),0f);drawHintBubble(c,stationSpeech());
            stroke.setColor(Color.argb(170,39,126,164));stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(5)},0));c.drawRoundRect(cashierZone,dp(15),dp(15),stroke);stroke.setPathEffect(null);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.2f));text.setColor(Color.rgb(50,111,145));c.drawText("КАСА — ПЕРЕТЯГНИ СЮДИ",cashierZone.centerX(),cashierZone.bottom-dp(18),text);drawBottomHint(c,"Затисни сніговика і проведи його до каси");
        }

        void drawStationBuilding(Canvas c,RectF b){
            p.setColor(Color.rgb(241,238,224));c.drawRoundRect(b,dp(10),dp(10),p);p.setColor(Color.rgb(66,111,140));c.drawRect(b.left,b.top,b.right,b.top+dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.WHITE);c.drawText("КАСА",b.centerX(),b.top+dp(14),text);p.setColor(Color.rgb(126,169,191));c.drawRoundRect(new RectF(b.left+dp(16),b.top+dp(40),b.right-dp(16),b.top+dp(88)),dp(6),dp(6),p);p.setColor(Color.rgb(112,83,59));c.drawRect(b.centerX()-dp(15),b.bottom-dp(48),b.centerX()+dp(15),b.bottom,p);
        }

        void drawPlatform(Canvas c,float y){
            float w=getWidth();p.setColor(Color.rgb(216,228,234));c.drawRect(0,y,w,getHeight(),p);p.setColor(Color.rgb(99,114,121));c.drawRect(0,y+dp(48),w,y+dp(54),p);p.setColor(Color.rgb(60,74,81));c.drawRect(0,y+dp(78),w,y+dp(84),p);for(int i=0;i<10;i++){float x=i*w/9f;p.setColor(Color.rgb(158,137,111));c.drawRect(x-dp(3),y+dp(43),x+dp(3),y+dp(91),p);}
        }

        void drawHintBubble(Canvas c,String msg){float w=getWidth(),top=safeTop+dp(122);RectF r=new RectF(dp(18),top,w-dp(18),top+dp(62));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(r,dp(19),dp(19),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.2f));text.setColor(Color.rgb(54,93,116));c.drawText(msg,r.centerX(),r.centerY()+dp(3),text);}
        void drawBottomHint(Canvas c,String msg){float bottom=getHeight()-safeBottom,top=bottom-dp(66);actionBtn.set(dp(18),top,getWidth()-dp(18),bottom-dp(10));p.setColor(Color.argb(240,37,106,151));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.5f));text.setColor(Color.WHITE);c.drawText(msg,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);}

        void drawChooseTicket(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom;drawHeader(c,"Каса","Крок 2/4 • вибери правильний рік призначення");drawHintBubble(c,"Потрібен квиток: Рік "+year+" → Рік "+nextYear+". Не переплутай.");
            int wrong1=nextYear>=7?Math.max(1,nextYear-2):nextYear+1,wrong2=nextYear<=2?nextYear+2:nextYear-1;int[] opts={wrong1,nextYear,wrong2};float gap=dp(10),left=dp(18),right=w-dp(18),top=safeTop+dp(215),hh=dp(82),ww=(right-left-gap*2)/3f;
            for(int i=0;i<3;i++){ticketChoices[i].set(left+i*(ww+gap),top,left+i*(ww+gap)+ww,top+hh);p.setColor(Color.WHITE);c.drawRoundRect(ticketChoices[i],dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(88,120,139));c.drawText("КВИТОК",ticketChoices[i].centerX(),ticketChoices[i].top+dp(22),text);text.setTextSize(tx(18));text.setColor(Color.rgb(35,103,150));c.drawText("РІК "+opts[i],ticketChoices[i].centerX(),ticketChoices[i].centerY()+dp(8),text);text.setTextSize(tx(7));text.setColor(Color.rgb(110,140,154));c.drawText("платформа "+(1+(opts[i]%2)),ticketChoices[i].centerX(),ticketChoices[i].bottom-dp(12),text);}
            RectF card=new RectF(dp(28),top+hh+dp(24),w-dp(28),top+hh+dp(93));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(card,dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(57,91,111));c.drawText(ticketCost==0?"Перший квиток: 0 монет":"Ціна правильного квитка: "+ticketCost+" монет",w/2,card.top+dp(26),text);text.setTextSize(tx(8));text.setColor(wallet>=ticketCost?Color.rgb(55,132,103):Color.rgb(170,73,73));c.drawText(wallet>=ticketCost?"У гаманці достатньо: "+wallet:"Не вистачає "+(ticketCost-wallet)+" монет",w/2,card.bottom-dp(15),text);drawBottomHint(c,wallet>=ticketCost?"Обери квиток у РІК "+nextYear:"ПОВЕРНУТИСЯ ЗАРОБИТИ МОНЕТИ");
        }

        void drawValidateTicket(Canvas c,float t){
            drawHeader(c,"Перевірка квитка","Крок 3/4 • протягни квиток через валідатор");drawHintBubble(c,"Запам'ятай: вагон "+wagonTarget+", платформа "+platformTarget+". Потім квиток сховається.");p.setColor(Color.rgb(48,102,138));c.drawRoundRect(validatorZone,dp(16),dp(16),p);p.setColor(Color.rgb(191,229,205));c.drawRoundRect(new RectF(validatorZone.left+dp(13),validatorZone.top+dp(14),validatorZone.right-dp(13),validatorZone.top+dp(42)),dp(7),dp(7),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.2f));text.setColor(Color.WHITE);c.drawText("ВАЛІДАТОР",validatorZone.centerX(),validatorZone.bottom-dp(15),text);drawGameTicket(c,ticketX,ticketY,1f);stroke.setColor(Color.argb(190,50,137,105));stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(5)},0));c.drawRoundRect(validatorZone,dp(16),dp(16),stroke);stroke.setPathEffect(null);drawBottomHint(c,"Перетягни квиток у зелений валідатор");
        }

        void drawGameTicket(Canvas c,float cx,float cy,float scale){
            float ww=dp(205)*scale,hh=dp(103)*scale;ticketRect.set(cx-ww/2,cy-hh/2,cx+ww/2,cy+hh/2);p.setColor(Color.rgb(253,252,245));c.drawRoundRect(ticketRect,dp(14)*scale,dp(14)*scale,p);stroke.setColor(Color.rgb(42,108,153));stroke.setStrokeWidth(dp(1.5f));stroke.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(4)},0));c.drawRoundRect(ticketRect,dp(14)*scale,dp(14)*scale,stroke);stroke.setPathEffect(null);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(11)*scale);text.setColor(Color.rgb(37,103,149));c.drawText("УКРЗАЛІЗНИЦЯ",ticketRect.left+dp(12),ticketRect.top+dp(22),text);text.setTextSize(tx(7)*scale);text.setColor(Color.rgb(79,111,128));c.drawText("ІГРОВИЙ КВИТОК • РІК "+nextYear,ticketRect.left+dp(12),ticketRect.top+dp(42),text);text.setTextSize(tx(8.5f)*scale);text.setColor(Color.rgb(42,68,82));c.drawText("ВАГОН "+wagonTarget+"   •   ПЛАТФОРМА "+platformTarget,ticketRect.left+dp(12),ticketRect.top+dp(66),text);text.setTextSize(tx(7)*scale);text.setColor(Color.rgb(105,132,145));c.drawText("Не є справжнім проїзним документом",ticketRect.left+dp(12),ticketRect.bottom-dp(12),text);
        }

        void drawFindWagon(Canvas c,float t){
            float bottom=getHeight()-safeBottom,platformY=bottom-dp(165);drawHeader(c,"Платформа "+platformTarget,"Крок 4/4 • знайди вагон по пам'яті");drawHintBubble(c,hint.equals("Проведи сніговика до каси")?"Квиток перевірено. До якого вагона треба сісти?":hint);drawPlatform(c,platformY);drawTrainSet(c,platformY,true);drawSnowKid(c,snowX,snowY,dp(28),0f);drawBottomHint(c,"Перетягни сніговика до дверей правильного вагона");
        }

        void drawTrainSet(Canvas c,float platformY,boolean numbered){
            float w=getWidth(),top=platformY-dp(105),margin=dp(8),gap=dp(5),carW=(w-margin*2-gap*2)/3f;
            for(int i=0;i<3;i++){float left=margin+i*(carW+gap),right=left+carW;int num=2+i;p.setColor(Color.rgb(39,102,157));c.drawRoundRect(new RectF(left,top,right,platformY),dp(12),dp(12),p);p.setColor(Color.rgb(243,196,42));c.drawRect(left,top+dp(15),right,top+dp(23),p);p.setColor(Color.rgb(213,235,247));for(int k=0;k<2;k++){float wx=left+dp(12)+k*(carW*.44f);c.drawRoundRect(new RectF(wx,top+dp(34),wx+carW*.27f,top+dp(58)),dp(4),dp(4),p);}RectF door=new RectF(right-dp(35),platformY-dp(52),right-dp(9),platformY);wagonDoors[i].set(door);p.setColor(Color.rgb(27,57,77));c.drawRect(door,p);p.setColor(Color.rgb(35,45,50));c.drawCircle(left+carW*.25f,platformY+dp(4),dp(8),p);c.drawCircle(right-carW*.25f,platformY+dp(4),dp(8),p);if(numbered){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(18));text.setColor(Color.WHITE);c.drawText(String.valueOf(num),left+dp(21),top+dp(76),text);}}
        }

        void drawBoarding(Canvas c,float t){float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom-dp(135);drawHeader(c,"Посадка у вагон "+wagonTarget,"Усе правильно • ігровий квиток перевірено");float k=smooth(t/3.1f),x=mix(w*.18f,w*.68f,k);drawTrainSet(c,ground,false);drawSnowKid(c,x,ground-dp(3),dp(28),smooth((t-1.6f)/1f));text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.rgb(45,84,108));c.drawText(t<1.7f?"Сніговик йде до дверей…":"Наступна зупинка — новий рік.",w/2,safeTop+dp(148),text);}

        void drawTrainRide(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;p.setColor(Color.rgb(235,227,209));c.drawRect(0,0,w,h,p);p.setColor(Color.rgb(61,108,145));c.drawRect(0,safeTop,w,safeTop+dp(72),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(13));text.setColor(Color.WHITE);c.drawText("ВАГОН "+wagonTarget+" • У РІК "+nextYear,w/2,safeTop+dp(43),text);RectF window=new RectF(dp(22),safeTop+dp(100),w-dp(22),bottom*.54f);p.setColor(Color.rgb(192,226,245));c.drawRoundRect(window,dp(14),dp(14),p);c.save();c.clipRect(window);p.setColor(Color.rgb(241,249,253));c.drawRect(window.left,window.top+window.height()*.62f,window.right,window.bottom,p);for(int i=0;i<9;i++){float x=window.right-((t*dp(78)+i*dp(75))%(window.width()+dp(80)));p.setColor(Color.rgb(123,159,168));Path tr=new Path();tr.moveTo(x,window.bottom-dp(52));tr.lineTo(x-dp(19),window.bottom);tr.lineTo(x+dp(19),window.bottom);tr.close();c.drawPath(tr,p);}c.restore();stroke.setColor(Color.rgb(93,104,110));stroke.setStrokeWidth(dp(5));c.drawRoundRect(window,dp(14),dp(14),stroke);p.setColor(Color.rgb(142,75,70));c.drawRoundRect(new RectF(dp(34),bottom*.60f,w-dp(34),bottom*.88f),dp(18),dp(18),p);drawSnowKid(c,w*.5f,bottom*.80f,dp(32),.35f);text.setTextSize(tx(9.5f));text.setColor(Color.rgb(49,74,88));c.drawText("Тук-тук… після вокзалу новий рік стане складнішим.",w/2,bottom*.57f,text);
        }

        void drawArrival(Canvas c,float t){
            if(!yearAdvanced){yearAdvanced=true;year=nextYear;prefs.edit().putInt("life_year",year).apply();int best=prefs.getInt("station_best",9999);if(stationSeconds()<best)prefs.edit().putInt("station_best",stationSeconds()).apply();}
            float w=getWidth(),bottom=getHeight()-safeBottom,a=smooth(t/.8f);drawHeader(c,"Прибуття","Вокзальний міні-рівень завершено");RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(92));p.setColor(Color.argb((int)(242*a),255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(31));text.setColor(Color.rgb(36,110,153));c.drawText("РІК "+year,w/2,card.top+dp(62),text);text.setTextSize(tx(17));text.setColor(Color.rgb(43,76,96));c.drawText(ageName(year),w/2,card.top+dp(96),text);text.setTextSize(tx(9));text.setColor(Color.rgb(92,130,148));c.drawText("Нова ціль: "+yearGoal(year)+" очок",w/2,card.top+dp(130),text);c.drawText("Вокзал: "+timeText(stationSeconds())+" • помилки: "+mistakes,w/2,card.top+dp(154),text);c.drawText("Сніг у новому році потребує більше руху й точності.",w/2,card.top+dp(180),text);if(mistakes==0){text.setTextSize(tx(10));text.setColor(Color.rgb(55,133,104));c.drawText("БЕЗ ПОМИЛОК • ЧИСТА ПОДОРОЖ",w/2,card.top+dp(215),text);}actionBtn.set(dp(22),bottom-dp(74),w-dp(22),bottom-dp(14));p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText(year>=7?"ПОЧАТИ РІВЕНЬ ШКОЛЯРА":"ПОЧАТИ РІК "+year,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }

        void drawSchoolFinish(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;drawHeader(c,"Рівень Школяр","Сніговик уже дістався першого великого рубежу");RectF card=new RectF(dp(22),safeTop+dp(150),w-dp(22),bottom-dp(95));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(29));text.setColor(Color.rgb(42,111,151));c.drawText("ШКОЛЯР",w/2,card.top+dp(65),text);text.setTextSize(tx(9.5f));text.setColor(Color.rgb(84,124,145));c.drawText("7 років зимових пригод пройдено",w/2,card.top+dp(98),text);drawSnowKid(c,w/2,card.bottom-dp(18),dp(43),.7f);actionBtn.set(dp(22),bottom-dp(74),w-dp(22),bottom-dp(14));p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("ГРАТИ ЯК ШКОЛЯР",actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }

        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;drawSnowBall(c,x,by,br);drawSnowBall(c,x,my,mr);drawSnowBall(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x-hr*.03f,hy);n.lineTo(x+hr*.72f,hy+hr*.09f);n.lineTo(x-hr*.03f,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));stroke.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x-mr*.62f,my,x-mr*1.35f,my-mr*.25f,stroke);c.drawLine(x+mr*.62f,my,x+mr*(1.35f+wave*.2f),my-mr*(.25f+.28f*wave),stroke);stroke.setStrokeCap(Paint.Cap.BUTT);
        }
        void drawSnowBall(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(198,226,240)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);stroke.setColor(Color.argb(70,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);}

        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();hint="";invalidate();}
        void startNewYear(){Intent i=new Intent(getContext(),MainActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}

        void chooseTicket(float x,float y){
            if(wallet<ticketCost){if(actionBtn.contains(x,y))startNewYear();return;}
            int wrong1=nextYear>=7?Math.max(1,nextYear-2):nextYear+1,wrong2=nextYear<=2?nextYear+2:nextYear-1;int[] opts={wrong1,nextYear,wrong2};
            for(int i=0;i<3;i++)if(ticketChoices[i].contains(x,y)){if(opts[i]==nextYear){wallet-=ticketCost;ticketOwned=true;prefs.edit().putInt("coins",wallet).apply();ticketX=getWidth()*.30f;ticketY=getHeight()-safeBottom-dp(145);switchStage(VALIDATE_TICKET);}else{mistakes++;hint="Це не той рік. Потрібен РІК "+nextYear;}invalidate();return;}
        }

        void tryValidate(){if(validatorZone.contains(ticketX,ticketY)){draggingTicket=false;snowX=getWidth()*.12f;hint="Квиток перевірено";switchStage(FIND_WAGON);}else{mistakes++;draggingTicket=false;ticketX=getWidth()*.30f;ticketY=getHeight()-safeBottom-dp(145);hint="Не потрапив у валідатор";invalidate();}}
        void tryBoard(){int selected=-1;for(int i=0;i<3;i++){RectF d=wagonDoors[i];if(Math.abs(snowX-d.centerX())<d.width()*1.35f){selected=2+i;break;}}if(selected==wagonTarget){draggingSnowman=false;switchStage(BOARDING);}else{mistakes++;draggingSnowman=false;hint=selected<0?"Підведи сніговика прямо до дверей":"Це вагон "+selected+". Потрібен інший";snowX=getWidth()*.12f;invalidate();}}

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(stage==WALK_TO_CASHIER&&year<7&&Math.abs(x-snowX)<dp(55)&&Math.abs(y-(snowY-dp(45)))<dp(100)){draggingSnowman=true;return true;}
                if(stage==VALIDATE_TICKET&&ticketRect.contains(x,y)){draggingTicket=true;return true;}
                if(stage==FIND_WAGON&&Math.abs(x-snowX)<dp(55)&&Math.abs(y-(snowY-dp(45)))<dp(100)){draggingSnowman=true;return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){if(draggingSnowman){snowX=clamp(x,dp(18),getWidth()-dp(18));invalidate();return true;}if(draggingTicket){ticketX=clamp(x,dp(55),getWidth()-dp(55));ticketY=clamp(y,safeTop+dp(150),getHeight()-safeBottom-dp(70));invalidate();return true;}return true;}
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                performClick();
                if(year>=7&&stage==WALK_TO_CASHIER&&actionBtn.contains(x,y)){startNewYear();return true;}
                if(stage==WALK_TO_CASHIER&&draggingSnowman){draggingSnowman=false;if(cashierZone.contains(snowX,snowY-dp(45))||snowX<cashierZone.right){switchStage(CHOOSE_TICKET);}else{mistakes++;hint="Каса ліворуч";invalidate();}return true;}
                if(stage==CHOOSE_TICKET){chooseTicket(x,y);return true;}
                if(stage==VALIDATE_TICKET&&draggingTicket){tryValidate();return true;}
                if(stage==FIND_WAGON&&draggingSnowman){tryBoard();return true;}
                if(stage==ARRIVAL&&actionBtn.contains(x,y)){startNewYear();return true;}
                return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
