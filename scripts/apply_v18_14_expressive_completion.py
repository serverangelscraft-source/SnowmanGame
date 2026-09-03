from pathlib import Path
import re

main_path=Path('app/src/main/java/com/snowmangame/MainActivity.java')
gradle_path=Path('app/build.gradle')
plan_path=Path('docs/PLAYER_RETURN_PLAN.md')
main=main_path.read_text(encoding='utf-8')
gradle=gradle_path.read_text(encoding='utf-8')
plan=plan_path.read_text(encoding='utf-8')

def rep(text, old, new, label):
    if new in text:
        return text
    if old not in text:
        raise SystemExit('v18.14 target changed: '+label)
    return text.replace(old,new,1)

main=rep(main,
'''        int avgDecor(){return decorPlaced==0?0:decorQuality/decorPlaced;}''',
'''        int avgDecor(){return decorPlaced==0?0:decorQuality/decorPlaced;}\n        boolean requiredDecorReady(){return items[EYES].placed&&items[NOSE].placed&&decorPlaced>=3;}\n        int optionalDecorCount(){return Math.max(0,decorPlaced-3);}''',
'completion helpers')

main=rep(main,
'''            int acc=Math.min(94,84+year),sec=Math.max(58,94-year*4);''',
'''            int acc=Math.min(94,84+year),sec=Math.max(48,72-year*3);''',
'mission speed rebalance')
# Same expression appears again in finishGame.
main=main.replace('int acc=Math.min(94,84+year),sec=Math.max(58,94-year*4);','int acc=Math.min(94,84+year),sec=Math.max(48,72-year*3);')

main=rep(main,
'''            float interactionH=balls<3?(compact?dp(112):dp(132)):(compact?dp(136):dp(158));''',
'''            float interactionH=balls<3?(compact?dp(112):dp(132)):(compact?dp(190):dp(214));''',
'decor panel height')

old_layout='''            float gap=dp(6),pad=dp(8);\n            float sw=(interaction.width()-pad*2-gap*2)/3f;\n            float sh=(interaction.height()-pad*2-gap)/2f;\n            for(int i=0;i<ACCESSORY_COUNT;i++){\n                int row=i/3,col=i%3;\n                float l=interaction.left+pad+col*(sw+gap),t=interaction.top+pad+row*(sh+gap);\n                items[i].slot.set(l,t,l+sw,t+sh);\n            }\n            finishBtn.set(interaction.left+dp(28),interaction.top+dp(22),interaction.right-dp(28),interaction.bottom-dp(22));'''
new_layout='''            float gap=dp(6),pad=dp(8),buttonH=dp(48);\n            float sw=(interaction.width()-pad*2-gap*2)/3f;\n            float slotsBottom=interaction.bottom-pad-buttonH-gap;\n            float sh=(slotsBottom-(interaction.top+pad)-gap)/2f;\n            for(int i=0;i<ACCESSORY_COUNT;i++){\n                int row=i/3,col=i%3;\n                float l=interaction.left+pad+col*(sw+gap),t=interaction.top+pad+row*(sh+gap);\n                items[i].slot.set(l,t,l+sw,t+sh);\n            }\n            finishBtn.set(interaction.left+pad,slotsBottom+gap,interaction.right-pad,interaction.bottom-pad);'''
main=rep(main,old_layout,new_layout,'reserve finish button')

old_hud='''            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(83,116,134));text.setTextSize(tx(narrow?6.8f:7.6f));\n            c.drawText("РІК "+year+" • "+ageName(),hud.left+dp(14),hud.top+dp(18),text);\n            text.setTextSize(tx(narrow?14:16));text.setColor(Color.rgb(38,69,89));\n            c.drawText(balls<3?"КУЛЯ "+(balls+1)+"/3":"ДЕКОР "+decorPlaced+"/6",hud.left+dp(14),hud.bottom-dp(13),text);\n            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(narrow?10:11));text.setColor(Color.rgb(34,104,146));\n            c.drawText("● "+wallet,hud.right-dp(14),hud.top+dp(25),text);\n            text.setTextSize(tx(6.6f));text.setColor(Color.rgb(108,139,154));\n            c.drawText("монети",hud.right-dp(14),hud.bottom-dp(11),text);'''
new_hud='''            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(83,116,134));text.setTextSize(tx(narrow?6.8f:7.6f));\n            c.drawText(balls<3?"ЛІПЛЕННЯ":"ОФОРМЛЕННЯ",hud.left+dp(14),hud.top+dp(18),text);\n            text.setTextSize(tx(narrow?14:16));text.setColor(Color.rgb(38,69,89));\n            c.drawText(balls<3?"КУЛЯ "+(balls+1)+"/3":"ДЕТАЛІ "+decorPlaced+"/6",hud.left+dp(14),hud.bottom-dp(13),text);\n            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(narrow?7.4f:8.4f));text.setColor(Color.rgb(34,104,146));\n            c.drawText(rewardedBuildsToday<3?("НАГОРОДИ "+rewardedBuildsToday+"/3"):"ВІЛЬНА РОБОТА",hud.right-dp(14),hud.top+dp(22),text);\n            text.setTextSize(tx(6.4f));text.setColor(Color.rgb(108,139,154));\n            c.drawText("Рік "+year+" • ● "+wallet,hud.right-dp(14),hud.bottom-dp(11),text);'''
main=rep(main,old_hud,new_hud,'task-first HUD')

old_tray='''            if(decorPlaced==ACCESSORY_COUNT){\n                p.setColor(Color.rgb(38,105,145));c.drawRoundRect(finishBtn,dp(20),dp(20),p);\n                text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12));text.setColor(Color.WHITE);\n                c.drawText("ЗАВЕРШИТИ СНІГОВИКА",finishBtn.centerX(),finishBtn.centerY()+dp(4),text);\n                return;\n            }\n            for(Accessory a:items){'''
new_tray='''            for(Accessory a:items){'''
main=rep(main,old_tray,new_tray,'remove 6-of-6 gate')

old_tray_end='''                c.drawText(label,a.slot.centerX(),a.slot.bottom-dp(7),text);\n            }\n        }'''
new_tray_end='''                c.drawText(label,a.slot.centerX(),a.slot.bottom-dp(7),text);\n            }\n            boolean ready=requiredDecorReady();\n            p.setColor(ready?Color.rgb(38,105,145):Color.rgb(228,238,244));c.drawRoundRect(finishBtn,dp(17),dp(17),p);\n            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(ready?9.6f:7.3f));text.setColor(ready?Color.WHITE:Color.rgb(91,126,143));\n            String finishLabel=ready?(optionalDecorCount()>0?("ЗАВЕРШИТИ • СТИЛЬ +"+optionalDecorCount()):"ЗАВЕРШИТИ СНІГОВИКА"):("ПОТРІБНО: ОЧІ + МОРКВА + ЩЕ 1");\n            c.drawText(finishLabel,finishBtn.centerX(),finishBtn.centerY()+dp(3),text);\n        }'''
main=rep(main,old_tray_end,new_tray_end,'persistent finish action')

main=rep(main,
'''                if(balls>=3&&decorPlaced==ACCESSORY_COUNT&&finishBtn.contains(x,y)){finishGame();return true;}''',
'''                if(balls>=3&&requiredDecorReady()&&finishBtn.contains(x,y)){finishGame();return true;}''',
'finish touch gate')

main=rep(main,
'''                if(decorPlaced>=ACCESSORY_COUNT)tip="Декор готовий — заверши сніговика";\n                else if(d<=tol)tip=a.name+": "+q+"%. Наступна деталь";''',
'''                if(decorPlaced>=ACCESSORY_COUNT)tip="Усі деталі на місці — можна завершувати";\n                else if(requiredDecorReady())tip="Можна завершити або додати ще деталей для стилю";\n                else if(d<=tol)tip=a.name+": "+q+"%. Потрібні очі, морква і ще одна деталь";''',
'tip after third meaningful part')

gradle=re.sub(r'versionCode\s+\d+','versionCode 47',gradle)
gradle=re.sub(r'versionName\s+"[^"]+"','versionName "18.14"',gradle)

plan=plan.replace('Updated: 2026-09-02\nCurrent Android build: v18.13','Updated: 2026-09-03\nCurrent Android build: v18.14')
plan=plan.replace('4. **Completion still requires all 6 accessories.** This makes every run structurally similar and reduces expressive builds; speed missions also become partly artificial.','4. **Forced 6/6 completion — fixed in v18.14.** Eyes + carrot + any third part unlock completion; the remaining parts stay optional and add style/score.')
plan=plan.replace('5. **Top HUD still prioritizes year/coins.** It should emphasize the current tactile task and today\'s special condition instead.','5. **Task-first HUD — improved in v18.14.** Current tactile step and daily reward/free status are primary; year/wallet are secondary. Daily snow condition is still missing.')
marker='''### DONE v18.13 — mission anti-reroll\n- One deterministic mission for local calendar day + year.\n- Replays on the same date keep the same mission.\n'''
addition='''\n### DONE v18.14 — expressive completion + mobile action hierarchy\n- Eyes + carrot + any third accessory unlock the finish action.\n- Remaining accessories stay visible and optional; each still adds score/character.\n- The finish button has a reserved bottom touch area instead of replacing the accessory tray.\n- Speed mission target is rebalanced for fewer mandatory interactions.\n- HUD prioritizes current sculpting step and today\'s 3-reward/free-work status; year/wallet are secondary.\n'''
if addition.strip() not in plan:
    if marker not in plan: raise SystemExit('plan milestone anchor changed')
    plan=plan.replace(marker,marker+addition)
plan=plan.replace('### P1 — expressive completion\n- Replace forced 6/6 completion with a minimum meaningful set: target 3 required parts, remaining parts optional.\n- Optional decoration should improve style/character score rather than block completion.\n- Keep placement hints for eyes/nose but allow intentionally odd snowmen.\n- Rebalance speed mission after the mandatory interaction count is reduced.\n\n### P1 — phone UI\n- During sculpting show only current step, short tactile hint, and contextual action.\n- Move wallet/year metadata to a secondary strip or result screen.\n- Keep all bottom controls above gesture navigation and comfortably reachable one-handed.\n\n','')
plan=plan.replace('The v18.13 reward-loop milestone is achieved. The next working milestone should be considered ready when:\n1. a player may finish a valid snowman with 3 meaningful required parts;\n2. optional parts improve character/style rather than gate completion;\n3. a date-stable snow condition visibly and mechanically changes the run;\n4. the main phone play screen remains uncluttered and one-handed;\n5. daily reward/free-mode rules from v18.13 remain intact.','The v18.14 expressive-completion milestone is achieved. The next working milestone should be considered ready when:\n1. a date-stable snow condition visibly and mechanically changes the run;\n2. the condition is communicated without another large HUD panel;\n3. one optional daily curiosity/client card adds variety without another farmable currency;\n4. daily reward/free-mode rules from v18.13 remain intact;\n5. school and pre-school story routing remain unaffected.')
update='''\n## Cycle update — 2026-09-03 early cycle\n- Closed the forced 6/6 accessory gate without removing optional decoration.\n- Preserved all six accessories and their score value, but made completion expressive after three meaningful parts.\n- Reserved a one-handed bottom finish action and simplified the HUD hierarchy.\n- Rebalanced the speed mission to avoid becoming trivial after fewer mandatory actions.\n- Next P2 is date-stable snow feel; after that, add one optional daily curiosity/client card.\n- Fresh business inspiration: the August 2026 multi-brand “Покоління 91” collaboration suggests a safe non-branded “майстерня чотирьох майстрів” cosmetic-memory week: different fictional makers contribute scarf/hat/button styles, with no logos, purchases or claims of partnership.\n'''
if update.strip() not in plan:
    plan += update

main_path.write_text(main,encoding='utf-8')
gradle_path.write_text(gradle,encoding='utf-8')
plan_path.write_text(plan,encoding='utf-8')
print('Applied v18.14 expressive completion, task-first HUD, speed rebalance, plan advance')
