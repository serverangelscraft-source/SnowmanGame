package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class YearTwoActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(239,248,252));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new YearTwoView(this));
    }

    static class YearTwoView extends View {
        static final int INTRO=0, INSPECT=1, CHOOSE=2, TRAIL=3, MITTEN=4, READY=5;
        final Context ctx;
        final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF action=new RectF();
        final RectF[] patches={new RectF(),new RectF(),new RectF()};
        final RectF[] ice={new RectF(),new RectF(),new RectF()};
        final boolean[] inspected={false,false,false};
        final Vibrator vibrator;
        ToneGenerator tone;
        final float density,textScale;
        float safeTop,safeBottom;
        int stage=INTRO,mistakes,lastPatch=-1;
        boolean dragging;
        float snowX=Float.NaN,snowY=Float.NaN;
        String feedback="";
        long feedbackUntil;

        YearTwoView(Context c){
            super(c);ctx=c;
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.16f);
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            try{tone=new ToneGenerator(AudioManager.STREAM_MUSIC,30);}catch(Exception ignored){}
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);
            setContentDescription("Рік 2 — сюжетний рівень Малюка-дослідника");
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
        float dist(float x1,float y1,float x2,float y2){return(float)Math.hypot(x1-x2,y1-y2);}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,75));else vibrator.vibrate(ms);}
        void tone(int type,int ms){if(tone!=null)try{tone.startTone(type,ms);}catch(Exception ignored){}}
        void say(String s,boolean good){feedback=s;feedbackUntil=android.os.SystemClock.elapsedRealtime()+1200;buzz(good?28:12);tone(good?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_NACK,90);invalidate();}
        void next(int s){stage=s;dragging=false;if(s==TRAIL){snowX=dp(58);snowY=getHeight()-safeBottom-dp(115);}invalidate();}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);layout();drawBackground(c);drawHeader(c);
            if(stage==INTRO)drawIntro(c);else if(stage==INSPECT||stage==CHOOSE)drawSnowLab(c);else if(stage==TRAIL)drawTrail(c);else if(stage==MITTEN)drawMittenStory(c);else drawReady(c);
            if(android.os.SystemClock.elapsedRealtime()<feedbackUntil)drawFeedback(c);
            postInvalidateDelayed(50);
        }

        void layout(){
            float w=getWidth(),bottom=getHeight()-safeBottom,top=safeTop+dp(185),gap=dp(10),pw=(w-dp(36)-gap*2)/3f;
            for(int i=0;i<3;i++){float l=dp(18)+i*(pw+gap);patches[i].set(l,top,l+pw,top+dp(126));}
            ice[0].set(w*.34f,bottom*.49f,w*.55f,bottom*.56f);ice[1].set(w*.61f,bottom*.60f,w*.85f,bottom*.67f);ice[2].set(w*.18f,bottom*.67f,w*.38f,bottom*.73f);
            action.set(dp(22),bottom-dp(76),w-dp(22),bottom-dp(14));
        }

        void drawBackground(Canvas c){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient g=new LinearGradient(0,safeTop,0,bottom*.72f,Color.rgb(158,217,246),Color.rgb(231,248,255),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(246,251,253));c.drawRect(0,bottom*.67f,w,h,p);p.setColor(Color.argb(220,255,255,255));c.drawOval(new RectF(-w*.25f,bottom*.55f,w*.65f,bottom*.78f),p);c.drawOval(new RectF(w*.35f,bottom*.58f,w*1.2f,bottom*.80f),p);
            for(int i=0;i<20;i++){float x=(i*83f+37f)%Math.max(1,w),y=safeTop+dp(18)+((i*67f)%Math.max(dp(100),bottom*.58f));p.setColor(Color.argb(120+(i%3)*35,255,255,255));c.drawCircle(x,y,dp(1+(i%3)*.45f),p);}
        }

        void drawHeader(Canvas c){
            float w=getWidth(),top=safeTop+dp(9);RectF card=new RectF(dp(13),top,w-dp(13),top+dp(144));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(25),dp(25),p);
            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(29,72,99));text.setTextSize(tx(8));c.drawText("РІК 2 • МАЛЮК-ДОСЛІДНИК",card.left+dp(17),card.top+dp(22),text);text.setTextSize(tx(19));c.drawText(title(),card.left+dp(17),card.top+dp(51),text);
            text.setTextSize(tx(8.2f));text.setColor(Color.rgb(91,128,149));c.drawText(subtitle(),card.left+dp(17),card.top+dp(78),text);c.drawText(progress(),card.left+dp(17),card.top+dp(101),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(7));text.setColor(Color.rgb(118,143,156));c.drawText("помилки "+mistakes,card.right-dp(17),card.top+dp(22),text);
            float y=card.bottom-dp(16);int step=stage==INTRO?0:(stage==INSPECT||stage==CHOOSE?1:(stage==TRAIL?2:(stage==MITTEN?3:4)));
            for(int i=0;i<5;i++){p.setColor(i<=step?Color.rgb(53,132,170):Color.rgb(213,231,240));c.drawCircle(card.left+dp(20)+i*dp(18),y,dp(3.2f),p);}
        }

        String title(){if(stage==INTRO)return"Новий сніг — нові правила";if(stage==INSPECT)return"Досліди три замети";if(stage==CHOOSE)return"Обери правильний сніг";if(stage==TRAIL)return"Слід веде до знахідки";if(stage==MITTEN)return"Хтось загубив рукавичку";return"Дослідження завершено";}
        String subtitle(){if(stage==INTRO)return"Цього року мало просто катати кулі — спочатку роздивись світ.";if(stage==INSPECT)return"Торкнись кожного замету: пухкий, мокрий чи крижаний?";if(stage==CHOOSE)return"Для ліпки потрібен пухкий сніг. Вибери його сам.";if(stage==TRAIL)return"Проведи сніговика до рукавички, не наступаючи на лід.";if(stage==MITTEN)return"Це не твоя річ. Збережемо її, щоб повернути власнику.";return"Тепер ти знаєш місцевий сніг і маєш першу загадку року.";}
        String progress(){if(stage==INTRO)return"ФОКУС: уважність";if(stage==INSPECT)return"ПЕРЕВІРЕНО: "+countInspected()+"/3";if(stage==CHOOSE)return"ЗАВДАННЯ: знайди пухкий";if(stage==TRAIL)return"НЕБЕЗПЕКА: лід • ЗНАХІДКА: ?";if(stage==MITTEN)return"ЗНАХІДКА: синя рукавичка";return"ПАМ'ЯТЬ РОКУ: синя рукавичка";}
        int countInspected(){int n=0;for(boolean b:inspected)if(b)n++;return n;}

        void drawIntro(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.70f;drawSnowKid(c,w*.50f,ground,dp(42),.15f);drawMagnifier(c,w*.69f,ground-dp(120),1f);
            RectF bubble=new RectF(dp(28),safeTop+dp(177),w-dp(28),safeTop+dp(276));p.setColor(Color.argb(240,255,255,255));c.drawRoundRect(bubble,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);text.setColor(Color.rgb(49,88,111));text.setTextSize(tx(11));c.drawText("«Я вже не просто Малюк.»",bubble.centerX(),bubble.top+dp(31),text);text.setTextSize(tx(8.4f));text.setColor(Color.rgb(95,130,147));c.drawText("Другий рік починається з дослідження.",bubble.centerX(),bubble.top+dp(57),text);c.drawText("Подивимось, який тут сніг.",bubble.centerX(),bubble.top+dp(78),text);drawAction(c,"ПОЧАТИ ДОСЛІДЖЕННЯ",true);
        }

        void drawSnowLab(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;for(int i=0;i<3;i++)drawPatch(c,i);RectF info=new RectF(dp(22),patches[0].bottom+dp(20),w-dp(22),Math.min(bottom-dp(96),patches[0].bottom+dp(112)));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(info,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(56,96,119));
            if(stage==INSPECT){String m=lastPatch<0?"Торкнись замету — сніговик перевірить його.":patchName(lastPatch)+": "+patchResult(lastPatch);c.drawText(m,info.centerX(),info.top+dp(30),text);text.setTextSize(tx(7.5f));text.setColor(Color.rgb(99,133,150));c.drawText(countInspected()<3?"Ще треба перевірити "+(3-countInspected())+".":"Усі три перевірено. Тепер обери.",info.centerX(),info.top+dp(57),text);drawAction(c,countInspected()==3?"ПЕРЕЙТИ ДО ВИБОРУ":"СПОЧАТКУ ПЕРЕВІР УСІ 3",countInspected()==3);}
            else{c.drawText("Який замет підходить для міцних куль?",info.centerX(),info.top+dp(30),text);text.setTextSize(tx(7.5f));text.setColor(Color.rgb(99,133,150));c.drawText("Натисни прямо на обраний замет.",info.centerX(),info.top+dp(57),text);drawAction(c,"ОБЕРИ ЗАМЕТ ВИЩЕ",false);}
        }

        String patchName(int i){return i==0?"Замет А":i==1?"Замет Б":"Замет В";} String patchResult(int i){return i==0?"твердий лід усередині":i==1?"пухкий і сухий — ліпиться":"занадто мокрий";}
        void drawPatch(Canvas c,int i){RectF r=patches[i];p.setColor(i==0?Color.rgb(205,235,246):i==1?Color.WHITE:Color.rgb(221,239,245));c.drawRoundRect(r,dp(22),dp(22),p);stroke.setStrokeWidth(dp(1.5f));stroke.setColor(inspected[i]?Color.rgb(64,137,169):Color.argb(100,90,153,181));c.drawRoundRect(r,dp(22),dp(22),stroke);p.setColor(i==0?Color.argb(95,96,178,210):i==2?Color.argb(75,111,169,190):Color.argb(55,144,191,214));for(int k=0;k<8;k++){float x=r.left+r.width()*(.16f+((k*37)%70)/100f),y=r.top+r.height()*(.26f+((k*29)%50)/100f);c.drawCircle(x,y,dp(1.4f+(k%3)*.6f),p);}text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(47,86,108));c.drawText(patchName(i),r.centerX(),r.bottom-dp(14),text);if(inspected[i]){p.setColor(i==1?Color.rgb(58,144,108):Color.rgb(145,111,82));c.drawCircle(r.right-dp(16),r.top+dp(16),dp(8),p);text.setTextSize(tx(8));text.setColor(Color.WHITE);c.drawText(i==1?"✓":"!",r.right-dp(16),r.top+dp(19),text);}}

        void drawTrail(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;RectF field=new RectF(dp(18),safeTop+dp(172),w-dp(18),bottom-dp(91));p.setColor(Color.argb(165,255,255,255));c.drawRoundRect(field,dp(24),dp(24),p);
            for(RectF r:ice){p.setColor(Color.rgb(173,221,239));c.drawOval(r,p);stroke.setColor(Color.argb(140,86,165,197));stroke.setStrokeWidth(dp(1.2f));c.drawOval(r,stroke);stroke.setColor(Color.argb(120,255,255,255));stroke.setStrokeWidth(dp(2));c.drawLine(r.left+r.width()*.25f,r.centerY(),r.right-r.width()*.20f,r.centerY()-r.height()*.16f,stroke);}
            float mx=w-dp(55),my=safeTop+dp(225);drawMitten(c,mx,my,1f);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(54,110,139));c.drawText("?",mx,my+dp(42),text);if(Float.isNaN(snowX)){snowX=dp(58);snowY=bottom-dp(115);}drawSnowKid(c,snowX,snowY,dp(27),dragging?.5f:.1f);
            text.setTextSize(tx(8));text.setColor(Color.rgb(66,118,145));c.drawText("Обійди блакитний лід — маршрут обираєш сам",w/2,bottom-dp(95),text);drawAction(c,"ТЯГНИ СНІГОВИКА ДО ЗНАХІДКИ",false);
        }
        boolean hitsIce(float x,float y){float rr=dp(24);for(RectF r:ice){RectF ex=new RectF(r.left-rr*.4f,r.top-rr*.4f,r.right+rr*.4f,r.bottom+rr*.4f);if(ex.contains(x,y-dp(28)))return true;}return false;}

        void drawMittenStory(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;drawMitten(c,w*.50f,safeTop+dp(255),1.8f);RectF card=new RectF(dp(24),safeTop+dp(330),w-dp(24),Math.min(bottom-dp(100),safeTop+dp(470)));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(13));text.setColor(Color.rgb(43,82,108));c.drawText("СИНЯ РУКАВИЧКА",card.centerX(),card.top+dp(34),text);text.setTextSize(tx(8.5f));text.setColor(Color.rgb(90,127,146));c.drawText("На ній вишита маленька сніжинка.",card.centerX(),card.top+dp(63),text);c.drawText("Власника тут немає — це загадка на майбутнє.",card.centerX(),card.top+dp(87),text);text.setTextSize(tx(7));text.setColor(Color.rgb(126,143,152));c.drawText("Це знахідка, а не заміна шарфа чи рук.",card.centerX(),card.top+dp(113),text);drawAction(c,"ЗАБРАТИ, ЩОБ ПОВЕРНУТИ ВЛАСНИКУ",true);
        }

        void drawReady(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.68f;drawSnowKid(c,w*.5f,ground,dp(39),.55f);drawMagnifier(c,w*.66f,ground-dp(122),.8f);drawMitten(c,w*.32f,ground-dp(115),.72f);RectF card=new RectF(dp(24),safeTop+dp(176),w-dp(24),safeTop+dp(305));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(42,83,108));c.drawText("ДОСЛІДНИК ГОТОВИЙ",card.centerX(),card.top+dp(35),text);text.setTextSize(tx(8.5f));text.setColor(Color.rgb(89,127,147));c.drawText("Пухкий сніг знайдено • лід обійдено",card.centerX(),card.top+dp(64),text);c.drawText("Рукавичку збережено як пам'ять Року 2",card.centerX(),card.top+dp(88),text);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(55,130,104));c.drawText("Далі — ліплення. Без повторної посилки й вокзалу.",card.centerX(),card.top+dp(111),text);drawAction(c,"ЛІПИТИ СНІГОВИКА • РІК 2",true);
        }

        void drawAction(Canvas c,String label,boolean enabled){p.setColor(enabled?Color.rgb(35,106,153):Color.rgb(183,205,216));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.4f));text.setColor(Color.WHITE);c.drawText(label,action.centerX(),action.centerY()+dp(3.5f),text);}
        void drawFeedback(Canvas c){float w=getWidth(),top=safeTop+dp(151);RectF r=new RectF(dp(35),top,w-dp(35),top+dp(42));p.setColor(Color.argb(242,255,248,225));c.drawRoundRect(r,dp(16),dp(16),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.5f));text.setColor(Color.rgb(115,88,55));c.drawText(feedback,r.centerX(),r.centerY()+dp(3),text);}
        void drawMagnifier(Canvas c,float x,float y,float s){stroke.setColor(Color.rgb(61,105,129));stroke.setStrokeWidth(dp(4)*s);c.drawCircle(x,y,dp(18)*s,stroke);c.drawLine(x+dp(13)*s,y+dp(13)*s,x+dp(31)*s,y+dp(31)*s,stroke);}
        void drawMitten(Canvas c,float x,float y,float s){p.setColor(Color.rgb(55,137,194));RectF palm=new RectF(x-dp(17)*s,y-dp(18)*s,x+dp(17)*s,y+dp(20)*s);c.drawRoundRect(palm,dp(9)*s,dp(9)*s,p);c.drawRoundRect(new RectF(x+dp(10)*s,y-dp(5)*s,x+dp(27)*s,y+dp(9)*s),dp(7)*s,dp(7)*s,p);p.setColor(Color.WHITE);for(int i=0;i<6;i++){double a=i*Math.PI/3;float x2=x+(float)Math.cos(a)*dp(8)*s,y2=y+(float)Math.sin(a)*dp(8)*s;c.drawCircle(x2,y2,dp(1.5f)*s,p);}c.drawCircle(x,y,dp(2.1f)*s,p);}
        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snowBall(c,x,by,br);snowBall(c,x,my,mr);snowBall(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.65f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.62f,my,x-mr*1.38f,my-mr*(.25f+.15f*wave),stroke);c.drawLine(x+mr*.62f,my,x+mr*1.38f,my-mr*(.25f+.20f*wave),stroke);}
        void snowBall(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(198,226,240)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);}

        void finishStory(){prefs.edit().putBoolean("year2_story_complete",true).putBoolean("year2_mitten_found",true).apply();Intent i=new Intent(ctx,MainActivity.class);i.putExtra("skip_year2_story",true);ctx.startActivity(i);((Activity)ctx).finish();}

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){if(stage==TRAIL&&dist(x,y,snowX,snowY-dp(55))<dp(65)){dragging=true;return true;}return true;}
            if(e.getAction()==MotionEvent.ACTION_MOVE&&stage==TRAIL&&dragging){float nx=clamp(x,dp(36),getWidth()-dp(36)),ny=clamp(y+dp(55),safeTop+dp(245),getHeight()-safeBottom-dp(100));if(hitsIce(nx,ny)){mistakes++;snowX=dp(58);snowY=getHeight()-safeBottom-dp(115);dragging=false;say("Обережно: лід! Повертаємось на безпечний сніг.",false);return true;}snowX=nx;snowY=ny;float mx=getWidth()-dp(55),my=safeTop+dp(225);if(dist(snowX,snowY-dp(55),mx,my)<dp(54)){dragging=false;tone(ToneGenerator.TONE_PROP_ACK,120);buzz(35);next(MITTEN);}invalidate();return true;}
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){performClick();if(stage==TRAIL){dragging=false;return true;}if(stage==INTRO&&action.contains(x,y)){next(INSPECT);return true;}if(stage==INSPECT){for(int i=0;i<3;i++)if(patches[i].contains(x,y)){inspected[i]=true;lastPatch=i;tone(ToneGenerator.TONE_PROP_BEEP,55);buzz(12);invalidate();return true;}if(action.contains(x,y)&&countInspected()==3){next(CHOOSE);return true;}}else if(stage==CHOOSE){for(int i=0;i<3;i++)if(patches[i].contains(x,y)){if(i==1){tone(ToneGenerator.TONE_PROP_ACK,120);buzz(30);next(TRAIL);}else{mistakes++;say(i==0?"Тут лід — куля буде ковзати.":"Надто мокрий — куля розповзеться.",false);}return true;}}else if(stage==MITTEN&&action.contains(x,y)){prefs.edit().putBoolean("year2_mitten_found",true).apply();next(READY);return true;}else if(stage==READY&&action.contains(x,y)){finishStory();return true;}return true;}return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
        @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(tone!=null){try{tone.release();}catch(Exception ignored){}tone=null;}}
    }
}
