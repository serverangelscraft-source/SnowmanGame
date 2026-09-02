from pathlib import Path
import re

main_path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
journey_path = Path("app/src/main/java/com/snowmangame/JourneyActivity.java")
gradle_path = Path("app/build.gradle")

main = main_path.read_text(encoding="utf-8")
journey = journey_path.read_text(encoding="utf-8")
gradle = gradle_path.read_text(encoding="utf-8")


def replace_once(text, old, new, label):
    if new in text:
        return text
    if old not in text:
        raise SystemExit(f"v18.9 patch failed: {label}")
    return text.replace(old, new, 1)


main = replace_once(
    main,
    "            float hudH=compact?dp(70):dp(78);",
    "            float hudH=compact?dp(60):dp(66);",
    "HUD height",
)

old_hud = '''        void drawHud(Canvas c){
            p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(hud,dp(21),dp(21),p);
            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(38,69,89));text.setTextSize(tx(narrow?8:9));
            c.drawText("РІК "+year+" • "+ageName(),hud.left+dp(14),hud.top+dp(18),text);
            text.setTextSize(tx(narrow?13:15));
            c.drawText(balls<3?"КУЛЯ "+(balls+1)+"/3":"ДЕКОР "+decorPlaced+"/6",hud.left+dp(14),hud.top+dp(42),text);
            text.setTextSize(tx(7.2f));text.setColor(Color.rgb(97,132,150));
            c.drawText("ЦІЛЬ "+yearGoal()+" • ЗУСИЛЛЯ ×"+String.format("%.1f",effort()),hud.left+dp(14),hud.bottom-dp(9),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(12));text.setColor(Color.rgb(34,104,146));
            c.drawText("★ "+score,hud.right-dp(14),hud.top+dp(23),text);
            text.setTextSize(tx(8.7f));text.setColor(Color.rgb(80,128,151));
            c.drawText("● "+wallet+" монет",hud.right-dp(14),hud.top+dp(43),text);
            text.setTextSize(tx(7.2f));text.setColor(Color.rgb(108,139,154));
            c.drawText(timeText(elapsed())+" • рек "+bestScore,hud.right-dp(14),hud.bottom-dp(9),text);
        }'''
new_hud = '''        void drawHud(Canvas c){
            p.setColor(Color.argb(245,255,255,255));c.drawRoundRect(hud,dp(21),dp(21),p);
            text.setTextAlign(Paint.Align.LEFT);text.setColor(Color.rgb(83,116,134));text.setTextSize(tx(narrow?6.8f:7.6f));
            c.drawText("РІК "+year+" • "+ageName(),hud.left+dp(14),hud.top+dp(18),text);
            text.setTextSize(tx(narrow?14:16));text.setColor(Color.rgb(38,69,89));
            c.drawText(balls<3?"КУЛЯ "+(balls+1)+"/3":"ДЕКОР "+decorPlaced+"/6",hud.left+dp(14),hud.bottom-dp(13),text);
            text.setTextAlign(Paint.Align.RIGHT);text.setTextSize(tx(narrow?10:11));text.setColor(Color.rgb(34,104,146));
            c.drawText("● "+wallet,hud.right-dp(14),hud.top+dp(25),text);
            text.setTextSize(tx(6.6f));text.setColor(Color.rgb(108,139,154));
            c.drawText("монети",hud.right-dp(14),hud.bottom-dp(11),text);
        }'''
main = replace_once(main, old_hud, new_hud, "simplified HUD")

main = replace_once(
    main,
    '                    if(journeyBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,DeliveryActivity.class));((Activity)ctx).finish();return true;}',
    '                    if(journeyBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,year>=7?SchoolActivity.class:DeliveryActivity.class));((Activity)ctx).finish();return true;}',
    "year 7 school route",
)

old_arrival = '''        void drawArrival(Canvas c,float t){
            if(!yearAdvanced){yearAdvanced=true;year=nextYear;prefs.edit().putInt("life_year",year).apply();int best=prefs.getInt("station_best",9999);if(stationSeconds()<best)prefs.edit().putInt("station_best",stationSeconds()).apply();}
            float w=getWidth(),bottom=getHeight()-safeBottom,a=smooth(t/.8f);drawHeader(c,"Прибуття","Вокзальний міні-рівень завершено");RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(92));p.setColor(Color.argb((int)(242*a),255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(31));text.setColor(Color.rgb(36,110,153));c.drawText("РІК "+year,w/2,card.top+dp(62),text);text.setTextSize(tx(17));text.setColor(Color.rgb(43,76,96));c.drawText(ageName(year),w/2,card.top+dp(96),text);text.setTextSize(tx(9));text.setColor(Color.rgb(92,130,148));c.drawText("Нова ціль: "+yearGoal(year)+" очок",w/2,card.top+dp(130),text);c.drawText("Вокзал: "+timeText(stationSeconds())+" • помилки: "+mistakes,w/2,card.top+dp(154),text);c.drawText("Сніг у новому році потребує більше руху й точності.",w/2,card.top+dp(180),text);if(mistakes==0){text.setTextSize(tx(10));text.setColor(Color.rgb(55,133,104));c.drawText("БЕЗ ПОМИЛОК • ЧИСТА ПОДОРОЖ",w/2,card.top+dp(215),text);}actionBtn.set(dp(22),bottom-dp(74),w-dp(22),bottom-dp(14));p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText(year>=7?"ПОЧАТИ РІВЕНЬ ШКОЛЯРА":"ПОЧАТИ РІК "+year,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }'''
new_arrival = '''        void drawArrival(Canvas c,float t){
            int previewYear=nextYear;
            float w=getWidth(),bottom=getHeight()-safeBottom,a=smooth(t/.8f);drawHeader(c,"Прибуття","Подорож завершена • перехід року ще не підтверджено");RectF card=new RectF(dp(20),safeTop+dp(145),w-dp(20),bottom-dp(92));p.setColor(Color.argb((int)(242*a),255,255,255));c.drawRoundRect(card,dp(27),dp(27),p);text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(31));text.setColor(Color.rgb(36,110,153));c.drawText("РІК "+previewYear,w/2,card.top+dp(62),text);text.setTextSize(tx(17));text.setColor(Color.rgb(43,76,96));c.drawText(ageName(previewYear),w/2,card.top+dp(96),text);text.setTextSize(tx(9));text.setColor(Color.rgb(92,130,148));c.drawText("Нова ціль: "+yearGoal(previewYear)+" очок",w/2,card.top+dp(130),text);c.drawText("Вокзал: "+timeText(stationSeconds())+" • помилки: "+mistakes,w/2,card.top+dp(154),text);c.drawText("Рік зміниться тільки після твого підтвердження.",w/2,card.top+dp(180),text);if(mistakes==0){text.setTextSize(tx(10));text.setColor(Color.rgb(55,133,104));c.drawText("БЕЗ ПОМИЛОК • ЧИСТА ПОДОРОЖ",w/2,card.top+dp(215),text);}actionBtn.set(dp(22),bottom-dp(74),w-dp(22),bottom-dp(14));p.setColor(Color.rgb(35,106,153));c.drawRoundRect(actionBtn,dp(20),dp(20),p);text.setTextSize(tx(10.5f));text.setColor(Color.WHITE);c.drawText("ПІДТВЕРДИТИ ПЕРЕХІД • РІК "+previewYear,actionBtn.centerX(),actionBtn.centerY()+dp(4),text);
        }'''
journey = replace_once(journey, old_arrival, new_arrival, "arrival confirmation screen")

journey = replace_once(
    journey,
    '''        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();hint="";invalidate();}
        void startNewYear(){Intent i=new Intent(getContext(),MainActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}''',
    '''        void switchStage(int s){stage=s;stageStart=SystemClock.elapsedRealtime();hint="";invalidate();}
        void startNewYear(){Intent i=new Intent(getContext(),MainActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}
        void startSchool(){Intent i=new Intent(getContext(),SchoolActivity.class);getContext().startActivity(i);((Activity)getContext()).finish();}
        void confirmYearAdvance(){
            if(!yearAdvanced){
                yearAdvanced=true;year=nextYear;
                SharedPreferences.Editor e=prefs.edit().putInt("life_year",year);
                int best=prefs.getInt("station_best",9999);if(stationSeconds()<best)e.putInt("station_best",stationSeconds());
                e.apply();
            }
            startNewYear();
        }''',
    "transition methods",
)

journey = replace_once(
    journey,
    '                if(year>=7&&stage==WALK_TO_CASHIER&&actionBtn.contains(x,y)){startNewYear();return true;}',
    '                if(year>=7&&stage==WALK_TO_CASHIER&&actionBtn.contains(x,y)){startSchool();return true;}',
    "journey school fallback",
)

journey = replace_once(
    journey,
    '                if(stage==ARRIVAL&&actionBtn.contains(x,y)){startNewYear();return true;}',
    '                if(stage==ARRIVAL&&actionBtn.contains(x,y)){confirmYearAdvance();return true;}',
    "arrival confirmation action",
)

gradle = re.sub(r'versionCode\s+\d+', 'versionCode 42', gradle)
gradle = re.sub(r'versionName\s+"[^"]+"', 'versionName "18.9"', gradle)

main_path.write_text(main, encoding="utf-8")
journey_path.write_text(journey, encoding="utf-8")
gradle_path.write_text(gradle, encoding="utf-8")
print("Applied SnowmanGame v18.9: simplified mobile HUD + explicit year transition + real school route")
