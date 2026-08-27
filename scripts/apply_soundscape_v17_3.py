from pathlib import Path

paths={
    "main":Path("app/src/main/java/com/snowmangame/MainActivity.java"),
    "year2":Path("app/src/main/java/com/snowmangame/YearTwoActivity.java"),
    "year3":Path("app/src/main/java/com/snowmangame/YearThreeActivity.java"),
    "uklon":Path("app/src/main/java/com/snowmangame/UklonActivity.java"),
    "journey":Path("app/src/main/java/com/snowmangame/JourneyActivity.java"),
    "summer":Path("app/src/main/java/com/snowmangame/SummerActivity.java"),
    "memory":Path("app/src/main/java/com/snowmangame/MemoryActivity.java"),
    "character":Path("app/src/main/java/com/snowmangame/CharacterActivity.java"),
    "wardrobe":Path("app/src/main/java/com/snowmangame/WardrobeActivity.java"),
    "school":Path("app/src/main/java/com/snowmangame/SchoolActivity.java"),
}
src={k:p.read_text(encoding="utf-8") for k,p in paths.items()}
hits=[]

def rep(key,old,new,label,required=True,count=1):
    if old not in src[key]:
        if required: raise SystemExit(f"v17.3 sound patch failed in {key} at: {label}")
        print(f"v17.3 optional sound hook skipped: {key} / {label}")
        return
    src[key]=src[key].replace(old,new,count)
    hits.append(label)

# MAIN: remove electronic beeps, add physical snow/item sounds and a global sound toggle.
rep("main",
    '        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), memoryBtn=new RectF(), wardrobeBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();',
    '        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), memoryBtn=new RectF(), wardrobeBtn=new RectF(), soundBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();',
    "main sound button field")
rep("main",
    '            drawTip(c);\n            drawWardrobeButton(c);\n            drawSnowman(c);',
    '            drawTip(c);\n            drawWardrobeButton(c);drawSoundButton(c);\n            drawSnowman(c);',
    "main sound button draw")
rep("main",
    '            wardrobeBtn.set(w-(narrow?dp(103):dp(119)),tipCard.bottom+dp(7),w-m,tipCard.bottom+(narrow?dp(35):dp(39)));\n            interaction.set(m,bottom-interactionH-dp(8),w-m,bottom-dp(8));\n            playTop=wardrobeBtn.bottom+dp(5);',
    '            wardrobeBtn.set(w-(narrow?dp(103):dp(119)),tipCard.bottom+dp(7),w-m,tipCard.bottom+(narrow?dp(35):dp(39)));\n            soundBtn.set(m,tipCard.bottom+dp(7),m+(narrow?dp(88):dp(101)),tipCard.bottom+(narrow?dp(35):dp(39)));\n            interaction.set(m,bottom-interactionH-dp(8),w-m,bottom-dp(8));\n            playTop=Math.max(wardrobeBtn.bottom,soundBtn.bottom)+dp(5);',
    "main sound button layout")
rep("main",
    '        void drawWardrobeButton(Canvas c){',
    '        void drawSoundButton(Canvas c){p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(soundBtn,dp(13),dp(13),p);text.setTextAlign(Paint.Align.CENTER);String s=SoundFx.label(prefs);text.setTextSize(tx(narrow?5.7f:6.3f));while(text.measureText(s)>soundBtn.width()-dp(12)&&text.getTextSize()>tx(4.8f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(SoundFx.enabled(prefs)?Color.rgb(55,121,101):Color.rgb(135,137,137));c.drawText(s,soundBtn.centerX(),soundBtn.centerY()+dp(2),text);}\n\n        void drawWardrobeButton(Canvas c){',
    "main sound button renderer")
rep("main",
    '                if(!finished&&wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));invalidate();return true;}',
    '                if(!finished&&soundBtn.contains(x,y)){SoundFx.toggle(ctx);invalidate();return true;}\n                if(!finished&&wardrobeBtn.contains(x,y)){SoundFx.play(ctx,SoundFx.UI);ctx.startActivity(new Intent(ctx,WardrobeActivity.class));invalidate();return true;}',
    "main sound toggle touch")
rep("main",
    '        void playTone(int type,int ms){if(tone!=null)try{tone.startTone(type,ms);}catch(Exception ignored){}}',
    '        void playTone(int type,int ms){}',
    "disable legacy beeps")
rep("main",
    '        void crunch(){long now=SystemClock.elapsedRealtime();if(now-lastCrunchAt>145){lastCrunchAt=now;playTone(ToneGenerator.TONE_PROP_BEEP2,24);}}',
    '        void crunch(){long now=SystemClock.elapsedRealtime();if(now-lastCrunchAt>120){lastCrunchAt=now;SoundFx.crunch(ctx);}}',
    "procedural rolling crunch")
rep("main",'playTone(ToneGenerator.TONE_PROP_ACK,90);showFeedback("КУЛЯ ГОТОВА",true);','SoundFx.play(ctx,SoundFx.SNOW_READY);showFeedback("КУЛЯ ГОТОВА",true);',"snowball ready sound",False)
rep("main",'playTone(q>=90?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_BEEP,95);showFeedback((q>=90?"ТОЧНО! • ":"ТОЧНІСТЬ • ")+q','SoundFx.play(ctx,SoundFx.SNOW_SET);showFeedback((q>=90?"ТОЧНО! • ":"ТОЧНІСТЬ • ")+q',"snowball placement sound",False)
rep("main",'playTone(ToneGenerator.TONE_PROP_NACK,95);showFeedback("НЕ В КОНТУРІ • СПРОБУЙ ЩЕ",false);','SoundFx.play(ctx,SoundFx.ERROR);showFeedback("НЕ В КОНТУРІ • СПРОБУЙ ЩЕ",false);',"snow placement error sound",False)
rep("main",'playTone(q>=90?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_BEEP,80);showFeedback((q>=90?"ЧІТКО! • ":"ДЕТАЛЬ • ")+q','SoundFx.play(ctx,(type==SCARF||type==HAT)?SoundFx.CLOTH:SoundFx.ITEM);showFeedback((q>=90?"ЧІТКО! • ":"ДЕТАЛЬ • ")+q',"accessory placement sound",False)
rep("main",'playTone(ToneGenerator.TONE_PROP_NACK,80);showFeedback("ТРОХИ БЛИЖЧЕ ДО МІСЦЯ",false);','SoundFx.play(ctx,SoundFx.ERROR);showFeedback("ТРОХИ БЛИЖЧЕ ДО МІСЦЯ",false);',"accessory error sound",False)
rep("main",'playTone(ToneGenerator.TONE_PROP_ACK,180);showFeedback(missionSuccess?"РІК ЗАВЕРШЕНО • МІСІЯ +250":"РІК ЗАВЕРШЕНО",true);','SoundFx.play(ctx,SoundFx.COMPLETE);showFeedback(missionSuccess?"РІК ЗАВЕРШЕНО • МІСІЯ +250":"РІК ЗАВЕРШЕНО",true);',"winter complete sound",False)

# YEAR 2: replace ACK/NACK tones with snow/cloth/memory cues.
rep("year2",'        void tone(int type,int ms){if(tone!=null)try{tone.startTone(type,ms);}catch(Exception ignored){}}','        void tone(int type,int ms){}',"Year2 disable legacy tone")
rep("year2",'        void say(String s,boolean good){feedback=s;feedbackUntil=android.os.SystemClock.elapsedRealtime()+1200;buzz(good?28:12);tone(good?ToneGenerator.TONE_PROP_ACK:ToneGenerator.TONE_PROP_NACK,90);invalidate();}','        void say(String s,boolean good){feedback=s;feedbackUntil=android.os.SystemClock.elapsedRealtime()+1200;buzz(good?28:12);SoundFx.play(ctx,good?SoundFx.CORRECT:SoundFx.WRONG);invalidate();}',"Year2 feedback sound")
rep("year2",'        void next(int s){stage=s;dragging=false;if(s==TRAIL){snowX=dp(58);snowY=getHeight()-safeBottom-dp(115);}invalidate();}','        void next(int s){stage=s;dragging=false;if(s==TRAIL){snowX=dp(58);snowY=getHeight()-safeBottom-dp(115);}SoundFx.play(ctx,s==MITTEN?SoundFx.MITTEN:(s==READY?SoundFx.MEMORY:SoundFx.UI));invalidate();}',"Year2 stage sounds")

# YEAR 3: friendship and snowball play should sound physical rather than menu-like.
rep("year3",'        void next(int s){stage=s;stageStart=SystemClock.elapsedRealtime();prefs.edit().putInt("year3_stage",stage).apply();hint="";invalidate();}','        void next(int s){stage=s;stageStart=SystemClock.elapsedRealtime();prefs.edit().putInt("year3_stage",stage).apply();hint="";SoundFx.play(ctx,s==RETURN_MITTEN?SoundFx.MITTEN:(s==SNOWBALLS?SoundFx.PLAY:(s==DONE?SoundFx.MEMORY:SoundFx.UI)));invalidate();}',"Year3 stage sounds")
rep("year3",'hits++;prefs.edit().putInt("year3_snowball_hits",hits).apply();hint=hits<5?','hits++;SoundFx.play(ctx,SoundFx.HIT);prefs.edit().putInt("year3_snowball_hits",hits).apply();hint=hits<5?',"Year3 snowball hit",False)
rep("year3",'hint="Ой! Вікно не мішень. Сніжик: «Тікаємо… жартую!»";buzz(18);invalidate();','hint="Ой! Вікно не мішень. Сніжик: «Тікаємо… жартую!»";buzz(18);SoundFx.play(ctx,SoundFx.WRONG);invalidate();',"Year3 window mistake",False)

# UKLON: each transport stage gets a distinct cue.
rep("uklon",'        void next(int s){stage=s;stageStart=SystemClock.elapsedRealtime();dragging=false;invalidate();}','        void next(int s){stage=s;stageStart=SystemClock.elapsedRealtime();dragging=false;if(s==WAIT)SoundFx.play(ctx,SoundFx.PHONE);else if(s==BOARD)SoundFx.play(ctx,SoundFx.CAR_ARRIVE);else if(s==RIDE){SoundFx.play(ctx,SoundFx.CAR_DOOR);SoundFx.play(ctx,SoundFx.ENGINE);}else if(s==ARRIVED)SoundFx.play(ctx,SoundFx.ARRIVAL);invalidate();}',"Uklon transport cues")

# JOURNEY: ticket, validator, train and arrival each get their own audio identity.
rep("journey",'        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();hint="";invalidate();}','        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();hint="";if(s==CHOOSE_TICKET)SoundFx.play(getContext(),SoundFx.UI);else if(s==VALIDATE_TICKET)SoundFx.play(getContext(),SoundFx.TICKET);else if(s==FIND_WAGON)SoundFx.play(getContext(),SoundFx.CORRECT);else if(s==BOARDING)SoundFx.play(getContext(),SoundFx.CAR_DOOR);else if(s==TRAIN)SoundFx.play(getContext(),SoundFx.TRAIN);else if(s==ARRIVAL)SoundFx.play(getContext(),SoundFx.ARRIVAL);invalidate();}',"Journey stage sounds")

# SUMMER: a subtle melt cue and two drops, not a continuous loop.
rep("summer",'        float safeTop,safeBottom;','        float safeTop,safeBottom;boolean dripA,dripB;',"summer drip state")
rep("summer",'            requestApplyInsets();\n        }','            requestApplyInsets();SoundFx.play(ctx,SoundFx.MELT);\n        }',"summer melt entrance")
rep("summer",'            float t=(SystemClock.elapsedRealtime()-started)/1000f;\n            drawSummer(c,t);','            float t=(SystemClock.elapsedRealtime()-started)/1000f;if(t>2.2f&&!dripA){dripA=true;SoundFx.play(ctx,SoundFx.DRIP);}if(t>4.1f&&!dripB){dripB=true;SoundFx.play(ctx,SoundFx.DRIP);}\n            drawSummer(c,t);',"summer drip cues")

# MEMORY ROOM: opening the room and touching a memory gets a crystalline cue.
rep("memory",'            });requestApplyInsets();\n        }','            });requestApplyInsets();SoundFx.play(ctx,SoundFx.CORE);\n        }',"memory core entrance")
rep("memory",'for(int i=0;i<slots.length;i++)if(slots[i].contains(x,y)&&owned(i)){selected=i;invalidate();return true;}','for(int i=0;i<slots.length;i++)if(slots[i].contains(x,y)&&owned(i)){selected=i;SoundFx.play(ctx,SoundFx.MEMORY);invalidate();return true;}',"memory item cue")

# CHARACTER / WARDROBE: selections feel tactile, cloth sounds like cloth.
rep("character",'if(boy.contains(x,y)){selected=0;invalidate();return true;}if(girl.contains(x,y)){selected=1;invalidate();return true;}','if(boy.contains(x,y)){selected=0;SoundFx.play(ctx,SoundFx.UI);invalidate();return true;}if(girl.contains(x,y)){selected=1;SoundFx.play(ctx,SoundFx.UI);invalidate();return true;}',"character selection sound")
rep("character",'if(go.contains(x,y)&&selected>=0){prefs.edit().putInt("character_type",selected).putBoolean("character_selected",true).apply();ctx.startActivity(new Intent(ctx,MainActivity.class));','if(go.contains(x,y)&&selected>=0){SoundFx.play(ctx,SoundFx.CORE);prefs.edit().putInt("character_type",selected).putBoolean("character_selected",true).apply();ctx.startActivity(new Intent(ctx,MainActivity.class));',"character confirm sound")
rep("wardrobe",'if(boyBtn.contains(x,y)){character=0;prefs.edit().putInt("character_type",0).putBoolean("character_selected",true).apply();invalidate();return true;}','if(boyBtn.contains(x,y)){character=0;SoundFx.play(ctx,SoundFx.UI);prefs.edit().putInt("character_type",0).putBoolean("character_selected",true).apply();invalidate();return true;}',"wardrobe boy sound")
rep("wardrobe",'if(girlBtn.contains(x,y)){character=1;prefs.edit().putInt("character_type",1).putBoolean("character_selected",true).apply();invalidate();return true;}','if(girlBtn.contains(x,y)){character=1;SoundFx.play(ctx,SoundFx.UI);prefs.edit().putInt("character_type",1).putBoolean("character_selected",true).apply();invalidate();return true;}',"wardrobe girl sound")
rep("wardrobe",'if(unlocked(i)){outfit=i;prefs.edit().putInt("equipped_vyshyvanka_year",outfit).apply();invalidate();}','if(unlocked(i)){outfit=i;SoundFx.play(ctx,outfit==0?SoundFx.UI:SoundFx.CLOTH);prefs.edit().putInt("equipped_vyshyvanka_year",outfit).apply();invalidate();}',"wardrobe cloth sound")

# SCHOOL: bell, teacher mitten, right/wrong answers and transitions.
rep("school",'        void next(int s){stage=s;feedback="";prefs.edit().putInt("school_stage",s==TEACHER?GATE:Math.min(s,LESSON)).apply();buzz(24);invalidate();}','        void next(int s){stage=s;feedback="";prefs.edit().putInt("school_stage",s==TEACHER?GATE:Math.min(s,LESSON)).apply();buzz(24);SoundFx.play(ctx,s==TEACHER?SoundFx.MITTEN:(s==CLASS_DOOR?SoundFx.SCHOOL_BELL:SoundFx.UI));invalidate();}',"school transition sounds")
rep("school",'feedback="Пан Криж: «Дякую. Я вже думав, що вона розтанула разом із тією зимою.»";buzz(45);invalidate();','feedback="Пан Криж: «Дякую. Я вже думав, що вона розтанула разом із тією зимою.»";buzz(45);SoundFx.play(ctx,SoundFx.MITTEN);invalidate();',"teacher mitten return sound")
rep("school",'feedback="Трохи ближче до вільної руки пана Крижа.";buzz(12);invalidate();','feedback="Трохи ближче до вільної руки пана Крижа.";buzz(12);SoundFx.play(ctx,SoundFx.WRONG);invalidate();',"teacher mitten miss sound")
rep("school",'feedback="Це не 1-А. Подивись на табличку ще раз.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();','feedback="Це не 1-А. Подивись на табличку ще раз.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);SoundFx.play(ctx,SoundFx.WRONG);invalidate();',"school wrong door sound")
rep("school",'feedback="На цій парті немає знака живої сніжинки.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();','feedback="На цій парті немає знака живої сніжинки.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);SoundFx.play(ctx,SoundFx.WRONG);invalidate();',"school wrong desk sound")
rep("school",'feedback=i==0?"Лід теж тане. Спробуй ще.":"Пісок тут точно ні до чого.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();','feedback=i==0?"Лід теж тане. Спробуй ще.":"Пісок тут точно ні до чого.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);SoundFx.play(ctx,SoundFx.WRONG);invalidate();',"school wrong answer sound")
rep("school",'void completeLesson(){prefs.edit().putBoolean("school_first_day_complete",true).putInt("school_stage",DONE).apply();stage=DONE;feedback="";buzz(52);invalidate();}','void completeLesson(){prefs.edit().putBoolean("school_first_day_complete",true).putInt("school_stage",DONE).apply();stage=DONE;feedback="";buzz(52);SoundFx.play(ctx,SoundFx.CORRECT);SoundFx.play(ctx,SoundFx.COMPLETE);invalidate();}',"school lesson complete sound",False)

for key,path in paths.items():path.write_text(src[key],encoding="utf-8")
if len(hits)<25: raise SystemExit(f"v17.3 sound patch applied too few hooks: {len(hits)}")
print(f"Applied SnowmanGame v17.3 procedural soundscape: {len(hits)} hooks")
