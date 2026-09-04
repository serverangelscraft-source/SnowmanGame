package com.snowmangame;

/** Optional story/visual integrations for school life.
 * They never award currency and never touch class/day progression keys.
 */
public final class SchoolIntegrationContent {
    private SchoolIntegrationContent() {}

    public static final int NONE=0;
    public static final int ORNAMENT=1;
    public static final int TECH_PICNIC=2;
    public static final int GIFT_WORKSHOP=3;
    public static final int FIVE_MIN_FAIR=4;
    public static final int SHELF_PROJECT=5;
    public static final int BREAK_MEMORY=6;
    public static final int WINTER_YARD=7;
    public static final int SNOW_SHADOW=8;
    public static final int REGIONAL_WORKSHOP=9;
    public static final int CITY_FOR_SNOWMAN=10;

    /** Deterministic by grade + school day. No rerolling on restart. */
    public static int eventFor(int grade,int schoolDay){
        int d=Math.max(1,Math.min(5,schoolDay));
        if(grade==7){int[] a={GIFT_WORKSHOP,ORNAMENT,BREAK_MEMORY,REGIONAL_WORKSHOP,WINTER_YARD};return a[d-1];}
        if(grade==8){int[] a={ORNAMENT,GIFT_WORKSHOP,WINTER_YARD,BREAK_MEMORY,REGIONAL_WORKSHOP};return a[d-1];}
        if(grade==9){int[] a={TECH_PICNIC,GIFT_WORKSHOP,FIVE_MIN_FAIR,SNOW_SHADOW,WINTER_YARD};return a[d-1];}
        if(grade==10){int[] a={FIVE_MIN_FAIR,TECH_PICNIC,SHELF_PROJECT,CITY_FOR_SNOWMAN,SNOW_SHADOW};return a[d-1];}
        if(grade==11){int[] a={SHELF_PROJECT,CITY_FOR_SNOWMAN,SNOW_SHADOW,WINTER_YARD,REGIONAL_WORKSHOP};return a[d-1];}
        return NONE;
    }

    public static String title(int id){
        switch(id){
            case ORNAMENT:return "ЗИМОВА МАЙСТЕРНЯ";
            case TECH_PICNIC:return "ШКІЛЬНИЙ ТЕХНОПІКНІК";
            case GIFT_WORKSHOP:return "МАЙСТЕРНЯ ПОДАРУНКА";
            case FIVE_MIN_FAIR:return "ЯРМАРОК ЗА 5 ХВИЛИН";
            case SHELF_PROJECT:return "ВІД МАЙСТЕРНІ ДО ПОЛИЦІ";
            case BREAK_MEMORY:return "СМАК ЗІ ШКІЛЬНОЇ ПЕРЕРВИ";
            case WINTER_YARD:return "ЗИМОВИЙ ДВІР";
            case SNOW_SHADOW:return "ТІНЬ СНІГОВИКА";
            case REGIONAL_WORKSHOP:return "РЕГІОНАЛЬНА МАЙСТЕРНЯ";
            case CITY_FOR_SNOWMAN:return "МІСТО ДЛЯ СНІГОВИКА";
            default:return "";
        }
    }

    public static String prompt(int id){
        switch(id){
            case ORNAMENT:return "Обери візерунок для зимового образу.";
            case TECH_PICNIC:return "Обери станцію, де хочеш допомогти.";
            case GIFT_WORKSHOP:return "Зроби маленький подарунок другові.";
            case FIVE_MIN_FAIR:return "Обери свою роль у швидкому ярмарку.";
            case SHELF_PROJECT:return "Збери зрозумілий зимовий продукт.";
            case BREAK_MEMORY:return "На перерві знайшлася маленька записка.";
            case WINTER_YARD:return "Обери місце для шкільного фото.";
            case SNOW_SHADOW:return "Знайди позу з найкращим силуетом.";
            case REGIONAL_WORKSHOP:return "Обери стилізований зимовий мотив.";
            case CITY_FOR_SNOWMAN:return "Що зробить зимовий двір зручнішим?";
            default:return "";
        }
    }

    public static String[] choices(int id){
        switch(id){
            case ORNAMENT:return new String[]{"ГЕОМЕТРІЯ","ХВИЛЯ","ЗІРКА"};
            case TECH_PICNIC:return new String[]{"ЗБУДУВАТИ","ОФОРМИТИ","ПРЕЗЕНТУВАТИ"};
            case GIFT_WORKSHOP:return new String[]{"ЛИСТІВКА","ПАКУВАННЯ","СУВЕНІР"};
            case FIVE_MIN_FAIR:return new String[]{"СТРАВА","ПАКУВАННЯ","СТІЙКА"};
            case SHELF_PROJECT:return new String[]{"ФОРМА","ЕТИКЕТКА","ДОСТАВКА"};
            case BREAK_MEMORY:return new String[]{"ПРОЧИТАТИ","ЗБЕРЕГТИ","ПОКАЗАТИ ДРУГУ"};
            case WINTER_YARD:return new String[]{"ЛАВКА","ЛІХТАР","ПОШТОВА СКРИНЬКА"};
            case SNOW_SHADOW:return new String[]{"КАПЕЛЮХ","ШАРФ","ПОВОРОТ"};
            case REGIONAL_WORKSHOP:return new String[]{"РОМБИ","ГІЛКА","СНІЖИНКА"};
            case CITY_FOR_SNOWMAN:return new String[]{"СВІТЛО","ЗАХИСТ ВІД ВІТРУ","ЗРУЧНИЙ ПРОХІД"};
            default:return new String[]{"ДАЛІ"};
        }
    }

    public static String memory(int id,int choice){
        String[] c=choices(id);int i=Math.max(0,Math.min(c.length-1,choice));
        switch(id){
            case ORNAMENT:return "Образ збережено: "+c[i].toLowerCase()+".";
            case TECH_PICNIC:return "Технопікнік: "+c[i].toLowerCase()+".";
            case GIFT_WORKSHOP:return "Подарунок: "+c[i].toLowerCase()+".";
            case FIVE_MIN_FAIR:return "Ярмарок: "+c[i].toLowerCase()+".";
            case SHELF_PROJECT:return "Проєкт: "+c[i].toLowerCase()+".";
            case BREAK_MEMORY:return "Спогад зі шкільної перерви збережено.";
            case WINTER_YARD:return "Фото у дворі: "+c[i].toLowerCase()+".";
            case SNOW_SHADOW:return "Фото «Тінь сніговика» збережено.";
            case REGIONAL_WORKSHOP:return "Майстерня: "+c[i].toLowerCase()+".";
            case CITY_FOR_SNOWMAN:return "Місто для сніговика: "+c[i].toLowerCase()+".";
            default:return "";
        }
    }

    /** One compact save namespace; never count this as a school day. */
    public static String memoryKey(int grade,int schoolDay){
        return "school_integration_memory_g"+grade+"_d"+Math.max(1,Math.min(5,schoolDay));
    }
}
