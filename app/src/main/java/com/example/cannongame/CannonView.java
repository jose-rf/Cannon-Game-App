package com.example.cannongame;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    public Activity activty;
    public boolean nightMode = false;

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activty = (Activity) context;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

            activty.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void stopGame() {

    }

    public void releaseResources() {

    }


    public int getBackgroundColor() {
        return nightMode ? 0xFF000000 : 0xFFFFFFFF; // preto ou branco
    }
}