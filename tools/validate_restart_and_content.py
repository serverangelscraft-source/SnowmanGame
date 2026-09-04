#!/usr/bin/env python3
"""Static regression checks for school resume safety and grades 7–11 content.

These checks intentionally avoid changing Android progression logic. They fail CI if
save/restart invariants or the senior-grade content pack are accidentally removed.
"""
from pathlib import Path
import re

root=Path(__file__).resolve().parents[1]
school=(root/'app/src/main/java/com/snowmangame/SchoolWeekActivity.java').read_text(encoding='utf-8')
content=(root/'app/src/main/java/com/snowmangame/SchoolGradeContent.java').read_text(encoding='utf-8')
resume=(root/'app/src/main/java/com/snowmangame/ResumeActivity.java').read_text(encoding='utf-8')

# Every interactive normal-day stage must be persisted, and its mini-state restored.
required_school_fragments={
    'stage saved':'putInt("school_player_stage",s)' ,
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

# Senior-grade packs must exist for every grade, every school day, and both lessons.
for grade in range(7,12):
    if f'case {grade}:return ' not in content:
        raise SystemExit(f'missing theme for grade {grade}')
    marker=f'if(grade=={grade})'
    if content.count(marker) < 3:
        raise SystemExit(f'grade {grade} does not have full hook/question/options coverage')

# Guard against the old generic fallback becoming the only visible senior content.
for phrase in [
    'Власний голос','Вибір і наслідки','Майстерність',
    'Плани на майбутнє','Випуск і свій шлях',
    'Лист собі в майбутнє','День перед випуском'
]:
    if phrase not in content:
        raise SystemExit('senior content regression: '+phrase)

# Mobile safety: answer cards are three columns, so keep senior labels compact.
arrays=re.findall(r'new String\[\]\{([^}]*)\}',content)
labels=[]
for body in arrays:
    labels += re.findall(r'"([^"]+)"',body)
long=[x for x in labels if len(x)>26]
if long:
    raise SystemExit('senior option label too long for narrow-phone cards: '+repr(long[:5]))

print('OK: restart save keys + grade 7–11 content invariants present')
