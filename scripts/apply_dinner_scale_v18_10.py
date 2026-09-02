from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolWeekActivity.java")
gradle_path=Path("app/build.gradle")
s=school_path.read_text(encoding="utf-8")

repls=[
('centerText(c,DISHES[idx],r.top+dp(70),20,Color.rgb(48,82,99));','centerText(c,DISHES[idx],r.top+dp(78),27,Color.rgb(48,82,99));'),
('centerText(c,"Після школи — одна домашня українська страва.",r.top+dp(101),7.3f,Color.rgb(99,124,134));','centerText(c,"Після школи — одна домашня українська страва.",r.top+dp(116),8.5f,Color.rgb(99,124,134));'),
('dishRect.set(r.centerX()-dp(95),r.top+dp(135),r.centerX()+dp(95),r.top+dp(260));drawDish(c,r.centerX(),r.top+dp(198),dp(72),idx);','dishRect.set(r.centerX()-dp(138),r.top+dp(145),r.centerX()+dp(138),r.top+dp(330));drawDish(c,r.centerX(),r.top+dp(238),dp(108),idx);'),
('c.drawCircle(r.centerX()-dp(42)+i*dp(21),r.top+dp(199),dp(5),p);','c.drawCircle(r.centerX()-dp(56)+i*dp(28),r.top+dp(239),dp(7),p);'),
('centerText(c,"З\'їдено "+Math.min(5,dinnerBites)+"/5",r.top+dp(286),7.2f,Color.rgb(89,122,137));','centerText(c,"З\'їдено "+Math.min(5,dinnerBites)+"/5",r.top+dp(360),9.2f,Color.rgb(89,122,137));'),
('drawHero(c,r.centerX(),r.bottom-dp(42),dp(36));','drawHero(c,r.centerX(),r.bottom-dp(56),dp(49));'),
('centerText(c,"Торкайся тарілки, щоб повечеряти.",r.bottom-dp(92),6.8f,Color.rgb(111,132,139));','centerText(c,"Торкайся великої тарілки, щоб повечеряти.",r.bottom-dp(122),8.2f,Color.rgb(111,132,139));'),
]
for old,new in repls:
    if old not in s:
        raise SystemExit("v18.10 dinner scale patch failed: "+old[:55])
    s=s.replace(old,new,1)

school_path.write_text(s,encoding="utf-8")
g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 43',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.10"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.10: readable dinner composition")
