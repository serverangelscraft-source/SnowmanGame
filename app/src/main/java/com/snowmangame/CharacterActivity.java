package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class CharacterActivity extends Activity {
    @Override public void onCreate(Bundle b){
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
        setContentView(new ChoiceView(this));
    }

    static class ChoiceView extends View {
        final Context ctx;
        final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF boy=new RectF(),girl=new RectF(),go=new RectF();
        final float density,textScale;
        float safeTop,safeBottom;
        int selected=-1;

        ChoiceView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            selected=prefs.getInt("character_type",-1);
            setClickable(true);setFocusable(true);setContentDescription("Вибір персонажа сніговика");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    safeTop=insets.getSystemWindowInsetTop();safeBottom=insets.getSystemWindowInsetBottom();invalidate();return insets;
                }
            });requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(),bottom=getHeight()-safeBottom;
            LinearGradient bg=new LinearGradient(0,0,0,bottom,Color.rgb(169,224,248),Color.rgb(239,250,254),Shader.TileMode.CLAMP);p.setShader(bg);c.drawRect(0,0,w,getHeight(),p);p.setShader(null);
            p.setColor(Color.argb(235,255,255,255));c.drawOval(new RectF(-w*.35f,bottom*.58f,w*.72f,bottom*.84f),p);c.drawOval(new RectF(w*.28f,bottom*.62f,w*1.2f,bottom*.88f),p);

            RectF head=new RectF(dp(16),safeTop+dp(14),w-dp(16),safeTop+dp(116));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(head,dp(25),dp(25),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(18));text.setColor(Color.rgb(43,73,91));c.drawText("ХТО ПОВЕРТАЄТЬСЯ ВЗИМКУ?",head.left+dp(18),head.top+dp(38),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(99,128,142));c.drawText("Обери образ. Це не змінює складність або прогрес.",head.left+dp(18),head.top+dp(68),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(127,143,148));c.drawText("Потім вибір можна змінити в гардеробі.",head.left+dp(18),head.bottom-dp(17),text);

            float gap=dp(12),left=dp(16),right=w-dp(16),top=head.bottom+dp(18),cardH=Math.min(dp(350),(bottom-top-dp(92))*.78f),cw=(right-left-gap)/2f;
            boy.set(left,top,left+cw,top+cardH);girl.set(left+cw+gap,top,right,top+cardH);
            drawChoice(c,boy,0,"ХЛОПЧИК","Сніговичок");drawChoice(c,girl,1,"ДІВЧИНКА","Снігівчинка");

            float bh=dp(58);go.set(dp(24),bottom-bh-dp(14),w-dp(24),bottom-dp(14));p.setColor(selected>=0?Color.rgb(38,108,151):Color.rgb(181,201,211));c.drawRoundRect(go,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.WHITE);c.drawText(selected>=0?"ЦЕ МІЙ СНІГОВИК":"СПОЧАТКУ ОБЕРИ",go.centerX(),go.centerY()+dp(4),text);
        }

        void drawChoice(Canvas c,RectF r,int type,String title,String sub){
            boolean active=selected==type;p.setColor(active?Color.rgb(246,253,255):Color.argb(239,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);
            if(active){stroke.setColor(Color.rgb(46,133,176));stroke.setStrokeWidth(dp(2.4f));c.drawRoundRect(r,dp(24),dp(24),stroke);}
            float x=r.centerX(),s=Math.min(r.width()*.31f,dp(48)),by=r.top+r.height()*.55f,mr=s*.72f,hr=s*.54f,my=by-(s+mr)*.83f,hy=my-(mr+hr)*.83f;
            drawBall(c,x,by,s);drawBall(c,x,my,mr);drawBall(c,x,hy,hr);drawFace(c,x,hy,hr,type);
            stroke.setColor(Color.rgb(108,80,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.55f,my,x-mr*1.45f,my-mr*.42f,stroke);c.drawLine(x+mr*.55f,my,x+mr*1.45f,my-mr*.42f,stroke);
            if(type==0){p.setColor(Color.rgb(45,73,103));c.drawRoundRect(new RectF(x-hr*.52f,hy-hr*.88f,x+hr*.52f,hy-hr*.54f),dp(6),dp(6),p);p.setColor(Color.rgb(61,147,190));c.drawRect(x-hr*.5f,hy-hr*.67f,x+hr*.5f,hy-hr*.58f,p);}else drawBow(c,x+hr*.56f,hy-hr*.62f,hr*.23f);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.rgb(44,83,105));c.drawText(title,x,r.bottom-dp(50),text);text.setTextSize(tx(8));text.setColor(Color.rgb(103,133,147));c.drawText(sub,x,r.bottom-dp(27),text);
        }

        void drawBall(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);}
        void drawFace(Canvas c,float x,float y,float r,int type){p.setColor(Color.rgb(43,57,66));c.drawCircle(x-r*.27f,y-r*.16f,r*.075f,p);c.drawCircle(x+r*.27f,y-r*.16f,r*.075f,p);stroke.setColor(Color.rgb(58,79,91));stroke.setStrokeWidth(dp(1.3f));if(type==1){c.drawLine(x-r*.36f,y-r*.23f,x-r*.43f,y-r*.31f,stroke);c.drawLine(x+r*.36f,y-r*.23f,x+r*.43f,y-r*.31f,stroke);}else{c.drawLine(x-r*.37f,y-r*.30f,x-r*.18f,y-r*.34f,stroke);c.drawLine(x+r*.18f,y-r*.34f,x+r*.37f,y-r*.30f,stroke);}Path n=new Path();n.moveTo(x,y);n.lineTo(x+r*.68f,y+r*.06f);n.lineTo(x,y+r*.13f);n.close();p.setColor(Color.rgb(240,118,34));c.drawPath(n,p);c.drawArc(new RectF(x-r*.20f,y+r*.09f,x+r*.20f,y+r*.33f),15,150,false,stroke);}
        void drawBow(Canvas c,float x,float y,float s){p.setColor(Color.rgb(202,65,83));Path a=new Path();a.moveTo(x,y);a.lineTo(x-s*1.5f,y-s*.8f);a.lineTo(x-s*1.35f,y+s*.8f);a.close();c.drawPath(a,p);Path b=new Path();b.moveTo(x,y);b.lineTo(x+s*1.5f,y-s*.8f);b.lineTo(x+s*1.35f,y+s*.8f);b.close();c.drawPath(b,p);c.drawCircle(x,y,s*.45f,p);}

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();
            if(boy.contains(x,y)){selected=0;invalidate();return true;}if(girl.contains(x,y)){selected=1;invalidate();return true;}
            if(go.contains(x,y)&&selected>=0){prefs.edit().putInt("character_type",selected).putBoolean("character_selected",true).apply();ctx.startActivity(new Intent(ctx,MainActivity.class));((Activity)ctx).finish();}
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
