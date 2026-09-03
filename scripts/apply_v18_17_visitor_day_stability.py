from pathlib import Path
import re

main_path=Path('app/src/main/java/com/snowmangame/MainActivity.java')
gradle_path=Path('app/build.gradle')
plan_path=Path('docs/PLAYER_RETURN_PLAN.md')
main=main_path.read_text(encoding='utf-8')
gradle=gradle_path.read_text(encoding='utf-8')
plan=plan_path.read_text(encoding='utf-8')
old='int dailyVisitor(){return (int)Math.floorMod(rewardDay*19L+year*11L,4L);}'
new='int dailyVisitor(){return (int)Math.floorMod(rewardDay*19L+11L,4L);}'
if new not in main:
    if old not in main: raise SystemExit('v18.17 dailyVisitor target changed')
    main=main.replace(old,new,1)
gradle=re.sub(r'versionCode\s+\d+','versionCode 50',gradle)
gradle=re.sub(r'versionName\s+"[^"]+"','versionName "18.17"',gradle)
plan=plan.replace('Current Android build: v18.16','Current Android build: v18.17')
plan=plan.replace('Replay cannot reroll the visitor because it derives from the same local-day state as mission/snow.', 'Replay and same-day year progression cannot reroll the visitor because it derives from local calendar date only.')
cycle='''
## Cycle update — 2026-09-03 visitor stability hotfix
- Found and fixed a same-day reroll edge case: v18.16 visitor selection also depended on life year.
- Visitor selection now depends on local calendar date only, matching the one-visitor-per-day rule and date-keyed memory.
- This keeps year progression, replay and free sculpting from changing today's visitor.
'''
if '## Cycle update — 2026-09-03 visitor stability hotfix' not in plan: plan=plan.rstrip()+"\n"+cycle
main_path.write_text(main,encoding='utf-8')
gradle_path.write_text(gradle,encoding='utf-8')
plan_path.write_text(plan,encoding='utf-8')
print('Applied v18.17: visitor is stable for the local calendar date regardless of life-year changes')
