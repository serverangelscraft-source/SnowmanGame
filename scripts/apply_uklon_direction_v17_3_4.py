from pathlib import Path
import re

p=Path("app/src/main/java/com/snowmangame/UklonActivity.java")
s=p.read_text(encoding="utf-8")

old='''            drawTop(c,"Водій уже їде","Стій біля санчат — машина під’їде праворуч");\n            drawSnowman(c,w*.20f,bottom*.70f,dp(31),.45f);drawSled(c,w*.20f,bottom*.70f+dp(8),.72f);\n            float k=smooth(t/2.7f),roadY=bottom*.69f,cx=mix(w+dp(130),w*.65f,k);drawCar(c,cx,roadY,1f,false);\n            RectF eta=new RectF(dp(30),safeTop+dp(140),w-dp(30),safeTop+dp(222));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(eta,dp(22),dp(22),p);\n            int sec=Math.max(0,3-(int)t);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(44,68,81));c.drawText(sec>0?"ВОДІЙ ЗА "+sec+"…":"МАШИНА ПРИЇХАЛА",eta.centerX(),eta.top+dp(34),text);text.setTextSize(tx(8));text.setColor(Color.rgb(95,120,133));c.drawText("Маршрут: Двір → Вокзал",eta.centerX(),eta.top+dp(59),text);'''
new='''            drawTop(c,"Водій уже їде","Напрямок руху: зліва → праворуч");\n            float k=smooth(t/2.7f),roadY=bottom*.69f,cx=mix(-dp(130),w*.65f,k);\n            // Keep the same spatial language as the trip progress: left = start, right = destination.\n            drawCar(c,cx,roadY,1f,false);\n            drawSnowman(c,w*.20f,bottom*.70f,dp(31),.45f);drawSled(c,w*.20f,bottom*.70f+dp(8),.72f);\n            RectF eta=new RectF(dp(30),safeTop+dp(140),w-dp(30),safeTop+dp(222));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(eta,dp(22),dp(22),p);\n            int sec=Math.max(0,3-(int)t);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(44,68,81));c.drawText(sec>0?"ВОДІЙ ЗА "+sec+"…":"МАШИНА ПРИЇХАЛА",eta.centerX(),eta.top+dp(31),text);text.setTextSize(tx(8));text.setColor(Color.rgb(95,120,133));c.drawText("Під’їзд: зліва → праворуч",eta.centerX(),eta.top+dp(52),text);\n            float pl=eta.left+dp(24),pr=eta.right-dp(24),py=eta.bottom-dp(13),px=mix(pl,pr,k);stroke.setStrokeWidth(dp(4));stroke.setColor(Color.rgb(218,228,233));c.drawLine(pl,py,pr,py,stroke);stroke.setColor(Color.rgb(252,190,24));c.drawLine(pl,py,px,py,stroke);p.setColor(Color.rgb(252,190,24));c.drawCircle(px,py,dp(6),p);'''
if old not in s:
    raise SystemExit("v17.3.4 Uklon direction patch failed: WAIT block not found")
s=s.replace(old,new,1)
p.write_text(s,encoding="utf-8")

gradle=Path("app/build.gradle")
g=gradle.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 30',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "17.3.4"',g)
gradle.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v17.3.4: Uklon approach and progress now both move left to right")
