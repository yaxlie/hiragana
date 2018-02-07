package com.mlmg.hiragana;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Moments;

import java.io.File;

public class PlayPaintActivity extends AppCompatActivity {

    private RelativeLayout mainLL;
    private DrawingView drawingView;
    private Button buttonRefresh;

//TODO 3 tryby : losowanie z wszystkich; losowanie z najmniej dokladnie rysowanych;
//TODO losowanie z najmniejszym ratio poprawnych/zlych odp

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        mainLL = (RelativeLayout) findViewById(R.id.mainView);
        mainLL.setAlpha(0);
        doAnimations();

        drawingView = (DrawingView)findViewById(R.id.drawingView);

        buttonRefresh = (Button)findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawingView.clearView();
            }
        });

        //initPaint();


//        ImageView img = (ImageView) findViewById(R.id.correctSign);
//        Bitmap bitmap = BitmapFactory.decodeResource(HelperApplication.getAppContext().getResources(),R.drawable.signs);
//        img.setImageBitmap(bitmap);
//
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
        //Moments moments = new Moments(bitmap);

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


}
