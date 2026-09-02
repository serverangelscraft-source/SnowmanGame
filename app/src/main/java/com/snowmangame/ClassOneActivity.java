package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

/** Playable life inside class 1-A after the first school lesson. */
public class ClassOneActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(236,247,252));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        getSharedPreferences("snowman_game",MODE_PRIVATE).edit().putBoolean("school_class_one_started",true).apply();
        setContentView(new ClassOneView(this));
    }

    static class ClassOneView extends View {
        static final int HUB=0, BREAK=1, PRACTICE=2, YARD=3, TERM_END=4, THAW=5, REBORN=6, GRADE2=7;
        final Context ctx; final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF action=new RectF(),memoryBtn=new RectF(),wardrobeBtn=new RectF();
        final RectF[] cards={new RectF(),new RectF(),new RectF()};
        final RectF[] choices={new RectF(),new RectF(),new RectF()};
        final RectF[] targets={new RectF(),new RectF(),new RectF()};
        final float density,textScale; final Vibrator vibrator;
        float safeTop,safeBottom; int stage,mistakes,practiceStep,yardMask; String feedback="";
        boolean breakDone,practiceDone,yardDone;

        ClassOneView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            breakDone=prefs.getBoolean("class1_break_done",false);
            practiceDone=prefs.getBoolean("class1_practice_done",false);
            yardDone=prefs.getBoolean("class1_yard_done",false);
            practiceStep=Math.max(0,Math.min(3,prefs.getInt("class1_practice_step",0)));
            yardMask=prefs.getInt("class1_yard_mask",0)&7;
            mistakes=Math.max(0,prefs.getInt("school_mistakes",0));
            stage=Math.max(HUB,Math.min(GRADE2,prefs.getInt("class1_stage",HUB)));
            if(prefs.getInt("school_grade",1)>=2)stage=GRADE2;
            SnowmanStyle.ensureUnlocked(prefs,7);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);setContentDescription("Життя сніговика у першому класі 1-А");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){@Override public WindowInsets onApplyWindowInsets(View v,WindowInsets i){safeTop=i.getSystemWindowInsetTop();safeBottom=i.getSystemWindowInsetBottom();invalidate();return i;}});requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,70));else vibrator.vibrate(ms);}
        void setStage(int s){stage=s;feedback="";prefs.edit().putInt("class1_stage",s).apply();buzz(18);SoundFx.play(ctx,SoundFx.UI);invalidate();}
        boolean allDone(){return breakDone&&practiceDone&&yardDone;}
        int doneCount(){return (breakDone?1:0)+(practiceDone?1:0)+(yardDone?1:0);}
        String title(){switch(stage){case HUB:return"Життя у 1-А";case BREAK:return"Перша перерва";case PRACTICE:return"Урок-дослід";case YARD:return"Шкільне подвір’я";case TERM_END:return"Перша шкільна зима";case THAW:return"Весна прийшла";case REBORN:return"Перший сніг";default:return"2-А";}}
        String sub(){switch(stage){case HUB:return"Перший урок був лише початком. У класі тепер є своє життя.";case BREAK:return"У коридорі всі схожі. Знайди Сніжика за його синім шарфом.";case PRACTICE:return"Від тепла до морозу: склади правильний шлях води.";case YARD:return"Після уроків клас виходить надвір — влуч у три снігові мішені.";case TERM_END:return"1-А вже не просто табличка на дверях — це твій перший клас.";case THAW:return"Тіло тане, але жива сніжинка забирає шкільні спогади.";case REBORN:return"Повернувся сніг. Пам’ять уже знає дорогу до школи.";default:return"Наступна зима. Тепер ти вже не наймолодший у школі.";}}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);drawBackground(c);drawHeader(c);
            if(stage==HUB)drawHub(c);else if(stage==BREAK)drawBreak(c);else if(stage==PRACTICE)drawPractice(c);else if(stage==YARD)drawYard(c);else if(stage==TERM_END)drawTermEnd(c);else if(stage==THAW)drawThaw(c);else if(stage==REBORN)drawReborn(c);else drawGrade2(c);
        }

        void drawBackground(Canvas c){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            if(stage==YARD||stage==REBORN||stage==GRADE2){LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(160,218,246),Color.rgb(237,249,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(247,252,254));c.drawRect(0,bottom*.68f,w,h,p);}
            else if(stage==THAW){LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(202,226,237),Color.rgb(239,229,199),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(200,181,135));c.drawRect(0,bottom*.76f,w,h,p);}
            else{LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(241,235,213),Color.rgb(226,220,198),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(191,154,109));c.drawRect(0,bottom-dp(82),w,h,p);}
        }

        void drawHeader(Canvas c){
            float w=getWidth(),top=safeTop+dp(10);RectF r=new RectF(dp(14),top,w-dp(14),top+dp(108));p.setColor(Color.argb(247,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(102,129,143));c.drawText(stage==GRADE2?"ШКОЛА • НАСТУПНА ЗИМА • 2-А":"ШКОЛА • ТЕРНОПІЛЬЩИНА • 1-А",r.left+dp(17),r.top+dp(20),text);
            text.setTextSize(tx(18));text.setColor(Color.rgb(37,73,94));c.drawText(title(),r.left+dp(17),r.top+dp(51),text);
            String s=sub();text.setTextSize(tx(7.8f));while(text.measureText(s)>r.width()-dp(34)&&text.getTextSize()>tx(5.4f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(91,124,141));c.drawText(s,r.left+dp(17),r.top+dp(78),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(6.5f));text.setColor(Color.rgb(128,143,149));c.drawText(stage==HUB?"справи "+doneCount()+"/3":"помилки "+mistakes,r.right-dp(17),r.bottom-dp(13),text);
        }

        void drawHub(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF room=new RectF(dp(18),safeTop+dp(132),w-dp(18),bottom-dp(232));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(room,dp(25),dp(25),p);
            drawClassroom(c,room);
            float gap=dp(8),left=dp(18),right=w-dp(18),top=bottom-dp(220),cw=(right-left-gap*2)/3f;String[] names={"ПЕРЕРВА","УРОК-ДОСЛІД","ПОДВІР’Я"};String[] notes={"знайти Сніжика","сніг → вода → лід","3 снігові мішені"};boolean[] done={breakDone,practiceDone,yardDone};
            for(int i=0;i<3;i++){cards[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(88));p.setColor(done[i]?Color.rgb(229,244,237):Color.rgb(247,250,251));c.drawRoundRect(cards[i],dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.1f));text.setColor(done[i]?Color.rgb(55,126,101):Color.rgb(54,100,125));c.drawText(done[i]?"✓ "+names[i]:names[i],cards[i].centerX(),cards[i].top+dp(30),text);text.setTextSize(tx(5.7f));text.setColor(Color.rgb(115,135,142));c.drawText(notes[i],cards[i].centerX(),cards[i].bottom-dp(20),text);}
            if(allDone())button(c,"ЗАВЕРШИТИ ПЕРШУ ШКІЛЬНУ ЗИМУ");else{action.setEmpty();text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(107,129,138));c.drawText("Виконай усі три справи 1-А",w/2,bottom-dp(34),text);}
        }

        void drawClassroom(Canvas c,RectF r){
            float boardTop=r.top+dp(18);p.setColor(Color.rgb(65,101,84));c.drawRoundRect(new RectF(r.left+dp(18),boardTop,r.right-dp(18),boardTop+dp(58)),dp(8),dp(8),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(12));text.setColor(Color.WHITE);c.drawText("1-А • СЬОГОДНІ РАЗОМ",r.centerX(),boardTop+dp(36),text);
            for(int i=0;i<3;i++){p.setColor(Color.rgb(156,211,233));float x=r.left+dp(30)+i*(r.width()-dp(90))/2f;c.drawRoundRect(new RectF(x,r.top+dp(91),x+dp(38),r.top+dp(129)),dp(5),dp(5),p);}
            float ground=r.bottom-dp(22);drawHero(c,r.left+r.width()*.19f,ground,dp(25),.2f);drawFriend(c,r.left+r.width()*.40f,ground,dp(21),.2f);drawClassmate(c,r.left+r.width()*.60f,ground,dp(20),0);drawClassmate(c,r.left+r.width()*.76f,ground,dp(20),1);drawTeacher(c,r.left+r.width()*.90f,ground,dp(25));
        }

        void drawBreak(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF hall=new RectF(dp(20),safeTop+dp(140),w-dp(20),bottom-dp(95));p.setColor(Color.argb(243,255,255,255));c.drawRoundRect(hall,dp(25),dp(25),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(65,96,111));c.drawText(feedback.length()>0?feedback:(breakDone?"Сніжик знайшовся. Перерва врятована.":"Хто з них Сніжик?"),hall.centerX(),hall.top+dp(36),text);
            float left=hall.left+dp(18),right=hall.right-dp(18),gap=dp(10),top=hall.top+dp(76),cw=(right-left-gap*2)/3f;
            for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,hall.bottom-dp(28));p.setColor(i==1?Color.rgb(239,249,253):Color.rgb(248,249,247));c.drawRoundRect(choices[i],dp(18),dp(18),p);drawSmallStudent(c,choices[i].centerX(),choices[i].bottom-dp(32),dp(25),i==1,i);}
            if(breakDone)button(c,"НАЗАД ДО КЛАСУ");else action.setEmpty();
        }

        void drawPractice(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF board=new RectF(dp(20),safeTop+dp(140),w-dp(20),safeTop+dp(270));p.setColor(Color.rgb(65,101,84));c.drawRoundRect(board,dp(18),dp(18),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(218,239,226));c.drawText("ПАН КРИЖ: «СПОЧАТКУ ТЕПЛО, ПОТІМ МОРОЗ.»",board.centerX(),board.top+dp(28),text);text.setTextSize(tx(12));text.setColor(Color.WHITE);c.drawText("СНІГ → ВОДА → ЛІД",board.centerX(),board.top+dp(65),text);text.setTextSize(tx(7));c.drawText(practiceDone?"Дослід завершено.":"Торкайся карток у правильній послідовності.",board.centerX(),board.bottom-dp(22),text);
            float left=dp(22),right=w-dp(22),gap=dp(12),top=safeTop+dp(300),cw=(right-left-gap*2)/3f;String[] names={"СНІГ","ВОДА","ЛІД"};
            for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(165));boolean passed=i<practiceStep;p.setColor(passed?Color.rgb(229,244,237):Color.argb(245,255,255,255));c.drawRoundRect(choices[i],dp(20),dp(20),p);drawMatter(c,choices[i].centerX(),choices[i].top+dp(62),i,passed);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(passed?Color.rgb(55,126,101):Color.rgb(55,101,124));c.drawText((passed?"✓ ":"")+names[i],choices[i].centerX(),choices[i].bottom-dp(28),text);}
            text.setTextSize(tx(7.5f));text.setColor(Color.rgb(94,121,133));c.drawText(feedback,w/2,top+dp(193),text);
            if(practiceDone)button(c,"НАЗАД ДО КЛАСУ");else action.setEmpty();
        }

        void drawMatter(Canvas c,float x,float y,int type,boolean passed){
            if(type==0){p.setColor(Color.rgb(225,242,250));for(int i=0;i<5;i++){double a=i*1.2566;c.drawCircle(x+(float)Math.cos(a)*dp(18),y+(float)Math.sin(a)*dp(12),dp(8),p);}p.setColor(Color.WHITE);c.drawCircle(x,y,dp(14),p);}
            else if(type==1){Path drop=new Path();drop.moveTo(x,y-dp(24));drop.cubicTo(x-dp(22),y,x-dp(17),y+dp(24),x,y+dp(24));drop.cubicTo(x+dp(17),y+dp(24),x+dp(22),y,x,y-dp(24));p.setColor(Color.rgb(93,183,225));c.drawPath(drop,p);}
            else{p.setColor(Color.rgb(160,220,241));RectF r=new RectF(x-dp(24),y-dp(24),x+dp(24),y+dp(24));c.drawRoundRect(r,dp(5),dp(5),p);stroke.setColor(Color.argb(110,255,255,255));stroke.setStrokeWidth(dp(2));c.drawLine(r.left+dp(6),r.top+dp(12),r.right-dp(6),r.bottom-dp(10),stroke);}
            if(passed){stroke.setColor(Color.rgb(65,148,117));stroke.setStrokeWidth(dp(3));c.drawCircle(x,y,dp(31),stroke);}
        }

        void drawYard(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.72f;
            RectF info=new RectF(dp(20),safeTop+dp(140),w-dp(20),safeTop+dp(230));p.setColor(Color.argb(243,255,255,255));c.drawRoundRect(info,dp(22),dp(22),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.rgb(57,94,113));c.drawText(yardDone?"Три влучання. 1-А офіційно на подвір’ї.":"Торкнись трьох снігових мішеней",info.centerX(),info.top+dp(36),text);text.setTextSize(tx(7));text.setColor(Color.rgb(101,128,140));c.drawText("Сніжик рахує влучання: "+Integer.bitCount(yardMask)+"/3",info.centerX(),info.bottom-dp(20),text);
            float[] xs={w*.27f,w*.52f,w*.76f}, ys={safeTop+dp(320),safeTop+dp(385),safeTop+dp(312)};
            for(int i=0;i<3;i++){float r=dp(37);targets[i].set(xs[i]-r,ys[i]-r,xs[i]+r,ys[i]+r);boolean hit=(yardMask&(1<<i))!=0;p.setColor(hit?Color.rgb(222,239,234):Color.argb(238,255,255,255));c.drawCircle(xs[i],ys[i],r,p);stroke.setColor(hit?Color.rgb(65,148,117):Color.rgb(86,149,181));stroke.setStrokeWidth(dp(3));c.drawCircle(xs[i],ys[i],r-dp(5),stroke);c.drawCircle(xs[i],ys[i],r-dp(17),stroke);if(hit){text.setTextSize(tx(18));text.setColor(Color.rgb(65,148,117));c.drawText("✓",xs[i],ys[i]+dp(7),text);}}
            drawHero(c,w*.28f,ground,dp(31),.65f);drawFriend(c,w*.72f,ground,dp(27),.65f);
            if(yardDone)button(c,"НАЗАД ДО КЛАСУ");else action.setEmpty();
        }

        void drawTermEnd(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF card=new RectF(dp(20),safeTop+dp(140),w-dp(20),bottom-dp(90));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(110,132,138));c.drawText("1-А • ПЕРША ШКІЛЬНА ЗИМА",card.centerX(),card.top+dp(28),text);text.setTextSize(tx(20));text.setColor(Color.rgb(43,108,142));c.drawText("КЛАС СТАВ СВОЇМ",card.centerX(),card.top+dp(66),text);text.setTextSize(tx(8));text.setColor(Color.rgb(94,123,137));c.drawText("Перерва • дослід • подвір’я — усе залишиться у живій сніжинці.",card.centerX(),card.top+dp(95),text);
            float y=card.top+dp(137);String[] b={"✓ ПЕРЕРВА","✓ ДОСЛІД","✓ ПОДВІР’Я"};for(int i=0;i<3;i++){RectF r=new RectF(card.left+dp(24),y+i*dp(48),card.right-dp(24),y+i*dp(48)+dp(36));p.setColor(Color.rgb(230,245,238));c.drawRoundRect(r,dp(13),dp(13),p);text.setTextSize(tx(7.5f));text.setColor(Color.rgb(55,126,101));c.drawText(b[i],r.centerX(),r.centerY()+dp(3),text);}
            float ground=card.bottom-dp(38);drawHero(c,card.centerX()-dp(68),ground,dp(34),.7f);drawFriend(c,card.centerX(),ground,dp(28),.7f);drawTeacher(c,card.centerX()+dp(76),ground,dp(31));button(c,"ДО ВЕСНИ");
        }

        void drawThaw(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF card=new RectF(dp(22),safeTop+dp(145),w-dp(22),bottom-dp(95));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(20));text.setColor(Color.rgb(59,112,137));c.drawText("ТІЛО ТАНЕ",card.centerX(),card.top+dp(55),text);text.setTextSize(tx(8.5f));text.setColor(Color.rgb(101,124,133));c.drawText("Але цього разу сніжинка забирає ще й перший клас.",card.centerX(),card.top+dp(87),text);
            float py=card.top+card.height()*.62f;p.setColor(Color.argb(150,129,205,235));c.drawOval(new RectF(card.centerX()-dp(105),py-dp(20),card.centerX()+dp(105),py+dp(26)),p);drawCore(c,card.centerX(),py-dp(82),dp(28));
            text.setTextSize(tx(7.4f));text.setColor(Color.rgb(75,128,152));c.drawText("Жива сніжинка: 7 зим + перша школа",card.centerX(),py-dp(36),text);button(c,"ДОЧЕКАТИСЯ ПЕРШОГО СНІГУ");
        }

        void drawReborn(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF card=new RectF(dp(22),safeTop+dp(145),w-dp(22),bottom-dp(95));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(20));text.setColor(Color.rgb(43,108,142));c.drawText("ВІН ПОВЕРНУВСЯ",card.centerX(),card.top+dp(55),text);text.setTextSize(tx(8.4f));text.setColor(Color.rgb(96,125,139));c.drawText("Новий сніг. Та сама пам’ять. Наступний клас уже чекає.",card.centerX(),card.top+dp(88),text);
            float cx=card.centerX(),ground=card.bottom-dp(42);snow(c,cx,ground-dp(55),dp(58));snow(c,cx,ground-dp(135),dp(43));snow(c,cx,ground-dp(194),dp(31));drawCore(c,cx,ground-dp(135),dp(17));button(c,"ЗІБРАТИСЯ ЗНОВУ • ДО 2-А");
        }

        void drawGrade2(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            RectF card=new RectF(dp(20),safeTop+dp(140),w-dp(20),bottom-dp(185));p.setColor(Color.argb(243,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(108,132,140));c.drawText("НАСТУПНА ГЛАВА",card.centerX(),card.top+dp(27),text);text.setTextSize(tx(26));text.setColor(Color.rgb(42,108,146));c.drawText("2-А",card.centerX(),card.top+dp(72),text);text.setTextSize(tx(10));text.setColor(Color.rgb(70,104,121));c.drawText("Тепер ти вже не наймолодший.",card.centerX(),card.top+dp(105),text);
            RectF goal=new RectF(card.left+dp(24),card.top+dp(133),card.right-dp(24),card.top+dp(205));p.setColor(Color.rgb(235,247,251));c.drawRoundRect(goal,dp(18),dp(18),p);text.setTextSize(tx(7));text.setColor(Color.rgb(112,133,139));c.drawText("ПЕРША ЦІЛЬ 2-А",goal.centerX(),goal.top+dp(23),text);text.setTextSize(tx(9));text.setColor(Color.rgb(53,101,128));c.drawText("Допомогти новачку знайти свою парту",goal.centerX(),goal.top+dp(50),text);
            float ground=card.bottom-dp(32);drawHero(c,card.centerX()-dp(50),ground,dp(36),.45f);drawFriend(c,card.centerX()+dp(53),ground,dp(29),.45f);
            float gap=dp(8),half=(w-dp(48)-gap)/2f;memoryBtn.set(dp(20),bottom-dp(174),dp(20)+half,bottom-dp(121));wardrobeBtn.set(memoryBtn.right+gap,bottom-dp(174),w-dp(20),bottom-dp(121));p.setColor(Color.rgb(232,242,237));c.drawRoundRect(memoryBtn,dp(17),dp(17),p);p.setColor(Color.rgb(234,242,247));c.drawRoundRect(wardrobeBtn,dp(17),dp(17),p);text.setTextSize(tx(8));text.setColor(Color.rgb(58,106,91));c.drawText("СПОГАДИ",memoryBtn.centerX(),memoryBtn.centerY()+dp(3),text);text.setColor(Color.rgb(61,103,127));c.drawText("ГАРДЕРОБ",wardrobeBtn.centerX(),wardrobeBtn.centerY()+dp(3),text);
            action.set(dp(22),bottom-dp(66),w-dp(22),bottom-dp(11));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextSize(tx(8));text.setColor(Color.WHITE);c.drawText("2-А • НАСТУПНИЙ РОЗДІЛ",action.centerX(),action.centerY()+dp(3),text);
        }

        void button(Canvas c,String label){float w=getWidth(),bottom=getHeight()-safeBottom;action.set(dp(22),bottom-dp(66),w-dp(22),bottom-dp(11));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.2f));while(text.measureText(label)>action.width()-dp(28)&&text.getTextSize()>tx(6.3f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.WHITE);c.drawText(label,action.centerX(),action.centerY()+dp(4),text);}

        void drawSmallStudent(Canvas c,float x,float ground,float r,boolean blue,int variant){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(48,65,74));c.drawCircle(x-hr*.27f,hy-hr*.13f,hr*.08f,p);c.drawCircle(x+hr*.27f,hy-hr*.13f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.65f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);if(blue){p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-mr*.64f,my-mr*.72f,x+mr*.64f,my-mr*.52f),dp(4),dp(4),p);}else if(variant==0){p.setColor(Color.rgb(210,105,118));c.drawCircle(x+hr*.55f,hy-hr*.55f,dp(5),p);}else{p.setColor(Color.rgb(81,116,75));c.drawRoundRect(new RectF(x-hr*.55f,hy-hr*.75f,x+hr*.55f,hy-hr*.55f),dp(4),dp(4),p);}}
        void drawHero(Canvas c,float x,float ground,float r,float wave){drawPerson(c,x,ground,r,wave,true);}
        void drawFriend(Canvas c,float x,float ground,float r,float wave){drawPerson(c,x,ground,r,wave,false);}
        void drawClassmate(Canvas c,float x,float ground,float r,int variant){drawSmallStudent(c,x,ground,r,false,variant);}
        void drawPerson(Canvas c,float x,float ground,float r,float wave,boolean hero){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(44,59,68));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.68f,hy+hr*.07f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(105,78,57));stroke.setStrokeWidth(Math.max(dp(2.5f),r*.075f));float lx=x-mr*.60f,ly=my,lxe=x-mr*1.38f,lye=my-mr*(.28f+.15f*wave),rx=x+mr*.60f,ry=my,rxe=x+mr*(1.38f+wave*.12f),rye=my-mr*(.28f+.18f*wave);c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);if(hero){int o=SnowmanStyle.outfit(prefs,7);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.25f,my-mr*.24f,mr*.36f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*(1.25f+wave*.08f),my-mr*(.24f+.12f*wave),mr*.36f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);}else{p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-mr*.62f,my-mr*.70f,x+mr*.62f,my-mr*.52f),dp(4),dp(4),p);}}
        void drawTeacher(Canvas c,float x,float ground,float r){
            float br=r,mr=r*.74f,hr=r*.55f,by=ground-br,my=by-(br+mr)*.82f,hy=my-(mr+hr)*.82f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(45,59,68));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.07f,p);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.07f,p);stroke.setColor(Color.rgb(52,67,76));stroke.setStrokeWidth(dp(1.6f));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawLine(x-hr*.09f,hy-hr*.12f,x+hr*.09f,hy-hr*.12f,stroke);p.setColor(Color.rgb(64,83,94));c.drawRoundRect(new RectF(x-mr*.52f,my-mr*.52f,x+mr*.52f,my+mr*.42f),dp(7),dp(7),p);RectF collar=new RectF(x-mr*.41f,my-mr*.59f,x+mr*.41f,my-mr*.47f);SnowmanStyle.drawPatternBand(c,p,density,collar,7);}
        void drawCore(Canvas c,float x,float y,float r){p.setColor(Color.argb(40,71,163,214));c.drawCircle(x,y,r*1.55f,p);stroke.setColor(Color.rgb(62,153,206));stroke.setStrokeWidth(dp(2));for(int i=0;i<6;i++){double a=i*Math.PI/3;c.drawLine(x,y,x+(float)Math.cos(a)*r,y+(float)Math.sin(a)*r,stroke);}p.setColor(Color.WHITE);c.drawCircle(x,y,dp(3),p);}
        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.30f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);stroke.setColor(Color.argb(65,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);}

        void countTermOnce(){if(prefs.getBoolean("class1_term_counted",false))return;int days=Math.max(1,prefs.getInt("school_days",1))+3;prefs.edit().putBoolean("class1_term_counted",true).putInt("school_days",days).apply();}
        void wrong(String s){mistakes++;feedback=s;prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(10);SoundFx.play(ctx,SoundFx.WRONG);invalidate();}
        void correct(String s){feedback=s;buzz(24);SoundFx.play(ctx,SoundFx.CORRECT);invalidate();}

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();
            if(stage==HUB){
                for(int i=0;i<3;i++)if(cards[i].contains(x,y)){setStage(i==0?BREAK:(i==1?PRACTICE:YARD));return true;}
                if(allDone()&&action.contains(x,y)){countTermOnce();prefs.edit().putBoolean("class1_winter_complete",true).apply();SoundFx.play(ctx,SoundFx.COMPLETE);setStage(TERM_END);return true;}
            }else if(stage==BREAK){
                if(!breakDone){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==1){breakDone=true;prefs.edit().putBoolean("class1_break_done",true).apply();correct("Синій шарф — це Сніжик. Знайшовся!");}else wrong("Не він. У Сніжика синій шарф.");return true;}}
                if(breakDone&&action.contains(x,y)){setStage(HUB);return true;}
            }else if(stage==PRACTICE){
                if(!practiceDone){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==practiceStep){practiceStep++;prefs.edit().putInt("class1_practice_step",practiceStep).apply();if(practiceStep>=3){practiceDone=true;prefs.edit().putBoolean("class1_practice_done",true).apply();correct("Сніг → вода → лід. Пан Криж киває.");}else correct("Правильно. Далі.");}else wrong("Не цей стан. Починай зліва за процесом.");return true;}}
                if(practiceDone&&action.contains(x,y)){setStage(HUB);return true;}
            }else if(stage==YARD){
                if(!yardDone){for(int i=0;i<3;i++)if(targets[i].contains(x,y)&&((yardMask&(1<<i))==0)){yardMask|=1<<i;prefs.edit().putInt("class1_yard_mask",yardMask).apply();SoundFx.play(ctx,SoundFx.HIT);buzz(14);if(yardMask==7){yardDone=true;prefs.edit().putBoolean("class1_yard_done",true).apply();correct("Три з трьох. Сніжик уже просить ще раунд.");}else invalidate();return true;}}
                if(yardDone&&action.contains(x,y)){setStage(HUB);return true;}
            }else if(stage==TERM_END&&action.contains(x,y)){SoundFx.play(ctx,SoundFx.MELT);setStage(THAW);return true;}
            else if(stage==THAW&&action.contains(x,y)){SoundFx.play(ctx,SoundFx.CORE);setStage(REBORN);return true;}
            else if(stage==REBORN&&action.contains(x,y)){prefs.edit().putBoolean("school_class_one_complete",true).putInt("school_grade",2).putInt("school_winter",8).putInt("class1_stage",GRADE2).apply();SoundFx.play(ctx,SoundFx.SNOW_READY);stage=GRADE2;invalidate();return true;}
            else if(stage==GRADE2){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}if(action.contains(x,y)){ctx.startActivity(new Intent(ctx,SchoolWeekActivity.class));return true;}}
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
