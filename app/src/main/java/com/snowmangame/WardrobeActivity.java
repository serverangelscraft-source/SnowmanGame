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

public class WardrobeActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);w.setNavigationBarColor(Color.rgb(238,242,235));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new WardrobeView(this));
    }

    static class WardrobeView extends View {
        final Context ctx;final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF boyBtn=new RectF(),girlBtn=new RectF(),closeBtn=new RectF();final RectF[] outfitBtns=new RectF[8];
        final float density,textScale;float safeTop,safeBottom;int year,character,outfit,mask;
        final String[] names={"Без вишиванки","Київщина","Черкащина","Кіровоградщина","Одещина","Вінниччина","Хмельниччина","Тернопільщина"};
        final String[] notes={"Паличкові руки без тканини","геометрія + рослинний ритм","середньонаддніпрянський ритм","ромби + стилізована квітка","бессарабський ритм","подільська геометрія","дрібний подільський орнамент","контрастний подільський орнамент"};

        WardrobeView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);density=getResources().getDisplayMetrics().density;textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            for(int i=0;i<outfitBtns.length;i++)outfitBtns[i]=new RectF();
            year=Math.max(1,Math.min(7,prefs.getInt("life_year",1)));character=Math.max(0,Math.min(1,prefs.getInt("character_type",0)));
            int earned=(1<<year)-1;mask=prefs.getInt("vyshyvanka_unlocked_mask",earned)|earned;
            outfit=prefs.contains("equipped_vyshyvanka_year")?Math.max(0,Math.min(7,prefs.getInt("equipped_vyshyvanka_year",year))):year;
            prefs.edit().putInt("vyshyvanka_unlocked_mask",mask).putInt("equipped_vyshyvanka_year",outfit).apply();
            setClickable(true);setFocusable(true);setContentDescription("Гардероб сніговика та вишиванки областей України");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){@Override public WindowInsets onApplyWindowInsets(View v,WindowInsets i){safeTop=i.getSystemWindowInsetTop();safeBottom=i.getSystemWindowInsetBottom();invalidate();return i;}});requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;} boolean unlocked(int r){return r==0||(mask&(1<<(r-1)))!=0;}
        String oblastName(){return names[year];}

        @Override protected void onDraw(Canvas c){super.onDraw(c);float w=getWidth(),bottom=getHeight()-safeBottom;
            LinearGradient bg=new LinearGradient(0,0,0,bottom,Color.rgb(238,243,234),Color.rgb(220,230,218),Shader.TileMode.CLAMP);p.setShader(bg);c.drawRect(0,0,w,getHeight(),p);p.setShader(null);
            drawHeader(c,w);drawPreview(c,w);drawGrid(c,w,bottom);drawClose(c,w,bottom);
        }

        void drawHeader(Canvas c,float w){RectF r=new RectF(dp(14),safeTop+dp(9),w-dp(14),safeTop+dp(92));p.setColor(Color.argb(247,255,255,255));c.drawRoundRect(r,dp(23),dp(23),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(16));text.setColor(Color.rgb(49,76,78));c.drawText("ГАРДЕРОБ",r.left+dp(16),r.top+dp(31),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(99,122,119));c.drawText("Зараз: "+oblastName()+" • зима "+year+"/7",r.left+dp(16),r.top+dp(55),text);
            text.setTextSize(tx(6.7f));text.setColor(Color.rgb(128,139,132));c.drawText("Візерунки — ігрова стилізація регіональних традицій",r.left+dp(16),r.bottom-dp(13),text);
            float bw=dp(70),bh=dp(29),gap=dp(6),right=r.right-dp(12);girlBtn.set(right-bw,r.top+dp(12),right,r.top+dp(12)+bh);boyBtn.set(right-bw*2-gap,r.top+dp(12),right-bw-gap,r.top+dp(12)+bh);drawProfilePill(c,boyBtn,0,"Хлопчик");drawProfilePill(c,girlBtn,1,"Дівчинка");
        }
        void drawProfilePill(Canvas c,RectF r,int type,String s){p.setColor(character==type?Color.rgb(47,128,166):Color.rgb(232,240,241));c.drawRoundRect(r,dp(12),dp(12),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(6.5f));text.setColor(character==type?Color.WHITE:Color.rgb(80,111,121));c.drawText(s,r.centerX(),r.centerY()+dp(2.4f),text);}

        void drawPreview(Canvas c,float w){float top=safeTop+dp(103),h=dp(176);RectF r=new RectF(dp(14),top,w-dp(14),top+h);p.setColor(Color.argb(241,255,255,255));c.drawRoundRect(r,dp(23),dp(23),p);
            float x=r.left+r.width()*.28f,s=Math.min(dp(43),r.height()*.25f),by=r.bottom-dp(31),mr=s*.72f,hr=s*.54f,my=by-(s+mr)*.83f,hy=my-(mr+hr)*.83f;drawBall(c,x,by,s);drawBall(c,x,my,mr);drawBall(c,x,hy,hr);drawFace(c,x,hy,hr);stroke.setColor(Color.rgb(106,79,58));stroke.setStrokeWidth(dp(3));float lx=x-mr*.58f,rx=x+mr*.58f,ey=my-mr*.47f;c.drawLine(lx,my,x-mr*1.62f,ey,stroke);c.drawLine(rx,my,x+mr*1.62f,ey,stroke);if(outfit>0){drawSleeve(c,lx,my,x-mr*1.48f,ey,mr*.42f,outfit);drawSleeve(c,rx,my,x+mr*1.48f,ey,mr*.42f,outfit);}if(character==1)drawBow(c,x+hr*.55f,hy-hr*.60f,hr*.20f);else{p.setColor(Color.rgb(54,114,151));c.drawRoundRect(new RectF(x-hr*.48f,hy-hr*.78f,x+hr*.48f,hy-hr*.67f),dp(3),dp(3),p);}
            float tx0=r.left+r.width()*.53f;text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7));text.setColor(Color.rgb(118,130,126));c.drawText(outfit==0?"БАЗОВИЙ ОБРАЗ":(outfit==year?"МІСЦЕВА ВИШИВАНКА":"ВИШИВАНКА З МАНДРІВ"),tx0,r.top+dp(37),text);
            String title=outfit==0?"Без вишиванки":names[outfit];text.setTextSize(tx(15));while(text.measureText(title)>r.right-tx0-dp(15)&&text.getTextSize()>tx(10))text.setTextSize(text.getTextSize()-dp(.35f));text.setColor(Color.rgb(54,82,83));c.drawText(title,tx0,r.top+dp(69),text);
            text.setTextSize(tx(7.4f));text.setColor(Color.rgb(100,120,116));c.drawText(notes[outfit],tx0,r.top+dp(96),text);text.setTextSize(tx(6.6f));text.setColor(Color.rgb(134,141,133));c.drawText("Рукави одягаються поверх паличок-рук",tx0,r.top+dp(120),text);c.drawText("і залишають самі палички видимими на кінцях.",tx0,r.top+dp(140),text);
        }

        void drawGrid(Canvas c,float w,float bottom){float top=safeTop+dp(290),closeTop=bottom-dp(62),gap=dp(6),left=dp(14),right=w-dp(14),avail=Math.max(dp(176),closeTop-top-dp(8)),rowH=(avail-gap*3)/4f,colW=(right-left-gap)/2f;
            for(int i=0;i<8;i++){int row=i/2,col=i%2;float l=left+col*(colW+gap),t=top+row*(rowH+gap);RectF r=outfitBtns[i];r.set(l,t,l+colW,t+rowH);boolean lock=!unlocked(i),active=outfit==i;
                p.setColor(lock?Color.argb(180,229,233,227):(active?Color.rgb(248,254,250):Color.argb(241,255,255,255)));c.drawRoundRect(r,dp(15),dp(15),p);if(active){stroke.setColor(Color.rgb(54,142,116));stroke.setStrokeWidth(dp(2));c.drawRoundRect(r,dp(15),dp(15),stroke);}
                text.setTextAlign(Paint.Align.LEFT);String nm=names[i];text.setTextSize(tx(7.4f));while(text.measureText(nm)>r.width()-dp(16)&&text.getTextSize()>tx(5.1f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(lock?Color.rgb(145,150,143):Color.rgb(63,92,91));c.drawText(nm,r.left+dp(8),r.top+dp(17),text);
                text.setTextSize(tx(5.8f));text.setColor(lock?Color.rgb(154,157,151):(i==year?Color.rgb(44,133,101):Color.rgb(120,133,128)));c.drawText(lock?"Ще не відкрита":(i==year?"МІСЦЕВА • ВІДКРИТО":"ВІДКРИТО"),r.left+dp(8),r.top+dp(34),text);
                RectF band=new RectF(r.left+dp(8),r.bottom-dp(17),r.right-dp(8),r.bottom-dp(7));if(i>0&&!lock)drawPatternBand(c,band,i);else{p.setColor(Color.rgb(231,236,231));c.drawRoundRect(band,dp(3),dp(3),p);}
            }
        }

        void drawClose(Canvas c,float w,float bottom){closeBtn.set(dp(20),bottom-dp(55),w-dp(20),bottom-dp(9));p.setColor(Color.rgb(45,105,119));c.drawRoundRect(closeBtn,dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.WHITE);c.drawText("НАЗАД ДО СНІГОВИКА",closeBtn.centerX(),closeBtn.centerY()+dp(3),text);}

        void drawBall(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);}
        void drawFace(Canvas c,float x,float y,float r){p.setColor(Color.rgb(44,58,66));c.drawCircle(x-r*.27f,y-r*.15f,r*.075f,p);c.drawCircle(x+r*.27f,y-r*.15f,r*.075f,p);if(character==1){stroke.setColor(Color.rgb(56,76,87));stroke.setStrokeWidth(dp(1.2f));c.drawLine(x-r*.35f,y-r*.22f,x-r*.43f,y-r*.30f,stroke);c.drawLine(x+r*.35f,y-r*.22f,x+r*.43f,y-r*.30f,stroke);}Path n=new Path();n.moveTo(x,y);n.lineTo(x+r*.68f,y+r*.06f);n.lineTo(x,y+r*.13f);n.close();p.setColor(Color.rgb(240,118,34));c.drawPath(n,p);}
        void drawBow(Canvas c,float x,float y,float s){p.setColor(Color.rgb(199,64,83));Path a=new Path();a.moveTo(x,y);a.lineTo(x-s*1.5f,y-s*.8f);a.lineTo(x-s*1.3f,y+s*.8f);a.close();c.drawPath(a,p);Path b=new Path();b.moveTo(x,y);b.lineTo(x+s*1.5f,y-s*.8f);b.lineTo(x+s*1.3f,y+s*.8f);b.close();c.drawPath(b,p);c.drawCircle(x,y,s*.45f,p);}

        void drawSleeve(Canvas c,float x1,float y1,float x2,float y2,float width,int region){float mx=(x1+x2)/2,my=(y1+y2)/2,len=(float)Math.hypot(x2-x1,y2-y1),ang=(float)Math.toDegrees(Math.atan2(y2-y1,x2-x1));c.save();c.rotate(ang,mx,my);RectF cloth=new RectF(mx-len*.47f,my-width/2,mx+len*.47f,my+width/2);p.setColor(Color.rgb(250,249,241));c.drawRoundRect(cloth,width*.28f,width*.28f,p);stroke.setColor(Color.argb(75,99,111,105));stroke.setStrokeWidth(dp(1));c.drawRoundRect(cloth,width*.28f,width*.28f,stroke);RectF band=new RectF(cloth.right-len*.28f,cloth.top+width*.14f,cloth.right-dp(2),cloth.bottom-width*.14f);drawPatternBand(c,band,region);c.restore();}

        void drawPatternBand(Canvas c,RectF r,int region){int a,b,accent;switch(region){case 1:a=Color.rgb(177,49,57);b=Color.rgb(48,63,70);accent=Color.rgb(83,132,91);break;case 2:a=Color.rgb(190,48,54);b=Color.rgb(58,58,61);accent=Color.rgb(222,107,70);break;case 3:a=Color.rgb(146,69,72);b=Color.rgb(96,101,101);accent=Color.rgb(196,133,94);break;case 4:a=Color.rgb(61,105,157);b=Color.rgb(161,53,72);accent=Color.rgb(205,143,71);break;case 5:a=Color.rgb(119,38,47);b=Color.rgb(42,43,43);accent=Color.rgb(184,78,57);break;case 6:a=Color.rgb(45,47,46);b=Color.rgb(154,49,53);accent=Color.rgb(184,121,74);break;default:a=Color.rgb(37,38,38);b=Color.rgb(112,35,43);accent=Color.rgb(190,91,52);break;}p.setColor(Color.rgb(248,246,236));c.drawRoundRect(r,dp(2),dp(2),p);float h=r.height(),step=Math.max(dp(7),h*.72f);int n=Math.max(2,(int)(r.width()/step)+1);for(int i=0;i<n;i++){float x=r.left+i*step+step*.45f,y=r.centerY();if(region==2||region==4){p.setColor(i%2==0?a:b);c.drawRect(x-step*.09f,r.top+dp(1),x+step*.09f,r.bottom-dp(1),p);c.drawRect(x-step*.27f,y-h*.09f,x+step*.27f,y+h*.09f,p);}else{Path d=new Path();d.moveTo(x,y-h*.38f);d.lineTo(x+step*.30f,y);d.lineTo(x,y+h*.38f);d.lineTo(x-step*.30f,y);d.close();p.setColor(i%2==0?a:b);c.drawPath(d,p);p.setColor(accent);c.drawCircle(x,y,Math.max(dp(1),h*.10f),p);}}if(region>=5){p.setColor(b);c.drawRect(r.left,r.top,r.right,r.top+Math.max(dp(1),h*.08f),p);c.drawRect(r.left,r.bottom-Math.max(dp(1),h*.08f),r.right,r.bottom,p);}}

        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();
            if(boyBtn.contains(x,y)){character=0;prefs.edit().putInt("character_type",0).putBoolean("character_selected",true).apply();invalidate();return true;}
            if(girlBtn.contains(x,y)){character=1;prefs.edit().putInt("character_type",1).putBoolean("character_selected",true).apply();invalidate();return true;}
            for(int i=0;i<outfitBtns.length;i++)if(outfitBtns[i].contains(x,y)){if(unlocked(i)){outfit=i;prefs.edit().putInt("equipped_vyshyvanka_year",outfit).apply();invalidate();}return true;}
            if(closeBtn.contains(x,y)){((Activity)ctx).finish();return true;}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
