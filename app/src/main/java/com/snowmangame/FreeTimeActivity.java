package com.snowmangame;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Free-play hub after the counted school day. Never mutates school day/year counters. */
public class FreeTimeActivity extends Activity {
    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        float d=getResources().getDisplayMetrics().density;
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER_HORIZONTAL);root.setPadding((int)(20*d),(int)(28*d),(int)(20*d),(int)(20*d));root.setBackgroundColor(Color.rgb(238,247,250));
        TextView title=new TextView(this);title.setText("ВІЛЬНИЙ ЧАС");title.setTextSize(25);title.setTextColor(Color.rgb(38,73,94));title.setGravity(Gravity.CENTER);root.addView(title,new LinearLayout.LayoutParams(-1,(int)(64*d)));
        TextView info=new TextView(this);info.setText("Зарахований день уже прожито. Але гра не закінчилась — обирай, що хочеш робити далі. Вільні заняття не витрачають день і не чекають нової дати.");info.setTextSize(15);info.setTextColor(Color.rgb(78,111,127));info.setGravity(Gravity.CENTER);root.addView(info,new LinearLayout.LayoutParams(-1,(int)(112*d)));
        add(root,"ЛІПИТИ СНІГОВИКА",new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(FreeTimeActivity.this,MainActivity.class));}});
        add(root,"СНІГОПЛАВАННЯ ДЛЯ РОЗВАГИ",new View.OnClickListener(){public void onClick(View v){Intent i=new Intent(FreeTimeActivity.this,SnowSwimActivity.class);i.putExtra("freePlay",true);startActivity(i);}});
        add(root,"СПОГАДИ",new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(FreeTimeActivity.this,MemoryActivity.class));}});
        add(root,"ГАРДЕРОБ",new View.OnClickListener(){public void onClick(View v){startActivity(new Intent(FreeTimeActivity.this,WardrobeActivity.class));}});
        TextView rule=new TextView(this);rule.setText("Новий день відкриває нову сюжетну подію, але грати можна скільки хочеш уже зараз.");rule.setTextSize(13);rule.setTextColor(Color.rgb(101,128,142));rule.setGravity(Gravity.CENTER);root.addView(rule,new LinearLayout.LayoutParams(-1,(int)(86*d)));
        setContentView(root);
    }
    void add(LinearLayout root,String label,View.OnClickListener click){float d=getResources().getDisplayMetrics().density;Button b=new Button(this);b.setText(label);b.setTextSize(15);b.setAllCaps(false);b.setOnClickListener(click);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,(int)(60*d));lp.setMargins(0,(int)(7*d),0,(int)(7*d));root.addView(b,lp);}
}
