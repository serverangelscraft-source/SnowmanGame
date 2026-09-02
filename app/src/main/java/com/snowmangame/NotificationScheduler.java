package com.snowmangame;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** Local, player-friendly reminders for the school-life loop. */
public final class NotificationScheduler {
    private static final String CHANNEL_ID="snowman_school_life";
    private static final int ALARM_REQ=1845;
    private static final int NOTIFY_ID=1845;
    private static final String PREFS="snowman_game";

    private NotificationScheduler(){}

    public static void onSchoolOpened(Activity activity){
        SharedPreferences p=activity.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        p.edit().putLong("school_notify_last_open_day",localDayNumber(Calendar.getInstance())).apply();
        ensureChannel(activity);
        if(Build.VERSION.SDK_INT>=33 && activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED && !p.getBoolean("school_notify_permission_asked",false)){
            p.edit().putBoolean("school_notify_permission_asked",true).apply();
            activity.requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},1845);
        }
        scheduleNext(activity);
    }

    public static void onDayCompleted(Context context){scheduleNext(context);}
    public static void onSystemEvent(Context context){ensureChannel(context);scheduleNext(context);}

    public static void fireReminder(Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        if(!p.getBoolean("class2_started",false) || !p.getBoolean("school_notifications_enabled",true)){scheduleNext(context);return;}
        if(Build.VERSION.SDK_INT>=33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){scheduleNext(context);return;}

        Calendar now=Calendar.getInstance();
        long today=localDayNumber(now);
        if(p.getLong("school_notify_last_sent_day",Long.MIN_VALUE)==today){scheduleNext(context);return;}
        long completed=p.getLong("school_player_last_completed_day",Long.MIN_VALUE);
        int school=Math.max(0,Math.min(5,p.getInt("school_year_school_done",0)));
        int weekend=Math.max(0,Math.min(2,p.getInt("school_year_weekend_done",0)));
        boolean yearComplete=p.getBoolean("school_year_complete",false) || (school>=5&&weekend>=2);
        long completeDay=p.getLong("school_year_complete_day",Long.MIN_VALUE);
        int grade=Math.max(2,Math.min(11,p.getInt("school_grade",2)));
        int dow=now.get(Calendar.DAY_OF_WEEK);
        boolean weekday=dow>=Calendar.MONDAY&&dow<=Calendar.FRIDAY;

        if(completed==today){scheduleNext(context);return;}
        if(!yearComplete && ((weekday&&school>=5)||(!weekday&&weekend>=2))){scheduleNext(context);return;}
        if(yearComplete && (today<=completeDay || grade>=11)){scheduleNext(context);return;}

        int total=school+weekend;
        long lastOpen=p.getLong("school_notify_last_open_day",today);
        long absent=Math.max(0,today-lastOpen);
        long stageDay=p.getLong("school_player_stage_day",Long.MIN_VALUE);
        int stage=p.getInt("school_player_stage",10);

        String title;
        String body;
        boolean offerCall=false,offerSwim=false;
        if(yearComplete){
            title="Нова зима вже чекає";
            body="Попередній рік прожито 7/7. Повернись, щоб почати наступний клас.";
        }else if(total>=6){
            title="До нового року лишився 1 прожитий день";
            body=weekday?"Сьогодні можна закрити останній навчальний день.":"Сьогодні можна прожити останній потрібний вихідний.";
            offerCall=weekday;offerSwim=!weekday;
        }else if(absent>=4){
            title="Твоя зима нікуди не поділась";
            body="Прогрес "+total+"/7 збережено. Сніговик не старіє без тебе — сьогодні можна продовжити.";
        }else if(absent>=2){
            title="Сніжик зберіг тобі місце";
            body="Ти кілька днів не заходив, але нічого не втрачено. Прогрес року: "+total+"/7.";
        }else if(stageDay==today && stage!=10 && stage!=20 && stage!=30 && stage!=31 && stage!=40){
            title="Ти вже почав сьогоднішній день";
            body="Залишився етап: "+stageName(stage)+". Можна продовжити з того самого місця.";
            offerCall=weekday;
        }else if(weekday){
            title="У "+grade+"-А зараз перерва";
            body="Можеш подзвонити малому в школу. День дограєш, коли буде зручно. Прогрес "+total+"/7.";
            offerCall=true;
        }else{
            title="Сьогодні ведемо малого на снігоплавання";
            body="Не вода: холодний сухий басейн зі сніговою крупою. Вихідні "+weekend+"/2.";
            offerSwim=true;
        }

        ensureChannel(context);
        PendingIntent content=PendingIntent.getActivity(context,NOTIFY_ID,new Intent(context,SchoolWeekActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(context,CHANNEL_ID):new Notification.Builder(context);
        b.setSmallIcon(R.drawable.ic_snow_notify).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setContentIntent(content).setAutoCancel(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_REMINDER).setVisibility(Notification.VISIBILITY_PUBLIC);
        if(offerCall){
            PendingIntent call=PendingIntent.getActivity(context,NOTIFY_ID+1,new Intent(context,SchoolCallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            b.addAction(new Notification.Action.Builder(null,"ПОДЗВОНИТИ",call).build());
        }else if(offerSwim){
            PendingIntent swim=PendingIntent.getActivity(context,NOTIFY_ID+2,new Intent(context,SnowSwimActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            b.addAction(new Notification.Action.Builder(null,"НА ПЛАВАННЯ",swim).build());
        }
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(nm!=null)nm.notify(NOTIFY_ID,b.build());
        p.edit().putLong("school_notify_last_sent_day",today).apply();
        scheduleNext(context);
    }

    public static void scheduleNext(Context context){
        SharedPreferences p=context.getSharedPreferences(PREFS,Context.MODE_PRIVATE);
        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if(am==null)return;
        PendingIntent pi=alarmIntent(context);am.cancel(pi);
        if(!p.getBoolean("class2_started",false)||!p.getBoolean("school_notifications_enabled",true))return;
        int school=Math.max(0,Math.min(5,p.getInt("school_year_school_done",0))),weekend=Math.max(0,Math.min(2,p.getInt("school_year_weekend_done",0))),grade=Math.max(2,Math.min(11,p.getInt("school_grade",2)));
        boolean yearComplete=p.getBoolean("school_year_complete",false) || (school>=5&&weekend>=2);if(yearComplete&&grade>=11)return;
        long completeDay=p.getLong("school_year_complete_day",Long.MIN_VALUE),completed=p.getLong("school_player_last_completed_day",Long.MIN_VALUE);Calendar now=Calendar.getInstance();
        for(int offset=0;offset<16;offset++){
            Calendar c=(Calendar)now.clone();c.add(Calendar.DAY_OF_YEAR,offset);int dow=c.get(Calendar.DAY_OF_WEEK);boolean weekday=dow>=Calendar.MONDAY&&dow<=Calendar.FRIDAY;long day=localDayNumber(c);if(day==completed)continue;boolean useful;int hour,minute;
            if(yearComplete){useful=day>completeDay;hour=11;minute=15;}else if(weekday){useful=school<5;hour=12;minute=40;}else{useful=weekend<2;hour=11;minute=30;}
            if(!useful)continue;c.set(Calendar.HOUR_OF_DAY,hour);c.set(Calendar.MINUTE,minute);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);if(c.getTimeInMillis()<=now.getTimeInMillis())continue;
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pi);p.edit().putLong("school_notify_next_at",c.getTimeInMillis()).apply();return;
        }
    }

    private static PendingIntent alarmIntent(Context context){Intent i=new Intent(context,SnowReminderReceiver.class).setAction("com.snowmangame.SCHOOL_REMINDER");return PendingIntent.getBroadcast(context,ALARM_REQ,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private static void ensureChannel(Context context){if(Build.VERSION.SDK_INT<26)return;NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm==null)return;NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"Життя сніговика",NotificationManager.IMPORTANCE_DEFAULT);channel.setDescription("Ненав'язливе нагадування, коли сьогодні можна прожити наступний день сніговика");channel.enableVibration(false);nm.createNotificationChannel(channel);}
    private static String stageName(int stage){switch(stage){case 11:return"перший урок";case 12:return"перерва зі Сніжиком";case 13:return"другий урок";case 14:return"дорога додому";case 15:return"вечеря";case 21:return"вихідна мінігра";default:return"сьогоднішній день";}}
    private static long localDayNumber(Calendar local){GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));utc.clear();utc.set(local.get(Calendar.YEAR),local.get(Calendar.MONTH),local.get(Calendar.DAY_OF_MONTH),0,0,0);return utc.getTimeInMillis()/86400000L;}
}
