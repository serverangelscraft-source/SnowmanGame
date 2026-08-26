package com.snowmangame;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.*;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;

public class SchoolActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();
        if(Build.VERSION.SDK_INT>=21){
            w.setStatusBarColor(Color.TRANSPARENT);
            w.setNavigationBarColor(Color.rgb(236,247,252));
            int flags=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if(Build.VERSION.SDK_INT>=26)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            w.getDecorView().setSystemUiVisibility(flags);
        }
        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);
        setContentView(new SchoolView(this));
    }

    static class SchoolView extends View {
        static final int GATE=0, CLASS_DOOR=1, DESK=2, LESSON=3, DONE=4;
        final Context ctx; final SharedPreferences prefs;
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG), text=new Paint(Paint.ANTI_ALIAS_FLAG), stroke=new Paint(Paint.ANTI_ALIAS_FLAG);
        final RectF action=new RectF(), memoryBtn=new RectF(), wardrobeBtn=new RectF();
        final RectF[] doors={new RectF(),new RectF(),new RectF()};
        final RectF[] desks={new RectF(),new RectF(),new RectF()};
        final RectF[] answers={new RectF(),new RectF(),new RectF()};
        final float density,textScale; final Vibrator vibrator;
        float safeTop,safeBottom; int stage,mistakes; String feedback="";

        SchoolView(Context c){
            super(c);ctx=c;prefs=c.getSharedPreferences("snowman_game",Context.MODE_PRIVATE);
            density=getResources().getDisplayMetrics().density;
            textScale=Math.min(getResources().getDisplayMetrics().scaledDensity,density*1.15f);
            vibrator=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);
            stage=prefs.getBoolean("school_first_day_complete",false)?DONE:Math.max(GATE,Math.min(LESSON,prefs.getInt("school_stage",GATE)));
            mistakes=Math.max(0,prefs.getInt("school_mistakes",0));
            prefs.edit().putBoolean("school_unlocked",true).apply();
            SnowmanStyle.ensureUnlocked(prefs,7);
            text.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD));stroke.setStyle(Paint.Style.STROKE);stroke.setStrokeCap(Paint.Cap.ROUND);
            setClickable(true);setFocusable(true);setContentDescription("Шкільні пригоди сніговика після сьомої зими");
            setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener(){@Override public WindowInsets onApplyWindowInsets(View v,WindowInsets i){safeTop=i.getSystemWindowInsetTop();safeBottom=i.getSystemWindowInsetBottom();invalidate();return i;}});requestApplyInsets();
        }

        float dp(float v){return v*density;} float tx(float v){return v*textScale;}
        void buzz(int ms){if(vibrator==null||!vibrator.hasVibrator())return;if(Build.VERSION.SDK_INT>=26)vibrator.vibrate(VibrationEffect.createOneShot(ms,80));else vibrator.vibrate(ms);}
        void next(int s){stage=s;feedback="";prefs.edit().putInt("school_stage",Math.min(s,LESSON)).apply();buzz(24);invalidate();}
        String title(){if(stage==GATE)return"Перший день";if(stage==CLASS_DOOR)return"Знайди свій клас";if(stage==DESK)return"Знайди свою парту";if(stage==LESSON)return"Перший урок";return"Школа відкрита";}
        String sub(){if(stage==GATE)return"7 зим привели сюди — тепер починається нова глава";if(stage==CLASS_DOOR)return"Сніговик записаний до 1-А. Не переплутай двері.";if(stage==DESK)return"На твоїй парті знак живої сніжинки.";if(stage==LESSON)return"Перше питання просте, але дуже особисте.";return"Перший урок завершено • Тернопільщина";}

        @Override protected void onDraw(Canvas c){super.onDraw(c);drawBackground(c);drawHeader(c);if(stage==GATE)drawGate(c);else if(stage==CLASS_DOOR)drawDoors(c);else if(stage==DESK)drawDesks(c);else if(stage==LESSON)drawLesson(c);else drawDone(c);}

        void drawBackground(Canvas c){float w=getWidth(),h=getHeight(),bottom=h-safeBottom;if(stage==GATE){LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(160,218,246),Color.rgb(236,248,253),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(244,250,253));c.drawRect(0,bottom*.67f,w,h,p);}else{LinearGradient g=new LinearGradient(0,0,0,bottom,Color.rgb(241,235,213),Color.rgb(226,220,198),Shader.TileMode.CLAMP);p.setShader(g);c.drawRect(0,0,w,h,p);p.setShader(null);p.setColor(Color.rgb(191,154,109));c.drawRect(0,bottom-dp(82),w,h,p);}}

        void drawHeader(Canvas c){float w=getWidth(),top=safeTop+dp(10);RectF r=new RectF(dp(14),top,w-dp(14),top+dp(108));p.setColor(Color.argb(247,255,255,255));c.drawRoundRect(r,dp(24),dp(24),p);text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(102,129,143));c.drawText("ШКОЛА • ТЕРНОПІЛЬЩИНА • 1-А",r.left+dp(17),r.top+dp(20),text);text.setTextSize(tx(18));text.setColor(Color.rgb(37,73,94));c.drawText(title(),r.left+dp(17),r.top+dp(51),text);String s=sub();text.setTextSize(tx(8));while(text.measureText(s)>r.width()-dp(34)&&text.getTextSize()>tx(5.8f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(91,124,141));c.drawText(s,r.left+dp(17),r.top+dp(78),text);text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(6.6f));text.setColor(Color.rgb(128,143,149));c.drawText("помилки "+mistakes,r.right-dp(17),r.bottom-dp(13),text);}

        void drawGate(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.72f;drawSchoolBuilding(c,w*.56f,ground,Math.min(w*.72f,dp(285)));drawHero(c,w*.26f,ground+dp(2),dp(39),.55f);drawFriend(c,w*.39f,ground+dp(2),dp(31),.35f);RectF card=new RectF(dp(22),safeTop+dp(137),w-dp(22),safeTop+dp(223));p.setColor(Color.argb(241,255,255,255));c.drawRoundRect(card,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11));text.setColor(Color.rgb(50,84,101));c.drawText("«Сніжик, ми реально дійшли до школи.»",card.centerX(),card.top+dp(33),text);text.setTextSize(tx(7.8f));text.setColor(Color.rgb(99,127,140));c.drawText("Маршрут дитинства: 7 зим • 7 областей.",card.centerX(),card.top+dp(59),text);button(c,"УВІЙТИ ДО ШКОЛИ");}

        void drawSchoolBuilding(Canvas c,float cx,float ground,float width){float h=width*.63f,l=cx-width/2,r=cx+width/2,top=ground-h;p.setColor(Color.rgb(236,225,202));c.drawRoundRect(new RectF(l,top,r,ground),dp(10),dp(10),p);p.setColor(Color.rgb(79,116,143));c.drawRect(l,top,r,top+dp(22),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(10));text.setColor(Color.WHITE);c.drawText("ШКОЛА",cx,top+dp(15),text);for(int row=0;row<2;row++)for(int col=0;col<4;col++){float x=l+width*(.12f+col*.245f),y=top+dp(42)+row*dp(52);p.setColor(Color.rgb(151,210,235));c.drawRoundRect(new RectF(x,y,x+dp(30),y+dp(31)),dp(4),dp(4),p);}p.setColor(Color.rgb(116,80,58));c.drawRoundRect(new RectF(cx-dp(22),ground-dp(55),cx+dp(22),ground),dp(4),dp(4),p);}

        void drawDoors(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF info=new RectF(dp(22),safeTop+dp(142),w-dp(22),safeTop+dp(211));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(info,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(59,91,107));c.drawText(feedback.length()>0?feedback:"Торкнись дверей класу 1-А",info.centerX(),info.centerY()+dp(3),text);float gap=dp(12),left=dp(20),right=w-dp(20),top=safeTop+dp(240),doorW=(right-left-gap*2)/3f,h=Math.min(dp(300),bottom-top-dp(88));String[] labels={"1-Б","1-А","2-А"};for(int i=0;i<3;i++){doors[i].set(left+i*(doorW+gap),top,left+i*(doorW+gap)+doorW,top+h);p.setColor(Color.rgb(119,83,59));c.drawRoundRect(doors[i],dp(8),dp(8),p);p.setColor(Color.rgb(239,232,210));RectF sign=new RectF(doors[i].left+dp(8),doors[i].top+dp(18),doors[i].right-dp(8),doors[i].top+dp(62));c.drawRoundRect(sign,dp(7),dp(7),p);text.setTextSize(tx(14));text.setColor(Color.rgb(53,82,98));c.drawText(labels[i],sign.centerX(),sign.centerY()+dp(5),text);p.setColor(Color.rgb(218,179,73));c.drawCircle(doors[i].right-dp(13),doors[i].centerY(),dp(4),p);}}

        void drawDesks(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;drawBoard(c,w);RectF info=new RectF(dp(22),safeTop+dp(197),w-dp(22),safeTop+dp(262));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(info,dp(19),dp(19),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.6f));text.setColor(Color.rgb(58,90,105));c.drawText(feedback.length()>0?feedback:"Шукай парту зі знаком сніжинки",info.centerX(),info.centerY()+dp(3),text);float gap=dp(14),left=dp(22),right=w-dp(22),top=safeTop+dp(292),dw=(right-left-gap*2)/3f,dh=Math.min(dp(190),bottom-top-dp(100));for(int i=0;i<3;i++){desks[i].set(left+i*(dw+gap),top,left+i*(dw+gap)+dw,top+dh);drawDesk(c,desks[i],i==1);if(i==2)drawFriend(c,desks[i].centerX(),desks[i].bottom+dp(22),dp(23),.2f);}}

        void drawBoard(Canvas c,float w){RectF b=new RectF(dp(26),safeTop+dp(136),w-dp(26),safeTop+dp(185));p.setColor(Color.rgb(64,101,84));c.drawRoundRect(b,dp(7),dp(7),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(11));text.setColor(Color.WHITE);c.drawText("ЛАСКАВО ДО 1-А",b.centerX(),b.centerY()+dp(4),text);}
        void drawDesk(Canvas c,RectF r,boolean mark){p.setColor(Color.rgb(174,126,78));c.drawRoundRect(new RectF(r.left,r.top+dp(36),r.right,r.top+dp(78)),dp(8),dp(8),p);stroke.setColor(Color.rgb(112,78,52));stroke.setStrokeWidth(dp(4));c.drawLine(r.left+dp(14),r.top+dp(76),r.left+dp(8),r.bottom,stroke);c.drawLine(r.right-dp(14),r.top+dp(76),r.right-dp(8),r.bottom,stroke);if(mark){float x=r.centerX(),y=r.top+dp(56),rr=dp(10);stroke.setColor(Color.WHITE);stroke.setStrokeWidth(dp(1.8f));for(int i=0;i<6;i++){double a=i*Math.PI/3;c.drawLine(x,y,x+(float)Math.cos(a)*rr,y+(float)Math.sin(a)*rr,stroke);}p.setColor(Color.WHITE);c.drawCircle(x,y,dp(2.2f),p);}}

        void drawLesson(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF q=new RectF(dp(22),safeTop+dp(142),w-dp(22),safeTop+dp(260));p.setColor(Color.argb(246,255,255,255));c.drawRoundRect(q,dp(22),dp(22),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(113,132,136));c.drawText("ПЕРШЕ ПИТАННЯ",q.centerX(),q.top+dp(24),text);text.setTextSize(tx(14));text.setColor(Color.rgb(48,82,99));c.drawText("Що стане зі снігом у теплі?",q.centerX(),q.top+dp(57),text);text.setTextSize(tx(8));text.setColor(Color.rgb(95,124,139));c.drawText(feedback.length()>0?feedback:"Сніговик має особливу причину це знати.",q.centerX(),q.bottom-dp(24),text);String[] a={"ЛІД","ВОДА","ПІСОК"};float left=dp(24),right=w-dp(24),gap=dp(12),top=safeTop+dp(300),aw=(right-left-gap*2)/3f,ah=Math.min(dp(150),bottom-top-dp(105));for(int i=0;i<3;i++){answers[i].set(left+i*(aw+gap),top,left+i*(aw+gap)+aw,top+ah);p.setColor(i==1?Color.rgb(238,250,255):Color.argb(243,255,255,255));c.drawRoundRect(answers[i],dp(20),dp(20),p);text.setTextSize(tx(13));text.setColor(Color.rgb(45,97,124));c.drawText(a[i],answers[i].centerX(),answers[i].centerY()+dp(5),text);}drawHero(c,w*.28f,bottom-dp(76),dp(26),.2f);drawFriend(c,w*.72f,bottom-dp(76),dp(23),.2f);}

        void drawDone(Canvas c){float w=getWidth(),bottom=getHeight()-safeBottom;RectF card=new RectF(dp(20),safeTop+dp(142),w-dp(20),bottom-dp(190));p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(card,dp(26),dp(26),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(111,131,137));c.drawText("НОВА ГЛАВА ЖИТТЯ",card.centerX(),card.top+dp(27),text);text.setTextSize(tx(22));text.setColor(Color.rgb(41,105,139));c.drawText("ПЕРШИЙ УРОК ПРОЙДЕНО",card.centerX(),card.top+dp(62),text);text.setTextSize(tx(8.4f));text.setColor(Color.rgb(91,123,138));c.drawText("Сніговик не закінчився на сьомій зимі — він школярує.",card.centerX(),card.top+dp(91),text);float ground=card.bottom-dp(32);drawHero(c,card.centerX()-dp(56),ground,dp(38),.8f);drawFriend(c,card.centerX()+dp(58),ground,dp(31),.8f);RectF badge=new RectF(card.left+dp(25),card.top+dp(116),card.right-dp(25),card.top+dp(158));p.setColor(Color.rgb(235,247,240));c.drawRoundRect(badge,dp(15),dp(15),p);text.setTextSize(tx(8));text.setColor(Color.rgb(55,126,101));c.drawText("ШКОЛА ВІДКРИТА • ДНІВ "+Math.max(1,prefs.getInt("school_days",1)),badge.centerX(),badge.centerY()+dp(3),text);
            float gap=dp(8),half=(w-dp(48)-gap)/2f;memoryBtn.set(dp(20),bottom-dp(174),dp(20)+half,bottom-dp(121));wardrobeBtn.set(memoryBtn.right+gap,bottom-dp(174),w-dp(20),bottom-dp(121));p.setColor(Color.rgb(232,242,237));c.drawRoundRect(memoryBtn,dp(17),dp(17),p);p.setColor(Color.rgb(234,242,247));c.drawRoundRect(wardrobeBtn,dp(17),dp(17),p);text.setTextSize(tx(8));text.setColor(Color.rgb(58,106,91));c.drawText("СПОГАДИ",memoryBtn.centerX(),memoryBtn.centerY()+dp(3),text);text.setColor(Color.rgb(61,103,127));c.drawText("ГАРДЕРОБ",wardrobeBtn.centerX(),wardrobeBtn.centerY()+dp(3),text);button(c,"ПРОЙТИ ШКІЛЬНИЙ ДЕНЬ ЩЕ РАЗ");}

        void drawHero(Canvas c,float x,float ground,float r,float wave){drawPerson(c,x,ground,r,wave,true);}
        void drawFriend(Canvas c,float x,float ground,float r,float wave){drawPerson(c,x,ground,r,wave,false);}
        void drawPerson(Canvas c,float x,float ground,float r,float wave,boolean hero){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(44,59,68));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.68f,hy+hr*.07f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(105,78,57));stroke.setStrokeWidth(Math.max(dp(2.5f),r*.075f));float lx=x-mr*.60f,ly=my,lxe=x-mr*1.38f,lye=my-mr*(.28f+.15f*wave),rx=x+mr*.60f,ry=my,rxe=x+mr*(1.38f+wave*.12f),rye=my-mr*(.28f+.18f*wave);c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);if(hero){int o=SnowmanStyle.outfit(prefs,7);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.25f,my-mr*.24f,mr*.36f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*(1.25f+wave*.08f),my-mr*(.24f+.12f*wave),mr*.36f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);}else{p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-mr*.62f,my-mr*.70f,x+mr*.62f,my-mr*.52f),dp(4),dp(4),p);}}
        void snow(Canvas c,float x,float y,float r){RadialGradient g=new RadialGradient(x-r*.30f,y-r*.35f,r*1.4f,new int[]{Color.WHITE,Color.rgb(247,252,255),Color.rgb(198,227,242)},null,Shader.TileMode.CLAMP);p.setShader(g);c.drawCircle(x,y,r,p);p.setShader(null);stroke.setColor(Color.argb(65,90,146,174));stroke.setStrokeWidth(dp(1));c.drawCircle(x,y,r-dp(.5f),stroke);}

        void button(Canvas c,String label){float w=getWidth(),bottom=getHeight()-safeBottom;action.set(dp(22),bottom-dp(66),w-dp(22),bottom-dp(11));p.setColor(Color.rgb(37,108,153));c.drawRoundRect(action,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.4f));while(text.measureText(label)>action.width()-dp(28)&&text.getTextSize()>tx(6.5f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.WHITE);c.drawText(label,action.centerX(),action.centerY()+dp(4),text);}

        void completeLesson(){int days=Math.max(0,prefs.getInt("school_days",0))+1;prefs.edit().putBoolean("school_first_day_complete",true).putBoolean("school_unlocked",true).putInt("school_days",days).putInt("school_stage",DONE).apply();stage=DONE;feedback="";buzz(45);invalidate();}
        @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;performClick();float x=e.getX(),y=e.getY();if(stage==GATE&&action.contains(x,y)){next(CLASS_DOOR);return true;}if(stage==CLASS_DOOR){for(int i=0;i<3;i++)if(doors[i].contains(x,y)){if(i==1)next(DESK);else{mistakes++;feedback="Це не 1-А. Подивись на табличку ще раз.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();}return true;}}if(stage==DESK){for(int i=0;i<3;i++)if(desks[i].contains(x,y)){if(i==1)next(LESSON);else{mistakes++;feedback="На цій парті немає знака живої сніжинки.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();}return true;}}if(stage==LESSON){for(int i=0;i<3;i++)if(answers[i].contains(x,y)){if(i==1)completeLesson();else{mistakes++;feedback=i==0?"Лід теж тане. Спробуй ще.":"Пісок тут точно ні до чого.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();}return true;}}if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}if(action.contains(x,y)){prefs.edit().putBoolean("school_first_day_complete",false).putInt("school_stage",GATE).apply();stage=GATE;mistakes=0;feedback="";invalidate();return true;}}return true;}
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
