from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolWeekActivity.java")
gradle_path=Path("app/build.gradle")
s=school_path.read_text(encoding="utf-8")

def rep(old,new,label):
    global s
    if old not in s:
        raise SystemExit(f"v18.2 school pacing patch failed: {label}")
    s=s.replace(old,new,1)

# Preserve the old numeric values for PLAY/DINNER/DONE so saves that already
# completed today's lesson or dinner are not pushed backwards. New stages use
# new values and are only introduced for unfinished/new school days.
rep(
    '        static final int PLAY=0, DINNER=1, DONE=2;',
    '        static final int PLAY=0, DINNER=1, DONE=2, BREAK=3, SECOND=4, HOME=5, MORNING=6;',
    'stage constants',
)

old_clock='''            long stageDay=prefs.getLong("school_clock_stage_day",Long.MIN_VALUE);\n            if(stageDay!=effectiveDay){stage=PLAY;feedback="";weekendChoice="";}else stage=Math.max(PLAY,Math.min(DONE,prefs.getInt("school_clock_stage",PLAY)));\n            if(prefs.getLong("school_clock_last_completed_day",Long.MIN_VALUE)==effectiveDay)stage=DONE;'''
new_clock='''            long stageDay=prefs.getLong("school_clock_stage_day",Long.MIN_VALUE);\n            if(stageDay!=effectiveDay){stage=schoolDay()?MORNING:PLAY;feedback="";weekendChoice="";}else{stage=prefs.getInt("school_clock_stage",schoolDay()?MORNING:PLAY);if(stage<PLAY||stage>MORNING)stage=schoolDay()?MORNING:PLAY;}\n            if(!prefs.getBoolean("school_pacing_v18_2",false)){if(schoolDay()&&stage==PLAY)stage=MORNING;prefs.edit().putBoolean("school_pacing_v18_2",true).putInt("school_clock_stage",stage).apply();}\n            if(prefs.getLong("school_clock_last_completed_day",Long.MIN_VALUE)==effectiveDay)stage=DONE;'''
rep(old_clock,new_clock,'clock migration')

old_helpers='''        int correctIndex(){switch(weekDay){case 2:case 3:return 1;default:return 0;}}'''
new_helpers='''        int correctIndex(){switch(weekDay){case 2:case 3:return 1;default:return 0;}}\n        String secondLessonTitle(){switch(weekDay){case 1:return"Класна розмова";case 2:return"Ще одна задача";case 3:return"Холод і тепло";case 4:return"Командна робота";default:return"Порядок у класі";}}\n        String secondQuestion(){switch(weekDay){case 1:return"Що робимо, коли однокласник говорить?";case 2:return"4 сніжки + 3 сніжки. Скільки разом?";case 3:return"Що допомагає снігу не танути?";case 4:return"Як краще виконувати спільну справу?";default:return"Що робимо перед виходом з класу?";}}\n        String[] secondOptions(){switch(weekDay){case 1:return new String[]{"СЛУХАЄМО","ПЕРЕБИВАЄМО","ЙДЕМО ГЕТЬ"};case 2:return new String[]{"6","7","8"};case 3:return new String[]{"ХОЛОД","СОНЦЕ","БАТАРЕЯ"};case 4:return new String[]{"РАЗОМ","КОЖЕН САМ","СВАРИМОСЯ"};default:return new String[]{"ПРИБИРАЄМО","РОЗКИДАЄМО","ТІКАЄМО"};}}\n        int secondCorrectIndex(){return weekDay==2?1:0;}'''
rep(old_helpers,new_helpers,'second lesson helpers')

rep(
    '        @Override protected void onDraw(Canvas c){super.onDraw(c);drawBackground(c);drawHeader(c);if(yearIntro){drawYearIntro(c);return;}if(stage==DONE)drawDone(c);else if(!schoolDay())drawWeekend(c);else if(stage==DINNER)drawDinner(c);else drawLesson(c);}',
    '        @Override protected void onDraw(Canvas c){super.onDraw(c);drawBackground(c);drawHeader(c);if(yearIntro){drawYearIntro(c);return;}if(stage==DONE)drawDone(c);else if(!schoolDay())drawWeekend(c);else if(stage==DINNER)drawDinner(c);else if(stage==HOME)drawHome(c);else if(stage==SECOND)drawSecondLesson(c);else if(stage==BREAK)drawBreak(c);else if(stage==MORNING)drawMorning(c);else drawLesson(c);}',
    'draw routing',
)

# Make the existing lesson explicitly the second part of a six-part day.
rep(
    'c.drawText(className()+" • "+lessonTitle().toUpperCase(),card.centerX(),card.top+dp(28),text);',
    'c.drawText("ЕТАП 2/6 • "+className()+" • "+lessonTitle().toUpperCase(),card.centerX(),card.top+dp(28),text);',
    'lesson progress label',
)
rep(
    'c.drawText(feedback.length()>0?feedback:"Одна правильна дія — і навчальний день іде далі.",card.centerX(),card.top+dp(96),text);',
    'c.drawText(feedback.length()>0?feedback:"Перший урок — це лише частина сьогоднішнього дня.",card.centerX(),card.top+dp(96),text);',
    'lesson pacing copy',
)

extra_methods='''        void drawMorning(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(88));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(111,132,139));c.drawText("ЕТАП 1/6 • РАНОК ПЕРЕД ШКОЛОЮ",card.centerX(),card.top+dp(28),text);text.setTextSize(tx(15));text.setColor(Color.rgb(48,82,99));c.drawText("Збираємося до "+className(),card.centerX(),card.top+dp(67),text);text.setTextSize(tx(7.4f));text.setColor(feedback.length()>0?Color.rgb(164,97,78):Color.rgb(102,127,138));c.drawText(feedback.length()>0?feedback:"Що точно треба покласти в рюкзак?",card.centerX(),card.top+dp(96),text);String[] o={"ЗОШИТ","КАСТРУЛЯ","ПОДУШКА"};float left=card.left+dp(18),right=card.right-dp(18),gap=dp(10),top=card.top+dp(126),cw=(right-left-gap*2)/3f;for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(105));p.setColor(Color.rgb(239,247,250));c.drawRoundRect(choices[i],dp(18),dp(18),p);text.setTextSize(tx(7.1f));text.setColor(Color.rgb(52,101,127));c.drawText(o[i],choices[i].centerX(),choices[i].centerY()+dp(3),text);}drawHero(c,card.centerX()-dp(38),card.bottom-dp(38),dp(34));drawFriend(c,card.centerX()+dp(50),card.bottom-dp(38),dp(27));action.setEmpty();}\n\n        void drawBreak(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(88));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(111,132,139));c.drawText("ЕТАП 3/6 • ПЕРЕРВА",card.centerX(),card.top+dp(28),text);text.setTextSize(tx(16));text.setColor(Color.rgb(48,82,99));c.drawText("Дзвінок. Є трохи часу для себе.",card.centerX(),card.top+dp(67),text);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(102,127,138));c.drawText("Обери, як провести перерву зі Сніжиком. Тут немає неправильної відповіді.",card.centerX(),card.top+dp(96),text);String[] o={"СНІЖКИ","ПОГОВОРИТИ","ДОПОМОГТИ"};float left=card.left+dp(18),right=card.right-dp(18),gap=dp(10),top=card.top+dp(126),cw=(right-left-gap*2)/3f;for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(112));p.setColor(Color.rgb(239,247,250));c.drawRoundRect(choices[i],dp(18),dp(18),p);text.setTextSize(tx(7));text.setColor(Color.rgb(52,101,127));c.drawText(o[i],choices[i].centerX(),choices[i].centerY()+dp(3),text);}drawHero(c,card.centerX()-dp(45),card.bottom-dp(37),dp(33));drawFriend(c,card.centerX()+dp(47),card.bottom-dp(37),dp(29));action.setEmpty();}\n\n        void drawSecondLesson(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(88));p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(111,132,139));c.drawText("ЕТАП 4/6 • "+secondLessonTitle().toUpperCase(),card.centerX(),card.top+dp(28),text);text.setTextSize(tx(11.5f));text.setColor(Color.rgb(48,82,99));String q=secondQuestion();while(text.measureText(q)>card.width()-dp(34)&&text.getTextSize()>tx(8f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(q,card.centerX(),card.top+dp(67),text);text.setTextSize(tx(7));text.setColor(feedback.length()>0?Color.rgb(164,97,78):Color.rgb(102,127,138));c.drawText(feedback.length()>0?feedback:"Після перерви — ще один короткий урок.",card.centerX(),card.top+dp(96),text);String[] o=secondOptions();float left=card.left+dp(18),right=card.right-dp(18),gap=dp(10),top=card.top+dp(126),cw=(right-left-gap*2)/3f;for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(112));p.setColor(Color.rgb(239,247,250));c.drawRoundRect(choices[i],dp(18),dp(18),p);text.setTextSize(tx(6.8f));while(text.measureText(o[i])>cw-dp(10)&&text.getTextSize()>tx(5.3f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(Color.rgb(52,101,127));c.drawText(o[i],choices[i].centerX(),choices[i].centerY()+dp(3),text);}drawTeacher(c,card.left+card.width()*.25f,card.bottom-dp(35),dp(31));drawHero(c,card.left+card.width()*.55f,card.bottom-dp(35),dp(32));drawFriend(c,card.left+card.width()*.78f,card.bottom-dp(35),dp(26));action.setEmpty();}\n\n        void drawHome(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(22),safeTop+dp(145),w-dp(22),bottom-dp(90));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(113,134,141));c.drawText("ЕТАП 5/6 • ПІСЛЯ УРОКІВ",card.centerX(),card.top+dp(29),text);text.setTextSize(tx(19));text.setColor(Color.rgb(48,82,99));c.drawText("Час повертатися додому",card.centerX(),card.top+dp(70),text);text.setTextSize(tx(7.6f));text.setColor(Color.rgb(99,124,134));c.drawText("Школа закінчилась. Ідемо зі Сніжиком додому, а вже потім вечеря.",card.centerX(),card.top+dp(101),text);stroke.setStrokeWidth(dp(4));stroke.setColor(Color.rgb(205,221,228));c.drawLine(card.left+dp(45),card.top+dp(165),card.right-dp(45),card.top+dp(165),stroke);p.setColor(Color.rgb(252,190,24));c.drawCircle(card.centerX(),card.top+dp(165),dp(7),p);drawHero(c,card.centerX()-dp(50),card.bottom-dp(48),dp(36));drawFriend(c,card.centerX()+dp(48),card.bottom-dp(48),dp(29));button(c,"ПОВЕРНУТИСЯ ДОДОМУ");}\n\n'''
anchor='        void drawDinner(Canvas c){'
if anchor not in s:
    raise SystemExit('v18.2 school pacing patch failed: dinner anchor')
s=s.replace(anchor,extra_methods+anchor,1)

rep(
    'c.drawText("ПІСЛЯ ШКОЛИ • ВЕЧЕРЯ "+weekDay+"/5",card.centerX(),card.top+dp(29),text);',
    'c.drawText("ЕТАП 6/6 • ВЕЧЕРЯ "+weekDay+"/5",card.centerX(),card.top+dp(29),text);',
    'dinner progress label',
)
rep(
    'String detail=schoolDay()?"Вечеря: "+DISHES[weekDay-1]+".":(weekendChoice.length()>0?weekendChoice+".":"Вихідний прожито.");',
    'String detail=schoolDay()?"6 етапів дня • вечеря: "+DISHES[weekDay-1]+".":(weekendChoice.length()>0?weekendChoice+".":"Вихідний прожито.");',
    'done day summary',
)

old_touch='''        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(yearIntro){if(action.contains(x,y)){prefs.edit().putInt("school_clock_announced_grade",grade).apply();yearIntro=false;SoundFx.play(ctx,SoundFx.SCHOOL_BELL);invalidate();}return true;}if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}return true;}if(!schoolDay()){String[] o=weekDay==6?new String[]{"Санчата","Прогулянка","Відпочинок"}:new String[]{"Спогади","Зібрані речі","Час зі Сніжиком"};for(int i=0;i<3;i++)if(choices[i].contains(x,y)){finishDay(o[i]);return true;}return true;}if(stage==PLAY){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==correctIndex()){feedback="Правильно.";SoundFx.play(ctx,SoundFx.CORRECT);buzz(20);setStage(DINNER);}else wrong("Не ця дія. Спробуй ще раз.");return true;}}else if(stage==DINNER&&action.contains(x,y)){finishDay("");return true;}return true;}'''
new_touch='''        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(yearIntro){if(action.contains(x,y)){prefs.edit().putInt("school_clock_announced_grade",grade).apply();yearIntro=false;SoundFx.play(ctx,SoundFx.SCHOOL_BELL);invalidate();}return true;}if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}return true;}if(!schoolDay()){String[] o=weekDay==6?new String[]{"Санчата","Прогулянка","Відпочинок"}:new String[]{"Спогади","Зібрані речі","Час зі Сніжиком"};for(int i=0;i<3;i++)if(choices[i].contains(x,y)){finishDay(o[i]);return true;}return true;}if(stage==MORNING){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==0){prefs.edit().putString("school_morning_last","Зошит").apply();feedback="";SoundFx.play(ctx,SoundFx.CORRECT);setStage(PLAY);}else wrong("Це не те, що треба на урок. Спробуй ще.");return true;}}else if(stage==PLAY){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==correctIndex()){feedback="";SoundFx.play(ctx,SoundFx.CORRECT);buzz(20);setStage(BREAK);}else wrong("Не ця дія. Спробуй ще раз.");return true;}}else if(stage==BREAK){String[] o={"Сніжки","Поговорити","Допомогти"};for(int i=0;i<3;i++)if(choices[i].contains(x,y)){prefs.edit().putString("school_break_last",o[i]).apply();feedback="";SoundFx.play(ctx,SoundFx.UI);setStage(SECOND);return true;}}else if(stage==SECOND){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==secondCorrectIndex()){feedback="";SoundFx.play(ctx,SoundFx.CORRECT);buzz(20);setStage(HOME);}else wrong("Не зовсім. Подумай ще раз.");return true;}}else if(stage==HOME&&action.contains(x,y)){feedback="";setStage(DINNER);return true;}else if(stage==DINNER&&action.contains(x,y)){finishDay("");return true;}return true;}'''
rep(old_touch,new_touch,'touch flow')

school_path.write_text(s,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 35',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.2"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.2: six-stage school day pacing")
