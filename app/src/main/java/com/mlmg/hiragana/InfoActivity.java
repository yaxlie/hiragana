package com.mlmg.hiragana;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mlmg.hiragana.database.HiraganaDatabase;

import java.util.HashMap;

public class InfoActivity extends AppCompatActivity {

    private LinearLayout layout[] = new LinearLayout[5];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        GoogleApiHelper apiHelper = new GoogleApiHelper(InfoActivity.this);
        apiHelper.loadAdd();
        addLetters();


    }

    private void addLetters(){
        HiraganaDatabase db = new HiraganaDatabase(HelperApplication.getAppContext());

        layout[0] = (LinearLayout) findViewById(R.id.layou1);
        layout[1] = (LinearLayout) findViewById(R.id.layou2);
        layout[2] = (LinearLayout) findViewById(R.id.layou3);
        layout[3] = (LinearLayout) findViewById(R.id.layou4);
        layout[4] = (LinearLayout) findViewById(R.id.layou5);

        //todo wyruwnac te same drugie liter ma, na itd.
        for(int i=0; i<5; i++){
            if(i>0){
                makeSpace(i);
            }

            Cursor res = db.getAllFromCategory(i+1);
            while (res.moveToNext()){
                String hString = res.getString(1);
                String lString = res.getString(2);
                if(lString.equals("MI") || lString.equals("RU") || lString.equals("ME")){
                    makeSpace(i);
                }
                if(lString.equals("U")){
                    makeSpace(i);
                }
                View child = getLayoutInflater().inflate(R.layout.letters, null);
                TextView h = (TextView) child.findViewById(R.id.textH);
                TextView l = (TextView) child.findViewById(R.id.textL);

                h.setText(hString);
                l.setText(lString);

                layout[i].addView(child);
            }
        }
    }
    private void makeSpace(int i){
        View child = getLayoutInflater().inflate(R.layout.letters, null);
        TextView h = (TextView) child.findViewById(R.id.textH);
        TextView l = (TextView) child.findViewById(R.id.textL);
        h.setText("");
        l.setText("");
        layout[i].addView(child);
    }
}
