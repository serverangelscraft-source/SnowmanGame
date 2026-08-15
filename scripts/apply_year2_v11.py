from pathlib import Path

path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
s = path.read_text(encoding="utf-8")

def rep(old: str, new: str, label: str) -> None:
    global s
    if old not in s:
        raise SystemExit(f"v11 year2 patch failed at: {label}")
    s = s.replace(old, new, 1)

rep(
    "        if(Build.VERSION.SDK_INT>=29) w.setNavigationBarContrastEnforced(false);\n        setContentView(new SnowmanView(this));",
    "        if(Build.VERSION.SDK_INT>=29) w.setNavigationBarContrastEnforced(false);\n        if(!getIntent().getBooleanExtra(\"skip_year2_story\",false)){\n            SharedPreferences progress=getSharedPreferences(\"snowman_game\",MODE_PRIVATE);\n            int storyYear=Math.max(1,Math.min(7,progress.getInt(\"life_year\",1)));\n            if(storyYear==2&&!progress.getBoolean(\"year2_story_complete\",false)){\n                startActivity(new Intent(this,YearTwoActivity.class));\n                finish();\n                return;\n            }\n        }\n        setContentView(new SnowmanView(this));",
    "year2 one-time routing",
)

rep(
    "        String missionText(){\n            int acc=Math.min(94,84+year),sec=Math.max(58,94-year*4);\n            if(mission==0)return \"МІСІЯ: точність куль ≥ \"+acc+\"%\";\n            if(mission==1)return \"МІСІЯ: завершити ≤ \"+sec+\" с\";\n            return \"МІСІЯ: декор ≥ \"+acc+\"%\";\n        }",
    "        String missionText(){\n            int acc=Math.min(94,84+year),sec=Math.max(58,94-year*4);\n            if(year==2){\n                if(mission==0)return \"ДОСЛІД РОКУ 2: кулі ≥ \"+acc+\"%\";\n                if(mission==1)return \"ДОСЛІД РОКУ 2: час ≤ \"+sec+\" с\";\n                return \"ДОСЛІД РОКУ 2: декор ≥ \"+acc+\"%\";\n            }\n            if(mission==0)return \"МІСІЯ: точність куль ≥ \"+acc+\"%\";\n            if(mission==1)return \"МІСІЯ: завершити ≤ \"+sec+\" с\";\n            return \"МІСІЯ: декор ≥ \"+acc+\"%\";\n        }",
    "year2 mission identity",
)

path.write_text(s, encoding="utf-8")
print("Applied SnowmanGame v11 Year 2 story routing")
