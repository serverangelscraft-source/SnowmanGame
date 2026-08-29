from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolActivity.java")
class1_path=Path("app/src/main/java/com/snowmangame/ClassOneActivity.java")
manifest_path=Path("app/src/main/AndroidManifest.xml")
gradle_path=Path("app/build.gradle")

school=school_path.read_text(encoding="utf-8")
class1=class1_path.read_text(encoding="utf-8")
manifest=manifest_path.read_text(encoding="utf-8")

# Returning students who have already reached Grade 2 go straight to the active 2-A chapter.
old='''        SharedPreferences classPrefs=getSharedPreferences("snowman_game",MODE_PRIVATE);\n        if(classPrefs.getBoolean("school_class_one_started",false)){startActivity(new Intent(this,ClassOneActivity.class));finish();return;}\n        setContentView(new SchoolView(this));'''
new='''        SharedPreferences classPrefs=getSharedPreferences("snowman_game",MODE_PRIVATE);\n        if(classPrefs.getInt("school_grade",1)>=2||classPrefs.getBoolean("class2_started",false)){startActivity(new Intent(this,GradeTwoActivity.class));finish();return;}\n        if(classPrefs.getBoolean("school_class_one_started",false)){startActivity(new Intent(this,ClassOneActivity.class));finish();return;}\n        setContentView(new SchoolView(this));'''
if old not in school:
    raise SystemExit("v17.5 grade2 patch failed: returning-school route")
school=school.replace(old,new,1)

# The v17.4 2-A card looked disabled by design. Turn it into a real primary CTA.
old_button='''            action.set(dp(22),bottom-dp(66),w-dp(22),bottom-dp(11));p.setColor(Color.rgb(213,229,237));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextSize(tx(8));text.setColor(Color.rgb(96,123,136));c.drawText("2-А • НАСТУПНИЙ РОЗДІЛ",action.centerX(),action.centerY()+dp(3),text);'''
new_button='''            action.set(dp(22),bottom-dp(66),w-dp(22),bottom-dp(11));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextSize(tx(9.2f));text.setColor(Color.WHITE);c.drawText("ПОЧАТИ ЖИТТЯ У 2-А",action.centerX(),action.centerY()+dp(4),text);'''
if old_button not in class1:
    raise SystemExit("v17.5 grade2 patch failed: inactive 2-A CTA")
class1=class1.replace(old_button,new_button,1)

old_touch='''            else if(stage==GRADE2){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}}'''
new_touch='''            else if(stage==GRADE2){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}if(action.contains(x,y)){prefs.edit().putBoolean("class2_started",true).putInt("school_grade",2).putInt("school_winter",8).putInt("class2_stage",0).apply();ctx.startActivity(new Intent(ctx,GradeTwoActivity.class));((Activity)ctx).finish();return true;}}'''
if old_touch not in class1:
    raise SystemExit("v17.5 grade2 patch failed: 2-A touch handler")
class1=class1.replace(old_touch,new_touch,1)

school_path.write_text(school,encoding="utf-8")
class1_path.write_text(class1,encoding="utf-8")

if '.GradeTwoActivity' not in manifest:
    anchor='        <activity android:name=".SchoolActivity" android:screenOrientation="portrait" android:exported="false"/>\n'
    if anchor not in manifest:
        raise SystemExit("v17.5 grade2 patch failed: manifest SchoolActivity anchor")
    manifest=manifest.replace(anchor,anchor+'        <activity android:name=".GradeTwoActivity" android:screenOrientation="portrait" android:exported="false"/>\n',1)
manifest_path.write_text(manifest,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 32',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.5"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.5: active 2-A chapter with newcomer Iskryk")
