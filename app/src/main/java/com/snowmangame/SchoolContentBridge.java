package com.snowmangame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Single bridge used by SchoolWeekActivity for senior content and optional
 * integrations. It deliberately does not mutate school progression keys.
 */
public final class SchoolContentBridge {
    private SchoolContentBridge() {}

    public static boolean senior(int grade){return grade>=7&&grade<=11;}

    public static String theme(int grade,String fallback){
        return senior(grade)?SchoolGradeContent.theme(grade):fallback;
    }

    public static String lessonTitle(int grade,int day,boolean second,String fallback){
        if(!senior(grade))return fallback;
        String s=SchoolGradeContent.lessonTitle(grade,day,second);
        return s==null?fallback:s;
    }

    public static String question(int grade,int day,boolean second,String fallback){
        if(!senior(grade))return fallback;
        String s=SchoolGradeContent.question(grade,day,second);
        return s==null?fallback:s;
    }

    public static String[] options(int grade,int day,boolean second,String[] fallback){
        if(!senior(grade))return fallback;
        String[] s=SchoolGradeContent.options(grade,day,second);
        return s==null||s.length!=3?fallback:s;
    }

    public static int correct(int grade,int day,boolean second,int fallback){
        if(!senior(grade))return fallback;
        int i=SchoolGradeContent.correct(grade,day,second);
        return i<0||i>2?fallback:i;
    }

    public static boolean integrationPending(Context context,int grade,int day){
        if(!senior(grade))return false;
        int event=SchoolIntegrationContent.eventFor(grade,day);
        if(event==SchoolIntegrationContent.NONE)return false;
        SharedPreferences p=context.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
        return !p.contains(SchoolIntegrationContent.memoryKey(grade,day));
    }

    public static void openIntegration(Context context,int grade,int day){
        Intent i=new Intent(context,SchoolIntegrationActivity.class);
        i.putExtra("grade",grade);
        i.putExtra("schoolDay",Math.max(1,Math.min(5,day)));
        context.startActivity(i);
    }
}
