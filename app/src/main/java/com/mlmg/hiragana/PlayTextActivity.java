package com.mlmg.hiragana;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class PlayTextActivity extends PlayActivity {

    private EditText editText;
    private Button buttonOk;
    private boolean locked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_text);

        initialize();

        editText = (EditText) findViewById(R.id.editText);

        Bundle b = getIntent().getExtras();
        levelId = b!=null? b.getInt("id"): 1;

        attemptText = (TextView) findViewById(R.id.attemptTextView);
        titleText = (TextView) findViewById(R.id.titleTextView);
        pointsText = (TextView) findViewById(R.id.pointsTextView);

        buttonOk = (Button) findViewById(R.id.buttonOk);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!locked) {
                    locked = true;
                    if (editText.getText().toString().toUpperCase().equals(letter.getLetter_l().toUpperCase()))
                        correctAnswer();
                    else
                        wrongAnswer();
                }
            }
        });

        setSceneT();
        refreshText();
    }

    protected void setSceneT(){
        losujMain();
        refreshText();
        editText.setText("");
    }

    @Override
    protected void finalizeCorrectAnswer(){
        handler.postDelayed(new Runnable() {
            public void run() {
                setSceneT();
//                editText.setTextColor(Color.WHITE);
                titleText.setTextColor(Color.WHITE);
                locked = false;
            }
        }, 700);
    }

    @Override
    protected void wrongAnswer() {
        audioPlayer(letter.getLetter_l().toLowerCase());

//        editText.setTextColor(getResources().getColor(R.color.buttonWrong));
        titleText.setTextColor(getResources().getColor(R.color.buttonWrong));
        titleText.setText(letter.getLetter_h() + " - " + letter.getLetter_l());
        dbPlayer.upStatsQuizValue(letter.getLetter_l(),false);

        handler.postDelayed(new Runnable() {
            public void run() {
                setSceneT();
//                editText.setTextColor(Color.WHITE);
                titleText.setTextColor(Color.WHITE);
                setSceneT();
                locked = false;
            }
        }, 2000);
    }

    @Override
    protected void correctAnswer() {
        audioPlayer(letter.getLetter_l().toLowerCase());
        attempt++;
        dbPlayer.upStatsQuizValue(letter.getLetter_l(),true);

//        editText.setTextColor(getResources().getColor(R.color.buttonCorrect));
        titleText.setTextColor(getResources().getColor(R.color.buttonCorrect));

        updatePoints();
        refreshText();
        finalizeCorrectAnswer();
    }

}
