from pathlib import Path

main_path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
journey_path = Path("app/src/main/java/com/snowmangame/JourneyActivity.java")
main = main_path.read_text(encoding="utf-8")
journey = journey_path.read_text(encoding="utf-8")


def rep_main(old: str, new: str, label: str) -> None:
    global main
    if old not in main:
        raise SystemExit(f"v12 season patch failed in MainActivity at: {label}")
    main = main.replace(old, new, 1)


def rep_journey(old: str, new: str, label: str) -> None:
    global journey
    if old not in journey:
        raise SystemExit(f"v12 season patch failed in JourneyActivity at: {label}")
    journey = journey.replace(old, new, 1)


rep_main(
    "        if(!getIntent().getBooleanExtra(\"skip_year2_story\",false)){\n            SharedPreferences progress=getSharedPreferences(\"snowman_game\",MODE_PRIVATE);",
    "        SharedPreferences seasonalProgress=getSharedPreferences(\"snowman_game\",MODE_PRIVATE);\n        int seasonalYear=Math.max(1,Math.min(7,seasonalProgress.getInt(\"life_year\",1)));\n        if(seasonalYear>=2&&!seasonalProgress.getBoolean(\"olx_scarf_owned\",false)){\n            seasonalProgress.edit().putBoolean(\"olx_scarf_owned\",true).apply();\n        }\n        if(!getIntent().getBooleanExtra(\"skip_summer\",false)&&seasonalProgress.getInt(\"summer_pending_year\",0)==seasonalYear){\n            startActivity(new Intent(this,SummerActivity.class));\n            finish();\n            return;\n        }\n        if(!getIntent().getBooleanExtra(\"skip_year2_story\",false)){\n            SharedPreferences progress=seasonalProgress;",
    "summer resume routing",
)

rep_main(
    "            baseR=clamp(Math.min(w*.205f,playH/4.05f),dp(34),dp(82));",
    "            float ageGrowth=1f+Math.min(.12f,(year-1)*.025f);\n            baseR=clamp(Math.min(w*.205f,playH/4.05f)*ageGrowth,dp(34),Math.min(dp(88),playH/3.95f));",
    "visible year growth",
)

rep_main(
    "                if(a.type==NOSE)label=\"Морква • мама\";\n                if(a.type==ARMS)label=\"ПАЛКА ЧОТКО\";",
    "                if(a.type==NOSE)label=\"Морква • мама\";\n                if(a.type==SCARF)label=prefs.getBoolean(\"olx_scarf_owned\",false)?\"Шарф • OLX ✓\":\"Шарф • OLX\";\n                if(a.type==ARMS)label=\"ПАЛКА ЧОТКО\";",
    "OLX scarf wardrobe label",
)

rep_main(
    "            else if(type==SCARF){p.setColor(Color.rgb(198,62,68));c.drawRoundRect(new RectF(x-s*.72f,y-s*.17f,x+s*.72f,y+s*.17f),dp(3),dp(3),p);}",
    "            else if(type==SCARF){p.setColor(Color.rgb(68,79,204));c.drawRoundRect(new RectF(x-s*.72f,y-s*.17f,x+s*.72f,y+s*.17f),dp(3),dp(3),p);}",
    "OLX scarf tray color",
)

rep_main(
    "            }else if(type==SCARF){\n                p.setColor(Color.argb(alpha,200,61,68));",
    "            }else if(type==SCARF){\n                p.setColor(Color.argb(alpha,68,79,204));",
    "OLX scarf worn color",
)

rep_main(
    "                float x=card.right-dp(45),y=card.centerY()+dp(4);Path n=new Path();n.moveTo(x-dp(24),y-dp(8));n.lineTo(x+dp(25),y);n.lineTo(x-dp(24),y+dp(8));n.close();p.setColor(Color.rgb(242,119,37));c.drawPath(n,p);\n            }else{",
    "                float x=card.right-dp(45),y=card.centerY()+dp(4);Path n=new Path();n.moveTo(x-dp(24),y-dp(8));n.lineTo(x+dp(25),y);n.lineTo(x-dp(24),y+dp(8));n.close();p.setColor(Color.rgb(242,119,37));c.drawPath(n,p);\n            }else if(giftType==SCARF){\n                text.setTextSize(tx(17));text.setColor(Color.rgb(68,79,204));c.drawText(\"ШАРФ З ГАРДЕРОБА\",card.left+dp(16),card.top+dp(47),text);\n                text.setTextSize(tx(9.5f));text.setColor(Color.rgb(55,91,110));c.drawText(\"Мама знайшла його на OLX поруч.\",card.left+dp(16),card.top+dp(72),text);\n                text.setTextSize(tx(8));text.setColor(Color.rgb(95,130,147));c.drawText(\"Майже новий. Носив один сніговик.\",card.left+dp(16),card.top+dp(94),text);\n                p.setColor(Color.rgb(68,79,204));c.drawRoundRect(new RectF(card.right-dp(82),card.centerY()-dp(8),card.right-dp(18),card.centerY()+dp(4)),dp(5),dp(5),p);\n                c.drawRoundRect(new RectF(card.right-dp(36),card.centerY(),card.right-dp(20),card.centerY()+dp(34)),dp(5),dp(5),p);\n            }else{",
    "OLX scarf story card",
)

rep_main(
    "                if(type==NOSE)showGift(NOSE);\n                if(type==ARMS)showGift(ARMS);",
    "                if(type==NOSE)showGift(NOSE);\n                if(type==SCARF){prefs.edit().putBoolean(\"olx_scarf_owned\",true).apply();showGift(SCARF);}\n                if(type==ARMS)showGift(ARMS);",
    "persist OLX scarf",
)

rep_journey(
    "            if(!yearAdvanced){yearAdvanced=true;year=nextYear;prefs.edit().putInt(\"life_year\",year).apply();int best=prefs.getInt(\"station_best\",9999);",
    "            if(!yearAdvanced){yearAdvanced=true;year=nextYear;prefs.edit().putInt(\"life_year\",year).putInt(\"summer_pending_year\",year).apply();int best=prefs.getInt(\"station_best\",9999);",
    "mark summer pending after arrival",
)

rep_journey(
    "        void startNewYear(){Intent i=new Intent(getContext(),MainActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}",
    "        void startNewYear(){int pending=prefs.getInt(\"summer_pending_year\",0),current=prefs.getInt(\"life_year\",1);Intent i=new Intent(getContext(),pending==current?SummerActivity.class:MainActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}",
    "route arrival through summer",
)

main_path.write_text(main, encoding="utf-8")
journey_path.write_text(journey, encoding="utf-8")
print("Applied SnowmanGame v12 OLX wardrobe + seasonal life cycle")
