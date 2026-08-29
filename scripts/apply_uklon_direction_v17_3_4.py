from pathlib import Path
import re

p=Path("app/src/main/java/com/snowmangame/UklonActivity.java")
s=p.read_text(encoding="utf-8")

def rep(old,new,label):
    global s
    if old not in s:
        raise SystemExit(f"v17.3.4 Uklon direction patch failed: {label}")
    s=s.replace(old,new,1)

# One spatial rule for the whole scene: left = beginning, right = forward/destination.
rep(
    'drawTop(c,"Водій уже їде","Стій біля санчат — машина під’їде праворуч");',
    'drawTop(c,"Водій уже їде","Напрямок руху: зліва → праворуч");',
    "WAIT subtitle",
)
rep(
    '            drawSnowman(c,w*.20f,bottom*.70f,dp(31),.45f);drawSled(c,w*.20f,bottom*.70f+dp(8),.72f);\n            float k=smooth(t/2.7f),roadY=bottom*.69f,cx=mix(w+dp(130),w*.65f,k);drawCar(c,cx,roadY,1f,false);',
    '            float k=smooth(t/2.7f),roadY=bottom*.69f,cx=mix(-dp(130),w*.65f,k);\n            drawCar(c,cx,roadY,1f,false);\n            drawSnowman(c,w*.20f,bottom*.70f,dp(31),.45f);drawSled(c,w*.20f,bottom*.70f+dp(8),.72f);',
    "taxi approach direction",
)
rep(
    'c.drawText(etaRoute,eta.centerX(),eta.top+dp(59),text);',
    'c.drawText(etaRoute,eta.centerX(),eta.top+dp(56),text);float pl=eta.left+dp(24),pr=eta.right-dp(24),py=eta.bottom-dp(11),px=mix(pl,pr,k);stroke.setStrokeWidth(dp(4));stroke.setColor(Color.rgb(218,228,233));c.drawLine(pl,py,pr,py,stroke);stroke.setColor(Color.rgb(252,190,24));c.drawLine(pl,py,px,py,stroke);p.setColor(Color.rgb(252,190,24));c.drawCircle(px,py,dp(6),p);',
    "ETA runner direction",
)

p.write_text(s,encoding="utf-8")

gradle=Path("app/build.gradle")
g=gradle.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 30',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.3.4"',g)
gradle.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.3.4: taxi approach and ETA runner both move left to right")
