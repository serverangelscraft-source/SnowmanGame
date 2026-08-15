package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class SummerActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(246,244,226));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new SummerView(this));
    }

    static class SummerView extends View {
        final Context ctx;
        final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF action=new RectF();
        final float density,textScale;
        final int year;
        final long started=SystemClock.elapsedRealtime();
        float safeTop,safeBottom;

        SummerView(Context c){
            super(c);ctx=c;
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            year=Math.max(1,Math.min(7,prefs.getInt("life_year",1)));
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);
            setContentDescription("Літня сцена: сніговик тане, а його речі залишаються у гардеробі");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());safeTop=bars.top;safeBottom=bars.bottom;}
                    else{safeTop=insets.getSystemWindowInsetTop();safeBottom=insets.getSystemWindowInsetBottom();}
                    invalidate();return insets;
                }
            });
            requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;}
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        float smooth(float v){v=clamp(v,0,1);return v*v*(3f-2f*v);}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float t=(SystemClock.elapsedRealtime()-started)/1000f;
            drawSummer(c,t);
            postInvalidateOnAnimation();
        }

        void drawSummer(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient sky=new LinearGradient(0,0,0,bottom*.72f,Color.rgb(143,211,244),Color.rgb(251,235,171),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(239,235,194));c.drawRect(0,bottom*.67f,w,h,p);
            p.setColor(Color.rgb(255,214,64));c.drawCircle(w-dp(58),safeTop+dp(67),dp(30),p);
            for(int i=0;i<9;i++){double a=i*Math.PI*2/9;float x=w-dp(58)+(float)Math.cos(a)*dp(46),y=safeTop+dp(67)+(float)Math.sin(a)*dp(46);stroke.setColor(Color.argb(125,255,198,36));stroke.setStrokeWidth(dp(3));c.drawLine(w-dp(58),safeTop+dp(67),x,y,stroke);}

            RectF header=new RectF(dp(14),safeTop+dp(10),w-dp(14),safeTop+dp(111));
            p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(header,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(18));text.setColor(Color.rgb(37,77,98));c.drawText("ЛІТО МІЖ РОКАМИ",header.left+dp(18),header.top+dp(34),text);
            text.setTextSize(tx(9));text.setColor(Color.rgb(95,128,142));c.drawText("Рік "+year+" уже настав, але зимі треба повернутися.",header.left+dp(18),header.top+dp(61),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(132,139,133));c.drawText("Тіло зі снігу тане • пам'ять і речі залишаються",header.left+dp(18),header.bottom-dp(16),text);

            float melt=smooth((t-1.0f)/4.2f),ground=bottom*.64f;
            drawMeltingSnowman(c,w*.29f,ground,dp(42),melt);
            drawPuddle(c,w*.29f,ground+dp(7),melt);

            RectF story=new RectF(w*.50f,safeTop+dp(145),w-dp(18),safeTop+dp(290));
            p.setColor(Color.argb(236,255,255,255));c.drawRoundRect(story,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(14));text.setColor(Color.rgb(46,86,105));c.drawText(melt<.35f?"СОНЦЕ ГРІЄ…":melt<.85f?"СНІГОВИК ТАНЕ":"ДО ЗУСТРІЧІ ВЗИМКУ",story.centerX(),story.top+dp(35),text);
            text.setTextSize(tx(8.2f));text.setColor(Color.rgb(103,129,141));
            c.drawText("Наступної зими його доведеться",story.centerX(),story.top+dp(68),text);
            c.drawText("зібрати знову — вже трохи старшим.",story.centerX(),story.top+dp(90),text);
            text.setTextSize(tx(7.1f));text.setColor(Color.rgb(126,139,142));c.drawText("Кожен новий рік тіло трохи виростає.",story.centerX(),story.bottom-dp(18),text);

            if(t>3.7f)drawWardrobe(c,bottom);
            if(t>5.0f)drawButton(c,bottom);
        }

        void drawMeltingSnowman(Canvas c,float x,float ground,float r,float melt){
            float scale=1f-.58f*melt;
            float sink=dp(30)*melt;
            float br=r*scale,mr=r*.72f*scale,hr=r*.53f*scale;
            float by=ground-br+sink,my=by-(br+mr)*.83f,hy=my-(mr+hr)*.82f;
            drawBall(c,x,by,br);drawBall(c,x,my,mr);drawBall(c,x,hy,hr);
            if(melt<.88f){
                p.setColor(Color.rgb(45,61,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,Math.max(dp(1.5f),hr*.08f),p);c.drawCircle(x+hr*.28f,hy-hr*.14f,Math.max(dp(1.5f),hr*.08f),p);
                Path n=new Path();n.moveTo(x,hy);n.lineTo(x+Math.max(dp(8),hr*.70f),hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(240,116,35));c.drawPath(n,p);
                stroke.setColor(Color.rgb(105,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.60f,my,x-mr*1.35f,my-mr*.26f,stroke);c.drawLine(x+mr*.60f,my,x+mr*1.35f,my-mr*.26f,stroke);
            }
            if(prefs.getBoolean("olx_scarf_owned",false)){
                p.setColor(Color.rgb(69,79,201));
                c.drawRoundRect(new RectF(x-r*.83f,ground+dp(14),x+r*.25f,ground+dp(25)),dp(5),dp(5),p);
                c.drawRoundRect(new RectF(x+r*.10f,ground+dp(18),x+r*.34f,ground+dp(48)),dp(5),dp(5),p);
            }
        }

        void drawBall(Canvas c,float x,float y,float r){
            if(r<dp(2))return;
            RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(199,226,240)},null,Shader.TileMode.CLAMP);
            p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);
            stroke.setColor(Color.argb(70,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,Math.max(0,r-dp(.5f)),stroke);
        }

        void drawPuddle(Canvas c,float x,float y,float melt){
            p.setColor(Color.argb((int)(45+130*melt),113,190,225));
            float ww=dp(28)+dp(88)*melt,hh=dp(8)+dp(19)*melt;c.drawOval(new RectF(x-ww,y-hh,x+ww,y+hh),p);
            p.setColor(Color.argb((int)(20+65*melt),255,255,255));c.drawOval(new RectF(x-ww*.55f,y-hh*.45f,x+ww*.10f,y+hh*.15f),p);
        }

        void drawWardrobe(Canvas c,float bottom){
            float w=getWidth();RectF card=new RectF(dp(18),bottom-dp(242),w-dp(18),bottom-dp(84));
            p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(card,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(13));text.setColor(Color.rgb(45,80,98));c.drawText("ГАРДЕРОБ ПЕРЕЖИВАЄ ЛІТО",card.left+dp(16),card.top+dp(29),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(127,141,146));c.drawText("Речі не треба купувати й відкривати повторно",card.left+dp(16),card.top+dp(51),text);
            drawItem(c,card.left+dp(16),card.top+dp(72),"Шарф • OLX",prefs.getBoolean("olx_scarf_owned",false),Color.rgb(68,79,204));
            drawItem(c,card.left+card.width()*.52f,card.top+dp(72),"Морква від мами",true,Color.rgb(239,118,34));
            drawItem(c,card.left+dp(16),card.top+dp(113),"Руки • ПАЛКА ЧОТКО",true,Color.rgb(113,82,58));
            drawItem(c,card.left+card.width()*.52f,card.top+dp(113),"Жовті санчата",prefs.getBoolean("sled_unlocked",false),Color.rgb(239,186,24));
        }

        void drawItem(Canvas c,float x,float y,String label,boolean owned,int color){
            p.setColor(owned?Color.argb(235,240,248,244):Color.argb(220,241,241,241));RectF r=new RectF(x,y,x+getWidth()*.40f,y+dp(31));c.drawRoundRect(r,dp(12),dp(12),p);
            p.setColor(owned?color:Color.rgb(180,185,188));c.drawCircle(r.left+dp(14),r.centerY(),dp(6),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.2f));text.setColor(owned?Color.rgb(67,101,116):Color.rgb(145,151,154));c.drawText(label,r.left+dp(26),r.centerY()+dp(3),text);
        }

        void drawButton(Canvas c,float bottom){
            float w=getWidth();action.set(dp(22),bottom-dp(70),w-dp(22),bottom-dp(13));
            p.setColor(Color.rgb(42,113,155));c.drawRoundRect(action,dp(20),dp(20),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("ДОЧЕКАТИСЯ ПЕРШОГО СНІГУ",action.centerX(),action.centerY()+dp(4),text);
        }

        void continueToWinter(){
            prefs.edit().putInt("summer_pending_year",0).putInt("summer_count",prefs.getInt("summer_count",0)+1).apply();
            Intent i=new Intent(ctx,MainActivity.class);i.putExtra("skip_summer",true);ctx.startActivity(i);((Activity)ctx).finish();
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            if((e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)&&SystemClock.elapsedRealtime()-started>5000&&action.contains(e.getX(),e.getY())){performClick();continueToWinter();return true;}
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
