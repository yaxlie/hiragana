package com.mlmg.hiragana;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.mlmg.hiragana.database.HiraganaDatabase;
import com.mlmg.hiragana.database.HiraganaTable;
import com.mlmg.hiragana.database.PlayerDatabase;

import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    private static int maxToGet = 10;
    private static int maxAttempt = 10;

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
    private GoogleApiClient apiClient;
    //private GoogleSignInOptions gso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        //gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build();
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
               // .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .enableAutoManage(this, null).build();

        Bundle b = getIntent().getExtras();
        levelId = b!=null? b.getInt("id"): 1;

        dbHiragana = new HiraganaDatabase(HelperApplication.getAppContext());
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());

        attemptText = (TextView) findViewById(R.id.attemptTextView);
        titleText = (TextView) findViewById(R.id.titleTextView);
        pointsText = (TextView) findViewById(R.id.pointsTextView);

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


        if(apiClient!= null && apiClient.isConnected()) {
            Games.Leaderboards.submitScore(apiClient, getString(R.string.leaderboard_points), dbPlayer.getScore());
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
            boolean unlock = true;
            for(int i=levelId; i>0; i--){
                if(!dbPlayer.isCrowned(i)) {
                    unlock = false;
                }
            }
            if(unlock && !dbPlayer.isUnlocked(levelId+1)){
                dbPlayer.unlockLevel(levelId+1);
                dbPlayer.addPoints(50*levelId+1);
            }

            if(apiClient.isConnected()) {
                if (unlock && levelId == 5)
                    Games.Achievements.unlock(apiClient, getString(R.string.achievement_sensei));

                switch (levelId) {
                    case 1:
                        Games.Achievements.unlock(apiClient, getString(R.string.achievement_level_1));
                    case 2:
                        Games.Achievements.unlock(apiClient, getString(R.string.achievement_level_2));
                    case 3:
                        Games.Achievements.unlock(apiClient, getString(R.string.achievement_level_3));
                    case 4:
                        Games.Achievements.unlock(apiClient, getString(R.string.achievement_level_4));
                    case 5:
                        Games.Achievements.unlock(apiClient, getString(R.string.achievement_level_5));
                }
            }
        }
        else
            dbPlayer.setUnCrown(levelId);


        if(apiClient.isConnected()) {
            if (dbPlayer.getScore() >= 2000)
                Games.Achievements.unlock(apiClient, getString(R.string.achievement_points_master));
            if (dbPlayer.getScore() >= 25000)
                Games.Achievements.unlock(apiClient, getString(R.string.achievement_points____whut));
        }
    }

}
