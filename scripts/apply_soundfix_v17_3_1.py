from pathlib import Path

paths={
    "main":Path("app/src/main/java/com/snowmangame/MainActivity.java"),
    "delivery":Path("app/src/main/java/com/snowmangame/DeliveryActivity.java"),
    "journey":Path("app/src/main/java/com/snowmangame/JourneyActivity.java"),
    "school":Path("app/src/main/java/com/snowmangame/SchoolActivity.java"),
}
src={k:p.read_text(encoding="utf-8") for k,p in paths.items()}


def rep(key,old,new,label):
    if old not in src[key]:
        raise SystemExit(f"v17.3.1 sound self-audit patch failed in {key} at: {label}")
    src[key]=src[key].replace(old,new,1)

# v17.3 optional hook missed the later v14 wording of the winter-finish line.
rep(
    "main",
    '            buzz(65);playTone(ToneGenerator.TONE_PROP_ACK,180);showFeedback(missionSuccess?"ЗИМА ПРОЖИТА • МІСІЯ +250":"ЗИМА ПРОЖИТА",true);invalidate();',
    '            buzz(65);SoundFx.play(ctx,SoundFx.COMPLETE);showFeedback(missionSuccess?"ЗИМА ПРОЖИТА • МІСІЯ +250":"ЗИМА ПРОЖИТА",true);invalidate();',
    "winter complete sound after v14 wording",
)

# v17.3 claimed transport sound, but the Year 1/2 sled DeliveryActivity had no hook at all.
rep(
    "delivery",
    '        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();invalidate();}',
    '        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();if(s==OPENED)SoundFx.play(ctx,SoundFx.PARCEL);else if(s==RIDE)SoundFx.play(ctx,SoundFx.SLED);else if(s==ARRIVED)SoundFx.play(ctx,SoundFx.ARRIVAL);invalidate();}',
    "delivery stage sounds",
)
rep(
    "delivery",
    '        void drawRide(Canvas c,float t){\n            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;',
    '        void drawRide(Canvas c,float t){\n            SoundFx.play(ctx,SoundFx.SLED);\n            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;',
    "sled glide cue during ride",
)

# Train boarding must not reuse the car-door sound.
rep(
    "journey",
    'else if(s==BOARDING)SoundFx.play(getContext(),SoundFx.CAR_DOOR);',
    'else if(s==BOARDING)SoundFx.play(getContext(),SoundFx.TRAIN_DOOR);',
    "train door identity",
)

# v17.3 optional hook missed the actual v17.1 completeLesson implementation.
rep(
    "school",
    '        void completeLesson(){int days=Math.max(0,prefs.getInt("school_days",0))+1;prefs.edit().putBoolean("school_first_day_complete",true).putBoolean("school_unlocked",true).putInt("school_days",days).putInt("school_stage",DONE).apply();stage=DONE;feedback="";buzz(45);invalidate();}',
    '        void completeLesson(){int days=Math.max(0,prefs.getInt("school_days",0))+1;prefs.edit().putBoolean("school_first_day_complete",true).putBoolean("school_unlocked",true).putInt("school_days",days).putInt("school_stage",DONE).apply();stage=DONE;feedback="";buzz(45);SoundFx.play(ctx,SoundFx.COMPLETE);invalidate();}',
    "school lesson complete sound",
)

for key,path in paths.items():
    path.write_text(src[key],encoding="utf-8")
print("Applied SnowmanGame v17.3.1 sound self-audit fixes")
