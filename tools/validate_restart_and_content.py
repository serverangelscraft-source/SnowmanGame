#!/usr/bin/env python3
"""Static regression checks for school resume safety and grades 7–11 content.

These checks intentionally avoid changing Android progression logic. They fail CI if
save/restart invariants, integration resume safety or the senior-grade content pack
are accidentally removed.
"""
from pathlib import Path
import re

root=Path(__file__).resolve().parents[1]
school=(root/'app/src/main/java/com/snowmangame/SchoolWeekActivity.java').read_text(encoding='utf-8')
content=(root/'app/src/main/java/com/snowmangame/SchoolGradeContent.java').read_text(encoding='utf-8')
resume=(root/'app/src/main/java/com/snowmangame/ResumeActivity.java').read_text(encoding='utf-8')
integration=(root/'app/src/main/java/com/snowmangame/SchoolIntegrationActivity.java').read_text(encoding='utf-8')
session=(root/'app/src/main/java/com/snowmangame/SchoolIntegrationSession.java').read_text(encoding='utf-8')

required_school_fragments={
    'stage saved':'putInt("school_player_stage",s)',
    'stage date saved':'putLong("school_player_stage_day",effectiveDay)',
    'stage restored':'stage=prefs.getInt("school_player_stage",defaultStage())',
    'bag restored':'bagMask=prefs.getInt("school_player_bag_mask",0)',
    'break restored':'miniHits=prefs.getInt("school_player_mini_hits",0)',
    'walk restored':'homeStep=prefs.getInt("school_player_home_step",0)',
    'dinner restored':'dinnerBites=Math.max(0,Math.min(3,prefs.getInt("school_player_dinner_bites",0)))',
    'bag persisted':'putInt("school_player_bag_mask",bagMask)',
    'break persisted':'putInt("school_player_mini_hits",miniHits)',
    'walk persisted':'putInt("school_player_home_step",homeStep)',
    'dinner persisted':'putInt("school_player_dinner_bites",dinnerBites)',
    'same-day completion guard':'school_player_last_completed_day',
    'year transition':'grade++',
    'grade cap':'grade<11',
    'graduation terminal':'grade>=11',
}
missing=[name for name,frag in required_school_fragments.items() if frag not in school]
if missing:
    raise SystemExit('restart/progression invariant missing: '+'; '.join(missing))

if 'SchoolProgressionGuard.repair(p);' not in resume:
    raise SystemExit('ResumeActivity no longer repairs known old saves before routing')

# Optional integrations must survive process death without becoming a progression key.
integration_checks={
    'resume routes pending integration':'SchoolIntegrationSession.pending(p)' in resume and 'SchoolIntegrationSession.resumeIntent(this,p)' in resume,
    'session has isolated active flag':'school_integration_active' in session,
    'session snapshots grade':'school_integration_active_grade' in session,
    'session snapshots school day':'school_integration_active_day' in session,
    'session snapshots selected card':'school_integration_active_choice' in session,
    'activity starts restart-safe session':'SchoolIntegrationSession.begin(prefs,grade,schoolDay)' in integration,
    'selection persists':'SchoolIntegrationSession.select(prefs,i)' in integration,
    'completion is atomic':'SchoolIntegrationSession.complete(prefs,grade,schoolDay,eventId,selected)' in integration,
    'back intentionally skips optional event':'SchoolIntegrationSession.clear(getSharedPreferences("snowman_game",Context.MODE_PRIVATE))' in integration,
}
failed=[name for name,ok in integration_checks.items() if not ok]
if failed:
    raise SystemExit('integration restart invariant missing: '+'; '.join(failed))
for forbidden in ['school_grade','school_year_school_done','school_year_weekend_done','school_player_last_completed_day']:
    if forbidden in session:
        raise SystemExit('integration session must not own progression key: '+forbidden)

for grade in range(7,12):
    if f'case {grade}:return ' not in content:
        raise SystemExit(f'missing theme for grade {grade}')
    if content.count(f'if(grade=={grade})') < 4:
        raise SystemExit(f'grade {grade} does not have hook/title/question/answer coverage')

for api in ['public static String lessonTitle','public static String question','public static String[] options','public static int correct']:
    if api not in content:
        raise SystemExit('senior content API missing: '+api)

for phrase in [
    'Власний голос','Вибір і наслідки','Майстерність',
    'Плани на майбутнє','Випуск і свій шлях',
    'Лист собі в майбутнє','День перед випуском'
]:
    if phrase not in content:
        raise SystemExit('senior content regression: '+phrase)

# Answers must be keyed by grade/day, not inferred from copy fragments. Otherwise a
# harmless wording edit can silently change the available answers.
if 'q.contains(' in content:
    raise SystemExit('senior options must not depend on question substring matching')
if 'return question(grade,schoolDay,second)==null?-1:0;' not in content:
    raise SystemExit('senior correct-answer contract changed unexpectedly')

arrays=re.findall(r'new String\[\]\{([^}]*)\}',content)
labels=[]
for body in arrays:
    labels += re.findall(r'"([^"]+)"',body)
long=[x for x in labels if len(x)>26]
if long:
    raise SystemExit('senior option label too long for narrow-phone cards: '+repr(long[:5]))

print('OK: restart keys + integration resume + deterministic grade 7–11 content invariants present')
