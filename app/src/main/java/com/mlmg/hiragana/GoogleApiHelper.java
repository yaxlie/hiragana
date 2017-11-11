package com.mlmg.hiragana;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.AnnotatedData;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mlmg.hiragana.database.PlayerDatabase;

/**
 * Created by Marcin on 10.11.2017.
 */

public class GoogleApiHelper {

    private Activity activity;
    public static final int RC_SIGN = 9002;
    public static final int RC_ACHIEVEMENT_UI = 9003;
    public static final int RC_LEADERBOARD_UI = 9004;

    GoogleApiHelper(Activity activity){
        this.activity = activity;
    }

    public void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        signInClient.silentSignIn().addOnCompleteListener(activity,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            GoogleSignInAccount signedInAccount = task.getResult();
                            Games.getGamesClient(activity, signedInAccount)
                                    .setViewForPopups(activity.findViewById(R.id.mainView));
                        } else {
                            //startSignInIntent();
                        }
                    }
                });
    }

    public void setViewForPopups(GoogleSignInAccount signAccount, View v){
        Games.getGamesClient(activity, signAccount).setViewForPopups(v);
    }

    public void startSignInIntent() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
        Intent intent = signInClient.getSignInIntent();
        activity.startActivityForResult(intent, RC_SIGN);
    }

    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(activity) != null;
    }

    public void signOut() {
        if (isSignedIn()) {
            GoogleSignInClient signInClient = GoogleSignIn.getClient(activity,
                    GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);
            signInClient.signOut().addOnCompleteListener(activity,
                    new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // at this point, the user is signed out.
                        }
                    });
        }
    }

    public void unlockAchi(String achiId){
            Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                    .unlock(achiId);
    }

    public void progressAchi(String achiId, int points){
            Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                    .increment(achiId, points);
    }

    public void showAchievements() {
            Games.getAchievementsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                    .getAchievementsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            activity.startActivityForResult(intent, RC_ACHIEVEMENT_UI);
                        }
                    });
    }

    public void updateLeaderboard(String lbId, int score){
            Games.getLeaderboardsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                    .submitScore(lbId, score);
    }


    public void showLeaderboard() {
            Games.getLeaderboardsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                    .getAllLeaderboardsIntent()
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            activity.startActivityForResult(intent, RC_LEADERBOARD_UI);
                        }
                    });
    }

    public void loadScoreFromLeaderBoard(String lbId) {
        if(isSignedIn()) {
            Games.getLeaderboardsClient(activity, GoogleSignIn.getLastSignedInAccount(activity))
                    .loadCurrentPlayerLeaderboardScore(lbId,
                    LeaderboardVariant.TIME_SPAN_ALL_TIME, LeaderboardVariant.COLLECTION_PUBLIC)
                    .addOnSuccessListener(new OnSuccessListener<AnnotatedData<LeaderboardScore>>() {
                        @Override
                        public void onSuccess(AnnotatedData<LeaderboardScore> leaderboardScoreAnnotatedData) {
                            if (leaderboardScoreAnnotatedData.get()!=null) {
                                long mPoints = leaderboardScoreAnnotatedData.get().getRawScore();
                                PlayerDatabase db = new PlayerDatabase(HelperApplication.getAppContext());
                                if (db.getScore() < (int) mPoints) {
                                    db.setScore((int) mPoints);
                                }
                            }
                        }
                    });
        }
    }

    public void loadAdd(){
        AdView adView = (AdView) activity.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

}
