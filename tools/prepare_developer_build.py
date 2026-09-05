from pathlib import Path

root = Path(__file__).resolve().parents[1]
gradle = root / "app" / "build.gradle"
school = root / "app" / "src" / "main" / "java" / "com" / "snowmangame" / "SchoolWeekActivity.java"

# Separate installable developer app. Production sources in git remain untouched;
# this script only patches the CI checkout before assembling the developer APK.
g = gradle.read_text(encoding="utf-8")
g = g.replace('applicationId "com.snowmangame"', 'applicationId "com.snowmangame.developer"')
g = g.replace('versionName "18.21"', 'versionName "18.21-developer"')
if 'com.snowmangame.developer' not in g:
    raise SystemExit("developer applicationId patch failed")
gradle.write_text(g, encoding="utf-8")

s = school.read_text(encoding="utf-8")
old_clock = '''long localDayNumber(){Calendar local=Calendar.getInstance();int y=local.get(Calendar.YEAR),m=local.get(Calendar.MONTH),d=local.get(Calendar.DAY_OF_MONTH);GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));utc.clear();utc.set(y,m,d,0,0,0);return utc.getTimeInMillis()/86400000L;}'''
new_clock = '''long localDayNumber(){Calendar local=Calendar.getInstance();int y=local.get(Calendar.YEAR),m=local.get(Calendar.MONTH),d=local.get(Calendar.DAY_OF_MONTH);GregorianCalendar utc=new GregorianCalendar(TimeZone.getTimeZone("UTC"));utc.clear();utc.set(y,m,d,0,0,0);long real=utc.getTimeInMillis()/86400000L;return prefs.getLong("developer_virtual_day",real);} void advanceDeveloperDay(){long next=Math.max(today,effectiveDay)+1;prefs.edit().putLong("developer_virtual_day",next).putLong("school_player_last_seen_day",next).remove("school_player_stage_day").apply();initState();invalidate();}'''
if old_clock not in s:
    raise SystemExit("localDayNumber patch target not found")
s = s.replace(old_clock, new_clock, 1)

# On completion/bonus screens, replace the normal two utility buttons with a
# single dominant developer action. This makes rapid whole-life testing explicit.
s = s.replace('drawBottomTools(c);}', 'button(c,"НАСТУПНИЙ ДЕНЬ • DEV");}', 3)

old_touch = '''if(stage==DONE||stage==YEAR_DONE||stage==BONUS){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}return true;}'''
new_touch = '''if(stage==DONE||stage==YEAR_DONE||stage==BONUS){if(action.contains(x,y)){advanceDeveloperDay();return true;}return true;}'''
if old_touch not in s:
    raise SystemExit("completed-stage touch patch target not found")
s = s.replace(old_touch, new_touch, 1)

# Developer build should say what it is, rather than pretending the calendar is real.
s = s.replace('setContentDescription("Шкільне життя сніговика: день зараховується лише після гри, максимум один за календарну дату");', 'setContentDescription("Режим розробника: шкільне життя можна проходити без очікування реальної дати");', 1)
s = s.replace('"Пропущений день не рахується • за одну дату максимум 1 день життя"', '"DEV • дата віртуальна • проходь дні без очікування"')
s = s.replace('"НАСТУПНИЙ ДЕНЬ — НЕ РАНІШЕ ЗАВТРА"', '"DEV • МОЖНА ПЕРЕЙТИ ДАЛІ ОДРАЗУ"')
s = s.replace('"Наступна зима відкриється, коли ти повернешся іншого дня."', '"DEV • наступну зиму можна відкрити одразу."')

if 'advanceDeveloperDay()' not in s or 'developer_virtual_day' not in s:
    raise SystemExit("developer school patch validation failed")
school.write_text(s, encoding="utf-8")

print("Developer build prepared: separate package + virtual school calendar")
