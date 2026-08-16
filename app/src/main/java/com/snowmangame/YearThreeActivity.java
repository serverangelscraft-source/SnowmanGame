package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class YearThreeActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(239,248,252));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new YearThreeView(this));
    }

    static class YearThreeView extends View {
        static final int INTRO=0, TRACKS=1, RETURN_MITTEN=2, SNOWBALLS=3, DONE=4;
        final Context ctx;
        final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), text=new Paint(Paint.ANTI_ALIAS_FLAG), stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF action=new RectF(), friendHand=new RectF(), mittenRect=new RectF(), windowRect=new RectF();
        final RectF[] trackCards={new RectF(),new RectF(),new RectF()};
        final RectF[] targets={new RectF(),new RectF(),new RectF()};
        final float density,textScale;
        final Vibrator vibrator;
        int stage,hits,mistakes;
        float safeTop,safeBottom,mittenX=Float.NaN,mittenY=Float.NaN;
        boolean draggingMitten;
        long stageStart=SystemClock.elapsedRealtime();
        String hint="";

        YearThreeView(Context c){
            super(c);ctx=c;
            prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.16f);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            stage=Math.max(INTRO,Math.min(DONE,prefs.getInt("year3_stage",INTRO)));
            hits=Math.max(0,prefs.getInt("year3_snowball_hits",0));
            mistakes=Math.max(0,prefs.getInt("year3_mistakes",0));
            if(prefs.getBoolean("year3_story_complete",false))stage=DONE;
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));
            stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);
            setContentDescription("Зима 3: знайти власника синьої рукавички та пограти у сніжки");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){
                @Override public WindowInsets onApplyWindowInsets(View v,WindowInsets insets){
                    if(Build.VERSION.SDK_INT>=30){Insets bars=insets.getInsets(WindowInsets.Type.systemBars());safeTop=bars.top;safeBottom=bars.bottom;}
                    else{safeTop=insets.getSystemWindowInsetTop();safeBottom=insets.getSystemWindowInsetBottom();}
                    invalidate();return insets;
                }
            });
            requestApplyInsets();
        }

        float dp(float v){return v*density;}
        float tx(float v){return v*textScale;}
        float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
        float smooth(float v){v=clamp(v,0,1);return v*v*(3f-2f*v);}
        float mix(float a,float b,float t){return a+(b-a)*t;}
        float dist(float x1,float y1,float x2,float y2){return(float)Math.hypot(x1-x2,y1-y2);}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,80));else vibrator.vibrate(ms);}
        void next(int s){stage=s;stageStart=SystemClock.elapsedRealtime();prefs.edit().putInt("year3_stage",stage).apply();hint="";invalidate();}

        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float t=(SystemClock.elapsedRealtime()-stageStart)/1000f;
            drawWorld(c,t);
            if(stage==INTRO)drawIntro(c,t);
            else if(stage==TRACKS)drawTracks(c,t);
            else if(stage==RETURN_MITTEN)drawReturn(c,t);
            else if(stage==SNOWBALLS)drawSnowballs(c,t);
            else drawDone(c,t);
            postInvalidateOnAnimation();
        }

        void drawWorld(Canvas c,float t){
            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;
            LinearGradient sky=new LinearGradient(0,safeTop,0,bottom*.70f,Color.rgb(145,207,241),Color.rgb(231,247,253),Shader.TileMode.CLAMP);
            p.setShader(sky);c.drawRect(0,0,w,h,p);p.setShader(null);
            p.setColor(Color.rgb(244,250,253));c.drawRect(0,bottom*.67f,w,h,p);
            p.setColor(Color.argb(230,255,255,255));c.drawOval(new RectF(-w*.30f,bottom*.54f,w*.62f,bottom*.76f),p);c.drawOval(new RectF(w*.38f,bottom*.56f,w*1.30f,bottom*.79f),p);
            for(int i=0;i<22;i++){float x=(i*83f+31f+(float)Math.sin(t*.55f+i)*dp(5))%Math.max(1,w),y=(i*67f+t*dp(10+i%3))%Math.max(dp(130),bottom*.63f);p.setColor(Color.argb(105+(i%3)*32,255,255,255));c.drawCircle(x,y,dp(1+(i%3)*.35f),p);}
            drawPines(c,bottom);
        }

        void drawPines(Canvas c,float bottom){
            float w=getWidth();
            for(int i=0;i<5;i++){
                float x=w*(.05f+i*.23f),ground=bottom*.68f,s=dp(20+(i%3)*5);
                p.setColor(Color.argb(105,62,111,128));Path tr=new Path();tr.moveTo(x,ground-s*3.2f);tr.lineTo(x-s,ground);tr.lineTo(x+s,ground);tr.close();c.drawPath(tr,p);
            }
        }

        void header(Canvas c,String title,String sub,String step){
            float w=getWidth(),top=safeTop+dp(10);RectF r=new RectF(dp(14),top,w-dp(14),top+dp(111));
            p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.3f));text.setColor(Color.rgb(114,136,146));c.drawText("ЗИМА 3/7 • ПУСТУН",r.left+dp(17),r.top+dp(20),text);
            text.setTextSize(tx(17));text.setColor(Color.rgb(32,72,94));c.drawText(title,r.left+dp(17),r.top+dp(49),text);
            text.setTextSize(tx(8.1f));text.setColor(Color.rgb(89,124,142));c.drawText(sub,r.left+dp(17),r.top+dp(74),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(7));text.setColor(Color.rgb(123,143,153));c.drawText(step+" • помилки "+mistakes,r.right-dp(17),r.bottom-dp(14),text);
        }

        void drawIntro(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            header(c,"Чия це рукавичка?","На синій тканині — знайомий знак сніжинки","КРОК 1/4");
            drawHero(c,w*.23f,bottom*.70f,dp(33),.25f);
            drawMitten(c,w*.47f,bottom*.57f,1.05f);
            drawFootprints(c,w*.61f,bottom*.58f,w*.92f,bottom*.43f,6,0);
            RectF bubble=new RectF(dp(24),safeTop+dp(137),w-dp(24),safeTop+dp(235));p.setColor(Color.argb(240,255,255,255));c.drawRoundRect(bubble,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11.2f));text.setColor(Color.rgb(47,84,105));c.drawText("«Вона чекала на власника цілу зиму.»",bubble.centerX(),bubble.top+dp(35),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(98,128,144));c.drawText("Поряд з’явилися свіжі сліди. Один із них має той самий знак.",bubble.centerX(),bubble.top+dp(63),text);
            button(c,"ЙТИ ПО СЛІДАХ",true);
        }

        void drawTracks(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            header(c,"Три сліди","Знайди доріжку зі знаком, як на рукавичці","КРОК 2/4");
            float left=dp(18),right=w-dp(18),gap=dp(9),top=safeTop+dp(152),hh=Math.min(dp(315),bottom-top-dp(95)),ww=(right-left-gap*2)/3f;
            for(int i=0;i<3;i++){
                RectF r=trackCards[i];r.set(left+i*(ww+gap),top,left+i*(ww+gap)+ww,top+hh);
                p.setColor(Color.argb(222,255,255,255));c.drawRoundRect(r,dp(20),dp(20),p);
                float sx=r.centerX(),sy=r.bottom-dp(34),ey=r.top+dp(75);drawFootprints(c,sx,sy,sx+(i-1)*dp(12),ey,7,i);
                text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(16));text.setColor(Color.rgb(47,100,132));c.drawText(i==0?"A":i==1?"B":"C",r.centerX(),r.top+dp(34),text);
                if(i==1){drawSnowflakeMark(c,r.centerX(),r.top+dp(62),dp(9),1f);}
            }
            if(hint.length()>0){text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.3f));text.setColor(Color.rgb(54,98,120));c.drawText(hint,w/2,bottom-dp(55),text);}
        }

        void drawReturn(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.71f;
            header(c,"Знайшли!","У малого сніговика на руці — друга така сама рукавичка","КРОК 3/4");
            drawHero(c,w*.20f,ground,dp(30),.65f);
            drawFriend(c,w*.73f,ground,dp(29),.25f,true);
            if(Float.isNaN(mittenX)){mittenX=w*.27f;mittenY=ground-dp(150);}
            drawMitten(c,mittenX,mittenY,.88f);
            mittenRect.set(mittenX-dp(32),mittenY-dp(38),mittenX+dp(34),mittenY+dp(38));
            friendHand.set(w*.73f-dp(72),ground-dp(105),w*.73f-dp(20),ground-dp(50));
            stroke.setColor(Color.rgb(57,148,195));stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(5)},0));c.drawRoundRect(friendHand,dp(12),dp(12),stroke);stroke.setPathEffect(null);
            RectF bubble=new RectF(dp(23),safeTop+dp(143),w-dp(23),safeTop+dp(237));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(bubble,dp(21),dp(21),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10.8f));text.setColor(Color.rgb(49,83,102));c.drawText("Сніжик: «Я думав, вона загубилася назавжди!»",bubble.centerX(),bubble.top+dp(34),text);
            text.setTextSize(tx(8));text.setColor(Color.rgb(95,127,143));c.drawText("Перетягни синю рукавичку до його вільної руки.",bubble.centerX(),bubble.top+dp(61),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(118,139,149));c.drawText("Рукавичка стане спогадом, а не частиною твого декору.",bubble.centerX(),bubble.top+dp(82),text);
            if(hint.length()>0){text.setTextSize(tx(8));text.setColor(Color.rgb(52,118,91));c.drawText(hint,w/2,bottom-dp(42),text);}
        }

        void drawSnowballs(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.72f;
            header(c,"Перша спільна пустощі","Влуч у 5 снігових мішеней, але не бий у вікно","КРОК 4/4");
            drawHero(c,w*.18f,ground,dp(28),.75f);drawFriend(c,w*.31f,ground,dp(25),.75f,false);
            drawHouse(c,w*.72f,ground);
            float[] baseX={w*.50f,w*.68f,w*.85f};float[] baseY={ground-dp(58),ground-dp(116),ground-dp(70)};
            for(int i=0;i<3;i++){
                float x=baseX[i]+(float)Math.sin(t*(1.1f+i*.23f)+i)*dp(12),y=baseY[i]+(float)Math.cos(t*.8f+i)*dp(6);targets[i].set(x-dp(25),y-dp(25),x+dp(25),y+dp(25));drawTarget(c,x,y,dp(21));
            }
            RectF score=new RectF(dp(22),safeTop+dp(143),w-dp(22),safeTop+dp(216));p.setColor(Color.argb(241,255,255,255));c.drawRoundRect(score,dp(21),dp(21),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(14));text.setColor(Color.rgb(42,87,111));c.drawText("СНІЖКИ  "+hits+"/5",score.centerX(),score.top+dp(31),text);
            text.setTextSize(tx(7.8f));text.setColor(Color.rgb(103,130,144));c.drawText(hint.length()>0?hint:"Тицяй по круглих мішенях — вікно лишаємо цілим.",score.centerX(),score.bottom-dp(15),text);
        }

        void drawDone(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.72f;
            header(c,"Перший друг","Рукавичка повернулась додому, а зима стала веселішою","ГОТОВО");
            drawHero(c,w*.38f,ground,dp(31),1f);drawFriend(c,w*.62f,ground,dp(29),1f,false);
            stroke.setColor(Color.rgb(105,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(w*.38f+dp(20),ground-dp(97),w*.50f,ground-dp(120),stroke);c.drawLine(w*.62f-dp(18),ground-dp(93),w*.50f,ground-dp(120),stroke);
            p.setColor(Color.rgb(246,203,47));c.drawCircle(w*.50f,ground-dp(120),dp(8),p);
            RectF card=new RectF(dp(22),safeTop+dp(141),w-dp(22),safeTop+dp(265));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(card,dp(23),dp(23),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(44,83,102));c.drawText("СПОГАД ВІДКРИТО: СНІЖИК",card.centerX(),card.top+dp(37),text);
            text.setTextSize(tx(8.2f));text.setColor(Color.rgb(94,126,142));c.drawText("Синю рукавичку повернено власнику.",card.centerX(),card.top+dp(67),text);c.drawText("Тепер у кімнаті пам’яті з’явився перший друг.",card.centerX(),card.top+dp(91),text);
            button(c,"ВИКЛИКАТИ ВОДІЯ • ДО ВОКЗАЛУ",true);
        }

        void drawHero(Canvas c,float x,float ground,float r,float wave){drawSnowman(c,x,ground,r,wave,Color.rgb(68,79,204),false);}
        void drawFriend(Canvas c,float x,float ground,float r,float wave,boolean oneMitten){drawSnowman(c,x,ground,r,wave,Color.rgb(55,137,194),oneMitten);}

        void drawSnowman(Canvas c,float x,float ground,float r,float wave,int scarfColor,boolean oneMitten){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);
            p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);
            Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.70f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);
            p.setColor(scarfColor);c.drawRoundRect(new RectF(x-mr*.70f,my-mr*.72f,x+mr*.70f,my-mr*.52f),dp(4),dp(4),p);
            stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.60f,my,x-mr*1.38f,my-mr*(.28f+.18f*wave),stroke);c.drawLine(x+mr*.60f,my,x+mr*1.38f,my-mr*(.28f+.18f*wave),stroke);
            if(oneMitten)drawMitten(c,x+mr*1.38f,my-mr*(.28f+.18f*wave),.52f);
        }

        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.3f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(246,252,255),Color.rgb(198,226,240)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);}

        void drawMitten(Canvas c,float x,float y,float s){
            p.setColor(Color.rgb(55,137,194));RectF palm=new RectF(x-dp(12)*s,y-dp(21)*s,x+dp(12)*s,y+dp(11)*s);c.drawRoundRect(palm,dp(11)*s,dp(11)*s,p);
            c.save();c.rotate(-32,x+dp(10)*s,y);c.drawRoundRect(new RectF(x+dp(5)*s,y-dp(3)*s,x+dp(24)*s,y+dp(9)*s),dp(6)*s,dp(6)*s,p);c.restore();
            p.setColor(Color.rgb(43,113,169));c.drawRoundRect(new RectF(x-dp(14)*s,y+dp(8)*s,x+dp(14)*s,y+dp(21)*s),dp(4)*s,dp(4)*s,p);drawSnowflakeMark(c,x,y-dp(4)*s,dp(6)*s,s);
        }

        void drawSnowflakeMark(Canvas c,float x,float y,float r,float alpha){stroke.setColor(Color.argb((int)(240*alpha),255,255,255));stroke.setStrokeWidth(Math.max(dp(1.2f),r*.12f));for(int i=0;i<6;i++){double a=i*Math.PI/3;float ex=x+(float)Math.cos(a)*r,ey=y+(float)Math.sin(a)*r;c.drawLine(x,y,ex,ey,stroke);}p.setColor(Color.argb((int)(245*alpha),255,255,255));c.drawCircle(x,y,r*.20f,p);}

        void drawFootprints(Canvas c,float x1,float y1,float x2,float y2,int count,int style){
            for(int i=0;i<count;i++){float k=(i+.5f)/count,x=mix(x1,x2,k),y=mix(y1,y2,k);float side=(i%2==0?-1:1)*dp(7);p.setColor(style==1?Color.rgb(143,193,218):Color.rgb(175,207,222));c.save();c.rotate(style==2?18:-10,x,y);c.drawOval(new RectF(x-dp(5)+side,y-dp(9),x+dp(5)+side,y+dp(9)),p);c.restore();}
        }

        void drawTarget(Canvas c,float x,float y,float r){p.setColor(Color.rgb(218,235,243));c.drawCircle(x,y,r,p);p.setColor(Color.rgb(86,154,187));c.drawCircle(x,y,r*.65f,p);p.setColor(Color.WHITE);c.drawCircle(x,y,r*.28f,p);}

        void drawHouse(Canvas c,float cx,float ground){
            float w=dp(118),h=dp(122),l=cx-w/2,r=cx+w/2,top=ground-h;p.setColor(Color.rgb(230,217,193));c.drawRoundRect(new RectF(l,top,r,ground),dp(8),dp(8),p);
            Path roof=new Path();roof.moveTo(l-dp(9),top+dp(8));roof.lineTo(cx,top-dp(54));roof.lineTo(r+dp(9),top+dp(8));roof.close();p.setColor(Color.rgb(119,92,75));c.drawPath(roof,p);
            windowRect.set(cx-dp(27),top+dp(27),cx+dp(27),top+dp(77));p.setColor(Color.rgb(143,211,238));c.drawRoundRect(windowRect,dp(5),dp(5),p);stroke.setColor(Color.WHITE);stroke.setStrokeWidth(dp(3));c.drawLine(windowRect.centerX(),windowRect.top,windowRect.centerX(),windowRect.bottom,stroke);c.drawLine(windowRect.left,windowRect.centerY(),windowRect.right,windowRect.centerY(),stroke);
        }

        void button(Canvas c,String label,boolean active){
            float w=getWidth(),bottom=getHeight()-safeBottom,bw=Math.min(w-dp(42),dp(350)),bh=dp(59),l=(w-bw)/2,top=bottom-dp(79);action.set(l,top,l+bw,top+bh);p.setColor(active?Color.rgb(37,108,153):Color.rgb(192,207,215));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.WHITE);c.drawText(label,action.centerX(),action.centerY()+dp(4),text);
        }

        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(stage==RETURN_MITTEN&&mittenRect.contains(x,y)){draggingMitten=true;buzz(10);return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE&&stage==RETURN_MITTEN&&draggingMitten){mittenX=clamp(x,dp(28),getWidth()-dp(28));mittenY=clamp(y,safeTop+dp(250),getHeight()-safeBottom-dp(60));invalidate();return true;}
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                performClick();
                if(stage==INTRO&&action.contains(x,y)){buzz(20);next(TRACKS);return true;}
                if(stage==TRACKS){
                    for(int i=0;i<3;i++)if(trackCards[i].contains(x,y)){if(i==1){hint="Той самий знак! Сліди ведуть до когось малого.";buzz(32);next(RETURN_MITTEN);}else{mistakes++;prefs.edit().putInt("year3_mistakes",mistakes).apply();hint=i==0?"Ці сліди старі — їх уже замело.":"Це сліди птаха, а не сніговика.";buzz(12);invalidate();}return true;}
                }
                if(stage==RETURN_MITTEN&&draggingMitten){
                    draggingMitten=false;
                    if(friendHand.contains(mittenX,mittenY)||dist(mittenX,mittenY,friendHand.centerX(),friendHand.centerY())<dp(48)){
                        prefs.edit().putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).putString("year3_friend_name","Сніжик").apply();hint="Рукавичка повернулась до Сніжика.";buzz(45);next(SNOWBALLS);
                    }else{mittenX=getWidth()*.27f;mittenY=(getHeight()-safeBottom)*.71f-dp(150);mistakes++;prefs.edit().putInt("year3_mistakes",mistakes).apply();hint="Трохи ближче до вільної руки.";buzz(12);invalidate();}
                    return true;
                }
                if(stage==SNOWBALLS){
                    if(windowRect.contains(x,y)){mistakes++;prefs.edit().putInt("year3_mistakes",mistakes).apply();hint="Ой! Вікно не мішень. Сніжик: «Тікаємо… жартую!»";buzz(18);invalidate();return true;}
                    for(RectF r:targets)if(r.contains(x,y)){hits++;prefs.edit().putInt("year3_snowball_hits",hits).apply();hint=hits<5?"Влучно! Ще "+(5-hits)+".":"П’ять влучань — і жодного розбитого вікна.";buzz(26);if(hits>=5){prefs.edit().putBoolean("year3_story_complete",true).putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).putInt("year3_stage",DONE).apply();next(DONE);}else invalidate();return true;}
                }
                if(stage==DONE&&action.contains(x,y)){prefs.edit().putBoolean("year3_story_complete",true).putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).apply();ctx.startActivity(new Intent(ctx,UklonActivity.class));((Activity)ctx).finish();return true;}
            }
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
