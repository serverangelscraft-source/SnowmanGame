package com.snowmangame;

import android.content.SharedPreferences;

final class SchoolProgressionGuard {
    private SchoolProgressionGuard(){}

    static void repair(SharedPreferences p){
        int grade=Math.max(2,Math.min(11,p.getInt("school_grade",2)));
        int school=Math.max(0,Math.min(5,p.getInt("school_year_school_done",0)));
        int weekend=Math.max(0,Math.min(2,p.getInt("school_year_weekend_done",0)));
        boolean complete=school>=5&&weekend>=2;

        SharedPreferences.Editor e=p.edit()
                .putInt("school_grade",grade)
                .putInt("school_year_school_done",school)
                .putInt("school_year_weekend_done",weekend);

        if(complete){
            e.putBoolean("school_year_complete",true);
            if(!p.contains("school_year_complete_day")){
                long last=p.getLong("school_player_last_completed_day",p.getLong("school_clock_last_completed_day",Long.MIN_VALUE));
                if(last!=Long.MIN_VALUE)e.putLong("school_year_complete_day",last);
            }
        }

        if(grade>2 || p.getBoolean("class2_started",false) || p.contains("school_player_stage_day")){
            e.putBoolean("school_first_day_complete",true).putBoolean("school_unlocked",true);
        }
        e.apply();
    }
}
