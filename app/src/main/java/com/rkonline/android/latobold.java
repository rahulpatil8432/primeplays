package com.rkonline.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class latobold extends androidx.appcompat.widget.AppCompatTextView {



    public latobold(Context context) {
        super(context);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "Oxygen-Bold.ttf");
        this.setTypeface(face);
    }

    public latobold(Context context, AttributeSet attrs) {
        super(context, attrs);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "Oxygen-Bold.ttf");
        this.setTypeface(face);
    }

    public latobold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Typeface face=Typeface.createFromAsset(context.getAssets(), "Oxygen-Bold.ttf");
        this.setTypeface(face);
    }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);


    }
}