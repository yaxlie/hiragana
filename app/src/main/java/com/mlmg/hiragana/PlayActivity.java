package com.mlmg.hiragana;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mlmg.hiragana.database.HiraganaDatabase;
import com.mlmg.hiragana.database.HiraganaTable;
import com.mlmg.hiragana.database.PlayerDatabase;

import java.util.Random;

public class PlayActivity extends AppCompatActivity {

    protected static int maxToGet = 10;
    protected static int maxAttempt = 15;

    protected TextView attemptText;
    protected TextView titleText;
    protected TextView pointsText;

    protected RelativeLayout mainLL;

    protected Button[] button = new Button[4];
    protected Button buttonEnd;

    protected Letter letter = null;

    protected int levelId;

    protected int pointsToGet = maxToGet;
    protected int attempt = 1;
    protected int score = 0;

    protected Handler handler = new Handler();

    protected HiraganaDatabase dbHiragana;
    protected PlayerDatabase dbPlayer;
    protected GoogleApiHelper apiHelper = new GoogleApiHelper(PlayActivity.this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        initialize();
        manageEndButton();
        
        Bundle b = getIntent().getExtras();
        levelId = b!=null? b.getInt("id"): 1;

        attemptText = (TextView) findViewById(R.id.attemptTextView);
        titleText = (TextView) findViewById(R.id.titleTextView);
        pointsText = (TextView) findViewById(R.id.timeTextView);

        button[0] = (Button) findViewById(R.id.button1);
        button[1] = (Button) findViewById(R.id.button2);
        button[2] = (Button) findViewById(R.id.button3);
        button[3] = (Button) findViewById(R.id.button4);

        refreshText();
        setScene();
    }

    @Override
    public void onBackPressed() {
        over();
        super.onBackPressed();
    }

    protected void losujMain(){
        int letterUid = letter!=null? letter.getUid(): -1;
        boolean losuj = true;

        while(losuj) {
            letter = levelId != HiraganaTable.Category.ALL ? dbHiragana.getRandomCategory(levelId) :
                    dbHiragana.getRandomAll();
            losuj = (letter.getUid() == letterUid);
        }
        titleText.setText(letter.getLetter_h());
    }

    protected void manageEndButton(){
        buttonEnd = (Button) findViewById(R.id.buttonEnd);
        buttonEnd.setVisibility(View.GONE);
        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                over();
                finish();
            }
        });
    }

    protected void initialize(){
        mainLL = (RelativeLayout) findViewById(R.id.mainView);
        mainLL.setAlpha(0);
        
        apiHelper.signInSilently();
        apiHelper.loadAdd();
        dbHiragana = new HiraganaDatabase(HelperApplication.getAppContext());
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());
        Animation anim = AnimationUtils.loadAnimation(PlayActivity.this, R.anim.fadein_anim);
        anim.setAnimationListener(new Animation.AnimationListener(){
            @Override
            public void onAnimationStart(Animation arg0) {
                mainLL.setAlpha(1);
            }
            @Override
            public void onAnimationRepeat(Animation arg0) {
            }
            @Override
            public void onAnimationEnd(Animation arg0) {
                mainLL.setAlpha(1);
            }
        });
        mainLL.startAnimation(anim);
    }

    protected void setPrzyciski(){
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

    protected void setScene(){
        losujMain();
        setPrzyciski();
    }


    public void audioPlayer(String fileName){
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), getResources().getIdentifier(fileName,"raw",getPackageName()));

        try {
            //mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void updatePoints(){
        score += pointsToGet;
        dbPlayer.addPoints(pointsToGet);
        pointsToGet = maxToGet;

        if(apiHelper.isSignedIn()) {
            apiHelper.progressAchi(getString(R.string.achievement_points_master), pointsToGet);
            apiHelper.progressAchi(getString(R.string.achievement_points____whut), pointsToGet/10);
            apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
        }
    }

    protected void finalizeCorrectAnswer(){
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
            Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_anim_endless);
            buttonEnd.startAnimation(scaleAnim);
        }
    }

    protected void correctAnswer() {
        setButtonsActive(false);
        audioPlayer(letter.getLetter_l().toLowerCase());

        updatePoints();
        refreshText();
        finalizeCorrectAnswer();
    }

    protected void refreshText(){
        pointsText.setText(Integer.toString(score));
        attemptText.setText(levelId!=6? Integer.toString(attempt) + "/" + Integer.toString(maxAttempt)
                : Integer.toString(attempt));
    }
    protected void setButtonsActive(boolean b){
        for(int i=0; i<4; i++){
            button[i].setEnabled(b);
        }
    }
    protected void wrongAnswer(){
        pointsToGet /= 2;
    }

    protected void over(){
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
