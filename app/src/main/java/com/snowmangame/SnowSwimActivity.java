package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** Weekend swimming mini-game in a near-freezing pool made for snowmen. */
public class SnowSwimActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){w.setStatusBarColor(Color.rgb(121,198,229));w.setNavigationBarColor(Color.rgb(235,249,253));}
        setContentView(new SwimView(this));
    }

    static class SwimView extends View {
        final Context ctx;final SharedPreferences prefs;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);final RectF iceBtn=new RectF(),strokeBtn=new RectF(),doneBtn=new RectF();final float d,ts;final Vibrator vibrator;
        int laps,warmth,target;boolean weekend,completedToday,finished;
        SwimView(Context c){super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",MODE_PRIVATE);d=getResources().getDisplayMetrics().density;ts=Math.min(getResources().getDisplayMetrics().scaledDensity,d*1.15f);text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);Calendar cal=Calendar.getInstance();int dow=cal.get(Calendar.DAY_OF_WEEK);weekend=dow==Calendar.SATURDAY||dow==Calendar.SUNDAY;long today=localDay();completedToday=prefs.getLong("school_player_last_completed_day",Long.MIN_VALUE)==today;laps=prefs.getInt("school_swim_laps",0);warmth=prefs.getInt("school_swim_warmth",0);target=Math.max(5,Math.min(6,prefs.getInt("school_year_weekend_done",0)==0?5:6));setClickable(true);}
        float dp(float v){return v*d;}float tx(float v){return v*ts;}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,60));else vibrator.vibrate(ms);}
        void ct(Canvas c,String s,float y,float size,int color){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(size));text.setColor(color);while(text.measureText(s)>getWidth()-dp(52)&&text.getTextSize()>tx(5.2f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(s,getWidth()/2f,y,text);}
        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),h=getHeight();LinearGradient g=new LinearGradient(0,0,0,h,Color.rgb(167,222,244),Color.rgb(239,250,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);ct(c,"ВИХІДНИЙ • КРИЖАНИЙ БАСЕЙН",dp(52),7,Color.rgb(65,112,137));if(!weekend){ct(c,"ПЛАВАННЯ — У ВИХІДНІ",dp(118),20,Color.rgb(44,101,132));ct(c,"У будні малий у школі. Басейн відкриється в суботу або неділю.",dp(158),7,Color.rgb(86,119,136));doneBtn.set(dp(28),h-dp(88),w-dp(28),h-dp(24));drawButton(c,doneBtn,"ПОВЕРНУТИСЯ",Color.rgb(48,123,160));return;}if(completedToday||finished){ct(c,"ПЛАВАННЯ ЗАВЕРШЕНО",dp(112),19,Color.rgb(44,101,132));ct(c,"Малий не розтанув і вихідний зараховано.",dp(150),7.2f,Color.rgb(85,120,136));drawPool(c,dp(190),h-dp(150));doneBtn.set(dp(28),h-dp(88),w-dp(28),h-dp(24));drawButton(c,doneBtn,"ДОДОМУ",Color.rgb(48,123,160));return;}ct(c,"Тримай воду близько 0°C",dp(102),17,Color.rgb(44,101,132));ct(c,"Роби гребки. Коли вода нагріється до 3°C — спочатку додай лід.",dp(137),6.8f,Color.rgb(85,120,136));RectF pool=new RectF(dp(24),dp(178),w-dp(24),h-dp(220));p.setColor(warmth>=3?Color.rgb(236,193,181):Color.rgb(121,204,234));c.drawRoundRect(pool,dp(35),dp(35),p);for(int i=0;i<8;i++){p.setColor(Color.argb(205,236,250,255));c.drawCircle(pool.left+dp(26)+i*(pool.width()-dp(52))/7f,pool.top+dp(30)+(i%2)*dp(18),dp(7),p);}drawSnowKid(c,pool.centerX(),pool.bottom-dp(36),dp(36));ct(c,"Гребки "+laps+"/"+target+" • вода "+warmth+"°C",pool.bottom+dp(38),8.3f,warmth>=3?Color.rgb(178,77,66):Color.rgb(55,115,145));if(warmth>=3)ct(c,"Занадто тепло — додай лід.",pool.bottom+dp(66),6.8f,Color.rgb(178,77,66));iceBtn.set(dp(24),h-dp(175),w/2f-dp(7),h-dp(112));strokeBtn.set(w/2f+dp(7),h-dp(175),w-dp(24),h-dp(112));drawButton(c,iceBtn,"+ ЛІД",Color.rgb(91,177,211));drawButton(c,strokeBtn,"ГРЕБОК",warmth>=3?Color.rgb(175,190,197):Color.rgb(47,130,172));if(laps>=target){doneBtn.set(dp(28),h-dp(92),w-dp(28),h-dp(28));drawButton(c,doneBtn,"ВИТЕРТИСЯ Й ДОДОМУ",Color.rgb(48,123,160));}}
        void drawPool(Canvas c,float top,float bottom){p.setColor(Color.rgb(121,204,234));RectF r=new RectF(dp(32),top,getWidth()-dp(32),bottom);c.drawRoundRect(r,dp(32),dp(32),p);drawSnowKid(c,r.centerX(),r.bottom-dp(32),dp(34));}
        void drawSnowKid(Canvas c,float x,float ground,float r){float br=r,mr=r*.72f,hr=r*.52f,by=ground-br,my=by-(br+mr)*.82f,hy=my-(mr+hr)*.82f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(44,59,68));c.drawCircle(x-hr*.28f,hy-hr*.12f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.12f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.68f,hy+hr*.07f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);}
        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);}
        void drawButton(Canvas c,RectF r,String label,int color){p.setColor(color);c.drawRoundRect(r,dp(19),dp(19),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.6f));text.setColor(Color.WHITE);c.drawText(label,r.centerX(),r.centerY()+dp(3),text);}
        void persist(){prefs.edit().putInt("school_swim_laps",laps).putInt("school_swim_warmth",warmth).apply();}
        void completeWeekend(){long today=localDay();if(prefs.getLong("school_player_last_completed_day",Long.MIN_VALUE)==today){finished=true;invalidate();return;}int school=Math.max(0,Math.min(5,prefs.getInt("school_year_school_done",0)));int weekendDone=Math.min(2,prefs.getInt("school_year_weekend_done",0)+1);boolean yearDone=school>=5&&weekendDone>=2;SharedPreferences.Editor e=prefs.edit().putInt("school_year_weekend_done",weekendDone).putLong("school_player_last_completed_day",today).putLong("school_clock_last_completed_day",today).putInt("school_clock_days_lived",prefs.getInt("school_clock_days_lived",0)+1).putString("school_weekend_last","Крижане плавання").putInt("school_swim_laps",0).putInt("school_swim_warmth",0).putInt("school_player_stage",yearDone?31:30).putLong("school_player_stage_day",today);if(yearDone)e.putBoolean("school_year_complete",true).putLong("school_year_complete_day",today);e.apply();NotificationScheduler.onDayCompleted(ctx);SoundFx.play(ctx,SoundFx.COMPLETE);buzz(28);finished=true;invalidate();}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(!weekend||completedToday||finished){if(doneBtn.contains(x,y))((Activity)ctx).finish();return true;}if(iceBtn.contains(x,y)){warmth=Math.max(0,warmth-2);persist();SoundFx.play(ctx,SoundFx.CRUNCH);buzz(8);invalidate();return true;}if(strokeBtn.contains(x,y)&&laps<target){if(warmth>=3){buzz(16);SoundFx.play(ctx,SoundFx.WRONG);return true;}laps++;warmth++;persist();SoundFx.play(ctx,SoundFx.PLAY);buzz(9);invalidate();return true;}if(laps>=target&&doneBtn.contains(x,y)){completeWeekend();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
        long localDay(){Calendar l=Calendar.getInstance();GregorianCalendar u=new GregorianCalendar(TimeZone.getTimeZone("UTC"));u.clear();u.set(l.get(Calendar.YEAR),l.get(Calendar.MONTH),l.get(Calendar.DAY_OF_MONTH));return u.getTimeInMillis()/86400000L;}
    }
}
