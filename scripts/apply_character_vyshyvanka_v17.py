from pathlib import Path

paths={
    "main":Path("app/src/main/java/com/snowmangame/MainActivity.java"),
    "delivery":Path("app/src/main/java/com/snowmangame/DeliveryActivity.java"),
    "journey":Path("app/src/main/java/com/snowmangame/JourneyActivity.java"),
    "uklon":Path("app/src/main/java/com/snowmangame/UklonActivity.java"),
    "summer":Path("app/src/main/java/com/snowmangame/SummerActivity.java"),
    "year2":Path("app/src/main/java/com/snowmangame/YearTwoActivity.java"),
    "year3":Path("app/src/main/java/com/snowmangame/YearThreeActivity.java"),
}
src={k:p.read_text(encoding="utf-8") for k,p in paths.items()}

def rep(key,old,new,label):
    if old not in src[key]:
        raise SystemExit(f"v17 character/vyshyvanka patch failed in {key} at: {label}")
    src[key]=src[key].replace(old,new,1)

# Main: gate the first personalized choice without touching any progress.
rep("main",
    '        setContentView(new SnowmanView(this));',
    '        if(!seasonalProgress.getBoolean("character_selected",false)){startActivity(new Intent(this,CharacterActivity.class));finish();return;}\n        setContentView(new SnowmanView(this));',
    "character first-choice gate")

rep("main",
    '        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), memoryBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();',
    '        final RectF sponsorBtn=new RectF(), journeyBtn=new RectF(), memoryBtn=new RectF(), wardrobeBtn=new RectF(), replayBtn=new RectF(), sponsorCloseBtn=new RectF();',
    "wardrobe button field")

rep("main",
    '            wallet=Math.max(0,prefs.getInt("coins",0));',
    '            wallet=Math.max(0,prefs.getInt("coins",0));\n            SnowmanStyle.ensureUnlocked(prefs,year);',
    "unlock regional outfits from save progress")

rep("main",
    '        String oblastName(){switch(year){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}\n        String missionText(){',
    '        String oblastName(){switch(year){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}\n        String characterName(){return SnowmanStyle.character(prefs)==1?"Снігівчинка":"Сніговичок";}\n        String missionText(){',
    "character helper")

rep("main",
    '            drawTip(c);\n            drawSnowman(c);',
    '            drawTip(c);\n            drawWardrobeButton(c);\n            drawSnowman(c);',
    "draw wardrobe control")

rep("main",
    '            tipCard.set(m,hud.bottom+dp(7),w-m,hud.bottom+dp(7)+tipH);\n            interaction.set(m,bottom-interactionH-dp(8),w-m,bottom-dp(8));\n            playTop=tipCard.bottom+dp(5);',
    '            tipCard.set(m,hud.bottom+dp(7),w-m,hud.bottom+dp(7)+tipH);\n            wardrobeBtn.set(w-(narrow?dp(103):dp(119)),tipCard.bottom+dp(7),w-m,tipCard.bottom+(narrow?dp(35):dp(39)));\n            interaction.set(m,bottom-interactionH-dp(8),w-m,bottom-dp(8));\n            playTop=wardrobeBtn.bottom+dp(5);',
    "reserve wardrobe space")

rep("main",
    '        void drawSnowman(Canvas c){',
    '        void drawWardrobeButton(Canvas c){\n            int o=SnowmanStyle.outfit(prefs,year);p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(wardrobeBtn,dp(13),dp(13),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(narrow?6.1f:6.8f));text.setColor(Color.rgb(55,101,119));c.drawText("ГАРДЕРОБ",wardrobeBtn.centerX(),wardrobeBtn.centerY()-dp(2),text);if(o>0){RectF band=new RectF(wardrobeBtn.left+dp(9),wardrobeBtn.bottom-dp(8),wardrobeBtn.right-dp(9),wardrobeBtn.bottom-dp(4));SnowmanStyle.drawPatternBand(c,p,density,band,o);}\n        }\n\n        void drawSnowman(Canvas c){',
    "wardrobe button renderer")

rep("main",
    '            for(Accessory a:items)if(a.placed)drawAccessory(c,a.type,a.x,a.y,255);',
    '            for(Accessory a:items)if(a.placed)drawAccessory(c,a.type,a.x,a.y,255);\n            drawRegionalLook(c);',
    "render selected look")

rep("main",
    '        void drawBall(Canvas c,float x,float y,float r,int seed){',
    '        void drawRegionalLook(Canvas c){\n            int o=SnowmanStyle.outfit(prefs,year);\n            if(items[ARMS].placed&&o>0){Accessory a=items[ARMS];float lx1=a.x-midR*.65f,ly1=a.y,lx2=a.x-midR*1.39f,ly2=a.y-midR*.43f,rx1=a.x+midR*.65f,ry1=a.y,rx2=a.x+midR*1.39f,ry2=a.y-midR*.43f;SnowmanStyle.drawSleeve(c,p,stroke,density,lx1,ly1,lx2,ly2,midR*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx1,ry1,rx2,ry2,midR*.34f,o);RectF collar=new RectF(getWidth()/2f-midR*.46f,midY-midR*.67f,getWidth()/2f+midR*.46f,midY-midR*.55f);SnowmanStyle.drawPatternBand(c,p,density,collar,o);}\n            if(items[EYES].placed)SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),getWidth()/2f,headY,headR);\n        }\n\n        void drawBall(Canvas c,float x,float y,float r,int seed){',
    "regional sleeves and character accent")

rep("main",
    '                performClick();\n                if(finished){',
    '                performClick();\n                if(!finished&&wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));invalidate();return true;}\n                if(finished){',
    "open wardrobe without resetting run")

# Delivery: Year 1/2 sled scenes keep the same selected character and sleeves.
rep("delivery",
'''        void drawSnowman(Canvas c,float x,float ground,float r,float wave){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;
            snowBall(c,x,by,br);snowBall(c,x,my,mr);snowBall(c,x,hy,hr);
            p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);
            Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.72f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);
            stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.62f,my,x-mr*1.40f,my-mr*.30f,stroke);c.drawLine(x+mr*.62f,my,x+mr*(1.35f+wave*.2f),my-mr*(.28f+.25f*wave),stroke);''',
'''        void drawSnowman(Canvas c,float x,float ground,float r,float wave){
            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;
            snowBall(c,x,by,br);snowBall(c,x,my,mr);snowBall(c,x,hy,hr);
            p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);
            Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.72f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);
            stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));float lx=x-mr*.62f,ly=my,lxe=x-mr*1.40f,lye=my-mr*.30f,rx=x+mr*.62f,ry=my,rxe=x+mr*(1.35f+wave*.2f),rye=my-mr*(.28f+.25f*wave);c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);int o=SnowmanStyle.outfit(prefs,year);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.28f,my-mr*.26f,mr*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*(1.24f+wave*.14f),my-mr*(.24f+.19f*wave),mr*.34f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);''',
    "delivery character sleeves")

# Journey: keep identity visible on platform, train and school milestone.
rep("journey",
'        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){\n            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;drawSnowBall(c,x,by,br);drawSnowBall(c,x,my,mr);drawSnowBall(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x-hr*.03f,hy);n.lineTo(x+hr*.72f,hy+hr*.09f);n.lineTo(x-hr*.03f,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));stroke.setStrokeCap(Paint.Cap.ROUND);c.drawLine(x-mr*.62f,my,x-mr*1.35f,my-mr*.25f,stroke);c.drawLine(x+mr*.62f,my,x+mr*(1.35f+wave*.2f),my-mr*(.25f+.28f*wave),stroke);stroke.setStrokeCap(Paint.Cap.BUTT);\n        }',
'        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){\n            float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;drawSnowBall(c,x,by,br);drawSnowBall(c,x,my,mr);drawSnowBall(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x-hr*.03f,hy);n.lineTo(x+hr*.72f,hy+hr*.09f);n.lineTo(x-hr*.03f,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));stroke.setStrokeCap(Paint.Cap.ROUND);float lx=x-mr*.62f,ly=my,lxe=x-mr*1.35f,lye=my-mr*.25f,rx=x+mr*.62f,ry=my,rxe=x+mr*(1.35f+wave*.2f),rye=my-mr*(.25f+.28f*wave);c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);int o=SnowmanStyle.outfit(prefs,year);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.24f,my-mr*.21f,mr*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*(1.24f+wave*.14f),my-mr*(.21f+.20f*wave),mr*.34f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);stroke.setStrokeCap(Paint.Cap.BUTT);\n        }',
    "journey character sleeves")

# Uklon: the same look survives the car scene.
rep("uklon",
'        void drawSnowman(Canvas c,float x,float ground,float r,float wave){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.72f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.62f,my,x-mr*1.35f,my-mr*.30f,stroke);c.drawLine(x+mr*.62f,my,x+mr*(1.30f+wave*.2f),my-mr*(.28f+.2f*wave),stroke);}',
'        void drawSnowman(Canvas c,float x,float ground,float r,float wave){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.72f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));float lx=x-mr*.62f,ly=my,lxe=x-mr*1.35f,lye=my-mr*.30f,rx=x+mr*.62f,ry=my,rxe=x+mr*(1.30f+wave*.2f),rye=my-mr*(.28f+.2f*wave);c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);int o=SnowmanStyle.outfit(prefs,year);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.24f,my-mr*.26f,mr*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*(1.18f+wave*.13f),my-mr*(.23f+.14f*wave),mr*.34f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);}',
    "Uklon character sleeves")

# Summer: the body can melt, but the selected identity and clothing are visibly the same until the cloth falls away.
rep("summer",
'                stroke.setColor(Color.rgb(105,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.60f,my,x-mr*1.35f,my-mr*.26f,stroke);c.drawLine(x+mr*.60f,my,x+mr*1.35f,my-mr*.26f,stroke);',
'                stroke.setColor(Color.rgb(105,78,58));stroke.setStrokeWidth(dp(3));float lx=x-mr*.60f,ly=my,lxe=x-mr*1.35f,lye=my-mr*.26f,rx=x+mr*.60f,ry=my,rxe=x+mr*1.35f,rye=my-mr*.26f;c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);int o=SnowmanStyle.outfit(prefs,Math.max(1,year-1));if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.24f,my-mr*.22f,mr*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*1.24f,my-mr*.22f,mr*.34f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);',
    "summer keeps selected identity")

# Year 2: research scenes use the chosen player look.
rep("year2",
'        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snowBall(c,x,by,br);snowBall(c,x,my,mr);snowBall(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.65f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));c.drawLine(x-mr*.62f,my,x-mr*1.38f,my-mr*(.25f+.15f*wave),stroke);c.drawLine(x+mr*.62f,my,x+mr*1.38f,my-mr*(.25f+.20f*wave),stroke);}',
'        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){float br=r,mr=r*.72f,hr=r*.53f,by=ground-br,my=by-(br+mr)*.84f,hy=my-(mr+hr)*.84f;snowBall(c,x,by,br);snowBall(c,x,my,mr);snowBall(c,x,hy,hr);p.setColor(Color.rgb(45,60,70));c.drawCircle(x-hr*.28f,hy-hr*.14f,hr*.08f,p);c.drawCircle(x+hr*.28f,hy-hr*.14f,hr*.08f,p);Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.65f,hy+hr*.08f);n.lineTo(x,hy+hr*.14f);n.close();p.setColor(Color.rgb(241,117,34));c.drawPath(n,p);stroke.setColor(Color.rgb(104,78,58));stroke.setStrokeWidth(dp(3));float lx=x-mr*.62f,ly=my,lxe=x-mr*1.38f,lye=my-mr*(.25f+.15f*wave),rx=x+mr*.62f,ry=my,rxe=x+mr*1.38f,rye=my-mr*(.25f+.20f*wave);c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);int o=SnowmanStyle.outfit(prefs,2);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.26f,my-mr*(.21f+.10f*wave),mr*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*1.26f,my-mr*(.21f+.13f*wave),mr*.34f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);}',
    "Year 2 character sleeves")

# Year 3: only the player's hero gets the selected identity; Snihyk keeps his own look.
rep("year3",
'        void drawHero(Canvas c,float x,float ground,float r,float wave){drawSnowman(c,x,ground,r,wave,Color.rgb(68,79,204),false);}',
'        void drawHero(Canvas c,float x,float ground,float r,float wave){drawSnowman(c,x,ground,r,wave,Color.rgb(68,79,204),false);float mr=r*.72f,hr=r*.53f,by=ground-r,my=by-(r+mr)*.84f,hy=my-(mr+hr)*.84f;float lx=x-mr*.60f,ly=my,rx=x+mr*.60f,ry=my;int o=SnowmanStyle.outfit(prefs,3);if(o>0){SnowmanStyle.drawSleeve(c,p,stroke,density,lx,ly,x-mr*1.26f,my-mr*(.24f+.13f*wave),mr*.34f,o);SnowmanStyle.drawSleeve(c,p,stroke,density,rx,ry,x+mr*1.26f,my-mr*(.24f+.13f*wave),mr*.34f,o);}SnowmanStyle.drawFaceAccent(c,p,stroke,density,SnowmanStyle.character(prefs),x,hy,hr);}',
    "Year 3 player identity")

for k,p in paths.items():p.write_text(src[k],encoding="utf-8")
print("Applied SnowmanGame v17 character choice + regional vyshyvanka wardrobe")
