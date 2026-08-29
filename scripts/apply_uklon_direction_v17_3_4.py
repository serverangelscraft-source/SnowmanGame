from pathlib import Path
import re

p=Path("app/src/main/java/com/snowmangame/UklonActivity.java")
s=p.read_text(encoding="utf-8")

def rep(old,new,label):
    global s
    if old not in s:
        raise SystemExit(f"v17.3.4 Uklon direction patch failed: {label}")
    s=s.replace(old,new,1)

# v17.1 already draws the ETA/progress runner from DВІР on the left to ВОКЗАЛ on the right.
# Make the physical taxi use that same direction instead of approaching from the opposite side.
rep(
    'drawTop(c,"Водій уже їде","Стій біля санчат — машина під’їде праворуч");',
    'drawTop(c,"Водій уже їде","Машина під’їжджає зліва → праворуч");',
    "WAIT subtitle",
)
rep(
    'float cx=mix(w+dp(130),w*.68f,k);',
    'float cx=mix(-dp(130),w*.68f,k);',
    "final v17.1 taxi approach direction",
)

p.write_text(s,encoding="utf-8")

gradle=Path("app/build.gradle")
g=gradle.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 30',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.3.4"',g)
gradle.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.3.4: ETA runner and taxi now both move left to right")
