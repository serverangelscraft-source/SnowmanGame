from pathlib import Path

paths = {
    "main": Path("app/src/main/java/com/snowmangame/MainActivity.java"),
    "journey": Path("app/src/main/java/com/snowmangame/JourneyActivity.java"),
    "memory": Path("app/src/main/java/com/snowmangame/MemoryActivity.java"),
    "uklon": Path("app/src/main/java/com/snowmangame/UklonActivity.java"),
    "summer": Path("app/src/main/java/com/snowmangame/SummerActivity.java"),
}
src = {k: p.read_text(encoding="utf-8") for k, p in paths.items()}


def rep(key: str, old: str, new: str, label: str) -> None:
    if old not in src[key]:
        raise SystemExit(f"v16.1 graphics patch failed in {key} at: {label}")
    src[key] = src[key].replace(old, new, 1)


# Main snow-building HUD/result: fit long oblast names instead of clipping them.
rep(
    "main",
    '            c.drawText(oblastName()+" • ЦІЛЬ "+yearGoal()+" • ×"+String.format("%.1f",effort()),hud.left+dp(14),hud.bottom-dp(9),text);',
    '            String oblastHud=oblastName()+" • ЦІЛЬ "+yearGoal()+" • ×"+String.format("%.1f",effort());text.setTextSize(tx(7.2f));while(text.measureText(oblastHud)>hud.width()-dp(28)&&text.getTextSize()>tx(5.3f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(oblastHud,hud.left+dp(14),hud.bottom-dp(9),text);',
    "main oblast HUD fit",
)
rep(
    "main",
    'c.drawText(oblastName()+" • ЖИТТЯ "+year+"/7 • ЦІЛЬ "+yearGoal(),card.centerX(),card.top+dp(105),text);',
    'String finishOblast=oblastName()+" • ЖИТТЯ "+year+"/7 • ЦІЛЬ "+yearGoal();while(text.measureText(finishOblast)>card.width()-dp(34)&&text.getTextSize()>tx(6.2f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(finishOblast,card.centerX(),card.top+dp(105),text);',
    "finish oblast fit",
)

# Journey helpers: all one-line cards shrink safely; train route gets a deliberate second line.
rep(
    "journey",
    '        void drawHintBubble(Canvas c,String msg){float w=getWidth(),top=safeTop+dp(122);RectF r=new RectF(dp(18),top,w-dp(18),top+dp(62));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(r,dp(19),dp(19),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.2f));text.setColor(Color.rgb(54,93,116));c.drawText(msg,r.centerX(),r.centerY()+dp(3),text);}',
    '        void drawHintBubble(Canvas c,String msg){float w=getWidth(),top=safeTop+dp(122);RectF r=new RectF(dp(18),top,w-dp(18),top+dp(62));p.setColor(Color.argb(238,255,255,255));c.drawRoundRect(r,dp(19),dp(19),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(8.2f));while(text.measureText(msg)>r.width()-dp(24)&&text.getTextSize()>tx(5.8f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(54,93,116));c.drawText(msg,r.centerX(),r.centerY()+dp(3),text);}',
    "hint bubble fit",
)
rep(
    "journey",
    '        void drawBottomHint(Canvas c,String msg){float bottom=getHeight()-safeBottom,top=bottom-dp(66);actionBtn.set(dp(18),top,getWidth()-dp(18),bottom-dp(10));p.setColor(Color.argb(240,37,106,151));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.5f));text.setColor(Color.WHITE);c.drawText(msg,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);}',
    '        void drawBottomHint(Canvas c,String msg){float bottom=getHeight()-safeBottom,top=bottom-dp(66);actionBtn.set(dp(18),top,getWidth()-dp(18),bottom-dp(10));p.setColor(Color.argb(240,37,106,151));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(9.5f));while(text.measureText(msg)>actionBtn.width()-dp(26)&&text.getTextSize()>tx(6.3f))text.setTextSize(text.getTextSize()-dp(.3f));text.setColor(Color.WHITE);c.drawText(msg,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);}',
    "bottom action fit",
)
rep(
    "journey",
    'text.setTextSize(tx(7.2f));text.setColor(Color.rgb(108,140,157));c.drawText("ЗИМА "+year+"/7 • "+oblastName(year),card.right-dp(16),card.top+dp(57),text);',
    'String headerOblast="ЗИМА "+year+"/7 • "+oblastName(year);text.setTextSize(tx(7.2f));while(text.measureText(headerOblast)>card.width()*.48f&&text.getTextSize()>tx(5.2f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(108,140,157));c.drawText(headerOblast,card.right-dp(16),card.top+dp(57),text);',
    "journey header fit",
)
rep(
    "journey",
    'text.setTextSize(tx(7.2f));text.setColor(Color.rgb(88,120,139));c.drawText("КВИТОК В ОБЛАСТЬ",ticketChoices[i].centerX(),ticketChoices[i].top+dp(20),text);text.setTextSize(tx(8.2f));text.setColor(Color.rgb(35,103,150));c.drawText(opts[i],ticketChoices[i].centerX(),ticketChoices[i].centerY()+dp(5),text);',
    'text.setTextSize(tx(7.2f));while(text.measureText("КВИТОК В ОБЛАСТЬ")>ticketChoices[i].width()-dp(8)&&text.getTextSize()>tx(5.1f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(Color.rgb(88,120,139));c.drawText("КВИТОК В ОБЛАСТЬ",ticketChoices[i].centerX(),ticketChoices[i].top+dp(20),text);text.setTextSize(tx(8.2f));while(text.measureText(opts[i])>ticketChoices[i].width()-dp(8)&&text.getTextSize()>tx(5.0f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(35,103,150));c.drawText(opts[i],ticketChoices[i].centerX(),ticketChoices[i].centerY()+dp(5),text);',
    "ticket card fit",
)
rep(
    "journey",
    'text.setTextSize(tx(7)*scale);text.setColor(Color.rgb(79,111,128));c.drawText(oblastName(year)+" → "+oblastName(nextYear)+" • ЗИМА "+nextYear+"/7",ticketRect.left+dp(12),ticketRect.top+dp(42),text);',
    'String ticketRoute=oblastName(year)+" → "+oblastName(nextYear)+" • ЗИМА "+nextYear+"/7";text.setTextSize(tx(7)*scale);while(text.measureText(ticketRoute)>ticketRect.width()-dp(24)*scale&&text.getTextSize()>tx(4.9f)*scale)text.setTextSize(text.getTextSize()-dp(.22f)*scale);text.setColor(Color.rgb(79,111,128));c.drawText(ticketRoute,ticketRect.left+dp(12),ticketRect.top+dp(42),text);',
    "printed ticket route fit",
)
rep(
    "journey",
    'text.setTextSize(tx(13));text.setTextColor(Color.WHITE);',
    'text.setTextSize(tx(13));text.setTextColor(Color.WHITE);',
    "noop guard",
) if False else None
rep(
    "journey",
    'text.setTextSize(tx(13));text.setColor(Color.WHITE);c.drawText("ВАГОН "+wagonTarget+" • "+oblastName(year)+" → "+oblastName(nextYear),w/2,safeTop+dp(43),text);',
    'text.setTextSize(tx(10));text.setColor(Color.WHITE);String trainTitle="ВАГОН "+wagonTarget+" • МАНДРИ УКРАЇНОЮ";while(text.measureText(trainTitle)>w-dp(30)&&text.getTextSize()>tx(7f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(trainTitle,w/2,safeTop+dp(30),text);String trainRoute=oblastName(year)+" → "+oblastName(nextYear);text.setTextSize(tx(8.6f));while(text.measureText(trainRoute)>w-dp(30)&&text.getTextSize()>tx(6f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(trainRoute,w/2,safeTop+dp(53),text);',
    "two-line train route",
)
rep(
    "journey",
    'text.setTextSize(tx(9.5f));text.setColor(Color.rgb(49,74,88));c.drawText("Тук-тук… наступну зиму ліпитимемо вже в іншій області.",w/2,bottom*.57f,text);',
    'String trainStory="Тук-тук… наступну зиму ліпитимемо вже в іншій області.";text.setTextSize(tx(9.5f));while(text.measureText(trainStory)>w-dp(34)&&text.getTextSize()>tx(6.4f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(49,74,88));c.drawText(trainStory,w/2,bottom*.57f,text);',
    "train story fit",
)
rep(
    "journey",
    'text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(24));text.setColor(Color.rgb(36,110,153));c.drawText(oblastName(year),w/2,card.top+dp(58),text);',
    'text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(24));while(text.measureText(oblastName(year))>card.width()-dp(38)&&text.getTextSize()>tx(15f))text.setTextSize(text.getTextSize()-dp(.4f));text.setColor(Color.rgb(36,110,153));c.drawText(oblastName(year),w/2,card.top+dp(58),text);',
    "arrival oblast fit",
)
rep(
    "journey",
    'text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText(year>=7?"ШКОЛА • "+oblastName(year):"ЛІПИТИ • "+oblastName(year),actionBtn.centerX(),actionBtn.centerY()+dp(4),text);',
    'String arrivalAction=year>=7?"ШКОЛА • "+oblastName(year):"ЛІПИТИ • "+oblastName(year);text.setTextSize(tx(10.5f));while(text.measureText(arrivalAction)>actionBtn.width()-dp(28)&&text.getTextSize()>tx(7f))text.setTextSize(text.getTextSize()-dp(.3f));text.setColor(Color.WHITE);c.drawText(arrivalAction,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);',
    "arrival button fit",
)
rep(
    "journey",
    'text.setTextSize(tx(9.5f));text.setColor(Color.rgb(84,124,145));c.drawText("7 зим • 7 областей • маршрут України пройдено",w/2,card.top+dp(98),text);',
    'String schoolRoute="7 зим • 7 областей • маршрут України пройдено";text.setTextSize(tx(9.5f));while(text.measureText(schoolRoute)>card.width()-dp(30)&&text.getTextSize()>tx(6.4f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(84,124,145));c.drawText(schoolRoute,w/2,card.top+dp(98),text);',
    "school route fit",
)

# Uklon: long oblast names fit in the top card, subtitle and local-route ETA card.
rep(
    "uklon",
    'c.drawText("ЗИМА "+year+"/7 • "+oblastName(),card.left+dp(17),card.top+dp(20),text);',
    'String uklonOblast="ЗИМА "+year+"/7 • "+oblastName();while(text.measureText(uklonOblast)>card.width()*.58f&&text.getTextSize()>tx(5.3f))text.setTextSize(text.getTextSize()-dp(.22f));c.drawText(uklonOblast,card.left+dp(17),card.top+dp(20),text);',
    "Uklon header fit",
)
rep(
    "uklon",
    'text.setTextSize(tx(8.2f));text.setColor(Color.rgb(89,118,133));c.drawText(sub,card.left+dp(17),card.top+dp(73),text);',
    'text.setTextSize(tx(8.2f));while(text.measureText(sub)>card.width()-dp(34)&&text.getTextSize()>tx(5.5f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(89,118,133));c.drawText(sub,card.left+dp(17),card.top+dp(73),text);',
    "Uklon subtitle fit",
)
rep(
    "uklon",
    'text.setTextSize(tx(8));text.setColor(Color.rgb(95,120,133));c.drawText("Маршрут по "+oblastName()+": Двір → Вокзал",eta.centerX(),eta.top+dp(59),text);',
    'String etaRoute="Маршрут по "+oblastName()+": Двір → Вокзал";text.setTextSize(tx(8));while(text.measureText(etaRoute)>eta.width()-dp(24)&&text.getTextSize()>tx(5.5f))text.setTextSize(text.getTextSize()-dp(.25f));text.setColor(Color.rgb(95,120,133));c.drawText(etaRoute,eta.centerX(),eta.top+dp(59),text);',
    "Uklon ETA route fit",
)

# Summer: keep the next-oblast line inside the header on compact devices.
rep(
    "summer",
    'c.drawText("Прожито "+lived+"/7 • далі "+oblastName()+", зима "+year+"/7.",header.left+dp(18),header.top+dp(61),text);',
    'String summerRoute="Прожито "+lived+"/7 • далі "+oblastName()+", зима "+year+"/7.";while(text.measureText(summerRoute)>header.width()-dp(36)&&text.getTextSize()>tx(5.8f))text.setTextSize(text.getTextSize()-dp(.25f));c.drawText(summerRoute,header.left+dp(18),header.top+dp(61),text);',
    "summer oblast fit",
)

# Memory room: compress shelves only when height is tight and keep the route strip readable.
rep(
    "memory",
    '            text.setTextAlign(Paint.Align.LEFT);text.setTextSize(tx(7.4f));text.setColor(Color.rgb(75,105,116));c.drawText("МАНДРИ УКРАЇНОЮ • "+visitedCount()+"/7 ОБЛАСТЕЙ",r.left+dp(12),r.top+dp(18),text);\n            text.setTextSize(tx(6.6f));text.setColor(Color.rgb(113,128,131));c.drawText("Зараз: "+oblastName(year()),r.left+dp(12),r.bottom-dp(11),text);',
    '            text.setTextAlign(Paint.Align.LEFT);String travelTitle="МАНДРИ УКРАЇНОЮ • "+visitedCount()+"/7 ОБЛАСТЕЙ";text.setTextSize(tx(7.4f));while(text.measureText(travelTitle)>r.width()-dp(136)&&text.getTextSize()>tx(5.4f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(Color.rgb(75,105,116));c.drawText(travelTitle,r.left+dp(12),r.top+dp(18),text);\n            String travelNow="Зараз: "+oblastName(year());text.setTextSize(tx(6.6f));while(text.measureText(travelNow)>r.width()-dp(136)&&text.getTextSize()>tx(5f))text.setTextSize(text.getTextSize()-dp(.2f));text.setColor(Color.rgb(113,128,131));c.drawText(travelNow,r.left+dp(12),r.bottom-dp(11),text);',
    "memory travel strip fit",
)
rep(
    "memory",
    '            float left=dp(18),right=w-dp(18),top=safeTop+dp(285),shelfGap=dp(142),colW=(right-left)/4f;\n            for(int row=0;row<2;row++){\n                float sy=top+row*shelfGap+dp(88);',
    '            float left=dp(18),right=w-dp(18),availableH=bottom-safeTop;boolean compact=availableH<dp(720);float top=safeTop+dp(compact?260:285),rowOffset=dp(compact?68:88),maxGap=bottom-dp(176)-top-rowOffset-dp(22),shelfGap=compact?clamp(maxGap,dp(70),dp(118)):dp(142),colW=(right-left)/4f;\n            for(int row=0;row<2;row++){\n                float sy=top+row*shelfGap+rowOffset;',
    "compact shelf geometry",
)
rep(
    "memory",
    '                    int i=row*4+col;float cx=left+colW*(col+.5f),cy=sy-dp(43);slots[i].set(cx-colW*.45f,cy-dp(49),cx+colW*.45f,sy+dp(28));',
    '                    int i=row*4+col;float cx=left+colW*(col+.5f),cy=sy-dp(compact?35:43);slots[i].set(cx-colW*.45f,cy-dp(compact?40:49),cx+colW*.45f,sy+dp(compact?22:28));',
    "compact shelf slots",
)
rep(
    "memory",
    'if(owned(i)){drawObject(c,i,cx,cy);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(6.2f));text.setColor(Color.rgb(81,91,91));drawLabel(c,names[i],cx,sy+dp(28),colW*.90f);}',
    'if(owned(i)){if(compact){c.save();c.scale(.86f,.86f,cx,cy);}drawObject(c,i,cx,cy);if(compact)c.restore();text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(compact?5.5f:6.2f));text.setColor(Color.rgb(81,91,91));drawLabel(c,names[i],cx,sy+dp(compact?21:28),colW*.90f);}',
    "compact shelf objects and labels",
)
rep(
    "memory",
    'else{p.setColor(Color.argb(60,92,96,94));c.drawCircle(cx,cy,dp(18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(6.1f));text.setColor(Color.rgb(143,143,137));c.drawText("ще не спогад",cx,sy+dp(28),text);}',
    'else{p.setColor(Color.argb(60,92,96,94));c.drawCircle(cx,cy,dp(compact?15:18),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(compact?5.3f:6.1f));text.setColor(Color.rgb(143,143,137));c.drawText("ще не спогад",cx,sy+dp(compact?21:28),text);}',
    "compact locked memory",
)

for key, path in paths.items():
    path.write_text(src[key], encoding="utf-8")
print("Applied SnowmanGame v16.1 adaptive oblast graphics + compact memory layout")
