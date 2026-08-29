from pathlib import Path
import math, random, struct, wave

SR=22050
RAW=Path("app/src/main/res/raw")
RAW.mkdir(parents=True,exist_ok=True)
rng=random.Random(20260829)

def env(n,attack_ms=20,release_ms=80):
    a=max(1,int(SR*attack_ms/1000))
    r=max(1,int(SR*release_ms/1000))
    out=[1.0]*n
    for i in range(min(a,n)): out[i]=i/max(1,a)
    for j in range(min(r,n)):
        idx=n-1-j
        out[idx]=min(out[idx],j/max(1,r))
    return out

def lowpass(x,cutoff):
    dt=1.0/SR
    rc=1.0/(2*math.pi*cutoff)
    alpha=dt/(rc+dt)
    y=[0.0]*len(x)
    if not x:return y
    y[0]=x[0]
    for i in range(1,len(x)): y[i]=y[i-1]+alpha*(x[i]-y[i-1])
    return y

def highpass(x,cutoff):
    lp=lowpass(x,cutoff)
    return [a-b for a,b in zip(x,lp)]

def save(name,x,peak):
    mean=sum(x)/max(1,len(x))
    x=[v-mean for v in x]
    m=max([abs(v) for v in x] or [1.0]) or 1.0
    scale=peak/m
    pcm=[max(-32767,min(32767,int(v*scale*32767))) for v in x]
    with wave.open(str(RAW/name),"wb") as w:
        w.setnchannels(1);w.setsampwidth(2);w.setframerate(SR)
        w.writeframes(struct.pack("<"+"h"*len(pcm),*pcm))

def noise(n):
    return [rng.uniform(-1.0,1.0) for _ in range(n)]

# Quiet UI tap: round, low-mid, no click transient.
dur=.105;n=int(SR*dur);e=env(n,14,45)
x=[]
for i in range(n):
    t=i/SR
    v=(math.sin(2*math.pi*430*t)*.8+math.sin(2*math.pi*645*t)*.18)*e[i]*math.exp(-t*13)
    x.append(v)
save("sfx_ui_soft.wav",x,.12)

# Snow crunch: filtered granular noise with a gentle low body.
dur=.18;n=int(SR*dur);tvals=[i/SR for i in range(n)];e=env(n,22,75)
ns=highpass(lowpass(noise(n),800),100)
x=[]
for i,t in enumerate(tvals):
    grain=.45+.25*math.sin(2*math.pi*13*t)+.12*math.sin(2*math.pi*23*t+1.2)
    low=math.sin(2*math.pi*70*t)*.08
    x.append((ns[i]*.65*grain+low)*e[i])
save("sfx_snow_crunch_soft.wav",x,.16)

# Packed snow placement: soft "whump" with slow attack, never a hard thump.
dur=.24;n=int(SR*dur);e=env(n,30,90);ns=lowpass(noise(n),420)
phase=0.0;x=[]
for i in range(n):
    t=i/SR
    f=82-16*(t/dur)
    phase+=2*math.pi*f/SR
    body=math.sin(phase)*math.exp(-t*8.5)
    soft=ns[i]*math.exp(-t*10)*.12
    x.append((body*.60+soft)*e[i])
save("sfx_snow_set_soft.wav",x,.17)

# Cloth / mitten / parcel: slow rustle, deliberately no impact transient.
dur=.22;n=int(SR*dur);e=env(n,30,80)
ns=highpass(lowpass(noise(n),1300),200);x=[]
for i in range(n):
    t=i/SR
    sw=.5+.25*math.sin(2*math.pi*5*t+.7)+.10*math.sin(2*math.pi*13*t)
    x.append(ns[i]*sw*e[i])
save("sfx_cloth_soft.wav",x,.12)

# Warm success / memory chime.
dur=.52;n=int(SR*dur);e=env(n,25,150);x=[0.0]*n
for f,delay,gain in [(523.25,0,.42),(659.25,.075,.28),(783.99,.15,.20)]:
    for i in range(n):
        t=i/SR-delay
        if t>=0:x[i]+=math.sin(2*math.pi*f*t)*math.exp(-t*5.6)*gain
x=[x[i]*e[i] for i in range(n)]
save("sfx_chime_soft.wav",x,.14)

# Transport: muted low hum instead of slam / engine crack.
dur=.38;n=int(SR*dur);e=env(n,55,120);ns=lowpass(noise(n),300);x=[]
for i in range(n):
    t=i/SR
    hum=math.sin(2*math.pi*68*t)+.28*math.sin(2*math.pi*102*t)+.08*math.sin(2*math.pi*136*t)
    body=.7+.3*math.sin(math.pi*t/dur)
    x.append((hum*.45+ns[i]*.08)*e[i]*body)
save("sfx_transport_soft.wav",x,.15)

# School bell: mellow and low in level.
dur=.65;n=int(SR*dur);e=env(n,28,170);x=[]
for i in range(n):
    t=i/SR
    v=(math.sin(2*math.pi*590*t)*.52+math.sin(2*math.pi*885*t)*.18+math.sin(2*math.pi*1180*t)*.06)
    x.append(v*math.exp(-t*4.4)*e[i])
save("sfx_bell_soft.wav",x,.13)

# Error: quiet descending tone, not NACK/beep.
dur=.26;n=int(SR*dur);e=env(n,25,100);phase=0.0;x=[]
for i in range(n):
    t=i/SR;f=320-70*(t/dur);phase+=2*math.pi*f/SR
    x.append(math.sin(phase)*math.exp(-t*6.5)*e[i])
save("sfx_error_soft.wav",x,.11)

# Summer water drop: tiny, soft tail.
dur=.20;n=int(SR*dur);e=env(n,16,80);phase=0.0;x=[]
for i in range(n):
    t=i/SR;f=760-320*(t/dur);phase+=2*math.pi*f/SR
    x.append(math.sin(phase)*math.exp(-t*12)*e[i])
save("sfx_drip_soft.wav",x,.09)

soundfx=Path("app/src/main/java/com/snowmangame/SoundFx.java")
soundfx.write_text(r'''package com.snowmangame;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.SystemClock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Soft mobile SFX: bundled WAVs with gentle envelopes; no electronic tone fallback. */
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
        boolean on=!enabled(p);
        p.edit().putBoolean("sound_enabled",on).apply();
        if(on)play(c,UI,true);
        return on;
    }

    public static void play(Context c,int effect){play(c,effect,false);}
    public static void crunch(Context c){play(c,CRUNCH,false);}

    private static void ensure(Context c){
        synchronized(LOCK){
            if(pool!=null)return;
            app=c.getApplicationContext();
            AudioAttributes attrs=new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build();
            pool=new SoundPool.Builder().setMaxStreams(6).setAudioAttributes(attrs).build();
            pool.setOnLoadCompleteListener((sp,sampleId,status)->{
                Float volume;
                synchronized(LOCK){
                    if(status==0){LOADED.add(sampleId);volume=PENDING.remove(sampleId);}
                    else volume=PENDING.remove(sampleId);
                }
                int effectForSample=effectForSample(sampleId);
                if(status==0&&volume!=null&&app!=null&&enabled(app)){
                    int stream=sp.play(sampleId,volume,volume,1,0,1f);
                    if(stream==0)playMedia(app,resFor(effectForSample),volume);
                }else if(status!=0&&app!=null&&volume!=null&&enabled(app)){
                    playMedia(app,resFor(effectForSample),volume);
                }
            });
        }
    }

    private static void play(Context c,int effect,boolean ignorePref){
        if(c==null)return;
        Context a=c.getApplicationContext();
        SharedPreferences p=a.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
        if(!ignorePref&&!enabled(p))return;

        long now=SystemClock.elapsedRealtime();
        int throttle=effect==CRUNCH?160:(effect==SLED?420:(effect==UI?90:70));
        synchronized(LAST){
            Long last=LAST.get(effect);
            if(last!=null&&now-last<throttle)return;
            LAST.put(effect,now);
        }

        float pref=Math.max(0f,Math.min(1f,p.getInt("sound_volume",58)/100f));
        float volume=Math.min(.62f,pref)*gainFor(effect);
        if(volume<.02f)return;

        ensure(a);
        int sampleId;
        boolean ready;
        synchronized(LOCK){
            Integer id=SAMPLES.get(effect);
            if(id==null){
                try{id=pool.load(a,resFor(effect),1);}
                catch(Exception ex){playMedia(a,resFor(effect),volume);return;}
                SAMPLES.put(effect,id);
            }
            sampleId=id;
            ready=LOADED.contains(sampleId);
            if(!ready)PENDING.put(sampleId,volume);
        }
        if(ready){
            try{
                int stream=pool.play(sampleId,volume,volume,1,0,1f);
                if(stream==0)playMedia(a,resFor(effect),volume);
            }catch(Exception ex){playMedia(a,resFor(effect),volume);}
        }
    }

    private static float gainFor(int e){
        switch(e){
            case UI:case TICKET:return .55f;
            case CLOTH:case ITEM:case MITTEN:case PARCEL:return .62f;
            case ERROR:case WRONG:return .68f;
            case CRUNCH:return .82f;
            case SNOW_SET:case HIT:return .78f;
            case ENGINE:case TRAIN:case CAR_ARRIVE:case CAR_DOOR:case TRAIN_DOOR:case SLED:return .72f;
            case SCHOOL_BELL:return .70f;
            case DRIP:return .58f;
            default:return .74f;
        }
    }

    private static int resFor(int e){
        switch(e){
            case CRUNCH:case PLAY:case MELT:return R.raw.sfx_snow_crunch_soft;
            case SNOW_SET:case HIT:return R.raw.sfx_snow_set_soft;
            case CLOTH:case ITEM:case MITTEN:case PARCEL:return R.raw.sfx_cloth_soft;
            case UI:case TICKET:case PHONE:return R.raw.sfx_ui_soft;
            case ERROR:case WRONG:return R.raw.sfx_error_soft;
            case ENGINE:case TRAIN:case CAR_ARRIVE:case CAR_DOOR:case TRAIN_DOOR:case SLED:return R.raw.sfx_transport_soft;
            case SCHOOL_BELL:return R.raw.sfx_bell_soft;
            case DRIP:return R.raw.sfx_drip_soft;
            default:return R.raw.sfx_chime_soft;
        }
    }

    private static int effectForSample(int sampleId){
        synchronized(LOCK){
            for(Map.Entry<Integer,Integer> e:SAMPLES.entrySet())if(e.getValue()==sampleId)return e.getKey();
        }
        return UI;
    }

    private static void playMedia(Context c,int res,float volume){
        try{
            final MediaPlayer mp=MediaPlayer.create(c,res);
            if(mp==null)return;
            mp.setVolume(volume,volume);
            mp.setOnCompletionListener(MediaPlayer::release);
            mp.setOnErrorListener((m,what,extra)->{m.release();return true;});
            mp.start();
        }catch(Exception ignored){}
    }
}
''',encoding="utf-8")

gradle=Path("app/build.gradle")
g=gradle.read_text(encoding="utf-8")
import re
g=re.sub(r'versionCode\s+\d+','versionCode 29',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.3.3"',g)
gradle.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.3.3 soft SFX: no sharp thumps or electronic fallback tones")
