package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SchoolWeekActivity extends Activity {
    private SchoolWeekView schoolView;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(238,247,250));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        schoolView=new SchoolWeekView(this);
        setContentView(schoolView);
        NotificationScheduler.onSchoolOpened(this);
    }

    @Override protected void onResume(){
        super.onResume();
        if(schoolView!=null)schoolView.refreshDateIfNeeded();
    }

    static class SchoolWeekView extends View {
        static final int MORNING=10,LESSON1=11,BREAK=12,LESSON2=13,HOME=14,DINNER=15;
        static final int WEEKEND=20,WEEKEND_MINI=21,DONE=30,YEAR_DONE=31,BONUS=40,INTRO=50;
        static final String[] DISHES={"БОРЩ","ВАРЕНИКИ З КАРТОПЛЕЮ","ГОЛУБЦІ","ДЕРУНИ","БАНОШ З БРИНЗОЮ"};
        static final String[] BAG={"ЗОШИТ","ОЛІВЕЦЬ","РУКАВИЧКИ","КАСТРУЛЯ","ПОДУШКА"};

        final Context ctx; final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF[] choices={new RectF(),new RectF(),new RectF()};
        final RectF[] bagRects={new RectF(),new RectF(),new RectF(),new RectF(),new RectF()};
        final RectF[] pathRects={new RectF(),new RectF(),new RectF(),new RectF()};
        final RectF action=new RectF(),memoryBtn=new RectF(),wardrobeBtn=new RectF(),target=new RectF(),dishRect=new RectF();
        final float density,textScale; final Vibrator vibrator;

        float safeTop,safeBottom;
        long today,effectiveDay;
        int calendarDow,grade,winter,stage,mistakes,schoolDone,weekendDone;
        int bagMask,miniHits,homeStep,dinnerBites;
        boolean yearIntro,yearComplete;
        String feedback="",weekendChoice="";

        SchoolWeekView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);
            setContentDescription("Шкільне життя сніговика: день зараховується лише після гри, максимум один за календарну дату");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets i){safeTop=i.getSystemWindowInsetTop();safeBottom=i.getSystemWindowInsetBottom();invalidate();return i;}
            });
            initState();requestApplyInsets();
        }

        void initState(){
            today=localDayNumber();
            long lastSeen=prefs.getLong("school_player_last_seen_day",prefs.getLong("school_clock_last_seen_day",today));
            effectiveDay=Math.max(today,lastSeen);
            if(today>lastSeen)lastSeen=today;
            calendarDow=dayOfWeek(today);
            grade=Math.max(2,Math.min(11,prefs.getInt("school_grade",2)));

            if(!prefs.getBoolean("school_playerpaced_v18_4",false)){
                int oldTotal=Math.max(0,prefs.getInt("school_clock_days_lived",0));
                int oldMeals=Math.max(0,prefs.getInt("school_meals_total",0));
                int inYear=oldTotal%7;
                int migratedSchool=Math.min(5,Math.min(inYear,oldMeals%5));
                if(oldTotal>0&&oldMeals>0&&migratedSchool==0&&inYear>0)migratedSchool=Math.min(5,Math.min(inYear,oldMeals));
                int migratedWeekend=Math.min(2,Math.max(0,inYear-migratedSchool));
                prefs.edit().putBoolean("school_playerpaced_v18_4",true).putInt("school_year_school_done",migratedSchool).putInt("school_year_weekend_done",migratedWeekend).putBoolean("school_year_complete",migratedSchool>=5&&migratedWeekend>=2).putLong("school_player_last_seen_day",lastSeen).apply();
            }

            if(prefs.getBoolean("school_year_complete",false) && !prefs.contains("school_year_complete_day")){
                prefs.edit().putLong("school_year_complete_day",effectiveDay).apply();
            }

            schoolDone=Math.max(0,Math.min(5,prefs.getInt("school_year_school_done",0)));
            weekendDone=Math.max(0,Math.min(2,prefs.getInt("school_year_weekend_done",0)));
            yearComplete=prefs.getBoolean("school_year_complete",schoolDone>=5&&weekendDone>=2);
            long completeDay=prefs.getLong("school_year_complete_day",Long.MIN_VALUE);

            if(yearComplete&&effectiveDay>completeDay&&grade<11){
                grade++;schoolDone=0;weekendDone=0;yearComplete=false;yearIntro=true;
                prefs.edit().putInt("school_grade",grade).putInt("school_year_school_done",0).putInt("school_year_weekend_done",0).putBoolean("school_year_complete",false).putBoolean("school_year_intro_pending",true).putInt("school_clock_announced_grade",grade-1).apply();
            }else yearIntro=prefs.getBoolean("school_year_intro_pending",false);

            winter=grade+6;
            long completedDay=prefs.getLong("school_player_last_completed_day",prefs.getLong("school_clock_last_completed_day",Long.MIN_VALUE));
            long stageDay=prefs.getLong("school_player_stage_day",Long.MIN_VALUE);
            if(yearComplete&&(completeDay==effectiveDay||grade>=11))stage=YEAR_DONE;
            else if(completedDay==effectiveDay)stage=DONE;
            else if(yearIntro)stage=INTRO;
            else if(stageDay==effectiveDay)stage=prefs.getInt("school_player_stage",defaultStage());
            else{stage=defaultStage();resetMiniForDate();}

            bagMask=prefs.getInt("school_player_bag_mask",0);miniHits=prefs.getInt("school_player_mini_hits",0);homeStep=prefs.getInt("school_player_home_step",0);dinnerBites=prefs.getInt("school_player_dinner_bites",0);weekendChoice=prefs.getString("school_player_weekend_choice","");mistakes=Math.max(0,prefs.getInt("school_mistakes",0));
            prefs.edit().putLong("school_player_last_seen_day",lastSeen).putLong("school_player_stage_day",effectiveDay).putInt("school_player_stage",stage).putInt("school_grade",grade).putInt("school_winter",winter).putBoolean("class2_started",true).apply();
        }

        void refreshDateIfNeeded(){
            long now=localDayNumber();
            if(now==today)return;
            initState();
            invalidate();
        }

        int defaultStage(){if(isWeekday())return schoolDone<5?MORNING:BONUS;return weekendDone<2?WEEKEND:BONUS;}
        void resetMiniForDate(){bagMask=0;miniHits=0;homeStep=0;dinnerBites=0;feedback="";weekendChoice="";prefs.edit().putInt("school_player_bag_mask",0).putInt("school_player_mini_hits",0).putInt("school_player_home_step",0).putInt("school_player_dinner_bites",0).putString("school_player_weekend_choice","").apply();}
        long localDayNumber(){Calendar local=Calendar.getInstance();int y=local.get(Calendar.YEAR),m=local.get(Calendar.MONTH),d=local.get(Calendar.DAY_OF_MONTH);GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));utc.clear();utc.set(y,m,d,0,0,0);return utc.getTimeInMillis()/86400000L;}
        int dayOfWeek(long dayNumber){GregorianCalendar g=new GregorianCalendar(TimeZone.getTimeZone("UTC"));g.setTimeInMillis(dayNumber*86400000L);return g.get(Calendar.DAY_OF_WEEK);}
        boolean isWeekday(){return calendarDow>=Calendar.MONDAY&&calendarDow<=Calendar.FRIDAY;}
        String dayName(){switch(calendarDow){case Calendar.MONDAY:return"ПОНЕДІЛОК";case Calendar.TUESDAY:return"ВІВТОРОК";case Calendar.WEDNESDAY:return"СЕРЕДА";case Calendar.THURSDAY:return"ЧЕТВЕР";case Calendar.FRIDAY:return"П’ЯТНИЦЯ";case Calendar.SATURDAY:return"СУБОТА";default:return"НЕДІЛЯ";}}
        float dp(float v){return v*density;}float tx(float v){return v*textScale;}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,65));else vibrator.vibrate(ms);}
        int totalDone(){return schoolDone+weekendDone;}int currentNumber(){return Math.min(7,totalDone()+((stage==DONE||stage==YEAR_DONE)?0:1));}int schoolOrdinal(){return Math.min(5,schoolDone+1);}String className(){return grade+"-А";}
        String theme(){switch(grade){case 2:return"Дружба";case 3:return"Команда";case 4:return"Самостійність";case 5:return"Допомога молодшим";case 6:return"Відповідальність";default:return"Шкільне життя";}}

        @Override protected void onDraw(Canvas c){super.onDraw(c);drawBackground(c);drawHeader(c);if(stage==INTRO){drawIntro(c);return;}if(stage==YEAR_DONE){drawYearDone(c);return;}if(stage==DONE){drawDone(c);return;}if(stage==BONUS){drawBonus(c);return;}if(stage==WEEKEND){drawWeekendChoice(c);return;}if(stage==WEEKEND_MINI){drawWeekendMini(c);return;}if(stage==MORNING){drawMorning(c);return;}if(stage==LESSON1){drawLesson(c,false);return;}if(stage==BREAK){drawBreak(c);return;}if(stage==LESSON2){drawLesson(c,true);return;}if(stage==HOME){drawHome(c);return;}drawDinner(c);}

        void drawBackground(Canvas c){float w=getWidth(),h=getHeight(),bottom=h-safeBottom;if(isWeekday()){LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(242,236,216),Color.rgb(229,224,203),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(193,158,114));c.drawRect(0,bottom-dp(78),w,h,p);}else{LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(164,220,246),Color.rgb(238,249,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(246,251,253));c.drawRect(0,bottom*.67f,w,h,p);}}
        void drawHeader(Canvas c){float w=getWidth(),top=safeTop+dp(10);RectF r=new RectF(dp(14),top,w-dp(14),top+dp(124));p.setColor(Color.argb(247,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7));text.setColor(Color.rgb(101,128,142));c.drawText("ШКОЛА • ЗИМА "+winter+" • "+className(),r.left+dp(17),r.top+dp(20),text);text.setTextSize(tx(17));text.setColor(Color.rgb(38,73,94));String title=(stage==BONUS?"Бонусний день":("День "+Math.max(1,currentNumber())+"/7"))+" • "+dayName();c.drawText(title,r.left+dp(17),r.top+dp(51),text);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(91,123,139));c.drawText("Прогрес року: навчання "+schoolDone+"/5 • вихідні "+weekendDone+"/2",r.left+dp(17),r.top+dp(78),text);text.setTextSize(tx(6.5f));text.setColor(Color.rgb(119,139,147));c.drawText("Пропущений день не рахується • за одну дату максимум 1 день життя",r.left+dp(17),r.top+dp(101),text);}
        RectF card(){float bottom=getHeight()-safeBottom;return new RectF(dp(20),safeTop+dp(155),getWidth()-dp(20),bottom-dp(88));}
        void cardBase(Canvas c,RectF r){p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(r,dp(27),dp(27),p);}
        void centerText(Canvas c,String s,float y,float size,int color){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(size));text.setColor(color);while(text.measureText(s)>getWidth()-dp(70)&&text.getTextSize()>tx(5.2f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(s,getWidth()/2f,y,text);}
        void centerTextAt(Canvas c,String s,float x,float y,float size,int color){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(size));text.setColor(color);c.drawText(s,x,y,text);}

        void drawIntro(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"НОВА ЗИМА",r.top+dp(32),7,Color.rgb(111,132,139));centerText(c,className(),r.top+dp(82),25,Color.rgb(42,106,141));centerText(c,"Тема року: «"+theme()+"»",r.top+dp(117),9,Color.rgb(82,117,133));centerText(c,"Рік пройде тільки після 5 навчальних і 2 вихідних, які ти реально проживеш.",r.top+dp(151),7.2f,Color.rgb(99,124,134));centerText(c,"Якщо не зайдеш завтра — нічого не втратиться.",r.top+dp(175),7.2f,Color.rgb(99,124,134));drawHero(c,r.centerX(),r.bottom-dp(55),dp(46));button(c,"ПОЧАТИ ЦЕЙ РІК");}

        void drawMorning(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"ЕТАП 1/6 • РАНОК",r.top+dp(28),7,Color.rgb(111,132,139));centerText(c,"Збери рюкзак до "+className(),r.top+dp(64),15,Color.rgb(48,82,99));centerText(c,"Знайди 3 речі, які справді потрібні до школи.",r.top+dp(92),7.2f,Color.rgb(102,127,138));float left=r.left+dp(18),right=r.right-dp(18),gap=dp(8),top=r.top+dp(122),cw=(right-left-gap)/2f,ch=dp(72);for(int i=0;i<5;i++){int row=i/2,col=i%2;float xx=left+col*(cw+gap),yy=top+row*(ch+gap);if(i==4)xx=r.centerX()-cw/2f;bagRects[i].set(xx,yy,xx+cw,yy+ch);boolean chosen=(bagMask&(1<<i))!=0;p.setColor(chosen?Color.rgb(221,243,232):Color.rgb(239,247,250));c.drawRoundRect(bagRects[i],dp(16),dp(16),p);centerTextAt(c,BAG[i],bagRects[i].centerX(),bagRects[i].centerY()+dp(3),6.6f,chosen?Color.rgb(52,126,96):Color.rgb(52,101,127));}centerText(c,"Зібрано "+Integer.bitCount(bagMask&7)+"/3",r.bottom-dp(110),7,Color.rgb(89,122,137));if((bagMask&7)==7)button(c,"ДО ШКОЛИ");else action.setEmpty();}

        String lessonTitle(boolean second){int o=schoolOrdinal();if(!second){switch(o){case 1:return"Ранкова зустріч";case 2:return"Математика зі сніжками";case 3:return"Урок про тепло";case 4:return"Допомога Іскрику";default:return"П'ятнична справа класу";}}switch(o){case 1:return"Класна розмова";case 2:return"Ще одна задача";case 3:return"Холод і тепло";case 4:return"Командна робота";default:return"Порядок у класі";}}
        String question(boolean second){int o=schoolOrdinal();if(!second){switch(o){case 1:return"З чого нормально почати шкільний день?";case 2:return"2 сніжки + 3 сніжки. Скільки разом?";case 3:return"Що станеться зі снігом у теплі?";case 4:return"Іскрик забув олівець. Що робимо?";default:return"У класі лишились папірці. Що робимо?";}}switch(o){case 1:return"Що робимо, коли однокласник говорить?";case 2:return"4 сніжки + 3 сніжки. Скільки разом?";case 3:return"Що допомагає снігу не танути?";case 4:return"Як краще виконувати спільну справу?";default:return"Що робимо перед виходом з класу?";}}
        String[] options(boolean second){int o=schoolOrdinal();if(!second){switch(o){case 1:return new String[]{"ПРИВІТАТИСЯ","СХОВАТИСЯ","ПІТИ ДОДОМУ"};case 2:return new String[]{"4","5","6"};case 3:return new String[]{"ЛІД","ВОДА","ПІСОК"};case 4:return new String[]{"ПОДІЛИТИСЯ","СМІЯТИСЯ","СХОВАТИ"};default:return new String[]{"ПРИБРАТИ","ЗАЛИШИТИ","РОЗКИДАТИ"};}}switch(o){case 1:return new String[]{"СЛУХАЄМО","ПЕРЕБИВАЄМО","ЙДЕМО"};case 2:return new String[]{"6","7","8"};case 3:return new String[]{"ХОЛОД","СОНЦЕ","БАТАРЕЯ"};case 4:return new String[]{"РАЗОМ","КОЖЕН САМ","СВАРИМОСЯ"};default:return new String[]{"ПРИБИРАЄМО","РОЗКИДАЄМО","ТІКАЄМО"};}}
        int correct(boolean second){int o=schoolOrdinal();if(!second)return(o==2||o==3)?1:0;return o==2?1:0;}
        void drawLesson(Canvas c,boolean second){RectF r=card();cardBase(c,r);centerText(c,"ЕТАП "+(second?"4":"2")+"/6 • "+lessonTitle(second).toUpperCase(),r.top+dp(28),7,Color.rgb(111,132,139));centerText(c,question(second),r.top+dp(68),11.2f,Color.rgb(48,82,99));centerText(c,feedback.length()>0?feedback:(second?"Другий короткий урок.":"Перший урок сьогодні."),r.top+dp(98),7,feedback.length()>0?Color.rgb(164,97,78):Color.rgb(102,127,138));String[] o=options(second);float left=r.left+dp(18),right=r.right-dp(18),gap=dp(10),top=r.top+dp(128),cw=(right-left-gap*2)/3f;for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(112));p.setColor(Color.rgb(239,247,250));c.drawRoundRect(choices[i],dp(18),dp(18),p);centerTextAt(c,o[i],choices[i].centerX(),choices[i].centerY()+dp(3),6.7f,Color.rgb(52,101,127));}drawTeacher(c,r.left+r.width()*.25f,r.bottom-dp(35),dp(31));drawHero(c,r.left+r.width()*.55f,r.bottom-dp(35),dp(32));drawFriend(c,r.left+r.width()*.78f,r.bottom-dp(35),dp(26));action.setEmpty();}

        void movingTarget(RectF r){float zoneTop=r.top+dp(130),zoneBottom=r.bottom-dp(150);long ms=SystemClock.elapsedRealtime()+miniHits*737L;float phase=(ms%2600L)/2600f;float xx=r.left+dp(55)+(r.width()-dp(110))*(.5f+.5f*(float)Math.sin(phase*Math.PI*2));float yy=zoneTop+(zoneBottom-zoneTop)*(.5f+.42f*(float)Math.cos(phase*Math.PI*2*1.37));target.set(xx-dp(28),yy-dp(28),xx+dp(28),yy+dp(28));}
        void drawTarget(Canvas c,boolean done){p.setColor(done?Color.rgb(215,239,226):Color.rgb(225,244,252));c.drawCircle(target.centerX(),target.centerY(),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(20));text.setColor(Color.rgb(57,139,188));c.drawText("✣",target.centerX(),target.centerY()+dp(7),text);}
        void drawBreak(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"ЕТАП 3/6 • ПЕРЕРВА",r.top+dp(28),7,Color.rgb(111,132,139));centerText(c,"Сніжик кидає сніжинки",r.top+dp(65),16,Color.rgb(48,82,99));centerText(c,"Влуч по рухомій сніжинці 3 рази.",r.top+dp(93),7.2f,Color.rgb(102,127,138));movingTarget(r);drawTarget(c,miniHits>=3);centerText(c,"Влучання "+Math.min(3,miniHits)+"/3",r.bottom-dp(110),7.2f,Color.rgb(89,122,137));drawHero(c,r.left+dp(85),r.bottom-dp(36),dp(31));drawFriend(c,r.right-dp(74),r.bottom-dp(36),dp(27));if(miniHits>=3)button(c,"ДЗВІНОК НА УРОК");else{action.setEmpty();postInvalidateOnAnimation();}}

        void drawHome(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"ЕТАП 5/6 • ДОРОГА ДОДОМУ",r.top+dp(28),7,Color.rgb(111,132,139));centerText(c,"Пройди слідами від школи додому",r.top+dp(67),15,Color.rgb(48,82,99));centerText(c,"Натискай сліди по черзі.",r.top+dp(95),7.2f,Color.rgb(102,127,138));float yy=r.centerY()-dp(10),left=r.left+dp(55),right=r.right-dp(55);stroke.setStrokeWidth(dp(3));stroke.setColor(Color.rgb(205,221,228));c.drawLine(left,yy,right,yy,stroke);for(int i=0;i<4;i++){float xx=left+(right-left)*i/3f;pathRects[i].set(xx-dp(25),yy-dp(25),xx+dp(25),yy+dp(25));p.setColor(i<homeStep?Color.rgb(214,239,226):(i==homeStep?Color.rgb(252,214,87):Color.rgb(235,243,247)));c.drawCircle(xx,yy,dp(22),p);centerTextAt(c,""+(i+1),xx,yy+dp(4),7,Color.rgb(65,111,137));}drawHero(c,r.left+dp(76),r.bottom-dp(40),dp(31));drawFriend(c,r.right-dp(72),r.bottom-dp(40),dp(27));if(homeStep>=4)button(c,"МИ ВДОМА");else action.setEmpty();}

        void drawDinner(Canvas c){RectF r=card();cardBase(c,r);int idx=Math.max(0,Math.min(4,schoolDone));centerText(c,"ЕТАП 6/6 • ВЕЧЕРЯ "+schoolOrdinal()+"/5",r.top+dp(28),7,Color.rgb(113,134,141));centerText(c,DISHES[idx],r.top+dp(70),20,Color.rgb(48,82,99));centerText(c,"Після школи — одна домашня українська страва.",r.top+dp(101),7.3f,Color.rgb(99,124,134));dishRect.set(r.centerX()-dp(95),r.top+dp(135),r.centerX()+dp(95),r.top+dp(260));drawDish(c,r.centerX(),r.top+dp(198),dp(72),idx);for(int i=0;i<5-dinnerBites;i++){p.setColor(Color.argb(210,255,255,255));c.drawCircle(r.centerX()-dp(42)+i*dp(21),r.top+dp(199),dp(5),p);}centerText(c,"З'їдено "+Math.min(5,dinnerBites)+"/5",r.top+dp(286),7.2f,Color.rgb(89,122,137));drawHero(c,r.centerX(),r.bottom-dp(42),dp(36));if(dinnerBites>=5)button(c,"ЗАВЕРШИТИ ДЕНЬ");else{action.setEmpty();centerText(c,"Торкайся тарілки, щоб повечеряти.",r.bottom-dp(92),6.8f,Color.rgb(111,132,139));}}

        void drawWeekendChoice(Canvas c){RectF r=card();cardBase(c,r);centerText(c,(calendarDow==Calendar.SATURDAY?"СУБОТА":"НЕДІЛЯ")+" • ВИХІДНИЙ "+(weekendDone+1)+"/2",r.top+dp(30),7,Color.rgb(109,132,140));centerText(c,"СЬОГОДНІ — СНІГОПЛАВАННЯ",r.top+dp(70),18,Color.rgb(43,105,139));centerText(c,"Не вода: холодний сухий басейн зі сніговою крупою при -8 °C.",r.top+dp(104),7.2f,Color.rgb(99,125,136));centerText(c,"Малий не тане, а буквально «пливе» крізь пухкий сніг.",r.top+dp(130),7.2f,Color.rgb(99,125,136));drawHero(c,r.centerX()-dp(42),r.bottom-dp(40),dp(34));drawFriend(c,r.centerX()+dp(52),r.bottom-dp(40),dp(28));button(c,"ВЕСТИ МАЛОГО НА ПЛАВАННЯ");}
        void drawWeekendMini(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"ВИХІДНИЙ • "+weekendChoice.toUpperCase(),r.top+dp(29),7,Color.rgb(109,132,140));centerText(c,"Ще трохи — і день буде прожито",r.top+dp(66),16,Color.rgb(43,105,139));centerText(c,"Влуч у рухому мітку 4 рази.",r.top+dp(95),7.4f,Color.rgb(99,125,136));movingTarget(r);drawTarget(c,miniHits>=4);centerText(c,"Прогрес "+Math.min(4,miniHits)+"/4",r.bottom-dp(108),7.2f,Color.rgb(89,122,137));drawHero(c,r.centerX()-dp(42),r.bottom-dp(38),dp(33));drawFriend(c,r.centerX()+dp(48),r.bottom-dp(38),dp(28));if(miniHits>=4)button(c,"ЗАВЕРШИТИ ВИХІДНИЙ");else{action.setEmpty();postInvalidateOnAnimation();}}

        void drawBonus(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"СЬОГОДНІ • "+dayName(),r.top+dp(31),7,Color.rgb(109,132,140));centerText(c,"Цей тип дня вже зараховано",r.top+dp(72),18,Color.rgb(43,105,139));String need=schoolDone<5?("Ще потрібно навчальних днів: "+(5-schoolDone)):("Ще потрібно вихідних: "+(2-weekendDone));centerText(c,need,r.top+dp(109),8,Color.rgb(99,125,136));centerText(c,"Можна зайти до спогадів або гардероба. Рік не прокручується сам.",r.top+dp(137),7,Color.rgb(99,125,136));drawHero(c,r.centerX()-dp(38),r.bottom-dp(44),dp(35));drawFriend(c,r.centerX()+dp(50),r.bottom-dp(44),dp(28));drawBottomTools(c);}
        void drawDone(Canvas c){RectF r=card();cardBase(c,r);centerText(c,className()+" • ПРОЖИТО "+totalDone()+"/7",r.top+dp(29),7,Color.rgb(109,132,140));centerText(c,"СЬОГОДНІШНІЙ ДЕНЬ ПРОЖИТО",r.top+dp(72),20,Color.rgb(42,106,141));String detail=isWeekday()?("Навчання "+schoolDone+"/5 • остання вечеря: "+prefs.getString("school_meal_last","—")):("Вихідні "+weekendDone+"/2 • "+prefs.getString("school_weekend_last","день прожито"));centerText(c,detail,r.top+dp(106),7.5f,Color.rgb(95,121,133));RectF lock=new RectF(r.left+dp(25),r.top+dp(137),r.right-dp(25),r.top+dp(215));p.setColor(Color.rgb(235,245,249));c.drawRoundRect(lock,dp(19),dp(19),p);centerTextAt(c,"НАСТУПНИЙ ДЕНЬ МОЖНА ПРОЖИТИ НЕ РАНІШЕ ЗАВТРА",lock.centerX(),lock.top+dp(30),6.7f,Color.rgb(105,129,139));centerTextAt(c,"Пропустиш завтра — прогрес залишиться "+totalDone()+"/7",lock.centerX(),lock.bottom-dp(20),7.8f,Color.rgb(50,104,134));drawHero(c,r.centerX()-dp(36),r.bottom-dp(40),dp(35));drawFriend(c,r.centerX()+dp(48),r.bottom-dp(40),dp(28));drawBottomTools(c);}
        void drawYearDone(Canvas c){RectF r=card();cardBase(c,r);centerText(c,"5 НАВЧАЛЬНИХ • 2 ВИХІДНІ",r.top+dp(31),7,Color.rgb(109,132,140));centerText(c,"РІК "+className()+" ПРОЖИТО",r.top+dp(78),22,Color.rgb(42,106,141));centerText(c,"Він минув через твої 7 взаємодій, а не просто через календар.",r.top+dp(116),7.5f,Color.rgb(95,121,133));centerText(c,grade>=11?"Шкільний шлях завершено. Спогади й гардероб залишаються доступними.":"Наступна зима відкриється, коли ти повернешся іншого дня.",r.top+dp(142),7.2f,Color.rgb(95,121,133));drawHero(c,r.centerX()-dp(35),r.bottom-dp(43),dp(38));drawFriend(c,r.centerX()+dp(52),r.bottom-dp(43),dp(29));drawBottomTools(c);}
        void drawBottomTools(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom,gap=dp(8),half=(w-dp(48)-gap)/2f;memoryBtn.set(dp(20),bottom-dp(70),dp(20)+half,bottom-dp(13));wardrobeBtn.set(memoryBtn.right+gap,bottom-dp(70),w-dp(20),bottom-dp(13));p.setColor(Color.rgb(232,243,237));c.drawRoundRect(memoryBtn,dp(18),dp(18),p);p.setColor(Color.rgb(235,243,248));c.drawRoundRect(wardrobeBtn,dp(18),dp(18),p);centerTextAt(c,"СПОГАДИ",memoryBtn.centerX(),memoryBtn.centerY()+dp(3),7.5f,Color.rgb(58,106,91));centerTextAt(c,"ГАРДЕРОБ",wardrobeBtn.centerX(),wardrobeBtn.centerY()+dp(3),7.5f,Color.rgb(60,103,127));}

        void drawDish(Canvas c,float x,float y,float r,int kind){p.setColor(Color.rgb(236,240,237));c.drawOval(new RectF(x-r,y-r*.42f,x+r,y+r*.42f),p);if(kind==0){p.setColor(Color.rgb(167,57,45));c.drawOval(new RectF(x-r*.70f,y-r*.27f,x+r*.70f,y+r*.25f),p);p.setColor(Color.rgb(247,245,226));c.drawCircle(x+r*.18f,y-r*.05f,r*.12f,p);}else if(kind==1){p.setColor(Color.rgb(238,205,145));for(int i=-1;i<=1;i++)c.drawOval(new RectF(x+i*r*.38f-r*.24f,y-r*.12f,x+i*r*.38f+r*.24f,y+r*.17f),p);}else if(kind==2){p.setColor(Color.rgb(112,152,88));for(int i=-1;i<=1;i++)c.drawRoundRect(new RectF(x+i*r*.35f-r*.20f,y-r*.15f,x+i*r*.35f+r*.20f,y+r*.16f),r*.08f,r*.08f,p);}else if(kind==3){p.setColor(Color.rgb(210,155,63));for(int i=-1;i<=1;i++)c.drawCircle(x+i*r*.34f,y,r*.22f,p);}else{p.setColor(Color.rgb(238,195,62));c.drawOval(new RectF(x-r*.68f,y-r*.25f,x+r*.68f,y+r*.24f),p);p.setColor(Color.rgb(250,245,223));for(int i=-2;i<=2;i++)c.drawCircle(x+i*r*.20f,y-r*.03f,r*.055f,p);}}
        void button(Canvas c,String label){float w=getWidth(),bottom=getHeight()-safeBottom;action.set(dp(22),bottom-dp(70),w-dp(22),bottom-dp(12));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(action,dp(20),dp(20),p);centerTextAt(c,label,action.centerX(),action.centerY()+dp(4),8.8f,Color.WHITE);}
        void drawHero(Canvas c,float x,float ground,float r){drawSnowPerson(c,x,ground,r,true,false);}void drawFriend(Canvas c,float x,float ground,float r){drawSnowPerson(c,x,ground,r,false,true);}
        void drawSnowPerson(Canvas c,float x,float ground,float r,boolean hero,boolean friend){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(44,59,68));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.68f,hy+hr*.07f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(105,78,57));stroke.setStrokeWidth(Math.max(dp(2.2f),r*.07f));c.drawLine(x-mr*.60f,my,x-mr*1.30f,my-mr*.25f,stroke);c.drawLine(x+mr*.60f,my,x+mr*1.30f,my-mr*.25f,stroke);if(hero)SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);else if(friend){p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-mr*.62f,my-mr*.70f,x+mr*.62f,my-mr*.52f),dp(4),dp(4),p);}}
        void drawTeacher(Canvas c,float x,float ground,float r){float br=r,mr=r*.74f,hr=r*.55f,by=ground-br,my=by-(br+mr)*.82f,hy=my-(mr+hr)*.82f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(45,59,68));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.07f,p);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.07f,p);stroke.setColor(Color.rgb(52,67,76));stroke.setStrokeWidth(dp(1.5f));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawLine(x-hr*.09f,hy-hr*.12f,x+hr*.09f,hy-hr*.12f,stroke);}
        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.30f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);stroke.setColor(Color.argb(65,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);}

        void setStage(int s){stage=s;feedback="";prefs.edit().putInt("school_player_stage",s).putLong("school_player_stage_day",effectiveDay).apply();buzz(16);SoundFx.play(ctx,SoundFx.UI);invalidate();}
        void persistMini(){prefs.edit().putInt("school_player_bag_mask",bagMask).putInt("school_player_mini_hits",miniHits).putInt("school_player_home_step",homeStep).putInt("school_player_dinner_bites",dinnerBites).putString("school_player_weekend_choice",weekendChoice).apply();}
        void wrong(String s){mistakes++;feedback=s;prefs.edit().putInt("school_mistakes",mistakes).apply();SoundFx.play(ctx,SoundFx.WRONG);buzz(10);invalidate();}
        void finishCountedDay(){if(prefs.getLong("school_player_last_completed_day",Long.MIN_VALUE)==effectiveDay)return;if(isWeekday())schoolDone=Math.min(5,schoolDone+1);else weekendDone=Math.min(2,weekendDone+1);SharedPreferences.Editor e=prefs.edit().putInt("school_year_school_done",schoolDone).putInt("school_year_weekend_done",weekendDone).putLong("school_player_last_completed_day",effectiveDay).putLong("school_clock_last_completed_day",effectiveDay).putInt("school_clock_days_lived",prefs.getInt("school_clock_days_lived",0)+1);if(isWeekday()){String dish=DISHES[Math.max(0,schoolDone-1)];e.putInt("school_meals_total",prefs.getInt("school_meals_total",0)+1).putString("school_meal_last",dish);}else e.putString("school_weekend_last",weekendChoice);yearComplete=schoolDone>=5&&weekendDone>=2;if(yearComplete)e.putBoolean("school_year_complete",true).putLong("school_year_complete_day",effectiveDay);e.putInt("school_player_stage",yearComplete?YEAR_DONE:DONE).putLong("school_player_stage_day",effectiveDay).apply();stage=yearComplete?YEAR_DONE:DONE;SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);NotificationScheduler.onDayCompleted(ctx);invalidate();}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(stage==INTRO){if(action.contains(x,y)){yearIntro=false;prefs.edit().putBoolean("school_year_intro_pending",false).putInt("school_clock_announced_grade",grade).apply();setStage(defaultStage());SoundFx.play(ctx,SoundFx.SCHOOL_BELL);}return true;}if(stage==DONE||stage==YEAR_DONE||stage==BONUS){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}return true;}if(stage==MORNING){for(int i=0;i<5;i++)if(bagRects[i].contains(x,y)){if(i<3){bagMask|=1<<i;SoundFx.play(ctx,SoundFx.CLOTH);buzz(10);}else wrong("Це краще залишити вдома.");persistMini();invalidate();return true;}if((bagMask&7)==7&&action.contains(x,y)){setStage(LESSON1);return true;}}else if(stage==LESSON1){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==correct(false)){SoundFx.play(ctx,SoundFx.CORRECT);buzz(18);setStage(BREAK);}else wrong("Не ця відповідь. Спробуй ще раз.");return true;}}else if(stage==BREAK){movingTarget(card());if(target.contains(x,y)&&miniHits<3){miniHits++;persistMini();SoundFx.play(ctx,SoundFx.PLAY);buzz(10);invalidate();return true;}if(miniHits>=3&&action.contains(x,y)){miniHits=0;persistMini();setStage(LESSON2);return true;}}else if(stage==LESSON2){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==correct(true)){SoundFx.play(ctx,SoundFx.CORRECT);buzz(18);setStage(HOME);}else wrong("Подумай ще раз.");return true;}}else if(stage==HOME){if(homeStep<4&&pathRects[homeStep].contains(x,y)){homeStep++;persistMini();SoundFx.play(ctx,SoundFx.CRUNCH);buzz(8);invalidate();return true;}if(homeStep>=4&&action.contains(x,y)){setStage(DINNER);return true;}}else if(stage==DINNER){if(dinnerBites<5&&dishRect.contains(x,y)){dinnerBites++;persistMini();SoundFx.play(ctx,SoundFx.ITEM);buzz(8);invalidate();return true;}if(dinnerBites>=5&&action.contains(x,y)){finishCountedDay();return true;}}else if(stage==WEEKEND){if(action.contains(x,y)){ctx.startActivity(new Intent(ctx,SnowSwimActivity.class));return true;}}else if(stage==WEEKEND_MINI){movingTarget(card());if(target.contains(x,y)&&miniHits<4){miniHits++;persistMini();SoundFx.play(ctx,SoundFx.PLAY);buzz(9);invalidate();return true;}if(miniHits>=4&&action.contains(x,y)){finishCountedDay();return true;}}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
