package com.mlmg.hiragana;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class PlayPaintActivity extends PlayActivity {

    private class Cords{
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
    private class Score{
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

    private static final int IMAGE_WIDTH = 256;
    private static final int IMAGE_HEIGHT = 256;
    private static final int S = 25;

    private HashMap<String,Cords> checkPoints = new HashMap<>();
    private Cords[][] checkPointsArray = new Cords[IMAGE_WIDTH][IMAGE_HEIGHT];

    private TextView textCorrect;

    private static final int ARRAY_SIZE = 16;

    private RelativeLayout mainLL;
    private DrawingView drawingView;
    private Button buttonRefresh;
    private Button buttonNext;

    private ImageView ivModel;

    private float matchingValue = 0;

    private boolean[][] modelSign = new boolean[ARRAY_SIZE][ARRAY_SIZE];
    private boolean[][] userSign = new boolean[ARRAY_SIZE][ARRAY_SIZE];

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

        ivModel = (ImageView) findViewById(R.id.debug2);
        mainLL = (RelativeLayout) findViewById(R.id.mainView);
        mainLL.setAlpha(0);
        doAnimations();

        textCorrect = (TextView) findViewById(R.id.textView);

        drawingView = (DrawingView)findViewById(R.id.drawingView);

        buttonRefresh = (Button)findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearView();
            }
        });

        buttonNext = (Button)findViewById(R.id.buttonEnd);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPoints = new HashMap<>();
                checkPointsArray = new Cords[IMAGE_WIDTH][IMAGE_HEIGHT];

                Bitmap bitmap = drawingView.getBitmap();
                bitmap = cutBitmap(bitmap);

                Context context = ivModel.getContext();
                int id = context.getResources().getIdentifier(letter.getLetter_l().toLowerCase()
                        , "drawable", context.getPackageName());

                Drawable drawable = getResources().getDrawable(id);
                Bitmap modelBitmap = ((BitmapDrawable) drawable).getBitmap();
                modelBitmap = cutBitmap(modelBitmap);
                Drawable d = new BitmapDrawable(modelBitmap);

                ivModel.setBackground(d);

                int cpCount = setCheckPoints(modelBitmap);
                Score score = achieveCheckPoints(bitmap);
                float scoreP = getPercentageScore(score.getScore(), cpCount, score.getBadPixels());
                textCorrect.setText(String.format("%.02f", scoreP) + "%");
            }
        });
    }

    @Override
    protected void losujMain(){
        int letterUid = letter!=null? letter.getUid(): -1;
        boolean losuj = true;

        while(losuj) {
            letter = dbHiragana.getRandomAll();
            losuj = (letter.getUid() == letterUid || letter.getLetter_l().equals("WI") || letter.getLetter_l().equals("WE"));
        }
        titleText.setText(letter.getLetter_h());
    }

    private float getPercentageScore(float c, float a, float bp){
        float score = c/a*100 - bp/40;
        if (score<0)
                score =0;
        return score;
    }

    @Override
    protected void setUi(){
        titleText = (TextView) findViewById(R.id.titleTextView);
    }

    @Override
    protected void refreshText(){
        titleText.setText(letter.getLetter_l());
    }

    @Override
    protected void initialize(){
        layAd = (LinearLayout) findViewById(R.id.layad);
        helperApplication = (HelperApplication) getApplication();
        helperApplication.loadAd(layAd);

        apiHelper.signInSilently();

        dbHiragana = new HiraganaDatabase(HelperApplication.getAppContext());
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());
    }

    private String cordsToString(int x, int y){
        return "X" + Integer.toString(x) + "Y" + Integer.toString(y);
    }

    @Override
    protected void setScene(){
        losujMain();
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




    private Bitmap cutBitmap(Bitmap bitmap){
        int x1=-1, y1=-1, x2=-1, y2=-1;
        int pixel;

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

        //find right margin
        for(int w=bitmap.getWidth()-1; w>=0; w--){
            for(int h=bitmap.getHeight()-1; h>=0; h--){
                pixel = bitmap.getPixel(w,h);
                if(pixel == Color.WHITE){
                    x2 = w;
                    break;
                }
                if(x2!=-1)
                    break;

            }
        }


        //find top margin
        for(int h=0; h<bitmap.getHeight(); h++){
            for(int w=0; w<bitmap.getWidth(); w++){
                pixel = bitmap.getPixel(w,h);
                if(pixel == Color.WHITE) {
                    y1 = h;
                    break;
                }
            }
            if(y1!=-1)
                break;
        }

        //find bot margin
        for(int h=bitmap.getHeight()-1; h>=0; h--){
            for(int w=0; w<bitmap.getWidth(); w++){
                pixel = bitmap.getPixel(w,h);
                if(pixel == Color.WHITE) {
                    y2 = h;
                    break;
                }
            }
            if(y2!=-1)
                break;
        }

        Bitmap result = bitmap;
        if(x1>0 && x2>0 && y1>0 && y2>0 && x2-x1>0 && y2-y1>0)
            result = Bitmap.createBitmap(bitmap, x1, y1, x2 - x1,y2 - y1);
        result = Bitmap.createScaledBitmap(result, IMAGE_WIDTH, IMAGE_HEIGHT, true);
        return result;
    }

    private Score achieveCheckPoints(Bitmap b){
        int pixel;
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
        }
        return score;
    }

    private int setCheckPoints(Bitmap b){
        int pixel;
        int points = 0;
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
        }
        return points;
    }

    private void saveCheckpoint(int x, int y){
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
