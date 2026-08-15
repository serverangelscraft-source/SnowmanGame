from pathlib import Path

path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
s = path.read_text(encoding="utf-8")


def rep(old: str, new: str, label: str) -> None:
    global s
    if old not in s:
        raise SystemExit(f"v12.2 story-order patch failed at: {label}")
    s = s.replace(old, new, 1)


rep(
    "        if(!getIntent().getBooleanExtra(\"skip_year2_story\",false)){\n            SharedPreferences progress=seasonalProgress;\n            int storyYear=Math.max(1,Math.min(7,progress.getInt(\"life_year\",1)));\n            if(storyYear==2&&!progress.getBoolean(\"year2_story_complete\",false)){\n                startActivity(new Intent(this,YearTwoActivity.class));\n                finish();\n                return;\n            }\n        }",
    "        if(seasonalYear==2&&!seasonalProgress.getBoolean(\"year2_story_order_v122\",false)){\n            seasonalProgress.edit().putBoolean(\"year2_story_complete\",false).putBoolean(\"year2_research_pending\",false).putBoolean(\"year2_story_order_v122\",true).apply();\n        }\n        if(seasonalYear==2&&seasonalProgress.getBoolean(\"year2_research_pending\",false)&&!seasonalProgress.getBoolean(\"year2_story_complete\",false)){\n            startActivity(new Intent(this,YearTwoActivity.class));\n            finish();\n            return;\n        }",
    "build before research routing",
)

rep(
    "        void addTrack(){\n            long now=SystemClock.elapsedRealtime();if(now-lastTrackAt<70)return;lastTrackAt=now;\n            tracks.add(new Track(rollX,rollY,Math.max(dp(8),rollingRadius()*.70f),rollAngle,now));\n            while(tracks.size()>34)tracks.remove(0);\n        }\n        void drawTracks(Canvas c){\n            long now=SystemClock.elapsedRealtime();\n            for(int i=tracks.size()-1;i>=0;i--){\n                Track tr=tracks.get(i);long age=now-tr.born;if(age>5200){tracks.remove(i);continue;}\n                int a=(int)(95*(1-age/5200f));stroke.setColor(Color.argb(Math.max(0,a),92,158,188));stroke.setStrokeWidth(dp(1.2f));\n                c.save();c.rotate(tr.angle,tr.x,tr.y);c.drawOval(new RectF(tr.x-tr.r,tr.y-tr.r*.33f,tr.x+tr.r,tr.y+tr.r*.33f),stroke);c.restore();\n            }\n        }",
    "        void addTrack(){\n            long now=SystemClock.elapsedRealtime();if(now-lastTrackAt<115)return;lastTrackAt=now;\n            tracks.add(new Track(rollX,rollY,Math.max(dp(7),rollingRadius()*.48f),rollAngle,now));\n            while(tracks.size()>9)tracks.remove(0);\n        }\n        void drawTracks(Canvas c){\n            long now=SystemClock.elapsedRealtime();\n            for(int i=tracks.size()-1;i>=0;i--){\n                Track tr=tracks.get(i);long age=now-tr.born;if(age>850){tracks.remove(i);continue;}\n                int a=(int)(58*(1-age/850f));p.setColor(Color.argb(Math.max(0,a),146,198,221));\n                c.save();c.rotate(tr.angle,tr.x,tr.y);c.drawOval(new RectF(tr.x-tr.r,tr.y-tr.r*.23f,tr.x+tr.r,tr.y+tr.r*.23f),p);c.restore();\n            }\n        }",
    "clean short snow tracks",
)

rep(
    "            String journeyLabel=year>=7?\"ДО ШКІЛЬНИХ ПРИГОД\":(year>=2?\"НА САНЧАТА • ДО ВОКЗАЛУ\":\"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ\");",
    "            boolean researchNeeded=year==2&&!prefs.getBoolean(\"year2_story_complete\",false);\n            String journeyLabel=year>=7?\"ДО ШКІЛЬНИХ ПРИГОД\":(researchNeeded?\"ДОСЛІДИТИ СНІГ • РІК 2\":(year>=2?\"НА САНЧАТА • ДО ВОКЗАЛУ\":\"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ\"));",
    "year2 result button label",
)

rep(
    "                    if(journeyBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,DeliveryActivity.class));((Activity)ctx).finish();return true;}",
    "                    if(journeyBtn.contains(x,y)){\n                        if(year==2&&!prefs.getBoolean(\"year2_story_complete\",false)){prefs.edit().putBoolean(\"year2_research_pending\",true).apply();ctx.startActivity(new Intent(ctx,YearTwoActivity.class));}\n                        else ctx.startActivity(new Intent(ctx,DeliveryActivity.class));\n                        ((Activity)ctx).finish();return true;\n                    }",
    "launch research only after completed build",
)

path.write_text(s, encoding="utf-8")
print("Applied SnowmanGame v12.2 Year 2 story order + clean tracks")
