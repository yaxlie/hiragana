package com.mlmg.hiragana;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.tasks.OnSuccessListener;
import com.mlmg.hiragana.database.PlayerDatabase;

import org.w3c.dom.Text;

import static com.mlmg.hiragana.GoogleApiHelper.RC_SIGN;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener {

    private PlayerDatabase dbPlayer;
    private Button buttonPlay[] = new Button[6];
    private GoogleApiHelper apiHelper = new GoogleApiHelper(MainActivity.this);

    //private GoogleSignInOptions gso;
    private GoogleApiClient apiClient;
    private static final int RC_LEADERBOARD_UI = 9004;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbPlayer = new PlayerDatabase(HelperApplication.getAppContext());

        //gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build();
        //connectGoogle();
        apiHelper.startSignInIntent();

        apiHelper.loadAdd();
        initiateUI();
        updateScore();
        setCrowns();
        activateLevels();
    }

    @Override
    protected void onResume() {
        super.onResume();
        apiHelper.signInSilently();
        updateScore();
        setCrowns();
        activateLevels();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(MainActivity.this, android.R.style.Theme_Holo_Dialog_NoActionBar);
        } else {
            builder = new AlertDialog.Builder(MainActivity.this);
        }
        builder.setTitle(getString(R.string.app_name))
                .setMessage("Quit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void initiateUI(){
        buttonPlay[0] = (Button) findViewById(R.id.buttonA);
        buttonPlay[1] = (Button) findViewById(R.id.buttonI);
        buttonPlay[2] = (Button) findViewById(R.id.buttonU);
        buttonPlay[3] = (Button) findViewById(R.id.buttonE);
        buttonPlay[4] = (Button) findViewById(R.id.buttonO);
        buttonPlay[5] = (Button) findViewById(R.id.buttonAll);

        for(int i=0; i<6; i++){
            final int finalI = i;
            buttonPlay[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                    intent.putExtra("id", finalI+1);
                    startActivity(intent);
                }
            });
        }

        Button achiButton = (Button) findViewById(R.id.buttonAchievements);
        achiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(apiHelper.isSignedIn())
                    apiHelper.showAchievements();
                else
                    apiHelper.startSignInIntent();
            }
        });

        Button rankButton = (Button) findViewById(R.id.buttonRanking);
        rankButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(apiHelper.isSignedIn())
                    apiHelper.showLeaderboard();
                else
                    apiHelper.startSignInIntent();
            }
        });

        Button infoButton = (Button) findViewById(R.id.buttonInfo);
        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InfoActivity.class);
                startActivity(intent);
            }
        });

        Button timeButton = (Button) findViewById(R.id.buttonTime);
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PlayTimeActivity.class);
                startActivity(intent);
            }
        });

        Button rateButton = (Button) findViewById(R.id.rateButton);
        rateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getPackageName());
                Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    startActivity(myAppLinkToMarket);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, " unable to find market app", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void updateScore(){
        TextView scoreText = (TextView)findViewById(R.id.pointsText);
        scoreText.setText(Integer.toString(dbPlayer.getScore()));
    }

    private void activateLevels(){
        Drawable drawable = getResources().getDrawable(R.drawable.button);

        //todo zrobic liste colorow i robic to w petli
        drawable.setColorFilter(getResources().getColor(R.color.level1), PorterDuff.Mode.SRC_ATOP);
        buttonPlay[0].setBackground(drawable);

        drawable = getResources().getDrawable(R.drawable.button);
        drawable.setColorFilter(getResources().getColor(R.color.level2), PorterDuff.Mode.SRC_ATOP);
        buttonPlay[1].setBackground(drawable);

        drawable = getResources().getDrawable(R.drawable.button);
        drawable.setColorFilter(getResources().getColor(R.color.level3), PorterDuff.Mode.SRC_ATOP);
        buttonPlay[2].setBackground(drawable);

        drawable = getResources().getDrawable(R.drawable.button);
        drawable.setColorFilter(getResources().getColor(R.color.level4), PorterDuff.Mode.SRC_ATOP);
        buttonPlay[3].setBackground(drawable);

        drawable = getResources().getDrawable(R.drawable.button);
        drawable.setColorFilter(getResources().getColor(R.color.level5), PorterDuff.Mode.SRC_ATOP);
        buttonPlay[4].setBackground(drawable);

        drawable = getResources().getDrawable(R.drawable.button);
        drawable.setColorFilter(getResources().getColor(R.color.locked), PorterDuff.Mode.SRC_ATOP);
        for(int i=0;i<5;i++){
            buttonPlay[i].setEnabled(dbPlayer.isUnlocked(i+1));
            if(!dbPlayer.isUnlocked(i+1))
                buttonPlay[i].setBackground(drawable);
        }
    }

    private void setCrowns(){
        ImageView crownImage[] = new ImageView[5];
        crownImage[0] = (ImageView) findViewById(R.id.crownA);
        crownImage[1] = (ImageView) findViewById(R.id.crownI);
        crownImage[2] = (ImageView) findViewById(R.id.crownU);
        crownImage[3] = (ImageView) findViewById(R.id.crownE);
        crownImage[4] = (ImageView) findViewById(R.id.crownO);
        for(int i=0; i<5; i++){
            crownImage[i].setVisibility(dbPlayer.isCrowned(i+1)? View.VISIBLE: View.GONE);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                apiHelper.setViewForPopups(signedInAccount, findViewById(R.id.mainView));

                Games.getLeaderboardsClient(MainActivity.this, signedInAccount)
                        .loadCurrentPlayerLeaderboardScore(getString(R.string.leaderboard_points),
                                LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                        .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                            @Override
                            public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                                if (leaderboardScoreAnnotatedData.get()!=null) {
                                    long mPoints = leaderboardScoreAnnotatedData.get().getRawScore();
                                    PlayerDatabase db = new PlayerDatabase(HelperApplication.getAppContext());
                                    if (db.getScore() < (int) mPoints) {
                                        db.setScore((int) mPoints);
                                        updateScore();
                                    }
                                }
                            }
                        });

            } else {
                Toast.makeText(this,"Signing in Error.", Toast.LENGTH_SHORT);
            }
        }
    }

}
