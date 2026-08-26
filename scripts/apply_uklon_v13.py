from pathlib import Path

main_path = Path("app/src/main/java/com/snowmangame/MainActivity.java")
delivery_path = Path("app/src/main/java/com/snowmangame/DeliveryActivity.java")
main = main_path.read_text(encoding="utf-8")
delivery = delivery_path.read_text(encoding="utf-8")


def rep_main(old: str, new: str, label: str) -> None:
    global main
    if old not in main:
        raise SystemExit(f"v13 Uklon patch failed in MainActivity at: {label}")
    main = main.replace(old, new, 1)


def rep_delivery(old: str, new: str, label: str) -> None:
    global delivery
    if old not in delivery:
        raise SystemExit(f"v13 Uklon patch failed in DeliveryActivity at: {label}")
    delivery = delivery.replace(old, new, 1)


rep_main(
    '            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(researchNeeded?"ДОСЛІДИТИ СНІГ • РІК 2":(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ"));',
    '            String journeyLabel=year>=7?"ДО ШКІЛЬНИХ ПРИГОД":(researchNeeded?"ДОСЛІДИТИ СНІГ • РІК 2":(year>=3?"ВИКЛИКАТИ ВОДІЯ • ДО ВОКЗАЛУ":(year>=2?"НА САНЧАТА • ДО ВОКЗАЛУ":"ЗАБРАТИ ПОСИЛКУ • ДО ВОКЗАЛУ")));',
    "Year 3 transport label",
)

rep_delivery(
    '        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);\n        setContentView(new DeliveryView(this));',
    '        if(Build.VERSION.SDK_INT>=29)w.setNavigationBarContrastEnforced(false);\n        SharedPreferences routePrefs=getSharedPreferences("snowman_game",MODE_PRIVATE);\n        int routeYear=Math.max(1,Math.min(7,routePrefs.getInt("life_year",1)));\n        if(routeYear>=3){\n            startActivity(new Intent(this,UklonActivity.class));\n            finish();\n            return;\n        }\n        setContentView(new DeliveryView(this));',
    "route Year 3+ to Uklon",
)

rep_delivery(
    'c.drawText("Тизер: згодом можна буде найняти водія Uklon.",teaser.centerX(),teaser.top+dp(76),text);',
    'c.drawText("Рік 3: можна буде найняти водія Uklon.",teaser.centerX(),teaser.top+dp(76),text);',
    "replace Uklon teaser with unlock hint",
)
rep_delivery(
    'c.drawText("Uklon • ДЕМО-ТИЗЕР • неофіційна інтеграція",teaser.centerX(),teaser.top+dp(91),text);',
    'c.drawText("Uklon • ДЕМО-КОЛАБ • неофіційна інтеграція",teaser.centerX(),teaser.top+dp(91),text);',
    "Uklon demo wording",
)

main_path.write_text(main, encoding="utf-8")
delivery_path.write_text(delivery, encoding="utf-8")
print("Applied SnowmanGame v13 Uklon driver transport")

# Keep the existing workflow/signing chain untouched: later story layers are applied after v13.
exec(Path("scripts/apply_lifecore_v14.py").read_text(encoding="utf-8"), {"__name__": "__main__"})
exec(Path("scripts/apply_memory_friend_v15.py").read_text(encoding="utf-8"), {"__name__": "__main__"})
exec(Path("scripts/apply_oblast_journey_v16.py").read_text(encoding="utf-8"), {"__name__": "__main__"})
exec(Path("scripts/apply_graphics_fit_v16_1.py").read_text(encoding="utf-8"), {"__name__": "__main__"})
