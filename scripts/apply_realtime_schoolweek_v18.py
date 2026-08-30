from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolActivity.java")
class1_path=Path("app/src/main/java/com/snowmangame/ClassOneActivity.java")
grade2_path=Path("app/src/main/java/com/snowmangame/GradeTwoActivity.java")
main_path=Path("app/src/main/java/com/snowmangame/MainActivity.java")
manifest_path=Path("app/src/main/AndroidManifest.xml")
gradle_path=Path("app/build.gradle")

school=school_path.read_text(encoding="utf-8")
class1=class1_path.read_text(encoding="utf-8")
grade2=grade2_path.read_text(encoding="utf-8")
main=main_path.read_text(encoding="utf-8")
manifest=manifest_path.read_text(encoding="utf-8")

# From Grade 2 onward, the real calendar is the canonical school system.
school=school.replace('startActivity(new Intent(this,GradeTwoActivity.class));finish();return;', 'startActivity(new Intent(this,SchoolWeekActivity.class));finish();return;', 1)
class1=class1.replace('ctx.startActivity(new Intent(ctx,GradeTwoActivity.class));((Activity)ctx).finish();return true;', 'ctx.startActivity(new Intent(ctx,SchoolWeekActivity.class));((Activity)ctx).finish();return true;', 1)

# Keep old deep links/saves safe: the old GradeTwoActivity immediately hands off to the new clocked system.
needle='''        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);\n        getSharedPreferences("snowman_game",MODE_PRIVATE).edit().putBoolean("class2_started",true).putInt("school_grade",2).putInt("school_winter",8).apply();\n        setContentView(new GradeTwoView(this));'''
repl='''        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);\n        SharedPreferences legacyPrefs=getSharedPreferences("snowman_game",MODE_PRIVATE);\n        legacyPrefs.edit().putBoolean("class2_started",true).putInt("school_grade",Math.max(2,legacyPrefs.getInt("school_grade",2))).apply();\n        startActivity(new Intent(this,SchoolWeekActivity.class));\n        finish();'''
if needle not in grade2:
    raise SystemExit("v18 school clock patch failed: GradeTwo handoff")
grade2=grade2.replace(needle,repl,1)

# Eskimo/ice-cream integration is retired. Remove the visible CTA and its touch path from the final result screen.
old_btn='''            sponsorBtn.set(card.left+dp(22),card.bottom-dp(178),card.right-dp(22),card.bottom-dp(128));\n            p.setColor(sponsorRewarded?Color.rgb(188,190,193):Color.rgb(226,91,122));c.drawRoundRect(sponsorBtn,dp(18),dp(18),p);\n            text.setTextSize(tx(9.5f));text.setColor(Color.WHITE);c.drawText(sponsorRewarded?"ЕСКІМОС УЖЕ СКУШТОВАНО":"СПРОБУВАТИ ЕСКІМОС +150",sponsorBtn.centerX(),sponsorBtn.centerY()+dp(3),text);\n\n'''
if old_btn in main:
    main=main.replace(old_btn,'            sponsorBtn.setEmpty();\n\n',1)
main=main.replace('if(sponsorBtn.contains(x,y)&&!sponsorRewarded){sponsorScene=true;sponsorStart=SystemClock.elapsedRealtime();buzz(20);invalidate();return true;}','',1)
# Ensure the retired sponsor scene cannot become visible from any stale in-memory state.
main=main.replace('            if(sponsorScene){drawSponsor(c);postInvalidateOnAnimation();return;}','            if(sponsorScene)sponsorScene=false;',1)

if '.SchoolWeekActivity' not in manifest:
    anchor='        <activity android:name=".GradeTwoActivity" android:screenOrientation="portrait" android:exported="false"/>\n'
    if anchor not in manifest:
        raise SystemExit("v18 school clock patch failed: manifest GradeTwo anchor")
    manifest=manifest.replace(anchor,anchor+'        <activity android:name=".SchoolWeekActivity" android:screenOrientation="portrait" android:exported="false"/>\n',1)

school_path.write_text(school,encoding="utf-8")
class1_path.write_text(class1,encoding="utf-8")
grade2_path.write_text(grade2,encoding="utf-8")
main_path.write_text(main,encoding="utf-8")
manifest_path.write_text(manifest,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 33',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.0"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.0: real-time 5+2 school week, Ukrainian dinners, Eskimo retired")
