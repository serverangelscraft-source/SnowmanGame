package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class UklonActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(244,248,250));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new UklonView(this));
    }

    static class UklonView extends View {
        static final int CALL=0, WAIT=1, BOARD=2, RIDE=3, ARRIVED=4;
        final Context ctx;
        final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF action=new RectF(),carRect=new RectF(),doorRect=new RectF();
        final Vibrator vibrator;
        final float density,textScale;
        final int year;
        int stage=CALL;
        float safeTop,safeBottom,snowX=Float.NaN,snowGround=Float.NaN;
        boolean dragging;
        long stageStart=SystemClock.elapsedRealtime();

        UklonView(Context c){
            super(c);ctx=c;
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            year=Math.max(3,Math.min(7,prefs.getInt("life_year",3)));
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.16f);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);
            setContentDescription("Демо-сцена виклику водія Uklon до вокзалу");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());safeTop=bars.top;safeBottom=bars.bottom;}
                    else{safeTop=insets.getSystemWindowInsetTop();safeBottom=insets.getSystemWindowInsetBottom();}
                    invalidate();return insets;
                }
            });
            requestApplyInsets();
        }

        float dp(float v){return v*density;}
        float tx(float v){return v*textScale;}
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        float smooth(float v){v=clamp(v,0,1);return v*v*(3-2*v);}
        float mix(float a,float b,float t){return a+(b-a)*t;}
        float dist(float x1,float y1,float x2,float y2){return(float)Math.hypot(x1-x2,y1-y2);}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,80));else vibrator.vibrate(ms);}
        String age(){switch(year){case 3:return"Пустун";case 4:return"Помічник";case 5:return"Майстер снігу";case 6:return"Майбутній школяр";default:return"Школяр";}}
        void next(int s){stage=s;stageStart=SystemClock.elapsedRealtime();dragging=false;invalidate();}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float t=(SystemClock.elapsedRealtime()-stageStart)/1000f;
            drawBackground(c,t);
            if(stage==CALL)drawCall(c,t);
            else if(stage==WAIT)drawWait(c,t);
            else if(stage==BOARD)drawBoard(c,t);
            else if(stage==RIDE)drawRide(c,t);
            else drawArrived(c,t);
            if(stage==WAIT&&t>3.1f){next(BOARD);return;}
            if(stage==RIDE&&t>5.3f){next(ARRIVED);return;}
            postInvalidateOnAnimation();
        }

        void drawBackground(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient sky=new LinearGradient(0,safeTop,0,bottom*.72f,Color.rgb(172,222,246),Color.rgb(236,249,255),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(247,251,253));c.drawRect(0,bottom*.67f,w,h,p);
            p.setColor(Color.argb(228,255,255,255));c.drawOval(new RectF(-w*.30f,bottom*.53f,w*.65f,bottom*.77f),p);c.drawOval(new RectF(w*.35f,bottom*.56f,w*1.24f,bottom*.80f),p);
            for(int i=0;i<18;i++){float x=(i*91f+37f+(float)Math.sin(t+i)*dp(4))%Math.max(1,w),y=(i*73f+t*dp(8+i%3))%Math.max(dp(120),bottom*.62f);p.setColor(Color.argb(120+(i%3)*30,255,255,255));c.drawCircle(x,y,dp(1+(i%3)*.35f),p);}
        }

        void drawTop(Canvas c,String title,String sub){
            float w=getWidth(),top=safeTop+dp(10);RectF card=new RectF(dp(14),top,w-dp(14),top+dp(108));
            p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.3f));text.setColor(Color.rgb(125,137,143));c.drawText("РІК "+year+" • "+age(),card.left+dp(17),card.top+dp(20),text);
            text.setTextSize(tx(18));text.setColor(Color.rgb(32,61,75));c.drawText(title,card.left+dp(17),card.top+dp(49),text);
            text.setTextSize(tx(8.2f));text.setColor(Color.rgb(89,118,133));c.drawText(sub,card.left+dp(17),card.top+dp(73),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(6.4f));text.setColor(Color.rgb(132,142,148));c.drawText("Uklon • ДЕМО-КОЛАБ • НЕОФІЦІЙНО",card.right-dp(17),card.top+dp(93),text);
        }

        void drawCall(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            drawTop(c,"Санчата вже замалі для маршруту","До вокзалу тепер далі — наймемо водія");
            drawSnowman(c,w*.22f,bottom*.70f,dp(31),.25f);
            drawSled(c,w*.22f,bottom*.70f+dp(8),.72f);
            RectF phone=new RectF(w*.40f,safeTop+dp(145),w-dp(24),bottom-dp(115));
            p.setColor(Color.rgb(31,34,38));c.drawRoundRect(phone,dp(25),dp(25),p);
            RectF screen=new RectF(phone.left+dp(7),phone.top+dp(8),phone.right-dp(7),phone.bottom-dp(8));p.setColor(Color.WHITE);c.drawRoundRect(screen,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(20));text.setColor(Color.rgb(252,190,24));c.drawText("Uklon",screen.left+dp(16),screen.top+dp(36),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(119,129,135));c.drawText("ІГРОВИЙ ВИКЛИК • 0 РЕАЛЬНИХ ГРН",screen.left+dp(16),screen.top+dp(56),text);
            text.setTextSize(tx(10));text.setColor(Color.rgb(45,64,75));c.drawText("ДВІР",screen.left+dp(16),screen.top+dp(91),text);c.drawText("ВОКЗАЛ",screen.left+dp(16),screen.top+dp(132),text);
            stroke.setColor(Color.rgb(252,190,24));stroke.setStrokeWidth(dp(3));c.drawLine(screen.left+dp(21),screen.top+dp(99),screen.left+dp(21),screen.top+dp(121),stroke);
            p.setColor(Color.rgb(252,190,24));c.drawCircle(screen.left+dp(21),screen.top+dp(90),dp(4),p);c.drawCircle(screen.left+dp(21),screen.top+dp(132),dp(4),p);
            text.setTextSize(tx(8));text.setColor(Color.rgb(87,111,124));c.drawText("Водій під’їде до сніговика",screen.left+dp(16),screen.bottom-dp(30),text);
            button(c,"ВИКЛИКАТИ ВОДІЯ UKLON",true);
        }

        void drawWait(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            drawTop(c,"Водій уже їде","Стій біля санчат — машина під’їде праворуч");
            drawSnowman(c,w*.20f,bottom*.70f,dp(31),.45f);drawSled(c,w*.20f,bottom*.70f+dp(8),.72f);
            float k=smooth(t/2.7f),roadY=bottom*.69f,cx=mix(w+dp(130),w*.65f,k);drawCar(c,cx,roadY,1f,false);
            RectF eta=new RectF(dp(30),safeTop+dp(140),w-dp(30),safeTop+dp(222));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(eta,dp(22),dp(22),p);
            int sec=Math.max(0,3-(int)t);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(44,68,81));c.drawText(sec>0?"ВОДІЙ ЗА "+sec+"…":"МАШИНА ПРИЇХАЛА",eta.centerX(),eta.top+dp(34),text);text.setTextSize(tx(8));text.setColor(Color.rgb(95,120,133));c.drawText("Маршрут: Двір → Вокзал",eta.centerX(),eta.top+dp(59),text);
        }

        void drawBoard(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.70f;
            drawTop(c,"Водій чекає","Перетягни сніговика до відкритих дверей");
            float carX=w*.66f;drawCar(c,carX,ground,1f,true);
            if(Float.isNaN(snowX)){snowX=w*.20f;snowGround=ground;}
            drawSnowman(c,snowX,snowGround,dp(29),dragging?.75f:.25f);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8));text.setColor(Color.rgb(62,103,124));c.drawText("Водій: «Сніговик? До вокзалу? Сідайте.»",w/2,safeTop+dp(146),text);
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeWidth(dp(2));stroke.setColor(Color.rgb(252,190,24));stroke.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(5)},0));c.drawRoundRect(doorRect,dp(12),dp(12),stroke);stroke.setPathEffect(null);
            button(c,"ПЕРЕТЯГНИ СНІГОВИКА В АВТО",false);
        }

        void drawRide(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,roadY=bottom*.69f,k=smooth(t/4.9f);
            drawTop(c,"Їдемо до вокзалу","Санчата лишилися вдома — сьогодні працює водій");
            p.setColor(Color.rgb(206,218,224));c.drawRect(0,roadY+dp(15),w,bottom,p);
            stroke.setColor(Color.WHITE);stroke.setStrokeWidth(dp(3));for(int i=0;i<5;i++){float x=w-((t*dp(130)+i*dp(120))%(w+dp(120)));c.drawLine(x,roadY+dp(42),x+dp(55),roadY+dp(42),stroke);}
            for(int i=0;i<7;i++){float x=w-((t*dp(72)+i*dp(100))%(w+dp(100)));p.setColor(Color.argb(110,83,128,146));Path tree=new Path();tree.moveTo(x,roadY-dp(74));tree.lineTo(x-dp(22),roadY);tree.lineTo(x+dp(22),roadY);tree.close();c.drawPath(tree,p);}
            drawCar(c,w*.52f+(float)Math.sin(t*2.2f)*dp(3),roadY,1.05f,false);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11));text.setColor(Color.rgb(44,74,91));c.drawText(t<2.4f?"Тепло. Сніговик трохи нервує…":"Водій: «Вокзал уже попереду.»",w/2,safeTop+dp(145),text);
            progress(c,k,bottom);
        }

        void drawArrived(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.70f;
            drawTop(c,"Приїхали","Нова транспортна ланка відкрита");
            drawStation(c,w,bottom);drawCar(c,w*.30f,ground,1f,false);drawSnowman(c,w*.53f,ground,dp(28),.8f);
            RectF card=new RectF(dp(24),safeTop+dp(132),w-dp(24),safeTop+dp(245));p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(card,dp(23),dp(23),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(38,63,76));c.drawText("ТРАНСПОРТ ВІДКРИТО: ВОДІЙ UKLON",card.centerX(),card.top+dp(34),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(89,118,133));c.drawText("Санчата залишаються пам’яттю Року 2.",card.centerX(),card.top+dp(61),text);c.drawText("З Року 3 довгі маршрути можна їхати автомобілем.",card.centerX(),card.top+dp(83),text);
            text.setTextSize(tx(6.6f));text.setColor(Color.rgb(133,143,148));c.drawText("Ігрова сцена • не є реальною послугою замовлення таксі",card.centerX(),card.top+dp(103),text);
            button(c,"УВІЙТИ НА ВОКЗАЛ",true);
        }

        void button(Canvas c,String label,boolean active){
            float w=getWidth(),bottom=getHeight()-safeBottom,bw=Math.min(w-dp(42),dp(350)),bh=dp(59),l=(w-bw)/2,top=bottom-dp(80);action.set(l,top,l+bw,top+bh);
            p.setColor(active?Color.rgb(252,190,24):Color.rgb(198,208,214));c.drawRoundRect(action,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(active?Color.rgb(33,39,43):Color.WHITE);c.drawText(label,action.centerX(),action.centerY()+dp(4),text);
        }

        void drawCar(Canvas c,float cx,float ground,float s,boolean doorOpen){
            float bw=dp(154)*s,bh=dp(54)*s,l=cx-bw/2,r=cx+bw/2,top=ground-bh-dp(18)*s,b=ground-dp(18)*s;
            carRect.set(l,top,r,b+dp(18)*s);p.setColor(Color.rgb(42,44,47));c.drawRoundRect(new RectF(l,top,r,b),dp(16)*s,dp(16)*s,p);
            Path roof=new Path();roof.moveTo(l+dp(34)*s,top);roof.lineTo(l+dp(60)*s,top-dp(28)*s);roof.lineTo(r-dp(38)*s,top-dp(28)*s);roof.lineTo(r-dp(18)*s,top);roof.close();p.setColor(Color.rgb(50,53,56));c.drawPath(roof,p);
            p.setColor(Color.rgb(176,215,232));c.drawRoundRect(new RectF(l+dp(48)*s,top-dp(23)*s,l+dp(78)*s,top-dp(3)*s),dp(4)*s,dp(4)*s,p);c.drawRoundRect(new RectF(l+dp(83)*s,top-dp(23)*s,r-dp(29)*s,top-dp(3)*s),dp(4)*s,dp(4)*s,p);
            p.setColor(Color.rgb(252,190,24));c.drawRoundRect(new RectF(l+dp(11)*s,top+dp(12)*s,l+dp(53)*s,top+dp(31)*s),dp(7)*s,dp(7)*s,p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8)*s);text.setColor(Color.rgb(34,39,42));c.drawText("Uklon",l+dp(32)*s,top+dp(26)*s,text);
            p.setColor(Color.rgb(29,32,35));c.drawCircle(l+dp(35)*s,ground-dp(18)*s,dp(17)*s,p);c.drawCircle(r-dp(35)*s,ground-dp(18)*s,dp(17)*s,p);p.setColor(Color.rgb(177,186,191));c.drawCircle(l+dp(35)*s,ground-dp(18)*s,dp(8)*s,p);c.drawCircle(r-dp(35)*s,ground-dp(18)*s,dp(8)*s,p);
            float dx=r-dp(57)*s;doorRect.set(dx-dp(22)*s,top-dp(2)*s,dx+dp(25)*s,b+dp(4)*s);
            if(doorOpen){stroke.setColor(Color.rgb(252,190,24));stroke.setStrokeWidth(dp(4)*s);c.drawLine(dx,top+dp(3)*s,dx+dp(32)*s,top-dp(12)*s,stroke);c.drawLine(dx+dp(32)*s,top-dp(12)*s,dx+dp(32)*s,b-dp(2)*s,stroke);}
            p.setColor(Color.rgb(68,56,45));c.drawCircle(l+dp(95)*s,top-dp(13)*s,dp(5)*s,p);p.setColor(Color.rgb(235,194,160));c.drawCircle(l+dp(95)*s,top-dp(12)*s,dp(4)*s,p);
        }

        void drawSled(Canvas c,float x,float ground,float s){stroke.setStrokeWidth(dp(3)*s);stroke.setColor(Color.rgb(99,108,113));c.drawArc(new RectF(x-dp(48)*s,ground-dp(2)*s,x+dp(45)*s,ground+dp(18)*s),5,160,false,stroke);p.setColor(Color.rgb(248,202,36));c.drawRoundRect(new RectF(x-dp(42)*s,ground-dp(28)*s,x+dp(42)*s,ground-dp(8)*s),dp(7)*s,dp(7)*s,p);}
        void drawSnowman(Canvas c,float x,float ground,float r,float wave){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.72f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.62f,my,x-mr*1.35f,my-mr*.30f,stroke);c.drawLine(x+mr*.62f,my,x+mr*(1.30f+wave*.2f),my-mr*(.28f+.2f*wave),stroke);}
        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(198,226,240)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);}
        void progress(Canvas c,float k,float bottom){float w=getWidth(),l=dp(36),r=w-dp(36),y=bottom-dp(70);stroke.setStrokeWidth(dp(5));stroke.setColor(Color.rgb(215,228,234));c.drawLine(l,y,r,y,stroke);stroke.setColor(Color.rgb(252,190,24));c.drawLine(l,y,mix(l,r,k),y,stroke);p.setColor(Color.rgb(252,190,24));c.drawCircle(mix(l,r,k),y,dp(7),p);text.setTextSize(tx(7));text.setColor(Color.rgb(88,111,124));text.setTextAlign(Paint.Align.LEFT);c.drawText("ДВІР",l,y-dp(13),text);text.setTextAlign(Paint.Align.RIGHT);c.drawText("ВОКЗАЛ",r,y-dp(13),text);}
        void drawStation(Canvas c,float w,float bottom){float l=w*.60f,top=bottom*.40f,r=w*.95f,b=bottom*.69f;p.setColor(Color.rgb(239,237,224));c.drawRoundRect(new RectF(l,top,r,b),dp(8),dp(8),p);p.setColor(Color.rgb(70,113,140));c.drawRect(l,top,r,top+dp(15),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8));text.setColor(Color.WHITE);c.drawText("ВОКЗАЛ",(l+r)/2,top+dp(11),text);p.setColor(Color.rgb(112,83,59));c.drawRect((l+r)/2-dp(12),b-dp(41),(l+r)/2+dp(12),b,p);}

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(stage==BOARD&&dist(x,y,snowX,snowGround-dp(50))<dp(66)){dragging=true;buzz(10);return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE&&stage==BOARD&&dragging){snowX=clamp(x,dp(32),getWidth()-dp(32));snowGround=clamp(y+dp(50),safeTop+dp(260),getHeight()-safeBottom-dp(95));invalidate();return true;}
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                performClick();
                if(stage==BOARD&&dragging){dragging=false;if(doorRect.contains(snowX,snowGround-dp(48))||dist(snowX,snowGround-dp(48),doorRect.centerX(),doorRect.centerY())<dp(55)){prefs.edit().putBoolean("uklon_unlocked",true).putInt("uklon_rides",prefs.getInt("uklon_rides",0)+1).apply();buzz(42);next(RIDE);}else{snowX=getWidth()*.20f;snowGround=(getHeight()-safeBottom)*.70f;buzz(12);invalidate();}return true;}
                if(stage==CALL&&action.contains(x,y)){buzz(22);next(WAIT);return true;}
                if(stage==ARRIVED&&action.contains(x,y)){ctx.startActivity(new Intent(ctx,JourneyActivity.class));((Activity)ctx).finish();return true;}
                return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
