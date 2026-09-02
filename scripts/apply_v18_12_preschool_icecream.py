from pathlib import Path
import re

main_path=Path('app/src/main/java/com/snowmangame/MainActivity.java')
gradle_path=Path('app/build.gradle')
main=main_path.read_text(encoding='utf-8')
gradle=gradle_path.read_text(encoding='utf-8')

def rep(old,new,label):
    global main
    if new in main:
        return
    if old not in main:
        raise SystemExit('v18.12 target changed: '+label)
    main=main.replace(old,new,1)

rep(
'''                    if(journeyBtn.contains(x,y)&&(year>=7||yearBuilds>=3)){ctx.startActivity(new Intent(ctx,year>=7?SchoolActivity.class:DeliveryActivity.class));((Activity)ctx).finish();return true;}''',
'''                    if(journeyBtn.contains(x,y)&&(year>=7||yearBuilds>=3)){
                        if(year>=7&&!prefs.getBoolean("pre_school_icecream_done",false)){
                            sponsorScene=true;sponsorRewarded=false;sponsorStart=SystemClock.elapsedRealtime();buzz(20);invalidate();return true;
                        }
                        ctx.startActivity(new Intent(ctx,year>=7?SchoolActivity.class:DeliveryActivity.class));((Activity)ctx).finish();return true;
                    }''',
'route school through preschool ice cream')

rep(
'''                    if(t>4.8f&&sponsorCloseBtn.contains(x,y)){sponsorScene=false;invalidate();}''',
'''                    if(t>4.8f&&sponsorCloseBtn.contains(x,y)){
                        if(year>=7){prefs.edit().putBoolean("pre_school_icecream_done",true).apply();ctx.startActivity(new Intent(ctx,SchoolActivity.class));((Activity)ctx).finish();return true;}
                        sponsorScene=false;invalidate();
                    }''',
'finish preschool ice cream into school')

rep(
'''c.drawText("ДЕМО-ІНТЕГРАЦІЯ",poster.left+dp(16),poster.top+dp(19),text);''',
'''c.drawText(year>=7?"ПЕРЕД ШКОЛОЮ • ДЕМО-ІНТЕГРАЦІЯ":"ДЕМО-ІНТЕГРАЦІЯ",poster.left+dp(16),poster.top+dp(19),text);''',
'preschool scene label')

rep(
'''c.drawText("ПОВЕРНУТИСЯ ДО РЕЗУЛЬТАТУ",sponsorCloseBtn.centerX(),sponsorCloseBtn.centerY()+dp(4),text);''',
'''c.drawText(year>=7?"ДО ШКОЛИ":"ПОВЕРНУТИСЯ ДО РЕЗУЛЬТАТУ",sponsorCloseBtn.centerX(),sponsorCloseBtn.centerY()+dp(4),text);''',
'preschool scene close label')

gradle=re.sub(r'versionCode\s+\d+','versionCode 45',gradle)
gradle=re.sub(r'versionName\s+"[^"]+"','versionName "18.12"',gradle)
main_path.write_text(main,encoding='utf-8')
gradle_path.write_text(gradle,encoding='utf-8')
print('Applied v18.12: ice cream is a one-time pre-school scene only')
