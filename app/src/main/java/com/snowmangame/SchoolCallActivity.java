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

/** In-game imitation of a short family phone call while the snowman is at school. */
public class SchoolCallActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){w.setStatusBarColor(Color.rgb(27,37,44));w.setNavigationBarColor(Color.rgb(27,37,44));}
        setContentView(new CallView(this));
    }

    static class CallView extends View {
        final Context ctx; final SharedPreferences prefs; final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF answer=new RectF(),decline=new RectF(),done=new RectF(); final float d,ts; final Vibrator vibrator;
        boolean talking=false,already=false; long today;
        CallView(Context c){super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",MODE_PRIVATE);d=getResources().getDisplayMetrics().density;ts=Math.min(getResources().getDisplayMetrics().scaledDensity,d*1.15f);text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);setClickable(true);today=localDay();already=prefs.getLong("school_phone_last_call_day",Long.MIN_VALUE)==today;SoundFx.play(c,SoundFx.PHONE);}
        float dp(float v){return v*d;} float tx(float v){return v*ts;}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,55));else vibrator.vibrate(ms);}
        void ct(Canvas c,String s,float y,float size,int color){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(size));text.setColor(color);while(text.measureText(s)>getWidth()-dp(58)&&text.getTextSize()>tx(5.2f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(s,getWidth()/2f,y,text);}
        String line(){int o=Math.max(1,Math.min(5,prefs.getInt("school_year_school_done",0)+1));switch(o){case 1:return"Ти вже в класі? Після школи не забудь повечеряти.";case 2:return"Рукавички забери із собою. І моркву не загуби.";case 3:return"Як урок про тепло? Сам тільки не розтань.";case 4:return"Передай Іскрику привіт. Побачимось увечері.";default:return"Останній навчальний день. Увечері чекатиме вечеря.";}}
        @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(Color.rgb(27,37,44));float w=getWidth(),h=getHeight();ct(c,"ТЕЛЕФОН • ШКОЛА",dp(54),7,Color.rgb(159,185,196));RectF phone=new RectF(dp(30),dp(86),w-dp(30),h-dp(74));p.setColor(Color.rgb(39,51,59));c.drawRoundRect(phone,dp(34),dp(34),p);p.setColor(Color.rgb(224,243,250));c.drawCircle(w/2f,phone.top+dp(98),dp(44),p);ct(c,"М",phone.top+dp(111),22,Color.rgb(55,119,149));ct(c,"МАМА",phone.top+dp(174),13,Color.WHITE);
            if(already&&!talking){ct(c,"Ви вже говорили сьогодні.",phone.top+dp(214),8,Color.rgb(188,207,214));done.set(phone.left+dp(28),phone.bottom-dp(74),phone.right-dp(28),phone.bottom-dp(20));p.setColor(Color.rgb(64,116,142));c.drawRoundRect(done,dp(18),dp(18),p);ct(c,"ПОВЕРНУТИСЯ",done.centerY()+dp(4),8,Color.WHITE);return;}
            if(!talking){ct(c,"ВХІДНИЙ ДЗВІНОК",phone.top+dp(215),7,Color.rgb(183,205,213));ct(c,"Велика перерва — можна коротко відповісти.",phone.top+dp(245),6.5f,Color.rgb(150,176,186));answer.set(phone.left+dp(24),phone.bottom-dp(86),phone.centerX()-dp(8),phone.bottom-dp(24));decline.set(phone.centerX()+dp(8),phone.bottom-dp(86),phone.right-dp(24),phone.bottom-dp(24));p.setColor(Color.rgb(62,157,101));c.drawRoundRect(answer,dp(20),dp(20),p);p.setColor(Color.rgb(187,80,74));c.drawRoundRect(decline,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.WHITE);c.drawText("ВІДПОВІСТИ",answer.centerX(),answer.centerY()+dp(3),text);c.drawText("СКИНУТИ",decline.centerX(),decline.centerY()+dp(3),text);}else{ct(c,line(),phone.top+dp(235),7.2f,Color.rgb(226,239,244));ct(c,"00:18 • коротка розмова",phone.top+dp(275),6,Color.rgb(153,181,191));done.set(phone.left+dp(28),phone.bottom-dp(74),phone.right-dp(28),phone.bottom-dp(20));p.setColor(Color.rgb(62,130,158));c.drawRoundRect(done,dp(18),dp(18),p);ct(c,"ЗАВЕРШИТИ РОЗМОВУ",done.centerY()+dp(4),7.5f,Color.WHITE);}}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(already&&!talking&&done.contains(x,y)){((Activity)ctx).finish();return true;}if(!talking&&answer.contains(x,y)){talking=true;already=false;prefs.edit().putLong("school_phone_last_call_day",today).putInt("school_calls_answered",prefs.getInt("school_calls_answered",0)+1).apply();SoundFx.play(ctx,SoundFx.PHONE);buzz(18);invalidate();return true;}if(!talking&&decline.contains(x,y)){prefs.edit().putLong("school_phone_last_call_day",today).putInt("school_calls_missed",prefs.getInt("school_calls_missed",0)+1).apply();buzz(10);((Activity)ctx).finish();return true;}if(talking&&done.contains(x,y)){((Activity)ctx).finish();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
        long localDay(){Calendar l=Calendar.getInstance();GregorianCalendar u=new GregorianCalendar(TimeZone.getTimeZone("UTC"));u.clear();u.set(l.get(Calendar.YEAR),l.get(Calendar.MONTH),l.get(Calendar.DAY_OF_MONTH));return u.getTimeInMillis()/86400000L;}
    }
}
