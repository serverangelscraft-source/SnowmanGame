package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class DeliveryActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.rgb(220,239,248));
            w.setNavigationBarColor(Color.rgb(239,248,252));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new DeliveryView(this));
    }

    static class DeliveryView extends View {
        static final int PACKAGE=0, OPENED=1, RIDE=2, ARRIVED=3;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Context ctx;
        final float density,textScale;
        final RectF action=new RectF();
        int stage=PACKAGE;
        float safeTop,safeBottom;
        long stageStart=SystemClock.elapsedRealtime();

        DeliveryView(Context c){
            super(c);ctx=c;
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.16f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=20){
                        safeTop=insets.getSystemWindowInsetTop();
                        safeBottom=insets.getSystemWindowInsetBottom();
                    }
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

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float t=(SystemClock.elapsedRealtime()-stageStart)/1000f;
            drawWinter(c,t);
            if(stage==PACKAGE)drawPackage(c,t);
            else if(stage==OPENED)drawOpened(c,t);
            else if(stage==RIDE)drawRide(c,t);
            else drawArrived(c,t);
            if(stage==RIDE&&t>5.2f)switchStage(ARRIVED);
            postInvalidateOnAnimation();
        }

        void drawWinter(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient sky=new LinearGradient(0,safeTop,0,bottom*.72f,Color.rgb(158,216,246),Color.rgb(231,248,255),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(242,250,254));c.drawRect(0,bottom*.68f,w,h,p);
            p.setColor(Color.argb(235,255,255,255));
            c.drawOval(new RectF(-w*.36f,bottom*.52f,w*.62f,bottom*.77f),p);
            c.drawOval(new RectF(w*.32f,bottom*.55f,w*1.28f,bottom*.80f),p);
            for(int i=0;i<25;i++){
                float x=(i*97f+31f+(float)Math.sin(t*.35f+i)*dp(5))%Math.max(1,w);
                float y=(i*61f+t*dp(12+i%4))%Math.max(dp(120),bottom*.70f);
                p.setColor(Color.argb(120+(i%3)*30,255,255,255));c.drawCircle(x,y,dp(1+(i%3)*.4f),p);
            }
        }

        void drawHeader(Canvas c,String title,String sub){
            float w=getWidth(),top=safeTop+dp(10);
            RectF card=new RectF(dp(14),top,w-dp(14),top+dp(100));
            p.setColor(Color.argb(243,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(17));text.setColor(Color.rgb(30,72,99));c.drawText(title,card.left+dp(18),card.top+dp(32),text);
            text.setTextSize(tx(8.5f));text.setColor(Color.rgb(91,128,149));c.drawText(sub,card.left+dp(18),card.top+dp(57),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(6.6f));text.setColor(Color.rgb(128,143,151));c.drawText("ДЕМО-КОЛАБ • НЕОФІЦІЙНО",card.right-dp(18),card.top+dp(80),text);
        }

        void drawPackage(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            drawHeader(c,"Посилка для сніговика","До вокзалу далеко — потрібен транспорт");
            float arrive=smooth(t/1.5f);
            float px=mix(w+dp(100),w*.63f,arrive),py=bottom*.55f;
            drawSnowman(c,w*.24f,bottom*.69f,dp(35),0);
            drawParcel(c,px,py,1f);
            drawSpeech(c,w*.49f,bottom*.39f,t<1.2f?"Хто це їде до мене?":"Посилка! Може, там транспорт?");
            float bw=Math.min(w-dp(40),dp(340)),bh=dp(58),l=(w-bw)/2,top=bottom-dp(78);
            action.set(l,top,l+bw,top+bh);
            p.setColor(t>1.1f?Color.rgb(36,106,153):Color.rgb(169,199,215));c.drawRoundRect(action,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("ВІДКРИТИ ПОСИЛКУ",action.centerX(),action.centerY()+dp(4),text);
        }

        void drawParcel(Canvas c,float x,float y,float scale){
            float bw=dp(116)*scale,bh=dp(82)*scale;
            RectF box=new RectF(x-bw/2,y-bh/2,x+bw/2,y+bh/2);
            p.setColor(Color.rgb(224,35,45));c.drawRoundRect(box,dp(12)*scale,dp(12)*scale,p);
            p.setColor(Color.rgb(184,24,32));c.drawRect(x-dp(8)*scale,box.top,x+dp(8)*scale,box.bottom,p);
            RectF label=new RectF(x-dp(40)*scale,y-dp(18)*scale,x+dp(40)*scale,y+dp(18)*scale);
            p.setColor(Color.WHITE);c.drawRoundRect(label,dp(7)*scale,dp(7)*scale,p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8)*scale);text.setColor(Color.rgb(215,52,60));c.drawText("НОВА ПОШТА",x,y-dp(1)*scale,text);
            text.setTextSize(tx(5.4f)*scale);text.setColor(Color.rgb(110,122,128));c.drawText("ДЕМО-КОЛАБ",x,y+dp(12)*scale,text);
        }

        void drawOpened(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            drawHeader(c,"У посилці — санчата","Жовтий транспорт до залізничного вокзалу");
            float pop=smooth(t/.8f);
            drawSnowman(c,w*.23f,bottom*.70f,dp(34),.6f*pop);
            drawParcelOpen(c,w*.52f,bottom*.60f,pop);
            drawSled(c,w*.67f,bottom*.62f,Math.max(.05f,pop));
            RectF note=new RectF(dp(20),safeTop+dp(124),w-dp(20),safeTop+dp(218));
            p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(note,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(14));text.setColor(Color.rgb(45,85,108));c.drawText("ЖОВТІ САНЧАТА",note.centerX(),note.top+dp(31),text);
            text.setTextSize(tx(8.3f));text.setColor(Color.rgb(93,129,148));c.drawText("Тепер між двором і Укрзалізницею є свій транспорт.",note.centerX(),note.top+dp(55),text);
            text.setTextSize(tx(7.2f));text.setColor(Color.rgb(132,126,113));c.drawText("«Нова пошта» • демо-сюжет, неофіційна інтеграція",note.centerX(),note.top+dp(77),text);

            float bw=Math.min(w-dp(40),dp(340)),bh=dp(58),l=(w-bw)/2,top=bottom-dp(78);
            action.set(l,top,l+bw,top+bh);p.setColor(Color.rgb(36,106,153));c.drawRoundRect(action,dp(20),dp(20),p);
            text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("СІСТИ НА САНЧАТА",action.centerX(),action.centerY()+dp(4),text);
        }

        void drawParcelOpen(Canvas c,float x,float y,float pop){
            float bw=dp(92),bh=dp(58);RectF box=new RectF(x-bw/2,y-bh/2,x+bw/2,y+bh/2);
            p.setColor(Color.rgb(224,35,45));c.drawRoundRect(box,dp(9),dp(9),p);
            p.setColor(Color.rgb(184,24,32));
            Path lid=new Path();lid.moveTo(box.left,y-bh/2);lid.lineTo(x,y-bh*.78f-dp(18)*pop);lid.lineTo(box.right,y-bh/2);lid.close();c.drawPath(lid,p);
        }

        void drawSled(Canvas c,float x,float ground,float scale){
            float s=clamp(scale,.05f,1.2f);
            stroke.setStrokeWidth(dp(4)*s);stroke.setColor(Color.rgb(98,108,113));
            c.drawArc(new RectF(x-dp(60)*s,ground-dp(2)*s,x+dp(55)*s,ground+dp(24)*s),5,160,false,stroke);
            c.drawArc(new RectF(x-dp(45)*s,ground+dp(8)*s,x+dp(70)*s,ground+dp(30)*s),5,160,false,stroke);
            p.setColor(Color.rgb(248,202,36));
            c.drawRoundRect(new RectF(x-dp(55)*s,ground-dp(35)*s,x+dp(55)*s,ground-dp(8)*s),dp(8)*s,dp(8)*s,p);
            p.setColor(Color.rgb(225,173,24));
            for(int i=-2;i<=2;i++)c.drawRoundRect(new RectF(x+i*dp(20)*s-dp(7)*s,ground-dp(36)*s,x+i*dp(20)*s+dp(7)*s,ground-dp(6)*s),dp(3)*s,dp(3)*s,p);
        }

        void drawRide(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            drawHeader(c,"До вокзалу!","Санчата вперше везуть сніговика у велику подорож");
            float k=smooth(t/4.8f),roadY=bottom*.69f;
            p.setColor(Color.argb(90,96,148,170));
            for(int i=0;i<10;i++){
                float x=w-((t*dp(75)+i*dp(90))%(w+dp(100)));
                Path tree=new Path();tree.moveTo(x,roadY-dp(80));tree.lineTo(x-dp(25),roadY);tree.lineTo(x+dp(25),roadY);tree.close();c.drawPath(tree,p);
            }
            float sx=mix(w*.18f,w*.72f,k)+(float)Math.sin(t*3)*dp(4);
            drawSled(c,sx,roadY,1f);
            drawSnowman(c,sx-dp(4),roadY-dp(30),dp(28),.25f);
            stroke.setStrokeWidth(dp(2.5f));stroke.setColor(Color.rgb(113,80,52));c.drawLine(sx+dp(50),roadY-dp(18),sx+dp(95),roadY-dp(8),stroke);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12));text.setColor(Color.rgb(45,85,108));c.drawText(t<2.7f?"Ш-ш-ш… санчата ковзають по снігу":"Вже видно вокзал!",w/2,safeTop+dp(140),text);
            drawRoadProgress(c,k,bottom);
        }

        void drawRoadProgress(Canvas c,float k,float bottom){
            float w=getWidth(),l=dp(35),r=w-dp(35),y=bottom-dp(70);
            stroke.setStrokeWidth(dp(5));stroke.setColor(Color.rgb(213,231,240));c.drawLine(l,y,r,y,stroke);
            stroke.setColor(Color.rgb(55,132,170));c.drawLine(l,y,mix(l,r,k),y,stroke);
            p.setColor(Color.rgb(55,132,170));c.drawCircle(mix(l,r,k),y,dp(7),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7));text.setColor(Color.rgb(90,128,147));c.drawText("ДВІР",l,y-dp(13),text);
            text.setTextAlign(Paint.Align.RIGHT);c.drawText("ВОКЗАЛ",r,y-dp(13),text);
        }

        void drawArrived(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            drawHeader(c,"Приїхали на вокзал","Санчата виконали свою першу транспортну місію");
            float ground=bottom*.70f;
            drawStation(c,w,h);
            drawSled(c,w*.30f,ground,1f);
            drawSnowman(c,w*.30f,ground-dp(30),dp(29),.8f);
            RectF teaser=new RectF(dp(20),safeTop+dp(126),w-dp(20),safeTop+dp(222));
            p.setColor(Color.argb(240,255,255,255));c.drawRoundRect(teaser,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12));text.setColor(Color.rgb(45,84,107));c.drawText("ТРАНСПОРТ ВІДКРИТО: САНЧАТА",teaser.centerX(),teaser.top+dp(30),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(91,127,146));c.drawText("На старших рівнях шлях стане довшим.",teaser.centerX(),teaser.top+dp(54),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(49,112,91));c.drawText("Тизер: згодом можна буде найняти водія Uklon.",teaser.centerX(),teaser.top+dp(76),text);
            text.setTextSize(tx(6.5f));text.setColor(Color.rgb(131,143,149));c.drawText("Uklon • ДЕМО-ТИЗЕР • неофіційна інтеграція",teaser.centerX(),teaser.top+dp(91),text);

            float bw=Math.min(w-dp(40),dp(340)),bh=dp(58),l=(w-bw)/2,top=bottom-dp(78);
            action.set(l,top,l+bw,top+bh);p.setColor(Color.rgb(36,106,153));c.drawRoundRect(action,dp(20),dp(20),p);
            text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("УВІЙТИ НА ВОКЗАЛ",action.centerX(),action.centerY()+dp(4),text);
        }

        void drawStation(Canvas c,float w,float h){
            float bottom=h-safeBottom,l=w*.55f,top=bottom*.39f,r=w*.94f,b=bottom*.69f;
            p.setColor(Color.rgb(239,237,224));c.drawRoundRect(new RectF(l,top,r,b),dp(8),dp(8),p);
            p.setColor(Color.rgb(70,113,140));c.drawRect(l,top,r,top+dp(15),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.WHITE);c.drawText("ВОКЗАЛ",(l+r)/2,top+dp(11),text);
            p.setColor(Color.rgb(121,165,188));for(int i=0;i<3;i++){float x=l+dp(15+i*28);c.drawRect(x,top+dp(36),x+dp(17),top+dp(60),p);}
            p.setColor(Color.rgb(112,83,59));c.drawRect((l+r)/2-dp(13),b-dp(43),(l+r)/2+dp(13),b,p);
        }

        void drawSnowman(Canvas c,float x,float ground,float r,float wave){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;
            snowBall(c,x,by,br);snowBall(c,x,my,mr);snowBall(c,x,hy,hr);
            p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);
            Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.72f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);
            stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.62f,my,x-mr*1.40f,my-mr*.30f,stroke);c.drawLine(x+mr*.62f,my,x+mr*(1.35f+wave*.2f),my-mr*(.28f+.25f*wave),stroke);
        }
        void snowBall(Canvas c,float x,float y,float r){
            RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(198,226,240)},null,Shader.TileMode.CLAMP);
            p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);
        }

        void drawSpeech(Canvas c,float cx,float cy,String msg){
            float w=Math.min(getWidth()-dp(42),dp(330));RectF r=new RectF(cx-w*.5f,cy-dp(34),cx+w*.5f,cy+dp(30));
            p.setColor(Color.argb(240,255,255,255));c.drawRoundRect(r,dp(18),dp(18),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.5f));text.setColor(Color.rgb(54,93,116));c.drawText(msg,r.centerX(),r.centerY()+dp(3),text);
        }

        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();invalidate();}

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            performClick();
            float x=e.getX(),y=e.getY(),t=(SystemClock.elapsedRealtime()-stageStart)/1000f;
            if(!action.contains(x,y))return true;
            if(stage==PACKAGE&&t>1.1f){switchStage(OPENED);return true;}
            if(stage==OPENED){switchStage(RIDE);return true;}
            if(stage==ARRIVED){
                ctx.startActivity(new Intent(ctx,JourneyActivity.class));
                ((Activity)ctx).finish();
                return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
