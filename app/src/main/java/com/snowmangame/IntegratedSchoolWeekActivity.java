package com.snowmangame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

/**
 * Live school screen with senior-grade content and optional integration events.
 * The inherited SchoolWeekView remains the single owner of progression/save logic.
 */
public class IntegratedSchoolWeekActivity extends Activity {
    private IntegratedSchoolWeekView schoolView;

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
        schoolView=new IntegratedSchoolWeekView(this);
        setContentView(schoolView);
        NotificationScheduler.onSchoolOpened(this);
    }

    @Override protected void onResume(){
        super.onResume();
        if(schoolView!=null)schoolView.refreshDateIfNeeded();
    }

    static class IntegratedSchoolWeekView extends SchoolWeekActivity.SchoolWeekView {
        boolean launchingIntegration;

        IntegratedSchoolWeekView(Activity c){super(c);}

        @Override String theme(){
            return SchoolContentBridge.theme(grade,super.theme());
        }

        @Override String lessonTitle(boolean second){
            return SchoolContentBridge.lessonTitle(grade,schoolOrdinal(),second,super.lessonTitle(second));
        }

        @Override String question(boolean second){
            return SchoolContentBridge.question(grade,schoolOrdinal(),second,super.question(second));
        }

        @Override String[] options(boolean second){
            return SchoolContentBridge.options(grade,schoolOrdinal(),second,super.options(second));
        }

        @Override int correct(boolean second){
            return SchoolContentBridge.correct(grade,schoolOrdinal(),second,super.correct(second));
        }

        @Override void setStage(int s){
            int from=stage;
            super.setStage(s);
            if(from==LESSON2 && s==HOME)openOptionalIntegration();
        }

        @Override void finishCountedDay(){
            int before=stage;
            super.finishCountedDay();
            if(before!=stage){
                ctx.startActivity(new Intent(ctx,FreeTimeActivity.class));
            }
        }

        void openOptionalIntegration(){
            if(launchingIntegration)return;
            int day=schoolOrdinal();
            if(!SchoolContentBridge.integrationPending(ctx,grade,day))return;
            launchingIntegration=true;
            Intent i=new Intent(ctx,SchoolIntegrationActivity.class);
            i.putExtra("grade",grade);
            i.putExtra("schoolDay",day);
            ctx.startActivity(i);
            postDelayed(new Runnable(){@Override public void run(){launchingIntegration=false;}},700);
        }
    }
}
