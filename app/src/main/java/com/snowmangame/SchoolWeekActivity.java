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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Real-time school loop. One phone calendar date equals one snowman day.
 * Days 1-5 are school days, days 6-7 are weekends. After seven real dates
 * the snowman moves into the next school year automatically.
 */
public class SchoolWeekActivity extends Activity {
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
        setContentView(new SchoolWeekView(this));
    }

    static class SchoolWeekView extends View {
        static final int PLAY=0, DINNER=1, DONE=2;
        static final String[] DISHES={"БОРЩ","ВАРЕНИКИ З КАРТОПЛЕЮ","ГОЛУБЦІ","ДЕРУНИ","БАНОШ З БРИНЗОЮ"};
        final Context ctx; final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF[] choices={new RectF(),new RectF(),new RectF()};
        final RectF action=new RectF(),memoryBtn=new RectF(),wardrobeBtn=new RectF();
        final float density,textScale; final Vibrator vibrator;
        float safeTop,safeBottom; long today,anchorDay,effectiveDay; int anchorGrade,weekDay,grade,winter,stage,mistakes; boolean yearIntro;
        String feedback="",weekendChoice="";

        SchoolWeekView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            initClock();
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);setContentDescription("Шкільний тиждень сніговика, прив'язаний до календарного дня телефона");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){@Override public WindowInsets onApplyWindowInsets(View v,WindowInsets i){safeTop=i.getSystemWindowInsetTop();safeBottom=i.getSystemWindowInsetBottom();invalidate();return i;}});requestApplyInsets();
        }

        void initClock(){
            today=localDayNumber();
            long lastSeen=prefs.getLong("school_clock_last_seen_day",today);
            effectiveDay=Math.max(today,lastSeen); // phone clock rollback must not replay an old day
            if(today>lastSeen)lastSeen=today;
            anchorDay=prefs.getLong("school_clock_anchor_day",Long.MIN_VALUE);
            anchorGrade=Math.max(2,prefs.getInt("school_clock_anchor_grade",prefs.getInt("school_grade",2)));
            if(anchorDay==Long.MIN_VALUE){
                int legacy=Math.max(1,Math.min(7,prefs.getInt("class2_days",1)));
                anchorDay=effectiveDay-(legacy-1L); // migrate current save without throwing the player back to day 1
                anchorGrade=Math.max(2,prefs.getInt("school_grade",2));
                prefs.edit().putLong("school_clock_anchor_day",anchorDay).putInt("school_clock_anchor_grade",anchorGrade).putInt("school_clock_announced_grade",anchorGrade).apply();
            }
            long elapsed=Math.max(0,effectiveDay-anchorDay);
            int week=(int)(elapsed/7L);weekDay=(int)(elapsed%7L)+1;
            grade=Math.min(11,anchorGrade+week);winter=grade+6;
            int announced=prefs.getInt("school_clock_announced_grade",grade);yearIntro=grade>announced;
            long stageDay=prefs.getLong("school_clock_stage_day",Long.MIN_VALUE);
            if(stageDay!=effectiveDay){stage=PLAY;feedback="";weekendChoice="";}else stage=Math.max(PLAY,Math.min(DONE,prefs.getInt("school_clock_stage",PLAY)));
            if(prefs.getLong("school_clock_last_completed_day",Long.MIN_VALUE)==effectiveDay)stage=DONE;
            mistakes=Math.max(0,prefs.getInt("school_mistakes",0));
            prefs.edit().putLong("school_clock_last_seen_day",lastSeen).putInt("school_grade",grade).putInt("school_winter",winter).putBoolean("class2_started",true).putLong("school_clock_stage_day",effectiveDay).apply();
        }

        long localDayNumber(){
            Calendar local=Calendar.getInstance();int y=local.get(Calendar.YEAR),m=local.get(Calendar.MONTH),d=local.get(Calendar.DAY_OF_MONTH);
            GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));utc.clear();utc.set(y,m,d,0,0,0);return utc.getTimeInMillis()/86400000L;
        }
        float dp(float v){return v*density;} float tx(float v){return v*textScale;}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,65));else vibrator.vibrate(ms);}
        boolean schoolDay(){return weekDay<=5;} String className(){return grade+"-А";}
        String modeLabel(){return schoolDay()?"НАВЧАННЯ":"ВИХІДНИЙ";}
        int daysToYear(){return 8-weekDay;}
        String theme(){switch(grade){case 2:return"Дружба";case 3:return"Команда";case 4:return"Самостійність";case 5:return"Допомога молодшим";case 6:return"Відповідальність";default:return"Шкільне життя";}}
        String lessonTitle(){switch(weekDay){case 1:return"Ранкова зустріч";case 2:return"Математика зі сніжками";case 3:return"Урок про тепло";case 4:return"Допомога Іскрику";default:return"П'ятнична справа класу";}}
        String question(){switch(weekDay){case 1:return"З чого нормально почати шкільний день?";case 2:return"На дошці 2 сніжки + 3 сніжки. Скільки разом?";case 3:return"Що станеться зі снігом у теплі?";case 4:return"Іскрик забув олівець. Що робимо?";default:return"Після уроків у класі лишились папірці. Що робимо?";}}
        String[] options(){switch(weekDay){case 1:return new String[]{"ПРИВІТАТИСЯ","СХОВАТИСЯ","ПІТИ ДОДОМУ"};case 2:return new String[]{"4","5","6"};case 3:return new String[]{"ЛІД","ВОДА","ПІСОК"};case 4:return new String[]{"ПОДІЛИТИСЯ","СМІЯТИСЯ","СХОВАТИ СВІЙ"};default:return new String[]{"ПРИБРАТИ РАЗОМ","ЗАЛИШИТИ","РОЗКИДАТИ ЩЕ"};}}
        int correctIndex(){switch(weekDay){case 2:case 3:return 1;default:return 0;}}

        @Override protected void onDraw(Canvas c){super.onDraw(c);drawBackground(c);drawHeader(c);if(yearIntro){drawYearIntro(c);return;}if(stage==DONE)drawDone(c);else if(!schoolDay())drawWeekend(c);else if(stage==DINNER)drawDinner(c);else drawLesson(c);}

        void drawBackground(Canvas c){float w=getWidth(),h=getHeight(),bottom=h-safeBottom;if(schoolDay()){LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(242,236,216),Color.rgb(229,224,203),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(193,158,114));c.drawRect(0,bottom-dp(78),w,h,p);}else{LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(164,220,246),Color.rgb(238,249,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(246,251,253));c.drawRect(0,bottom*.67f,w,h,p);}}

        void drawHeader(Canvas c){float w=getWidth(),top=safeTop+dp(10);RectF r=new RectF(dp(14),top,w-dp(14),top+dp(114));p.setColor(Color.argb(247,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.1f));text.setColor(Color.rgb(101,128,142));c.drawText("ШКОЛА • ЗИМА "+winter+" • "+className(),r.left+dp(17),r.top+dp(20),text);text.setTextSize(tx(18));text.setColor(Color.rgb(38,73,94));c.drawText("День "+weekDay+"/7 • "+modeLabel(),r.left+dp(17),r.top+dp(52),text);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(91,123,139));c.drawText(schoolDay()?"5 навчальних днів • 2 вихідні • 1 день телефона = 1 день гри":"Сьогодні школа закрита. Рік усе одно продовжується.",r.left+dp(17),r.top+dp(78),text);text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(6.5f));text.setColor(Color.rgb(126,143,150));c.drawText(daysToYear()==1?"нова зима завтра":"до нового року "+daysToYear()+" дн.",r.right-dp(17),r.bottom-dp(13),text);}

        void drawYearIntro(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(22),safeTop+dp(145),w-dp(22),bottom-dp(90));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(113,134,141));c.drawText("МИНУВ ОДИН ЛЮДСЬКИЙ ТИЖДЕНЬ",card.centerX(),card.top+dp(31),text);text.setTextSize(tx(25));text.setColor(Color.rgb(41,105,141));c.drawText("НОВА ЗИМА • "+className(),card.centerX(),card.top+dp(77),text);text.setTextSize(tx(9));text.setColor(Color.rgb(82,117,133));c.drawText("Сніговик став на рік старшим. Тема року: «"+theme()+"».»,",card.centerX(),card.top+dp(113),text);drawHero(c,card.centerX(),card.bottom-dp(80),dp(48));button(c,"ПОЧАТИ НОВИЙ РІК");}

        void drawLesson(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(88));p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(111,132,139));c.drawText(className()+" • "+lessonTitle().toUpperCase(),card.centerX(),card.top+dp(28),text);text.setTextSize(tx(12));text.setColor(Color.rgb(48,82,99));c.drawText(question(),card.centerX(),card.top+dp(67),text);text.setTextSize(tx(7));text.setColor(feedback.length()>0?Color.rgb(164,97,78):Color.rgb(102,127,138));c.drawText(feedback.length()>0?feedback:"Одна правильна дія — і навчальний день іде далі.",card.centerX(),card.top+dp(96),text);String[] o=options();float left=card.left+dp(18),right=card.right-dp(18),gap=dp(10),top=card.top+dp(126),cw=(right-left-gap*2)/3f;for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(115));p.setColor(Color.rgb(239,247,250));c.drawRoundRect(choices[i],dp(18),dp(18),p);text.setTextSize(tx(7.1f));text.setColor(Color.rgb(52,101,127));c.drawText(o[i],choices[i].centerX(),choices[i].centerY()+dp(3),text);}drawTeacher(c,card.left+card.width()*.25f,card.bottom-dp(35),dp(31));drawHero(c,card.left+card.width()*.55f,card.bottom-dp(35),dp(32));drawFriend(c,card.left+card.width()*.78f,card.bottom-dp(35),dp(26));action.setEmpty();}

        void drawDinner(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(22),safeTop+dp(145),w-dp(22),bottom-dp(90));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(113,134,141));c.drawText("ПІСЛЯ ШКОЛИ • ВЕЧЕРЯ "+weekDay+"/5",card.centerX(),card.top+dp(29),text);text.setTextSize(tx(21));text.setColor(Color.rgb(48,82,99));c.drawText(DISHES[weekDay-1],card.centerX(),card.top+dp(70),text);text.setTextSize(tx(8));text.setColor(Color.rgb(99,124,134));c.drawText("Сьогодні одна нормальна страва. Без морозива й рекламної сцени.",card.centerX(),card.top+dp(100),text);drawDish(c,card.centerX(),card.top+dp(205),dp(72),weekDay-1);drawHero(c,card.centerX(),card.bottom-dp(45),dp(37));button(c,"ПОВЕЧЕРЯТИ");}

        void drawWeekend(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(22),safeTop+dp(145),w-dp(22),bottom-dp(90));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(109,132,140));c.drawText("ВИХІДНИЙ "+(weekDay-5)+"/2",card.centerX(),card.top+dp(30),text);text.setTextSize(tx(18));text.setColor(Color.rgb(43,105,139));c.drawText(weekDay==6?"Суботній день без школи":"Тихий день перед новою зимою",card.centerX(),card.top+dp(67),text);text.setTextSize(tx(7.5f));text.setColor(Color.rgb(99,125,136));c.drawText(weekDay==6?"Обери одну справу. Більше навчальних завдань сьогодні немає.":"Обери, як провести останній день цього року.",card.centerX(),card.top+dp(96),text);String[] o=weekDay==6?new String[]{"САНЧАТА","ПРОГУЛЯНКА","ВІДПОЧИНОК"}:new String[]{"СПОГАДИ","ЗІБРАТИ РЕЧІ","ПОБУТИ ЗІ СНІЖИКОМ"};float left=card.left+dp(18),right=card.right-dp(18),gap=dp(10),top=card.top+dp(132),cw=(right-left-gap*2)/3f;for(int i=0;i<3;i++){choices[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+dp(120));p.setColor(Color.rgb(238,248,251));c.drawRoundRect(choices[i],dp(18),dp(18),p);text.setTextSize(tx(7));text.setColor(Color.rgb(56,107,133));c.drawText(o[i],choices[i].centerX(),choices[i].centerY()+dp(3),text);}drawHero(c,card.centerX()-dp(42),card.bottom-dp(40),dp(34));drawFriend(c,card.centerX()+dp(52),card.bottom-dp(40),dp(28));action.setEmpty();}

        void drawDone(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(180));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(card,dp(28),dp(28),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(109,132,140));c.drawText(className()+" • ДЕНЬ "+weekDay+"/7",card.centerX(),card.top+dp(29),text);text.setTextSize(tx(21));text.setColor(Color.rgb(42,106,141));c.drawText("СЬОГОДНІШНІЙ ДЕНЬ ПРОЖИТО",card.centerX(),card.top+dp(72),text);text.setTextSize(tx(8));text.setColor(Color.rgb(95,121,133));String detail=schoolDay()?"Вечеря: "+DISHES[weekDay-1]+".":(weekendChoice.length()>0?weekendChoice+".":"Вихідний прожито.");c.drawText(detail,card.centerX(),card.top+dp(106),text);RectF lock=new RectF(card.left+dp(25),card.top+dp(137),card.right-dp(25),card.top+dp(211));p.setColor(Color.rgb(235,245,249));c.drawRoundRect(lock,dp(19),dp(19),p);text.setTextSize(tx(7));text.setColor(Color.rgb(105,129,139));c.drawText("НАСТУПНИЙ ДЕНЬ ВІДКРИЄТЬСЯ ПІСЛЯ ЗМІНИ ДАТИ НА ТЕЛЕФОНІ",lock.centerX(),lock.top+dp(27),text);text.setTextSize(tx(10));text.setColor(Color.rgb(50,104,134));c.drawText(weekDay==7?"ЗАВТРА • НОВА ЗИМА":"ЗАВТРА • ДЕНЬ "+(weekDay+1)+"/7",lock.centerX(),lock.bottom-dp(20),text);drawHero(c,card.centerX()-dp(36),card.bottom-dp(40),dp(35));drawFriend(c,card.centerX()+dp(48),card.bottom-dp(40),dp(28));float gap=dp(8),half=(w-dp(48)-gap)/2f;memoryBtn.set(dp(20),bottom-dp(165),dp(20)+half,bottom-dp(108));wardrobeBtn.set(memoryBtn.right+gap,bottom-dp(165),w-dp(20),bottom-dp(108));p.setColor(Color.rgb(232,243,237));c.drawRoundRect(memoryBtn,dp(18),dp(18),p);p.setColor(Color.rgb(235,243,248));c.drawRoundRect(wardrobeBtn,dp(18),dp(18),p);text.setTextSize(tx(8));text.setColor(Color.rgb(58,106,91));c.drawText("СПОГАДИ",memoryBtn.centerX(),memoryBtn.centerY()+dp(3),text);text.setColor(Color.rgb(60,103,127));c.drawText("ГАРДЕРОБ",wardrobeBtn.centerX(),wardrobeBtn.centerY()+dp(3),text);action.setEmpty();}

        void drawDish(Canvas c,float x,float y,float r,int kind){p.setColor(Color.rgb(236,240,237));c.drawOval(new RectF(x-r,y-r*.42f,x+r,y+r*.42f),p);if(kind==0){p.setColor(Color.rgb(167,57,45));c.drawOval(new RectF(x-r*.70f,y-r*.27f,x+r*.70f,y+r*.25f),p);p.setColor(Color.rgb(247,245,226));c.drawCircle(x+r*.18f,y-r*.05f,r*.12f,p);}else if(kind==1){p.setColor(Color.rgb(238,205,145));for(int i=-1;i<=1;i++)c.drawOval(new RectF(x+i*r*.38f-r*.24f,y-r*.12f,x+i*r*.38f+r*.24f,y+r*.17f),p);}else if(kind==2){p.setColor(Color.rgb(112,152,88));for(int i=-1;i<=1;i++)c.drawRoundRect(new RectF(x+i*r*.35f-r*.20f,y-r*.15f,x+i*r*.35f+r*.20f,y+r*.16f),r*.08f,r*.08f,p);}else if(kind==3){p.setColor(Color.rgb(210,155,63));for(int i=-1;i<=1;i++)c.drawCircle(x+i*r*.34f,y,r*.22f,p);}else{p.setColor(Color.rgb(238,195,62));c.drawOval(new RectF(x-r*.68f,y-r*.25f,x+r*.68f,y+r*.24f),p);p.setColor(Color.rgb(250,245,223));for(int i=-2;i<=2;i++)c.drawCircle(x+i*r*.20f,y-r*.03f,r*.055f,p);}}
        void button(Canvas c,String label){float w=getWidth(),bottom=getHeight()-safeBottom;action.set(dp(22),bottom-dp(70),w-dp(22),bottom-dp(12));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.WHITE);c.drawText(label,action.centerX(),action.centerY()+dp(4),text);}

        void drawHero(Canvas c,float x,float ground,float r){drawSnowPerson(c,x,ground,r,true,false);}
        void drawFriend(Canvas c,float x,float ground,float r){drawSnowPerson(c,x,ground,r,false,true);}
        void drawSnowPerson(Canvas c,float x,float ground,float r,boolean hero,boolean friend){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(44,59,68));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.68f,hy+hr*.07f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(105,78,57));stroke.setStrokeWidth(Math.max(dp(2.2f),r*.07f));c.drawLine(x-mr*.60f,my,x-mr*1.30f,my-mr*.25f,stroke);c.drawLine(x+mr*.60f,my,x+mr*1.30f,my-mr*.25f,stroke);if(hero)SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);else if(friend){p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-mr*.62f,my-mr*.70f,x+mr*.62f,my-mr*.52f),dp(4),dp(4),p);}}
        void drawTeacher(Canvas c,float x,float ground,float r){float br=r,mr=r*.74f,hr=r*.55f,by=ground-br,my=by-(br+mr)*.82f,hy=my-(mr+hr)*.82f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(45,59,68));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.07f,p);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.07f,p);stroke.setColor(Color.rgb(52,67,76));stroke.setStrokeWidth(dp(1.5f));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawLine(x-hr*.09f,hy-hr*.12f,x+hr*.09f,hy-hr*.12f,stroke);}
        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.30f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);stroke.setColor(Color.argb(65,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);}

        void setStage(int s){stage=s;prefs.edit().putInt("school_clock_stage",s).putLong("school_clock_stage_day",effectiveDay).apply();buzz(16);SoundFx.play(ctx,SoundFx.UI);invalidate();}
        void finishDay(String weekend){if(prefs.getLong("school_clock_last_completed_day",Long.MIN_VALUE)==effectiveDay)return;int lived=prefs.getInt("school_clock_days_lived",0)+1;SharedPreferences.Editor e=prefs.edit().putLong("school_clock_last_completed_day",effectiveDay).putInt("school_clock_days_lived",lived).putInt("school_clock_stage",DONE).putLong("school_clock_stage_day",effectiveDay);if(schoolDay()){int meals=prefs.getInt("school_meals_total",0)+1;e.putInt("school_meals_total",meals).putString("school_meal_last",DISHES[weekDay-1]);}else e.putString("school_weekend_last",weekend);e.apply();weekendChoice=weekend;stage=DONE;SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);invalidate();}
        void wrong(String s){mistakes++;feedback=s;prefs.edit().putInt("school_mistakes",mistakes).apply();SoundFx.play(ctx,SoundFx.WRONG);buzz(10);invalidate();}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(yearIntro){if(action.contains(x,y)){prefs.edit().putInt("school_clock_announced_grade",grade).apply();yearIntro=false;SoundFx.play(ctx,SoundFx.SCHOOL_BELL);invalidate();}return true;}if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}return true;}if(!schoolDay()){String[] o=weekDay==6?new String[]{"Санчата","Прогулянка","Відпочинок"}:new String[]{"Спогади","Зібрані речі","Час зі Сніжиком"};for(int i=0;i<3;i++)if(choices[i].contains(x,y)){finishDay(o[i]);return true;}return true;}if(stage==PLAY){for(int i=0;i<3;i++)if(choices[i].contains(x,y)){if(i==correctIndex()){feedback="Правильно.";SoundFx.play(ctx,SoundFx.CORRECT);buzz(20);setStage(DINNER);}else wrong("Не ця дія. Спробуй ще раз.");return true;}}else if(stage==DINNER&&action.contains(x,y)){finishDay("");return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
