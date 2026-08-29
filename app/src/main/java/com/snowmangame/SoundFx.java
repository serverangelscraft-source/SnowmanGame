package com.snowmangame;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Reliable short game sounds backed by real WAV resources, with a tone fallback. */
public final class SoundFx {
    public static final int UI=1,CRUNCH=2,SNOW_READY=3,SNOW_SET=4,ITEM=5,ERROR=6,COMPLETE=7,MITTEN=8,MEMORY=9,PLAY=10,HIT=11,PHONE=12,CAR_ARRIVE=13,CAR_DOOR=14,ENGINE=15,TICKET=16,TRAIN=17,ARRIVAL=18,MELT=19,DRIP=20,SCHOOL_BELL=21,CORRECT=22,WRONG=23,CORE=24,CLOTH=25,PARCEL=26,SLED=27,TRAIN_DOOR=28;

    private static final Object LOCK=new Object();
    private static final Map<Integer,Long> LAST=new HashMap<>();
    private static final Map<Integer,Integer> SAMPLES=new HashMap<>();
    private static final Map<Integer,Float> PENDING=new HashMap<>();
    private static final Set<Integer> LOADED=new HashSet<>();
    private static SoundPool pool;
    private static Context app;

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

    private static void ensure(Context c){
        synchronized(LOCK){
            if(pool!=null)return;
            app=c.getApplicationContext();
            AudioAttributes attrs=new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            pool=new SoundPool.Builder().setMaxStreams(8).setAudioAttributes(attrs).build();
            pool.setOnLoadCompleteListener((sp,sampleId,status)->{
                Float volume=null;
                synchronized(LOCK){
                    if(status==0){LOADED.add(sampleId);volume=PENDING.remove(sampleId);}else PENDING.remove(sampleId);
                }
                if(status==0&&volume!=null&&app!=null&&enabled(app)){
                    int effectForSample=effectForSample(sampleId);
                    int stream=sp.play(sampleId,volume,volume,1,0,rateFor(effectForSample));
                    if(stream==0)fallback(effectForSample,volume);
                }else if(status!=0)fallback(UI,.9f);
            });
        }
    }

    private static void play(Context c,int effect,boolean ignorePref){
        if(c==null)return;
        Context a=c.getApplicationContext();
        SharedPreferences p=a.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
        if(!ignorePref&&!enabled(p))return;
        long now=SystemClock.elapsedRealtime();
        int throttle=effect==CRUNCH?110:(effect==SLED?230:(effect==UI?65:40));
        synchronized(LAST){Long last=LAST.get(effect);if(last!=null&&now-last<throttle)return;LAST.put(effect,now);}
        float volume=Math.max(.25f,Math.min(1f,p.getInt("sound_volume",90)/100f));
        ensure(a);
        int sampleId;
        boolean ready;
        synchronized(LOCK){
            Integer id=SAMPLES.get(effect);
            if(id==null){
                try{id=pool.load(a,resFor(effect),1);}catch(Exception ex){fallback(effect,volume);return;}
                SAMPLES.put(effect,id);
            }
            sampleId=id;
            ready=LOADED.contains(sampleId);
            if(!ready)PENDING.put(sampleId,volume);
        }
        if(ready){
            try{int stream=pool.play(sampleId,volume,volume,1,0,rateFor(effect));if(stream==0)fallback(effect,volume);}catch(Exception ex){fallback(effect,volume);}
        }
    }

    private static int resFor(int e){
        switch(e){
            case CRUNCH:case ENGINE:case TRAIN:case CAR_ARRIVE:case MELT:case DRIP:return R.raw.sfx_crunch;
            case SNOW_SET:case HIT:case CAR_DOOR:case TRAIN_DOOR:case PARCEL:case CLOTH:return R.raw.sfx_thump;
            case SLED:return R.raw.sfx_sled;
            case UI:case TICKET:return R.raw.sfx_ui;
            default:return R.raw.sfx_ready;
        }
    }

    private static float rateFor(int e){
        switch(e){
            case ERROR:case WRONG:return .68f;
            case ENGINE:case TRAIN:case CAR_ARRIVE:return .58f;
            case MELT:return .82f;
            case DRIP:return 1.65f;
            case SCHOOL_BELL:return 1.55f;
            case CORE:case MEMORY:case MITTEN:return 1.28f;
            case PHONE:return 1.18f;
            case CORRECT:return 1.20f;
            case COMPLETE:case ARRIVAL:return .92f;
            case ITEM:case CLOTH:return 1.35f;
            default:return 1f;
        }
    }

    private static int effectForSample(int sampleId){
        synchronized(LOCK){for(Map.Entry<Integer,Integer> e:SAMPLES.entrySet())if(e.getValue()==sampleId)return e.getKey();}
        return UI;
    }

    private static void fallback(int effect,float volume){
        try{
            int tone;
            switch(effect){
                case ERROR:case WRONG:tone=ToneGenerator.TONE_PROP_NACK;break;
                case COMPLETE:case ARRIVAL:case CORRECT:tone=ToneGenerator.TONE_PROP_ACK;break;
                case PHONE:tone=ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD;break;
                default:tone=ToneGenerator.TONE_PROP_BEEP;break;
            }
            ToneGenerator tg=new ToneGenerator(AudioManager.STREAM_MUSIC,Math.max(45,Math.min(100,(int)(volume*100))));
            tg.startTone(tone,effect==COMPLETE||effect==ARRIVAL?220:90);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->{try{tg.release();}catch(Exception ignored){}},300);
        }catch(Exception ignored){}
    }
}
