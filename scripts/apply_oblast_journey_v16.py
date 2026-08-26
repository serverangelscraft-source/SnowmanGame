from pathlib import Path

main_path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
journey_path = Path("app/src/main/java/com/snowmangame/JourneyActivity.java")
memory_path = Path("app/src/main/java/com/snowmangame/MemoryActivity.java")
uklon_path = Path("app/src/main/java/com/snowmangame/UklonActivity.java")
summer_path = Path("app/src/main/java/com/snowmangame/SummerActivity.java")

main = main_path.read_text(encoding="utf-8")
journey = journey_path.read_text(encoding="utf-8")
memory = memory_path.read_text(encoding="utf-8")
uklon = uklon_path.read_text(encoding="utf-8")
summer = summer_path.read_text(encoding="utf-8")


def rep(src: str, old: str, new: str, label: str) -> str:
    if old not in src:
        raise SystemExit(f"v16 oblast journey patch failed at: {label}")
    return src.replace(old, new, 1)

# --- Main snow-building screen: every winter now belongs to a concrete oblast. ---
main = rep(
    main,
    '        String missionText(){',
    '        String oblastName(){switch(year){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}\n        String missionText(){',
    "main oblast helper",
)
main = rep(
    main,
    '            c.drawText("ЦІЛЬ "+yearGoal()+" • ЗУСИЛЛЯ ×"+String.format("%.1f",effort()),hud.left+dp(14),hud.bottom-dp(9),text);',
    '            c.drawText(oblastName()+" • ЦІЛЬ "+yearGoal()+" • ×"+String.format("%.1f",effort()),hud.left+dp(14),hud.bottom-dp(9),text);',
    "main HUD oblast",
)
main = rep(
    main,
    'c.drawText("ЖИТТЯ "+year+"/7 ЗИМ • ЦІЛЬ "+yearGoal()+" • "+(score>=yearGoal()?"ВИКОНАНО":"РОСТЕМО ДАЛІ"),card.centerX(),card.top+dp(105),text);',
    'c.drawText(oblastName()+" • ЖИТТЯ "+year+"/7 • ЦІЛЬ "+yearGoal(),card.centerX(),card.top+dp(105),text);',
    "finish oblast identity",
)

# --- Journey: travel between oblasts instead of abstractly travelling to a number. ---
journey = rep(
    journey,
    '        int yearGoal(int y){return 1100+(y-1)*320;}\n',
    '        int yearGoal(int y){return 1100+(y-1)*320;}\n        String oblastName(int y){switch(Math.max(1,Math.min(7,y))){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}\n        int correctTicketSlot(){return (year+1)%3;}\n        String[] oblastOptions(){\n            String target=oblastName(nextYear);\n            String wrongA=oblastName(nextYear%7+1);\n            String wrongB=oblastName((nextYear+2)%7+1);\n            String[] out=new String[3];int c=correctTicketSlot(),w=0;\n            for(int i=0;i<3;i++){if(i==c)out[i]=target;else out[i]=(w++==0?wrongA:wrongB);}\n            return out;\n        }\n',
    "journey oblast helpers",
)
journey = rep(
    journey,
    '            nextYear=Math.min(7,year+1);\n            ticketCost=year==1?0:Math.min(12,(year-1)*2);',
    '            nextYear=Math.min(7,year+1);\n            if(!prefs.contains("current_oblast")){int mask=(1<<year)-1;prefs.edit().putString("current_oblast",oblastName(year)).putInt("visited_oblasts_mask",mask).putBoolean("oblast_route_v16",true).apply();}\n            ticketCost=year==1?0:Math.min(12,(year-1)*2);',
    "journey route migration",
)
journey = rep(
    journey,
    'text.setTextSize(tx(7.2f));text.setColor(Color.rgb(108,140,157));c.drawText("РІК "+year+" • "+ageName(year),card.right-dp(16),card.top+dp(57),text);',
    'text.setTextSize(tx(7.2f));text.setColor(Color.rgb(108,140,157));c.drawText("ЗИМА "+year+"/7 • "+oblastName(year),card.right-dp(16),card.top+dp(57),text);',
    "journey header oblast",
)
journey = rep(
    journey,
    '        String stationSpeech(){return ticketCost==0?"Мій перший переїзд: квиток для Малюка — 0 монет.":"Я вже "+ageName(year)+". До року "+nextYear+" квиток коштує "+ticketCost+" монет.";}',
    '        String stationSpeech(){return ticketCost==0?"Перший маршрут: "+oblastName(year)+" → "+oblastName(nextYear)+". Квиток — 0 монет.":"Їду далі Україною: "+oblastName(year)+" → "+oblastName(nextYear)+". Квиток: "+ticketCost+" монет.";}',
    "station route speech",
)
journey = rep(
    journey,
    '            float w=getWidth(),bottom=getHeight()-safeBottom;drawHeader(c,"Каса","Крок 2/4 • вибери правильний рік призначення");drawHintBubble(c,"Потрібен квиток: Рік "+year+" → Рік "+nextYear+". Не переплутай.");\n            int wrong1=nextYear>=7?Math.max(1,nextYear-2):nextYear+1,wrong2=nextYear<=2?nextYear+2:nextYear-1;int[] opts={wrong1,nextYear,wrong2};float gap=dp(10),left=dp(18),right=w-dp(18),top=safeTop+dp(215),hh=dp(82),ww=(right-left-gap*2)/3f;\n            for(int i=0;i<3;i++){ticketChoices[i].set(left+i*(ww+gap),top,left+i*(ww+gap)+ww,top+hh);p.setColor(Color.WHITE);c.drawRoundRect(ticketChoices[i],dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(88,120,139));c.drawText("КВИТОК",ticketChoices[i].centerX(),ticketChoices[i].top+dp(22),text);text.setTextSize(tx(18));text.setColor(Color.rgb(35,103,150));c.drawText("РІК "+opts[i],ticketChoices[i].centerX(),ticketChoices[i].centerY()+dp(8),text);text.setTextSize(tx(7));text.setColor(Color.rgb(110,140,154));c.drawText("платформа "+(1+(opts[i]%2)),ticketChoices[i].centerX(),ticketChoices[i].bottom-dp(12),text);}\n            RectF card=new RectF(dp(28),top+hh+dp(24),w-dp(28),top+hh+dp(93));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(card,dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(57,91,111));c.drawText(ticketCost==0?"Перший квиток: 0 монет":"Ціна правильного квитка: "+ticketCost+" монет",w/2,card.top+dp(26),text);text.setTextSize(tx(8));text.setColor(wallet>=ticketCost?Color.rgb(55,132,103):Color.rgb(170,73,73));c.drawText(wallet>=ticketCost?"У гаманці достатньо: "+wallet:"Не вистачає "+(ticketCost-wallet)+" монет",w/2,card.bottom-dp(15),text);drawBottomHint(c,wallet>=ticketCost?"Обери квиток у РІК "+nextYear:"ПОВЕРНУТИСЯ ЗАРОБИТИ МОНЕТИ");',
    '            float w=getWidth(),bottom=getHeight()-safeBottom;drawHeader(c,"Каса","Крок 2/4 • вибери область призначення");drawHintBubble(c,"Маршрут цієї зими: "+oblastName(year)+" → "+oblastName(nextYear)+".");\n            String[] opts=oblastOptions();float gap=dp(10),left=dp(18),right=w-dp(18),top=safeTop+dp(215),hh=dp(82),ww=(right-left-gap*2)/3f;\n            for(int i=0;i<3;i++){ticketChoices[i].set(left+i*(ww+gap),top,left+i*(ww+gap)+ww,top+hh);p.setColor(Color.WHITE);c.drawRoundRect(ticketChoices[i],dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7.2f));text.setColor(Color.rgb(88,120,139));c.drawText("КВИТОК В ОБЛАСТЬ",ticketChoices[i].centerX(),ticketChoices[i].top+dp(20),text);text.setTextSize(tx(8.2f));text.setColor(Color.rgb(35,103,150));c.drawText(opts[i],ticketChoices[i].centerX(),ticketChoices[i].centerY()+dp(5),text);text.setTextSize(tx(6.6f));text.setColor(Color.rgb(110,140,154));c.drawText("зима "+nextYear+"/7",ticketChoices[i].centerX(),ticketChoices[i].bottom-dp(12),text);}\n            RectF card=new RectF(dp(28),top+hh+dp(24),w-dp(28),top+hh+dp(93));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(card,dp(18),dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9));text.setColor(Color.rgb(57,91,111));c.drawText(ticketCost==0?"Перший переїзд: 0 монет":"Квиток до "+oblastName(nextYear)+": "+ticketCost+" монет",w/2,card.top+dp(26),text);text.setTextSize(tx(8));text.setColor(wallet>=ticketCost?Color.rgb(55,132,103):Color.rgb(170,73,73));c.drawText(wallet>=ticketCost?"У гаманці достатньо: "+wallet:"Не вистачає "+(ticketCost-wallet)+" монет",w/2,card.bottom-dp(15),text);drawBottomHint(c,wallet>=ticketCost?"ОБЕРИ "+oblastName(nextYear):"ПОВЕРНУТИСЯ ЗАРОБИТИ МОНЕТИ");',
    "oblast ticket chooser",
)
journey = rep(
    journey,
    'text.setTextSize(tx(7)*scale);text.setColor(Color.rgb(79,111,128));c.drawText("ІГРОВИЙ КВИТОК • РІК "+nextYear,ticketRect.left+dp(12),ticketRect.top+dp(42),text);',
    'text.setTextSize(tx(7)*scale);text.setColor(Color.rgb(79,111,128));c.drawText(oblastName(year)+" → "+oblastName(nextYear)+" • ЗИМА "+nextYear+"/7",ticketRect.left+dp(12),ticketRect.top+dp(42),text);',
    "ticket route print",
)
journey = rep(
    journey,
    'c.drawText(t<1.7f?"Сніговик йде до дверей…":"Наступна зупинка — новий рік.",w/2,safeTop+dp(148),text);',
    'c.drawText(t<1.7f?"Сніговик йде до дверей…":"Наступна зупинка — "+oblastName(nextYear)+".",w/2,safeTop+dp(148),text);',
    "boarding oblast destination",
)
journey = rep(
    journey,
    'c.drawText("ВАГОН "+wagonTarget+" • У РІК "+nextYear,w/2,safeTop+dp(43),text);',
    'c.drawText("ВАГОН "+wagonTarget+" • "+oblastName(year)+" → "+oblastName(nextYear),w/2,safeTop+dp(43),text);',
    "train route title",
)
journey = rep(
    journey,
    'c.drawText("Тук-тук… після вокзалу новий рік стане складнішим.",w/2,bottom*.57f,text);',
    'c.drawText("Тук-тук… наступну зиму ліпитимемо вже в іншій області.",w/2,bottom*.57f,text);',
    "train route story",
)
journey = rep(
    journey,
    '            if(!yearAdvanced){yearAdvanced=true;year=nextYear;prefs.edit().putInt("life_year",year).putInt("summer_pending_year",year).apply();int best=prefs.getInt("station_best",9999);if(stationSeconds()<best)prefs.edit().putInt("station_best",stationSeconds()).apply();}',
    '            if(!yearAdvanced){yearAdvanced=true;int fromYear=year;year=nextYear;int visited=prefs.getInt("visited_oblasts_mask",1)|(1<<(year-1));prefs.edit().putInt("life_year",year).putInt("summer_pending_year",year).putString("current_oblast",oblastName(year)).putInt("visited_oblasts_mask",visited).putString("last_oblast_route",oblastName(fromYear)+" → "+oblastName(year)).putBoolean("oblast_route_v16",true).apply();int best=prefs.getInt("station_best",9999);if(stationSeconds()<best)prefs.edit().putInt("station_best",stationSeconds()).apply();}',
    "persist oblast arrival",
)
journey = rep(
    journey,
    'text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(31));text.setColor(Color.rgb(36,110,153));c.drawText("РІК "+year,w/2,card.top+dp(62),text);text.setTextSize(tx(17));text.setColor(Color.rgb(43,76,96));c.drawText(ageName(year),w/2,card.top+dp(96),text);',
    'text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(24));text.setColor(Color.rgb(36,110,153));c.drawText(oblastName(year),w/2,card.top+dp(58),text);text.setTextSize(tx(14));text.setColor(Color.rgb(43,76,96));c.drawText("ЗИМА "+year+"/7 • "+ageName(year),w/2,card.top+dp(94),text);',
    "arrival oblast card",
)
journey = rep(
    journey,
    'c.drawText("Сніг у новому році потребує більше руху й точності.",w/2,card.top+dp(180),text);',
    'c.drawText("Нова область — новий сніг, але пам’ять сніговика та сама.",w/2,card.top+dp(180),text);',
    "arrival region story",
)
journey = rep(
    journey,
    'c.drawText(year>=7?"ПОЧАТИ РІВЕНЬ ШКОЛЯРА":"ПОЧАТИ РІК "+year,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);',
    'c.drawText(year>=7?"ШКОЛА • "+oblastName(year):"ЛІПИТИ • "+oblastName(year),actionBtn.centerX(),actionBtn.centerY()+dp(4),text);',
    "arrival action oblast",
)
journey = rep(
    journey,
    'c.drawText("7 років зимових пригод пройдено",w/2,card.top+dp(98),text);',
    'c.drawText("7 зим • 7 областей • маршрут України пройдено",w/2,card.top+dp(98),text);',
    "school route milestone",
)
journey = rep(
    journey,
    '            int wrong1=nextYear>=7?Math.max(1,nextYear-2):nextYear+1,wrong2=nextYear<=2?nextYear+2:nextYear-1;int[] opts={wrong1,nextYear,wrong2};\n            for(int i=0;i<3;i++)if(ticketChoices[i].contains(x,y)){if(opts[i]==nextYear){wallet-=ticketCost;ticketOwned=true;prefs.edit().putInt("coins",wallet).apply();ticketX=getWidth()*.30f;ticketY=getHeight()-safeBottom-dp(145);switchStage(VALIDATE_TICKET);}else{mistakes++;hint="Це не той рік. Потрібен РІК "+nextYear;}invalidate();return;}',
    '            String[] opts=oblastOptions();\n            for(int i=0;i<3;i++)if(ticketChoices[i].contains(x,y)){if(opts[i].equals(oblastName(nextYear))){wallet-=ticketCost;ticketOwned=true;prefs.edit().putInt("coins",wallet).apply();ticketX=getWidth()*.30f;ticketY=getHeight()-safeBottom-dp(145);switchStage(VALIDATE_TICKET);}else{mistakes++;hint="Не туди. Наступна область — "+oblastName(nextYear);}invalidate();return;}',
    "oblast ticket validation",
)

# --- Summer transition: explicitly announce where the next winter will happen. ---
summer = rep(
    summer,
    '        float dp(float v){return v*density;} float tx(float v){return v*textScale;}',
    '        float dp(float v){return v*density;} float tx(float v){return v*textScale;}\n        String oblastName(){switch(year){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}',
    "summer oblast helper",
)
summer = rep(
    summer,
    'c.drawText("Прожито "+lived+"/7 зим • попереду зима "+year+"/7.",header.left+dp(18),header.top+dp(61),text);',
    'c.drawText("Прожито "+lived+"/7 • далі "+oblastName()+", зима "+year+"/7.",header.left+dp(18),header.top+dp(61),text);',
    "summer next oblast",
)

# --- Uklon remains local transport: yard -> station inside the current oblast. ---
uklon = rep(
    uklon,
    '        String age(){switch(year){case 3:return"Пустун";case 4:return"Помічник";case 5:return"Майстер снігу";case 6:return"Майбутній школяр";default:return"Школяр";}}',
    '        String age(){switch(year){case 3:return"Пустун";case 4:return"Помічник";case 5:return"Майстер снігу";case 6:return"Майбутній школяр";default:return"Школяр";}}\n        String oblastName(){switch(year){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}',
    "Uklon oblast helper",
)
uklon = rep(
    uklon,
    'c.drawText("РІК "+year+" • "+age(),card.left+dp(17),card.top+dp(20),text);',
    'c.drawText("ЗИМА "+year+"/7 • "+oblastName(),card.left+dp(17),card.top+dp(20),text);',
    "Uklon current oblast",
)
uklon = rep(
    uklon,
    'c.drawText("Маршрут: Двір → Вокзал",eta.centerX(),eta.top+dp(59),text);',
    'c.drawText("Маршрут по "+oblastName()+": Двір → Вокзал",eta.centerX(),eta.top+dp(59),text);',
    "Uklon local route",
)
uklon = rep(
    uklon,
    '            drawTop(c,"Їдемо до вокзалу","Санчата лишилися вдома — сьогодні працює водій");',
    '            drawTop(c,"Їдемо до вокзалу","По "+oblastName()+" до станції; між областями — вже поїзд");',
    "Uklon explains local role",
)

# --- Memory room: add a compact route strip so travel becomes part of the biography. ---
memory = rep(
    memory,
    '        int lived(){return Math.max(prefs.getInt("winters_lived",0),Math.max(0,year()-1));}\n',
    '        int lived(){return Math.max(prefs.getInt("winters_lived",0),Math.max(0,year()-1));}\n        String oblastName(int y){switch(Math.max(1,Math.min(7,y))){case 1:return "Київщина";case 2:return "Черкащина";case 3:return "Кіровоградщина";case 4:return "Одещина";case 5:return "Вінниччина";case 6:return "Хмельниччина";default:return "Тернопільщина";}}\n        int visitedCount(){int mask=prefs.getInt("visited_oblasts_mask",(1<<year())-1),n=0;for(int i=0;i<7;i++)if((mask&(1<<i))!=0)n++;return Math.max(1,n);}\n',
    "memory oblast helpers",
)
memory = rep(
    memory,
    '            super.onDraw(c);drawRoom(c);drawHeader(c);drawCore(c);drawShelves(c);drawDetail(c);drawClose(c);',
    '            super.onDraw(c);drawRoom(c);drawHeader(c);drawCore(c);drawTravelStrip(c);drawShelves(c);drawDetail(c);drawClose(c);',
    "memory route strip call",
)
memory = rep(
    memory,
    '        void drawShelves(Canvas c){',
    '        void drawTravelStrip(Canvas c){\n            float w=getWidth(),top=safeTop+dp(210);RectF r=new RectF(dp(18),top,w-dp(18),top+dp(58));p.setColor(Color.argb(236,255,255,255));c.drawRoundRect(r,dp(17),dp(17),p);\n            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(75,105,116));c.drawText("МАНДРИ УКРАЇНОЮ • "+visitedCount()+"/7 ОБЛАСТЕЙ",r.left+dp(12),r.top+dp(18),text);\n            text.setTextSize(tx(6.6f));text.setColor(Color.rgb(113,128,131));c.drawText("Зараз: "+oblastName(year()),r.left+dp(12),r.bottom-dp(11),text);\n            float x0=r.right-dp(112),gap=dp(15),cy=r.centerY()+dp(1);int mask=prefs.getInt("visited_oblasts_mask",(1<<year())-1);for(int i=0;i<7;i++){boolean v=(mask&(1<<i))!=0;p.setColor(v?Color.rgb(64,148,184):Color.rgb(203,211,213));c.drawCircle(x0+i*gap,cy,dp(v?4.2f:3.2f),p);}\n        }\n\n        void drawShelves(Canvas c){',
    "memory route strip renderer",
)

main_path.write_text(main, encoding="utf-8")
journey_path.write_text(journey, encoding="utf-8")
memory_path.write_text(memory, encoding="utf-8")
uklon_path.write_text(uklon, encoding="utf-8")
summer_path.write_text(summer, encoding="utf-8")
print("Applied SnowmanGame v16 Ukraine oblast journey")
