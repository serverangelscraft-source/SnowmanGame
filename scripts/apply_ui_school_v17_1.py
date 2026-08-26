from pathlib import Path

paths={
    "main":Path("app/src/main/java/com/snowmangame/MainActivity.java"),
    "delivery":Path("app/src/main/java/com/snowmangame/DeliveryActivity.java"),
    "journey":Path("app/src/main/java/com/snowmangame/JourneyActivity.java"),
    "memory":Path("app/src/main/java/com/snowmangame/MemoryActivity.java"),
    "uklon":Path("app/src/main/java/com/snowmangame/UklonActivity.java"),
    "wardrobe":Path("app/src/main/java/com/snowmangame/WardrobeActivity.java"),
}
src={k:p.read_text(encoding="utf-8") for k,p in paths.items()}

def rep(key,old,new,label):
    if old not in src[key]:
        raise SystemExit(f"v17.1 UI patch failed in {key} at: {label}")
    src[key]=src[key].replace(old,new,1)

def section(key,start_marker,end_marker,new_text,label):
    s=src[key]
    a=s.find(start_marker)
    if a<0: raise SystemExit(f"v17.1 UI patch missing start in {key}: {label}")
    b=s.find(end_marker,a+len(start_marker))
    if b<0: raise SystemExit(f"v17.1 UI patch missing end in {key}: {label}")
    src[key]=s[:a]+new_text+s[b:]

# Year 7 is no longer routed back through transport after the seven-oblast arc.
start='                    if(journeyBtn.contains(x,y)){'
end='                    if(replayBtn.contains(x,y)){reset();return true;}'
new='''                    if(journeyBtn.contains(x,y)){
                        if(year>=7){prefs.edit().putBoolean("school_unlocked",true).apply();ctx.startActivity(new Intent(ctx,SchoolActivity.class));}
                        else if(year==2&&!prefs.getBoolean("year2_story_complete",false)){prefs.edit().putBoolean("year2_research_pending",true).apply();ctx.startActivity(new Intent(ctx,YearTwoActivity.class));}
                        else if(year==3&&!prefs.getBoolean("year3_story_complete",false)){ctx.startActivity(new Intent(ctx,YearThreeActivity.class));}
                        else ctx.startActivity(new Intent(ctx,DeliveryActivity.class));
                        ((Activity)ctx).finish();return true;
                    }
'''
section("main",start,end,new,"route completed year 7 straight to school")

rep("delivery",
    '        if(routeYear>=3){',
    '        if(routeYear>=7&&routePrefs.getInt("winters_lived",0)>=7){routePrefs.edit().putBoolean("school_unlocked",true).apply();startActivity(new Intent(this,SchoolActivity.class));finish();return;}\n        if(routeYear>=3){',
    "skip obsolete Year 7 Uklon route")

rep("uklon",
    '        setContentView(new UklonView(this));',
    '        SharedPreferences schoolPrefs=getSharedPreferences("snowman_game",MODE_PRIVATE);if(schoolPrefs.getInt("life_year",1)>=7&&schoolPrefs.getInt("winters_lived",0)>=7){schoolPrefs.edit().putBoolean("school_unlocked",true).apply();startActivity(new Intent(this,SchoolActivity.class));finish();return;}\n        setContentView(new UklonView(this));',
    "redirect completed arc away from Uklon")

# Replace the giant empty school milestone card with a filled composition.
school_method='''        void drawSchoolFinish(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom;
            drawHeader(c,"Шкільна глава","Маршрут дитинства завершено • попереду перший день у класі");
            RectF scene=new RectF(dp(18),safeTop+dp(132),w-dp(18),bottom-dp(88));
            p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(scene,dp(27),dp(27),p);
            float gap=dp(7),pillW=(scene.width()-dp(36)-gap*2)/3f,py=scene.top+dp(18);
            drawSchoolPill(c,new RectF(scene.left+dp(12),py,scene.left+dp(12)+pillW,py+dp(38)),"7 ЗИМ");
            drawSchoolPill(c,new RectF(scene.left+dp(12)+pillW+gap,py,scene.left+dp(12)+pillW*2+gap,py+dp(38)),"7 ОБЛАСТЕЙ");
            drawSchoolPill(c,new RectF(scene.left+dp(12)+pillW*2+gap*2,py,scene.right-dp(12),py+dp(38)),"1-А");
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(22));text.setColor(Color.rgb(42,111,151));c.drawText("ШКОЛЯР",scene.centerX(),scene.top+dp(88),text);
            text.setTextSize(tx(8.4f));text.setColor(Color.rgb(87,123,142));c.drawText("Тернопільщина • тепер мандри стають шкільним життям",scene.centerX(),scene.top+dp(114),text);
            float ground=scene.bottom-dp(24),bw=Math.min(scene.width()*.52f,dp(220));
            drawSchoolMilestoneBuilding(c,scene.right-bw*.48f,ground,bw);
            drawSnowKid(c,scene.left+scene.width()*.24f,ground+dp(2),Math.min(dp(53),scene.width()*.12f),.75f);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(8));text.setColor(Color.rgb(71,104,121));c.drawText("Жива сніжинка пам’ятає всі 7 зим.",scene.left+dp(18),scene.top+dp(144),text);
            text.setTextSize(tx(7));text.setColor(Color.rgb(113,132,140));c.drawText("Наступна ціль — знайти 1-А, свою парту і пережити перший урок.",scene.left+dp(18),scene.top+dp(166),text);
            actionBtn.set(dp(22),bottom-dp(72),w-dp(22),bottom-dp(13));p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);String label="ПЕРШИЙ ДЕНЬ У ШКОЛІ";text.setTextSize(tx(10.5f));while(text.measureText(label)>actionBtn.width()-dp(28)&&text.getTextSize()>tx(7f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.WHITE);c.drawText(label,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }

        void drawSchoolPill(Canvas c,RectF r,String label){p.setColor(Color.rgb(233,245,250));c.drawRoundRect(r,dp(13),dp(13),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(55,113,142));c.drawText(label,r.centerX(),r.centerY()+dp(3),text);}
        void drawSchoolMilestoneBuilding(Canvas c,float cx,float ground,float width){float h=width*.64f,l=cx-width/2,r=cx+width/2,top=ground-h;p.setColor(Color.rgb(236,225,203));c.drawRoundRect(new RectF(l,top,r,ground),dp(8),dp(8),p);p.setColor(Color.rgb(68,111,140));c.drawRect(l,top,r,top+dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.5f));text.setColor(Color.WHITE);c.drawText("ШКОЛА",cx,top+dp(13),text);for(int row=0;row<2;row++)for(int col=0;col<3;col++){float x=l+width*(.10f+col*.31f),y=top+dp(34)+row*dp(40);p.setColor(Color.rgb(151,210,235));c.drawRoundRect(new RectF(x,y,x+dp(25),y+dp(25)),dp(4),dp(4),p);}p.setColor(Color.rgb(112,81,59));c.drawRoundRect(new RectF(cx-dp(18),ground-dp(47),cx+dp(18),ground),dp(4),dp(4),p);}

'''
section("journey",'        void drawSchoolFinish(Canvas c){','        void drawSnowKid(Canvas c,float x,float ground,float r,float wave){',school_method,'school milestone composition')
rep("journey",
    '                if(year>=7&&stage==WALK_TO_CASHIER&&actionBtn.contains(x,y)){startNewYear();return true;}',
    '                if(year>=7&&stage==WALK_TO_CASHIER&&actionBtn.contains(x,y)){prefs.edit().putBoolean("school_unlocked",true).apply();getContext().startActivity(new Intent(getContext(),SchoolActivity.class));((Activity)getContext()).finish();return true;}',
    "school button must not loop to winter 7")

# Uklon wait screen: fill the dead middle, fix copy and make the sled read as a sled.
wait_method='''        void drawWait(Canvas c,float t){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.64f;
            drawTop(c,"Водій уже їде","Стій біля санчат — машина під’їде праворуч");
            RectF eta=new RectF(dp(28),safeTop+dp(138),w-dp(28),safeTop+dp(220));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(eta,dp(22),dp(22),p);
            int sec=Math.max(0,3-(int)t);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(15));text.setColor(Color.rgb(44,68,81));c.drawText(sec>0?"ВОДІЙ ЗА "+sec+" С":"МАШИНА ПІД’ЇЖДЖАЄ",eta.centerX(),eta.top+dp(34),text);
            String etaRoute=oblastName()+" • Двір → Вокзал";text.setTextSize(tx(8));while(text.measureText(etaRoute)>eta.width()-dp(24)&&text.getTextSize()>tx(5.5f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(95,120,133));c.drawText(etaRoute,eta.centerX(),eta.top+dp(59),text);
            RectF route=new RectF(dp(31),safeTop+dp(239),w-dp(31),safeTop+dp(307));p.setColor(Color.argb(225,248,252,254));c.drawRoundRect(route,dp(19),dp(19),p);float l=route.left+dp(35),r=route.right-dp(35),y=route.centerY()-dp(4);stroke.setStrokeWidth(dp(4));stroke.setColor(Color.rgb(205,221,228));c.drawLine(l,y,r,y,stroke);float k=smooth(t/2.7f);stroke.setColor(Color.rgb(252,190,24));c.drawLine(l,y,mix(l,r,k),y,stroke);p.setColor(Color.rgb(252,190,24));c.drawCircle(mix(l,r,k),y,dp(6),p);text.setTextSize(tx(6.6f));text.setColor(Color.rgb(92,117,130));text.setTextAlign(Paint.Align.LEFT);c.drawText("ДВІР",l,route.bottom-dp(10),text);text.setTextAlign(Paint.Align.RIGHT);c.drawText("ВОКЗАЛ",r,route.bottom-dp(10),text);
            p.setColor(Color.rgb(221,232,237));c.drawRoundRect(new RectF(dp(16),ground+dp(18),w-dp(16),ground+dp(48)),dp(12),dp(12),p);
            drawSnowman(c,w*.20f,ground,dp(32),.45f);drawSled(c,w*.20f,ground+dp(9),.78f);
            float cx=mix(w+dp(130),w*.68f,k);drawCar(c,cx,ground,1f,false);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(101,127,139));c.drawText("Uklon довозить до вокзалу • між областями їде поїзд",w/2,ground+dp(78),text);
        }

'''
section("uklon",'        void drawWait(Canvas c,float t){','        void drawBoard(Canvas c,float t){',wait_method,'Uklon wait composition')
sled_method='''        void drawSled(Canvas c,float x,float ground,float s){
            stroke.setStrokeWidth(dp(2.8f)*s);stroke.setColor(Color.rgb(96,106,112));
            c.drawArc(new RectF(x-dp(50)*s,ground-dp(2)*s,x+dp(43)*s,ground+dp(18)*s),5,165,false,stroke);
            c.drawArc(new RectF(x-dp(37)*s,ground+dp(7)*s,x+dp(55)*s,ground+dp(24)*s),5,165,false,stroke);
            p.setColor(Color.rgb(248,202,36));c.drawRoundRect(new RectF(x-dp(43)*s,ground-dp(30)*s,x+dp(43)*s,ground-dp(10)*s),dp(6)*s,dp(6)*s,p);
            p.setColor(Color.rgb(224,171,24));for(int i=-2;i<=2;i++)c.drawRoundRect(new RectF(x+i*dp(16)*s-dp(5)*s,ground-dp(31)*s,x+i*dp(16)*s+dp(5)*s,ground-dp(8)*s),dp(2)*s,dp(2)*s,p);
            stroke.setColor(Color.rgb(108,112,112));stroke.setStrokeWidth(dp(2)*s);c.drawLine(x-dp(32)*s,ground-dp(9)*s,x-dp(28)*s,ground+dp(5)*s,stroke);c.drawLine(x+dp(32)*s,ground-dp(9)*s,x+dp(28)*s,ground+dp(5)*s,stroke);
        }
'''
section("uklon",'        void drawSled(Canvas c,float x,float ground,float s){','        void drawSnowman(Canvas c,float x,float ground,float r,float wave){',sled_method,'Uklon sled readability')

# Memory room: use the intentionally reserved detail area even when no object is selected.
rep("memory",
    '            super.onDraw(c);drawRoom(c);drawHeader(c);drawCore(c);drawTravelStrip(c);drawShelves(c);drawDetail(c);drawClose(c);',
    '            super.onDraw(c);drawRoom(c);drawHeader(c);drawCore(c);drawTravelStrip(c);drawShelves(c);drawMemoryFooter(c);drawDetail(c);drawClose(c);',
    "memory footer call")
footer='''        void drawMemoryFooter(Canvas c){
            if(selected>=0)return;float w=getWidth(),bottom=getHeight()-safeBottom;RectF r=new RectF(dp(20),bottom-dp(165),w-dp(20),bottom-dp(82));p.setColor(Color.argb(242,255,255,255));c.drawRoundRect(r,dp(22),dp(22),p);
            int o=SnowmanStyle.outfit(prefs,year());String who=SnowmanStyle.character(prefs)==1?"Снігівчинка":"Сніговичок";String outfit=o==0?"без вишиванки":oblastName(o);
            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7));text.setColor(Color.rgb(124,137,136));c.drawText("ЖИТТЯ ПІСЛЯ 7 ЗИМ",r.left+dp(15),r.top+dp(20),text);
            String line=who+" • "+outfit;text.setTextSize(tx(9.2f));while(text.measureText(line)>r.width()-dp(30)&&text.getTextSize()>tx(6.4f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(62,91,98));c.drawText(line,r.left+dp(15),r.top+dp(46),text);
            text.setTextSize(tx(6.8f));text.setColor(Color.rgb(121,135,136));c.drawText("Торкнись предмета вище — тут з’явиться його історія.",r.left+dp(15),r.bottom-dp(13),text);
            if(o>0){RectF band=new RectF(r.right-dp(112),r.top+dp(18),r.right-dp(14),r.top+dp(32));SnowmanStyle.drawPatternBand(c,p,density,band,o);}
        }

'''
section("memory",'        void drawDetail(Canvas c){','        String detail(int i){',footer+'''        void drawDetail(Canvas c){
            if(selected<0||!owned(selected))return;float w=getWidth(),bottom=getHeight()-safeBottom;RectF r=new RectF(dp(20),bottom-dp(165),w-dp(20),bottom-dp(82));p.setColor(Color.argb(248,255,255,255));c.drawRoundRect(r,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);String nm=names[selected];text.setTextSize(tx(10.5f));while(text.measureText(nm)>r.width()-dp(28)&&text.getTextSize()>tx(7f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(61,82,88));c.drawText(nm,r.centerX(),r.top+dp(27),text);
            String d=detail(selected);text.setTextSize(tx(7.2f));while(text.measureText(d)>r.width()-dp(28)&&text.getTextSize()>tx(5.3f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(Color.rgb(110,128,132));c.drawText(d,r.centerX(),r.top+dp(53),text);
            text.setTextSize(tx(6.4f));text.setColor(Color.rgb(137,142,137));c.drawText("Цей спогад не тане разом із тілом.",r.centerX(),r.bottom-dp(11),text);
        }

''','memory footer/detail compact card')
rep("memory",'text.setTextSize(tx(compact?5.5f:6.2f));','text.setTextSize(tx(compact?6.0f:6.5f));','memory owned label readability')
rep("memory",'text.setTextSize(tx(compact?5.3f:6.1f));','text.setTextSize(tx(compact?5.8f:6.4f));','memory locked label readability')
rep("memory",
    'c.drawText("ПОВЕРНУТИСЯ ДО ЗИМИ",closeBtn.centerX(),closeBtn.centerY()+dp(4),text);',
    'c.drawText(year()>=7&&lived()>=7?"ПОВЕРНУТИСЯ":"ПОВЕРНУТИСЯ ДО ЗИМИ",closeBtn.centerX(),closeBtn.centerY()+dp(4),text);',
    "memory close wording after arc")

# Wardrobe: stop the 4x2 grid from colliding with the bottom button on shorter displays.
preview='''        void drawPreview(Canvas c,float w){float bottom=getHeight()-safeBottom;boolean compact=bottom-safeTop<dp(650);float top=safeTop+dp(compact?94:103),h=dp(compact?150:176);RectF r=new RectF(dp(14),top,w-dp(14),top+h);p.setColor(Color.argb(241,255,255,255));c.drawRoundRect(r,dp(23),dp(23),p);
            float x=r.left+r.width()*.27f,s=Math.min(dp(compact?37:43),r.height()*.25f),by=r.bottom-dp(compact?24:31),mr=s*.72f,hr=s*.54f,my=by-(s+mr)*.83f,hy=my-(mr+hr)*.83f;drawBall(c,x,by,s);drawBall(c,x,my,mr);drawBall(c,x,hy,hr);drawFace(c,x,hy,hr);stroke.setColor(Color.rgb(106,79,58));stroke.setStrokeWidth(dp(3));float lx=x-mr*.58f,rx=x+mr*.58f,ey=my-mr*.47f;c.drawLine(lx,my,x-mr*1.62f,ey,stroke);c.drawLine(rx,my,x+mr*1.62f,ey,stroke);if(outfit>0){drawSleeve(c,lx,my,x-mr*1.48f,ey,mr*.42f,outfit);drawSleeve(c,rx,my,x+mr*1.48f,ey,mr*.42f,outfit);}if(character==1)drawBow(c,x+hr*.55f,hy-hr*.60f,hr*.20f);else{p.setColor(Color.rgb(54,114,151));c.drawRoundRect(new RectF(x-hr*.48f,hy-hr*.78f,x+hr*.48f,hy-hr*.67f),dp(3),dp(3),p);}
            float tx0=r.left+r.width()*.52f;text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(6.8f));text.setColor(Color.rgb(118,130,126));c.drawText(outfit==0?"БАЗОВИЙ ОБРАЗ":(outfit==year?"МІСЦЕВА ВИШИВАНКА":"ВИШИВАНКА З МАНДРІВ"),tx0,r.top+dp(compact?29:37),text);
            String title=outfit==0?"Без вишиванки":names[outfit];text.setTextSize(tx(compact?13:15));while(text.measureText(title)>r.right-tx0-dp(15)&&text.getTextSize()>tx(9))text.setTextSize(text.getTextSize()-dp(.35f));text.setColor(Color.rgb(54,82,83));c.drawText(title,tx0,r.top+dp(compact?55:69),text);
            String note=notes[outfit];text.setTextSize(tx(compact?6.6f:7.4f));while(text.measureText(note)>r.right-tx0-dp(12)&&text.getTextSize()>tx(5.2f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(Color.rgb(100,120,116));c.drawText(note,tx0,r.top+dp(compact?78:96),text);text.setTextSize(tx(compact?5.8f:6.6f));text.setColor(Color.rgb(134,141,133));c.drawText("Рукави — поверх паличок-рук",tx0,r.top+dp(compact?101:120),text);c.drawText("кінчики паличок лишаються видимими.",tx0,r.top+dp(compact?119:140),text);
        }

'''
section("wardrobe",'        void drawPreview(Canvas c,float w){','        void drawGrid(Canvas c,float w,float bottom){',preview,'wardrobe compact preview')
grid='''        void drawGrid(Canvas c,float w,float bottom){boolean compact=bottom-safeTop<dp(650);float top=safeTop+dp(compact?252:290),closeTop=bottom-dp(62),gap=dp(6),left=dp(14),right=w-dp(14),avail=Math.max(dp(154),closeTop-top-dp(8)),rowH=(avail-gap*3)/4f,colW=(right-left-gap)/2f;
            if(top+rowH*4+gap*3>closeTop-dp(6))rowH=Math.max(dp(34),(closeTop-dp(6)-top-gap*3)/4f);
            for(int i=0;i<8;i++){int row=i/2,col=i%2;float l=left+col*(colW+gap),t=top+row*(rowH+gap);RectF r=outfitBtns[i];r.set(l,t,l+colW,t+rowH);boolean lock=!unlocked(i),active=outfit==i;
                p.setColor(lock?Color.argb(180,229,233,227):(active?Color.rgb(248,254,250):Color.argb(241,255,255,255)));c.drawRoundRect(r,dp(15),dp(15),p);if(active){stroke.setColor(Color.rgb(54,142,116));stroke.setStrokeWidth(dp(2));c.drawRoundRect(r,dp(15),dp(15),stroke);}
                text.setTextAlign(Paint.Align.LEFT);String nm=names[i];text.setTextSize(tx(compact?6.7f:7.4f));while(text.measureText(nm)>r.width()-dp(16)&&text.getTextSize()>tx(5.0f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(lock?Color.rgb(145,150,143):Color.rgb(63,92,91));c.drawText(nm,r.left+dp(8),r.top+dp(Math.min(17,rowH*.34f)),text);
                text.setTextSize(tx(compact?5.2f:5.8f));text.setColor(lock?Color.rgb(154,157,151):(i==year?Color.rgb(44,133,101):Color.rgb(120,133,128)));c.drawText(lock?"Ще не відкрита":(i==year?"МІСЦЕВА • ВІДКРИТО":"ВІДКРИТО"),r.left+dp(8),r.top+dp(Math.min(34,rowH*.64f)),text);
                float bandH=Math.max(dp(5),Math.min(dp(10),rowH*.16f));RectF band=new RectF(r.left+dp(8),r.bottom-bandH-dp(6),r.right-dp(8),r.bottom-dp(6));if(i>0&&!lock)drawPatternBand(c,band,i);else{p.setColor(Color.rgb(231,236,231));c.drawRoundRect(band,dp(3),dp(3),p);}
            }
        }

'''
section("wardrobe",'        void drawGrid(Canvas c,float w,float bottom){','        void drawClose(Canvas c,float w,float bottom){',grid,'wardrobe grid fit')

for key,path in paths.items():path.write_text(src[key],encoding="utf-8")
print("Applied SnowmanGame v17.1 UI audit fixes + playable school transition")
