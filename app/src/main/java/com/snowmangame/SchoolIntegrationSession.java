package com.snowmangame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/** Restart-safe state for an optional school integration.
 * This namespace never mutates school grade/day progression counters.
 */
public final class SchoolIntegrationSession {
    private static final String ACTIVE="school_integration_active";
    private static final String GRADE="school_integration_active_grade";
    private static final String DAY="school_integration_active_day";
    private static final String CHOICE="school_integration_active_choice";

    private SchoolIntegrationSession() {}

    public static void begin(SharedPreferences p,int grade,int day){
        grade=Math.max(7,Math.min(11,grade));
        day=Math.max(1,Math.min(5,day));
        String memoryKey=SchoolIntegrationContent.memoryKey(grade,day);
        if(p.contains(memoryKey)){clear(p);return;}
        boolean same=p.getBoolean(ACTIVE,false)&&p.getInt(GRADE,-1)==grade&&p.getInt(DAY,-1)==day;
        SharedPreferences.Editor e=p.edit().putBoolean(ACTIVE,true).putInt(GRADE,grade).putInt(DAY,day);
        if(!same)e.putInt(CHOICE,-1);
        e.apply();
    }

    public static boolean pending(SharedPreferences p){
        if(!p.getBoolean(ACTIVE,false))return false;
        int grade=p.getInt(GRADE,-1),day=p.getInt(DAY,-1);
        if(grade<7||grade>11||day<1||day>5){clear(p);return false;}
        if(p.contains(SchoolIntegrationContent.memoryKey(grade,day))){clear(p);return false;}
        return SchoolIntegrationContent.eventFor(grade,day)!=SchoolIntegrationContent.NONE;
    }

    public static Intent resumeIntent(Context c,SharedPreferences p){
        Intent i=new Intent(c,SchoolIntegrationActivity.class);
        i.putExtra("grade",Math.max(7,Math.min(11,p.getInt(GRADE,7))));
        i.putExtra("schoolDay",Math.max(1,Math.min(5,p.getInt(DAY,1))));
        return i;
    }

    public static int savedChoice(SharedPreferences p){return Math.max(-1,Math.min(2,p.getInt(CHOICE,-1)));}
    public static void select(SharedPreferences p,int choice){p.edit().putInt(CHOICE,Math.max(0,Math.min(2,choice))).apply();}

    public static void complete(SharedPreferences p,int grade,int day,int eventId,int choice){
        String key=SchoolIntegrationContent.memoryKey(grade,day);
        p.edit()
            .putString(key,SchoolIntegrationContent.memory(eventId,choice))
            .putInt(key+"_choice",choice)
            .remove(ACTIVE).remove(GRADE).remove(DAY).remove(CHOICE)
            .apply();
    }

    public static void clear(SharedPreferences p){p.edit().remove(ACTIVE).remove(GRADE).remove(DAY).remove(CHOICE).apply();}
}
