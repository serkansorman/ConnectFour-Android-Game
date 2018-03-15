package com.example.serkansorman.connect4;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Shows menu and input dialogs
 */
public class MenuActivity extends Activity {

    /**Normal or timed*/
    private String gameVersion;
    /**PvP or PvC*/
    private String gameMode;
    /**Game board size*/
    private String strSize="5";
    /**waiting time for move*/
    private String timeVal = "10";
    private Button menuButtons[];
    /**Access index for menu buttons*/
    private int index = 0;


    @Override
    /**
     * Sets menu buttons and image animations
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.menu);
        final Animation bubble = AnimationUtils.loadAnimation(this, R.anim.bubble);
        final Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
        final ImageView logo = findViewById(R.id.Logo);
        menuButtons = new Button[4];

        menuButtons[0] = findViewById(R.id.PvPButton);
        menuButtons[1] = findViewById(R.id.PvCButton);
        menuButtons[2] = findViewById(R.id.About);
        menuButtons[3] = findViewById(R.id.Exit);

        for(int i = 0;i<4;++i)//Makes invisible all buttons at the beginning
            menuButtons[i].setVisibility(View.INVISIBLE);


        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {//Sets time between animations
                menuButtons[0].startAnimation(bubble);
                bubble.setDuration(300);
                bubbleAnim(bubble);

            }
        }, 500);

        shake.setRepeatCount(Animation.INFINITE);
        logo.startAnimation(shake);
    }

    /**
     * Set bubble animation to menu buttons
     * @param anim bubble animation
     */
    private void bubbleAnim(final Animation anim){

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                menuButtons[0].setVisibility(View.VISIBLE);
                menuButtons[index].setAnimation(null);
                menuButtons[index + 1].startAnimation(anim);
                menuButtons[index + 1].setVisibility(View.VISIBLE);
                ++index;
                if(index == 3)//Stop animation in last menu button
                    return;
                bubbleAnim(anim);

            }
        }, 300);

    }

    /**
     * Prepare game for PvP mode
     * @param view
     */
    public void pvpMode(View view){
        gameMode = "PvP";
        selectGameVersion();
    }

    /**
     * Prepare game for PvC mode
     * @param view
     */
    public void pvcMode(View view){
        gameMode = "PvC";
        selectGameVersion();
    }

    /**
     * Take board size and time with seek bar
     */
    private void takeTimeAndSize() {
        final Dialog d = new Dialog(this);
        d.getWindow().setBackgroundDrawable(resize(getDrawable(R.drawable.wod),800,500));
        d.setCancelable(false);
        d.setContentView( R.layout.input);

        ImageView iv =  d.findViewById(R.id.setting);//et setting image
        iv.setImageResource(R.drawable.settings);

        ImageView iv2 = d.findViewById(R.id.clock);//Set clock image
        iv2.setImageResource(R.drawable.time);

        if(gameVersion.equals("normal")) {//Makes invisible timed game components, if game mode is not timed
            TextView tv = d.findViewById(R.id.textView2);
            iv2.setVisibility(View.GONE);
            tv.setVisibility(View.GONE);
        }

        Button yes = d.findViewById(R.id.button);
        yes.setBackgroundResource(R.drawable.marks);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                sendData();//Send all data after user taps okay
            }
        });

        Button no = d.findViewById(R.id.button2);
        no.setBackgroundResource(R.drawable.ddd);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        //Set time and size bar
        SeekBar sizeBar = d.findViewById(R.id.seekBar);
        SeekBar timeBar = d.findViewById(R.id.seekBar2);
        final TextView sizeText = d.findViewById(R.id.size);
        final TextView timeText = d.findViewById(R.id.time);
        sizeBarChange(sizeBar,sizeText);

        if(gameVersion.equals("timed"))
            timeBarChange(timeBar, timeText);
        else{//Makes invisible timed game components, if game mode is not timed
            timeBar.setVisibility(View.INVISIBLE);
            timeText.setVisibility(View.INVISIBLE);
        }

        d.show();
    }

    /**
     * Sets size bar settings
     * @param sb size bar
     * @param tv board size
     */
    private void sizeBarChange(SeekBar sb,final TextView tv){
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                    tv.setText(Integer.toString(progress + 5) + " x " + Integer.toString(progress + 5));
                    strSize = Integer.toString(progress + 5);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Sets time bar settings
     * @param sb time bar
     * @param tv time
     */
    private void timeBarChange(SeekBar sb,final TextView tv){
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                timeFormat(progress + 10, tv);
                timeVal = Integer.toString(progress + 10);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Convert given time to digital format
     * @param time input time
     * @param tv time on screen
     */
    private void timeFormat(int time,TextView tv){

        NumberFormat f = new DecimalFormat("00");
        long min = time  / 60;
        long sec = time % 60;

        tv.setText(f.format(min) + ":" + f.format(sec));

    }

    /**
     * Quit game if user taps exit
     * @param view
     */
    public void Quit(View view) {

        final Dialog d = new Dialog(this);
        d.setCancelable(false);
        d.setContentView( R.layout.custom);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ImageView iv =  d.findViewById(R.id.imageView1);
        iv.setImageResource(R.drawable.exit);

        Button ok = d.findViewById(R.id.back);
        ok.setBackgroundResource(R.drawable.marks);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                finish();
                System.exit(0);
            }
        });
        Button no = d.findViewById(R.id.retry);
        no.setBackgroundResource(R.drawable.ddd);
        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });
        d.show();
    }

    /**
     * Send data to Game
     */
    private void sendData(){
        Intent myIntent = new Intent(MenuActivity.this, Game.class);
        myIntent.putExtra("boardSize",strSize);
        myIntent.putExtra("gameMode",gameMode);
        myIntent.putExtra("gameVersion",gameVersion);
        myIntent.putExtra("timeVal",timeVal);
        Toast.makeText(getApplicationContext(),strSize + " x " + strSize + " Game is Preparing...",Toast.LENGTH_SHORT).show();
        MenuActivity.this.startActivity(myIntent);
        MenuActivity.this.finish();
    }

    /**
     * Resize images
     * @param image image that will resized
     * @return image which is resized
     */
    private Drawable resize(Drawable image, int w,int h) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, w, h, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    /**
     * Pops up game versions
     */
    private void selectGameVersion(){

        final Dialog d = new Dialog(this);
        d.setContentView( R.layout.game_version);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button normalMode = d.findViewById(R.id.normalMode);
        normalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                gameVersion = "normal";
                takeTimeAndSize();
            }
        });

        Button timedMode = d.findViewById(R.id.timedMode);
        timedMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                gameVersion = "timed";
                takeTimeAndSize();

            }
        });
        d.show();
    }

    /**
     * Shows how to play game
     * @param view
     */
    public void howToPlay(View view){

        final Dialog d = new Dialog(this);
        d.setContentView( R.layout.custom);
        d.getWindow().setBackgroundDrawableResource(R.drawable.scroll);
        Button dialogButton = d.findViewById(R.id.back);
        dialogButton.setBackgroundResource(R.drawable.back);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
            }
        });

        Button retry = d.findViewById(R.id.retry);
        retry.setVisibility(View.INVISIBLE);
        Button back = d.findViewById(R.id.back);
        back.setVisibility(View.INVISIBLE);


        d.show();
    }
}
