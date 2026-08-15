from pathlib import Path

path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
year2_path = Path("app/src/main/java/com/snowmangame/YearTwoActivity.java")
s = path.read_text(encoding="utf-8")
y2 = year2_path.read_text(encoding="utf-8")


def rep(old: str, new: str, label: str) -> None:
    global s
    if old not in s:
        raise SystemExit(f"v12.2 story-order patch failed in MainActivity at: {label}")
    s = s.replace(old, new, 1)


def rep_y2(old: str, new: str, label: str) -> None:
    global y2
    if old not in y2:
        raise SystemExit(f"v12.2 story-order patch failed in YearTwoActivity at: {label}")
    y2 = y2.replace(old, new, 1)


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

rep_y2(
    "if(stage==INTRO)return\"Новий сніг — нові правила\";",
    "if(stage==INTRO)return\"Після ліплення — дослідження\";",
    "intro title",
)
rep_y2(
    "if(stage==INTRO)return\"Цього року мало просто катати кулі — спочатку роздивись світ.\";",
    "if(stage==INTRO)return\"Сніговика вже зібрано. Тепер досліди місцевий сніг.\";",
    "intro subtitle",
)
rep_y2(
    "c.drawText(\"«Я вже не просто Малюк.»\",bubble.centerX(),bubble.top+dp(31),text);text.setTextSize(tx(8.4f));text.setColor(Color.rgb(95,130,147));c.drawText(\"Другий рік починається з дослідження.\",bubble.centerX(),bubble.top+dp(57),text);c.drawText(\"Подивимось, який тут сніг.\",bubble.centerX(),bubble.top+dp(78),text);",
    "c.drawText(\"«Сніговика зліплено. Я готовий!»\",bubble.centerX(),bubble.top+dp(31),text);text.setTextSize(tx(8.4f));text.setColor(Color.rgb(95,130,147));c.drawText(\"Тепер перевірю, який тут сніг.\",bubble.centerX(),bubble.top+dp(57),text);c.drawText(\"Може, зима щось приховала?\",bubble.centerX(),bubble.top+dp(78),text);",
    "intro bubble",
)
rep_y2(
    "c.drawText(\"Це знахідка, а не заміна шарфа чи рук.\",card.centerX(),card.top+dp(113),text);",
    "c.drawText(\"Сховаємо її до зустрічі з власником.\",card.centerX(),card.top+dp(113),text);",
    "natural mitten story copy",
)
rep_y2(
    "c.drawText(\"Рукавичку збережено як пам'ять Року 2\",card.centerX(),card.top+dp(88),text);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(55,130,104));c.drawText(\"Далі — ліплення. Без повторної посилки й вокзалу.\",card.centerX(),card.top+dp(111),text);drawAction(c,\"ЛІПИТИ СНІГОВИКА • РІК 2\",true);",
    "c.drawText(\"Рукавичка чекає на свого власника\",card.centerX(),card.top+dp(88),text);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(55,130,104));c.drawText(\"Сніговик уже зліплений — час вирушати далі.\",card.centerX(),card.top+dp(111),text);drawAction(c,\"НА САНЧАТА • ДО ВОКЗАЛУ\",true);",
    "ready card and next action",
)
rep_y2(
    "        void drawMitten(Canvas c,float x,float y,float s){p.setColor(Color.rgb(55,137,194));RectF palm=new RectF(x-dp(17)*s,y-dp(18)*s,x+dp(17)*s,y+dp(20)*s);c.drawRoundRect(palm,dp(9)*s,dp(9)*s,p);c.drawRoundRect(new RectF(x+dp(10)*s,y-dp(5)*s,x+dp(27)*s,y+dp(9)*s),dp(7)*s,dp(7)*s,p);p.setColor(Color.WHITE);for(int i=0;i<6;i++){double a=i*Math.PI/3;float x2=x+(float)Math.cos(a)*dp(8)*s,y2=y+(float)Math.sin(a)*dp(8)*s;c.drawCircle(x2,y2,dp(1.5f)*s,p);}c.drawCircle(x,y,dp(2.1f)*s,p);}",
    "        void drawMitten(Canvas c,float x,float y,float s){\n            p.setColor(Color.rgb(55,137,194));\n            RectF palm=new RectF(x-dp(14)*s,y-dp(25)*s,x+dp(14)*s,y+dp(13)*s);c.drawRoundRect(palm,dp(13)*s,dp(13)*s,p);\n            c.save();c.rotate(-32,x+dp(12)*s,y+dp(1)*s);c.drawRoundRect(new RectF(x+dp(7)*s,y-dp(4)*s,x+dp(28)*s,y+dp(10)*s),dp(7)*s,dp(7)*s,p);c.restore();\n            p.setColor(Color.rgb(43,113,169));c.drawRoundRect(new RectF(x-dp(16)*s,y+dp(10)*s,x+dp(16)*s,y+dp(25)*s),dp(5)*s,dp(5)*s,p);\n            p.setColor(Color.argb(210,255,255,255));c.drawRoundRect(new RectF(x-dp(13)*s,y+dp(12)*s,x+dp(13)*s,y+dp(15)*s),dp(2)*s,dp(2)*s,p);\n            p.setColor(Color.WHITE);float cy=y-dp(5)*s;for(int i=0;i<6;i++){double a=i*Math.PI/3;float x2=x+(float)Math.cos(a)*dp(7)*s,y2=cy+(float)Math.sin(a)*dp(7)*s;c.drawCircle(x2,y2,dp(1.45f)*s,p);}c.drawCircle(x,cy,dp(2)*s,p);\n        }",
    "recognizable mitten shape",
)
rep_y2(
    "        void finishStory(){prefs.edit().putBoolean(\"year2_story_complete\",true).putBoolean(\"year2_mitten_found\",true).apply();Intent i=new Intent(ctx,MainActivity.class);i.putExtra(\"skip_year2_story\",true);ctx.startActivity(i);((Activity)ctx).finish();}",
    "        void finishStory(){prefs.edit().putBoolean(\"year2_story_complete\",true).putBoolean(\"year2_mitten_found\",true).putBoolean(\"year2_research_pending\",false).apply();Intent i=new Intent(ctx,DeliveryActivity.class);ctx.startActivity(i);((Activity)ctx).finish();}",
    "research continues to transport",
)

path.write_text(s, encoding="utf-8")
year2_path.write_text(y2, encoding="utf-8")
print("Applied SnowmanGame v12.2 Year 2 story order, mitten and clean tracks")
