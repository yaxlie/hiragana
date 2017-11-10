package com.mlmg.hiragana.ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by Marcin on 08.11.2017.
 */

public class LogoImageView extends android.support.v7.widget.AppCompatImageView {

    public LogoImageView(Context context) {
        super(context);
    }

    public LogoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LogoImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int WidthSpec, int HeightSpec){

        int newWidthSpec = (int)(WidthSpec * 0.7);
        int newHeightSpec = (int)(HeightSpec * 0.7);
        super.onMeasure(newWidthSpec, newHeightSpec);
    }
}