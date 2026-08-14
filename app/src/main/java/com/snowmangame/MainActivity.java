package com.snowmangame;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.os.SystemClock;
import android.os.Vibrator;
import android.os.VibrationEffect;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.rgb(235, 247, 253));
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            window.getDecorView().setSystemUiVisibility(flags);
        }
        if (Build.VERSION.SDK_INT >= 29) window.setNavigationBarContrastEnforced(false);
        setContentView(new SnowmanView(this));
    }

    static class SnowmanView extends View {
        static final int EYES=0, NOSE=1, BUTTONS=2, SCARF=3, HAT=4, ARMS=5, ACCESSORY_COUNT=6;

        final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Paint stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        final Random rnd = new Random();
        final ArrayList<Dust> dust = new ArrayList<>();
        final Accessory[] accessories = new Accessory[ACCESSORY_COUNT];
        final float density, textScale;
        final Vibrator vib;
        final SharedPreferences prefs;

        float safeTop, safeBottom;
        boolean compact, narrow, finished;
        int balls, score, bestScore, qualitySum, decorQualitySum, decorPlaced, combo, maxCombo;
        String tip = "Коти сніг пальцем — зроби першу кулю";

        float rollProgress, rollX=Float.NaN, rollY=Float.NaN, lastX, lastY, lastDx, lastDy, turnPenalty;
        boolean rolling, draggingBall, ballReady;
        int draggingAccessory = -1;
        float dragAccessoryX, dragAccessoryY;

        long startTime;
        int finishElapsed, mission;
        boolean missionSuccess;

        boolean sponsorScene = false;
        boolean sponsorRewarded = false;
        long sponsorStart = 0L;

        final RectF hudRect = new RectF();
        final RectF tipRect = new RectF();
        final RectF interactionRect = new RectF();
        final RectF finishBtn = new RectF();
        final RectF restartBtn = new RectF();
        final RectF sponsorBtn = new RectF();
        final RectF sponsorCloseBtn = new RectF();

        float playTop, playBottom, baseR, midR, headR, baseY, midY, headY, targetX, targetY, targetR;

        SnowmanView(Context c) {
            super(c);
            density = getResources().getDisplayMetrics().density;
            textScale = Math.min(getResources().getDisplayMetrics().scaledDensity, density * 1.18f);
            vib = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            prefs = c.getSharedPreferences("snowman_game", Context.MODE_PRIVATE);
            bestScore = prefs.getInt("best_score", 0);
            text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeWidth(dp(2));
            setFocusable(true);
            setClickable(true);
            setContentDescription("Гра Зліпи сніговика");
            String[] names = {"Очі", "Морква", "Ґудзики", "Шарф", "Шапка", "Руки"};
            for (int i=0;i<ACCESSORY_COUNT;i++) accessories[i] = new Accessory(i, names[i]);
            mission = rnd.nextInt(3);
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener() {
                @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                    if (Build.VERSION.SDK_INT >= 30) {
                        Insets bars = insets.getInsets(WindowInsets.Type.systemBars());
                        safeTop = bars.top;
                        safeBottom = bars.bottom;
                    } else {
                        safeTop = insets.getSystemWindowInsetTop();
                        safeBottom = insets.getSystemWindowInsetBottom();
                    }
                    invalidate();
                    return insets;
                }
            });
            requestApplyInsets();
        }

        float dp(float v){ return v*density; }
        float tx(float v){ return v*textScale; }
        float clamp(float v,float a,float b){ return Math.max(a,Math.min(b,v)); }
        float dist(float x1,float y1,float x2,float y2){ return (float)Math.hypot(x1-x2,y1-y2); }
        float smooth(float v){ v=clamp(v,0f,1f); return v*v*(3f-2f*v); }
        float mix(float a,float b,float t){ return a+(b-a)*t; }
        void vibrate(int ms){
            if(vib==null||!vib.hasVibrator()) return;
            if(Build.VERSION.SDK_INT>=26) vib.vibrate(VibrationEffect.createOneShot(ms,85)); else vib.vibrate(ms);
        }
        void ensureTimer(){ if(startTime==0) startTime=SystemClock.elapsedRealtime(); }
        int elapsedSeconds(){ if(startTime==0)return 0; if(finished)return finishElapsed; return (int)((SystemClock.elapsedRealtime()-startTime)/1000L); }
        int avgBuild(){ return balls==0?0:qualitySum/balls; }
        int avgDecor(){ return decorPlaced==0?0:decorQualitySum/decorPlaced; }

        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);
            layoutGame();
            if (sponsorScene) {
                drawSponsorScene(c);
                postInvalidateOnAnimation();
                return;
            }
            drawBackground(c);
            drawHud(c);
            drawTip(c);
            drawSnowman(c);
            if(!finished) {
                if(balls<3) drawRollingArea(c); else drawDecorationTray(c);
            }
            drawDust(c);
            if(finished) drawFinishOverlay(c); else if(startTime!=0) postInvalidateDelayed(500);
        }

        void layoutGame() {
            float w=getWidth(), h=getHeight(), bottom=h-safeBottom, usableH=Math.max(dp(420),bottom-safeTop);
            compact=usableH<dp(650); narrow=w<dp(360);
            float margin=narrow?dp(9):dp(13), hudH=compact?dp(62):dp(70), tipH=compact?dp(34):dp(40);
            float interactionH=balls<3?(compact?dp(102):dp(124)):(compact?dp(130):dp(154));
            hudRect.set(margin,safeTop+dp(8),w-margin,safeTop+dp(8)+hudH);
            tipRect.set(margin,hudRect.bottom+dp(7),w-margin,hudRect.bottom+dp(7)+tipH);
            interactionRect.set(margin,bottom-interactionH-dp(7),w-margin,bottom-dp(7));
            playTop=tipRect.bottom+dp(5); playBottom=interactionRect.top-dp(6);
            float playH=Math.max(dp(220),playBottom-playTop);
            baseR=clamp(Math.min(w*.205f,playH/4.02f),dp(34),dp(82));
            midR=baseR*.72f; headR=baseR*.54f;
            baseY=playBottom-baseR-dp(5); midY=baseY-(baseR+midR)*.84f; headY=midY-(midR+headR)*.84f;
            targetX=w/2f;
            if(balls==0){targetY=baseY;targetR=baseR;} else if(balls==1){targetY=midY;targetR=midR;} else {targetY=headY;targetR=headR;}
            setAccessoryTargets();
            layoutAccessorySlots();
            if(Float.isNaN(rollX)||Float.isNaN(rollY)) resetRollingBallPosition();
            if(!draggingBall&&balls<3) keepRollingBallInsideZone();
        }

        void setAccessoryTargets(){
            float cx=getWidth()/2f;
            setTarget(EYES,cx,headY-headR*.16f);
            setTarget(NOSE,cx,headY+headR*.05f);
            setTarget(BUTTONS,cx,midY+midR*.02f);
            setTarget(SCARF,cx,midY-midR*.73f);
            setTarget(HAT,cx,headY-headR*1.28f);
            setTarget(ARMS,cx,midY-midR*.08f);
        }
        void setTarget(int type,float x,float y){ accessories[type].targetX=x; accessories[type].targetY=y; }
        void layoutAccessorySlots(){
            if(balls<3)return;
            float gap=dp(6),pad=dp(8),slotW=(interactionRect.width()-pad*2-gap*2)/3f,slotH=(interactionRect.height()-pad*2-gap)/2f;
            for(int i=0;i<ACCESSORY_COUNT;i++){
                int row=i/3,col=i%3;
                float left=interactionRect.left+pad+col*(slotW+gap),top=interactionRect.top+pad+row*(slotH+gap);
                accessories[i].slot.set(left,top,left+slotW,top+slotH);
            }
            finishBtn.set(interactionRect.left+dp(28),interactionRect.top+dp(22),interactionRect.right-dp(28),interactionRect.bottom-dp(22));
        }
        void resetRollingBallPosition(){ rollX=interactionRect.centerX(); rollY=interactionRect.centerY()+dp(3); }
        float rollingRadius(){ if(balls>=3)return 0; float start=dp(13),max=Math.min(targetR*.72f,interactionRect.height()*.36f); return start+(max-start)*clamp(rollProgress/100f,0,1); }
        void keepRollingBallInsideZone(){
            float r=Math.max(dp(13),rollingRadius());
            rollX=clamp(rollX,interactionRect.left+r+dp(3),interactionRect.right-r-dp(3));
            rollY=clamp(rollY,interactionRect.top+r+dp(3),interactionRect.bottom-r-dp(3));
        }

        void drawBackground(Canvas c){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient sky=new LinearGradient(0,safeTop,0,bottom*.66f,Color.rgb(156,217,247),Color.rgb(225,246,255),Shader.TileMode.CLAMP);
            p.setShader(sky); c.drawRect(0,0,w,h,p); p.setShader(null);
            p.setColor(Color.argb(220,255,255,255)); c.drawOval(new RectF(-w*.42f,bottom*.39f,w*.68f,bottom*.69f),p);
            p.setColor(Color.argb(238,247,252,255)); c.drawOval(new RectF(w*.22f,bottom*.46f,w*1.34f,bottom*.73f),p);
            p.setColor(Color.rgb(239,249,254)); c.drawRect(0,bottom*.62f,w,h,p);
            p.setColor(Color.argb(56,55,111,132));
            for(int i=0;i<5;i++){
                float x=w*(.07f+i*.235f),y=bottom*(.58f+(i%2)*.016f),s=dp(14+(i%3)*4);
                Path tree=new Path(); tree.moveTo(x,y-s*2.3f); tree.lineTo(x-s,y); tree.lineTo(x+s,y); tree.close(); c.drawPath(tree,p);
            }
            p.setColor(Color.argb(145,255,255,255));
            int flakes=compact?18:26; float range=Math.max(dp(120),playBottom-safeTop);
            for(int i=0;i<flakes;i++){
                float x=(i*139f+31f)%Math.max(1f,w),y=safeTop+dp(16)+((i*197f)%range);
                c.drawCircle(x,y,dp(1.1f+(i%3)*.45f),p);
            }
        }

        String formatTime(int sec){ return String.format("%d:%02d",sec/60,sec%60); }
        String missionText(){ if(mission==0)return"МІСІЯ: точність куль ≥ 85%"; if(mission==1)return"МІСІЯ: завершити ≤ 90 с"; return"МІСІЯ: декор ≥ 85%"; }
        void drawHud(Canvas c){
            RectF r=hudRect; p.setColor(Color.argb(242,255,255,255)); c.drawRoundRect(r,dp(21),dp(21),p);
            text.setTextAlign(Paint.Align.LEFT); text.setColor(Color.rgb(38,69,89)); text.setTextSize(tx(narrow?9:10));
            String stage=balls<3?"КУЛЯ "+(balls+1)+"/3":"ДЕКОР "+decorPlaced+"/6";
            c.drawText(stage,r.left+dp(15),r.top+dp(20),text);
            text.setTextSize(tx(narrow?15:17)); c.drawText(balls<3?(int)rollProgress+"%":avgDecor()+"%",r.left+dp(15),r.bottom-dp(14),text);
            float barLeft=r.left+(narrow?dp(76):dp(88)),barRight=r.right-dp(105),barTop=r.centerY()+dp(1),barH=dp(9);
            RectF bar=new RectF(barLeft,barTop,Math.max(barLeft+dp(24),barRight),barTop+barH);
            p.setColor(Color.rgb(220,237,246)); c.drawRoundRect(bar,barH/2,barH/2,p);
            float progress=balls<3?rollProgress/100f:decorPlaced/6f;
            RectF fill=new RectF(bar.left,bar.top,bar.left+bar.width()*clamp(progress,0,1),bar.bottom);
            p.setColor((ballReady||decorPlaced==6)?Color.rgb(69,157,127):Color.rgb(57,136,180)); if(fill.width()>dp(1))c.drawRoundRect(fill,barH/2,barH/2,p);
            text.setTextAlign(Paint.Align.RIGHT); text.setColor(Color.rgb(31,74,104)); text.setTextSize(tx(narrow?12.5f:14)); c.drawText("★ "+score,r.right-dp(14),r.top+dp(23),text);
            text.setTextSize(tx(narrow?8:9)); text.setColor(Color.rgb(105,133,149)); c.drawText(formatTime(elapsedSeconds())+"   РЕК "+bestScore,r.right-dp(14),r.bottom-dp(13),text);
        }
        void drawTip(Canvas c){
            p.setColor(Color.argb(210,234,247,254)); c.drawRoundRect(tipRect,dp(17),dp(17),p);
            text.setTextAlign(Paint.Align.CENTER); text.setTextSize(tx(narrow?9.6f:10.8f)); text.setColor(Color.rgb(43,82,108)); c.drawText(tip,tipRect.centerX(),tipRect.centerY()-dp(1),text);
            text.setTextSize(tx(narrow?7.2f:8.2f)); text.setColor(Color.rgb(92,137,159)); c.drawText(missionText(),tipRect.centerX(),tipRect.centerY()+dp(11),text);
        }

        void drawSnowman(Canvas c){
            float cx=getWidth()/2f;
            p.setColor(Color.argb(42,68,129,157)); c.drawOval(new RectF(cx-baseR*.92f,baseY+baseR*.69f,cx+baseR*.92f,baseY+baseR*1.05f),p);
            if(balls>=1)drawBall(c,cx,baseY,baseR,1);
            if(balls>=2)drawBall(c,cx,midY,midR,2);
            if(balls>=3)drawBall(c,cx,headY,headR,3);
            if(balls<3){
                stroke.setStrokeWidth(dp(2)); stroke.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(6)},0)); stroke.setColor(Color.argb(ballReady?190:90,52,126,163)); c.drawCircle(targetX,targetY,targetR,stroke); stroke.setPathEffect(null);
                if(ballReady){ text.setTextAlign(Paint.Align.CENTER); text.setTextSize(tx(9)); text.setColor(Color.rgb(65,123,151)); c.drawText("ПЕРЕТЯГНИ СЮДИ",targetX,targetY,text); }
            }
            for(Accessory a:accessories) if(a.placed) drawAccessoryPreview(c,a.type,a.x,a.y,255);
            if(draggingAccessory>=0){ Accessory a=accessories[draggingAccessory]; drawTargetHint(c,a); drawAccessoryPreview(c,a.type,dragAccessoryX,dragAccessoryY,235); }
        }
        void drawBall(Canvas c,float x,float y,float r,int seed){
            RadialGradient g=new RadialGradient(x-r*.31f,y-r*.37f,r*1.42f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},new float[]{0,.57f,1},Shader.TileMode.CLAMP);
            p.setShader(g); c.drawCircle(x,y,r,p); p.setShader(null);
            stroke.setColor(Color.argb(82,101,162,193)); stroke.setStrokeWidth(dp(1)); c.drawCircle(x,y,r-dp(.5f),stroke);
            p.setColor(Color.argb(55,136,186,210));
            for(int i=0;i<7;i++){
                double a=i*2.22+seed*.8; float rr=r*(.23f+((i*31+seed*17)%49)/100f),px=x+(float)Math.cos(a)*rr,py=y+(float)Math.sin(a)*rr;
                c.drawCircle(px,py,Math.max(dp(.8f),r*.018f),p);
            }
            p.setColor(Color.argb(170,255,255,255)); c.drawOval(new RectF(x-r*.52f,y-r*.58f,x-r*.08f,y-r*.28f),p);
        }

        void drawRollingArea(Canvas c){
            RectF r=interactionRect; p.setColor(Color.argb(221,251,254,255)); c.drawRoundRect(r,dp(23),dp(23),p);
            stroke.setStrokeWidth(dp(1.3f)); stroke.setColor(Color.argb(105,111,177,208)); c.drawRoundRect(new RectF(r.left+dp(1),r.top+dp(1),r.right-dp(1),r.bottom-dp(1)),dp(22),dp(22),stroke);
            p.setColor(Color.argb(42,84,153,184));
            for(int i=0;i<5;i++){ float yy=r.top+r.height()*(.23f+i*.14f); c.drawRoundRect(new RectF(r.left+dp(18),yy,r.right-dp(18),yy+dp(1.2f)),dp(1),dp(1),p); }
            drawRollingBall(c,rollX,rollY,rollingRadius());
            text.setTextAlign(Paint.Align.LEFT); text.setTextSize(tx(narrow?8.3f:9.2f)); text.setColor(Color.rgb(96,151,177)); c.drawText(ballReady?"ЗАТИСНИ КУЛЮ":"КОТИ ПАЛЬЦЕМ",r.left+dp(12),r.top+dp(16),text);
        }
        void drawRollingBall(Canvas c,float x,float y,float r){
            RadialGradient g=new RadialGradient(x-r*.32f,y-r*.38f,r*1.45f,new int[]{Color.WHITE,Color.rgb(244,251,255),Color.rgb(190,224,240)},new float[]{0,.56f,1},Shader.TileMode.CLAMP);
            p.setShader(g); c.drawCircle(x,y,r,p); p.setShader(null);
            stroke.setColor(Color.argb(90,91,153,184)); stroke.setStrokeWidth(dp(1)); c.drawCircle(x,y,r-dp(.5f),stroke);
            stroke.setColor(Color.argb(70,82,142,171)); RectF arc=new RectF(x-r*.42f,y-r*.42f,x+r*.42f,y+r*.42f); c.drawArc(arc,205,75,false,stroke);
        }

        void drawDecorationTray(Canvas c){
            RectF r=interactionRect; p.setColor(Color.argb(224,250,254,255)); c.drawRoundRect(r,dp(23),dp(23),p);
            if(decorPlaced==ACCESSORY_COUNT){
                p.setColor(Color.rgb(38,105,145)); c.drawRoundRect(finishBtn,dp(20),dp(20),p);
                text.setTextAlign(Paint.Align.CENTER); text.setTextSize(tx(13)); text.setColor(Color.WHITE); c.drawText("ЗАВЕРШИТИ СНІГОВИКА",finishBtn.centerX(),finishBtn.centerY()+dp(5),text); return;
            }
            for(Accessory a:accessories){
                RectF s=a.slot; p.setColor(a.placed?Color.rgb(226,242,235):Color.WHITE); c.drawRoundRect(s,dp(15),dp(15),p);
                if(a.placed){ text.setTextAlign(Paint.Align.CENTER); text.setTextSize(tx(11)); text.setColor(Color.rgb(69,141,116)); c.drawText("✓",s.centerX(),s.centerY()-dp(2),text); }
                else drawTrayIcon(c,a.type,s.centerX(),s.centerY()-dp(5),Math.min(s.width(),s.height())*.27f);
                text.setTextAlign(Paint.Align.CENTER); text.setTextSize(tx(narrow?6.8f:7.7f)); text.setColor(a.placed?Color.rgb(82,145,124):Color.rgb(84,128,151)); c.drawText(a.name,s.centerX(),s.bottom-dp(7),text);
            }
        }
        void drawTrayIcon(Canvas c,int type,float x,float y,float s){
            p.setColor(Color.rgb(48,70,83));
            if(type==EYES){ c.drawCircle(x-s*.34f,y,s*.18f,p); c.drawCircle(x+s*.34f,y,s*.18f,p); }
            else if(type==NOSE){ Path n=new Path(); n.moveTo(x-s*.28f,y-s*.15f); n.lineTo(x+s*.70f,y); n.lineTo(x-s*.28f,y+s*.15f); n.close(); p.setColor(Color.rgb(242,119,37)); c.drawPath(n,p); }
            else if(type==BUTTONS){ for(int i=-1;i<=1;i++) c.drawCircle(x,y+i*s*.43f,s*.13f,p); }
            else if(type==SCARF){ p.setColor(Color.rgb(198,62,68)); c.drawRoundRect(new RectF(x-s*.72f,y-s*.17f,x+s*.72f,y+s*.17f),dp(3),dp(3),p); }
            else if(type==HAT){ p.setColor(Color.rgb(45,62,78)); c.drawRect(x-s*.72f,y+s*.18f,x+s*.72f,y+s*.36f,p); c.drawRoundRect(new RectF(x-s*.42f,y-s*.58f,x+s*.42f,y+s*.20f),dp(4),dp(4),p); }
            else { stroke.setColor(Color.rgb(111,82,58)); stroke.setStrokeWidth(dp(3)); c.drawLine(x-s*.75f,y+s*.30f,x-s*.15f,y-s*.20f,stroke); c.drawLine(x+s*.75f,y+s*.30f,x+s*.15f,y-s*.20f,stroke); }
        }
        float accessoryTolerance(int type){ if(type==HAT)return headR*.95f; if(type==ARMS||type==SCARF||type==BUTTONS)return midR*.90f; return headR*.80f; }
        void drawTargetHint(Canvas c,Accessory a){
            float radius=accessoryTolerance(a.type)*.52f; stroke.setStrokeWidth(dp(2)); stroke.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(5)},0)); stroke.setColor(Color.argb(185,46,126,164)); c.drawCircle(a.targetX,a.targetY,radius,stroke); stroke.setPathEffect(null);
        }
        void drawAccessoryPreview(Canvas c,int type,float x,float y,int alpha){
            if(type==EYES){
                p.setColor(Color.argb(alpha,40,54,64)); c.drawCircle(x-headR*.29f,y,headR*.075f,p); c.drawCircle(x+headR*.29f,y,headR*.075f,p);
            } else if(type==NOSE){
                Path n=new Path(); n.moveTo(x-headR*.05f,y-headR*.09f); n.lineTo(x+headR*.78f,y+headR*.04f); n.lineTo(x-headR*.05f,y+headR*.12f); n.close(); p.setColor(Color.argb(alpha,244,118,35)); c.drawPath(n,p);
            } else if(type==BUTTONS){
                p.setColor(Color.argb(alpha,48,63,73)); for(int i=-1;i<=1;i++)c.drawCircle(x,y+i*midR*.38f,midR*.055f,p);
            } else if(type==SCARF){
                p.setColor(Color.argb(alpha,200,61,68)); c.drawRoundRect(new RectF(x-midR*.84f,y-midR*.10f,x+midR*.84f,y+midR*.12f),dp(7),dp(7),p); c.drawRoundRect(new RectF(x+midR*.34f,y+midR*.02f,x+midR*.60f,y+midR*.80f),dp(6),dp(6),p);
            } else if(type==HAT){
                p.setColor(Color.argb(alpha,45,62,78)); c.drawRoundRect(new RectF(x-headR*.75f,y-headR*.02f,x+headR*.75f,y+headR*.17f),dp(5),dp(5),p); c.drawRoundRect(new RectF(x-headR*.50f,y-headR*.70f,x+headR*.50f,y+headR*.04f),dp(8),dp(8),p); p.setColor(Color.argb(alpha,70,132,164)); c.drawRect(x-headR*.50f,y-headR*.10f,x+headR*.50f,y+headR*.03f,p);
            } else {
                stroke.setColor(Color.argb(alpha,108,80,58)); stroke.setStrokeWidth(Math.max(dp(3),baseR*.035f)); stroke.setStrokeCap(Paint.Cap.ROUND);
                c.drawLine(x-midR*.65f,y,x-midR*1.55f,y-midR*.48f,stroke); c.drawLine(x+midR*.65f,y,x+midR*1.55f,y-midR*.48f,stroke);
                c.drawLine(x-midR*1.45f,y-midR*.42f,x-midR*1.68f,y-midR*.70f,stroke); c.drawLine(x+midR*1.45f,y-midR*.42f,x+midR*1.68f,y-midR*.70f,stroke); stroke.setStrokeCap(Paint.Cap.BUTT);
            }
        }

        void addDust(float x,float y,float distance){
            if(dust.size()>38)dust.remove(0);
            float rr=dp(2)+Math.min(dp(4.5f),distance*.018f);
            dust.add(new Dust(x+rnd.nextFloat()*dp(11)-dp(5.5f),y+rnd.nextFloat()*dp(9)-dp(4.5f),rr,205));
        }
        void drawDust(Canvas c){
            Iterator<Dust>it=dust.iterator(); boolean more=false;
            while(it.hasNext()){
                Dust d=it.next(); d.alpha-=15; d.radius+=dp(.08f);
                if(d.alpha<=0){it.remove();continue;} more=true; p.setColor(Color.argb(d.alpha,255,255,255)); c.drawCircle(d.x,d.y,d.radius,p);
            }
            if(more)postInvalidateOnAnimation();
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(sponsorScene){
                if(e.getAction()==MotionEvent.ACTION_UP){ performClick(); float t=(SystemClock.elapsedRealtime()-sponsorStart)/1000f; if(t>=5.7f && sponsorCloseBtn.contains(x,y)){ sponsorScene=false; vibrate(16); invalidate(); } }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                lastX=x;lastY=y;lastDx=0;lastDy=0;
                if(finished)return true;
                if(balls<3){
                    float rr=rollingRadius();
                    if(ballReady&&dist(x,y,rollX,rollY)<=rr*1.55f+dp(10)){ ensureTimer(); draggingBall=true; rolling=false; tip="Перетягни кулю в пунктирний контур"; return true; }
                    if(!ballReady&&interactionRect.contains(x,y)){ ensureTimer(); rolling=true; draggingBall=false; rollX=x; rollY=y; keepRollingBallInsideZone(); return true; }
                } else if(decorPlaced<ACCESSORY_COUNT){
                    for(Accessory a:accessories) if(!a.placed&&a.slot.contains(x,y)){ ensureTimer(); draggingAccessory=a.type; dragAccessoryX=x; dragAccessoryY=y; tip="Перетягни «"+a.name+"» на сніговика"; invalidate(); return true; }
                }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                if(finished)return true;
                if(rolling&&!ballReady&&balls<3){
                    float dx=x-lastX,dy=y-lastY,d=(float)Math.hypot(dx,dy);
                    if(d>dp(1.2f)){
                        float required=dp(350)+targetR*1.55f;
                        rollProgress=Math.min(100,rollProgress+d/required*100);
                        if(lastDx!=0||lastDy!=0){ float a=(float)Math.hypot(lastDx,lastDy),cosine=(lastDx*dx+lastDy*dy)/Math.max(.001f,a*d); if(cosine<-.45f)turnPenalty+=1; }
                        lastDx=dx;lastDy=dy;rollX=x;rollY=y;keepRollingBallInsideZone();addDust(rollX,rollY,d);
                        if(rollProgress>=100){rollProgress=100;ballReady=true;rolling=false;tip="Куля готова — затисни й постав у контур";vibrate(26);} else tip="Коти кулю: "+(int)rollProgress+"%";
                        lastX=x;lastY=y;invalidate();
                    }
                    return true;
                }
                if(draggingBall&&ballReady&&balls<3){ rollX=clamp(x,dp(8),getWidth()-dp(8)); rollY=clamp(y,playTop,interactionRect.bottom); addDust(rollX,rollY,dp(3)); invalidate(); return true; }
                if(draggingAccessory>=0){ dragAccessoryX=clamp(x,dp(6),getWidth()-dp(6)); dragAccessoryY=clamp(y,playTop-dp(35),interactionRect.bottom); invalidate(); return true; }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                performClick();
                if(finished){
                    if(sponsorBtn.contains(x,y)){ startSponsorScene(); return true; }
                    if(restartBtn.contains(x,y)){ reset(); return true; }
                    return true;
                }
                if(draggingBall&&ballReady&&balls<3){ draggingBall=false; tryPlaceBall(); return true; }
                rolling=false;
                if(draggingAccessory>=0){ int type=draggingAccessory; draggingAccessory=-1; tryPlaceAccessory(type,x,y); return true; }
                if(balls>=3&&decorPlaced==ACCESSORY_COUNT&&finishBtn.contains(x,y)){ finishGame(); return true; }
                return true;
            }
            return true;
        }
        @Override public boolean performClick(){ super.performClick(); return true; }

        void tryPlaceBall(){
            float d=dist(rollX,rollY,targetX,targetY),threshold=targetR*.90f+rollingRadius()*.30f;
            if(d<=threshold){
                float accuracy=clamp(1-d/Math.max(dp(1),threshold),0,1);
                int quality=Math.max(60,Math.min(100,Math.round(72+accuracy*28-Math.min(8,turnPenalty*.45f))));
                qualitySum+=quality; score+=100+Math.round(accuracy*100);
                if(quality>=90){combo++;score+=20*combo;maxCombo=Math.max(maxCombo,combo);}else combo=0;
                balls++;vibrate(38);rollProgress=0;ballReady=false;turnPenalty=0;resetRollingBallPosition();
                tip=balls<3?"Точність "+quality+"%. Скоти кулю "+(balls+1):"Каркас готовий — перетягуй деталі знизу"; invalidate();
            } else { tip="Не попав у контур — постав кулю точніше"; resetRollingBallPosition(); vibrate(14); invalidate(); }
        }
        void tryPlaceAccessory(int type,float x,float y){
            Accessory a=accessories[type]; float tolerance=accessoryTolerance(type),d=dist(x,y,a.targetX,a.targetY);
            if(d<=tolerance){
                float accuracy=clamp(1-d/Math.max(dp(1),tolerance),0,1); int quality=Math.max(55,Math.min(100,Math.round(58+accuracy*42)));
                a.placed=true;a.x=x;a.y=y;a.quality=quality;decorPlaced++;decorQualitySum+=quality;score+=70+quality;
                if(quality>=90){combo++;score+=combo*20;maxCombo=Math.max(maxCombo,combo);}else combo=0;
                vibrate(24);tip=decorPlaced<ACCESSORY_COUNT?a.name+": "+quality+"%. Вибери наступну деталь":"Декор готовий — натисни «Завершити»";
            } else {combo=0;tip="Занадто далеко. «"+a.name+"» повернуто в набір";vibrate(12);}
            invalidate();
        }
        void finishGame(){
            ensureTimer();finishElapsed=elapsedSeconds();score+=Math.max(0,180-finishElapsed*2);
            if(mission==0)missionSuccess=avgBuild()>=85;else if(mission==1)missionSuccess=finishElapsed<=90;else missionSuccess=avgDecor()>=85;
            if(missionSuccess)score+=250;finished=true;
            if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}
            vibrate(65);invalidate();
        }

        void drawFinishOverlay(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            p.setColor(Color.argb(192,20,43,59));c.drawRect(0,0,getWidth(),getHeight(),p);
            float cardW=Math.min(w-dp(24),dp(382)),cardH=Math.min(bottom-safeTop-dp(22),compact?dp(420):dp(465)),left=(w-cardW)/2,top=safeTop+(bottom-safeTop-cardH)/2;
            RectF card=new RectF(left,top,left+cardW,top+cardH);p.setColor(Color.WHITE);c.drawRoundRect(card,dp(28),dp(28),p);

            text.setTextAlign(Paint.Align.CENTER);text.setColor(Color.rgb(30,69,93));text.setTextSize(tx(18));c.drawText("Сніговик готовий!",card.centerX(),card.top+dp(40),text);
            text.setTextSize(tx(36));text.setColor(Color.rgb(39,117,159));c.drawText(String.valueOf(score),card.centerX(),card.top+dp(91),text);
            text.setTextSize(tx(9.2f));text.setColor(Color.rgb(105,139,157));c.drawText("КУЛІ  "+avgBuild()+"%     ДЕКОР  "+avgDecor()+"%",card.centerX(),card.top+dp(120),text);
            c.drawText("ЧАС  "+formatTime(finishElapsed)+"     КОМБО ×"+maxCombo,card.centerX(),card.top+dp(141),text);
            c.drawText("РЕКОРД  "+bestScore,card.centerX(),card.top+dp(162),text);

            RectF badge=new RectF(card.left+dp(22),card.top+dp(180),card.right-dp(22),card.top+dp(226));
            p.setColor(missionSuccess?Color.rgb(229,246,237):Color.rgb(244,239,232));c.drawRoundRect(badge,dp(17),dp(17),p);
            text.setTextSize(tx(8.8f));text.setColor(missionSuccess?Color.rgb(55,130,104):Color.rgb(145,106,72));c.drawText(missionSuccess?"МІСІЮ ВИКОНАНО +250":"МІСІЮ НЕ ВИКОНАНО",badge.centerX(),badge.centerY()-dp(3),text);
            text.setTextSize(tx(7.3f));c.drawText(missionText(),badge.centerX(),badge.centerY()+dp(11),text);

            sponsorBtn.set(card.left+dp(22),card.bottom-dp(128),card.right-dp(22),card.bottom-dp(76));
            p.setColor(Color.rgb(226,91,122));c.drawRoundRect(sponsorBtn,dp(18),dp(18),p);
            text.setTextSize(tx(narrow?9.3f:10.5f));text.setColor(Color.WHITE);c.drawText(sponsorRewarded?"ЕСКІМОС УЖЕ СКУШТОВАНО":"СПРОБУВАТИ ЕСКІМОС  +150",sponsorBtn.centerX(),sponsorBtn.centerY()-dp(2),text);
            text.setTextSize(tx(6.8f));text.setColor(Color.argb(230,255,255,255));c.drawText("ДЕМО-ІНТЕГРАЦІЯ",sponsorBtn.centerX(),sponsorBtn.centerY()+dp(13),text);

            restartBtn.set(card.left+dp(22),card.bottom-dp(66),card.right-dp(22),card.bottom-dp(16));
            p.setColor(Color.rgb(38,105,145));c.drawRoundRect(restartBtn,dp(18),dp(18),p);
            text.setTextSize(tx(11.5f));text.setColor(Color.WHITE);c.drawText("ЩЕ ОДИН СНІГОВИК",restartBtn.centerX(),restartBtn.centerY()+dp(4),text);
        }

        void startSponsorScene(){
            sponsorScene=true; sponsorStart=SystemClock.elapsedRealtime(); vibrate(22); invalidate();
        }

        void drawSponsorScene(Canvas c){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            drawSponsorBackdrop(c,w,h,bottom);
            float t=(SystemClock.elapsedRealtime()-sponsorStart)/1000f;

            float cx=w*.48f;
            float snowBottom=bottom-dp(compact?112:126);
            float sr=Math.min(w*.19f,dp(72));
            float sm=sr*.72f, sh=sr*.54f;
            float syBase=snowBottom-sr;
            float syMid=syBase-(sr+sm)*.84f;
            float syHead=syMid-(sm+sh)*.84f;

            drawSponsorSnowman(c,cx,syBase,sr,syMid,sm,syHead,sh,t);

            float appear=smooth((t-.15f)/1.0f);
            float take=smooth((t-1.0f)/1.45f);
            float eat=smooth((t-2.55f)/2.7f);
            float productX=mix(w+dp(42),cx+sm*1.35f,appear);
            float productY=mix(h*.58f,syMid-sm*.10f,appear);
            if(take>0f){
                float mouthX=cx+sh*.18f;
                float mouthY=syHead+sh*.23f;
                float pulse=(float)(.5f-.5f*Math.cos(clamp((t-2.55f)/2.7f,0f,1f)*Math.PI*6f));
                float bite=smooth((pulse-.35f)/.65f)*eat;
                productX=mix(productX,mouthX+sh*.65f,take*.80f+eat*.20f);
                productY=mix(productY,mouthY+sh*.30f,take*.80f+eat*.20f);
                productY-=bite*sh*.28f;
            }
            float scoopScale=1f-.62f*eat;

            drawSponsorArms(c,cx,syMid,sm,productX,productY,take);
            drawIceCream(c,productX,productY,Math.max(.34f,scoopScale));
            drawSponsorPoster(c,w,h,t);

            if(t>=5.15f && !sponsorRewarded){
                sponsorRewarded=true; score+=150; if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();} vibrate(48);
            }

            if(t>=4.9f){
                float a=smooth((t-4.9f)/.7f);
                text.setTextAlign(Paint.Align.CENTER);text.setColor(Color.argb((int)(255*a),41,93,121));text.setTextSize(tx(narrow?16:19));
                c.drawText("Холодне до холодного!",w/2f,safeTop+dp(176),text);
                text.setTextSize(tx(narrow?9:10.5f));text.setColor(Color.argb((int)(230*a),83,132,154));
                c.drawText(sponsorRewarded?"Сніговик задоволений  •  +150":"Сніговик ласує морозивом",w/2f,safeTop+dp(198),text);
                drawSparkles(c,cx,syHead,sh,t,a);
            }

            if(t>=5.7f){
                float bw=Math.min(w-dp(38),dp(338)); float bh=dp(54); float bx=(w-bw)/2f; float by=bottom-dp(70);
                sponsorCloseBtn.set(bx,by,bx+bw,by+bh);
                p.setColor(Color.rgb(38,105,145));c.drawRoundRect(sponsorCloseBtn,dp(18),dp(18),p);
                text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("ПОВЕРНУТИСЯ ДО РЕЗУЛЬТАТУ",sponsorCloseBtn.centerX(),sponsorCloseBtn.centerY()+dp(4),text);
            }
        }

        void drawSponsorBackdrop(Canvas c,float w,float h,float bottom){
            LinearGradient sky=new LinearGradient(0,0,0,bottom*.75f,Color.rgb(168,224,249),Color.rgb(232,248,255),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(242,250,255));c.drawRect(0,bottom*.64f,w,h,p);
            p.setColor(Color.argb(225,255,255,255));c.drawOval(new RectF(-w*.35f,bottom*.48f,w*.60f,bottom*.75f),p);
            p.setColor(Color.argb(235,250,254,255));c.drawOval(new RectF(w*.38f,bottom*.50f,w*1.35f,bottom*.78f),p);
            for(int i=0;i<28;i++){
                float x=(i*97f+23f)%Math.max(1f,w);float y=safeTop+((i*137f+(SystemClock.elapsedRealtime()%9000)/12f)%Math.max(dp(130),bottom*.62f));
                p.setColor(Color.argb(130+(i%3)*30,255,255,255));c.drawCircle(x,y,dp(1f+(i%4)*.35f),p);
            }
        }

        void drawSponsorSnowman(Canvas c,float cx,float by,float br,float my,float mr,float hy,float hr,float t){
            p.setColor(Color.argb(45,67,126,151));c.drawOval(new RectF(cx-br*.95f,by+br*.72f,cx+br*.95f,by+br*1.04f),p);
            drawBall(c,cx,by,br,10);drawBall(c,cx,my,mr,11);drawBall(c,cx,hy,hr,12);
            p.setColor(Color.rgb(43,57,66));c.drawCircle(cx-hr*.28f,hy-hr*.15f,hr*.078f,p);c.drawCircle(cx+hr*.28f,hy-hr*.15f,hr*.078f,p);
            Path nose=new Path();nose.moveTo(cx-hr*.04f,hy-hr*.03f);nose.lineTo(cx+hr*.72f,hy+hr*.08f);nose.lineTo(cx-hr*.04f,hy+hr*.14f);nose.close();p.setColor(Color.rgb(241,117,34));c.drawPath(nose,p);
            p.setColor(Color.rgb(48,62,72));for(int i=-1;i<=1;i++)c.drawCircle(cx,my+i*mr*.36f,mr*.055f,p);
            p.setColor(Color.rgb(200,62,69));c.drawRoundRect(new RectF(cx-mr*.84f,my-mr*.84f,cx+mr*.84f,my-mr*.64f),dp(6),dp(6),p);
            p.setColor(Color.rgb(46,61,77));c.drawRoundRect(new RectF(cx-hr*.50f,hy-hr*1.75f,cx+hr*.50f,hy-hr*.98f),dp(8),dp(8),p);c.drawRoundRect(new RectF(cx-hr*.76f,hy-hr*1.03f,cx+hr*.76f,hy-hr*.86f),dp(5),dp(5),p);
            if(t>4.2f){
                stroke.setColor(Color.rgb(56,90,106));stroke.setStrokeWidth(dp(2));RectF smile=new RectF(cx-hr*.35f,hy+hr*.08f,cx+hr*.35f,hy+hr*.52f);c.drawArc(smile,20,140,false,stroke);
            }
        }

        void drawSponsorArms(Canvas c,float cx,float my,float mr,float px,float py,float amount){
            amount=smooth(amount);
            float leftSX=cx-mr*.62f,rightSX=cx+mr*.62f,sy=my-mr*.05f;
            float leftTX=px-dp(7),rightTX=px+dp(7),ty=py+dp(16);
            float leftEX=mix(cx-mr*1.55f,leftTX,amount),rightEX=mix(cx+mr*1.55f,rightTX,amount),ey=mix(my-mr*.48f,ty,amount);
            stroke.setColor(Color.rgb(105,78,56));stroke.setStrokeWidth(Math.max(dp(3),mr*.05f));stroke.setStrokeCap(Paint.Cap.ROUND);
            c.drawLine(leftSX,sy,leftEX,ey,stroke);c.drawLine(rightSX,sy,rightEX,ey,stroke);
            c.drawLine(leftEX,ey,leftEX-dp(7),ey-dp(9),stroke);c.drawLine(rightEX,ey,rightEX+dp(7),ey-dp(9),stroke);stroke.setStrokeCap(Paint.Cap.BUTT);
        }

        void drawIceCream(Canvas c,float x,float y,float scoopScale){
            float coneH=dp(52),coneW=dp(31),scoopR=dp(22)*scoopScale;
            Path cone=new Path();cone.moveTo(x-coneW/2,y+dp(10));cone.lineTo(x+coneW/2,y+dp(10));cone.lineTo(x,y+coneH);cone.close();p.setColor(Color.rgb(211,151,80));c.drawPath(cone,p);
            stroke.setColor(Color.argb(105,116,77,44));stroke.setStrokeWidth(dp(1));
            for(int i=0;i<3;i++){float yy=y+dp(18+i*9);c.drawLine(x-coneW*.31f,yy,x+coneW*.31f,yy+dp(7),stroke);c.drawLine(x+coneW*.31f,yy,x-coneW*.31f,yy+dp(7),stroke);}
            p.setColor(Color.rgb(247,136,169));c.drawCircle(x,y,scoopR,p);p.setColor(Color.argb(165,255,255,255));c.drawCircle(x-scoopR*.32f,y-scoopR*.34f,scoopR*.28f,p);
            p.setColor(Color.rgb(255,255,255));c.drawRoundRect(new RectF(x-dp(21),y+dp(27),x+dp(21),y+dp(41)),dp(5),dp(5),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(5.8f));text.setColor(Color.rgb(201,70,105));c.drawText("ЕСКІМОС",x,y+dp(37),text);
        }

        void drawSponsorPoster(Canvas c,float w,float h,float t){
            float a=smooth((t-.2f)/.8f);float cardW=Math.min(w-dp(32),dp(370));float left=(w-cardW)/2f;RectF card=new RectF(left,safeTop+dp(14),left+cardW,safeTop+dp(136));
            p.setColor(Color.argb((int)(235*a),255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(6.8f));text.setColor(Color.argb((int)(180*a),109,138,153));c.drawText("ДЕМО-ІНТЕГРАЦІЯ • МОЖЛИВЕ МІСЦЕ ДЛЯ ПАРТНЕРА",card.left+dp(16),card.top+dp(18),text);
            text.setTextSize(tx(narrow?24:28));text.setColor(Color.argb((int)(255*a),214,72,111));c.drawText("ЕСКІМОС",card.left+dp(16),card.top+dp(55),text);
            text.setTextSize(tx(narrow?9:10.5f));text.setColor(Color.argb((int)(225*a),62,101,122));c.drawText("Морозиво, яке розуміє зиму.",card.left+dp(16),card.top+dp(79),text);
            text.setTextSize(tx(narrow?7.2f:8.2f));text.setColor(Color.argb((int)(190*a),92,129,146));c.drawText("Сніговик може ласувати ним — бо воно теж холодне.",card.left+dp(16),card.top+dp(100),text);
        }

        void drawSparkles(Canvas c,float cx,float hy,float hr,float t,float alpha){
            p.setColor(Color.argb((int)(200*alpha),255,255,255));
            for(int i=0;i<9;i++){
                double a=i*.72+t*.8;float rr=hr*(1.3f+(i%3)*.35f);float x=cx+(float)Math.cos(a)*rr,y=hy+(float)Math.sin(a)*rr;
                c.drawCircle(x,y,dp(1.5f+(i%2)),p);
            }
        }

        void reset(){
            balls=0;score=0;qualitySum=0;decorQualitySum=0;decorPlaced=0;combo=0;maxCombo=0;finished=false;
            rollProgress=0;rolling=false;draggingBall=false;ballReady=false;turnPenalty=0;draggingAccessory=-1;startTime=0;finishElapsed=0;missionSuccess=false;mission=rnd.nextInt(3);
            sponsorScene=false;sponsorRewarded=false;sponsorStart=0;
            tip="Коти сніг пальцем — зроби першу кулю";dust.clear();
            for(Accessory a:accessories){a.placed=false;a.quality=0;a.x=a.y=0;}
            rollX=Float.NaN;rollY=Float.NaN;vibrate(18);invalidate();
        }

        static class Accessory {
            final int type; final String name; final RectF slot=new RectF();
            float targetX,targetY,x,y; int quality; boolean placed;
            Accessory(int type,String name){this.type=type;this.name=name;}
        }
        static class Dust {
            float x,y,radius; int alpha;
            Dust(float x,float y,float radius,int alpha){this.x=x;this.y=y;this.radius=radius;this.alpha=alpha;}
        }
    }
}
