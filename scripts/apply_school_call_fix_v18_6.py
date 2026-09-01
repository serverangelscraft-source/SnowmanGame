from pathlib import Path

p=Path("app/src/main/java/com/snowmangame/SchoolCallActivity.java")
s=p.read_text(encoding="utf-8")
old='performClick();finish();return true;'
new='performClick();((Activity)ctx).finish();return true;'
if old not in s:
    raise SystemExit("v18.6 school-call fix failed: finish target")
s=s.replace(old,new,1)
p.write_text(s,encoding="utf-8")
print("Applied SnowmanGame v18.6 school-call compile fix")
