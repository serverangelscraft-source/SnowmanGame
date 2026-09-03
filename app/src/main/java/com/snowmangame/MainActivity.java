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

import java.util.Random;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(235,247,253));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26) flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29) w.setNavigationBarContrastEnforced(false);
        setContentView(new SnowmanView(this));
    }

    static class SnowmanView extends View {
        static final int EYES=0, NOSE=1, BUTTONS=2, SCARF=3, HAT=4, ARMS=5, ACCESSORY_COUNT=6;
        final Context ctx;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), text=new Paint(Paint.ANTI_ALIAS_FLAG), stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final SharedPreferences prefs;
        final Vibrator vibrator;
        final Random rnd=new Random();
        final Accessory[] items=new Accessory[ACCESSORY_COUNT];

        float density, textScale, safeTop, safeBottom;
        boolean compact, narrow, finished, rolling, ballReady, draggingBall, sponsorScene, sponsorRewarded, coinsAwarded;
        int balls, score, bestScore, buildQuality, decorQuality, decorPlaced, combo;
        int year, wallet, runCoins, mission, yearBuilds, rewardedBuildsToday, snowCondition, visitorType;
        long rewardDay, challengeDay;
        long startTime, sponsorStart;
        int finishSeconds;
        boolean missionSuccess;

        float rollProgress, rollX=Float.NaN, rollY=Float.NaN, lastX, lastY;
        int draggingAccessory=-1;
        float dragX, dragY;

        int giftType=-1;
        long giftUntil=0;

        final RectF hud=new RectF(), tipCard=new RectF(), interaction=new RectF(), finishBtn=new RectF();
        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();
        float playTop, playBottom, baseR, midR, headR, baseY, midY, headY, targetX, targetY, targetR;
        String tip="Коти сніг пальцем — зроби першу кулю";

        SnowmanView(Context c){
            super(c);
            ctx=c;
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.18f);
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            bestScore=prefs.getInt("best_score",0);
            year=Math.max(1,Math.min(7,prefs.getInt("life_year",1)));
            wallet=Math.max(0,prefs.getInt("coins",0));
            yearBuilds=Math.max(0,Math.min(3,prefs.getInt("year_builds_"+year,0)));
            syncDailyState();
            challengeDay=rewardDay;
            snowCondition=dailySnowCondition();
            visitorType=dailyVisitor();
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);
            stroke.setStrokeCap(Paint.Cap.ROUND);
            String[] names={"Очі","Морква","Ґудзики","Шарф","Шапка","Руки"};
            for(int i=0;i<ACCESSORY_COUNT;i++) items[i]=new Accessory(i,names[i]);
            mission=dailyMission();
            tip=visitorRequest();
            setClickable(true);
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=20){
                        safeTop=insets.getSystemWindowInsetTop();
                        safeBottom=insets.getSystemWindowInsetBottom();
                    }
                    invalidate();
                    return insets;
                }
            });
            requestApplyInsets();
        }

        float dp(float v){return v*density;}
        float tx(float v){return v*textScale;}
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        float dist(float x1,float y1,float x2,float y2){return(float)Math.hypot(x1-x2,y1-y2);}
        float smooth(float v){v=clamp(v,0,1);return v*v*(3-2*v);}
        float mix(float a,float b,float t){return a+(b-a)*t;}
        int elapsed(){if(startTime==0)return 0;if(finished)return finishSeconds;return(int)((SystemClock.elapsedRealtime()-startTime)/1000L);}
        void ensureTimer(){if(startTime==0)startTime=SystemClock.elapsedRealtime();}
        int avgBuild(){return balls==0?0:buildQuality/balls;}
        int avgDecor(){return decorPlaced==0?0:decorQuality/decorPlaced;}
        boolean requiredDecorReady(){return items[EYES].placed&&items[NOSE].placed&&decorPlaced>=3;}
        int optionalDecorCount(){return Math.max(0,decorPlaced-3);}
        int yearGoal(){return 1100+(year-1)*320;}
        float effort(){
            float snow=snowCondition==0?.90f:(snowCondition==1?1.12f:1f);
            return (1f+(year-1)*.20f)*snow;
        }
        String ageName(){
            switch(year){
                case 1:return "Малюк";
                case 2:return "Малюк-дослідник";
                case 3:return "Пустун";
                case 4:return "Помічник";
                case 5:return "Майстер снігу";
                case 6:return "Майбутній школяр";
                default:return "Школяр";
            }
        }
        String missionText(){
            int acc=Math.min(94,84+year),sec=Math.max(48,72-year*3);
            if(mission==0)return "МІСІЯ: точність куль ≥ "+acc+"%";
            if(mission==1)return "МІСІЯ: завершити ≤ "+sec+" с";
            return "МІСІЯ: декор ≥ "+acc+"%";
        }
        long localDayNumber(){
            Calendar local=Calendar.getInstance();
            int y=local.get(Calendar.YEAR),m=local.get(Calendar.MONTH),d=local.get(Calendar.DAY_OF_MONTH);
            GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            utc.clear();utc.set(y,m,d,0,0,0);
            return utc.getTimeInMillis()/86400000L;
        }
        void syncDailyState(){
            long now=localDayNumber();
            long saved=prefs.getLong("reward_day",Long.MIN_VALUE);
            if(saved!=now){
                rewardDay=now;rewardedBuildsToday=0;
                prefs.edit().putLong("reward_day",now).putInt("rewarded_builds_today",0).apply();
            }else{
                rewardDay=saved;rewardedBuildsToday=Math.max(0,Math.min(3,prefs.getInt("rewarded_builds_today",0)));
            }
        }
        int dailyMission(){
            long seed=challengeDay*31L+year*17L;
            return (int)Math.floorMod(seed,3L);
        }
        int dailySnowCondition(){return (int)Math.floorMod(challengeDay*13L+7L,3L);}
        String snowName(){return snowCondition==0?"ПУХКИЙ":(snowCondition==1?"МОКРИЙ":"КРИЖАНИЙ");}
        int dailyVisitor(){return (int)Math.floorMod(challengeDay*19L+11L,4L);}
        String visitorName(){return visitorType==0?"МАЙСТРИНЯ":(visitorType==1?"ФОТОГРАФ":(visitorType==2?"ДИТИНА":"СУСІД"));}
        String visitorRequest(){
            if(visitorType==0)return "Гість дня: майстриня • хоче шарф і 4+ деталі";
            if(visitorType==1)return "Гість дня: фотограф • хоче точні кулі й декор";
            if(visitorType==2)return "Гість дня: дитина • хоче шапку і 5+ деталей";
            return "Гість дня: сусід • хоче простого сніговика з 3 деталей";
        }
        boolean visitorSuccess(){
            if(visitorType==0)return items[SCARF].placed&&decorPlaced>=4;
            if(visitorType==1)return avgBuild()>=90&&avgDecor()>=75;
            if(visitorType==2)return items[HAT].placed&&decorPlaced>=5;
            return decorPlaced==3;
        }
        String visitorMemoryKey(){return "visitor_memory_"+challengeDay;}
        void refreshDailyIfNeeded(){
            long now=localDayNumber();
            if(now==challengeDay)return;
            if(startTime!=0)return;
            syncDailyState();challengeDay=rewardDay;mission=dailyMission();snowCondition=dailySnowCondition();visitorType=dailyVisitor();
            tip=visitorRequest();
        }
        String timeText(int s){return String.format("%d:%02d",s/60,s%60);}
        void buzz(int ms){
            if(vibrator==null||!vibrator.hasVibrator())return;
            if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,80));
            else vibrator.vibrate(ms);
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            refreshDailyIfNeeded();
            layout();
            if(sponsorScene){drawSponsor(c);postInvalidateOnAnimation();return;}
            drawBackground(c);
            drawHud(c);
            drawTip(c);
            drawSnowman(c);
            if(!finished){
                if(balls<3)drawRollingArea(c);
                else drawTray(c);
            }
            if(finished)drawFinish(c);
            if(giftType>=0){
                if(SystemClock.elapsedRealtime()<giftUntil){
                    drawGift(c);
                    postInvalidateDelayed(60);
                }else giftType=-1;
            }
            if(startTime!=0&&!finished)postInvalidateDelayed(500);
        }

        void layout(){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            float usable=Math.max(dp(430),bottom-safeTop);
            compact=usable<dp(650);
            narrow=w<dp(360);
            float m=narrow?dp(9):dp(13);
            float hudH=compact?dp(60):dp(66);
            float tipH=compact?dp(44):dp(48);
            float interactionH=balls<3?(compact?dp(112):dp(132)):(compact?dp(190):dp(214));
            hud.set(m,safeTop+dp(8),w-m,safeTop+dp(8)+hudH);
            tipCard.set(m,hud.bottom+dp(7),w-m,hud.bottom+dp(7)+tipH);
            interaction.set(m,bottom-interactionH-dp(8),w-m,bottom-dp(8));
            playTop=tipCard.bottom+dp(5);
            playBottom=interaction.top-dp(6);
            float playH=Math.max(dp(220),playBottom-playTop);
            baseR=clamp(Math.min(w*.205f,playH/4.05f),dp(34),dp(82));
            midR=baseR*.72f;headR=baseR*.54f;
            baseY=playBottom-baseR-dp(5);
            midY=baseY-(baseR+midR)*.84f;
            headY=midY-(midR+headR)*.84f;
            targetX=w/2f;
            if(balls==0){targetY=baseY;targetR=baseR;}
            else if(balls==1){targetY=midY;targetR=midR;}
            else{targetY=headY;targetR=headR;}
            setAccessoryTargets();
            layoutSlots();
            if(Float.isNaN(rollX)||Float.isNaN(rollY)){rollX=interaction.centerX();rollY=interaction.centerY()+dp(3);}
            if(!draggingBall&&balls<3)keepBallInside();
        }

        void setAccessoryTargets(){
            float cx=getWidth()/2f;
            target(EYES,cx,headY-headR*.16f);
            target(NOSE,cx,headY+headR*.05f);
            target(BUTTONS,cx,midY+midR*.03f);
            target(SCARF,cx,midY-midR*.73f);
            target(HAT,cx,headY-headR*1.28f);
            target(ARMS,cx,midY-midR*.08f);
        }
        void target(int type,float x,float y){items[type].targetX=x;items[type].targetY=y;}
        void layoutSlots(){
            if(balls<3)return;
            float gap=dp(6),pad=dp(8),buttonH=dp(48);
            float sw=(interaction.width()-pad*2-gap*2)/3f;
            float slotsBottom=interaction.bottom-pad-buttonH-gap;
            float sh=(slotsBottom-(interaction.top+pad)-gap)/2f;
            for(int i=0;i<ACCESSORY_COUNT;i++){
                int row=i/3,col=i%3;
                float l=interaction.left+pad+col*(sw+gap),t=interaction.top+pad+row*(sh+gap);
                items[i].slot.set(l,t,l+sw,t+sh);
            }
            finishBtn.set(interaction.left+pad,slotsBottom+gap,interaction.right-pad,interaction.bottom-pad);
        }
        float rollingRadius(){
            float max=Math.min(targetR*.72f,interaction.height()*.36f);
            return dp(13)+(max-dp(13))*clamp(rollProgress/100f,0,1);
        }
        void keepBallInside(){
            float r=Math.max(dp(13),rollingRadius());
            rollX=clamp(rollX,interaction.left+r+dp(3),interaction.right-r-dp(3));
            rollY=clamp(rollY,interaction.top+r+dp(3),interaction.bottom-r-dp(3));
        }

        void drawBackground(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            LinearGradient g=new LinearGradient(0,safeTop,0,bottom*.70f,Color.rgb(154,215,246),Color.rgb(231,248,255),Shader.TileMode.CLAMP);
            p.setShader(g);c.drawRect(0,0,w,getHeight(),p);p.setShader(null);
            p.setColor(Color.argb(235,255,255,255));
            c.drawOval(new RectF(-w*.42f,bottom*.39f,w*.68f,bottom*.70f),p);
            p.setColor(Color.rgb(242,250,254));
            c.drawOval(new RectF(w*.20f,bottom*.46f,w*1.35f,bottom*.75f),p);
            c.drawRect(0,bottom*.64f,w,getHeight(),p);
            p.setColor(Color.argb(70,62,119,137));
            for(int i=0;i<5;i++){
                float x=w*(.06f+i*.235f),y=bottom*(.60f+(i%2)*.015f),s=dp(14+(i%3)*4);
                Path tr=new Path();tr.moveTo(x,y-s*2.3f);tr.lineTo(x-s,y);tr.lineTo(x+s,y);tr.close();c.drawPath(tr,p);
            }
            p.setColor(Color.argb(150,255,255,255));
            for(int i=0;i<22;i++){
                float x=(i*137f+33f)%Math.max(1,w);
                float y=safeTop+dp(18)+((i*193f)%Math.max(dp(100),playBottom-safeTop));
                c.drawCircle(x,y,dp(1+(i%3)*.4f),p);
            }
        }

        void drawHud(Canvas c){
            p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(hud,dp(21),dp(21),p);
            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(83,116,134));text.setTextSize(tx(narrow?6.8f:7.6f));
            c.drawText(balls<3?"ЛІПЛЕННЯ":"ОФОРМЛЕННЯ",hud.left+dp(14),hud.top+dp(18),text);
            text.setTextSize(tx(narrow?14:16));text.setColor(Color.rgb(38,69,89));
            c.drawText(balls<3?"КУЛЯ "+(balls+1)+"/3":"ДЕТАЛІ "+decorPlaced+"/6",hud.left+dp(14),hud.bottom-dp(13),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(narrow?7.4f:8.4f));text.setColor(Color.rgb(34,104,146));
            c.drawText(rewardedBuildsToday<3?("НАГОРОДИ "+rewardedBuildsToday+"/3"):"ВІЛЬНА РОБОТА",hud.right-dp(14),hud.top+dp(22),text);
            text.setTextSize(tx(6.4f));text.setColor(Color.rgb(108,139,154));
            c.drawText("СНІГ: "+snowName()+" • Рік "+year+" • ● "+wallet,hud.right-dp(14),hud.bottom-dp(11),text);
        }

        void drawTip(Canvas c){
            p.setColor(Color.argb(220,236,248,254));c.drawRoundRect(tipCard,dp(17),dp(17),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(narrow?9:10.2f));text.setColor(Color.rgb(43,82,108));
            c.drawText(tip,tipCard.centerX(),tipCard.centerY()-dp(3),text);
            text.setTextSize(tx(narrow?6.5f:7.5f));text.setColor(Color.rgb(92,137,159));
            c.drawText(missionText(),tipCard.centerX(),tipCard.centerY()+dp(12),text);
        }

        void drawSnowman(Canvas c){
            float cx=getWidth()/2f;
            p.setColor(Color.argb(42,68,129,157));
            c.drawOval(new RectF(cx-baseR*.92f,baseY+baseR*.69f,cx+baseR*.92f,baseY+baseR*1.05f),p);
            if(balls>=1)drawBall(c,cx,baseY,baseR,1);
            if(balls>=2)drawBall(c,cx,midY,midR,2);
            if(balls>=3)drawBall(c,cx,headY,headR,3);
            if(balls<3){
                stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(7),dp(6)},0));
                stroke.setColor(Color.argb(ballReady?190:90,52,126,163));c.drawCircle(targetX,targetY,targetR,stroke);stroke.setPathEffect(null);
                if(ballReady){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8));text.setColor(Color.rgb(65,123,151));c.drawText("ПЕРЕТЯГНИ СЮДИ",targetX,targetY,text);}
            }
            for(Accessory a:items)if(a.placed)drawAccessory(c,a.type,a.x,a.y,255);
            if(draggingAccessory>=0){
                Accessory a=items[draggingAccessory];
                stroke.setColor(Color.argb(180,46,126,164));stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(5)},0));
                c.drawCircle(a.targetX,a.targetY,tolerance(a.type)*.55f,stroke);stroke.setPathEffect(null);
                drawAccessory(c,a.type,dragX,dragY,235);
            }
        }

        void drawBall(Canvas c,float x,float y,float r,int seed){
            RadialGradient g=new RadialGradient(x-r*.31f,y-r*.37f,r*1.42f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},new float[]{0,.57f,1},Shader.TileMode.CLAMP);
            p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);
            stroke.setColor(Color.argb(82,101,162,193));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);
            p.setColor(Color.argb(55,136,186,210));
            for(int i=0;i<6;i++){double a=i*2.1+seed*.7;float rr=r*(.25f+((i*31+seed*17)%45)/100f);c.drawCircle(x+(float)Math.cos(a)*rr,y+(float)Math.sin(a)*rr,Math.max(dp(.8f),r*.018f),p);}
        }

        void drawRollingArea(Canvas c){
            p.setColor(Color.argb(228,251,254,255));c.drawRoundRect(interaction,dp(23),dp(23),p);
            stroke.setColor(Color.argb(100,111,177,208));stroke.setStrokeWidth(dp(1.3f));c.drawRoundRect(interaction,dp(23),dp(23),stroke);
            drawRollingBall(c,rollX,rollY,rollingRadius());
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(8.4f));text.setColor(Color.rgb(86,145,174));
            c.drawText(ballReady?"КУЛЯ ГОТОВА — ПЕРЕТЯГНИ":"КОТИ ПАЛЬЦЕМ • "+(int)rollProgress+"%",interaction.left+dp(12),interaction.top+dp(17),text);
        }
        void drawRollingBall(Canvas c,float x,float y,float r){
            RadialGradient g=new RadialGradient(x-r*.32f,y-r*.38f,r*1.45f,new int[]{Color.WHITE,Color.rgb(244,251,255),Color.rgb(190,224,240)},new float[]{0,.56f,1},Shader.TileMode.CLAMP);
            p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);
            stroke.setColor(Color.argb(90,91,153,184));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);
        }

        void drawTray(Canvas c){
            p.setColor(Color.argb(230,250,254,255));c.drawRoundRect(interaction,dp(23),dp(23),p);
            if(decorPlaced==ACCESSORY_COUNT){
                p.setColor(Color.rgb(38,105,145));c.drawRoundRect(finishBtn,dp(20),dp(20),p);
                text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12));text.setColor(Color.WHITE);
                c.drawText("ЗАВЕРШИТИ СНІГОВИКА",finishBtn.centerX(),finishBtn.centerY()+dp(4),text);
                return;
            }
            for(Accessory a:items){
                p.setColor(a.placed?Color.rgb(226,242,235):Color.WHITE);c.drawRoundRect(a.slot,dp(15),dp(15),p);
                if(a.placed){
                    text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12));text.setColor(Color.rgb(69,141,116));c.drawText("✓",a.slot.centerX(),a.slot.centerY(),text);
                }else drawTrayIcon(c,a.type,a.slot.centerX(),a.slot.centerY()-dp(5),Math.min(a.slot.width(),a.slot.height())*.26f);
                text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(narrow?6.2f:7.2f));text.setColor(Color.rgb(84,128,151));
                String label=a.name;
                if(a.type==NOSE)label="Морква • мама";
                if(a.type==ARMS)label="ПАЛКА ЧОТКО";
                c.drawText(label,a.slot.centerX(),a.slot.bottom-dp(7),text);
            }
            boolean ready=requiredDecorReady();
            p.setColor(ready?Color.rgb(38,105,145):Color.rgb(228,238,244));c.drawRoundRect(finishBtn,dp(17),dp(17),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(ready?9.6f:7.3f));text.setColor(ready?Color.WHITE:Color.rgb(91,126,143));
            String finishLabel=ready?(optionalDecorCount()>0?("ЗАВЕРШИТИ • СТИЛЬ +"+optionalDecorCount()):"ЗАВЕРШИТИ СНІГОВИКА"):("ПОТРІБНО: ОЧІ + МОРКВА + ЩЕ 1");
            c.drawText(finishLabel,finishBtn.centerX(),finishBtn.centerY()+dp(3),text);
        }

        void drawTrayIcon(Canvas c,int type,float x,float y,float s){
            p.setColor(Color.rgb(48,70,83));
            if(type==EYES){c.drawCircle(x-s*.34f,y,s*.18f,p);c.drawCircle(x+s*.34f,y,s*.18f,p);}
            else if(type==NOSE){Path n=new Path();n.moveTo(x-s*.28f,y-s*.15f);n.lineTo(x+s*.75f,y);n.lineTo(x-s*.28f,y+s*.15f);n.close();p.setColor(Color.rgb(242,119,37));c.drawPath(n,p);}
            else if(type==BUTTONS){for(int i=-1;i<=1;i++)c.drawCircle(x,y+i*s*.43f,s*.13f,p);}
            else if(type==SCARF){p.setColor(Color.rgb(198,62,68));c.drawRoundRect(new RectF(x-s*.72f,y-s*.17f,x+s*.72f,y+s*.17f),dp(3),dp(3),p);}
            else if(type==HAT){p.setColor(Color.rgb(45,62,78));c.drawRect(x-s*.72f,y+s*.18f,x+s*.72f,y+s*.36f,p);c.drawRoundRect(new RectF(x-s*.42f,y-s*.58f,x+s*.42f,y+s*.20f),dp(4),dp(4),p);}
            else{stroke.setColor(Color.rgb(111,82,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-s*.75f,y+s*.30f,x-s*.15f,y-s*.20f,stroke);c.drawLine(x+s*.75f,y+s*.30f,x+s*.15f,y-s*.20f,stroke);}
        }

        float tolerance(int type){
            float snow=snowCondition==0?1.06f:(snowCondition==2?.90f:.98f);
            float mul=Math.max(.72f,1f-(year-1)*.035f)*snow;
            if(type==HAT)return headR*.95f*mul;
            if(type==ARMS||type==SCARF||type==BUTTONS)return midR*.90f*mul;
            return headR*.80f*mul;
        }
        void drawAccessory(Canvas c,int type,float x,float y,int alpha){
            if(type==EYES){
                p.setColor(Color.argb(alpha,40,54,64));c.drawCircle(x-headR*.29f,y,headR*.075f,p);c.drawCircle(x+headR*.29f,y,headR*.075f,p);
            }else if(type==NOSE){
                Path n=new Path();n.moveTo(x-headR*.05f,y-headR*.09f);n.lineTo(x+headR*.78f,y+headR*.04f);n.lineTo(x-headR*.05f,y+headR*.12f);n.close();p.setColor(Color.argb(alpha,244,118,35));c.drawPath(n,p);
            }else if(type==BUTTONS){
                p.setColor(Color.argb(alpha,48,63,73));for(int i=-1;i<=1;i++)c.drawCircle(x,y+i*midR*.38f,midR*.055f,p);
            }else if(type==SCARF){
                p.setColor(Color.argb(alpha,200,61,68));c.drawRoundRect(new RectF(x-midR*.84f,y-midR*.10f,x+midR*.84f,y+midR*.12f),dp(7),dp(7),p);c.drawRoundRect(new RectF(x+midR*.34f,y+midR*.02f,x+midR*.60f,y+midR*.80f),dp(6),dp(6),p);
            }else if(type==HAT){
                p.setColor(Color.argb(alpha,45,62,78));c.drawRoundRect(new RectF(x-headR*.75f,y-headR*.02f,x+headR*.75f,y+headR*.17f),dp(5),dp(5),p);c.drawRoundRect(new RectF(x-headR*.50f,y-headR*.70f,x+headR*.50f,y+headR*.04f),dp(8),dp(8),p);
            }else{
                stroke.setColor(Color.argb(alpha,108,80,58));stroke.setStrokeWidth(Math.max(dp(3),baseR*.035f));
                c.drawLine(x-midR*.65f,y,x-midR*1.55f,y-midR*.48f,stroke);c.drawLine(x+midR*.65f,y,x+midR*1.55f,y-midR*.48f,stroke);
            }
        }

        void showGift(int type){
            giftType=type;giftUntil=SystemClock.elapsedRealtime()+3700;postInvalidateOnAnimation();
        }
        void drawGift(Canvas c){
            float w=getWidth();
            float cw=Math.min(w-dp(28),dp(370)),ch=dp(116),l=(w-cw)/2,top=Math.max(safeTop+dp(14),tipCard.bottom+dp(8));
            RectF card=new RectF(l,top,l+cw,top+ch);
            p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(6.5f));text.setColor(Color.rgb(126,143,151));
            c.drawText("ІГРОВА СЦЕНА • БЕЗ РЕАЛЬНОГО ПАРТНЕРСТВА",card.left+dp(16),card.top+dp(18),text);
            if(giftType==NOSE){
                text.setTextSize(tx(17));text.setColor(Color.rgb(238,112,35));c.drawText("ПЕРША МОРКВИНА",card.left+dp(16),card.top+dp(47),text);
                text.setTextSize(tx(10));text.setColor(Color.rgb(55,91,110));c.drawText("Її сніговику купила мама у крамниці.",card.left+dp(16),card.top+dp(72),text);
                text.setTextSize(tx(8));text.setColor(Color.rgb(95,130,147));c.drawText("Тепер у нього є справжній перший ніс.",card.left+dp(16),card.top+dp(94),text);
                float x=card.right-dp(45),y=card.centerY()+dp(4);Path n=new Path();n.moveTo(x-dp(24),y-dp(8));n.lineTo(x+dp(25),y);n.lineTo(x-dp(24),y+dp(8));n.close();p.setColor(Color.rgb(242,119,37));c.drawPath(n,p);
            }else{
                text.setTextSize(tx(17));text.setColor(Color.rgb(53,91,120));c.drawText("РУКИ — ПАЛКА ЧОТКО",card.left+dp(16),card.top+dp(47),text);
                text.setTextSize(tx(9.5f));text.setColor(Color.rgb(55,91,110));c.drawText("Для активного користувача інтернету.",card.left+dp(16),card.top+dp(72),text);
                text.setTextSize(tx(8));text.setColor(Color.rgb(95,130,147));c.drawText("Українські палки теж можуть мати характер.",card.left+dp(16),card.top+dp(94),text);
                stroke.setColor(Color.rgb(107,78,56));stroke.setStrokeWidth(dp(4));c.drawLine(card.right-dp(75),card.centerY()+dp(16),card.right-dp(24),card.centerY()-dp(20),stroke);
            }
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(sponsorScene){
                if(e.getAction()==MotionEvent.ACTION_UP){
                    performClick();
                    float t=(SystemClock.elapsedRealtime()-sponsorStart)/1000f;
                    if(t>4.8f&&sponsorCloseBtn.contains(x,y)){
                        if(year>=7){prefs.edit().putBoolean("pre_school_icecream_done",true).apply();ctx.startActivity(new Intent(ctx,SchoolActivity.class));((Activity)ctx).finish();return true;}
                        sponsorScene=false;invalidate();
                    }
                }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                lastX=x;lastY=y;
                if(finished)return true;
                if(balls<3){
                    if(ballReady&&dist(x,y,rollX,rollY)<=rollingRadius()*1.55f+dp(10)){
                        ensureTimer();draggingBall=true;rolling=false;tip="Перетягни кулю в контур";return true;
                    }
                    if(!ballReady&&interaction.contains(x,y)){
                        ensureTimer();rolling=true;rollX=x;rollY=y;keepBallInside();return true;
                    }
                }else if(decorPlaced<ACCESSORY_COUNT){
                    for(Accessory a:items)if(!a.placed&&a.slot.contains(x,y)){
                        ensureTimer();draggingAccessory=a.type;dragX=x;dragY=y;tip="Перетягни «"+a.name+"» на сніговика";invalidate();return true;
                    }
                }
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                if(finished)return true;
                if(rolling&&!ballReady&&balls<3){
                    float d=dist(x,y,lastX,lastY);
                    if(d>dp(1)){
                        float required=(dp(340)+targetR*1.6f)*effort();
                        rollProgress=Math.min(100,rollProgress+d/required*100);
                        rollX=x;rollY=y;keepBallInside();lastX=x;lastY=y;
                        if(rollProgress>=100){rollProgress=100;ballReady=true;rolling=false;tip="Куля готова — постав її в контур";buzz(26);}
                        else tip="Коти кулю: "+(int)rollProgress+"%";
                        invalidate();
                    }
                    return true;
                }
                if(draggingBall){rollX=x;rollY=y;invalidate();return true;}
                if(draggingAccessory>=0){dragX=x;dragY=y;invalidate();return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                performClick();
                if(finished){
                    if(journeyBtn.contains(x,y)&&(year>=7||yearBuilds>=3)){
                        if(year>=7&&!prefs.getBoolean("pre_school_icecream_done",false)){
                            sponsorScene=true;sponsorRewarded=false;sponsorStart=SystemClock.elapsedRealtime();buzz(20);invalidate();return true;
                        }
                        ctx.startActivity(new Intent(ctx,year>=7?SchoolActivity.class:DeliveryActivity.class));((Activity)ctx).finish();return true;
                    }
                    if(replayBtn.contains(x,y)){reset();return true;}
                    return true;
                }
                if(draggingBall){draggingBall=false;tryPlaceBall();return true;}
                rolling=false;
                if(draggingAccessory>=0){int type=draggingAccessory;draggingAccessory=-1;tryPlaceAccessory(type,x,y);return true;}
                if(balls>=3&&requiredDecorReady()&&finishBtn.contains(x,y)){finishGame();return true;}
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}

        void tryPlaceBall(){
            float d=dist(rollX,rollY,targetX,targetY),threshold=targetR*.95f+rollingRadius()*.28f;
            if(d<=threshold){
                float acc=clamp(1-d/Math.max(1,threshold),0,1);int q=Math.max(55,Math.min(100,Math.round(72+acc*28)));
                buildQuality+=q;score+=100+Math.round(acc*100);
                if(q>=90){combo++;score+=combo*20;}else combo=0;
                balls++;rollProgress=0;ballReady=false;buzz(38);
                rollX=interaction.centerX();rollY=interaction.centerY()+dp(3);
                tip=balls<3?"Точність "+q+"%. Готуй наступну кулю":"Каркас готовий — додай деталі";
            }else{
                tip="Не попав у контур — постав точніше";rollX=interaction.centerX();rollY=interaction.centerY();buzz(12);
            }
            invalidate();
        }

        void tryPlaceAccessory(int type,float x,float y){
            Accessory a=items[type];float tol=tolerance(type),d=dist(x,y,a.targetX,a.targetY);
            float loose=tol*1.65f;
            if(d<=loose){
                float acc=clamp(1-d/Math.max(1,tol),0,1);
                int q=Math.max(25,Math.min(100,Math.round(35+acc*65)));
                a.placed=true;a.x=x;a.y=y;a.quality=q;decorPlaced++;decorQuality+=q;score+=45+q;
                if(q>=90){combo++;score+=combo*20;}else combo=0;
                buzz(q>=70?24:14);
                if(type==NOSE)showGift(NOSE);
                if(type==ARMS)showGift(ARMS);
                if(decorPlaced>=ACCESSORY_COUNT)tip="Усі деталі на місці — можна завершувати";
                else if(requiredDecorReady())tip="Можна завершити або додати ще деталей для стилю";
                else if(d<=tol)tip=a.name+": "+q+"%. Потрібні очі, морква і ще одна деталь";
                else tip=a.name+" кривенько — зате з характером";
            }else{
                combo=0;tip="Це вже повз сніговика — спробуй ближче";buzz(12);
            }
            invalidate();
        }

        void finishGame(){
            ensureTimer();finishSeconds=elapsed();
            score+=Math.max(0,180-finishSeconds*2);
            int acc=Math.min(94,84+year),sec=Math.max(48,72-year*3);
            missionSuccess=mission==0?avgBuild()>=acc:mission==1?finishSeconds<=sec:avgDecor()>=acc;
            if(missionSuccess)score+=250;
            finished=true;
            if(!coinsAwarded){
                syncDailyState();
                coinsAwarded=true;
                if(rewardedBuildsToday<3){
                    runCoins=Math.max(1,score/300);wallet+=runCoins;rewardedBuildsToday++;yearBuilds=Math.min(3,yearBuilds+1);
                    prefs.edit().putInt("coins",wallet).putLong("reward_day",rewardDay).putInt("rewarded_builds_today",rewardedBuildsToday).putInt("year_builds_"+year,yearBuilds).apply();
                }else runCoins=0;
            }
            if(visitorSuccess()&&!prefs.getBoolean(visitorMemoryKey(),false))prefs.edit().putBoolean(visitorMemoryKey(),true).apply();
            if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}
            buzz(65);invalidate();
        }

        void drawFinish(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            p.setColor(Color.argb(194,20,43,59));c.drawRect(0,0,getWidth(),getHeight(),p);
            float cw=Math.min(w-dp(22),dp(390)),ch=Math.min(bottom-safeTop-dp(18),compact?dp(478):dp(525));
            float l=(w-cw)/2,top=safeTop+(bottom-safeTop-ch)/2;
            RectF card=new RectF(l,top,l+cw,top+ch);
            p.setColor(Color.WHITE);c.drawRoundRect(card,dp(28),dp(28),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(17));text.setColor(Color.rgb(30,69,93));c.drawText("Сніговика завершено • рік "+year,card.centerX(),card.top+dp(36),text);
            text.setTextSize(tx(34));text.setColor(Color.rgb(39,117,159));c.drawText(String.valueOf(score),card.centerX(),card.top+dp(82),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(103,137,155));c.drawText("ЦІЛЬ "+yearGoal()+" • "+(score>=yearGoal()?"ВИКОНАНО":"ЩЕ Є КУДИ РОСТИ"),card.centerX(),card.top+dp(105),text);
            c.drawText("КУЛІ "+avgBuild()+"% • ДЕКОР "+avgDecor()+"% • "+timeText(finishSeconds),card.centerX(),card.top+dp(127),text);
            RectF coin=new RectF(card.left+dp(24),card.top+dp(145),card.right-dp(24),card.top+dp(194));
            p.setColor(runCoins>0?Color.rgb(235,247,239):Color.rgb(239,245,248));c.drawRoundRect(coin,dp(17),dp(17),p);text.setTextSize(tx(8.5f));text.setColor(runCoins>0?Color.rgb(55,126,99):Color.rgb(79,119,140));
            String rewardText=runCoins>0?("+"+runCoins+" МОНЕТ • НАГОРОДИ "+rewardedBuildsToday+"/3"):("ВІЛЬНА РОБОТА • НАГОРОДИ 3/3 • БАЛАНС "+wallet);
            c.drawText(rewardText,coin.centerX(),coin.centerY()+dp(3),text);
            RectF badge=new RectF(card.left+dp(24),card.top+dp(205),card.right-dp(24),card.top+dp(248));
            p.setColor(missionSuccess?Color.rgb(229,246,237):Color.rgb(246,239,232));c.drawRoundRect(badge,dp(15),dp(15),p);text.setTextSize(tx(8));text.setColor(missionSuccess?Color.rgb(55,130,104):Color.rgb(145,106,72));c.drawText(missionSuccess?"МІСІЮ ВИКОНАНО • +250 БАЛІВ":"МІСІЮ НЕ ВИКОНАНО",badge.centerX(),badge.centerY()+dp(3),text);

            RectF visitor=new RectF(card.left+dp(24),card.top+dp(256),card.right-dp(24),card.top+dp(292));
            boolean visitorDone=visitorSuccess();
            p.setColor(visitorDone?Color.rgb(237,245,255):Color.rgb(247,244,238));c.drawRoundRect(visitor,dp(13),dp(13),p);
            text.setTextSize(tx(7.1f));text.setColor(visitorDone?Color.rgb(57,103,151):Color.rgb(127,105,75));
            c.drawText(visitorDone?("СПОГАД ДНЯ • "+visitorName()+" • ЗБЕРЕЖЕНО"):("ГІСТЬ ДНЯ • "+visitorName()+" • СПРОБУЙ ЩЕ"),visitor.centerX(),visitor.centerY()+dp(3),text);

            sponsorBtn.set(card.left+dp(22),card.bottom-dp(178),card.right-dp(22),card.bottom-dp(128));
            boolean yearReady=year>=7||yearBuilds>=3;
            p.setColor(yearReady?Color.rgb(231,246,238):Color.rgb(239,245,248));c.drawRoundRect(sponsorBtn,dp(18),dp(18),p);
            text.setTextSize(tx(8.4f));text.setTextColor(Color.rgb(50,126,96));
            text.setColor(yearReady?Color.rgb(50,126,96):Color.rgb(79,119,140));
            String yearProgress=year>=7?"ШКІЛЬНИЙ ЕТАП ВІДКРИТО":(yearReady?"3/3 • ПОДОРОЖ У НОВИЙ РІК ВІДКРИТО":"ПРОГРЕС РОКУ "+yearBuilds+"/3 • ЩЕ "+(3-yearBuilds)+" ДО ПОДОРОЖІ");
            c.drawText(yearProgress,sponsorBtn.centerX(),sponsorBtn.centerY()+dp(3),text);

            journeyBtn.set(card.left+dp(22),card.bottom-dp(116),card.right-dp(22),card.bottom-dp(62));
            p.setColor(yearReady?Color.rgb(35,106,153):Color.rgb(165,188,201));c.drawRoundRect(journeyBtn,dp(19),dp(19),p);text.setTextSize(tx(yearReady?10.5f:8.8f));text.setColor(Color.WHITE);
            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(yearReady?(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ"):("ЗРОБИ ЩЕ "+(3-yearBuilds)+" СНІГОВИК(И)"));
            c.drawText(journeyLabel,journeyBtn.centerX(),journeyBtn.centerY()+dp(4),text);

            replayBtn.set(card.left+dp(45),card.bottom-dp(51),card.right-dp(45),card.bottom-dp(12));
            p.setColor(Color.rgb(238,246,250));c.drawRoundRect(replayBtn,dp(16),dp(16),p);text.setTextSize(tx(8));text.setColor(Color.rgb(76,118,142));c.drawText("ЩЕ РАЗ У ЦЬОМУ РОЦІ",replayBtn.centerX(),replayBtn.centerY()+dp(3),text);
        }

        void drawSponsor(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom,t=(SystemClock.elapsedRealtime()-sponsorStart)/1000f;
            LinearGradient sky=new LinearGradient(0,0,0,bottom*.75f,Color.rgb(168,224,249),Color.rgb(232,248,255),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,getHeight(),p);p.setShader(null);
            RectF poster=new RectF(dp(16),safeTop+dp(15),w-dp(16),safeTop+dp(132));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(poster,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(6.5f));text.setColor(Color.rgb(112,140,154));c.drawText(year>=7?"ПЕРЕД ШКОЛОЮ • ІГРОВА СЦЕНА":"ІГРОВА СЦЕНА",poster.left+dp(16),poster.top+dp(19),text);
            text.setTextSize(tx(25));text.setColor(Color.rgb(214,72,111));c.drawText("ЕСКІМОС",poster.left+dp(16),poster.top+dp(55),text);
            text.setTextSize(tx(9));text.setColor(Color.rgb(62,101,122));c.drawText("Холодне до холодного.",poster.left+dp(16),poster.top+dp(82),text);
            float cx=w*.46f,br=Math.min(w*.18f,dp(70)),mr=br*.72f,hr=br*.54f,by=bottom-dp(120)-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;
            drawBall(c,cx,by,br,10);drawBall(c,cx,my,mr,11);drawBall(c,cx,hy,hr,12);
            p.setColor(Color.rgb(43,57,66));c.drawCircle(cx-hr*.28f,hy-hr*.15f,hr*.078f,p);c.drawCircle(cx+hr*.28f,hy-hr*.15f,hr*.078f,p);
            Path nose=new Path();nose.moveTo(cx,hy);nose.lineTo(cx+hr*.72f,hy+hr*.08f);nose.lineTo(cx,hy+hr*.14f);nose.close();p.setColor(Color.rgb(241,117,34));c.drawPath(nose,p);
            float eat=smooth((t-1.2f)/2.7f),ix=mix(w+dp(30),cx+hr*.75f,smooth(t/.9f)),iy=hy+hr*.35f;
            drawIceCream(c,ix,iy,Math.max(.30f,1-.70f*eat));
            stroke.setColor(Color.rgb(105,78,56));stroke.setStrokeWidth(dp(3));c.drawLine(cx+mr*.62f,my,ix,iy+dp(20),stroke);
            if(t>3.8f&&!sponsorRewarded){sponsorRewarded=true;score+=150;if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}buzz(40);}
            if(t>3.7f){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(42,93,121));c.drawText("Холодне до холодного! +150",w/2,safeTop+dp(168),text);}
            if(t>4.8f){
                float bw=Math.min(w-dp(38),dp(338)),bh=dp(54),l=(w-bw)/2,top=bottom-dp(70);sponsorCloseBtn.set(l,top,l+bw,top+bh);
                p.setColor(Color.rgb(38,105,145));c.drawRoundRect(sponsorCloseBtn,dp(18),dp(18),p);text.setTextSize(tx(9.5f));text.setColor(Color.WHITE);c.drawText(year>=7?"ДО ШКОЛИ":"ПОВЕРНУТИСЯ ДО РЕЗУЛЬТАТУ",sponsorCloseBtn.centerX(),sponsorCloseBtn.centerY()+dp(4),text);
            }
        }
        void drawIceCream(Canvas c,float x,float y,float scale){
            float ch=dp(48),cw=dp(29),r=dp(21)*scale;Path cone=new Path();cone.moveTo(x-cw/2,y+dp(10));cone.lineTo(x+cw/2,y+dp(10));cone.lineTo(x,y+ch);cone.close();p.setColor(Color.rgb(211,151,80));c.drawPath(cone,p);p.setColor(Color.rgb(247,136,169));c.drawCircle(x,y,r,p);
        }

        void reset(){
            balls=0;score=0;buildQuality=0;decorQuality=0;decorPlaced=0;combo=0;finished=false;rolling=false;ballReady=false;draggingBall=false;draggingAccessory=-1;
            sponsorScene=false;sponsorRewarded=false;coinsAwarded=false;runCoins=0;rollProgress=0;startTime=0;finishSeconds=0;missionSuccess=false;syncDailyState();challengeDay=rewardDay;mission=dailyMission();snowCondition=dailySnowCondition();visitorType=dailyVisitor();giftType=-1;
            refreshDailyIfNeeded();tip=visitorRequest();rollX=Float.NaN;rollY=Float.NaN;
            for(Accessory a:items){a.placed=false;a.quality=0;a.x=a.y=0;}
            buzz(18);invalidate();
        }

        static class Accessory{
            final int type;final String name;final RectF slot=new RectF();
            float targetX,targetY,x,y;int quality;boolean placed;
            Accessory(int t,String n){type=t;name=n;}
        }
    }
}