package com.mlmg.hiragana;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mlmg.hiragana.database.HiraganaDatabase;
import com.mlmg.hiragana.database.HiraganaTable;
import com.mlmg.hiragana.database.PlayerDatabase;

import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private static int maxToGet = 10;
    private static int maxAttempt = 15;

    private TextView attemptText;
    private TextView titleText;
    private TextView pointsText;

    private Button[] button = new Button[4];
    private Button buttonEnd;

    private Letter letter = null;

    private int levelId;

    private int pointsToGet = maxToGet;
    private int attempt = 1;
    private int score = 0;

    private Handler handler = new Handler();

    private HiraganaDatabase dbHiragana;
    private PlayerDatabase dbPlayer;
    private GoogleApiHelper apiHelper = new GoogleApiHelper(PlayActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        apiHelper.signInSilently();
        apiHelper.loadAdd();

        Bundle b = getIntent().getExtras();
        levelId = b!=null? b.getInt("id"): 1;

        dbHiragana = new HiraganaDatabase(HelperApplication.getAppContext());
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());

        attemptText = (TextView) findViewById(R.id.attemptTextView);
        titleText = (TextView) findViewById(R.id.titleTextView);
        pointsText = (TextView) findViewById(R.id.timeTextView);

        button[0] = (Button) findViewById(R.id.button1);
        button[1] = (Button) findViewById(R.id.button2);
        button[2] = (Button) findViewById(R.id.button3);
        button[3] = (Button) findViewById(R.id.button4);

        buttonEnd = (Button) findViewById(R.id.buttonEnd);
        buttonEnd.setVisibility(View.GONE);
        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                over();
                finish();
            }
        });

        refreshText();
        setScene();
    }

    @Override
    public void onBackPressed() {
        over();
        super.onBackPressed();
    }

    private void setScene(){
        int letterUid = letter!=null? letter.getUid(): -1;
        boolean losuj = true;

        while(losuj) {
            letter = levelId != HiraganaTable.Category.ALL ? dbHiragana.getRandomCategory(levelId) :
                    dbHiragana.getRandomAll();
            losuj = (letter.getUid() == letterUid);
        }
        titleText.setText(letter.getLetter_h());

        for(int i=0; i<4; i++){
            button[i].setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.buttonColor));
            button[i].setText("");
            button[i].setEnabled(true);
        }

        Random rand = new Random();
        final int r = rand.nextInt(4);
        button[r].setText(letter.getLetter_l());
        button[r].setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                button[r].setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.buttonCorrect));
                correctAnswer();
            }
        });

        Letter letterNext = new Letter(letter);
        for(int i=0; i<4; i++){
            //todo do poprawy ten random, wykorzystac   int cSize = dbHiragana.getSizeCategory(levelId);
            if(button[i].getText().equals("")){
                while(letterNext.getLetter_l().equals(button[0].getText()) ||
                        letterNext.getLetter_l().equals(button[1].getText()) ||
                        letterNext.getLetter_l().equals(button[2].getText()) ||
                        letterNext.getLetter_l().equals(button[3].getText())) {
                    letterNext = levelId != HiraganaTable.Category.ALL ? dbHiragana.getRandomCategory(levelId) :
                            dbHiragana.getRandomAll();
                }
                button[i].setText(letterNext.getLetter_l());
                final int finalI = i;
                button[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        button[finalI].setBackgroundColor(ContextCompat.getColor(PlayActivity.this, R.color.buttonWrong));
                        button[finalI].setEnabled(false);
                        wrongAnswer();
                    }
                });
            }
        }

    }

    private void correctAnswer() {
        score += pointsToGet;
        dbPlayer.addPoints(pointsToGet);
        pointsToGet = maxToGet;
        setButtonsActive(false);


        if(apiHelper.isSignedIn()) {
            apiHelper.progressAchi(getString(R.string.achievement_points_master), pointsToGet);
            apiHelper.progressAchi(getString(R.string.achievement_points____whut), pointsToGet/10);
            apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
        }

        refreshText();
        if (++attempt <= maxAttempt || levelId==6) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    setScene();
                }
            }, levelId!=6? 700: 100);
        }
        else{
            over();
            buttonEnd.setVisibility(View.VISIBLE);
        }

    }

    private void refreshText(){
        pointsText.setText(Integer.toString(score));
        attemptText.setText(levelId!=6? Integer.toString(attempt) + "/" + Integer.toString(maxAttempt)
                : Integer.toString(attempt));
    }
    private void setButtonsActive(boolean b){
        for(int i=0; i<4; i++){
            button[i].setEnabled(b);
        }
    }
    private void wrongAnswer(){
        pointsToGet /= 2;
    }

    private void over(){
        if(score >= maxToGet * maxAttempt){
            dbPlayer.setCrown(levelId);
            boolean unlockSix = false;

            for(int i=1; i<6; i++){
                if(dbPlayer.isCrowned(i) && !dbPlayer.isUnlocked(i+1)) {
                    dbPlayer.unlockLevel(i+1);
                    dbPlayer.addPoints(50*(i+1));
                    if(i==5)
                        unlockSix = true;
                    if(apiHelper.isSignedIn()) {
                        apiHelper.progressAchi(getString(R.string.achievement_points_master), 50*(i+1));
                        apiHelper.progressAchi(getString(R.string.achievement_points____whut), 5*(i+1));
                        apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
                    }
                    break;
                }
                if(!dbPlayer.isCrowned(i))
                    break;
            }

            if(apiHelper.isSignedIn()) {
                if (unlockSix)
                    apiHelper.unlockAchi(getString(R.string.achievement_sensei));

                switch (levelId) {
                    case 1:
                        apiHelper.unlockAchi(getString(R.string.achievement_level_1));
                        break;
                    case 2:
                        apiHelper.unlockAchi(getString(R.string.achievement_level_2));
                        break;
                    case 3:
                        apiHelper.unlockAchi(getString(R.string.achievement_level_3));
                        break;
                    case 4:
                        apiHelper.unlockAchi(getString(R.string.achievement_level_4));
                        break;
                    case 5:
                        apiHelper.unlockAchi(getString(R.string.achievement_level_5));
                        break;
                }
            }
        }
        else
            dbPlayer.setUnCrown(levelId);


        if(apiHelper.isSignedIn()) {
            if (dbPlayer.getScore() >= 2000)
                apiHelper.unlockAchi(getString(R.string.achievement_points_master));
            if (dbPlayer.getScore() >= 25000)
                apiHelper.unlockAchi(getString(R.string.achievement_points____whut));
        }
    }

}
