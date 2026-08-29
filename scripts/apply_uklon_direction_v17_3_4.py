from pathlib import Path
import re

p=Path("app/src/main/java/com/snowmangame/UklonActivity.java")
s=p.read_text(encoding="utf-8")

def rep(old,new,label):
    global s
    if old not in s:
        raise SystemExit(f"v17.3.4 Uklon direction patch failed: {label}")
    s=s.replace(old,new,1)

# One spatial rule for the whole Uklon scene: left = start, right = forward/destination.
rep(
    'drawTop(c,"Водій уже їде","Стій біля санчат — машина під’їде праворуч");',
    'drawTop(c,"Водій уже їде","Машина під’їжджає зліва → праворуч");',
    "WAIT subtitle",
)

# Patch only the stable coordinate expression: later character/outfit patches change the snowman draw call.
rep(
    'cx=mix(w+dp(130),w*.65f,k)',
    'cx=mix(-dp(130),w*.65f,k)',
    "taxi approach direction",
)

# The trip runner already goes left -> right. Add the same visual direction to the ETA card when its route line exists.
eta_marker='c.drawText(etaRoute,eta.centerX(),eta.top+dp(59),text);'
if eta_marker in s:
    s=s.replace(
        eta_marker,
        'c.drawText(etaRoute,eta.centerX(),eta.top+dp(56),text);float pl=eta.left+dp(24),pr=eta.right-dp(24),py=eta.bottom-dp(11),px=mix(pl,pr,k);stroke.setStrokeWidth(dp(4));stroke.setColor(Color.rgb(218,228,233));c.drawLine(pl,py,pr,py,stroke);stroke.setColor(Color.rgb(252,190,24));c.drawLine(pl,py,px,py,stroke);p.setColor(Color.rgb(252,190,24));c.drawCircle(px,py,dp(6),p);',
        1,
    )
else:
    print("v17.3.4: ETA route marker changed by an earlier layout patch; taxi/RIDE direction is still unified")

p.write_text(s,encoding="utf-8")

gradle=Path("app/build.gradle")
g=gradle.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 30',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.3.4"',g)
gradle.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.3.4: Uklon movement is consistently left to right")
