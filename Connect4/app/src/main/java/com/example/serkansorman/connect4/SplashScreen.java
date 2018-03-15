package com.example.serkansorman.connect4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * Shows splash screen with many animation before the menu is showed
 */
public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        final ImageView logo = findViewById(R.id.imageView);
        final ImageView red = findViewById(R.id.imageView2);
        final ImageView black = findViewById(R.id.imageView3);

        //Creates animations
        final Animation bubble = AnimationUtils.loadAnimation(this, R.anim.bubble);
        final Animation rotate = AnimationUtils.loadAnimation(this, R.anim.rotate);
        final Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        final TranslateAnimation slideDown = new TranslateAnimation(0, 0, -650,0);
        final TranslateAnimation slideUp = new TranslateAnimation(0, 0, 650,0);
        final AnimationSet as = new AnimationSet(true);
        final AnimationSet moveUpRotate = new AnimationSet(true);
        final AnimationSet moveDownRotate = new AnimationSet(true);

        slideDown.setDuration(3000);
        slideDown.setFillAfter(true);
        slideUp.setDuration(3000);
        slideUp.setFillAfter(true);

        red.setVisibility(View.INVISIBLE);
        black.setVisibility(View.INVISIBLE);
        bubble.setDuration(2500);
        as.addAnimation(rotate);
        as.addAnimation(bubble);
        logo.startAnimation(as);

        moveDownRotate.addAnimation(rotate);
        moveDownRotate.addAnimation(slideDown);

        moveUpRotate.addAnimation(rotate);
        moveUpRotate.addAnimation(slideUp);

        as.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                logo.setAnimation(null);
                rotate.setDuration(500);
                black.setVisibility(View.VISIBLE);
                red.setVisibility(View.VISIBLE);
                black.startAnimation(moveDownRotate);
                red.startAnimation(moveUpRotate);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        moveDownRotate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                logo.startAnimation(shake);
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        Intent i = new Intent(getBaseContext(),MenuActivity.class);
                        startActivity(i);
                        finish();

                    }
                }, 3000);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }
}
