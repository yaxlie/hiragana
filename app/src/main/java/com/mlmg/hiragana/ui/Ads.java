package com.mlmg.hiragana.ui;

import android.app.Activity;
import android.app.Application;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.mlmg.hiragana.R;

/**
 * Created by Marcin on 01.02.2018.
 */

public class Ads extends Application {

    AdView adView;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub

        super.onCreate();

        adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(getResources().getString(R.string.ad_id));
        // Request for Ads
        AdRequest adRequest = new AdRequest.Builder().build();

        // Load ads into Banner Ads
        adView.loadAd(adRequest);
    }

    public void loadAd(LinearLayout layAd) {

        // Locate the Banner Ad in activity xml
        if (adView.getParent() != null) {
            ViewGroup tempVg = (ViewGroup) adView.getParent();
            tempVg.removeView(adView);
        }

        layAd.addView(adView);

    }

}
