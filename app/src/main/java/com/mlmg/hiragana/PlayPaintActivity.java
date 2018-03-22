package com.mlmg.hiragana;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.nfc.Tag;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ExpandedMenuView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.mlmg.hiragana.database.HiraganaDatabase;
import com.mlmg.hiragana.database.HiraganaTable;
import com.mlmg.hiragana.database.PlayerDatabase;

import org.w3c.dom.Text;

import java.io.File;
import java.util.HashMap;

public class PlayPaintActivity extends AppCompatActivity {

    protected class Cords{
        private int x;
        private int y;
        private boolean active=false;

        public Cords(int x, int y, boolean active){
            this.x = x;
            this.y = y;
            this.active = active;
        }

        public Cords(int x, int y){
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
    protected class Score{
        private int badPixels=0;
        private int score=0;
        public void incScore(){
            score++;
        }
        public void incbadPixels(){
            badPixels++;
        }

        public int getBadPixels() {
            return badPixels;
        }

        public void setBadPixels(int badPixels) {
            this.badPixels = badPixels;
        }

        public int getScore() {
            return score;
        }

        public void setScore(int score) {
            this.score = score;
        }
    }

    protected static int maxToGet = 30;
    protected static int maxAttempt = 10;

    protected int points;
    protected boolean skipProcessing = false;
    protected TextView attemptText;
    protected TextView titleText;
    protected TextView pointsText;


    protected boolean done = false;
    protected Animation anim;


    protected Button[] button = new Button[4];
    protected Button buttonEnd;

    protected HelperApplication helperApplication;
    protected LinearLayout layAd;

    protected Letter letter = null;
    protected Bitmap modelBitmap;

    protected int levelId;

    protected int correctIdButton;

    protected int pointsToGet = maxToGet;
    protected int attempt = 1;

    protected Handler handler = new Handler();
    protected ProgressDialog dialog;

    protected HiraganaDatabase dbHiragana;
    protected PlayerDatabase dbPlayer;
    protected GoogleApiHelper apiHelper = new GoogleApiHelper(PlayPaintActivity.this);

    protected static final int IMAGE_WIDTH = 256;
    protected static final int IMAGE_HEIGHT = 256;
    protected static final int S = 25;

    protected HashMap<String,Cords> checkPoints = new HashMap<>();
    protected Cords[][] checkPointsArray = new Cords[IMAGE_WIDTH][IMAGE_HEIGHT];

    protected TextView textCorrect;
    protected float percentageScore=0;

    protected static final int ARRAY_SIZE = 16;

    protected RelativeLayout mainLL;
    protected DrawingView drawingView;
    protected Button buttonRefresh;
    protected Button buttonNext;

    protected ImageView ivModel;


    protected boolean[][] modelSign = new boolean[ARRAY_SIZE][ARRAY_SIZE];
    protected boolean[][] userSign = new boolean[ARRAY_SIZE][ARRAY_SIZE];

//TODO 3 tryby : losowanie z wszystkich; losowanie z najmniej dokladnie rysowanych;
//TODO losowanie z najmniejszym ratio poprawnych/zlych odp


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        initialize();
        setUi();
        setScene();
        refreshText();
        doAnimations();

    }

    protected  void processImage(Bitmap bitmap){
        checkPoints = new HashMap<>();
        checkPointsArray = new Cords[IMAGE_WIDTH][IMAGE_HEIGHT];
        int cpCount = setCheckPoints(modelBitmap);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage("60%");
            }
        });

        Score score = achieveCheckPoints(bitmap);
        percentageScore = getPercentageScore(score.getScore(), cpCount, score.getBadPixels());
    }

    protected void loadBitmapModel(){
        Context context = ivModel.getContext();
        int id = context.getResources().getIdentifier("sign_"+letter.getLetter_l().toLowerCase()
                , "drawable", context.getPackageName());
        Drawable drawable = getResources().getDrawable(id);
        modelBitmap = ((BitmapDrawable) drawable).getBitmap();
        modelBitmap = cutBitmap(modelBitmap);
        Drawable d = new BitmapDrawable(modelBitmap);
        Bitmap b = Bitmap.createScaledBitmap(((BitmapDrawable) drawable).getBitmap(), 80, 80, true);
        ivModel.setBackground(new BitmapDrawable(getResources(), b));
    }

    protected void losujMain(){
        int letterUid = letter!=null? letter.getUid(): -1;
        boolean losuj = true;

        while(losuj) {
            letter = dbHiragana.getRandomAll();
            losuj = (letter.getUid() == letterUid || letter.getLetter_l().equals("WI") || letter.getLetter_l().equals("WE"));
        }
        titleText.setText(letter.getLetter_h());
    }

    protected float getPercentageScore(float c, float a, float bp){
        float score = c/a*100 - bp/40;
        if (score<0)
                score =0;
        return score;
    }

    protected void goNext(){
        try {
            attempt++;
            setScene();
            refreshText();
            drawingView.clearView();
            ivModel.setVisibility(View.INVISIBLE);
        }
        catch (Exception e){}
    }

    protected void setUi(){
        titleText = (TextView) findViewById(R.id.titleTextView);
        textCorrect = (TextView) findViewById(R.id.textView);
        attemptText = (TextView) findViewById(R.id.attemptTextView);
        pointsText = (TextView) findViewById(R.id.pointsTextView);
        pointsText.setText("0");
        drawingView = (DrawingView)findViewById(R.id.drawingView);

        buttonRefresh = (Button)findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearView();
            }
        });

        buttonNext = (Button)findViewById(R.id.buttonNext);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(attempt < maxAttempt)
                    goNext();
                else{
                    finish();
                }
            }
        });

        ivModel = (ImageView) findViewById(R.id.debug2);
        mainLL = (RelativeLayout) findViewById(R.id.mainView);
        mainLL.setAlpha(0);

        buttonNext.startAnimation(anim);

        buttonEnd = (Button) findViewById(R.id.buttonEnd);
        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.setMessage("0%");
                dialog.show();

                final Bitmap[] bitmap = {drawingView.getBitmap()};

                new Thread(new Runnable() {
                    public void run() {
                        bitmap[0] = cutBitmap(bitmap[0]);
                        if(!skipProcessing)
                            processImage(bitmap[0]);
                        dialog.dismiss();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ivModel.setVisibility(View.VISIBLE);
                                refreshText();
                                buttonNext.setVisibility(View.VISIBLE);
                                buttonNext.startAnimation(anim);
                                if(!done){
                                    points += maxToGet*(percentageScore/100);
                                    dbPlayer.upStatsDrawValue(letter.getLetter_l(),percentageScore);
                                    updateAchievements((int)(maxToGet*(percentageScore/100)),percentageScore);
                                    done = true;
                                }
                            }
                        });
                    }
                }).start();

            }
        });
    }

    protected void updateAchievements(int points, float percentageScore){
        try {
            dbPlayer.addPoints(points);
            if (apiHelper.isSignedIn() && percentageScore > 0) {
                if(points>0) {
                    apiHelper.progressAchi(getString(R.string.achievement_points_master), points);
                    apiHelper.progressAchi(getString(R.string.achievement_points____whut), points);
                    apiHelper.updateLeaderboard(getString(R.string.leaderboard_points), dbPlayer.getScore());
                }

                float all = dbPlayer.getDrawScoreAll();
                apiHelper.updateLeaderboard(getString(R.string.leaderboard_writers), (long) (all*100));

                if(all>=60){
                    apiHelper.unlockAchi(getString(R.string.achievement_patient));
                }
                if (percentageScore > 80) {
                    apiHelper.unlockAchi(getString(R.string.achievement_hand_writer));
                    apiHelper.progressAchi(getString(R.string.achievement_novelist), 1);
                }
            }
        }
        catch (Exception e){}
    }

    protected void refreshText(){
        titleText.setText(letter.getLetter_l());
        textCorrect.setText(String.format("%.02f", percentageScore) + "%");
        attemptText.setText(Integer.toString(attempt) + "/" + Integer.toString(maxAttempt));
        pointsText.setText(Integer.toString(points));
    }

    protected void initialize(){
        anim = AnimationUtils.loadAnimation(PlayPaintActivity.this, R.anim.scale_anim_endless);
        dialog = new ProgressDialog(this, android.R.style.Theme_Holo_Dialog_NoActionBar);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);

        layAd = (LinearLayout) findViewById(R.id.layad);
        helperApplication = (HelperApplication) getApplication();
        helperApplication.loadAd(layAd);

        apiHelper.signInSilently();

        dbHiragana = new HiraganaDatabase(HelperApplication.getAppContext());
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());
    }

    protected String cordsToString(int x, int y){
        return "X" + Integer.toString(x) + "Y" + Integer.toString(y);
    }

    protected void setScene(){
        losujMain();
        percentageScore=0;
        checkPoints = new HashMap<>();
        checkPointsArray = new Cords[IMAGE_WIDTH][IMAGE_HEIGHT];
        loadBitmapModel();
        ivModel.setVisibility(View.INVISIBLE);
        buttonNext.setVisibility(View.INVISIBLE);
        buttonNext.clearAnimation();
        done = false;
    }

    protected void doAnimations(){
        Animation anim = AnimationUtils.loadAnimation(PlayPaintActivity.this, R.anim.fadein_anim);
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



    protected void setDialogProgress(final float progress){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.setMessage(String.format("%.00f", progress)+"%");
            }
        });
    }

    protected Bitmap cutBitmap(Bitmap bitmap){
        int x1=-1, y1=-1, x2=-1, y2=-1;
        int pixel;
        skipProcessing = false;

        bitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, true);

        //find left margin
        for(int w=0; w<bitmap.getWidth(); w++){
            for(int h=0; h<bitmap.getHeight(); h++){
                pixel = bitmap.getPixel(w,h);
                if(pixel == Color.WHITE){
                    x1 = w;
                    break;
                }
            }
            if(x1!=-1)
                break;
        }

        setDialogProgress(5);

        if(x1 != -1) {
            //find right margin
            for (int w = bitmap.getWidth() - 1; w >= 0; w--) {
                for (int h = bitmap.getHeight() - 1; h >= 0; h--) {
                    pixel = bitmap.getPixel(w, h);
                    if (pixel == Color.WHITE) {
                        x2 = w;
                        break;
                    }
                    if (x2 != -1)
                        break;

                }
            }

            setDialogProgress(10);

            //find top margin
            for (int h = 0; h < bitmap.getHeight(); h++) {
                for (int w = 0; w < bitmap.getWidth(); w++) {
                    pixel = bitmap.getPixel(w, h);
                    if (pixel == Color.WHITE) {
                        y1 = h;
                        break;
                    }
                }
                if (y1 != -1)
                    break;
            }

            setDialogProgress(15);

            //find bot margin
            for (int h = bitmap.getHeight() - 1; h >= 0; h--) {
                for (int w = 0; w < bitmap.getWidth(); w++) {
                    pixel = bitmap.getPixel(w, h);
                    if (pixel == Color.WHITE) {
                        y2 = h;
                        break;
                    }
                }
                if (y2 != -1)
                    break;
            }
        }
        else
            skipProcessing = true;

        setDialogProgress(20);

        Bitmap result = bitmap;
        if(x1>0 && x2>0 && y1>0 && y2>0 && x2-x1>0 && y2-y1>0)
            result = Bitmap.createBitmap(bitmap, x1, y1, x2 - x1,y2 - y1);
        result = Bitmap.createScaledBitmap(result, IMAGE_WIDTH, IMAGE_HEIGHT, true);
        return result;
    }

    protected Score achieveCheckPoints(Bitmap b){
        int pixel;
        final float wMax = b.getWidth();
        Score score= new Score();
        for(int w=0; w<b.getWidth(); w++){
            for(int h=0; h<b.getHeight(); h++){
                pixel = b.getPixel(w,h);
                if(pixel == Color.WHITE){
                    if(checkPointsArray[w][h]!=null
                            && !checkPoints.get(cordsToString(checkPointsArray[w][h].getX(),checkPointsArray[w][h].getY())).active ){
                        score.incScore();
                        checkPoints.get(cordsToString(checkPointsArray[w][h].getX(),checkPointsArray[w][h].getY())).setActive(true);
                    }
                    else if(checkPointsArray[w][h]==null){
                        score.incbadPixels();
                    }
                }
            }
            final int finalW = w;
            setDialogProgress(finalW /wMax/10*400 + 60);
        }
        return score;
    }

    protected int setCheckPoints(Bitmap b){
        int pixel;
        int points = 0;
        final float wMax = b.getWidth();
        for(int w=0; w<b.getWidth(); w++){
            for(int h=0; h<b.getHeight(); h++){
                pixel = b.getPixel(w,h);
                //Log.d("W:" ,Integer.toString(w));
                if(pixel == Color.WHITE){
                    if(checkPointsArray[w][h]==null){
                        saveCheckpoint(w,h);
                        points++;
                    }
                }
            }
            final float finalW = w;
            setDialogProgress(finalW /wMax/10*400 + 20);
        }
        return points;
    }

    protected void saveCheckpoint(int x, int y){
        checkPoints.put(cordsToString(x,y), new Cords(x,y));
        int sX = x-S>=0?x-S:0;
        int sY = y-S>=0?y-S:0;
        int eX = x+S<IMAGE_WIDTH?x+S:IMAGE_WIDTH-1;
        int eY = y+S<IMAGE_HEIGHT?y+S:IMAGE_HEIGHT-1;
        for(int w = sX; w<eX; w++){
            for(int h = sY; h<eY; h++){
                checkPointsArray[w][h] = new Cords(x, y, true);
            }
        }
    }


}
