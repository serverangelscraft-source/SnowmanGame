package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class ResumeActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        SharedPreferences p=getSharedPreferences("snowman_game", Context.MODE_PRIVATE);
        SchoolProgressionGuard.repair(p);

        Intent next;
        boolean schoolWeekStarted=p.getBoolean("class2_started",false)
                || p.contains("school_player_stage_day")
                || p.contains("school_year_school_done")
                || p.contains("school_year_weekend_done")
                || p.getInt("school_grade",2)>2;

        boolean schoolUnlocked=p.getBoolean("school_unlocked",false)
                || p.getBoolean("school_first_day_complete",false)
                || p.getInt("life_year",1)>=7;

        boolean hasProgress=p.getInt("life_year",1)>1
                || p.getInt("coins",0)>0
                || p.getInt("best_score",0)>0
                || p.contains("reward_day")
                || p.contains("year_builds_1")
                || p.contains("year_builds_2")
                || p.contains("year_builds_3")
                || p.contains("year_builds_4")
                || p.contains("year_builds_5")
                || p.contains("year_builds_6")
                || p.contains("year_builds_7");

        if(schoolWeekStarted) next=new Intent(this,SchoolWeekActivity.class);
        else if(schoolUnlocked) next=new Intent(this,SchoolActivity.class);
        else if(hasProgress) next=new Intent(this,MainActivity.class);
        else next=new Intent(this,IntroActivity.class);

        startActivity(next);
        finish();
    }
}
