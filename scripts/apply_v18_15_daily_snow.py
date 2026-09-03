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
        raise SystemExit('v18.15 target changed: '+label)
    main=main.replace(old,new,1)

rep('int year, wallet, runCoins, mission, yearBuilds, rewardedBuildsToday;',
    'int year, wallet, runCoins, mission, yearBuilds, rewardedBuildsToday, snowCondition;',
    'snow condition field')

rep('            syncDailyState();\n            text.setTypeface',
    '            syncDailyState();\n            snowCondition=dailySnowCondition();\n            text.setTypeface',
    'load daily snow')

rep('            mission=dailyMission();\n            setClickable(true);',
    '            mission=dailyMission();\n            tip="Сніг сьогодні: "+snowName()+" • коти першу кулю";\n            setClickable(true);',
    'initial snow tip')

rep('        float effort(){return 1f+(year-1)*.20f;}',
'''        float effort(){
            float snow=snowCondition==0?.90f:(snowCondition==1?1.12f:1f);
            return (1f+(year-1)*.20f)*snow;
        }''',
    'snow rolling feel')

rep('''        int dailyMission(){
            long seed=rewardDay*31L+year*17L;
            return (int)Math.floorMod(seed,3L);
        }
        String timeText''',
'''        int dailyMission(){
            long seed=rewardDay*31L+year*17L;
            return (int)Math.floorMod(seed,3L);
        }
        int dailySnowCondition(){return (int)Math.floorMod(rewardDay*13L+7L,3L);}
        String snowName(){return snowCondition==0?"ПУХКИЙ":(snowCondition==1?"МОКРИЙ":"КРИЖАНИЙ");}
        void refreshDailyIfNeeded(){
            long now=localDayNumber();
            if(now==rewardDay)return;
            syncDailyState();mission=dailyMission();snowCondition=dailySnowCondition();
            tip="Новий день • сніг: "+snowName()+" • коти першу кулю";
        }
        String timeText''',
    'daily snow helpers')

rep('''        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            layout();''',
'''        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            refreshDailyIfNeeded();
            layout();''',
    'midnight refresh')

rep('c.drawText("Рік "+year+" • ● "+wallet,hud.right-dp(14),hud.bottom-dp(11),text);',
    'c.drawText("СНІГ: "+snowName()+" • Рік "+year+" • ● "+wallet,hud.right-dp(14),hud.bottom-dp(11),text);',
    'snow hud indicator')

rep('''        float tolerance(int type){
            float mul=Math.max(.72f,1f-(year-1)*.035f);''',
'''        float tolerance(int type){
            float snow=snowCondition==0?1.06f:(snowCondition==2?.90f:.98f);
            float mul=Math.max(.72f,1f-(year-1)*.035f)*snow;''',
    'snow placement feel')

rep('tip="Коти сніг пальцем — зроби першу кулю";rollX=Float.NaN;rollY=Float.NaN;',
    'refreshDailyIfNeeded();tip="Сніг сьогодні: "+snowName()+" • коти першу кулю";rollX=Float.NaN;rollY=Float.NaN;',
    'reset snow tip')

gradle=re.sub(r'versionCode\s+\d+','versionCode 48',gradle)
gradle=re.sub(r'versionName\s+"[^"]+"','versionName "18.15"',gradle)

plan=plan.replace('Current Android build: v18.14','Current Android build: v18.15')
plan=plan.replace('5. **Task-first HUD — improved in v18.14.** Current tactile step and daily reward/free status are primary; year/wallet are secondary. Daily snow condition is still missing.',
'''5. **Task-first HUD — improved in v18.14.** Current tactile step and daily reward/free status are primary; year/wallet are secondary.
6. **Daily snow condition — fixed in v18.15.** `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` is deterministic for the local calendar date, shown compactly, and slightly changes rolling/placement feel.
7. **Midnight-open Activity stale day — fixed in v18.15.** The view notices a local date change without requiring an Activity restart and refreshes reward quota, mission and snow together.''')
plan=plan.replace('6. **Build source mutation — fixed in v18.12.**','8. **Build source mutation — fixed in v18.12.**')
plan=plan.replace('### P2 — daily snow condition\n- Add one date-stable condition: `ПУХКИЙ`, `МОКРИЙ`, or `КРИЖАНИЙ`.\n- Condition changes actual feel slightly: rolling effort / placement tolerance, never making a run impossible.\n- Show the condition before play and with a small icon/word during sculpting; do not add another large HUD panel.\n\n','''### DONE v18.15 — daily snow feel + midnight refresh
- Added date-stable `ПУХКИЙ / МОКРИЙ / КРИЖАНИЙ` snow.
- Powder is slightly easier to roll, wet snow takes slightly more movement, icy snow slightly tightens placement tolerance.
- Snow is shown in the existing HUD line and opening tip; no extra card/panel.
- A date change while the Activity remains open refreshes quota, mission and snow together.

''')
plan=plan.replace('The v18.14 expressive-completion milestone is achieved. The next working milestone should be considered ready when:\n1. a date-stable snow condition visibly and mechanically changes the run;\n2. the condition is communicated without another large HUD panel;\n3. one optional daily curiosity/client card adds variety without another farmable currency;\n4. daily reward/free-mode rules from v18.13 remain intact;\n5. school and pre-school story routing remain unaffected.',
'''The v18.15 daily-snow milestone is achieved. The next working milestone should be considered ready when:
1. one optional daily curiosity/client card adds variety without another farmable currency;
2. the visitor request is deterministic for the local date and cannot be rerolled by replay;
3. compact phones keep the snowman, accessory tray and finish button readable;
4. daily reward/free-mode rules from v18.13 remain intact;
5. school and pre-school story routing remain unaffected.''')
plan += '''\n## Cycle update — 2026-09-03 morning\n- Added date-stable daily snow feel rather than another menu or currency.\n- Fixed the stale-day edge case when the app stays open across local midnight.\n- Kept the mechanical differences intentionally small so daily variation creates curiosity rather than punishment.\n- Next P2 is one optional daily curiosity visitor/client and compact-phone regression testing.\n- Fresh inspiration: Ukrainian retail coverage around late-August 2026 highlights strong familiar national brands; adapt the useful pattern as fictional rotating local makers/visitors, not logos or claimed partnerships.\n'''

main_path.write_text(main,encoding='utf-8')
gradle_path.write_text(gradle,encoding='utf-8')
plan_path.write_text(plan,encoding='utf-8')
print('Applied v18.15 daily snow + local-midnight refresh')
