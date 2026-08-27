from pathlib import Path

paths={
    "delivery":Path("app/src/main/java/com/snowmangame/DeliveryActivity.java"),
    "journey":Path("app/src/main/java/com/snowmangame/JourneyActivity.java"),
    "school":Path("app/src/main/java/com/snowmangame/SchoolActivity.java"),
}
src={k:p.read_text(encoding="utf-8") for k,p in paths.items()}


def rep(key,old,new,label):
    if old not in src[key]:
        raise SystemExit(f"v17.3.1 sound self-audit patch failed in {key} at: {label}")
    src[key]=src[key].replace(old,new,1)

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

# Do not stack two reward jingles at exactly the same moment after the first lesson.
rep(
    "school",
    'SoundFx.play(ctx,SoundFx.CORRECT);SoundFx.play(ctx,SoundFx.COMPLETE);invalidate();',
    'SoundFx.play(ctx,SoundFx.COMPLETE);invalidate();',
    "avoid doubled lesson-complete jingle",
)

for key,path in paths.items():
    path.write_text(src[key],encoding="utf-8")
print("Applied SnowmanGame v17.3.1 sound self-audit fixes")
