from pathlib import Path

path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
s = path.read_text(encoding="utf-8")


def rep(old: str, new: str, label: str) -> None:
    global s
    if old not in s:
        raise SystemExit(f"v10 polish patch failed at: {label}")
    s = s.replace(old, new, 1)

rep(
    "import android.content.SharedPreferences;\nimport android.graphics.*;",
    "import android.content.SharedPreferences;\nimport android.graphics.*;\nimport android.media.AudioManager;\nimport android.media.ToneGenerator;",
    "media imports",
)
rep(
    "import java.util.Random;",
    "import java.util.ArrayList;\nimport java.util.Random;",
    "collection import",
)
rep(
    "        final Accessory[] items=new Accessory[ACCESSORY_COUNT];\n\n        float density, textScale, safeTop, safeBottom;",
    "        final Accessory[] items=new Accessory[ACCESSORY_COUNT];\n        final ArrayList<Track> tracks=new ArrayList<>();\n        ToneGenerator tone;\n        long lastCrunchAt,feedbackUntil,lastTrackAt;\n        float rollAngle;\n        String feedbackText=\"\";\n        boolean feedbackGood;\n\n        float density, textScale, safeTop, safeBottom;",
    "polish fields",
)
rep(
    "            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);\n            bestScore=prefs.getInt(\"best_score\",0);",
    "            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);\n            try{tone=new ToneGenerator(AudioManager.STREAM_MUSIC,34);}catch(Exception ignored){}\n            bestScore=prefs.getInt(\"best_score\",0);",
    "tone init",
)
rep(
    "        void buzz(int ms){\n            if(vibrator==null||!vibrator.hasVibrator())return;\n            if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,80));\n            else vibrator.vibrate(ms);\n        }\n\n        @Override protected void onDraw(Canvas c){",
    "        void buzz(int ms){\n            if(vibrator==null||!vibrator.hasVibrator())return;\n            if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,80));\n            else vibrator.vibrate(ms);\n        }\n        void playTone(int type,int ms){if(tone!=null)try{tone.startTone(type,ms);}catch(Exception ignored){}}\n        void crunch(){long now=SystemClock.elapsedRealtime();if(now-lastCrunchAt>145){lastCrunchAt=now;playTone(ToneGenerator.TONE_PROP_BEEP2,24);}}\n        void showFeedback(String msg,boolean good){feedbackText=msg;feedbackGood=good;feedbackUntil=SystemClock.elapsedRealtime()+850;postInvalidateOnAnimation();}\n        void addTrack(){\n            long now=SystemClock.elapsedRealtime();if(now-lastTrackAt<70)return;lastTrackAt=now;\n            tracks.add(new Track(rollX,rollY,Math.max(dp(8),rollingRadius()*.70f),rollAngle,now));\n            while(tracks.size()>34)tracks.remove(0);\n        }\n        void drawTracks(Canvas c){\n            long now=SystemClock.elapsedRealtime();\n            for(int i=tracks.size()-1;i>=0;i--){\n                Track tr=tracks.get(i);long age=now-tr.born;if(age>5200){tracks.remove(i);continue;}\n                int a=(int)(95*(1-age/5200f));stroke.setColor(Color.argb(Math.max(0,a),92,158,188));stroke.setStrokeWidth(dp(1.2f));\n                c.save();c.rotate(tr.angle,tr.x,tr.y);c.drawOval(new RectF(tr.x-tr.r,tr.y-tr.r*.33f,tr.x+tr.r,tr.y+tr.r*.33f),stroke);c.restore();\n            }\n        }\n        void drawFeedback(Canvas c){\n            float w=Math.min(getWidth()-dp(40),dp(250)),h=dp(38),l=(getWidth()-w)/2,top=tipCard.bottom+dp(8);\n            RectF r=new RectF(l,top,l+w,top+h);p.setColor(feedbackGood?Color.argb(232,226,246,236):Color.argb(232,252,231,231));c.drawRoundRect(r,dp(15),dp(15),p);\n            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(feedbackGood?Color.rgb(48,126,96):Color.rgb(164,75,75));c.drawText(feedbackText,r.centerX(),r.centerY()+dp(3),text);\n        }\n\n        @Override protected void onDraw(Canvas c){",
    "polish methods",
)
rep(
    "            if(giftType>=0){\n                if(SystemClock.elapsedRealtime()<giftUntil){\n                    drawGift(c);\n                    postInvalidateDelayed(60);\n                }else giftType=-1;\n            }\n            if(startTime!=0&&!finished)postInvalidateDelayed(500);",
    "            if(giftType>=0){\n                if(SystemClock.elapsedRealtime()<giftUntil){\n                    drawGift(c);\n                    postInvalidateDelayed(60);\n                }else giftType=-1;\n            }\n            if(SystemClock.elapsedRealtime()<feedbackUntil&&!finished){drawFeedback(c);postInvalidateDelayed(50);}\n            if(startTime!=0&&!finished)postInvalidateDelayed(500);",
    "feedback drawing",
)
rep(
    "            stroke.setColor(Color.argb(100,111,177,208));stroke.setStrokeWidth(dp(1.3f));c.drawRoundRect(interaction,dp(23),dp(23),stroke);\n            drawRollingBall(c,rollX,rollY,rollingRadius());",
    "            stroke.setColor(Color.argb(100,111,177,208));stroke.setStrokeWidth(dp(1.3f));c.drawRoundRect(interaction,dp(23),dp(23),stroke);\n            drawTracks(c);\n            drawRollingBall(c,rollX,rollY,rollingRadius());",
    "track drawing",
)
rep(
    "            stroke.setColor(Color.argb(90,91,153,184));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);\n        }",
    "            stroke.setColor(Color.argb(90,91,153,184));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);\n            c.save();c.rotate(rollAngle,x,y);stroke.setColor(Color.argb(90,116,177,204));stroke.setStrokeWidth(Math.max(dp(1),r*.025f));\n            c.drawArc(new RectF(x-r*.55f,y-r*.35f,x+r*.35f,y+r*.45f),205,105,false,stroke);\n            c.drawArc(new RectF(x-r*.25f,y-r*.55f,x+r*.60f,y+r*.28f),25,95,false,stroke);c.restore();\n        }",
    "rolling rotation marks",
)
rep(
    "                        rollProgress=Math.min(100,rollProgress+d/required*100);\n                        rollX=x;rollY=y;keepBallInside();lastX=x;lastY=y;\n                        if(rollProgress>=100){rollProgress=100;ballReady=true;rolling=false;tip=\"Куля готова — постав її в контур\";buzz(26);}",
    "                        rollProgress=Math.min(100,rollProgress+d/required*100);\n                        rollX=x;rollY=y;keepBallInside();rollAngle=(rollAngle+d/Math.max(dp(8),rollingRadius())*34f)%360f;addTrack();crunch();lastX=x;lastY=y;\n                        if(rollProgress>=100){rollProgress=100;ballReady=true;rolling=false;tip=\"Куля готова — постав її в контур\";buzz(26);playTone(ToneGenerator.TONE_PROP_ACK,90);showFeedback(\"КУЛЯ ГОТОВА\",true);}",
    "rolling feel",
)
rep(
    "                balls++;rollProgress=0;ballReady=false;buzz(38);\n                rollX=interaction.centerX();rollY=interaction.centerY()+dp(3);",
    "                balls++;rollProgress=0;ballReady=false;buzz(q>=90?48:34);playTone(q>=90?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_BEEP,95);showFeedback((q>=90?\"ТОЧНО! • \":\"ТОЧНІСТЬ • \")+q+\"%\"+(combo>1?\" • КОМБО x\"+combo:\"\"),true);tracks.clear();\n                rollX=interaction.centerX();rollY=interaction.centerY()+dp(3);",
    "ball success feedback",
)
rep(
    "                tip=\"Не попав у контур — постав точніше\";rollX=interaction.centerX();rollY=interaction.centerY();buzz(12);",
    "                tip=\"Не попав у контур — постав точніше\";rollX=interaction.centerX();rollY=interaction.centerY();buzz(12);playTone(ToneGenerator.TONE_PROP_NACK,95);showFeedback(\"НЕ В КОНТУРІ • СПРОБУЙ ЩЕ\",false);tracks.clear();",
    "ball fail feedback",
)
rep(
    "                buzz(24);\n                if(type==NOSE)showGift(NOSE);",
    "                buzz(q>=90?34:24);playTone(q>=90?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_BEEP,80);showFeedback((q>=90?\"ЧІТКО! • \":\"ДЕТАЛЬ • \")+q+\"%\"+(combo>1?\" • КОМБО x\"+combo:\"\"),true);\n                if(type==NOSE)showGift(NOSE);",
    "accessory success feedback",
)
rep(
    "            }else{combo=0;tip=\"Занадто далеко — постав точніше\";buzz(12);}\n            invalidate();",
    "            }else{combo=0;tip=\"Занадто далеко — постав точніше\";buzz(12);playTone(ToneGenerator.TONE_PROP_NACK,80);showFeedback(\"ТРОХИ БЛИЖЧЕ ДО МІСЦЯ\",false);}\n            invalidate();",
    "accessory fail feedback",
)
rep(
    "            buzz(65);invalidate();",
    "            buzz(65);playTone(ToneGenerator.TONE_PROP_ACK,180);showFeedback(missionSuccess?\"РІК ЗАВЕРШЕНО • МІСІЯ +250\":\"РІК ЗАВЕРШЕНО\",true);invalidate();",
    "finish feedback",
)
rep(
    "            sponsorScene=false;sponsorRewarded=false;coinsAwarded=false;runCoins=0;rollProgress=0;startTime=0;finishSeconds=0;missionSuccess=false;mission=rnd.nextInt(3);giftType=-1;",
    "            sponsorScene=false;sponsorRewarded=false;coinsAwarded=false;runCoins=0;rollProgress=0;rollAngle=0;startTime=0;finishSeconds=0;missionSuccess=false;mission=rnd.nextInt(3);giftType=-1;tracks.clear();feedbackUntil=0;",
    "reset polish state",
)
rep(
    "        static class Accessory{\n            final int type;final String name;final RectF slot=new RectF();",
    "        @Override protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(tone!=null){try{tone.release();}catch(Exception ignored){}tone=null;}}\n\n        static class Track{final float x,y,r,angle;final long born;Track(float x,float y,float r,float angle,long born){this.x=x;this.y=y;this.r=r;this.angle=angle;this.born=born;}}\n\n        static class Accessory{\n            final int type;final String name;final RectF slot=new RectF();",
    "cleanup and track class",
)

path.write_text(s, encoding="utf-8")
print("Applied SnowmanGame v10 polish patch")
