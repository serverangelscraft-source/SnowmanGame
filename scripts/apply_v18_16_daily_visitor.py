from pathlib import Path
import re

main_path=Path('app/src/main/java/com/snowmangame/MainActivity.java')
gradle_path=Path('app/build.gradle')
plan_path=Path('docs/PLAYER_RETURN_PLAN.md')
main=main_path.read_text(encoding='utf-8')
gradle=gradle_path.read_text(encoding='utf-8')
plan=plan_path.read_text(encoding='utf-8')

def rep(old,new,label):
    global main
    if new in main:
        return
    if old not in main:
        raise SystemExit('v18.16 target changed: '+label)
    main=main.replace(old,new,1)

rep('int year, wallet, runCoins, mission, yearBuilds, rewardedBuildsToday, snowCondition;',
    'int year, wallet, runCoins, mission, yearBuilds, rewardedBuildsToday, snowCondition, visitorType;',
    'visitor field')
rep('snowCondition=dailySnowCondition();\n            text.setTypeface',
    'snowCondition=dailySnowCondition();\n            visitorType=dailyVisitor();\n            text.setTypeface',
    'visitor init')
rep('tip="Сніг сьогодні: "+snowName()+" • коти першу кулю";',
    'tip=visitorRequest();',
    'opening visitor request')
rep('String snowName(){return snowCondition==0?"ПУХКИЙ":(snowCondition==1?"МОКРИЙ":"КРИЖАНИЙ");}\n        void refreshDailyIfNeeded(){',
'''String snowName(){return snowCondition==0?"ПУХКИЙ":(snowCondition==1?"МОКРИЙ":"КРИЖАНИЙ");}
        int dailyVisitor(){return (int)Math.floorMod(rewardDay*19L+year*11L,4L);}
        String visitorName(){return visitorType==0?"МАЙСТРИНЯ":(visitorType==1?"ФОТОГРАФ":(visitorType==2?"ДИТИНА":"СУСІД"));}
        String visitorRequest(){
            if(visitorType==0)return "Гість дня: майстриня • хоче шарф і 4+ деталі";
            if(visitorType==1)return "Гість дня: фотограф • хоче точні кулі й декор";
            if(visitorType==2)return "Гість дня: дитина • хоче шапку і 5+ деталей";
            return "Гість дня: сусід • хоче простого сніговика з 3 деталей";
        }
        boolean visitorSuccess(){
            if(visitorType==0)return items[SCARF].placed&&decorPlaced>=4;
            if(visitorType==1)return avgBuild()>=90&&avgDecor()>=75;
            if(visitorType==2)return items[HAT].placed&&decorPlaced>=5;
            return decorPlaced==3;
        }
        String visitorMemoryKey(){return "visitor_memory_"+rewardDay;}
        void refreshDailyIfNeeded(){''',
    'visitor helpers')
rep('syncDailyState();mission=dailyMission();snowCondition=dailySnowCondition();\n            tip="Новий день • сніг: "+snowName()+" • коти першу кулю";',
    'syncDailyState();mission=dailyMission();snowCondition=dailySnowCondition();visitorType=dailyVisitor();\n            tip=visitorRequest();',
    'midnight visitor refresh')
rep('if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}\n            buzz(65);invalidate();',
'''if(visitorSuccess()&&!prefs.getBoolean(visitorMemoryKey(),false))prefs.edit().putBoolean(visitorMemoryKey(),true).apply();
            if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}
            buzz(65);invalidate();''',
    'visitor memory save')
rep('''            p.setColor(missionSuccess?Color.rgb(229,246,237):Color.rgb(246,239,232));c.drawRoundRect(badge,dp(15),dp(15),p);text.setTextSize(tx(8));text.setTextColor(missionSuccess?Color.rgb(55,130,104):Color.rgb(145,106,72));'''.replace('setTextColor','setColor'),
'''            p.setColor(missionSuccess?Color.rgb(229,246,237):Color.rgb(246,239,232));c.drawRoundRect(badge,dp(15),dp(15),p);text.setTextSize(tx(8));text.setColor(missionSuccess?Color.rgb(55,130,104):Color.rgb(145,106,72));''',
    'badge anchor no-op')
anchor='''            p.setColor(missionSuccess?Color.rgb(229,246,237):Color.rgb(246,239,232));c.drawRoundRect(badge,dp(15),dp(15),p);text.setTextSize(tx(8));text.setColor(missionSuccess?Color.rgb(55,130,104):Color.rgb(145,106,72));c.drawText(missionSuccess?"МІСІЮ ВИКОНАНО +250":"МІСІЮ НЕ ВИКОНАНО",badge.centerX(),badge.centerY()+dp(3),text);

            sponsorBtn.set'''
insert='''            p.setColor(missionSuccess?Color.rgb(229,246,237):Color.rgb(246,239,232));c.drawRoundRect(badge,dp(15),dp(15),p);text.setTextSize(tx(8));text.setColor(missionSuccess?Color.rgb(55,130,104):Color.rgb(145,106,72));c.drawText(missionSuccess?"МІСІЮ ВИКОНАНО +250":"МІСІЮ НЕ ВИКОНАНО",badge.centerX(),badge.centerY()+dp(3),text);

            RectF visitor=new RectF(card.left+dp(24),card.top+dp(256),card.right-dp(24),card.top+dp(292));
            boolean visitorDone=visitorSuccess();
            p.setColor(visitorDone?Color.rgb(237,245,255):Color.rgb(247,244,238));c.drawRoundRect(visitor,dp(13),dp(13),p);
            text.setTextSize(tx(7.1f));text.setColor(visitorDone?Color.rgb(57,103,151):Color.rgb(127,105,75));
            c.drawText(visitorDone?("СПОГАД ДНЯ • "+visitorName()+" • ЗБЕРЕЖЕНО"):("ГІСТЬ ДНЯ • "+visitorName()+" • СПРОБУЙ ЩЕ"),visitor.centerX(),visitor.centerY()+dp(3),text);

            sponsorBtn.set'''
rep(anchor,insert,'visitor result card')
rep('syncDailyState();mission=dailyMission();giftType=-1;\n            refreshDailyIfNeeded();tip="Сніг сьогодні: "+snowName()+" • коти першу кулю";',
    'syncDailyState();mission=dailyMission();visitorType=dailyVisitor();giftType=-1;\n            refreshDailyIfNeeded();tip=visitorRequest();',
    'reset visitor')

gradle=re.sub(r'versionCode\s+\d+','versionCode 49',gradle)
gradle=re.sub(r'versionName\s+"[^"]+"','versionName "18.16"',gradle)

plan=plan.replace('Current Android build: v18.15','Current Android build: v18.16')
old='''### P2 — daily curiosity card
- One optional visitor/client per day asks for a style: stable / funny / photo-friendly / regional-pattern.
- Do not lock core progression behind the client card.
- Reward should primarily be a memory/cosmetic entry; avoid creating another farmable currency path.
'''
new='''### DONE v18.16 — daily curiosity visitor
- One deterministic visitor per local calendar day asks for a compact style goal: майстриня / фотограф / дитина / сусід.
- Replay cannot reroll the visitor because it derives from the same local-day state as mission/snow.
- Fulfilling the request stores only a date-keyed memory marker; it awards no coins and never gates progression.
- The request uses the existing opening tip; the result uses a small block inside the existing result card, so no new menu is added.

### P2 — compact-phone regression + memory presentation
- Verify 320–360dp-wide layouts after the larger accessory tray and visitor result line.
- If the result card becomes crowded, shorten copy before shrinking touch targets.
- Later expose saved visitor memories as a lightweight scrapbook, not a second economy.
'''
if old in plan: plan=plan.replace(old,new)
elif '### DONE v18.16 — daily curiosity visitor' not in plan: raise SystemExit('v18.16 plan target changed')

cycle='''
## Cycle update — 2026-09-03 visitor cycle
- Added one date-stable optional visitor request without another currency or progression gate.
- Visitor success writes a one-per-date memory marker only; replay cannot farm value or reroll the request.
- Kept the request in the existing tip and result card to protect phone screen space.
- Next priority is compact-phone regression and deciding whether saved memories deserve a tiny scrapbook surface.
- Fresh business inspiration: late-August Ukrainian campaigns increasingly combine several makers/brands around one shared theme; the safe game translation is rotating fictional winter visitors with distinct craft requests, not logos or claimed partnerships.
'''
if '## Cycle update — 2026-09-03 visitor cycle' not in plan: plan=plan.rstrip()+"\n"+cycle

main_path.write_text(main,encoding='utf-8')
gradle_path.write_text(gradle,encoding='utf-8')
plan_path.write_text(plan,encoding='utf-8')
print('Applied v18.16: deterministic daily visitor, memory-only reward, compact existing UI')
