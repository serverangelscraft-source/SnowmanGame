from pathlib import Path
import re

main_path = Path('app/src/main/java/com/snowmangame/MainActivity.java')
school_path = Path('app/src/main/java/com/snowmangame/SchoolWeekActivity.java')
gradle_path = Path('app/build.gradle')

main = main_path.read_text(encoding='utf-8')
school = school_path.read_text(encoding='utf-8')


def replace_once(text, old, new, label):
    if new in text:
        return text
    if old not in text:
        raise SystemExit('v18.11 patch target changed: ' + label)
    return text.replace(old, new, 1)

# Slow the pre-school life progression without timers: 3 distinct snowman missions per year.
main = replace_once(
    main,
    '        int year, wallet, runCoins, mission;',
    '        int year, wallet, runCoins, mission, yearBuilds;',
    'yearBuilds field',
)
main = replace_once(
    main,
    '            wallet=Math.max(0,prefs.getInt("coins",0));',
    '            wallet=Math.max(0,prefs.getInt("coins",0));\n            yearBuilds=Math.max(0,Math.min(3,prefs.getInt("year_builds_"+year,0)));',
    'load yearBuilds',
)
main = main.replace('mission=rnd.nextInt(3);', 'mission=yearBuilds<3?yearBuilds:rnd.nextInt(3);')

main = replace_once(
    main,
    '                runCoins=Math.max(1,score/300);wallet+=runCoins;coinsAwarded=true;prefs.edit().putInt("coins",wallet).apply();',
    '                runCoins=Math.max(1,score/300);wallet+=runCoins;coinsAwarded=true;yearBuilds=Math.min(3,yearBuilds+1);prefs.edit().putInt("coins",wallet).putInt("year_builds_"+year,yearBuilds).apply();',
    'year progress award',
)

old_sponsor = '''            sponsorBtn.set(card.left+dp(22),card.bottom-dp(178),card.right-dp(22),card.bottom-dp(128));
            p.setColor(sponsorRewarded?Color.rgb(188,190,193):Color.rgb(226,91,122));c.drawRoundRect(sponsorBtn,dp(18),dp(18),p);
            text.setTextSize(tx(9.5f));text.setColor(Color.WHITE);c.drawText(sponsorRewarded?"ЕСКІМОС УЖЕ СКУШТОВАНО":"СПРОБУВАТИ ЕСКІМОС +150",sponsorBtn.centerX(),sponsorBtn.centerY()+dp(3),text);'''
new_sponsor = '''            sponsorBtn.set(card.left+dp(22),card.bottom-dp(178),card.right-dp(22),card.bottom-dp(128));
            boolean yearReady=year>=7||yearBuilds>=3;
            p.setColor(yearReady?Color.rgb(231,246,238):Color.rgb(239,245,248));c.drawRoundRect(sponsorBtn,dp(18),dp(18),p);
            text.setTextSize(tx(8.4f));text.setColor(yearReady?Color.rgb(50,126,96):Color.rgb(79,119,140));
            String yearProgress=year>=7?"ШКІЛЬНИЙ ЕТАП ВІДКРИТО":(yearReady?"3/3 • ПОДОРОЖ У НОВИЙ РІК ВІДКРИТО":"ПРОГРЕС РОКУ "+yearBuilds+"/3 • ЩЕ "+(3-yearBuilds)+" ДО ПОДОРОЖІ");
            c.drawText(yearProgress,sponsorBtn.centerX(),sponsorBtn.centerY()+dp(3),text);'''
main = replace_once(main, old_sponsor, new_sponsor, 'remove ice-cream CTA')

old_journey = '''            journeyBtn.set(card.left+dp(22),card.bottom-dp(116),card.right-dp(22),card.bottom-dp(62));
            p.setColor(Color.rgb(35,106,153));c.drawRoundRect(journeyBtn,dp(19),dp(19),p);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);
            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ");
            c.drawText(journeyLabel,journeyBtn.centerX(),journeyBtn.centerY()+dp(4),text);'''
new_journey = '''            journeyBtn.set(card.left+dp(22),card.bottom-dp(116),card.right-dp(22),card.bottom-dp(62));
            p.setColor(yearReady?Color.rgb(35,106,153):Color.rgb(165,188,201));c.drawRoundRect(journeyBtn,dp(19),dp(19),p);text.setTextSize(tx(yearReady?10.5f:8.8f));text.setColor(Color.WHITE);
            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(yearReady?(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ"):("ЗРОБИ ЩЕ "+(3-yearBuilds)+" СНІГОВИК(И)"));
            c.drawText(journeyLabel,journeyBtn.centerX(),journeyBtn.centerY()+dp(4),text);'''
main = replace_once(main, old_journey, new_journey, 'gate journey until 3 builds')

main = replace_once(
    main,
    '                    if(sponsorBtn.contains(x,y)&&!sponsorRewarded){sponsorScene=true;sponsorStart=SystemClock.elapsedRealtime();buzz(20);invalidate();return true;}\n                    if(journeyBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,year>=7?SchoolActivity.class:DeliveryActivity.class));((Activity)ctx).finish();return true;}',
    '                    if(journeyBtn.contains(x,y)&&(year>=7||yearBuilds>=3)){ctx.startActivity(new Intent(ctx,year>=7?SchoolActivity.class:DeliveryActivity.class));((Activity)ctx).finish();return true;}',
    'remove sponsor interaction and gate journey',
)

# Materialize the dinner UI that Gradle used to mutate at build time.
dinner_replacements = [
    ('centerText(c,DISHES[idx],r.top+dp(70),20,Color.rgb(48,82,99));', 'centerText(c,DISHES[idx],r.top+dp(78),27,Color.rgb(48,82,99));'),
    ('centerText(c,"Після школи — одна домашня українська страва.",r.top+dp(101),7.3f,Color.rgb(99,124,134));', 'centerText(c,"Після школи — одна домашня українська страва.",r.top+dp(116),8.5f,Color.rgb(99,124,134));'),
    ('dishRect.set(r.centerX()-dp(95),r.top+dp(135),r.centerX()+dp(95),r.top+dp(260));drawDish(c,r.centerX(),r.top+dp(198),dp(72),idx);', 'dishRect.set(r.centerX()-dp(138),r.top+dp(145),r.centerX()+dp(138),r.top+dp(330));drawDish(c,r.centerX(),r.top+dp(238),dp(108),idx);'),
    ('c.drawCircle(r.centerX()-dp(42)+i*dp(21),r.top+dp(199),dp(5),p);', 'c.drawCircle(r.centerX()-dp(56)+i*dp(28),r.top+dp(239),dp(7),p);'),
    ('centerText(c,"З\'їдено "+Math.min(5,dinnerBites)+"/5",r.top+dp(286),7.2f,Color.rgb(89,122,137));', 'centerText(c,"З\'їдено "+Math.min(5,dinnerBites)+"/5",r.top+dp(360),9.2f,Color.rgb(89,122,137));'),
    ('drawHero(c,r.centerX(),r.bottom-dp(42),dp(36));', 'drawHero(c,r.centerX(),r.bottom-dp(56),dp(49));'),
    ('centerText(c,"Торкайся тарілки, щоб повечеряти.",r.bottom-dp(92),6.8f,Color.rgb(111,132,139));', 'centerText(c,"Торкайся великої тарілки, щоб повечеряти.",r.bottom-dp(122),8.2f,Color.rgb(111,132,139));'),
]
for old, new in dinner_replacements:
    if new not in school:
        if old not in school:
            raise SystemExit('v18.11 dinner source target changed: ' + old[:60])
        school = school.replace(old, new, 1)

# Canonical clean Gradle file: no source mutation during preBuild.
gradle = '''plugins {
    id 'com.android.application'
}

android {
    namespace 'com.snowmangame'
    compileSdk 35

    defaultConfig {
        applicationId "com.snowmangame"
        minSdk 24
        targetSdk 35
        versionCode 44
        versionName "18.11"
    }
}
'''

main_path.write_text(main, encoding='utf-8')
school_path.write_text(school, encoding='utf-8')
gradle_path.write_text(gradle, encoding='utf-8')
print('Applied v18.11: 3-build year pacing, removed ice-cream CTA, canonicalized dinner UI, removed Gradle source mutation')
