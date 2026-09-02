from pathlib import Path
import re

main_path=Path('app/src/main/java/com/snowmangame/MainActivity.java')
gradle_path=Path('app/build.gradle')
plan_path=Path('docs/PLAYER_RETURN_PLAN.md')
main=main_path.read_text(encoding='utf-8')
gradle=gradle_path.read_text(encoding='utf-8')
plan=plan_path.read_text(encoding='utf-8')

def rep(old,new,label):
    global main
    if new in main:
        return
    if old not in main:
        raise SystemExit('v18.13 target changed: '+label)
    main=main.replace(old,new,1)

rep('import java.util.Random;','import java.util.Random;\nimport java.util.Calendar;\nimport java.util.GregorianCalendar;\nimport java.util.TimeZone;','calendar imports')
rep('        int year, wallet, runCoins, mission, yearBuilds;','        int year, wallet, runCoins, mission, yearBuilds, rewardedBuildsToday;\n        long rewardDay;','daily reward fields')
rep('            yearBuilds=Math.max(0,Math.min(3,prefs.getInt("year_builds_"+year,0)));','            yearBuilds=Math.max(0,Math.min(3,prefs.getInt("year_builds_"+year,0)));\n            syncDailyState();','load daily reward state')
rep('            mission=yearBuilds<3?yearBuilds:rnd.nextInt(3);','            mission=dailyMission();','daily mission constructor')

anchor='''        String timeText(int s){return String.format("%d:%02d",s/60,s%60);}'''
insert='''        long localDayNumber(){
            Calendar local=Calendar.getInstance();
            int y=local.get(Calendar.YEAR),m=local.get(Calendar.MONTH),d=local.get(Calendar.DAY_OF_MONTH);
            GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            utc.clear();utc.set(y,m,d,0,0,0);
            return utc.getTimeInMillis()/86400000L;
        }
        void syncDailyState(){
            long now=localDayNumber();
            long saved=prefs.getLong("reward_day",Long.MIN_VALUE);
            if(saved!=now){
                rewardDay=now;rewardedBuildsToday=0;
                prefs.edit().putLong("reward_day",now).putInt("rewarded_builds_today",0).apply();
            }else{
                rewardDay=saved;rewardedBuildsToday=Math.max(0,Math.min(3,prefs.getInt("rewarded_builds_today",0)));
            }
        }
        int dailyMission(){
            long seed=rewardDay*31L+year*17L;
            return (int)Math.floorMod(seed,3L);
        }
        String timeText(int s){return String.format("%d:%02d",s/60,s%60);}'''
rep(anchor,insert,'daily state helpers')

old='''            if(!coinsAwarded){
                runCoins=Math.max(1,score/300);wallet+=runCoins;coinsAwarded=true;yearBuilds=Math.min(3,yearBuilds+1);prefs.edit().putInt("coins",wallet).putInt("year_builds_"+year,yearBuilds).apply();
            }'''
new='''            if(!coinsAwarded){
                syncDailyState();
                coinsAwarded=true;
                if(rewardedBuildsToday<3){
                    runCoins=Math.max(1,score/300);wallet+=runCoins;rewardedBuildsToday++;yearBuilds=Math.min(3,yearBuilds+1);
                    prefs.edit().putInt("coins",wallet).putLong("reward_day",rewardDay).putInt("rewarded_builds_today",rewardedBuildsToday).putInt("year_builds_"+year,yearBuilds).apply();
                }else runCoins=0;
            }'''
rep(old,new,'daily reward gate')

old_coin='''            p.setColor(Color.rgb(235,247,239));c.drawRoundRect(coin,dp(17),dp(17),p);text.setTextSize(tx(9));text.setColor(Color.rgb(55,126,99));c.drawText("+"+runCoins+" МОНЕТ • БАЛАНС "+wallet,coin.centerX(),coin.centerY()+dp(3),text);'''
new_coin='''            p.setColor(runCoins>0?Color.rgb(235,247,239):Color.rgb(239,245,248));c.drawRoundRect(coin,dp(17),dp(17),p);text.setTextSize(tx(8.5f));text.setColor(runCoins>0?Color.rgb(55,126,99):Color.rgb(79,119,140));
            String rewardText=runCoins>0?("+"+runCoins+" МОНЕТ • НАГОРОДИ "+rewardedBuildsToday+"/3"):("ВІЛЬНА РОБОТА • НАГОРОДИ 3/3 • БАЛАНС "+wallet);
            c.drawText(rewardText,coin.centerX(),coin.centerY()+dp(3),text);'''
rep(old_coin,new_coin,'finish reward status')

rep('sponsorScene=false;sponsorRewarded=false;coinsAwarded=false;runCoins=0;rollProgress=0;startTime=0;finishSeconds=0;missionSuccess=false;mission=yearBuilds<3?yearBuilds:rnd.nextInt(3);giftType=-1;',
    'sponsorScene=false;sponsorRewarded=false;coinsAwarded=false;runCoins=0;rollProgress=0;startTime=0;finishSeconds=0;missionSuccess=false;syncDailyState();mission=dailyMission();giftType=-1;',
    'reset keeps daily mission')

gradle=re.sub(r'versionCode\s+\d+','versionCode 46',gradle)
gradle=re.sub(r'versionName\s+"[^"]+"','versionName "18.13"',gradle)

plan=plan.replace('Current Android build: v18.10','Current Android build: v18.13')
plan=plan.replace('1. **Unlimited coin farming in MainActivity.** Every replay resets `coinsAwarded=false`, so every completed snowman can award coins again. There is no per-calendar-day paid reward cap.','1. **Daily reward farming — fixed in v18.13.** Android now stores `reward_day` + `rewarded_builds_today`; only the first 3 completed builds of the local calendar date award coins/progression. Later builds remain playable as free work.')
plan=plan.replace('3. **Random mission has weak return motivation.** It changes per replay, not per day; therefore it feels like rerolling rather than a daily reason to come back.','3. **Mission reroll — fixed in v18.13.** Mission is deterministic for local calendar day + year and stays unchanged through replays; it changes on a new local date.')
plan=plan.replace('### P0 — daily reward integrity','### DONE v18.13 — daily reward integrity')
plan=plan.replace('### P0 — mission anti-reroll','### DONE v18.13 — mission anti-reroll')
plan += '\n\n## Cycle update — 2026-09-02 late evening\n- Implemented local-date reward quota in Android: 3 rewarded/progression snowmen per day, then unlimited free sculpting.\n- Implemented stable daily mission keyed from local date + year; replay cannot reroll it.\n- Finish card now explicitly says either `НАГОРОДИ N/3` or `ВІЛЬНА РОБОТА • НАГОРОДИ 3/3`.\n- Next P1: expressive completion (minimum meaningful parts rather than forced 6/6) and daily snow condition.\n- Collaboration research direction: use current Ukrainian retail/logistics themes only as inspiration, never imply official partnership.\n'

main_path.write_text(main,encoding='utf-8')
gradle_path.write_text(gradle,encoding='utf-8')
plan_path.write_text(plan,encoding='utf-8')
print('Applied v18.13: date-aware 3/day rewards, stable daily mission, free mode after quota')
