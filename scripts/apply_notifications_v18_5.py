from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolWeekActivity.java")
manifest_path=Path("app/src/main/AndroidManifest.xml")
gradle_path=Path("app/build.gradle")

school=school_path.read_text(encoding="utf-8")
manifest=manifest_path.read_text(encoding="utf-8")

old='''        setContentView(new SchoolWeekView(this));\n    }'''
new='''        setContentView(new SchoolWeekView(this));\n        NotificationScheduler.onSchoolOpened(this);\n    }'''
if old not in school:
    raise SystemExit("v18.5 notifications patch failed: SchoolWeekActivity onCreate")
school=school.replace(old,new,1)

old='''stage=yearComplete?YEAR_DONE:DONE;SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);invalidate();}'''
new='''stage=yearComplete?YEAR_DONE:DONE;SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);NotificationScheduler.onDayCompleted(ctx);invalidate();}'''
if old not in school:
    raise SystemExit("v18.5 notifications patch failed: counted day completion")
school=school.replace(old,new,1)

if 'android.permission.POST_NOTIFICATIONS' not in manifest:
    anchor='    <uses-permission android:name="android.permission.VIBRATE"/>\n'
    if anchor not in manifest:
        raise SystemExit("v18.5 notifications patch failed: VIBRATE permission anchor")
    manifest=manifest.replace(anchor,anchor+'    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>\n    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>\n',1)

if '.SnowReminderReceiver' not in manifest:
    receiver='''        <receiver\n            android:name=".SnowReminderReceiver"\n            android:enabled="true"\n            android:exported="false">\n            <intent-filter>\n                <action android:name="android.intent.action.BOOT_COMPLETED"/>\n                <action android:name="android.intent.action.TIME_SET"/>\n                <action android:name="android.intent.action.TIMEZONE_CHANGED"/>\n                <action android:name="android.intent.action.DATE_CHANGED"/>\n            </intent-filter>\n        </receiver>\n'''
    if '    </application>' not in manifest:
        raise SystemExit("v18.5 notifications patch failed: application close anchor")
    manifest=manifest.replace('    </application>',receiver+'    </application>',1)

school_path.write_text(school,encoding="utf-8")
manifest_path.write_text(manifest,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 38',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.5"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.5: adaptive local school-life notifications")
