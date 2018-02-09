package com.mlmg.hiragana;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.mlmg.hiragana.database.HiraganaDatabase;
import com.mlmg.hiragana.database.PlayerDatabase;
import com.mlmg.hiragana.ui.Ads;
import com.mlmg.hiragana.ui.VerticalProgressBar;

import java.util.HashMap;

public class InfoActivity extends AppCompatActivity {

    //TODO zapisywanie zlych/dobrych odpowiedzi i wyswietlanie przy znaczkach + progress bar z prawej strony znaczka
    //TODO zapisywanie dokladnosci rysowania poszczegolnych znaczkow + progress bar z lewej strony znaczka

    private LinearLayout layout[] = new LinearLayout[5];
    private RelativeLayout mainLL;
    private HelperApplication helperApplication;
    private PlayerDatabase dbPlayer;
    private LinearLayout layAd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());

        mainLL = (RelativeLayout) findViewById(R.id.mainView);
        mainLL.setAlpha(0);

        GoogleApiHelper apiHelper = new GoogleApiHelper(InfoActivity.this);
        //apiHelper.loadAdd("InfoAds");
        layAd = (LinearLayout) findViewById(R.id.layad);
        helperApplication = (HelperApplication) getApplication();
        helperApplication.loadAd(layAd);

        addLetters();

        Animation anim = AnimationUtils.loadAnimation(InfoActivity.this, R.anim.fadein_anim);
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

    private void addLetters(){
        HiraganaDatabase db = new HiraganaDatabase(HelperApplication.getAppContext());

        layout[0] = (LinearLayout) findViewById(R.id.layou1);
        layout[1] = (LinearLayout) findViewById(R.id.layou2);
        layout[2] = (LinearLayout) findViewById(R.id.layou3);
        layout[3] = (LinearLayout) findViewById(R.id.layou4);
        layout[4] = (LinearLayout) findViewById(R.id.layou5);

        //todo wyrównac te same drugie liter ma, na itd.
        for(int i=0; i<5; i++){
            if(i>0){
                makeSpace(i);
            }

            Cursor res = db.getAllFromCategory(i+1);
            while (res.moveToNext()){
                String hString = res.getString(1);
                String lString = res.getString(2);

                if(lString.equals("MI") || lString.equals("ME")){
                    makeSpace(i);
                }

                View child = getLayoutInflater().inflate(R.layout.letters, null);
                TextView h = (TextView) child.findViewById(R.id.textH);
                TextView l = (TextView) child.findViewById(R.id.textL);

                VerticalProgressBar dPb =(VerticalProgressBar) child.findViewById(R.id.drawProgressbar);
                VerticalProgressBar qPb =(VerticalProgressBar) child.findViewById(R.id.quizProgressbar);

                dPb.setMax(100);
                qPb.setMax(100);

                dPb.setProgress((int)dbPlayer.getStatsDrawValue(lString));
                qPb.setProgress((int)dbPlayer.getStatsQuizValue(lString));

                setProgressColor(dPb);
                setProgressColor(qPb);


                if(lString.equals("WI") || lString.equals("WE")){
                    dPb.setVisibility(View.GONE);
                }

                if(lString.equals("A") || lString.equals("I") || lString.equals("U")
                        || lString.equals("E") || lString.equals("O")){
                    l.setTextColor(HelperApplication.getAppContext().getResources().getColor(R.color.level6));
                }

                h.setText(hString);
                l.setText(lString);

                layout[i].addView(child,0);
            }
            res.close();
        }
    }

    private void setProgressColor(VerticalProgressBar pb){
        if(pb.getProgress()<30){
            pb.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.buttonWrong), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if(pb.getProgress()<60){
            pb.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.level4), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else if(pb.getProgress()<80){
            pb.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.green), android.graphics.PorterDuff.Mode.SRC_IN);
        }
        else{
            pb.getProgressDrawable().setColorFilter(
                    ContextCompat.getColor(this, R.color.buttonCorrect), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void makeSpace(int i){
        View child = getLayoutInflater().inflate(R.layout.letters, null);
        TextView h = (TextView) child.findViewById(R.id.textH);
        TextView l = (TextView) child.findViewById(R.id.textL);
        ImageView imageView = (ImageView)child.findViewById(R.id.iconQuiz);
        imageView.setVisibility(View.INVISIBLE);
        imageView = (ImageView)child.findViewById(R.id.iconDraw);
        imageView.setVisibility(View.INVISIBLE);
        VerticalProgressBar dPb =(VerticalProgressBar) child.findViewById(R.id.drawProgressbar);
        VerticalProgressBar qPb =(VerticalProgressBar) child.findViewById(R.id.quizProgressbar);
        dPb.setVisibility(View.INVISIBLE);
        qPb.setVisibility(View.INVISIBLE);
        h.setText("");
        l.setText("");
        layout[i].addView(child,0);
    }
}
