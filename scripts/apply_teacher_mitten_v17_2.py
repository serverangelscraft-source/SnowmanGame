from pathlib import Path

paths = {
    "main": Path("app/src/main/java/com/snowmangame/MainActivity.java"),
    "year2": Path("app/src/main/java/com/snowmangame/YearTwoActivity.java"),
    "year3": Path("app/src/main/java/com/snowmangame/YearThreeActivity.java"),
    "memory": Path("app/src/main/java/com/snowmangame/MemoryActivity.java"),
    "style": Path("app/src/main/java/com/snowmangame/SnowmanStyle.java"),
    "character": Path("app/src/main/java/com/snowmangame/CharacterActivity.java"),
    "wardrobe": Path("app/src/main/java/com/snowmangame/WardrobeActivity.java"),
    "school": Path("app/src/main/java/com/snowmangame/SchoolActivity.java"),
}
src = {k: p.read_text(encoding="utf-8") for k, p in paths.items()}

def rep(key: str, old: str, new: str, label: str, required: bool = True) -> None:
    if old not in src[key]:
        if required:
            raise SystemExit(f"v17.2 teacher/face patch failed in {key} at: {label}")
        return
    src[key] = src[key].replace(old, new, 1)

def section(key: str, start_marker: str, end_marker: str, new_text: str, label: str) -> None:
    s = src[key]
    a = s.find(start_marker)
    if a < 0:
        raise SystemExit(f"v17.2 section missing start in {key}: {label}")
    b = s.find(end_marker, a + len(start_marker))
    if b < 0:
        raise SystemExit(f"v17.2 section missing end in {key}: {label}")
    src[key] = s[:a] + new_text + s[b:]

# SAVE MIGRATION
rep(
    "main",
    '.putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).putString("year3_friend_name","Сніжик")',
    '.putBoolean("year3_friend_met",true).putString("year3_friend_name","Сніжик")',
    "remove obsolete Snowik mitten ownership from old-save migration",
)
rep(
    "main",
    '        if(!seasonalProgress.getBoolean("character_selected",false)){startActivity(new Intent(this,CharacterActivity.class));finish();return;}\n',
    '        if(!seasonalProgress.getBoolean("mitten_teacher_arc_v172",false)&&seasonalProgress.getBoolean("year2_mitten_found",false)){\n'
    '            seasonalProgress.edit().putBoolean("year2_mitten_returned",false).putBoolean("teacher_mitten_returned",false).putBoolean("mitten_teacher_arc_v172",true).apply();\n'
    '        }\n'
    '        if(!seasonalProgress.getBoolean("character_selected",false)){startActivity(new Intent(this,CharacterActivity.class));finish();return;}\n',
    "migrate existing mitten saves to teacher arc",
)

# YEAR 2
rep(
    "year2",
    'if(stage==MITTEN)return"Це не твоя річ. Збережемо її, щоб повернути власнику.";',
    'if(stage==MITTEN)return"На ній знак сніжинки. Збережемо її до зустрічі зі старшим сніговиком.";',
    "Year 2 mitten subtitle",
)
rep(
    "year2",
    '"Власника тут немає — це загадка на майбутнє."',
    '"Рукавичка завелика для малого сніговика — власник десь старший."',
    "Year 2 mitten owner clue",
)
rep(
    "year2",
    '"Сховаємо її до зустрічі з власником."',
    '"Збережемо її. Колись цей знак допоможе знайти власника."',
    "Year 2 mitten keep copy",
)

# YEAR 3
rep(
    "year3",
    'setContentDescription("Зима 3: знайти власника синьої рукавички та пограти у сніжки");',
    'setContentDescription("Зима 3: показати Сніжику синю рукавичку, отримати підказку та пограти у сніжки");',
    "Year 3 accessibility story",
)
rep(
    "year3",
    'header(c,"Знайшли!","У малого сніговика на руці — друга така сама рукавичка","КРОК 3/4");',
    'header(c,"Це не його рукавичка","Сніжик упізнав знак, але рукавичка йому завелика","КРОК 3/4");',
    "Year 3 reveal header",
)
rep(
    "year3",
    'drawFriend(c,w*.73f,ground,dp(29),.25f,true);',
    'drawFriend(c,w*.73f,ground,dp(29),.25f,false);',
    "Snowik must not wear the matching mitten",
)
rep(
    "year3",
    'c.drawText("Сніжик: «Я думав, вона загубилася назавжди!»",bubble.centerX(),bubble.top+dp(34),text);',
    'c.drawText("Сніжик: «Ні, це не моя. Вона навіть завелика.»",bubble.centerX(),bubble.top+dp(34),text);',
    "Snowik denial",
)
rep(
    "year3",
    'c.drawText("Перетягни синю рукавичку до його вільної руки.",bubble.centerX(),bubble.top+dp(61),text);',
    'c.drawText("Покажи рукавичку Сніжику — перетягни її до нього.",bubble.centerX(),bubble.top+dp(61),text);',
    "Year 3 show mitten instruction",
)
rep(
    "year3",
    'c.drawText("Рукавичка стане спогадом, а не частиною твого декору.",bubble.centerX(),bubble.top+dp(82),text);',
    'c.drawText("Він бачив такий знак у старшого сніговика біля школи.",bubble.centerX(),bubble.top+dp(82),text);',
    "Year 3 teacher clue",
)
rep(
    "year3",
    'prefs.edit().putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).putString("year3_friend_name","Сніжик").apply();hint="Рукавичка повернулась до Сніжика.";',
    'prefs.edit().putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",false).putBoolean("mitten_teacher_clue",true).putString("year3_friend_name","Сніжик").apply();hint="Сніжик: «Не моя. Я бачив такий знак у старшого сніговика.»";',
    "Year 3 mitten comparison result",
)
rep(
    "year3",
    'prefs.edit().putBoolean("year3_story_complete",true).putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).putInt("year3_stage",DONE).apply();',
    'prefs.edit().putBoolean("year3_story_complete",true).putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",false).putBoolean("mitten_teacher_clue",true).putInt("year3_stage",DONE).apply();',
    "Year 3 completion keeps mitten",
)
rep(
    "year3",
    'prefs.edit().putBoolean("year3_story_complete",true).putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",true).apply();',
    'prefs.edit().putBoolean("year3_story_complete",true).putBoolean("year3_friend_met",true).putBoolean("year2_mitten_returned",false).putBoolean("mitten_teacher_clue",true).apply();',
    "Year 3 DONE keeps mitten",
)
rep(
    "year3",
    'header(c,"Перший друг","Рукавичка повернулась додому, а зима стала веселішою","ГОТОВО");',
    'header(c,"Перший друг","Сніжик дав підказку, а рукавичка лишилася з тобою","ГОТОВО");',
    "Year 3 done header",
)
rep(
    "year3",
    'c.drawText("Синю рукавичку повернено власнику.",card.centerX(),card.top+dp(67),text);',
    'c.drawText("Сніжик не власник — рукавичка лишилася з тобою.",card.centerX(),card.top+dp(67),text);',
    "Year 3 done memory line one",
)
rep(
    "year3",
    'c.drawText("Тепер у кімнаті пам’яті з’явився перший друг.",card.centerX(),card.top+dp(91),text);',
    'c.drawText("Він бачив цей знак у старшого сніговика біля школи.",card.centerX(),card.top+dp(91),text);',
    "Year 3 done memory line two",
)

# MEMORY ROOM
rep(
    "memory",
    '        @Override protected void onDraw(Canvas c){',
    '        String memoryName(int i){if(i==5)return prefs.getBoolean("teacher_mitten_returned",false)?"Рукавичка вчителя":"Синя рукавичка";return names[i];}\n\n'
    '        @Override protected void onDraw(Canvas c){',
    "dynamic mitten memory title",
)
rep("memory", "drawLabel(c,names[i],", "drawLabel(c,memoryName(i),", "memory shelf dynamic title")
rep("memory", "c.drawText(names[selected],", "c.drawText(memoryName(selected),", "memory detail dynamic title")
rep(
    "memory",
    'if(i==5)return prefs.getBoolean("year2_mitten_returned",false)?"Знайдена у Зимі 2 й повернена Сніжику у Зимі 3.":"Знайдена у Зимі 2. Власник ще десь поруч.";',
    'if(i==5)return prefs.getBoolean("teacher_mitten_returned",false)?"Знайдена у Зимі 2 й повернена пану Крижу в школі.":(prefs.getBoolean("mitten_teacher_clue",false)?"Сніжик не власник. Він бачив цей знак у старшого сніговика.":"Знайдена у Зимі 2. Власник ще десь попереду.");',
    "memory mitten biography",
)

# SOFTER GIRL FACE
rep(
    "style",
    'c.drawLine(x-r*.36f,y-r*.23f,x-r*.43f,y-r*.32f,stroke);c.drawLine(x+r*.36f,y-r*.23f,x+r*.43f,y-r*.32f,stroke);drawBow(c,p,density,x+r*.57f,y-r*.58f,Math.max(density*3.2f,r*.18f));',
    'c.drawLine(x-r*.39f,y-r*.30f,x-r*.20f,y-r*.30f,stroke);c.drawLine(x+r*.20f,y-r*.30f,x+r*.39f,y-r*.30f,stroke);drawBow(c,p,density,x+r*.57f,y-r*.58f,Math.max(density*3.2f,r*.18f));',
    "soft girl brows in shared renderer",
)
rep(
    "character",
    'if(type==1){c.drawLine(x-r*.36f,y-r*.23f,x-r*.43f,y-r*.31f,stroke);c.drawLine(x+r*.36f,y-r*.23f,x+r*.43f,y-r*.31f,stroke);}',
    'if(type==1){c.drawLine(x-r*.39f,y-r*.30f,x-r*.20f,y-r*.30f,stroke);c.drawLine(x+r*.20f,y-r*.30f,x+r*.39f,y-r*.30f,stroke);}',
    "soft girl brows in character picker",
)
rep(
    "wardrobe",
    'if(character==1){stroke.setColor(Color.rgb(56,76,87));stroke.setStrokeWidth(dp(1.2f));c.drawLine(x-r*.35f,y-r*.22f,x-r*.43f,y-r*.30f,stroke);c.drawLine(x+r*.35f,y-r*.22f,x+r*.43f,y-r*.30f,stroke);}',
    'if(character==1){stroke.setColor(Color.rgb(56,76,87));stroke.setStrokeWidth(dp(1.2f));c.drawLine(x-r*.39f,y-r*.30f,x-r*.20f,y-r*.30f,stroke);c.drawLine(x+r*.20f,y-r*.30f,x+r*.39f,y-r*.30f,stroke);}',
    "soft girl brows in wardrobe preview",
)

# SCHOOL: PAN KRYZH + MITTEN RETURN
rep(
    "school",
    'static final int GATE=0, CLASS_DOOR=1, DESK=2, LESSON=3, DONE=4;',
    'static final int GATE=0, CLASS_DOOR=1, DESK=2, LESSON=3, DONE=4, TEACHER=5;',
    "teacher stage constant",
)
rep(
    "school",
    'final RectF action=new RectF(), memoryBtn=new RectF(), wardrobeBtn=new RectF();',
    'final RectF action=new RectF(), memoryBtn=new RectF(), wardrobeBtn=new RectF(), teacherHand=new RectF(), teacherMitten=new RectF();',
    "teacher hitboxes",
)
rep(
    "school",
    'float safeTop,safeBottom; int stage,mistakes; String feedback="";',
    'float safeTop,safeBottom,teacherMittenX=Float.NaN,teacherMittenY=Float.NaN; int stage,mistakes; String feedback=""; boolean draggingTeacherMitten;',
    "teacher drag state",
)
rep(
    "school",
    'stage=prefs.getBoolean("school_first_day_complete",false)?DONE:Math.max(GATE,Math.min(LESSON,prefs.getInt("school_stage",GATE)));',
    'boolean schoolDone=prefs.getBoolean("school_first_day_complete",false);boolean mittenPending=prefs.getBoolean("year2_mitten_found",false)&&!prefs.getBoolean("teacher_mitten_returned",false);stage=schoolDone?(mittenPending?TEACHER:DONE):Math.max(GATE,Math.min(LESSON,prefs.getInt("school_stage",GATE)));',
    "teacher stage save migration",
)
rep(
    "school",
    'float dp(float v){return v*density;} float tx(float v){return v*textScale;}',
    'float dp(float v){return v*density;} float tx(float v){return v*textScale;} float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));} float dist(float x1,float y1,float x2,float y2){return(float)Math.hypot(x1-x2,y1-y2);}',
    "school drag helpers",
)
rep(
    "school",
    'void next(int s){stage=s;feedback="";prefs.edit().putInt("school_stage",Math.min(s,LESSON)).apply();buzz(24);invalidate();}',
    'void next(int s){stage=s;feedback="";prefs.edit().putInt("school_stage",s==TEACHER?GATE:Math.min(s,LESSON)).apply();buzz(24);invalidate();}',
    "teacher stage persistence",
)
rep(
    "school",
    'String title(){if(stage==GATE)return"Перший день";if(stage==CLASS_DOOR)return"Знайди свій клас";if(stage==DESK)return"Знайди свою парту";if(stage==LESSON)return"Перший урок";return"Школа відкрита";}',
    'String title(){if(stage==GATE)return"Перший день";if(stage==TEACHER)return"Знайомство з учителем";if(stage==CLASS_DOOR)return"Знайди свій клас";if(stage==DESK)return"Знайди свою парту";if(stage==LESSON)return"Перший урок";return"Школа відкрита";}',
    "teacher title",
)
rep(
    "school",
    'String sub(){if(stage==GATE)return"7 зим привели сюди — тепер починається нова глава";if(stage==CLASS_DOOR)return"Сніговик записаний до 1-А. Не переплутай двері.";if(stage==DESK)return"На твоїй парті знак живої сніжинки.";if(stage==LESSON)return"Перше питання просте, але дуже особисте.";return"Перший урок завершено • Тернопільщина";}',
    'String sub(){if(stage==GATE)return"7 зим привели сюди — тепер починається нова глава";if(stage==TEACHER)return"Пан Криж упізнав синю рукавичку, знайдену ще у другій зимі.";if(stage==CLASS_DOOR)return"Сніговик записаний до 1-А. Не переплутай двері.";if(stage==DESK)return"На твоїй парті знак живої сніжинки.";if(stage==LESSON)return"Перше питання просте, але дуже особисте.";return"Перший урок завершено • Тернопільщина";}',
    "teacher subtitle",
)
rep(
    "school",
    'if(stage==GATE)drawGate(c);else if(stage==CLASS_DOOR)drawDoors(c);else if(stage==DESK)drawDesks(c);else if(stage==LESSON)drawLesson(c);else drawDone(c);',
    'if(stage==GATE)drawGate(c);else if(stage==TEACHER)drawTeacherScene(c);else if(stage==CLASS_DOOR)drawDoors(c);else if(stage==DESK)drawDesks(c);else if(stage==LESSON)drawLesson(c);else drawDone(c);',
    "render teacher stage",
)

teacher_scene = '''        void drawTeacherScene(Canvas c){
            float w=getWidth(),bottom=getHeight()-safeBottom,ground=bottom*.72f;
            boolean returned=prefs.getBoolean("teacher_mitten_returned",false);
            drawHero(c,w*.20f,ground+dp(2),dp(32),.35f);
            drawTeacher(c,w*.72f,ground+dp(2),dp(45),returned);
            RectF card=new RectF(dp(20),safeTop+dp(137),w-dp(20),safeTop+dp(270));p.setColor(Color.argb(244,255,255,255));c.drawRoundRect(card,dp(22),dp(22),p);
            text.setTextAlign(Paint.Align.CENTER);text.setTextSize(tx(7));text.setColor(Color.rgb(112,132,139));c.drawText("ПАН КРИЖ • ВЧИТЕЛЬ • 23-ТЯ ЗИМА",card.centerX(),card.top+dp(23),text);
            text.setTextSize(tx(11));text.setColor(Color.rgb(48,82,98));c.drawText(returned?"«Ти зберіг її аж до школи. Дякую.»":"«Це ж моя рукавичка… Я загубив її кілька зим тому.»",card.centerX(),card.top+dp(53),text);
            text.setTextSize(tx(7.8f));text.setColor(Color.rgb(96,124,137));c.drawText(returned?"Довіра вчителя +1 • тепер пара знову разом.":"Перетягни знайдену синю рукавичку до його вільної руки.",card.centerX(),card.top+dp(82),text);
            if(returned){
                text.setTextSize(tx(7));text.setColor(Color.rgb(55,128,102));c.drawText("СПОГАД ОНОВЛЕНО: РУКАВИЧКА ВЧИТЕЛЯ",card.centerX(),card.bottom-dp(18),text);
                button(c,prefs.getBoolean("school_first_day_complete",false)?"ПОВЕРНУТИСЯ ДО КЛАСУ":"ДАЛІ ДО 1-А");
            }else{
                if(Float.isNaN(teacherMittenX)){teacherMittenX=w*.30f;teacherMittenY=ground-dp(142);}
                drawBlueMitten(c,teacherMittenX,teacherMittenY,.92f);
                teacherMitten.set(teacherMittenX-dp(30),teacherMittenY-dp(36),teacherMittenX+dp(31),teacherMittenY+dp(36));
                teacherHand.set(w*.72f+dp(25),ground-dp(125),w*.72f+dp(84),ground-dp(65));
                stroke.setColor(Color.rgb(57,148,195));stroke.setStrokeWidth(dp(2));stroke.setPathEffect(new DashPathEffect(new float[]{dp(6),dp(5)},0));c.drawRoundRect(teacherHand,dp(13),dp(13),stroke);stroke.setPathEffect(null);
                if(feedback.length()>0){text.setTextSize(tx(7.2f));text.setColor(Color.rgb(145,92,76));c.drawText(feedback,w/2,bottom-dp(42),text);}
            }
        }

        void drawTeacher(Canvas c,float x,float ground,float r,boolean bothMittens){
            float br=r,mr=r*.74f,hr=r*.55f,by=ground-br,my=by-(br+mr)*.82f,hy=my-(mr+hr)*.82f;
            snow(c,x,by,br);snow(c,x,my,mr);snow(c,x,hy,hr);
            p.setColor(Color.rgb(45,59,68));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.07f,p);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.07f,p);
            stroke.setColor(Color.rgb(52,67,76));stroke.setStrokeWidth(dp(1.8f));c.drawCircle(x-hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawCircle(x+hr*.27f,hy-hr*.12f,hr*.18f,stroke);c.drawLine(x-hr*.09f,hy-hr*.12f,x+hr*.09f,hy-hr*.12f,stroke);
            Path n=new Path();n.moveTo(x,hy);n.lineTo(x+hr*.58f,hy+hr*.06f);n.lineTo(x,hy+hr*.13f);n.close();p.setColor(Color.rgb(224,121,45));c.drawPath(n,p);
            p.setColor(Color.rgb(64,83,94));c.drawRoundRect(new RectF(x-mr*.55f,my-mr*.58f,x+mr*.55f,my+mr*.48f),dp(8),dp(8),p);
            RectF collar=new RectF(x-mr*.42f,my-mr*.62f,x+mr*.42f,my-mr*.49f);SnowmanStyle.drawPatternBand(c,p,density,collar,7);
            stroke.setColor(Color.rgb(101,76,57));stroke.setStrokeWidth(dp(3));float lx=x-mr*.58f,ly=my-mr*.05f,lxe=x-mr*1.42f,lye=my-mr*.30f,rx=x+mr*.58f,ry=my-mr*.05f,rxe=x+mr*1.42f,rye=my-mr*.30f;c.drawLine(lx,ly,lxe,lye,stroke);c.drawLine(rx,ry,rxe,rye,stroke);
            drawBlueMitten(c,lxe,lye,.60f);if(bothMittens)drawBlueMitten(c,rxe,rye,.60f);
            stroke.setColor(Color.rgb(91,67,50));stroke.setStrokeWidth(dp(2));c.drawLine(rx,ry,rxe+dp(16),rye-dp(31),stroke);
        }

        void drawBlueMitten(Canvas c,float x,float y,float s){
            p.setColor(Color.rgb(55,137,194));c.drawRoundRect(new RectF(x-dp(11)*s,y-dp(20)*s,x+dp(11)*s,y+dp(10)*s),dp(10)*s,dp(10)*s,p);
            c.save();c.rotate(-32,x+dp(8)*s,y);c.drawRoundRect(new RectF(x+dp(4)*s,y-dp(3)*s,x+dp(22)*s,y+dp(8)*s),dp(6)*s,dp(6)*s,p);c.restore();
            p.setColor(Color.rgb(43,113,169));c.drawRoundRect(new RectF(x-dp(13)*s,y+dp(7)*s,x+dp(13)*s,y+dp(19)*s),dp(4)*s,dp(4)*s,p);
            float rr=dp(5)*s;stroke.setColor(Color.WHITE);stroke.setStrokeWidth(dp(1.2f));for(int i=0;i<6;i++){double a=i*Math.PI/3;c.drawLine(x,y-dp(4)*s,x+(float)Math.cos(a)*rr,y-dp(4)*s+(float)Math.sin(a)*rr,stroke);}
        }

'''
rep(
    "school",
    '        void drawDoors(Canvas c){',
    teacher_scene + '        void drawDoors(Canvas c){',
    "teacher scene methods",
)

rep(
    "school",
    'drawHero(c,w*.28f,bottom-dp(76),dp(26),.2f);drawFriend(c,w*.72f,bottom-dp(76),dp(23),.2f);',
    'drawHero(c,w*.17f,bottom-dp(76),dp(25),.2f);drawFriend(c,w*.50f,bottom-dp(76),dp(22),.2f);drawTeacher(c,w*.82f,bottom-dp(73),dp(27),true);',
    "teacher visible during first lesson",
)

touch = '''        @Override public boolean onTouchEvent(MotionEvent e){
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN){
                if(stage==TEACHER&&!prefs.getBoolean("teacher_mitten_returned",false)&&teacherMitten.contains(x,y)){draggingTeacherMitten=true;buzz(10);return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE){
                if(stage==TEACHER&&draggingTeacherMitten){teacherMittenX=clamp(x,dp(28),getWidth()-dp(28));teacherMittenY=clamp(y,safeTop+dp(275),getHeight()-safeBottom-dp(55));invalidate();return true;}
                return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL){
                performClick();
                if(stage==TEACHER&&draggingTeacherMitten){
                    draggingTeacherMitten=false;
                    if(teacherHand.contains(teacherMittenX,teacherMittenY)||dist(teacherMittenX,teacherMittenY,teacherHand.centerX(),teacherHand.centerY())<dp(48)){
                        int trust=Math.max(1,prefs.getInt("teacher_trust",0)+1);
                        prefs.edit().putBoolean("teacher_mitten_returned",true).putBoolean("year2_mitten_returned",true).putBoolean("teacher_mitten_scene_seen",true).putInt("teacher_trust",trust).apply();
                        feedback="Пан Криж: «Дякую. Я вже думав, що вона розтанула разом із тією зимою.»";buzz(45);invalidate();
                    }else{
                        teacherMittenX=getWidth()*.30f;teacherMittenY=(getHeight()-safeBottom)*.72f-dp(142);feedback="Трохи ближче до вільної руки пана Крижа.";buzz(12);invalidate();
                    }
                    return true;
                }
                if(stage==GATE&&action.contains(x,y)){
                    if(prefs.getBoolean("year2_mitten_found",false)&&!prefs.getBoolean("teacher_mitten_returned",false))next(TEACHER);else next(CLASS_DOOR);
                    return true;
                }
                if(stage==TEACHER&&prefs.getBoolean("teacher_mitten_returned",false)&&action.contains(x,y)){
                    if(prefs.getBoolean("school_first_day_complete",false)){stage=DONE;feedback="";invalidate();}else next(CLASS_DOOR);
                    return true;
                }
                if(stage==CLASS_DOOR){for(int i=0;i<3;i++)if(doors[i].contains(x,y)){if(i==1)next(DESK);else{mistakes++;feedback="Це не 1-А. Подивись на табличку ще раз.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();}return true;}}
                if(stage==DESK){for(int i=0;i<3;i++)if(desks[i].contains(x,y)){if(i==1)next(LESSON);else{mistakes++;feedback="На цій парті немає знака живої сніжинки.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();}return true;}}
                if(stage==LESSON){for(int i=0;i<3;i++)if(answers[i].contains(x,y)){if(i==1)completeLesson();else{mistakes++;feedback=i==0?"Лід теж тане. Спробуй ще.":"Пісок тут точно ні до чого.";prefs.edit().putInt("school_mistakes",mistakes).apply();buzz(12);invalidate();}return true;}}
                if(stage==DONE){if(memoryBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,MemoryActivity.class));return true;}if(wardrobeBtn.contains(x,y)){ctx.startActivity(new Intent(ctx,WardrobeActivity.class));return true;}if(action.contains(x,y)){prefs.edit().putBoolean("school_first_day_complete",false).putInt("school_stage",GATE).apply();stage=GATE;mistakes=0;feedback="";invalidate();return true;}}
                return true;
            }
            return true;
        }
'''
section(
    "school",
    '        @Override public boolean onTouchEvent(MotionEvent e){',
    '        @Override public boolean performClick(){',
    touch,
    "school touch handler with mitten drag",
)

for key, path in paths.items():
    path.write_text(src[key], encoding="utf-8")
print("Applied SnowmanGame v17.2 teacher mitten canon + softer girl expression")
