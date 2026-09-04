package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class SchoolIntegrationActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.rgb(239,247,250));}
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        int grade=Math.max(7,Math.min(11,getIntent().getIntExtra("grade",7)));
        int schoolDay=Math.max(1,Math.min(5,getIntent().getIntExtra("schoolDay",1)));
        setContentView(new IntegrationView(this,grade,schoolDay));
    }

    static class IntegrationView extends View {
        final Activity activity; final SharedPreferences prefs; final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),t=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF[] cards={new RectF(),new RectF(),new RectF()}; final RectF done=new RectF();
        final float d,ts; final int grade,schoolDay,eventId; final String[] choices; float safeTop,safeBottom; int selected=-1;
        IntegrationView(Activity a,int grade,int schoolDay){
            super(a);activity=a;this.grade=grade;this.schoolDay=schoolDay;eventId=SchoolIntegrationContent.eventFor(grade,schoolDay);choices=SchoolIntegrationContent.choices(eventId);prefs=a.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);d=getResources().getDisplayMetrics().density;ts=Math.min(getResources().getDisplayMetrics().scaledDensity,d*1.12f);t.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);setClickable(true);setFocusable(true);setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){@Override public WindowInsets onApplyWindowInsets(View v,WindowInsets i){safeTop=i.getSystemWindowInsetTop();safeBottom=i.getSystemWindowInsetBottom();invalidate();return i;}});requestApplyInsets();
        }
        float dp(float v){return v*d;}float tx(float v){return v*ts;}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient g=new LinearGradient(0,0,0,h,Color.rgb(229,242,249),Color.rgb(250,244,227),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);
            RectF head=new RectF(dp(16),safeTop+dp(14),w-dp(16),safeTop+dp(142));p.setColor(Color.WHITE);c.drawRoundRect(head,dp(26),dp(26),p);
            center(c,"ДОДАТКОВА ПОДІЯ • "+grade+"-А",head.top+dp(25),7,Color.rgb(103,128,139));
            center(c,SchoolIntegrationContent.title(eventId),head.top+dp(62),15,Color.rgb(40,82,105));
            center(c,SchoolIntegrationContent.prompt(eventId),head.top+dp(94),7.5f,Color.rgb(90,119,132));
            center(c,"Без монет і без впливу на прожитий день",head.top+dp(119),6.5f,Color.rgb(126,142,148));
            float top=head.bottom+dp(24),gap=dp(10),left=dp(20),cw=(w-dp(40)-gap*2)/3f;
            float available=Math.max(dp(96),bottom-top-dp(112));float ch=Math.min(dp(158),available);
            for(int i=0;i<3;i++){
                cards[i].set(left+i*(cw+gap),top,left+i*(cw+gap)+cw,top+ch);
                p.setColor(i==selected?Color.rgb(219,240,230):Color.WHITE);c.drawRoundRect(cards[i],dp(20),dp(20),p);
                drawIcon(c,cards[i].centerX(),cards[i].top+ch*.42f,i);
                cardText(c,choices[Math.min(i,choices.length-1)],cards[i]);
            }
            if(selected>=0){done.set(dp(28),bottom-dp(72),w-dp(28),bottom-dp(14));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(done,dp(20),dp(20),p);center(c,"ЗБЕРЕГТИ СПОГАД",done.centerY()+dp(4),8.4f,Color.WHITE);}else done.setEmpty();
        }
        void drawIcon(Canvas c,float x,float y,int i){float r=dp(26);if(eventId==SchoolIntegrationContent.GIFT_WORKSHOP){p.setColor(Color.rgb(231,111,104));c.drawRoundRect(new RectF(x-r,y-r*.7f,x+r,y+r*.7f),dp(8),dp(8),p);p.setColor(Color.WHITE);c.drawRect(x-dp(3),y-r*.7f,x+dp(3),y+r*.7f,p);c.drawRect(x-r,y-dp(3),x+r,y+dp(3),p);}else if(eventId==SchoolIntegrationContent.TECH_PICNIC){p.setColor(Color.rgb(76,139,181));c.drawCircle(x,y,r,p);p.setColor(Color.WHITE);c.drawCircle(x,y,dp(7+i*2),p);}else if(eventId==SchoolIntegrationContent.WINTER_YARD){p.setColor(Color.rgb(94,132,151));c.drawRect(x-r*.1f,y-r,x+r*.1f,y+r,p);p.setColor(Color.rgb(246,218,111));c.drawCircle(x,y-r*.75f,dp(9),p);}else{p.setColor(Color.rgb(229,181,84));Path star=new Path();for(int k=0;k<8;k++){double a=-Math.PI/2+k*Math.PI/4;float rr=(k%2==0)?r:r*.45f;float xx=x+(float)Math.cos(a)*rr,yy=y+(float)Math.sin(a)*rr;if(k==0)star.moveTo(xx,yy);else star.lineTo(xx,yy);}star.close();c.drawPath(star,p);}}
        void center(Canvas c,String s,float y,float size,int color){t.setTextAlign(Paint.Align.CENTER);t.setTextSize(tx(size));t.setColor(color);float max=getWidth()-dp(54);while(t.measureText(s)>max&&t.getTextSize()>tx(5.0f))t.setTextSize(t.getTextSize()-dp(.2f));c.drawText(s,getWidth()/2f,y,t);}
        void cardText(Canvas c,String s,RectF card){t.setTextAlign(Paint.Align.CENTER);t.setTextSize(tx(6.2f));t.setColor(Color.rgb(52,101,127));float max=card.width()-dp(10);while(t.measureText(s)>max&&t.getTextSize()>tx(4.6f))t.setTextSize(t.getTextSize()-dp(.2f));c.drawText(s,card.centerX(),card.bottom-dp(27),t);}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();for(int i=0;i<3;i++)if(cards[i].contains(x,y)){selected=i;invalidate();return true;}if(selected>=0&&done.contains(x,y)){String key=SchoolIntegrationContent.memoryKey(grade,schoolDay);prefs.edit().putString(key,SchoolIntegrationContent.memory(eventId,selected)).putInt(key+"_choice",selected).apply();activity.setResult(Activity.RESULT_OK);activity.finish();return true;}return true;}@Override public boolean performClick(){super.performClick();return true;}
    }
}
