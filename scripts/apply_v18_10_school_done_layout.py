from pathlib import Path
import re

school_path = Path('app/src/main/java/com/snowmangame/SchoolWeekActivity.java')
gradle_path = Path('app/build.gradle')

school = school_path.read_text(encoding='utf-8')
gradle = gradle_path.read_text(encoding='utf-8')

old = '''        void drawDone(Canvas c){RectF r=card();cardBase(c,r);centerText(c,className()+" • ПРОЖИТО "+totalDone()+"/7",r.top+dp(29),7,Color.rgb(109,132,140));centerText(c,"СЬОГОДНІШНІЙ ДЕНЬ ПРОЖИТО",r.top+dp(72),20,Color.rgb(42,106,141));String detail=isWeekday()?("Навчання "+schoolDone+"/5 • остання вечеря: "+prefs.getString("school_meal_last","—")):("Вихідні "+weekendDone+"/2 • "+prefs.getString("school_weekend_last","день прожито"));centerText(c,detail,r.top+dp(106),7.5f,Color.rgb(95,121,133));RectF lock=new RectF(r.left+dp(25),r.top+dp(137),r.right-dp(25),r.top+dp(215));p.setColor(Color.rgb(235,245,249));c.drawRoundRect(lock,dp(19),dp(19),p);centerTextAt(c,"НАСТУПНИЙ ДЕНЬ МОЖНА ПРОЖИТИ НЕ РАНІШЕ ЗАВТРА",lock.centerX(),lock.top+dp(30),6.7f,Color.rgb(105,129,139));centerTextAt(c,"Пропустиш завтра — прогрес залишиться "+totalDone()+"/7",lock.centerX(),lock.bottom-dp(20),7.8f,Color.rgb(50,104,134));drawHero(c,r.centerX()-dp(36),r.bottom-dp(40),dp(35));drawFriend(c,r.centerX()+dp(48),r.bottom-dp(40),dp(28));drawBottomTools(c);}'''

new = '''        void drawDone(Canvas c){
            RectF r=card();cardBase(c,r);
            centerText(c,className()+" • ПРОЖИТО "+totalDone()+"/7",r.top+dp(27),7,Color.rgb(109,132,140));
            centerText(c,"ДЕНЬ ПРОЖИТО",r.top+dp(66),18,Color.rgb(42,106,141));
            String detail=isWeekday()?("Навчання "+schoolDone+"/5 • вечеря: "+prefs.getString("school_meal_last","—")):("Вихідні "+weekendDone+"/2 • "+prefs.getString("school_weekend_last","день прожито"));
            centerText(c,detail,r.top+dp(96),7.3f,Color.rgb(95,121,133));

            RectF lock=new RectF(r.left+dp(24),r.top+dp(118),r.right-dp(24),r.top+dp(184));
            p.setColor(Color.rgb(235,245,249));c.drawRoundRect(lock,dp(19),dp(19),p);
            centerTextAt(c,"НАСТУПНИЙ ДЕНЬ — НЕ РАНІШЕ ЗАВТРА",lock.centerX(),lock.top+dp(25),6.9f,Color.rgb(105,129,139));
            centerTextAt(c,"Пропустиш день — прогрес залишиться "+totalDone()+"/7",lock.centerX(),lock.bottom-dp(17),7.4f,Color.rgb(50,104,134));

            centerText(c,"А зараз можна просто побути разом або зайти до спогадів.",lock.bottom+dp(31),7.1f,Color.rgb(99,125,136));
            float freeH=Math.max(dp(140),r.bottom-lock.bottom-dp(45));
            float heroR=Math.max(dp(40),Math.min(dp(60),freeH/3.85f));
            float friendR=heroR*.76f;
            float ground=r.bottom-dp(26);
            drawHero(c,r.centerX()-heroR*.68f,ground,heroR);
            drawFriend(c,r.centerX()+heroR*.96f,ground,friendR);
            drawBottomTools(c);
        }'''

if new not in school:
    if old not in school:
        raise SystemExit('v18.10 drawDone pattern changed')
    school = school.replace(old, new, 1)

gradle = re.sub(r'versionCode\s+\d+', 'versionCode 43', gradle)
gradle = re.sub(r'versionName\s+"[^"]+"', 'versionName "18.10"', gradle)

school_path.write_text(school, encoding='utf-8')
gradle_path.write_text(gradle, encoding='utf-8')
print('Applied v18.10: compact DONE screen, larger characters, less dead space')
