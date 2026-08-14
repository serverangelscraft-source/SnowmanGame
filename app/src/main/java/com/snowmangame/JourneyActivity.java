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

public class JourneyActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.rgb(221,239,248));
            w.setNavigationBarColor(Color.rgb(238,248,253));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29) w.setNavigationBarContrastEnforced(false);
        setContentView(new JourneyView(this));
    }

    static class JourneyView extends View {
        static final int STATION=0, TICKET=1, BOARDING=2, TRAIN=3, ARRIVAL=4;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), text=new Paint(Paint.ANTI_ALIAS_FLAG), stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final SharedPreferences prefs;
        final float density, textScale;
        final RectF actionBtn=new RectF(), smallBtn=new RectF();
        int stage=STATION, year, wallet, ticketCost;
        boolean ticketOwned, yearAdvanced;
        long stageStart=SystemClock.elapsedRealtime();

        JourneyView(Context c){
            super(c);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.16f);
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            year=Math.max(1,Math.min(7,prefs.getInt("life_year",1)));
            wallet=Math.max(0,prefs.getInt("coins",0));
            ticketCost=year==1?0:Math.min(12,(year-1)*2);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);
        }

        float dp(float v){return v*density;}
        float tx(float v){return v*textScale;}
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        float smooth(float v){v=clamp(v,0,1);return v*v*(3-2*v);}
        float mix(float a,float b,float t){return a+(b-a)*t;}
        String ageName(int y){
            switch(y){
                case 1:return "Малюк";
                case 2:return "Малюк-дослідник";
                case 3:return "Пустун";
                case 4:return "Помічник";
                case 5:return "Майстер снігу";
                case 6:return "Майбутній школяр";
                default:return "Школяр";
            }
        }
        int yearGoal(int y){return 1100+(y-1)*320;}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float t=(SystemClock.elapsedRealtime()-stageStart)/1000f;
            drawSky(c,t);
            if(year>=7 && stage==STATION){drawSchoolFinish(c);return;}
            if(stage==STATION) drawStation(c,t);
            else if(stage==TICKET) drawTicket(c,t);
            else if(stage==BOARDING) drawBoarding(c,t);
            else if(stage==TRAIN) drawTrainRide(c,t);
            else drawArrival(c,t);
            if(stage==BOARDING && t>3.4f) switchStage(TRAIN);
            if(stage==TRAIN && t>6.2f) switchStage(ARRIVAL);
            postInvalidateOnAnimation();
        }

        void drawSky(Canvas c,float t){
            float w=getWidth(),h=getHeight();
            LinearGradient g=new LinearGradient(0,0,0,h*.78f,Color.rgb(163,213,239),Color.rgb(230,246,253),Shader.TileMode.CLAMP);
            p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(242,249,253));c.drawRect(0,h*.67f,w,h,p);
            for(int i=0;i<32;i++){
                float x=(i*91f+17f+(float)Math.sin(t*.5f+i)*dp(8))%Math.max(1,w);
                float y=(i*53f+t*dp(16+i%5))%(h*.68f);
                p.setColor(Color.argb(135+(i%3)*30,255,255,255));c.drawCircle(x,y,dp(1+(i%3)*.4f),p);
            }
        }

        void drawHeader(Canvas c,String title,String sub){
            float w=getWidth();RectF card=new RectF(dp(14),dp(18),w-dp(14),dp(112));
            p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(18));text.setColor(Color.rgb(29,72,101));c.drawText(title,card.left+dp(18),card.top+dp(33),text);
            text.setTextSize(tx(9));text.setColor(Color.rgb(91,128,149));c.drawText(sub,card.left+dp(18),card.top+dp(58),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(10));text.setColor(Color.rgb(34,104,146));c.drawText("● "+wallet+" монет",card.right-dp(18),card.top+dp(33),text);
            text.setTextSize(tx(7.5f));text.setColor(Color.rgb(112,143,158));c.drawText("РІК "+year+" • "+ageName(year),card.right-dp(18),card.top+dp(58),text);
        }

        void drawStation(Canvas c,float t){
            float w=getWidth(),h=getHeight();drawHeader(c,"Залізничний вокзал","Час вирушати у наступний рік життя");
            float platformY=h*.70f;
            p.setColor(Color.rgb(222,231,235));c.drawRect(0,platformY,w,h,p);
            p.setColor(Color.rgb(101,117,125));c.drawRect(0,platformY+dp(55),w,platformY+dp(62),p);
            p.setColor(Color.rgb(70,82,88));c.drawRect(0,platformY+dp(83),w,platformY+dp(88),p);
            for(int i=0;i<9;i++){float x=i*w/8f;p.setColor(Color.rgb(157,137,112));c.drawRect(x-dp(3),platformY+dp(47),x+dp(3),platformY+dp(94),p);}
            drawStationBuilding(c,w,h);
            drawTrain(c,w*.66f,platformY-dp(2),1f);
            drawSnowKid(c,w*.26f,platformY-dp(48),dp(34),0f);
            drawSpeech(c,w*.26f,h*.48f,"Я ще малюк. Мій перший переїзд — безкоштовний!");
            float bw=Math.min(w-dp(36),dp(350)),bh=dp(60),left=(w-bw)/2f,top=h-dp(82);
            actionBtn.set(left,top,left+bw,top+bh);p.setColor(Color.rgb(34,105,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11));text.setColor(Color.WHITE);
            c.drawText(ticketCost==0?"ОТРИМАТИ КВИТОК • 0 МОНЕТ":"КУПИТИ КВИТОК • "+ticketCost+" МОНЕТ",actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
            if(wallet<ticketCost){text.setTextSize(tx(8));text.setColor(Color.rgb(169,76,76));c.drawText("Потрібно ще "+(ticketCost-wallet)+" монет",w/2f,top-dp(10),text);}
        }

        void drawStationBuilding(Canvas c,float w,float h){
            float l=dp(16),top=h*.30f,r=w*.50f,b=h*.68f;
            p.setColor(Color.rgb(239,237,224));c.drawRoundRect(new RectF(l,top,r,b),dp(8),dp(8),p);
            p.setColor(Color.rgb(70,113,140));c.drawRect(l,top,r,top+dp(16),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.WHITE);c.drawText("ВОКЗАЛ",(l+r)/2,top+dp(12),text);
            p.setColor(Color.rgb(121,165,188));for(int i=0;i<3;i++)c.drawRect(l+dp(15+i*35),top+dp(40),l+dp(35+i*35),top+dp(68),p);
            p.setColor(Color.rgb(112,83,59));c.drawRect((l+r)/2-dp(15),b-dp(52),(l+r)/2+dp(15),b,p);
            p.setColor(Color.rgb(250,250,245));c.drawCircle(r-dp(27),top+dp(32),dp(14),p);stroke.setColor(Color.rgb(60,75,84));stroke.setStrokeWidth(dp(1.4f));c.drawCircle(r-dp(27),top+dp(32),dp(14),stroke);c.drawLine(r-dp(27),top+dp(32),r-dp(27),top+dp(24),stroke);c.drawLine(r-dp(27),top+dp(32),r-dp(20),top+dp(36),stroke);
        }

        void drawTrain(Canvas c,float cx,float base,float scale){
            float w=dp(220)*scale,h=dp(96)*scale,left=cx-w/2,top=base-h;
            p.setColor(Color.rgb(39,102,157));c.drawRoundRect(new RectF(left,top,cx+w/2,base),dp(16)*scale,dp(16)*scale,p);
            p.setColor(Color.rgb(243,196,42));c.drawRect(left,top+dp(15)*scale,cx+w/2,top+dp(23)*scale,p);
            p.setColor(Color.rgb(213,235,247));for(int i=0;i<4;i++){float x=left+dp(22+i*45)*scale;c.drawRoundRect(new RectF(x,top+dp(34)*scale,x+dp(29)*scale,top+dp(61)*scale),dp(4)*scale,dp(4)*scale,p);}
            p.setColor(Color.rgb(28,55,74));c.drawRect(left+dp(15)*scale,base-dp(26)*scale,left+dp(47)*scale,base,p);
            p.setColor(Color.rgb(35,45,50));c.drawCircle(left+dp(45)*scale,base+dp(4)*scale,dp(10)*scale,p);c.drawCircle(cx+w/2-dp(45)*scale,base+dp(4)*scale,dp(10)*scale,p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(6.5f)*scale);text.setColor(Color.WHITE);c.drawText("УКРЗАЛІЗНИЦЯ",cx,top+dp(80)*scale,text);
        }

        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;
            drawSnowBall(c,x,by,br);drawSnowBall(c,x,my,mr);drawSnowBall(c,x,hy,hr);
            p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);
            Path n=new Path();n.moveTo(x-hr*.03f,hy);n.lineTo(x+hr*.72f,hy+hr*.09f);n.lineTo(x-hr*.03f,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);
            stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));stroke.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(x-mr*.62f,my,x-mr*1.40f,my-mr*.30f,stroke);
            c.drawLine(x+mr*.62f,my,x+mr*(1.35f+wave*.25f),my-mr*(.28f+.32f*wave),stroke);stroke.setStrokeCap(Paint.Cap.BUTT);
        }
        void drawSnowBall(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(198,226,240)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);stroke.setColor(Color.argb(70,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);}
        void drawSpeech(Canvas c,float cx,float cy,String msg){float w=Math.min(getWidth()-dp(50),dp(330));RectF r=new RectF(cx-w*.50f,cy-dp(34),cx+w*.50f,cy+dp(30));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(r,dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.5f));text.setColor(Color.rgb(54,93,116));c.drawText(msg,r.centerX(),r.centerY()+dp(3),text);}

        void drawTicket(Canvas c,float t){
            float w=getWidth(),h=getHeight();drawHeader(c,"Квиток отримано","Ігрова подорож • не є справжнім проїзним документом");
            float cw=Math.min(w-dp(34),dp(360)),ch=dp(230),l=(w-cw)/2,top=h*.28f;RectF card=new RectF(l,top,l+cw,top+ch);
            p.setColor(Color.rgb(252,252,247));c.drawRoundRect(card,dp(22),dp(22),p);stroke.setColor(Color.rgb(45,110,156));stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(5)},0));c.drawRoundRect(card,dp(22),dp(22),stroke);stroke.setPathEffect(null);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(18));text.setColor(Color.rgb(32,93,139));c.drawText("УКРЗАЛІЗНИЦЯ",card.left+dp(18),card.top+dp(37),text);
            text.setTextSize(tx(9));text.setColor(Color.rgb(92,118,132));c.drawText("КВИТОК У НАСТУПНИЙ РІК",card.left+dp(18),card.top+dp(60),text);
            text.setTextSize(tx(12));text.setColor(Color.rgb(39,67,82));c.drawText("Рік "+year+"  →  Рік "+(year+1),card.left+dp(18),card.top+dp(96),text);
            text.setTextSize(tx(9));c.drawText("Вагон 3   •   Місце 12",card.left+dp(18),card.top+dp(125),text);c.drawText("Пасажир: Сніговик • "+ageName(year),card.left+dp(18),card.top+dp(151),text);
            text.setTextSize(tx(11));text.setColor(Color.rgb(49,132,101));c.drawText(ticketCost==0?"ІГРОВА ЦІНА: БЕЗКОШТОВНО":"ІГРОВА ЦІНА: "+ticketCost+" МОНЕТ",card.left+dp(18),card.top+dp(190),text);
            float bw=Math.min(w-dp(46),dp(330)),bh=dp(58),bl=(w-bw)/2,bt=h-dp(86);actionBtn.set(bl,bt,bl+bw,bt+bh);p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(19),dp(19),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11));text.setColor(Color.WHITE);c.drawText("ДО ВАГОНА",actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }

        void drawBoarding(Canvas c,float t){
            float w=getWidth(),h=getHeight();drawHeader(c,"Посадка","Вагон уже чекає на маленького пасажира");
            float ground=h*.76f;drawTrain(c,w*.69f,ground,1.08f);float k=smooth(t/3.1f);float x=mix(w*.16f,w*.57f,k);drawSnowKid(c,x,ground-dp(5),dp(31),smooth((t-1.6f)/1.0f));
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11));text.setColor(Color.rgb(45,84,108));c.drawText(t<1.6f?"Сніговик поспішає до свого вагона…":"Провідник уже відкрив двері",w/2,h*.20f,text);
        }

        void drawTrainRide(Canvas c,float t){
            float w=getWidth(),h=getHeight();
            p.setColor(Color.rgb(235,227,209));c.drawRect(0,0,w,h,p);
            p.setColor(Color.rgb(61,108,145));c.drawRect(0,0,w,dp(72),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(14));text.setColor(Color.WHITE);c.drawText("ВАГОН 3 • ДО НАСТУПНОГО РОКУ",w/2,dp(43),text);
            RectF window=new RectF(dp(24),dp(104),w-dp(24),h*.53f);p.setColor(Color.rgb(192,226,245));c.drawRoundRect(window,dp(14),dp(14),p);
            c.save();c.clipRect(window);p.setColor(Color.rgb(241,249,253));c.drawRect(window.left,window.top+window.height()*.62f,window.right,window.bottom,p);for(int i=0;i<8;i++){float x=window.right-((t*dp(70)+i*dp(83))%(window.width()+dp(80)));p.setColor(Color.rgb(123,159,168));Path tr=new Path();tr.moveTo(x,window.bottom-dp(52));tr.lineTo(x-dp(20),window.bottom);tr.lineTo(x+dp(20),window.bottom);tr.close();c.drawPath(tr,p);}c.restore();
            stroke.setColor(Color.rgb(93,104,110));stroke.setStrokeWidth(dp(5));c.drawRoundRect(window,dp(14),dp(14),stroke);
            p.setColor(Color.rgb(143,73,69));c.drawRoundRect(new RectF(dp(34),h*.58f,w-dp(34),h*.90f),dp(18),dp(18),p);
            drawSnowKid(c,w*.50f,h*.80f,dp(34),.3f);
            text.setTextSize(tx(11));text.setColor(Color.rgb(49,74,88));c.drawText("Тук-тук… кожна станція робить його старшим.",w/2,h*.56f,text);
            text.setTextSize(tx(8.5f));c.drawText("Наступного року сніг буде важчий, а ціль — вища.",w/2,h*.94f,text);
        }

        void drawArrival(Canvas c,float t){
            if(!yearAdvanced){yearAdvanced=true;year=Math.min(7,year+1);prefs.edit().putInt("life_year",year).apply();ticketCost=year==1?0:Math.min(12,(year-1)*2);}
            float w=getWidth(),h=getHeight(),a=smooth(t/.9f);drawHeader(c,"Прибуття","Подорож завершена — починається новий цикл");
            p.setColor(Color.argb((int)(238*a),255,255,255));RectF card=new RectF(dp(22),h*.27f,w-dp(22),h*.72f);c.drawRoundRect(card,dp(28),dp(28),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(34));text.setColor(Color.rgb(36,110,153));c.drawText("РІК "+year,w/2,card.top+dp(64),text);
            text.setTextSize(tx(18));text.setColor(Color.rgb(43,76,96));c.drawText(ageName(year),w/2,card.top+dp(98),text);
            text.setTextSize(tx(9));text.setColor(Color.rgb(92,130,148));c.drawText("Нова ціль: "+yearGoal(year)+" очок",w/2,card.top+dp(134),text);c.drawText("Котити сніг доведеться приблизно на "+(100+(year-1)*20)+"% зусиль",w/2,card.top+dp(158),text);
            if(year>=7){text.setTextSize(tx(14));text.setColor(Color.rgb(65,139,105));c.drawText("РІВЕНЬ «ШКОЛЯР» ВІДКРИТО",w/2,card.top+dp(205),text);}else{text.setTextSize(tx(9));text.setColor(Color.rgb(108,131,143));c.drawText("Попереду ще "+(7-year)+" роки до школи",w/2,card.top+dp(204),text);}
            float bw=Math.min(w-dp(42),dp(340)),bh=dp(60),l=(w-bw)/2,top=h-dp(86);actionBtn.set(l,top,l+bw,top+bh);p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextSize(tx(11));text.setColor(Color.WHITE);c.drawText(year>=7?"ПОЧАТИ РІВЕНЬ ШКОЛЯРА":"ПОЧАТИ НОВИЙ РІК",actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }

        void drawSchoolFinish(Canvas c){
            float w=getWidth(),h=getHeight();drawHeader(c,"Рівень Школяр","Сніговик доріс до першого великого рубежу");
            RectF card=new RectF(dp(22),h*.26f,w-dp(22),h*.72f);p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(30));text.setColor(Color.rgb(42,111,151));c.drawText("ШКОЛЯР",w/2,card.top+dp(70),text);
            text.setTextSize(tx(10));text.setColor(Color.rgb(84,124,145));c.drawText("7 років зимових пригод",w/2,card.top+dp(101),text);c.drawText("Тепер можна відкривати шкільні місії, друзів і нові міста.",w/2,card.top+dp(134),text);
            drawSnowKid(c,w/2,card.bottom-dp(26),dp(45),.7f);
            float bw=Math.min(w-dp(42),dp(340)),bh=dp(60),l=(w-bw)/2,top=h-dp(86);actionBtn.set(l,top,l+bw,top+bh);p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextSize(tx(11));text.setColor(Color.WHITE);c.drawText("ГРАТИ ЯК ШКОЛЯР",actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }

        void buyTicket(){
            if(wallet<ticketCost)return;
            wallet-=ticketCost;ticketOwned=true;prefs.edit().putInt("coins",wallet).apply();switchStage(TICKET);
        }
        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();invalidate();}
        void startNewYear(){Intent i=new Intent(getContext(),MainActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            performClick();float x=e.getX(),y=e.getY();
            if(!actionBtn.contains(x,y))return true;
            if(year>=7&&stage==STATION){startNewYear();return true;}
            if(stage==STATION){buyTicket();return true;}
            if(stage==TICKET){switchStage(BOARDING);return true;}
            if(stage==ARRIVAL){startNewYear();return true;}
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
