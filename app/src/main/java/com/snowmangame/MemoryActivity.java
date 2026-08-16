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

public class MemoryActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(238,235,222));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new MemoryView(this));
    }

    static class MemoryView extends View {
        final Context ctx;
        final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),text=new Paint(Paint.ANTI_ALIAS_FLAG),stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF closeBtn=new RectF();
        final RectF[] slots=new RectF[8];
        final float density,textScale;
        float safeTop,safeBottom;
        int selected=-1;
        final String[] names={"Морква від мами","Шарф • OLX","ПАЛКА ЧОТКО","Жовті санчата","Перший квиток","Синя рукавичка","Перший друг","Поїздка Uklon"};

        MemoryView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            for(int i=0;i<slots.length;i++)slots[i]=new RectF();
            setClickable(true);setFocusable(true);setContentDescription("Кімната пам’яті сніговика");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());safeTop=bars.top;safeBottom=bars.bottom;}
                    else{safeTop=insets.getSystemWindowInsetTop();safeBottom=insets.getSystemWindowInsetBottom();}
                    invalidate();return insets;
                }
            });requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;} float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        int year(){return Math.max(1,Math.min(7,prefs.getInt("life_year",1)));}
        int lived(){return Math.max(prefs.getInt("winters_lived",0),Math.max(0,year()-1));}
        boolean owned(int i){
            if(i==0||i==2)return true;
            if(i==1)return prefs.getBoolean("olx_scarf_owned",false);
            if(i==3)return prefs.getBoolean("sled_unlocked",false);
            if(i==4)return lived()>=1||year()>=2;
            if(i==5)return prefs.getBoolean("year2_mitten_found",false);
            if(i==6)return prefs.getBoolean("year3_friend_met",false);
            return prefs.getInt("uklon_rides",0)>0;
        }

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);drawRoom(c);drawHeader(c);drawCore(c);drawShelves(c);drawDetail(c);drawClose(c);
        }

        void drawRoom(Canvas c){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient wall=new LinearGradient(0,0,0,bottom,Color.rgb(243,239,223),Color.rgb(225,218,193),Shader.TileMode.CLAMP);p.setShader(wall);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(191,161,121));c.drawRect(0,bottom-dp(72),w,h,p);
            for(int i=0;i<9;i++){p.setColor(i%2==0?Color.argb(45,111,78,52):Color.argb(20,111,78,52));c.drawRect(i*w/9f,bottom-dp(72),(i+1)*w/9f,h,p);}
            RectF win=new RectF(w-dp(112),safeTop+dp(126),w-dp(18),safeTop+dp(255));p.setColor(Color.rgb(191,225,241));c.drawRoundRect(win,dp(9),dp(9),p);stroke.setColor(Color.WHITE);stroke.setStrokeWidth(dp(4));c.drawLine(win.centerX(),win.top,win.centerX(),win.bottom,stroke);c.drawLine(win.left,win.centerY(),win.right,win.centerY(),stroke);
            p.setColor(Color.argb(160,255,255,255));for(int i=0;i<11;i++)c.drawCircle(win.left+dp(8+(i*17)%82),win.top+dp(10+(i*29)%111),dp(1.5f+(i%2)),p);
        }

        void drawHeader(Canvas c){
            float w=getWidth(),top=safeTop+dp(10);RectF r=new RectF(dp(14),top,w-dp(14),top+dp(102));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(17));text.setColor(Color.rgb(56,77,84));c.drawText("КІМНАТА ПАМ’ЯТІ",r.left+dp(17),r.top+dp(34),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(112,126,128));c.drawText("Те, що переживає літо разом із живою сніжинкою",r.left+dp(17),r.top+dp(59),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(135,139,132));c.drawText("Торкнися речі, щоб згадати її історію",r.left+dp(17),r.bottom-dp(15),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(10));text.setColor(Color.rgb(51,119,151));c.drawText("ЗИМ "+Math.min(7,lived())+"/7",r.right-dp(17),r.top+dp(34),text);
        }

        void drawCore(Canvas c){
            float x=dp(63),y=safeTop+dp(170),r=dp(15);RadialGradient halo=new RadialGradient(x,y,r*3,new int[]{Color.argb(105,97,199,247),Color.argb(0,97,199,247)},null,Shader.TileMode.CLAMP);p.setShader(halo);c.drawCircle(x,y,r*3,p);p.setShader(null);
            stroke.setColor(Color.rgb(70,163,210));stroke.setStrokeWidth(dp(2));for(int i=0;i<6;i++){double a=i*Math.PI/3;float ex=x+(float)Math.cos(a)*r,ey=y+(float)Math.sin(a)*r;c.drawLine(x,y,ex,ey,stroke);}p.setColor(Color.WHITE);c.drawCircle(x,y,dp(4),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(8));text.setColor(Color.rgb(79,111,125));c.drawText("Жива сніжинка",x+dp(28),y-dp(2),text);text.setTextSize(tx(6.8f));text.setColor(Color.rgb(125,137,139));c.drawText("пам’ятає всі зими",x+dp(28),y+dp(16),text);
        }

        void drawShelves(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            float left=dp(18),right=w-dp(18),top=safeTop+dp(285),shelfGap=dp(142),colW=(right-left)/4f;
            for(int row=0;row<2;row++){
                float sy=top+row*shelfGap+dp(88);p.setColor(Color.rgb(145,104,67));c.drawRoundRect(new RectF(left-dp(3),sy,right+dp(3),sy+dp(10)),dp(4),dp(4),p);p.setColor(Color.argb(55,72,48,32));c.drawRect(left,sy+dp(10),right,sy+dp(16),p);
                for(int col=0;col<4;col++){
                    int i=row*4+col;float cx=left+colW*(col+.5f),cy=sy-dp(43);slots[i].set(cx-colW*.45f,cy-dp(49),cx+colW*.45f,sy+dp(28));
                    if(owned(i)){drawObject(c,i,cx,cy);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(6.2f));text.setColor(Color.rgb(81,91,91));drawLabel(c,names[i],cx,sy+dp(28),colW*.90f);}
                    else{p.setColor(Color.argb(60,92,96,94));c.drawCircle(cx,cy,dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(6.1f));text.setColor(Color.rgb(143,143,137));c.drawText("ще не спогад",cx,sy+dp(28),text);}
                    if(selected==i&&owned(i)){stroke.setColor(Color.rgb(65,145,181));stroke.setStrokeWidth(dp(2));c.drawRoundRect(slots[i],dp(14),dp(14),stroke);}
                }
            }
            if(bottom<top+shelfGap*2+dp(40)){} // keeps layout stable on compact screens
        }

        void drawLabel(Canvas c,String s,float cx,float y,float maxW){
            if(text.measureText(s)<=maxW){c.drawText(s,cx,y,text);return;}String[] words=s.split(" ");String a="",b="";for(String word:words){String t=a.length()==0?word:a+" "+word;if(text.measureText(t)<=maxW||a.length()==0)a=t;else b+=b.length()==0?word:" "+word;}c.drawText(a,cx,y-dp(3),text);c.drawText(b,cx,y+dp(10),text);
        }

        void drawObject(Canvas c,int i,float x,float y){
            if(i==0){Path n=new Path();n.moveTo(x-dp(16),y-dp(6));n.lineTo(x+dp(24),y);n.lineTo(x-dp(16),y+dp(7));n.close();p.setColor(Color.rgb(240,118,35));c.drawPath(n,p);p.setColor(Color.rgb(77,149,77));c.drawOval(new RectF(x-dp(22),y-dp(18),x-dp(10),y-dp(3)),p);}
            else if(i==1){p.setColor(Color.rgb(68,79,204));c.drawRoundRect(new RectF(x-dp(31),y-dp(7),x+dp(28),y+dp(6)),dp(5),dp(5),p);c.drawRoundRect(new RectF(x+dp(15),y,x+dp(28),y+dp(31)),dp(5),dp(5),p);}
            else if(i==2){stroke.setColor(Color.rgb(113,82,58));stroke.setStrokeWidth(dp(5));c.drawLine(x-dp(29),y+dp(13),x+dp(29),y-dp(12),stroke);stroke.setStrokeWidth(dp(3));c.drawLine(x+dp(12),y-dp(5),x+dp(24),y-dp(25),stroke);}
            else if(i==3){stroke.setColor(Color.rgb(96,104,109));stroke.setStrokeWidth(dp(3));c.drawArc(new RectF(x-dp(36),y+dp(4),x+dp(36),y+dp(25)),5,165,false,stroke);p.setColor(Color.rgb(247,198,34));c.drawRoundRect(new RectF(x-dp(31),y-dp(16),x+dp(31),y+dp(5)),dp(6),dp(6),p);}
            else if(i==4){p.setColor(Color.rgb(246,239,209));c.drawRoundRect(new RectF(x-dp(30),y-dp(20),x+dp(30),y+dp(20)),dp(7),dp(7),p);stroke.setColor(Color.rgb(91,132,151));stroke.setStrokeWidth(dp(2));c.drawLine(x-dp(7),y-dp(17),x-dp(7),y+dp(17),stroke);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(71,105,124));c.drawText("КВИТОК",x+dp(8),y+dp(3),text);}
            else if(i==5){drawMitten(c,x,y,.95f);if(prefs.getBoolean("year2_mitten_returned",false)){p.setColor(Color.rgb(55,137,194));c.drawCircle(x+dp(23),y-dp(18),dp(8),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.WHITE);c.drawText("✓",x+dp(23),y-dp(15),text);}}
            else if(i==6){drawFriendFace(c,x,y);}
            else{p.setColor(Color.rgb(42,44,47));c.drawRoundRect(new RectF(x-dp(33),y-dp(13),x+dp(33),y+dp(13)),dp(8),dp(8),p);p.setColor(Color.rgb(252,190,24));c.drawRoundRect(new RectF(x-dp(28),y-dp(8),x-dp(5),y+dp(6)),dp(4),dp(4),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(5.5f));text.setColor(Color.rgb(35,38,40));c.drawText("U",x-dp(16),y+dp(1),text);p.setColor(Color.rgb(30,32,34));c.drawCircle(x-dp(21),y+dp(14),dp(8),p);c.drawCircle(x+dp(22),y+dp(14),dp(8),p);}
        }

        void drawMitten(Canvas c,float x,float y,float s){p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-dp(11)*s,y-dp(20)*s,x+dp(11)*s,y+dp(10)*s),dp(10)*s,dp(10)*s,p);c.save();c.rotate(-32,x+dp(8)*s,y);c.drawRoundRect(new RectF(x+dp(4)*s,y-dp(3)*s,x+dp(22)*s,y+dp(8)*s),dp(6)*s,dp(6)*s,p);c.restore();p.setColor(Color.rgb(43,113,169));c.drawRoundRect(new RectF(x-dp(13)*s,y+dp(7)*s,x+dp(13)*s,y+dp(19)*s),dp(4)*s,dp(4)*s,p);}
        void drawFriendFace(Canvas c,float x,float y){p.setColor(Color.rgb(245,251,254));c.drawCircle(x,y,dp(25),p);stroke.setColor(Color.rgb(185,218,234));stroke.setStrokeWidth(dp(1.5f));c.drawCircle(x,y,dp(25),stroke);p.setColor(Color.rgb(48,62,71));c.drawCircle(x-dp(8),y-dp(5),dp(2.3f),p);c.drawCircle(x+dp(8),y-dp(5),dp(2.3f),p);Path n=new Path();n.moveTo(x,y);n.lineTo(x+dp(16),y+dp(3));n.lineTo(x,y+dp(5));n.close();p.setColor(Color.rgb(240,117,34));c.drawPath(n,p);p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-dp(22),y+dp(17),x+dp(22),y+dp(26)),dp(4),dp(4),p);}

        void drawDetail(Canvas c){
            if(selected<0||!owned(selected))return;float w=getWidth(),bottom=getHeight()-safeBottom;RectF r=new RectF(dp(20),bottom-dp(165),w-dp(20),bottom-dp(82));p.setColor(Color.argb(248,255,255,255));c.drawRoundRect(r,dp(22),dp(22),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10.5f));text.setColor(Color.rgb(61,82,88));c.drawText(names[selected],r.centerX(),r.top+dp(29),text);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(110,128,132));c.drawText(detail(selected),r.centerX(),r.top+dp(54),text);text.setTextSize(tx(6.6f));text.setColor(Color.rgb(137,142,137));c.drawText("Цей спогад не тане разом із тілом.",r.centerX(),r.bottom-dp(12),text);
        }

        String detail(int i){
            if(i==0)return"Мама принесла першу моркву — з неї почалося обличчя.";
            if(i==1)return"Знайдений поруч шарф став особистою річчю на багато зим.";
            if(i==2)return"Перші руки й перший жарт про ПАЛКА ЧОТКО.";
            if(i==3)return"Перший транспорт: жовті санчата до вокзалу.";
            if(i==4)return"Перший квиток довів, що за двором є більший світ.";
            if(i==5)return prefs.getBoolean("year2_mitten_returned",false)?"Знайдена у Зимі 2 й повернена Сніжику у Зимі 3.":"Знайдена у Зимі 2. Власник ще десь поруч.";
            if(i==6)return"Сніжик — перший друг і перша спільна гра у сніжки.";
            return"Коли маршрут став довшим, з’явилась перша поїздка автомобілем.";
        }

        void drawClose(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;closeBtn.set(dp(22),bottom-dp(68),w-dp(22),bottom-dp(12));p.setColor(Color.rgb(64,103,119));c.drawRoundRect(closeBtn,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.8f));text.setColor(Color.WHITE);c.drawText("ПОВЕРНУТИСЯ ДО ЗИМИ",closeBtn.centerX(),closeBtn.centerY()+dp(4),text);}

        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(closeBtn.contains(x,y)){((Activity)ctx).finish();return true;}for(int i=0;i<slots.length;i++)if(slots[i].contains(x,y)&&owned(i)){selected=i;invalidate();return true;}return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
