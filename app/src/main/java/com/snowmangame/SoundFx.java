package com.snowmangame;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Tiny procedural SFX engine for the prototype. No external audio files are used,
 * so every effect is generated at runtime and can be tuned in code.
 */
public final class SoundFx {
    public static final int UI=1;
    public static final int CRUNCH=2;
    public static final int SNOW_READY=3;
    public static final int SNOW_SET=4;
    public static final int ITEM=5;
    public static final int ERROR=6;
    public static final int COMPLETE=7;
    public static final int MITTEN=8;
    public static final int MEMORY=9;
    public static final int PLAY=10;
    public static final int HIT=11;
    public static final int PHONE=12;
    public static final int CAR_ARRIVE=13;
    public static final int CAR_DOOR=14;
    public static final int ENGINE=15;
    public static final int TICKET=16;
    public static final int TRAIN=17;
    public static final int ARRIVAL=18;
    public static final int MELT=19;
    public static final int DRIP=20;
    public static final int SCHOOL_BELL=21;
    public static final int CORRECT=22;
    public static final int WRONG=23;
    public static final int CORE=24;
    public static final int CLOTH=25;

    private static final int SR=22050;
    private static final ExecutorService EXEC=Executors.newFixedThreadPool(2);
    private static final Map<Integer,Long> LAST=new HashMap<>();
    private static int noiseState=0x13579BDF;

    private SoundFx(){}

    public static boolean enabled(SharedPreferences p){return p.getBoolean("sound_enabled",true);}
    public static boolean enabled(Context c){return enabled(c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE));}
    public static String label(SharedPreferences p){return enabled(p)?"ЗВУК: УВІМК":"ЗВУК: ВИМК";}

    public static boolean toggle(Context c){
        SharedPreferences p=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
        boolean on=!enabled(p);p.edit().putBoolean("sound_enabled",on).apply();
        if(on)play(c,UI,true);
        return on;
    }

    public static void play(Context c,int effect){play(c,effect,false);}
    public static void crunch(Context c){play(c,CRUNCH,false);}

    private static void play(Context c,int effect,boolean ignorePref){
        if(c==null)return;
        Context app=c.getApplicationContext();
        SharedPreferences p=app.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
        if(!ignorePref&&!enabled(p))return;
        long now=SystemClock.elapsedRealtime();
        int throttle=effect==CRUNCH?105:(effect==UI?65:35);
        synchronized(LAST){Long last=LAST.get(effect);if(last!=null&&now-last<throttle)return;LAST.put(effect,now);}
        final float volume=Math.max(.08f,Math.min(1f,p.getInt("sound_volume",68)/100f));
        EXEC.execute(() -> renderAndPlay(effect,volume));
    }

    private static void renderAndPlay(int effect,float volume){
        int ms=duration(effect);int n=Math.max(64,SR*ms/1000);short[] pcm=new short[n];
        for(int i=0;i<n;i++){
            double t=i/(double)SR, u=i/(double)Math.max(1,n-1);double v=sample(effect,t,u,i,n);
            v=Math.max(-1,Math.min(1,v))*volume*.72;
            pcm[i]=(short)(v*32767);
        }
        AudioTrack track=null;
        try{
            AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            AudioFormat fmt=new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(SR).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build();
            track=new AudioTrack.Builder().setAudioAttributes(attrs).setAudioFormat(fmt).setBufferSizeInBytes(pcm.length*2).setTransferMode(AudioTrack.MODE_STATIC).build();
            if(track.getState()!=AudioTrack.STATE_INITIALIZED){track.release();return;}
            track.write(pcm,0,pcm.length);track.play();
            try{Thread.sleep(ms+45L);}catch(InterruptedException ignored){Thread.currentThread().interrupt();}
        }catch(Exception ignored){}finally{if(track!=null)try{track.stop();track.release();}catch(Exception ignored){}}
    }

    private static int duration(int e){
        switch(e){
            case CRUNCH:return 58;case SNOW_READY:return 220;case SNOW_SET:return 135;case ITEM:return 110;case ERROR:return 160;
            case COMPLETE:return 520;case MITTEN:return 210;case MEMORY:return 360;case PLAY:return 120;case HIT:return 115;
            case PHONE:return 240;case CAR_ARRIVE:return 430;case CAR_DOOR:return 170;case ENGINE:return 500;case TICKET:return 135;
            case TRAIN:return 560;case ARRIVAL:return 520;case MELT:return 360;case DRIP:return 180;case SCHOOL_BELL:return 720;
            case CORRECT:return 300;case WRONG:return 220;case CORE:return 620;case CLOTH:return 145;default:return 52;
        }
    }

    private static double sample(int e,double t,double u,int i,int n){
        double a=attackRelease(u,.05,.20), noise=noise()*a;
        switch(e){
            case UI:return Math.sin(2*Math.PI*760*t)*a*.38;
            case CRUNCH:{double grains=(noise*.72+Math.sin(2*Math.PI*118*t)*.20)*Math.pow(1-u,1.8);return grains;}
            case SNOW_READY:return note(t,u,520,.12)+note(t-.07,shift(u,.32),780,.16)+noise*.10;
            case SNOW_SET:return Math.sin(2*Math.PI*(105+35*u)*t)*Math.pow(1-u,2.5)*.55+noise*.18;
            case ITEM:return noise*.32*Math.sin(Math.PI*u)+Math.sin(2*Math.PI*480*t)*a*.09;
            case CLOTH:return noise*.42*Math.sin(Math.PI*u)*(.55+.45*Math.sin(2*Math.PI*18*t));
            case ERROR:return Math.sin(2*Math.PI*(255-85*u)*t)*Math.pow(1-u,1.3)*.40;
            case COMPLETE:return chord(t,u,new double[]{523.25,659.25,783.99},.20)+Math.sin(2*Math.PI*1046.5*t)*Math.pow(1-u,2.2)*.10;
            case MITTEN:return noise*.24*Math.sin(Math.PI*u)+note(t-.08,shift(u,.38),880,.13);
            case MEMORY:return chord(t,u,new double[]{659.25,987.77,1318.5},.13);
            case PLAY:return Math.sin(2*Math.PI*(390+220*u)*t)*a*.22+noise*.12;
            case HIT:return Math.sin(2*Math.PI*140*t)*Math.pow(1-u,3)*.55+noise*.20;
            case PHONE:return note(t,u,740,.20)+note(t-.11,shift(u,.47),930,.17);
            case CAR_ARRIVE:return Math.sin(2*Math.PI*(82+6*Math.sin(2*Math.PI*7*t))*t)*a*.32+noise*.08;
            case CAR_DOOR:return Math.sin(2*Math.PI*73*t)*Math.pow(1-u,4)*.72+noise*Math.pow(1-u,5)*.20;
            case ENGINE:return Math.sin(2*Math.PI*(86+4*Math.sin(2*Math.PI*8*t))*t)*(.18+.15*Math.sin(Math.PI*u))+noise*.05;
            case TICKET:return noise*Math.pow(1-u,3)*.16+Math.sin(2*Math.PI*900*t)*Math.pow(1-u,3)*.22;
            case TRAIN:{double pulse=(pulse(u,.08,.035)+pulse(u,.48,.035)+pulse(u,.58,.028)+pulse(u,.90,.03));return noise*pulse*.55+Math.sin(2*Math.PI*72*t)*.12*a;}
            case ARRIVAL:return chord(t,u,new double[]{523.25,659.25,784.0},.18);
            case MELT:return Math.sin(2*Math.PI*(320-170*u)*t)*a*.12+noise*.06;
            case DRIP:return Math.sin(2*Math.PI*(1350-600*u)*t)*Math.pow(1-u,2.4)*.30;
            case SCHOOL_BELL:return (Math.sin(2*Math.PI*740*t)+.55*Math.sin(2*Math.PI*1110*t)+.28*Math.sin(2*Math.PI*1480*t))*Math.pow(1-u,1.65)*.28;
            case CORRECT:return note(t,u,660,.18)+note(t-.12,shift(u,.42),990,.18);
            case WRONG:return Math.sin(2*Math.PI*(300-120*u)*t)*Math.pow(1-u,1.4)*.35;
            case CORE:return chord(t,u,new double[]{880,1320,1760},.11)*(.65+.35*Math.sin(Math.PI*u));
            default:return Math.sin(2*Math.PI*700*t)*a*.25;
        }
    }

    private static double note(double t,double u,double f,double gain){if(t<0||u<0||u>1)return 0;return Math.sin(2*Math.PI*f*t)*Math.pow(1-u,2.2)*gain;}
    private static double chord(double t,double u,double[] f,double gain){double s=0;for(double x:f)s+=Math.sin(2*Math.PI*x*t);return s/Math.max(1,f.length)*Math.pow(1-u,1.65)*gain*3.0;}
    private static double attackRelease(double u,double attack,double release){double a=Math.min(1,u/Math.max(.001,attack));double r=Math.min(1,(1-u)/Math.max(.001,release));return Math.max(0,Math.min(a,r));}
    private static double shift(double u,double start){return (u-start)/Math.max(.001,1-start);}
    private static double pulse(double u,double center,double width){double d=(u-center)/Math.max(.001,width);return Math.exp(-d*d*6);}
    private static double noise(){noiseState^=noiseState<<13;noiseState^=noiseState>>>17;noiseState^=noiseState<<5;return ((noiseState&0x7fffffff)/(double)0x3fffffff)-1.0;}
}
