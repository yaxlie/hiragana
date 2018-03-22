package com.mlmg.hiragana;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.Random;

public class PlayDuelActivity extends PlayActivity {

    private int botLevel;
    private TextView tPlayer;
    private TextView tBot;
    private int playerCorrect = 0;
    private int botCorrect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_duel);

        initialize();

        Bundle b = getIntent().getExtras();
        levelId = b!=null? b.getInt("id"): 1;

        botLevel = dbPlayer.getBotLevel();

        setUiDuel();
        refreshText();
        setScene();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void setUiDuel(){
        titleText = (TextView) findViewById(R.id.titleTextView);

        tPlayer = (TextView)findViewById(R.id.playerScore);
        tBot = (TextView)findViewById(R.id.botScore);

        button[0] = (Button) findViewById(R.id.button1);
        button[1] = (Button) findViewById(R.id.button2);
        button[2] = (Button) findViewById(R.id.button3);
        button[3] = (Button) findViewById(R.id.button4);
    }

    @Override
    protected void setPrzyciski(){
        super.setPrzyciski();
        for(int i=0; i<4; i++){
            button[i].setCompoundDrawablesWithIntrinsicBounds( null, null, null, null);
        }
    }
    private void botAnswer(){
        Random rand = new Random();
        int  n = rand.nextInt(100) + 1;
        int id;
        id =  n > 100-botLevel? correctIdButton: (correctIdButton + rand.nextInt(3) + 1)%4;

        if(id == correctIdButton){
            botCorrect++;
        }

        Drawable img = getResources().getDrawable( R.drawable.android_icon );
        img.setBounds(10,0,0,0);
        button[id].setCompoundDrawablesWithIntrinsicBounds( img, null, null, null);
    }

    @Override
    protected void correctAnswer() {
        setButtonsActive(false);
        audioPlayer(letter.getLetter_l().toLowerCase());
        playerCorrect++;
        botAnswer();

        finalizectAnswer();
    }

    @Override
    protected void wrongAnswer(){
        setButtonsActive(false);
        button[correctIdButton].setBackgroundColor(ContextCompat.getColor(PlayDuelActivity.this, R.color.buttonCorrect));
        audioPlayer(letter.getLetter_l().toLowerCase());
        botAnswer();
        finalizectAnswer();
    }

    private void finalizectAnswer(){
        tPlayer.setText(Integer.toString(playerCorrect));
        tBot.setText(Integer.toString(botCorrect));

        if (++attempt <= maxAttempt) {
            handler.postDelayed(new Runnable() {
                public void run() {
                    setScene();
                }
            }, 1500);
        }
        else{
            over();
            buttonEnd.setVisibility(View.VISIBLE);
            Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_anim_endless);
            buttonEnd.startAnimation(scaleAnim);
        }
    }

    @Override
    protected void over(){

        int bonus = botLevel<95? (botLevel/2): 100;

        String msg;
        if(playerCorrect == botCorrect) {
            msg = getString(R.string.draw_bonus) + bonus/2;

            dbPlayer.addPoints(bonus/2);
            if(apiHelper.isSignedIn()) {
                apiHelper.progressAchi(getString(R.string.achievement_points_master), (bonus/2));
                apiHelper.progressAchi(getString(R.string.achievement_points____whut), (bonus/2));
                apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
            }
        }
        else if(playerCorrect > botCorrect) {
            msg = getString(R.string.win_bonus) + bonus;

            dbPlayer.addPoints(bonus);
            if(apiHelper.isSignedIn()) {
                apiHelper.unlockAchi(getString(R.string.achievement_duelist));
                if(botLevel == 95){
                    apiHelper.unlockAchi(getString(R.string.achievement_android_trainer));
                }
                apiHelper.progressAchi(getString(R.string.achievement_points_master), (bonus));
                apiHelper.progressAchi(getString(R.string.achievement_points____whut), (bonus));
                apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
            }

            dbPlayer.winDuel();
        }
        else {
            msg = getString(R.string.lose);
            dbPlayer.loseDuel();
        }

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(PlayDuelActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar);
        } else {
            builder = new AlertDialog.Builder(PlayDuelActivity.this);
        }
        builder.setTitle("Hiragana DUEL")
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .show();

        if(apiHelper.isSignedIn()) {
            if (dbPlayer.getScore() >= 2000)
                apiHelper.unlockAchi(getString(R.string.achievement_points_master));
            if (dbPlayer.getScore() >= 25000)
                apiHelper.unlockAchi(getString(R.string.achievement_points____whut));
        }

    }

}
