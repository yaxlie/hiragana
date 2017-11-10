package com.mlmg.hiragana;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mlmg.hiragana.database.HiraganaDatabase;

public class InfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        addLetters();


    }

    private void addLetters(){
        HiraganaDatabase db = new HiraganaDatabase(HelperApplication.getAppContext());
        LinearLayout layout[] = new LinearLayout[5];

        layout[0] = (LinearLayout) findViewById(R.id.layou1);
        layout[1] = (LinearLayout) findViewById(R.id.layou2);
        layout[2] = (LinearLayout) findViewById(R.id.layou3);
        layout[3] = (LinearLayout) findViewById(R.id.layou4);
        layout[4] = (LinearLayout) findViewById(R.id.layou5);

        //todo wyruwnac te same drugie liter ma, na itd.
        for(int i=0; i<5; i++){
            Cursor res = db.getAllFromCategory(i+1);
            while (res.moveToNext()){
                View child = getLayoutInflater().inflate(R.layout.letters, null);
                TextView h = (TextView) child.findViewById(R.id.textH);
                TextView l = (TextView) child.findViewById(R.id.textL);

                h.setText(res.getString(1));
                l.setText(res.getString(2));

                layout[i].addView(child);
            }
        }
    }
}
