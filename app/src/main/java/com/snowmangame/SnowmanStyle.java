package com.snowmangame;

import android.content.SharedPreferences;
import android.graphics.*;

final class SnowmanStyle {
    private SnowmanStyle(){}

    static int character(SharedPreferences prefs){return Math.max(0,Math.min(1,prefs.getInt("character_type",0)));}
    static int outfit(SharedPreferences prefs,int year){int o=Math.max(0,Math.min(7,prefs.getInt("equipped_vyshyvanka_year",year)));if(o==0)return 0;int mask=prefs.getInt("vyshyvanka_unlocked_mask",(1<<Math.max(1,Math.min(7,year)))-1);return (mask&(1<<(o-1)))!=0?o:0;}
    static void ensureUnlocked(SharedPreferences prefs,int year){int y=Math.max(1,Math.min(7,year)),earned=(1<<y)-1,mask=prefs.getInt("vyshyvanka_unlocked_mask",earned)|earned;SharedPreferences.Editor e=prefs.edit().putInt("vyshyvanka_unlocked_mask",mask);if(!prefs.contains("equipped_vyshyvanka_year"))e.putInt("equipped_vyshyvanka_year",y);e.apply();}

    static void drawFaceAccent(Canvas c,Paint p,Paint stroke,float density,int character,float x,float y,float r){
        stroke.setColor(Color.rgb(58,78,89));stroke.setStrokeWidth(Math.max(density*1.0f,r*.035f));stroke.setStrokeCap(Paint.Cap.ROUND);
        if(character==1){
            c.drawLine(x-r*.36f,y-r*.23f,x-r*.43f,y-r*.32f,stroke);c.drawLine(x+r*.36f,y-r*.23f,x+r*.43f,y-r*.32f,stroke);drawBow(c,p,density,x+r*.57f,y-r*.58f,Math.max(density*3.2f,r*.18f));
        }else{
            c.drawLine(x-r*.38f,y-r*.30f,x-r*.18f,y-r*.34f,stroke);c.drawLine(x+r*.18f,y-r*.34f,x+r*.38f,y-r*.30f,stroke);
        }
    }

    static void drawSleeve(Canvas c,Paint p,Paint stroke,float density,float x1,float y1,float x2,float y2,float width,int region){
        if(region<=0)return;float mx=(x1+x2)/2f,my=(y1+y2)/2f,len=(float)Math.hypot(x2-x1,y2-y1),ang=(float)Math.toDegrees(Math.atan2(y2-y1,x2-x1));
        c.save();c.rotate(ang,mx,my);RectF cloth=new RectF(mx-len*.47f,my-width/2f,mx+len*.47f,my+width/2f);p.setColor(Color.rgb(250,249,241));c.drawRoundRect(cloth,width*.27f,width*.27f,p);stroke.setColor(Color.argb(72,99,111,105));stroke.setStrokeWidth(Math.max(density*.7f,width*.04f));c.drawRoundRect(cloth,width*.27f,width*.27f,stroke);RectF band=new RectF(cloth.right-len*.30f,cloth.top+width*.13f,cloth.right-density*1.5f,cloth.bottom-width*.13f);drawPatternBand(c,p,density,band,region);c.restore();
    }

    static void drawPatternBand(Canvas c,Paint p,float density,RectF r,int region){
        int a,b,accent;switch(region){case 1:a=Color.rgb(177,49,57);b=Color.rgb(48,63,70);accent=Color.rgb(83,132,91);break;case 2:a=Color.rgb(190,48,54);b=Color.rgb(58,58,61);accent=Color.rgb(222,107,70);break;case 3:a=Color.rgb(146,69,72);b=Color.rgb(96,101,101);accent=Color.rgb(196,133,94);break;case 4:a=Color.rgb(61,105,157);b=Color.rgb(161,53,72);accent=Color.rgb(205,143,71);break;case 5:a=Color.rgb(119,38,47);b=Color.rgb(42,43,43);accent=Color.rgb(184,78,57);break;case 6:a=Color.rgb(45,47,46);b=Color.rgb(154,49,53);accent=Color.rgb(184,121,74);break;default:a=Color.rgb(37,38,38);b=Color.rgb(112,35,43);accent=Color.rgb(190,91,52);break;}
        p.setColor(Color.rgb(248,246,236));c.drawRoundRect(r,density*2,density*2,p);float h=r.height(),step=Math.max(density*6.3f,h*.78f);int n=Math.max(2,(int)(r.width()/step)+1);
        for(int i=0;i<n;i++){float x=r.left+i*step+step*.45f,y=r.centerY();if(region==2||region==4){p.setColor(i%2==0?a:b);c.drawRect(x-step*.09f,r.top+density*.6f,x+step*.09f,r.bottom-density*.6f,p);c.drawRect(x-step*.26f,y-h*.09f,x+step*.26f,y+h*.09f,p);}else{Path d=new Path();d.moveTo(x,y-h*.38f);d.lineTo(x+step*.29f,y);d.lineTo(x,y+h*.38f);d.lineTo(x-step*.29f,y);d.close();p.setColor(i%2==0?a:b);c.drawPath(d,p);p.setColor(accent);c.drawCircle(x,y,Math.max(density*.8f,h*.10f),p);}}
        if(region>=5){p.setColor(b);float edge=Math.max(density*.75f,h*.07f);c.drawRect(r.left,r.top,r.right,r.top+edge,p);c.drawRect(r.left,r.bottom-edge,r.right,r.bottom,p);}
    }

    static void drawBow(Canvas c,Paint p,float density,float x,float y,float s){p.setColor(Color.rgb(199,64,83));Path a=new Path();a.moveTo(x,y);a.lineTo(x-s*1.5f,y-s*.8f);a.lineTo(x-s*1.3f,y+s*.8f);a.close();c.drawPath(a,p);Path b=new Path();b.moveTo(x,y);b.lineTo(x+s*1.5f,y-s*.8f);b.lineTo(x+s*1.3f,y+s*.8f);b.close();c.drawPath(b,p);c.drawCircle(x,y,Math.max(density*1.1f,s*.45f),p);}
}
