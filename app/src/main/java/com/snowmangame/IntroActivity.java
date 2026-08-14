package com.snowmangame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class IntroActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.rgb(177,226,248));
            w.setNavigationBarColor(Color.rgb(238,249,255));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29) w.setNavigationBarContrastEnforced(false);
        setContentView(new StoryView());
    }

    final class StoryView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF skipRect=new RectF(), startRect=new RectF();
        final float density,textScale;
        final long storyStart=SystemClock.elapsedRealtime();
        boolean leaving=false;
        float safeTop=0,safeBottom=0;

        final float[] sceneStarts={0f,2.8f,4.8f,7.4f,10.4f,13.0f,15.6f};
        final String[] title={
                "Пішов перший сніг.",
                "Та вона була не сама.",
                "Сніжинок ставало більше.",
                "Друзі зібралися разом.",
                "І міцно взялися за ручки.",
                "Разом вони стали сильнішими…",
                "Так починається наш сніговик."
        };
        final String[] subtitle={
                "Одна сніжинка тихо опустилася на землю.",
                "Поруч прилетіла ще одна сніжинка-друг.",
                "Кожна знаходила поруч нових друзів.",
                "Вони вирішили триматися ближче один до одного.",
                "Ніхто не хотів відпускати друга.",
                "…і дружно перетворилися на снігову кулю.",
                "Тепер твоя черга допомогти сніжинкам-друзям."
        };

        StoryView(){
            super(IntroActivity.this);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.16f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    safeTop=insets.getSystemWindowInsetTop();
                    safeBottom=insets.getSystemWindowInsetBottom();
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
            float w=getWidth(),h=getHeight();
            float t=(SystemClock.elapsedRealtime()-storyStart)/1000f;
            drawWinter(c,w,h,t);
            drawFriends(c,w,h,t);
            drawStoryCloud(c,w,h,t);
            drawSkip(c,w);
            if(t>=15.6f) drawStart(c,w,h,t);
            if(!leaving) postInvalidateOnAnimation();
        }

        void drawWinter(Canvas c,float w,float h,float t){
            LinearGradient sky=new LinearGradient(0,0,0,h*.78f,Color.rgb(166,222,249),Color.rgb(226,246,255),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.argb(235,255,255,255));c.drawOval(new RectF(-w*.35f,h*.55f,w*.72f,h*.82f),p);
            p.setColor(Color.rgb(244,251,255));c.drawOval(new RectF(w*.24f,h*.58f,w*1.28f,h*.84f),p);
            p.setColor(Color.rgb(239,249,254));c.drawRect(0,h*.70f,w,h,p);
            p.setColor(Color.argb(48,56,112,133));
            for(int i=0;i<6;i++){
                float x=w*(.04f+i*.19f),y=h*(.69f+(i%2)*.012f),s=dp(14+(i%3)*4);
                Path tr=new Path();tr.moveTo(x,y-s*2.3f);tr.lineTo(x-s,y);tr.lineTo(x+s,y);tr.close();c.drawPath(tr,p);
            }
            for(int i=0;i<24;i++){
                float speed=dp(11+(i%6)*2);
                float yy=(i*dp(53)+t*speed)%(h*.76f);
                float xx=(i*dp(79)+dp(17)+(float)Math.sin(t*.45f+i)*dp(5))%Math.max(1f,w);
                p.setColor(Color.argb(82+(i%3)*26,255,255,255));c.drawCircle(xx,yy,dp(.8f+(i%4)*.34f),p);
            }
        }

        void drawFriends(Canvas c,float w,float h,float t){
            final int count=10;float cx=w/2f,groundY=h*.62f;
            float gather=smooth((t-7.2f)/2.2f),tighten=smooth((t-10.5f)/1.5f),becomeBall=smooth((t-12.8f)/2.1f);
            float[] xs=new float[count],ys=new float[count],alphas=new float[count];
            float ringR=Math.min(w*.25f,dp(92)),tightR=Math.min(w*.105f,dp(40));
            for(int i=0;i<count;i++){
                float appearAt=i==0?1f:(i==1?3.15f:4.35f+(i-2)*.30f);
                float appear=smooth((t-appearAt)/.75f);alphas[i]=appear;
                float startX=w*(.10f+((i*37)%80)/100f),startY=h*(.14f+((i*29)%22)/100f),fall=smooth((t-appearAt)/1.8f);
                double a=-Math.PI/2+i*Math.PI*2/count;
                float restX=cx+(float)Math.cos(a)*ringR*1.18f,restY=groundY+(float)Math.sin(a)*ringR*.62f;
                float ringX=cx+(float)Math.cos(a)*ringR,ringY=groundY+(float)Math.sin(a)*ringR*.56f;
                float tightX=cx+(float)Math.cos(a)*tightR,tightY=groundY+(float)Math.sin(a)*tightR*.72f;
                float x=mix(startX,restX,fall),y=mix(startY,restY,fall);
                x=mix(x,ringX,gather);y=mix(y,ringY,gather);x=mix(x,tightX,tighten);y=mix(y,tightY,tighten);
                xs[i]=x;ys[i]=y;
            }
            if(t>=.9f&&t<4.9f){
                if(t<3f) drawFocusHalo(c,xs[0],ys[0],dp(44),smooth((t-.9f)/.6f));
                else{float a=smooth((t-3f)/.55f);drawFocusHalo(c,xs[0],ys[0],dp(38),.72f);drawFocusHalo(c,xs[1],ys[1],dp(38),a);}
            }
            if(t>=9.7f&&becomeBall<.96f){
                float handIn=smooth((t-9.7f)/.7f);int alpha=(int)(220*handIn*(1-becomeBall));
                stroke.setColor(Color.argb(alpha,83,145,176));stroke.setStrokeWidth(dp(2.4f));
                for(int i=0;i<count;i++){int j=(i+1)%count;c.drawLine(xs[i],ys[i],xs[j],ys[j],stroke);}
            }
            int friendFade=(int)(255*(1-becomeBall));
            for(int i=0;i<count;i++){int alpha=(int)(friendFade*alphas[i]);if(alpha>5)drawFriendFlake(c,xs[i],ys[i],dp(i==0?16:13+(i%3)),alpha);}
            if(becomeBall>0){
                float r=mix(dp(18),Math.min(w*.18f,dp(72)),becomeBall);
                p.setColor(Color.argb((int)(55*becomeBall),70,132,160));c.drawOval(new RectF(cx-r*.92f,groundY+r*.55f,cx+r*.92f,groundY+r*.92f),p);
                RadialGradient g=new RadialGradient(cx-r*.30f,groundY-r*.34f,r*1.42f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(194,225,241)},new float[]{0,.57f,1},Shader.TileMode.CLAMP);
                p.setShader(g);c.drawCircle(cx,groundY,r,p);p.setShader(null);
                stroke.setColor(Color.argb((int)(115*becomeBall),98,161,191));stroke.setStrokeWidth(dp(1.2f));c.drawCircle(cx,groundY,r-dp(.5f),stroke);
                p.setColor(Color.argb((int)(165*becomeBall),255,255,255));
                for(int i=0;i<8;i++){double a=i*.91+t*.18;float rr=r*(.28f+(i%3)*.13f);c.drawCircle(cx+(float)Math.cos(a)*rr,groundY+(float)Math.sin(a)*rr,dp(1.4f+(i%2)*.6f),p);}
            }
        }

        void drawFocusHalo(Canvas c,float x,float y,float r,float alpha){
            RadialGradient halo=new RadialGradient(x,y,r,new int[]{Color.argb((int)(105*alpha),255,255,255),Color.argb(0,255,255,255)},null,Shader.TileMode.CLAMP);
            p.setShader(halo);c.drawCircle(x,y,r,p);p.setShader(null);
        }

        void drawFriendFlake(Canvas c,float x,float y,float r,int alpha){
            stroke.setColor(Color.argb(alpha,119,181,210));stroke.setStrokeWidth(Math.max(dp(1.3f),r*.10f));
            for(int k=0;k<6;k++){double a=k*Math.PI/3;float ex=x+(float)Math.cos(a)*r*1.22f,ey=y+(float)Math.sin(a)*r*1.22f;c.drawLine(x,y,ex,ey,stroke);}
            RadialGradient g=new RadialGradient(x-r*.25f,y-r*.30f,r*1.2f,new int[]{Color.WHITE,Color.rgb(239,249,255),Color.rgb(196,227,241)},null,Shader.TileMode.CLAMP);
            p.setShader(g);p.setAlpha(alpha);c.drawCircle(x,y,r,p);p.setShader(null);p.setAlpha(255);
            p.setColor(Color.argb(alpha,53,83,101));c.drawCircle(x-r*.27f,y-r*.14f,r*.075f,p);c.drawCircle(x+r*.27f,y-r*.14f,r*.075f,p);
            stroke.setColor(Color.argb(alpha,69,105,124));stroke.setStrokeWidth(Math.max(dp(1),r*.055f));c.drawArc(new RectF(x-r*.30f,y-r*.02f,x+r*.30f,y+r*.40f),20,140,false,stroke);
        }

        int sceneIndex(float t){int idx=0;for(int i=1;i<sceneStarts.length;i++)if(t>=sceneStarts[i])idx=i;return idx;}

        void drawStoryCloud(Canvas c,float w,float h,float t){
            int idx=sceneIndex(t);
            float blend=idx==0?smooth(t/.65f):smooth((t-sceneStarts[idx])/.70f);
            float cardW=Math.min(w-dp(24),dp(390));
            float cardH=dp(w<dp(360)?118:112);
            float left=(w-cardW)/2f;
            float desiredTop=h*.30f;
            float top=Math.max(safeTop+dp(58),desiredTop);
            float maxTop=h*.46f-cardH;
            if(maxTop>safeTop+dp(58)) top=Math.min(top,maxTop);
            RectF cloud=new RectF(left,top,left+cardW,top+cardH);

            int cloudAlpha=238;
            p.setColor(Color.argb(cloudAlpha,255,255,255));
            c.drawCircle(cloud.left+dp(40),cloud.top+dp(8),dp(25),p);
            c.drawCircle(cloud.left+dp(78),cloud.top+dp(2),dp(31),p);
            c.drawCircle(cloud.right-dp(72),cloud.top+dp(5),dp(28),p);
            c.drawRoundRect(cloud,dp(28),dp(28),p);

            Path tail=new Path();
            float tailX=cloud.centerX()+dp(24);
            tail.moveTo(tailX-dp(18),cloud.bottom-dp(3));
            tail.lineTo(tailX+dp(17),cloud.bottom-dp(3));
            tail.lineTo(cloud.centerX()+dp(6),Math.min(h*.54f,cloud.bottom+dp(38)));
            tail.close();c.drawPath(tail,p);

            stroke.setColor(Color.argb(62,72,135,164));stroke.setStrokeWidth(dp(1));c.drawRoundRect(cloud,dp(28),dp(28),stroke);

            if(idx>0&&blend<1f) drawTextBlock(c,cloud,idx-1,1f-blend,w);
            drawTextBlock(c,cloud,idx,blend,w);

            float dotGap=dp(11),start=cloud.centerX()-dotGap*(sceneStarts.length-1)/2f;
            for(int i=0;i<sceneStarts.length;i++){
                p.setColor(i==idx?Color.rgb(65,135,170):Color.rgb(207,226,236));
                c.drawCircle(start+i*dotGap,cloud.bottom-dp(11),dp(i==idx?2.5f:1.8f),p);
            }
        }

        void drawTextBlock(Canvas c,RectF cloud,int idx,float alpha,float w){
            int a=(int)(255*clamp(alpha,0,1));
            text.setTextAlign(Paint.Align.CENTER);text.setColor(Color.argb(a,34,75,101));text.setTextSize(tx(w<dp(360)?14.2f:16f));
            c.drawText(title[idx],cloud.centerX(),cloud.top+dp(34),text);
            text.setTextSize(tx(w<dp(360)?9.2f:10.5f));text.setColor(Color.argb(a,79,126,151));
            drawWrappedCentered(c,subtitle[idx],cloud.centerX(),cloud.top+dp(62),cloud.width()-dp(32),dp(16),a);
        }

        void drawWrappedCentered(Canvas c,String s,float cx,float y,float maxW,float lineH,int alpha){
            if(text.measureText(s)<=maxW){c.drawText(s,cx,y,text);return;}
            String[] words=s.split(" ");String first="",second="";
            for(String word:words){String trial=first.length()==0?word:first+" "+word;if(text.measureText(trial)<=maxW||first.length()==0)first=trial;else second+=second.length()==0?word:" "+word;}
            c.drawText(first,cx,y-dp(3),text);c.drawText(second,cx,y+lineH-dp(3),text);
        }

        void drawSkip(Canvas c,float w){
            float top=safeTop+dp(7);skipRect.set(w-dp(103),top,w-dp(9),top+dp(38));
            p.setColor(Color.argb(155,255,255,255));c.drawRoundRect(skipRect,dp(16),dp(16),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.7f));text.setColor(Color.rgb(74,119,144));c.drawText("ПРОПУСТИТИ",skipRect.centerX(),skipRect.centerY()+dp(3.5f),text);
        }

        void drawStart(Canvas c,float w,float h,float t){
            float intro=smooth((t-15.6f)/.7f),pulse=.985f+(float)Math.sin(t*2.2f)*.012f;
            float bw=Math.min(w-dp(42),dp(330))*pulse,bh=dp(58)*pulse,cx=w/2f,cy=h-safeBottom-dp(70);
            startRect.set(cx-bw/2,cy-bh/2,cx+bw/2,cy+bh/2);
            p.setColor(Color.argb((int)(255*intro),38,105,145));c.drawRoundRect(startRect,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12.5f));text.setColor(Color.argb((int)(255*intro),255,255,255));c.drawText("ПОЧАТИ ЛІПИТИ",startRect.centerX(),startRect.centerY()+dp(4.5f),text);
            text.setTextSize(tx(8.2f));text.setColor(Color.argb((int)(205*intro),75,125,150));c.drawText("Сніжинки-друзі чекають на тебе",cx,startRect.top-dp(13),text);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()==MotionEvent.ACTION_UP){
                performClick();float t=(SystemClock.elapsedRealtime()-storyStart)/1000f;
                if(skipRect.contains(e.getX(),e.getY())||(t>=15.6f&&startRect.contains(e.getX(),e.getY())))startGame();
                return true;
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
        void startGame(){if(leaving)return;leaving=true;startActivity(new Intent(IntroActivity.this,MainActivity.class));overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);finish();}
    }
}
