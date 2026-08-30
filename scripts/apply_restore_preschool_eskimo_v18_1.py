from pathlib import Path
import re

main_path=Path("app/src/main/java/com/snowmangame/MainActivity.java")
gradle_path=Path("app/build.gradle")
main=main_path.read_text(encoding="utf-8")

# v18 moved the school years to SchoolWeekActivity. Restore the original Eskimo
# result interaction in MainActivity only; this activity is the pre-school life loop.
old_draw='            if(sponsorScene)sponsorScene=false;'
new_draw='            if(sponsorScene){drawSponsor(c);postInvalidateOnAnimation();return;}'
if old_draw not in main:
    raise SystemExit("v18.1 preschool Eskimo restore failed: sponsor scene draw gate")
main=main.replace(old_draw,new_draw,1)

old_btn='            sponsorBtn.setEmpty();'
new_btn='''            sponsorBtn.set(card.left+dp(22),card.bottom-dp(178),card.right-dp(22),card.bottom-dp(128));
            p.setColor(sponsorRewarded?Color.rgb(188,190,193):Color.rgb(226,91,122));c.drawRoundRect(sponsorBtn,dp(18),dp(18),p);
            text.setTextSize(tx(9.5f));text.setColor(Color.WHITE);c.drawText(sponsorRewarded?"ЕСКІМОС УЖЕ СКУШТОВАНО":"СПРОБУВАТИ ЕСКІМОС +150",sponsorBtn.centerX(),sponsorBtn.centerY()+dp(3),text);'''
if old_btn not in main:
    raise SystemExit("v18.1 preschool Eskimo restore failed: result CTA")
main=main.replace(old_btn,new_btn,1)

sponsor_touch='if(sponsorBtn.contains(x,y)&&!sponsorRewarded){sponsorScene=true;sponsorStart=SystemClock.elapsedRealtime();buzz(20);invalidate();return true;}'
journey_touch='if(journeyBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,DeliveryActivity.class));((Activity)ctx).finish();return true;}'
if sponsor_touch not in main:
    if journey_touch not in main:
        raise SystemExit("v18.1 preschool Eskimo restore failed: journey touch anchor")
    main=main.replace(journey_touch,sponsor_touch+'\n                    '+journey_touch,1)

main_path.write_text(main,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 34',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.1"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.1: restored original Eskimo only before school")
