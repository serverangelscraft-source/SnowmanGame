from pathlib import Path
import re

school_path=Path("app/src/main/java/com/snowmangame/SchoolWeekActivity.java")
gradle_path=Path("app/build.gradle")
s=school_path.read_text(encoding="utf-8")

def rep(old,new,label):
    global s
    if old not in s:
        raise SystemExit(f"v18.3 calendar weekdays patch failed: {label}")
    s=s.replace(old,new,1)

rep(
    '        float safeTop,safeBottom; long today,anchorDay,effectiveDay; int anchorGrade,weekDay,grade,winter,stage,mistakes; boolean yearIntro;',
    '        float safeTop,safeBottom; long today,anchorDay,effectiveDay,cycleStartDay; int anchorGrade,weekDay,calendarDow,mealDay,grade,winter,stage,mistakes; boolean yearIntro;',
    'calendar fields',
)
rep(
    '            int week=(int)(elapsed/7L);weekDay=(int)(elapsed%7L)+1;\n            grade=Math.min(11,anchorGrade+week);winter=grade+6;',
    '            int week=(int)(elapsed/7L);weekDay=(int)(elapsed%7L)+1;\n            cycleStartDay=effectiveDay-(weekDay-1L);\n            calendarDow=dayOfWeek(effectiveDay);\n            mealDay=schoolOrdinal(cycleStartDay,effectiveDay);\n            grade=Math.min(11,anchorGrade+week);winter=grade+6;',
    'derive real weekday and school ordinal',
)
rep(
    '        boolean schoolDay(){return weekDay<=5;} String className(){return grade+"-А";}\n        String modeLabel(){return schoolDay()?"НАВЧАННЯ":"ВИХІДНИЙ";}',
    '''        int dayOfWeek(long dayNumber){GregorianCalendar g=new GregorianCalendar(TimeZone.getTimeZone("UTC"));g.setTimeInMillis(dayNumber*86400000L);return g.get(Calendar.DAY_OF_WEEK);}\n        int schoolOrdinal(long start,long day){int n=0;for(long d=start;d<=day;d++){int dow=dayOfWeek(d);if(dow>=Calendar.MONDAY&&dow<=Calendar.FRIDAY)n++;}return n;}\n        boolean schoolDay(){return calendarDow>=Calendar.MONDAY&&calendarDow<=Calendar.FRIDAY;} String className(){return grade+"-А";}\n        String modeLabel(){return schoolDay()?"НАВЧАННЯ":"ВИХІДНИЙ";}\n        String calendarDayName(){switch(calendarDow){case Calendar.MONDAY:return"ПОНЕДІЛОК";case Calendar.TUESDAY:return"ВІВТОРОК";case Calendar.WEDNESDAY:return"СЕРЕДА";case Calendar.THURSDAY:return"ЧЕТВЕР";case Calendar.FRIDAY:return"П’ЯТНИЦЯ";case Calendar.SATURDAY:return"СУБОТА";default:return"НЕДІЛЯ";}}''',
    'real weekday helpers',
)
for old,new,label in [
    ('String lessonTitle(){switch(weekDay){','String lessonTitle(){switch(mealDay){','first lesson ordinal'),
    ('String question(){switch(weekDay){','String question(){switch(mealDay){','first question ordinal'),
    ('String[] options(){switch(weekDay){','String[] options(){switch(mealDay){','first options ordinal'),
    ('int correctIndex(){switch(weekDay){','int correctIndex(){switch(mealDay){','first answer ordinal'),
    ('String secondLessonTitle(){switch(weekDay){','String secondLessonTitle(){switch(mealDay){','second lesson ordinal'),
    ('String secondQuestion(){switch(weekDay){','String secondQuestion(){switch(mealDay){','second question ordinal'),
    ('String[] secondOptions(){switch(weekDay){','String[] secondOptions(){switch(mealDay){','second options ordinal'),
    ('int secondCorrectIndex(){return weekDay==2?1:0;}','int secondCorrectIndex(){return mealDay==2?1:0;}','second answer ordinal'),
]: rep(old,new,label)
rep('c.drawText("День "+weekDay+"/7 • "+modeLabel(),r.left+dp(17),r.top+dp(52),text);','c.drawText("День "+weekDay+"/7 • "+calendarDayName(),r.left+dp(17),r.top+dp(52),text);','header real weekday')
rep('c.drawText(schoolDay()?"5 навчальних днів • 2 вихідні • 1 день телефона = 1 день гри":"Сьогодні школа закрита. Рік усе одно продовжується.",r.left+dp(17),r.top+dp(78),text);','c.drawText(schoolDay()?"НАВЧАННЯ • сьогодні "+mealDay+"-й навчальний день із 5":"ВИХІДНИЙ • школа сьогодні закрита",r.left+dp(17),r.top+dp(78),text);','header mode copy')
rep('"ЕТАП 6/6 • ВЕЧЕРЯ "+weekDay+"/5"','"ЕТАП 6/6 • ВЕЧЕРЯ "+mealDay+"/5"','dinner counter')
while 'DISHES[weekDay-1]' in s:s=s.replace('DISHES[weekDay-1]','DISHES[mealDay-1]',1)
rep('c.drawText("ВИХІДНИЙ "+(weekDay-5)+"/2",card.centerX(),card.top+dp(30),text);','c.drawText(calendarDayName(),card.centerX(),card.top+dp(30),text);','weekend title')
rep('c.drawText(weekDay==6?"Суботній день без школи":"Тихий день перед новою зимою",card.centerX(),card.top+dp(67),text);','c.drawText(calendarDow==Calendar.SATURDAY?"Суботній день без школи":"Недільний день без школи",card.centerX(),card.top+dp(67),text);','weekend headline')
rep('c.drawText(weekDay==6?"Обери одну справу. Більше навчальних завдань сьогодні немає.":"Обери, як провести останній день цього року.",card.centerX(),card.top+dp(96),text);','c.drawText(calendarDow==Calendar.SATURDAY?"Обери одну справу на суботу.":"Обери одну справу на неділю.",card.centerX(),card.top+dp(96),text);','weekend copy')
rep('String[] o=weekDay==6?new String[]{"САНЧАТА","ПРОГУЛЯНКА","ВІДПОЧИНОК"}:new String[]{"СПОГАДИ","ЗІБРАТИ РЕЧІ","ПОБУТИ ЗІ СНІЖИКОМ"};','String[] o=calendarDow==Calendar.SATURDAY?new String[]{"САНЧАТА","ПРОГУЛЯНКА","ВІДПОЧИНОК"}:new String[]{"СПОГАДИ","ЗІБРАТИ РЕЧІ","ПОБУТИ ЗІ СНІЖИКОМ"};','weekend draw choices')
rep('String[] o=weekDay==6?new String[]{"Санчата","Прогулянка","Відпочинок"}:new String[]{"Спогади","Зібрані речі","Час зі Сніжиком"};','String[] o=calendarDow==Calendar.SATURDAY?new String[]{"Санчата","Прогулянка","Відпочинок"}:new String[]{"Спогади","Зібрані речі","Час зі Сніжиком"};','weekend touch choices')
school_path.write_text(s,encoding="utf-8")
g=gradle_path.read_text(encoding="utf-8");g=re.sub(r'versionCode\s+\d+','versionCode 36',g);g=re.sub(r'versionName\s+"[^"]+"','versionName "18.3"',g);gradle_path.write_text(g,encoding="utf-8")
print("Applied SnowmanGame v18.3: real Mon-Fri school, Sat-Sun weekend, start any day")
