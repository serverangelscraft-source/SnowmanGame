from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolWeekActivity.java")
manifest_path=Path("app/src/main/AndroidManifest.xml")
gradle_path=Path("app/build.gradle")
call_path=Path("app/src/main/java/com/snowmangame/SchoolCallActivity.java")
swim_path=Path("app/src/main/java/com/snowmangame/SnowSwimActivity.java")

school=school_path.read_text(encoding="utf-8")
manifest=manifest_path.read_text(encoding="utf-8")

old='''        setContentView(new SchoolWeekView(this));\n    }'''
new='''        setContentView(new SchoolWeekView(this));\n        NotificationScheduler.onSchoolOpened(this);\n    }'''
if old not in school: raise SystemExit("v18.6 patch failed: SchoolWeekActivity onCreate")
school=school.replace(old,new,1)

old='''stage=yearComplete?YEAR_DONE:DONE;SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);invalidate();}'''
new='''stage=yearComplete?YEAR_DONE:DONE;SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);NotificationScheduler.onDayCompleted(ctx);invalidate();}'''
if old not in school: raise SystemExit("v18.6 patch failed: counted day completion")
school=school.replace(old,new,1)

# Weekend is now a concrete family activity: snow-swimming in a sub-zero dry pool.
weekend_draw='''        void drawWeekendChoice(Canvas c){RectF r=card();cardBase(c,r);centerText(c,(calendarDow==Calendar.SATURDAY?"СУБОТА":"НЕДІЛЯ")+" • ВИХІДНИЙ "+(weekendDone+1)+"/2",r.top+dp(30),7,Color.rgb(109,132,140));centerText(c,"СЬОГОДНІ — СНІГОПЛАВАННЯ",r.top+dp(70),18,Color.rgb(43,105,139));centerText(c,"Не вода: холодний сухий басейн зі сніговою крупою при -8 °C.",r.top+dp(104),7.2f,Color.rgb(99,125,136));centerText(c,"Малий не тане, а буквально «пливе» крізь пухкий сніг.",r.top+dp(130),7.2f,Color.rgb(99,125,136));drawHero(c,r.centerX()-dp(42),r.bottom-dp(40),dp(34));drawFriend(c,r.centerX()+dp(52),r.bottom-dp(40),dp(28));button(c,"ВЕСТИ МАЛОГО НА ПЛАВАННЯ");}\n        void drawWeekendMini'''
pattern=r'        void drawWeekendChoice\(Canvas c\)\{.*?\n        void drawWeekendMini'
school,n=re.subn(pattern,weekend_draw,school,count=1,flags=re.S)
if n!=1: raise SystemExit("v18.6 patch failed: weekend screen")

pattern=r'\}else if\(stage==WEEKEND\)\{.*?\}else if\(stage==WEEKEND_MINI\)\{'
replacement='''}else if(stage==WEEKEND){if(action.contains(x,y)){ctx.startActivity(new Intent(ctx,SnowSwimActivity.class));return true;}}else if(stage==WEEKEND_MINI){'''
school,n=re.subn(pattern,replacement,school,count=1,flags=re.S)
if n!=1: raise SystemExit("v18.6 patch failed: weekend touch route")

call_java=r'''package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import java.util.Calendar;

public class SchoolCallActivity extends Activity {
    @Override public void onCreate(Bundle b){super.onCreate(b);Window w=getWindow();if(Build.VERSION.SDK_INT>=21){w.setStatusBarColor(Color.BLACK);w.setNavigationBarColor(Color.BLACK);}setContentView(new CallView(this));}
    static class CallView extends View {
        final Context ctx;final SharedPreferences prefs;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),t=new Paint(Paint.ANTI_ALIAS_FLAG);final RectF endBtn=new RectF();final float d,ts;final long started=SystemClock.elapsedRealtime();boolean recorded=false;
        CallView(Context c){super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",MODE_PRIVATE);d=getResources().getDisplayMetrics().density;ts=Math.min(getResources().getDisplayMetrics().scaledDensity,d*1.12f);t.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));setClickable(true);}
        float dp(float v){return v*d;}float tx(float v){return v*ts;}long callDay(){Calendar c=Calendar.getInstance();return c.get(Calendar.YEAR)*1000L+c.get(Calendar.DAY_OF_YEAR);}long localDay(){Calendar c=Calendar.getInstance();java.util.GregorianCalendar u=new java.util.GregorianCalendar(java.util.TimeZone.getTimeZone("UTC"));u.clear();u.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));return u.getTimeInMillis()/86400000L;}
        String line(){Calendar c=Calendar.getInstance();int dow=c.get(Calendar.DAY_OF_WEEK),school=Math.max(0,Math.min(5,prefs.getInt("school_year_school_done",0)));if(prefs.getLong("school_player_last_completed_day",Long.MIN_VALUE)==localDay())return"Я вже вдома. Сьогоднішній день ми прожили.";switch(dow){case Calendar.MONDAY:return"У нас перерва. Пан Криж сказав, що сьогодні працюємо в парі.";case Calendar.TUESDAY:return"Ми рахували сніжки. Сніжик знову зробив вигляд, що не знає відповідь.";case Calendar.WEDNESDAY:return"Все добре. Після уроків ще буде вечеря "+Math.min(5,school+1)+"/5.";case Calendar.THURSDAY:return"Іскрик сьогодні сів поруч. Я не загубив рукавички.";default:return"П'ятниця. Ми вже майже закрили навчальний тиждень.";}}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();LinearGradient g=new LinearGradient(0,0,0,h,Color.rgb(22,37,50),Color.rgb(5,12,18),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);long ms=SystemClock.elapsedRealtime()-started;boolean connected=ms>1700;t.setTextAlign(Paint.Align.CENTER);t.setColor(Color.WHITE);t.setTextSize(tx(8));c.drawText(connected?"НА ЗВ'ЯЗКУ":"ВИКЛИК...",w/2f,dp(72),t);drawSnow(c,w/2f,dp(250),dp(68));t.setTextSize(tx(20));c.drawText("Сніговичок • "+Math.max(2,prefs.getInt("school_grade",2))+"-А",w/2f,dp(360),t);t.setTextSize(tx(7));t.setColor(Color.rgb(175,202,219));c.drawText(connected?"перерва у школі":"чекаємо, поки візьме слухавку",w/2f,dp(390),t);RectF bubble=new RectF(dp(28),dp(430),w-dp(28),dp(565));p.setColor(Color.argb(30,255,255,255));c.drawRoundRect(bubble,dp(24),dp(24),p);t.setTextAlign(Paint.Align.LEFT);t.setTextSize(tx(8));t.setColor(Color.WHITE);if(connected){drawWrapped(c,line(),bubble.left+dp(18),bubble.top+dp(35),bubble.width()-dp(36),dp(25));if(!recorded){recorded=true;long cd=callDay();if(prefs.getLong("school_call_last_day",Long.MIN_VALUE)!=cd)prefs.edit().putLong("school_call_last_day",cd).putInt("school_calls_total",prefs.getInt("school_calls_total",0)+1).putString("school_call_last_text",line()).apply();SoundFx.play(ctx,SoundFx.PHONE);}}else{t.setTextAlign(Paint.Align.CENTER);c.drawText("...",bubble.centerX(),bubble.centerY(),t);}endBtn.set(dp(55),h-dp(115),w-dp(55),h-dp(45));p.setColor(Color.rgb(187,59,57));c.drawRoundRect(endBtn,dp(28),dp(28),p);t.setTextAlign(Paint.Align.CENTER);t.setTextSize(tx(8));t.setColor(Color.WHITE);c.drawText("ЗАВЕРШИТИ ДЗВІНОК",endBtn.centerX(),endBtn.centerY()+dp(3),t);if(!connected)postInvalidateOnAnimation();}
        void drawWrapped(Canvas c,String s,float x,float y,float max,float lh){String[] words=s.split(" ");String line="";for(String w:words){String n=line.length()==0?w:line+" "+w;if(t.measureText(n)>max&&line.length()>0){c.drawText(line,x,y,t);y+=lh;line=w;}else line=n;}if(line.length()>0)c.drawText(line,x,y,t);}void drawSnow(Canvas c,float x,float y,float r){p.setColor(Color.WHITE);c.drawCircle(x,y+r*.55f,r,p);c.drawCircle(x,y-r*.35f,r*.72f,p);c.drawCircle(x,y-r*1.12f,r*.53f,p);p.setColor(Color.rgb(40,50,58));c.drawCircle(x-r*.16f,y-r*1.20f,r*.05f,p);c.drawCircle(x+r*.16f,y-r*1.20f,r*.05f,p);p.setColor(Color.rgb(242,117,32));Path nose=new Path();nose.moveTo(x,y-r*1.10f);nose.lineTo(x+r*.55f,y-r*1.04f);nose.lineTo(x,y-r*.98f);nose.close();c.drawPath(nose,p);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP&&endBtn.contains(e.getX(),e.getY())){performClick();finish();return true;}return true;}@Override public boolean performClick(){super.performClick();return true;}
    }
}
'''

swim_java=r'''package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class SnowSwimActivity extends Activity {
    @Override public void onCreate(Bundle b){super.onCreate(b);Window w=getWindow();if(Build.VERSION.SDK_INT>=21){w.setStatusBarColor(Color.rgb(19,93,135));w.setNavigationBarColor(Color.rgb(226,245,251));}setContentView(new SwimView(this));}
    static class SwimView extends View {
        final Context ctx;final SharedPreferences prefs;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),t=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);final RectF left=new RectF(),right=new RectF(),finish=new RectF();final float d,ts;int strokes=0;boolean expectLeft=true,done=false,counted=false;String feedback="Чергуй лівий і правий гребок.";
        SwimView(Context c){super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",MODE_PRIVATE);d=getResources().getDisplayMetrics().density;ts=Math.min(getResources().getDisplayMetrics().scaledDensity,d*1.12f);t.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);setClickable(true);}
        float dp(float v){return v*d;}float tx(float v){return v*ts;}long localDay(){Calendar c=Calendar.getInstance();GregorianCalendar u=new GregorianCalendar(TimeZone.getTimeZone("UTC"));u.clear();u.set(c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));return u.getTimeInMillis()/86400000L;}boolean weekend(){int x=Calendar.getInstance().get(Calendar.DAY_OF_WEEK);return x==Calendar.SATURDAY||x==Calendar.SUNDAY;}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();LinearGradient g=new LinearGradient(0,0,0,h,Color.rgb(166,225,248),Color.rgb(236,249,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);t.setTextAlign(Paint.Align.CENTER);t.setColor(Color.rgb(31,88,119));t.setTextSize(tx(7));c.drawText("КРИЖАНИЙ СПОРТЗАЛ • -8 °C",w/2f,dp(48),t);t.setTextSize(tx(22));c.drawText("СНІГОПЛАВАННЯ",w/2f,dp(88),t);t.setTextSize(tx(7));t.setColor(Color.rgb(75,119,139));c.drawText("Сухий басейн зі сніговою крупою • без води • не танемо",w/2f,dp(116),t);
            RectF pool=new RectF(dp(22),dp(155),w-dp(22),dp(500));p.setColor(Color.rgb(220,243,251));c.drawRoundRect(pool,dp(30),dp(30),p);stroke.setColor(Color.rgb(117,189,221));stroke.setStrokeWidth(dp(3));for(int i=1;i<4;i++)c.drawLine(pool.left,pool.top+pool.height()*i/4f,pool.right,pool.top+pool.height()*i/4f,stroke);for(int i=0;i<28;i++){float x=pool.left+dp(15)+(i*47%(int)Math.max(1,pool.width()-dp(30)));float y=pool.top+dp(15)+(i*83%(int)Math.max(1,pool.height()-dp(30)));p.setColor(Color.argb(150,255,255,255));c.drawCircle(x,y,dp(5+(i%3)),p);}float progress=Math.min(1f,strokes/8f),sx=pool.left+dp(55)+(pool.width()-dp(110))*progress;drawSnow(c,sx,pool.centerY()+dp(42),dp(34));t.setTextSize(tx(8));t.setColor(Color.rgb(49,105,132));c.drawText(done?"Доріжку пропливли":"Гребки "+strokes+"/8",w/2f,dp(535),t);t.setTextSize(tx(6.8f));c.drawText(feedback,w/2f,dp(565),t);
            if(!done){float gap=dp(10),bw=(w-dp(54)-gap)/2f;left.set(dp(22),h-dp(105),dp(22)+bw,h-dp(35));right.set(left.right+gap,h-dp(105),w-dp(22),h-dp(35));p.setColor(expectLeft?Color.rgb(37,119,165):Color.rgb(116,170,196));c.drawRoundRect(left,dp(22),dp(22),p);p.setColor(!expectLeft?Color.rgb(37,119,165):Color.rgb(116,170,196));c.drawRoundRect(right,dp(22),dp(22),p);t.setTextSize(tx(8));t.setColor(Color.WHITE);c.drawText("ЛІВИЙ ГРЕБОК",left.centerX(),left.centerY()+dp(4),t);c.drawText("ПРАВИЙ ГРЕБОК",right.centerX(),right.centerY()+dp(4),t);finish.setEmpty();}else{finish.set(dp(34),h-dp(105),w-dp(34),h-dp(35));p.setColor(Color.rgb(37,119,165));c.drawRoundRect(finish,dp(22),dp(22),p);t.setTextSize(tx(8));t.setColor(Color.WHITE);c.drawText("ВИЛІЗТИ З БАСЕЙНУ",finish.centerX(),finish.centerY()+dp(4),t);}
        }
        void drawSnow(Canvas c,float x,float y,float r){p.setColor(Color.WHITE);c.drawCircle(x,y,r,p);c.drawCircle(x,y-r*.78f,r*.72f,p);c.drawCircle(x,y-r*1.43f,r*.52f,p);p.setColor(Color.rgb(38,54,64));c.drawCircle(x-r*.14f,y-r*1.50f,r*.05f,p);c.drawCircle(x+r*.14f,y-r*1.50f,r*.05f,p);}
        void stroke(boolean isLeft){if(done)return;if(isLeft!=expectLeft){feedback="Не двічі однією рукою — чергуй гребки.";SoundFx.play(ctx,SoundFx.WRONG);invalidate();return;}strokes++;expectLeft=!expectLeft;feedback=strokes<8?"Добре. Тепер інша рука.":"Фініш! Сніг тримає форму.";SoundFx.play(ctx,SoundFx.SLED);if(strokes>=8){done=true;countWeekend();}invalidate();}
        void countWeekend(){if(counted)return;counted=true;long today=localDay();int weekendDone=Math.max(0,Math.min(2,prefs.getInt("school_year_weekend_done",0))),schoolDone=Math.max(0,Math.min(5,prefs.getInt("school_year_school_done",0)));boolean can=weekend()&&prefs.getLong("school_player_last_completed_day",Long.MIN_VALUE)!=today&&weekendDone<2;if(!can){feedback=weekend()?"Тренування завершено. Сьогоднішній день уже врахований.":"Тренування завершено. Прогрес року рахується тільки у вихідні.";return;}weekendDone++;boolean yearDone=schoolDone>=5&&weekendDone>=2;SharedPreferences.Editor e=prefs.edit().putInt("school_year_weekend_done",weekendDone).putLong("school_player_last_completed_day",today).putLong("school_clock_last_completed_day",today).putInt("school_clock_days_lived",prefs.getInt("school_clock_days_lived",0)+1).putString("school_weekend_last","Снігоплавання").putInt("school_player_stage",yearDone?31:30).putLong("school_player_stage_day",today);if(yearDone)e.putBoolean("school_year_complete",true).putLong("school_year_complete_day",today);e.apply();NotificationScheduler.onDayCompleted(ctx);feedback=yearDone?"Вихідний прожито. Рік 7/7 завершено.":"Вихідний прожито. Снігоплавання зараховано.";SoundFx.play(ctx,SoundFx.COMPLETE);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(!done){if(left.contains(x,y)){stroke(true);return true;}if(right.contains(x,y)){stroke(false);return true;}}else if(finish.contains(x,y)){Intent i=new Intent(ctx,SchoolWeekActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);ctx.startActivity(i);((Activity)ctx).finish();return true;}return true;}@Override public boolean performClick(){super.performClick();return true;}
    }
}
'''

call_path.write_text(call_java,encoding="utf-8")
swim_path.write_text(swim_java,encoding="utf-8")

if 'android.permission.POST_NOTIFICATIONS' not in manifest:
    anchor='    <uses-permission android:name="android.permission.VIBRATE"/>\n'
    if anchor not in manifest: raise SystemExit("v18.6 patch failed: VIBRATE permission")
    manifest=manifest.replace(anchor,anchor+'    <uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>\n    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>\n',1)

if '.SchoolCallActivity' not in manifest:
    anchor='        <activity android:name=".SchoolWeekActivity" android:screenOrientation="portrait" android:exported="false"/>\n'
    if anchor not in manifest: raise SystemExit("v18.6 patch failed: SchoolWeek manifest anchor")
    manifest=manifest.replace(anchor,anchor+'        <activity android:name=".SchoolCallActivity" android:screenOrientation="portrait" android:exported="false"/>\n        <activity android:name=".SnowSwimActivity" android:screenOrientation="portrait" android:exported="false"/>\n',1)

if '.SnowReminderReceiver' not in manifest:
    receiver='''        <receiver\n            android:name=".SnowReminderReceiver"\n            android:enabled="true"\n            android:exported="false">\n            <intent-filter>\n                <action android:name="android.intent.action.BOOT_COMPLETED"/>\n                <action android:name="android.intent.action.TIME_SET"/>\n                <action android:name="android.intent.action.TIMEZONE_CHANGED"/>\n                <action android:name="android.intent.action.DATE_CHANGED"/>\n            </intent-filter>\n        </receiver>\n'''
    if '    </application>' not in manifest: raise SystemExit("v18.6 patch failed: application close")
    manifest=manifest.replace('    </application>',receiver+'    </application>',1)

school_path.write_text(school,encoding="utf-8")
manifest_path.write_text(manifest,encoding="utf-8")

g=gradle_path.read_text(encoding="utf-8")
g=re.sub(r'versionCode\s+\d+','versionCode 39',g)
g=re.sub(r'versionName\s+"[^"]+"','versionName "18.6"',g)
gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.6: adaptive reminders, school phone calls and weekend snow-swimming")
