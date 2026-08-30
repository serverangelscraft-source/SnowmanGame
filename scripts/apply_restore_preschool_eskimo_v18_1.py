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

# Insert the original Eskimo action specifically inside the ACTION_UP/CANCEL block,
# immediately before that block's finished-state routing. This deliberately does
# not depend on what later story version uses as the journey destination.
sponsor_guard='if(finished&&sponsorBtn.contains(x,y)&&!sponsorRewarded){sponsorScene=true;sponsorStart=SystemClock.elapsedRealtime();buzz(20);invalidate();return true;}'
if sponsor_guard not in main:
    action_marker='if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){'
    a=main.find(action_marker)
    if a<0:
        raise SystemExit("v18.1 preschool Eskimo restore failed: ACTION_UP anchor")
    f=main.find('if(finished){',a+len(action_marker))
    if f<0:
        raise SystemExit("v18.1 preschool Eskimo restore failed: finished state inside ACTION_UP")
    line_start=main.rfind('\n',0,f)+1
    indent=main[line_start:f]
    main=main[:line_start]+indent+sponsor_guard+'\n'+main[line_start:]

main_path.write_text(main,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 34',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.1"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.1: restored original Eskimo only before school")
