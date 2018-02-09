package com.mlmg.hiragana;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class PlayPaintProblematicActivity extends PlayPaintActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_draw_problematic);

        initialize();
        setUi();
        setScene();
        refreshText();
        doAnimations();
    }

    @Override
    protected void losujMain(){
        int letterUid = letter!=null? letter.getUid(): -1;
        boolean losuj = true;
        String l;

        while(losuj) {
            l = dbPlayer.getWorseDrawLetter();
            letter = dbHiragana.getLetter(l);
            losuj = (letter.getUid() == letterUid || letter.getLetter_l().equals("WI") || letter.getLetter_l().equals("WE"));
        }
        titleText.setText(letter.getLetter_h());
    }
}
