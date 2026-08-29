from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolActivity.java")
class1_path=Path("app/src/main/java/com/snowmangame/ClassOneActivity.java")
gradle_path=Path("app/build.gradle")

school=school_path.read_text(encoding="utf-8")
class1=class1_path.read_text(encoding="utf-8")

def rep(old,new,label):
    global school
    if old not in school:
        raise SystemExit(f"v17.4 school-life patch failed: {label}")
    school=school.replace(old,new,1)

# Once the extended 1-A chapter has started, normal school entry returns to that persistent hub.
rep(
    '        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);\n        setContentView(new SchoolView(this));',
    '        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);\n        SharedPreferences classPrefs=getSharedPreferences("snowman_game",MODE_PRIVATE);\n        if(classPrefs.getBoolean("school_class_one_started",false)){startActivity(new Intent(this,ClassOneActivity.class));finish();return;}\n        setContentView(new SchoolView(this));',
    "route returning students to class 1-A hub",
)

# The first lesson is no longer the end of school. The old replay button becomes the doorway into school life.
rep(
    'button(c,"ПРОЙТИ ШКІЛЬНИЙ ДЕНЬ ЩЕ РАЗ");',
    'button(c,prefs.getBoolean("year2_mitten_found",false)&&!prefs.getBoolean("teacher_mitten_returned",false)?"ПОВЕРНУТИ РУКАВИЧКУ ВЧИТЕЛЮ":"ПРОДОВЖИТИ ЖИТТЯ 1-А");',
    "replace school replay CTA",
)

old_done='if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}if(action.contains(x,y)){prefs.edit().putBoolean("school_first_day_complete",false).putInt("school_stage",GATE).apply();stage=GATE;mistakes=0;feedback="";invalidate();return true;}}'
new_done='if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}if(action.contains(x,y)){if(prefs.getBoolean("year2_mitten_found",false)&&!prefs.getBoolean("teacher_mitten_returned",false)){stage=TEACHER;feedback="";teacherMittenX=Float.NaN;teacherMittenY=Float.NaN;invalidate();return true;}prefs.edit().putBoolean("school_class_one_started",true).putInt("school_grade",1).putInt("class1_stage",0).apply();ctx.startActivity(new Intent(ctx,ClassOneActivity.class));((Activity)ctx).finish();return true;}}'
rep(old_done,new_done,"continue from first lesson into class 1-A")

school_path.write_text(school,encoding="utf-8")

# Keep the committed source directly buildable as well; the first draft used an invalid second array declarator.
class1=class1.replace(
    'float[] xs={w*.27f,w*.52f,w*.76f},[]ys={safeTop+dp(320),safeTop+dp(385),safeTop+dp(312)};',
    'float[] xs={w*.27f,w*.52f,w*.76f},ys={safeTop+dp(320),safeTop+dp(385),safeTop+dp(312)};'
)
class1_path.write_text(class1,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 31',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.4"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.4: playable 1-A life, spring memory cycle and 2-A transition")
