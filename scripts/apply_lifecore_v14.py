from pathlib import Path

intro_path = Path("app/src/main/java/com/snowmangame/IntroActivity.java")
summer_path = Path("app/src/main/java/com/snowmangame/SummerActivity.java")
main_path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
intro = intro_path.read_text(encoding="utf-8")
summer = summer_path.read_text(encoding="utf-8")
main = main_path.read_text(encoding="utf-8")


def rep(src: str, old: str, new: str, label: str) -> str:
    if old not in src:
        raise SystemExit(f"v14 life-core patch failed at: {label}")
    return src.replace(old, new, 1)

# --- Origin story: one canonical viewing and a persistent living snowflake core. ---
intro = rep(
    intro,
    "import android.content.Intent;\nimport android.graphics.*;",
    "import android.content.Intent;\nimport android.content.SharedPreferences;\nimport android.graphics.*;",
    "intro SharedPreferences import",
)
intro = rep(
    intro,
    "        super.onCreate(b);\n        Window w=getWindow();",
    "        super.onCreate(b);\n        SharedPreferences storyPrefs=getSharedPreferences(\"snowman_game\",MODE_PRIVATE);\n        if(storyPrefs.getBoolean(\"origin_seen\",false)){\n            startActivity(new Intent(this,MainActivity.class));\n            finish();\n            return;\n        }\n        Window w=getWindow();",
    "show origin once",
)
intro = rep(
    intro,
    '                "Так починається наш сніговик."',
    '                "Усередині прокинулась жива сніжинка."',
    "origin final title",
)
intro = rep(
    intro,
    '                "Тепер твоя черга допомогти сніжинкам-друзям."',
    '                "Вона пам’ятатиме кожну зиму — навіть коли тіло розтане."',
    "origin final subtitle",
)
intro = rep(
    intro,
    "                for(int i=0;i<8;i++){double a=i*.91+t*.18;float rr=r*(.28f+(i%3)*.13f);c.drawCircle(cx+(float)Math.cos(a)*rr,groundY+(float)Math.sin(a)*rr,dp(1.4f+(i%2)*.6f),p);}\n            }\n        }\n\n        void drawFocusHalo",
    "                for(int i=0;i<8;i++){double a=i*.91+t*.18;float rr=r*(.28f+(i%3)*.13f);c.drawCircle(cx+(float)Math.cos(a)*rr,groundY+(float)Math.sin(a)*rr,dp(1.4f+(i%2)*.6f),p);}\n                float coreA=smooth((becomeBall-.42f)/.46f);\n                if(coreA>0)drawLifeCore(c,cx,groundY,r*.24f,coreA,t);\n            }\n        }\n\n        void drawLifeCore(Canvas c,float x,float y,float r,float alpha,float t){\n            float pulse=.92f+(float)Math.sin(t*3.2f)*.08f;int a=(int)(235*clamp(alpha,0,1));\n            RadialGradient halo=new RadialGradient(x,y,r*2.8f,new int[]{Color.argb((int)(90*alpha),118,214,255),Color.argb(0,118,214,255)},null,Shader.TileMode.CLAMP);\n            p.setShader(halo);c.drawCircle(x,y,r*2.8f,p);p.setShader(null);\n            stroke.setColor(Color.argb(a,78,167,215));stroke.setStrokeWidth(Math.max(dp(1.2f),r*.10f));\n            for(int k=0;k<6;k++){double ang=k*Math.PI/3+t*.06f;float ex=x+(float)Math.cos(ang)*r*pulse,ey=y+(float)Math.sin(ang)*r*pulse;c.drawLine(x,y,ex,ey,stroke);}\n            p.setColor(Color.argb(a,240,252,255));c.drawCircle(x,y,r*.30f,p);\n        }\n\n        void drawFocusHalo",
    "draw living core in first snowball",
)
intro = rep(
    intro,
    'c.drawText("ПОЧАТИ ЛІПИТИ",startRect.centerX(),startRect.centerY()+dp(4.5f),text);\n            text.setTextSize(tx(8.2f));text.setColor(Color.argb((int)(205*intro),75,125,150));c.drawText("Сніжинки-друзі чекають на тебе",cx,startRect.top-dp(13),text);',
    'c.drawText("ПОДАРУВАТИ ПЕРШУ ЗИМУ",startRect.centerX(),startRect.centerY()+dp(4.5f),text);\n            text.setTextSize(tx(8.2f));text.setColor(Color.argb((int)(205*intro),75,125,150));c.drawText("Мета життя: прожити разом 7 зим",cx,startRect.top-dp(13),text);',
    "origin call to action",
)
intro = rep(
    intro,
    '        void startGame(){if(leaving)return;leaving=true;startActivity(new Intent(IntroActivity.this,MainActivity.class));overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);finish();}',
    '        void startGame(){if(leaving)return;leaving=true;getSharedPreferences("snowman_game",MODE_PRIVATE).edit().putBoolean("origin_seen",true).putBoolean("life_core_awake",true).apply();startActivity(new Intent(IntroActivity.this,MainActivity.class));overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);finish();}',
    "persist origin and core",
)

# --- Summer: body melts, life-core survives, memories and items persist. ---
summer = rep(
    summer,
    "            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;\n            LinearGradient sky=",
    "            float w=getWidth(),h=getHeight(),bottom=h-safeBottom;\n            int lived=Math.max(1,year-1);\n            LinearGradient sky=",
    "summer lived winters count",
)
summer = rep(
    summer,
    'c.drawText("ЛІТО МІЖ РОКАМИ",header.left+dp(18),header.top+dp(34),text);\n            text.setTextSize(tx(9));text.setColor(Color.rgb(95,128,142));c.drawText("Рік "+year+" уже настав, але зимі треба повернутися.",header.left+dp(18),header.top+dp(61),text);\n            text.setTextSize(tx(7));text.setColor(Color.rgb(132,139,133));c.drawText("Тіло зі снігу тане • пам\'ять і речі залишаються",header.left+dp(18),header.bottom-dp(16),text);',
    'c.drawText("МІЖ ЗИМАМИ",header.left+dp(18),header.top+dp(34),text);\n            text.setTextSize(tx(9));text.setColor(Color.rgb(95,128,142));c.drawText("Прожито "+lived+"/7 зим • попереду зима "+year+"/7.",header.left+dp(18),header.top+dp(61),text);\n            text.setTextSize(tx(7));text.setColor(Color.rgb(132,139,133));c.drawText("Тіло тане • жива сніжинка пам’ятає все",header.left+dp(18),header.bottom-dp(16),text);',
    "summer life goal header",
)
summer = rep(
    summer,
    "            drawMeltingSnowman(c,w*.29f,ground,dp(42),melt);\n            drawPuddle(c,w*.29f,ground+dp(7),melt);",
    "            drawMeltingSnowman(c,w*.29f,ground,dp(42),melt);\n            drawPuddle(c,w*.29f,ground+dp(7),melt);\n            if(melt>.46f)drawLifeCore(c,w*.29f,ground-dp(22)-dp(28)*smooth((melt-.46f)/.54f),dp(12),smooth((melt-.46f)/.36f),t);",
    "summer core survives melt",
)
summer = rep(
    summer,
    'c.drawText(melt<.35f?"СОНЦЕ ГРІЄ…":melt<.85f?"СНІГОВИК ТАНЕ":"ДО ЗУСТРІЧІ ВЗИМКУ",story.centerX(),story.top+dp(35),text);\n            text.setTextSize(tx(8.2f));text.setColor(Color.rgb(103,129,141));\n            c.drawText("Наступної зими його доведеться",story.centerX(),story.top+dp(68),text);\n            c.drawText("зібрати знову — вже трохи старшим.",story.centerX(),story.top+dp(90),text);\n            text.setTextSize(tx(7.1f));text.setColor(Color.rgb(126,139,142));c.drawText("Кожен новий рік тіло трохи виростає.",story.centerX(),story.bottom-dp(18),text);',
    'c.drawText(melt<.35f?"СОНЦЕ ГРІЄ…":melt<.82f?"ТІЛО ТАНЕ":"ПАМ’ЯТЬ ЗАЛИШАЄТЬСЯ",story.centerX(),story.top+dp(35),text);\n            text.setTextSize(tx(8.2f));text.setColor(Color.rgb(103,129,141));\n            c.drawText("Жива сніжинка збереже друзів,",story.centerX(),story.top+dp(68),text);\n            c.drawText("поїздки й речі до першого снігу.",story.centerX(),story.top+dp(90),text);\n            text.setTextSize(tx(7.1f));text.setColor(Color.rgb(126,139,142));c.drawText("Нова зима — нове тіло, та сама історія.",story.centerX(),story.bottom-dp(18),text);',
    "summer story meaning",
)
summer = rep(
    summer,
    "        void drawBall(Canvas c,float x,float y,float r){",
    "        void drawLifeCore(Canvas c,float x,float y,float r,float alpha,float t){\n            float pulse=.92f+(float)Math.sin(t*3.1f)*.08f;\n            RadialGradient halo=new RadialGradient(x,y,r*3f,new int[]{Color.argb((int)(105*alpha),107,206,250),Color.argb(0,107,206,250)},null,Shader.TileMode.CLAMP);\n            p.setShader(halo);c.drawCircle(x,y,r*3f,p);p.setShader(null);\n            stroke.setColor(Color.argb((int)(235*alpha),61,154,207));stroke.setStrokeWidth(dp(1.8f));\n            for(int k=0;k<6;k++){double a=k*Math.PI/3+t*.08f;float ex=x+(float)Math.cos(a)*r*pulse,ey=y+(float)Math.sin(a)*r*pulse;c.drawLine(x,y,ex,ey,stroke);}\n            p.setColor(Color.argb((int)(245*alpha),244,253,255));c.drawCircle(x,y,r*.30f,p);\n        }\n\n        void drawBall(Canvas c,float x,float y,float r){",
    "summer life core renderer",
)
summer = rep(
    summer,
    'c.drawText("ГАРДЕРОБ ПЕРЕЖИВАЄ ЛІТО",card.left+dp(16),card.top+dp(29),text);',
    'c.drawText("РЕЧІ ПАМ’ЯТАЮТЬ ЗИМИ",card.left+dp(16),card.top+dp(29),text);',
    "summer wardrobe title",
)
summer = rep(
    summer,
    'c.drawText("ДОЧЕКАТИСЯ ПЕРШОГО СНІГУ",action.centerX(),action.centerY()+dp(4),text);',
    'c.drawText("ДОЧЕКАТИСЯ ЗИМИ "+year+"/7",action.centerX(),action.centerY()+dp(4),text);',
    "summer next winter button",
)
summer = rep(
    summer,
    'prefs.edit().putInt("summer_pending_year",0).putInt("summer_count",prefs.getInt("summer_count",0)+1).apply();',
    'prefs.edit().putInt("summer_pending_year",0).putInt("summer_count",prefs.getInt("summer_count",0)+1).putBoolean("life_core_awake",true).apply();',
    "keep core alive",
)

# --- Gameplay: make the long-term seven-winter objective visible without adding another screen. ---
main = rep(
    main,
    'c.drawText("РІК "+year+" • "+ageName(),hud.left+dp(14),hud.top+dp(18),text);',
    'c.drawText("ЗИМА "+year+"/7 • "+ageName(),hud.left+dp(14),hud.top+dp(18),text);',
    "HUD seven winter identity",
)
main = rep(
    main,
    'text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(17));text.setColor(Color.rgb(30,69,93));c.drawText("Рік "+year+" завершено",card.centerX(),card.top+dp(36),text);',
    'text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(17));text.setColor(Color.rgb(30,69,93));c.drawText("Зима "+year+" прожита",card.centerX(),card.top+dp(36),text);',
    "finish winter wording",
)
main = rep(
    main,
    'c.drawText("ЦІЛЬ "+yearGoal()+" • "+(score>=yearGoal()?"ВИКОНАНО":"ЩЕ Є КУДИ РОСТИ"),card.centerX(),card.top+dp(105),text);',
    'c.drawText("ЖИТТЯ "+year+"/7 ЗИМ • ЦІЛЬ "+yearGoal()+" • "+(score>=yearGoal()?"ВИКОНАНО":"РОСТЕМО ДАЛІ"),card.centerX(),card.top+dp(105),text);',
    "finish long-term goal",
)
main = rep(
    main,
    '            if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}\n            buzz(65);playTone(ToneGenerator.TONE_PROP_ACK,180);showFeedback(missionSuccess?"РІК ЗАВЕРШЕНО • МІСІЯ +250":"РІК ЗАВЕРШЕНО",true);invalidate();',
    '            int lived=Math.max(prefs.getInt("winters_lived",0),year);prefs.edit().putInt("winters_lived",lived).putBoolean("life_core_awake",true).apply();\n            if(score>bestScore){bestScore=score;prefs.edit().putInt("best_score",bestScore).apply();}\n            buzz(65);playTone(ToneGenerator.TONE_PROP_ACK,180);showFeedback(missionSuccess?"ЗИМА ПРОЖИТА • МІСІЯ +250":"ЗИМА ПРОЖИТА",true);invalidate();',
    "persist winters lived",
)

intro_path.write_text(intro, encoding="utf-8")
summer_path.write_text(summer, encoding="utf-8")
main_path.write_text(main, encoding="utf-8")
print("Applied SnowmanGame v14 living snowflake core + seven-winter motivation")
