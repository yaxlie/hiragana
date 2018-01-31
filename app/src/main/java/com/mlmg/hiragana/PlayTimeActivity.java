package com.mlmg.hiragana;

import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

public class PlayTimeActivity extends PlayActivity {

    private static int startTime = 35;
    private static int correctPoints = 12;
    private static int timePenalty = 5000;


    private long timeLeft;

    private TextView scoreText;
    private TextView timeText;

    private int bestScore;


    private CountDownTimer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_time);

        mainLL = (RelativeLayout) findViewById(R.id.mainView);
        mainLL.setAlpha(0);

        apiHelper.signInSilently();
        timeLeft = startTime;

        dbHiragana = new HiraganaDatabase(HelperApplication.getAppContext());
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());

        scoreText = (TextView) findViewById(R.id.scoreTextView);
        titleText = (TextView) findViewById(R.id.titleTextView);
        timeText = (TextView) findViewById(R.id.timeTextView);

        button[0] = (Button) findViewById(R.id.button1);
        button[1] = (Button) findViewById(R.id.button2);
        button[2] = (Button) findViewById(R.id.button3);
        button[3] = (Button) findViewById(R.id.button4);

        apiHelper.loadAdd();
        timeText.setText(Integer.toString(startTime));
        scoreText.setText(Integer.toString(score));

        bestScore = dbPlayer.getTimescore();
        TextView textBest = (TextView) findViewById(R.id.textBestScore);
        textBest.setText("Best Score : " +Integer.toString(bestScore));

        setScene();

        Animation anim = AnimationUtils.loadAnimation(PlayTimeActivity.this, R.anim.fadein_anim);
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
                timer = setUpTimer(startTime * 1000).start();
            }
        });
        mainLL.startAnimation(anim);

    }

    @Override
    public void onBackPressed() {
        try {
            timer.cancel();
            over();
            super.onBackPressed();
        }
        catch (Exception e){}
    }


    @Override
    protected void correctAnswer() {
        scoreText.setText(Integer.toString(++score));
        dbPlayer.addPoints(correctPoints);
        setButtonsActive(false);
        audioPlayer("correct");


        if(apiHelper.isSignedIn()) {
            apiHelper.progressAchi(getString(R.string.achievement_points_master), correctPoints);
            apiHelper.progressAchi(getString(R.string.achievement_points____whut), correctPoints/10);
            apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
        }

        handler.postDelayed(new Runnable() {
            public void run() {
                setScene();
            }
        }, 50);
    }

    @Override
    protected void wrongAnswer(){
        //audioPlayer("wrong");
        timer.cancel();
        timeLeft -= timePenalty;
        timer = setUpTimer((int)timeLeft).start();

    }

    @Override
    protected void over(){
        if(dbPlayer.getTimescore() < score)
            dbPlayer.setTimescore(score);
        if(apiHelper.isSignedIn()) {
            apiHelper.updateLeaderboard(getString(R.string.leaderboard_time_challenge), score);
            if (dbPlayer.getScore() >= 2000)
                apiHelper.unlockAchi(getString(R.string.achievement_points_master));
            if (dbPlayer.getScore() >= 25000)
                apiHelper.unlockAchi(getString(R.string.achievement_points____whut));
        }
    }

    private CountDownTimer setUpTimer(int time){
        return new CountDownTimer(time, 100) {

            public void onTick(long millisUntilFinished) {
                double d = millisUntilFinished * 1.0 / 1000;
                timeLeft = millisUntilFinished;
                if(d<5)
                    timeText.setTextColor(Color.RED);
                timeText.setText(String.format( "%.2f", d ));
            }

            public void onFinish() {
                timeText.setText("0.0");
                setButtonsActive(false);

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(PlayTimeActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar);
                } else {
                    builder = new AlertDialog.Builder(PlayTimeActivity.this);
                }
                builder.setTitle("Stop!")
                        .setMessage("Score : \n \n" + Integer.toString(score))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                over();
                                finish();
                            }
                        }).setCancelable(false)
                        .show();
            }
        };
    }
}