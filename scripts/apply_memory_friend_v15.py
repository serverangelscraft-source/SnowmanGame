from pathlib import Path

path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
s = path.read_text(encoding="utf-8")


def rep(old: str, new: str, label: str) -> None:
    global s
    if old not in s:
        raise SystemExit(f"v15 memory/friend patch failed at: {label}")
    s = s.replace(old, new, 1)

# Existing saves that already reached Year 4+ cannot legitimately still carry the loose mitten.
rep(
    "        setContentView(new SnowmanView(this));",
    "        if(seasonalYear>=4&&!seasonalProgress.contains(\"year3_story_complete\")){\n            seasonalProgress.edit().putBoolean(\"year3_story_complete\",true).putBoolean(\"year3_friend_met\",true).putBoolean(\"year2_mitten_returned\",true).putString(\"year3_friend_name\",\"Сніжик\").apply();\n        }\n        setContentView(new SnowmanView(this));",
    "migrate old Year 4+ saves",
)

# Add a real memory-room control to the finish screen without making the result card taller.
rep(
    "        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();",
    "        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), memoryBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();",
    "memory button field",
)

# Give Year 3 its own gameplay identity in the ordinary snowman-building HUD.
rep(
    "            if(mission==0)return \"МІСІЯ: точність куль ≥ \"+acc+\"%\";",
    "            if(year==3){\n                if(mission==0)return \"ПУСТОЩІ ЗИМИ 3: кулі ≥ \"+acc+\"%\";\n                if(mission==1)return \"ПУСТОЩІ ЗИМИ 3: час ≤ \"+sec+\" с\";\n                return \"ПУСТОЩІ ЗИМИ 3: декор ≥ \"+acc+\"%\";\n            }\n            if(mission==0)return \"МІСІЯ: точність куль ≥ \"+acc+\"%\";",
    "Year 3 mission identity",
)

# Year 3 now resolves the mitten and meets the first friend before the already-existing Uklon trip.
rep(
    '            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(researchNeeded?"ДОСЛІДИТИ СНІГ • РІК 2":(year>=3?"ВИКЛИКАТИ ВОДІЯ • ДО ВОКЗАЛУ":(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ")));',
    '            boolean friendStoryNeeded=year==3&&!prefs.getBoolean("year3_story_complete",false);\n            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(researchNeeded?"ДОСЛІДИТИ СНІГ • РІК 2":(friendStoryNeeded?"ЗНАЙТИ ВЛАСНИКА РУКАВИЧКИ":(year>=3?"ВИКЛИКАТИ ВОДІЯ • ДО ВОКЗАЛУ":(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ"))));',
    "Year 3 friend button label",
)

rep(
    "            replayBtn.set(card.left+dp(45),card.bottom-dp(51),card.right-dp(45),card.bottom-dp(12));\n            p.setColor(Color.rgb(238,246,250));c.drawRoundRect(replayBtn,dp(16),dp(16),p);text.setTextSize(tx(8));text.setColor(Color.rgb(76,118,142));c.drawText(\"ЩЕ РАЗ У ЦЬОМУ РОЦІ\",replayBtn.centerX(),replayBtn.centerY()+dp(3),text);",
    "            float mid=card.centerX(),gap=dp(6);\n            memoryBtn.set(card.left+dp(22),card.bottom-dp(51),mid-gap/2,card.bottom-dp(12));\n            replayBtn.set(mid+gap/2,card.bottom-dp(51),card.right-dp(22),card.bottom-dp(12));\n            p.setColor(Color.rgb(232,242,237));c.drawRoundRect(memoryBtn,dp(16),dp(16),p);text.setTextSize(tx(7.8f));text.setColor(Color.rgb(62,112,91));c.drawText(\"СПОГАДИ\",memoryBtn.centerX(),memoryBtn.centerY()+dp(3),text);\n            p.setColor(Color.rgb(238,246,250));c.drawRoundRect(replayBtn,dp(16),dp(16),p);text.setTextSize(tx(7.8f));text.setColor(Color.rgb(76,118,142));c.drawText(\"ЩЕ РАЗ\",replayBtn.centerX(),replayBtn.centerY()+dp(3),text);",
    "split memories/replay controls",
)

rep(
    "                    if(journeyBtn.contains(x,y)){\n                        if(year==2&&!prefs.getBoolean(\"year2_story_complete\",false)){prefs.edit().putBoolean(\"year2_research_pending\",true).apply();ctx.startActivity(new Intent(ctx,YearTwoActivity.class));}\n                        else ctx.startActivity(new Intent(ctx,DeliveryActivity.class));\n                        ((Activity)ctx).finish();return true;\n                    }\n                    if(replayBtn.contains(x,y)){reset();return true;}",
    "                    if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}\n                    if(journeyBtn.contains(x,y)){\n                        if(year==2&&!prefs.getBoolean(\"year2_story_complete\",false)){prefs.edit().putBoolean(\"year2_research_pending\",true).apply();ctx.startActivity(new Intent(ctx,YearTwoActivity.class));}\n                        else if(year==3&&!prefs.getBoolean(\"year3_story_complete\",false)){ctx.startActivity(new Intent(ctx,YearThreeActivity.class));}\n                        else ctx.startActivity(new Intent(ctx,DeliveryActivity.class));\n                        ((Activity)ctx).finish();return true;\n                    }\n                    if(replayBtn.contains(x,y)){reset();return true;}",
    "memory room and Year 3 routing",
)

path.write_text(s, encoding="utf-8")
print("Applied SnowmanGame v15 memory room + first friend + Year 3 adventure routing")
