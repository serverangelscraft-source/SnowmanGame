package com.snowmangame;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import android.content.*;
import android.os.Vibrator;
import android.os.VibrationEffect;
import java.util.Random;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(new SnowmanView(this));
    }

    static class SnowmanView extends View {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
        float snow = 0, lastX, lastY;
        int balls = 0, score = 0;
        boolean face = false, dressed = false, finished = false;
        RectF makeBtn = new RectF(), faceBtn = new RectF(), dressBtn = new RectF(), finishBtn = new RectF();
        Random rnd = new Random();
        String tip = "Води пальцем по снігу";
        Vibrator vib;

        SnowmanView(Context c) {
            super(c);
            text.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            vib = (Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
        }

        void vibrate(int ms) {
            if (vib == null || !vib.hasVibrator()) return;
            if (android.os.Build.VERSION.SDK_INT >= 26) vib.vibrate(VibrationEffect.createOneShot(ms, 80));
            else vib.vibrate(ms);
        }

        @Override protected void onDraw(Canvas c) {
            float w=getWidth(), h=getHeight();
            p.setColor(Color.rgb(190,233,255)); c.drawRect(0,0,w,h*.55f,p);
            p.setColor(Color.WHITE); c.drawOval(new RectF(-w*.15f,h*.45f,w*1.15f,h*.82f),p);
            p.setColor(Color.rgb(241,250,255)); c.drawRect(0,h*.58f,w,h,p);

            p.setColor(Color.argb(230,255,255,255)); c.drawRoundRect(new RectF(18,18,w-18,94),24,24,p);
            text.setColor(Color.rgb(24,54,78)); text.setTextSize(30); text.setTextAlign(Paint.Align.LEFT);
            c.drawText("Сніг: "+(int)snow+"/100",35,65,text);
            text.setTextAlign(Paint.Align.RIGHT); c.drawText("★ "+score,w-35,65,text);
            text.setTextAlign(Paint.Align.CENTER); text.setTextSize(24); c.drawText(tip,w/2,128,text);

            p.setColor(Color.rgb(250,253,255)); c.drawOval(new RectF(w*.06f,h*.57f,w*.94f,h*.82f),p);
            text.setTextSize(20); text.setColor(Color.rgb(104,166,199)); c.drawText("СВАЙПАЙ ПО СНІГУ",w/2,h*.68f,text);

            float cx=w/2;
            if(balls>=1) ball(c,cx,h*.58f,150);
            if(balls>=2) ball(c,cx,h*.43f,115);
            if(balls>=3) ball(c,cx,h*.315f,86);

            if(face && balls>=3){
                p.setColor(Color.rgb(35,51,65));
                c.drawCircle(cx-24,h*.292f,8,p); c.drawCircle(cx+24,h*.292f,8,p);
                Path nose=new Path(); nose.moveTo(cx,h*.31f); nose.lineTo(cx+58,h*.32f); nose.lineTo(cx,h*.33f); nose.close();
                p.setColor(Color.rgb(255,122,25)); c.drawPath(nose,p);
                p.setColor(Color.rgb(35,51,65));
                c.drawCircle(cx,h*.395f,7,p); c.drawCircle(cx,h*.44f,7,p); c.drawCircle(cx,h*.485f,7,p);
            }
            if(dressed){
                p.setColor(Color.rgb(45,62,80)); c.drawRect(cx-55,h*.235f,cx+55,h*.255f,p);
                c.drawRoundRect(new RectF(cx-39,h*.18f,cx+39,h*.242f),12,12,p);
                p.setColor(Color.rgb(206,61,61)); c.drawRoundRect(new RectF(cx-72,h*.345f,cx+72,h*.37f),12,12,p);
                c.drawRect(cx+28,h*.36f,cx+50,h*.43f,p);
            }

            float bottom=h-25, bh=72, bw=(w-50)/4;
            makeBtn.set(10,bottom-bh,10+bw,bottom);
            faceBtn.set(20+bw,bottom-bh,20+2*bw,bottom);
            dressBtn.set(30+2*bw,bottom-bh,30+3*bw,bottom);
            finishBtn.set(40+3*bw,bottom-bh,w-10,bottom);
            drawButton(c,makeBtn,"Куля",balls<3);
            drawButton(c,faceBtn,"Обличчя",balls>=3&&!face);
            drawButton(c,dressBtn,"Одяг",face&&!dressed);
            drawButton(c,finishBtn,"Готово",dressed&&!finished);

            if(finished){
                p.setColor(Color.argb(215,20,43,61)); c.drawRect(0,0,w,h,p);
                p.setColor(Color.WHITE); c.drawRoundRect(new RectF(45,h*.3f,w-45,h*.67f),30,30,p);
                text.setColor(Color.rgb(24,54,78)); text.setTextSize(38); c.drawText("Сніговик готовий!",w/2,h*.4f,text);
                text.setTextSize(58); text.setColor(Color.rgb(37,117,163)); c.drawText(""+score,w/2,h*.5f,text);
                text.setTextSize(24); text.setColor(Color.rgb(24,54,78)); c.drawText("Торкнися — грати ще",w/2,h*.59f,text);
            }
        }

        void ball(Canvas c,float x,float y,float r){
            RadialGradient g=new RadialGradient(x-r*.28f,y-r*.33f,r*1.35f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(211,235,247)},new float[]{0,.55f,1},Shader.TileMode.CLAMP);
            p.setShader(g); c.drawCircle(x,y,r,p); p.setShader(null);
        }
        void drawButton(Canvas c,RectF r,String s,boolean active){
            p.setColor(active?Color.rgb(25,59,82):Color.rgb(205,220,229)); c.drawRoundRect(r,18,18,p);
            text.setColor(active?Color.WHITE:Color.rgb(115,135,148)); text.setTextSize(18); c.drawText(s,r.centerX(),r.centerY()+7,text);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(), y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(finished){ reset(); return true; }
                lastX=x; lastY=y;
                if(makeBtn.contains(x,y)){ makeBall(); return true; }
                if(faceBtn.contains(x,y)){ addFace(); return true; }
                if(dressBtn.contains(x,y)){ addDress(); return true; }
                if(finishBtn.contains(x,y)){ finish(); return true; }
            } else if(e.getAction()==MotionEvent.ACTION_MOVE && balls<3 && y>getHeight()*.52f){
                float d=(float)Math.hypot(x-lastX,y-lastY);
                snow=Math.min(100,snow+d*.12f); lastX=x; lastY=y;
                if(snow>=100) tip="Натисни «Куля»"; invalidate();
            }
            return true;
        }
        void makeBall(){
            if(balls>=3)return;
            if(snow<100){tip="Назбирай 100 снігу";invalidate();return;}
            snow=0;balls++;score+=100;vibrate(35);tip=balls<3?"Збери сніг для кулі "+(balls+1):"Додай обличчя";invalidate();
        }
        void addFace(){ if(balls<3||face)return;face=true;score+=150;tip="Тепер одягни сніговика";vibrate(25);invalidate(); }
        void addDress(){ if(!face||dressed)return;dressed=true;score+=150;tip="Можна завершувати";vibrate(25);invalidate(); }
        void finish(){ if(!dressed)return;score+=100+rnd.nextInt(101);finished=true;vibrate(60);invalidate(); }
        void reset(){ snow=0;balls=0;score=0;face=false;dressed=false;finished=false;tip="Води пальцем по снігу";invalidate(); }
    }
}
