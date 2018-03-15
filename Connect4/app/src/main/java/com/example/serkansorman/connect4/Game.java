package com.example.serkansorman.connect4;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;
import java.util.Vector;
/**
 * @author Serkan Sorman
 */

/**
 * Creates a board and plays game
 */
public class Game extends Activity {
    /**Game board size*/
    private int size;
    /**Sequence of player*/
    private int player = 1;
    /**waiting time for move*/
    private int time;
    /**player 1 score*/
    private Integer p1Score = 0;
    /**player 2 score*/
    private Integer p2Score = 0;
    /**Indicates computer first move*/
    private boolean isFirstMove = true;
    /**Normal or timed*/
    private String gameVersion;
    /**PvP or PvC*/
    private String gameMode;
    /**Game board that holds empty or filled cells*/
    private int[][] gameCells;
    /**Game board which is shown on screen*/
    private ImageButton[][] cellButtons;
    /**Buttons for falling animation*/
    private ImageButton[] fallingDiscs;
    private Button undoButton;
    /**Holds all old moves of game*/
    private Vector<Integer> oldMoves;
    /**Random column number for move*/
    private Random rnd;
    private CountDownTimer moveTimer;
    /**All game sounds*/
    private MediaPlayer sound;
    /**Indicates game sound on or of*/
    private boolean soundOn = true;
    /**Indicates countdown sound on or off*/
    private boolean tiktakSound = false;
    /**Holds score images 0,1,2 and 3*/
    private Drawable scoreNum[];
    /**Player 1's score board*/
    private ImageView scoreImageFirst;
    /**Player 1's score board*/
    private ImageView scoreImageSecond;
    /**Countdown timer which is shown on screen*/
    private TextView timePanel;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setGameScreen();
    }

    /**
     * Set board and all components of game screen
     */
    private void  setGameScreen() {
        setContentView(R.layout.game_screen);

        Drawable d1 = this.getResources().getDrawable(R.drawable.wood2, null);
        getWindow().getDecorView().setBackground(d1);
        //Prepares game
        takeData();
        initBoard();
        setScoreImages();

        final GridLayout board = findViewById(R.id.board);
        board.setColumnCount(size);
        if(gameVersion.equals("normal"))
            board.setRowCount(size+1);
        else//Disabled falling disc buttons
            board.setRowCount(size);

        timePanel = findViewById(R.id.timePanel);
        cellButtons = new ImageButton[size][size];
        fallingDiscs = new ImageButton[size];
        setFallingDiscsButton(board);
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                cellButtons[i][j] = new ImageButton(Game.this);
                GridLayout.LayoutParams param = new GridLayout.LayoutParams();
                param.height = 216;
                param.width = 216;
                board.addView(cellButtons[i][j]);
                cellButtons[i][j].setId(j);
                cellButtons[i][j].setLayoutParams(param);
                cellButtons[i][j].setBackgroundResource(R.drawable.empty2);
                cellButtons[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int colNum = v.getId();
                        if (gameVersion.equals("normal")) {//Normal mode
                            if (gameMode.equals("PvP"))
                                playPvP(colNum);
                            else//PvC mode
                                playPvC(colNum);
                        }
                        else {//Timed mode
                            if (gameMode.equals("PvP")){
                                playTimedPvP(colNum);
                            }
                            else {//PvC timed mode
                                playTimedPvC(colNum);
                            }
                        }
                    }
                });
            }
        }

        //Set images that represents users
        ImageView iv = findViewById(R.id.p1);
        iv.setImageDrawable(getDrawable(R.drawable.p1));
        ImageView iv2 = findViewById(R.id.p2);
        iv2.setImageDrawable(getDrawable(R.drawable.p2));

        setMenuButton();
        setUndoButton();
        setSoundButton();
        setScoreTable();
        scrollAtBeginning();
        if (gameVersion.equals("timed"))
            setTimePanel();
    }

    /**
     * Set undo button to screen
     */
    private void setUndoButton(){
        undoButton = findViewById(R.id.undoButton);
        undoButton.setBackgroundResource(R.drawable.undom);
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoMove();
                if(gameVersion.equals("timed")){
                    if(tiktakSound)
                        sound.stop();
                    moveTimer.cancel();//In each clicks,restart countdown timer
                    startCountDown();
                }
                if(gameMode.equals("PvP"))
                    player++;
                else//In PvC mode,undo 2 moves
                    undoMove();

            }
        });
        //Set rotate animation to undo button
        RotateAnimation rotate = new RotateAnimation(360, 0,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(1000);
        rotate.setRepeatCount(Animation.INFINITE);
        undoButton.setAnimation(rotate);
    }

    /**
     * set menu button to screen
     */
    private void setMenuButton(){
        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setBackgroundResource(R.drawable.menu);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backToMenu();
            }
        });
    }

    /**
     * set sound button to screen
     */
    private  void setSoundButton(){

        final Button soundButton=findViewById(R.id.soundButton);
        soundButton.setBackgroundResource(R.drawable.soundon);
        soundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AudioManager appSound=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                if(soundOn) {
                    //Disable sound
                    soundButton.setBackgroundResource(R.drawable.soundoff);
                    appSound.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                    if(tiktakSound)
                        sound.stop();
                    soundOn = false;
                }
                else {
                    //Enable sound
                    soundButton.setBackgroundResource(R.drawable.soundon);
                    appSound.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                    soundOn = true;
                }
            }
        });
    }

    /**
     * set time panel to screen and starts countdown
     */
    private void setTimePanel(){
        timePanel.setTextColor(Color.BLACK);
        timePanel.setTextSize(20);
        timePanel.setTypeface(Typeface.DEFAULT_BOLD);
        startCountDown();
    }

    /**
     * set player's score boards to screen
     */
    private void setScoreTable(){

        scoreImageFirst = findViewById(R.id.p1Score);
        scoreImageFirst.setImageDrawable(getDrawable(R.drawable.zero));
        scoreImageSecond = findViewById(R.id.p2Score);
        scoreImageSecond.setImageDrawable(getDrawable(R.drawable.zero));
    }


    /**
     * Scroll down board at the beginning of the game
     */
    private void scrollAtBeginning(){
        final ScrollView sv = findViewById(R.id.verScroll);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                sv.fullScroll(View.FOCUS_DOWN);
            }
        }, 5);
    }

    /**
     * Set disc image for falling animation to board
     * @param gridLayout game board that show on screen
     */
    private  void setFallingDiscsButton(GridLayout gridLayout){
        for (int i = 0; i < size && gameVersion.equals("normal"); ++i) {
            fallingDiscs[i] =  new ImageButton(Game.this);
            GridLayout.LayoutParams param2 = new GridLayout.LayoutParams();
            param2.height = 216;
            param2.width = 216;
            fallingDiscs[i].setLayoutParams(param2);
            gridLayout.addView(fallingDiscs[i]);
            fallingDiscs[i].setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }
    }

    /**
     * Get computer's last played column
     * @return computer's last played column number
     */
    private int getComputerLastMove(){
        computerMove();
        if (oldMoves.size() % 2 == 0)
            return oldMoves.get(oldMoves.size() - 1);
        else
            return oldMoves.get(oldMoves.size() - 2);
    }

    /**
     * Play PvC game with drop animation
     * @param col column that will be played
     */
    private void dropPvC(final int col){

        final TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 216 * (lastAvailableRow(col) + 1));
        anim.setDuration(850);
        anim.setFillAfter(false);
        if(player % 2 == 1)
            fallingDiscs[col].setImageDrawable(resize(getDrawable(R.drawable.user1),216));
        else
            fallingDiscs[col].setImageDrawable(resize(getDrawable(R.drawable.user2),216));
        fallingDiscs[col].startAnimation(anim);

        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                handleButtons(false);//Disable buttons during the animation
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                fallingDiscs[col].setVisibility(View.INVISIBLE);
                handleButtons(true);//Enable buttons after the animation end
                updateBoard();
                if(checkAll() || isBoardFull()) { //Checks after users played
                    endGame();
                    return;
                }
                ++player;  //Changes the player
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });


    }

    /**
     * /**Finds row that which has empty cell on selected column
     * @param colNum column that will be played
     * @return row number that available for moving
     */
    private int lastAvailableRow(int colNum){
        for(int i=size - 1;i>=0;--i){
            for(int j=size - 1;j>=0;--j){
                if(j == colNum && gameCells[i][j] == 0 ){
                    return i+1;
                }
            }
        }
        return 0;
    }

    /**
     * Resize images
     * @param image image that will resized
     * @param scaleSize amount of scale
     * @return image which is resized
     */
    private Drawable resize(Drawable image, int scaleSize) {
        Bitmap b = ((BitmapDrawable) image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, scaleSize, scaleSize, false);
        return new BitmapDrawable(getResources(), bitmapResized);
    }

    /**
     * Sets discs to empty cell
     * @param x row of cell
     * @param y column of cell
     * @param scaleSize amount of scale
     * @param imageID
     */
    private void setButtonImage(int x, int y, int scaleSize, int imageID) {
        cellButtons[x][y].setImageDrawable(resize(getDrawable(imageID), scaleSize));
    }

    /**
     * Set border of time panel
     * @param color time panel's background color
     * @return  time panel's shape
     */
    private GradientDrawable setBorder(int color){

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(50);
        gd.setStroke(10, Color.BLUE);

        return gd;
    }

    /**
     * Enabled and disabled buttons on screen
     * @param status is true or false for enabled and disabled
     */
    private void handleButtons(boolean status){
        for(int i=0;i<size;++i) {
            if(gameVersion.equals("normal"))
                fallingDiscs[i].setEnabled(status);
            for (int j = 0; j < size; ++j)
                cellButtons[i][j].setEnabled(status);
        }
        undoButton.setEnabled(status);
    }

    /**
     * Takes data from MenuActivity
     */
    private void takeData(){
        Bundle extras = getIntent().getExtras();
        gameVersion = extras.getString("gameVersion");
        gameMode = extras.getString("gameMode");
        size = Integer.parseInt(extras.getString("boardSize"));
        time = Integer.parseInt(extras.getString("timeVal"));
    }

    /**
     * Passes to menu from game screen
     */
    private void backToMenu(){
        Toast.makeText(getApplicationContext(),"RETURNİNG TO MENU...",Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(Game.this, MenuActivity.class);
        if(gameVersion.equals("timed"))
            moveTimer.cancel();
        if(tiktakSound && gameVersion.equals("timed"))
            sound.stop();
        AudioManager appSound=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        appSound.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        Game.this.startActivity(myIntent);
        Game.this.finish();
    }

    /**
     * Creates a dialog which pops up end of game
     * @param message indicate that game end with draw,win or lose
     */
    private void endDialog(String message){

        final Dialog d = new Dialog(this);
        d.setCancelable(false);
        d.setContentView( R.layout.custom);
        d.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ImageView iv = setEndStatus(message,d);

        Button back = d.findViewById(R.id.back);
        back.setBackgroundResource(R.drawable.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                sound.stop();
                backToMenu();
            }
        });
        Button retry = d.findViewById(R.id.retry);
        retry.setBackgroundResource(R.drawable.retry);
        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d.dismiss();
                if (gameVersion.equals("timed"))
                    moveTimer.cancel();
                sound.stop();
                startNewGame();
            }
        });

        if(p1Score == 3 || p2Score == 3) {
            back.setVisibility(View.INVISIBLE);
            retry.setVisibility(View.INVISIBLE);

            if(p2Score == 3 && gameMode.equals("PvC")) {
                sound = MediaPlayer.create(Game.this, R.raw.lose);
                iv.setImageResource(R.drawable.lose);
            }
            else{
                sound = MediaPlayer.create(Game.this, R.raw.win);
                iv.setImageResource(R.drawable.winner);
            }
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backToMenu();

                }
            }, 3500);
        }

        if(soundOn)
            sound.start();
        d.show();
    }

    /**
     * Sets sounds and image to end dialog according to end status
     * @param message indicate that game end with draw,win or lose
     * @param d dialog which pops up end of game
     * @return end image
     */
    private ImageView setEndStatus(String message,Dialog d){
        ImageView iv =  d.findViewById(R.id.imageView1);

        if(message.equals("DRAW")) {
            sound= MediaPlayer.create(Game.this,R.raw.tada);
            iv.setImageResource(R.drawable.draw);
        }
        else if(message.equals("P1")) {
            sound= MediaPlayer.create(Game.this,R.raw.applause);
            iv.setImageResource(R.drawable.p1win2);
        }
        else if(message.equals("P2")) {
            sound= MediaPlayer.create(Game.this,R.raw.applause);
            iv.setImageResource(R.drawable.p2win2);
        }
        else if(message.equals("WİN")) {
            sound= MediaPlayer.create(Game.this,R.raw.applause);
            iv.setImageResource(R.drawable.youwin);
        }
        else {
            sound= MediaPlayer.create(Game.this,R.raw.lose);
            iv.setImageResource(R.drawable.youlose);
        }

        return iv;
    }

    /**
     * Checks coordinates to prevent reach out of board
     * @param coordX x position
     * @param coordY y position
     * @return true if coordinate is legal otherwise return false
     */
    private boolean isLegalPos(int coordX,int coordY){
        return coordX >=0 && coordX < size && coordY >= 0 && coordY<size;
    }

    /**
     * Checks whether the selected column is full
     * @param colNum Column that player moved
     * @return true if column is full otherwise return false
     */
    private boolean isColumnFull(int colNum){
        for(int i=0;i<size;++i)
            if(gameCells[i][colNum] == 0)
                return false;
        return true;
    }

    /**
     * Checks board is full or not
     * @return true if board  is full otherwise return false
     */
    private boolean isBoardFull(){
        for(int i=0;i<size;++i)
            for(int j=0;j<size;++j)
                if(gameCells[i][j] == 0)
                    return false;
        return true;
    }

    /**
     * Creates a board with empty cells and set oldMoves for undo
     */
    private  void initBoard(){

        oldMoves = new Vector<>();
        gameCells =  new int[size][size];
        for(int i=0;i<size;i++)
            for(int j=0;j<size;j++)
                gameCells[i][j] = 0;
    }

    /**
     * Clean the board and starts a new game after user taps retry button
     */
    private  void startNewGame(){
        for (int row = 0; row < size; row++)
            for (int column = 0; column < size; column++)
                cellButtons[row][column].setImageResource(R.drawable.empty2);

        handleButtons(true);//Enabled all buttons after game started
        initBoard();
        player = 1;//Set sequence
        isFirstMove = true;
        if(gameVersion.equals("timed"))//Restart countdown
            startCountDown();

    }

    /**
     * Makes moves of user1 or user2
     * @param colNum Column that player moved
     */
    private void playerMove(int colNum) {

        for(int i=size - 1;i>=0;--i){
            for(int j=size - 1;j>=0;--j){
                if(j == colNum && gameCells[i][j] == 0 ){// Puts red  Or black disc according to current player
                    if(player % 2 == 1 || gameMode.equals("PvC") ){
                        gameCells[i][j] = 1;
                        oldMoves.addElement(colNum);
                        return;
                    }
                    else if(player % 2 == 0 && gameMode.equals("PvP")){
                        gameCells[i][j] = 2;
                        oldMoves.addElement(colNum);
                        return;
                    }
                }
            }
        }
    }

    /**Makes computer's move*/
    private void computerMove(){
        if(isThreeVertical() || isThree()); //First priority attack
        else if(defend3Vertical() || defend3Horizon() || defend3DiaLeft() || defend3DiaRight());//Second priority defence
        else if(defend2Horizon() || isTwoVertical()  || isTwo());
        else if(isOneVertical() || isOne());
        else
            defaultOne();
    }

    /**
     * Undo last move from board
     */
    private  void undoMove(){
        for(int i=0;i<size;++i){
            for(int j=0;j<size;++j){
                //Undo the last move of user or computer according to last played column
                if( oldMoves.size() > 0 && j == (oldMoves.get(oldMoves.size() - 1)) && gameCells[i][j] != 0 ){
                    gameCells[i][j] = 0; //Makes empty the cell
                    cellButtons[i][j].setImageResource(R.drawable.empty2);
                    oldMoves.removeElementAt(oldMoves.size() - 1);
                    return;
                }
            }
        }
    }

    /**
     * Plays game in PvP mode with drop animation
     * @param col Column that user's played
     */
    private void playPvP(final int col){
        if(!isColumnFull(col)){
            final TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 216 * lastAvailableRow(col));
            anim.setDuration(850);
            anim.setFillAfter(false);
            if(player % 2 == 1)//Sets disc images according to the sequence
                fallingDiscs[col].setImageDrawable(resize(getDrawable(R.drawable.user1),216));
            else
                fallingDiscs[col].setImageDrawable(resize(getDrawable(R.drawable.user2),216));
            fallingDiscs[col].startAnimation(anim);

            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    handleButtons(false);//Disable buttons during the animation
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    fallingDiscs[col].setVisibility(View.INVISIBLE);
                    handleButtons(true);//Enable buttons after animation finishes
                    playerMove(col);
                    updateBoard();
                    if(checkAll() || isBoardFull()) { //Checks after users played
                        endGame();
                        return;
                    }
                    ++player;  //Changes the player
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }

    /**
     * Plays game in PvC mode with drop animation
     * @param col Column that user's played
     */
    private void playPvC(final int col){

            if(!isColumnFull(col)){
                final TranslateAnimation anim = new TranslateAnimation(0, 0, 0, 216 * lastAvailableRow(col));
                anim.setDuration(850);
                anim.setFillAfter(false);
                if(player % 2 == 1)//Sets disc images according to the sequence
                    fallingDiscs[col].setImageDrawable(resize(getDrawable(R.drawable.user1),216));
                else
                    fallingDiscs[col].setImageDrawable(resize(getDrawable(R.drawable.user2),216));
                fallingDiscs[col].startAnimation(anim);

                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        //Disable buttons during the animation
                        handleButtons(false);
                    }
                    @Override
                    public void onAnimationEnd(Animation animation) {
                        fallingDiscs[col].setVisibility(View.INVISIBLE);
                        handleButtons(true);//Enable buttons after animation finishes
                        playerMove(col);
                        updateBoard();
                        if(checkAll() || isBoardFull()) { //Checks after users played
                            endGame();
                            return;
                        }
                        ++player;  //Changes the player

                        if(gameMode.equals("PvC"))
                            dropPvC(getComputerLastMove());
                    }
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
            }
    }

    /**
     * Plays game in PvP mode and timed version
     * @param colNum Column that user's played
     */
    private boolean playTimedPvP(int colNum){

        if (!isColumnFull(colNum)) {
            playerMove(colNum);
            updateBoard();
            if (soundOn)
                sound.stop();
            moveTimer.cancel();
            startCountDown();
            if (checkAll() || isBoardFull()) { //Checks after users played
                moveTimer.cancel();
                endGame();
                return true;
            }
            ++player;  //Changes the player
        }
        return false;
    }

    /**
     * Plays game in PvC mode and timed version
     * @param colNum Column that user's played
     */
    private void playTimedPvC(int colNum){
        if(!isColumnFull(colNum)) {
            if(playTimedPvP(colNum))
                return;
            computerMove();
            updateBoard();
            if (checkAll() || isBoardFull()) {//Checks after user played
                moveTimer.cancel();
                endGame();
                return;
            }
            player++; //Changes the player
        }
    }

    /**
     * Genarate random column number for random move
     * @return random column number
     */
    private int randColumn(){
        rnd = new Random();
        return rnd.nextInt(size);
    }

    /**
     * Makes random move on board
     * @param randNum random column number
     */
    private void randomMove(int randNum)
    {
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(gameCells[i][j] == 0 && j == randNum && player % 2 == 1){//Random move for player 1
                    gameCells[i][j] = 1;
                    oldMoves.addElement(j);
                    return;
                }
                else if(gameCells[i][j] == 0 && j == randNum && player % 2 == 0){//Random move for player 2
                    gameCells[i][j] = 2;
                    oldMoves.addElement(j);
                    return;
                }
            }
        }
        randomMove(randColumn());//if moved column is full restart random move
    }

    /**
     * Starts countdown timer
     */
    private void startCountDown() {

        sound = MediaPlayer.create(Game.this,R.raw.tiktak);
        moveTimer = new CountDownTimer((time) * 1000, 50) {
            public void onTick(long millisUntilFinished) {
                NumberFormat f = new DecimalFormat("00");
                long min = (millisUntilFinished / 60000) % 60;
                long sec = (millisUntilFinished / 1000) % 60;
                long millis = (millisUntilFinished % 1000) % 100;
                if(min == 0 && sec <= 5) {//if second is less than 5 turn color to red and starts sound
                    timePanel.setBackground(setBorder(Color.RED));
                    if(soundOn) {//Checks game sound
                        sound.start();
                        tiktakSound = true;
                    }
                }
                else////if second is more than 5 turn color to green
                    timePanel.setBackground(setBorder(Color.GREEN));

                timePanel.setText("\t"+f.format(min) + ":" + f.format(sec)+  ":" + f.format(millis));
            }

            public void onFinish(){
                playAuto();
            }
        }.start();

    }

    /**
     * Plays automatically if no one makes move
     */
    private void playAuto(){
        if(soundOn)
            sound.stop();
        randomMove(randColumn());
        if(gameMode.equals("PvC")) {
            if(checkAll() || isBoardFull() ) { //Checks after users played
                endGame();
                return;
            }
            computerMove();
            ++player;
        }
        updateBoard();
        if(checkAll() || isBoardFull()) { //Checks after users played
            endGame();
            return;
        }
        ++player;  //Changes the player
        startCountDown();
    }
    /**Updates board on screen after users or computer played*/
    private void updateBoard() {
        for (int row = 0; row < size; row++) {
            for (int column = 0; column < size; column++) {
                if (gameCells[row][column] == 1) { //Put red disc to cell
                    setButtonImage(row,column,226,R.drawable.user1);
                }
                if (gameCells[row][column] == 2) { //Put black disc to cell
                    setButtonImage(row,column,226,R.drawable.user2);
                }

            }
        }
    }

    /**
     * Checks whether the four same cells are neighbor horizontally
     * @return if finds same four neighbor cells, returns true
     */
    private boolean checkHorizon()
    {
        int count = 0;

        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if((gameCells[i][j] == 1 && player % 2 == 1) ||
                        (gameCells[i][j] == 2 && player % 2 ==  0))
                    ++count; // Counts same cells horizontally
                else
                    count = 0;
                if(count == 4){	// if there are four same neighbor cells
                    for(int a=0;a<4;a++){ // Converts them star disc
                        if(player % 2 == 1)
                            setButtonImage(i,j-a,226,R.drawable.blackstar);
                        else
                            setButtonImage(i,j-a,226,R.drawable.redstar);
                    }
                    return true;
                }
            }
            count = 0;
        }
        return false;
    }


    /**
     * Checks whether the four same cells are neighbor vertically
     * @return if finds same four neighbor cells, returns true
     */
    private boolean checkVertical()
    {
        int count = 0;

        for(int i=size - 1;i>=0;--i){
            for(int j=size-1;j>=0;--j){
                if(isLegalPos(j,i)){
                    if((gameCells[j][i] == 1 && player % 2 == 1) ||
                            (gameCells[j][i] == 2 && player % 2 == 0))
                        ++count; // Counts same cells vertically
                    else
                        count = 0;
                    if(count == 4){ // if there are four same neighbor cells
                        for(int a=0;a<4;a++){ // Converts them star disc
                            if(player % 2 == 1)
                                setButtonImage(j+a,i,226,R.drawable.blackstar);
                            else
                                setButtonImage(j+a,i,226,R.drawable.redstar);
                        }
                        return true;
                    }
                }
            }
            count = 0;
        }
        return false;
    }

    /**
     * Checks whether the four same cells are neighbor in right cross
     * @return if finds same four neighbor cells, returns true
     */
    private boolean checkDiaRight()
    {
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(isLegalPos(i-1,j+1) && isLegalPos(i-2,j+2) && isLegalPos(i-3,j+3)){
                    if( (gameCells[i][j] == 1 && gameCells[i-1][j+1] == 1//Four same neighbor player 1
                            && gameCells[i-2][j+2] == 1 && gameCells[i-3][j+3] == 1) ||

                            (gameCells[i][j] == 2 && gameCells[i-1][j+1] == 2//Four same neighbor player 2
                                    && gameCells[i-2][j+2] == 2 && gameCells[i-3][j+3] == 2)){
                        for(int a=0;a<4;a++){ // Converts them star disc
                            if(player % 2 == 1)
                                setButtonImage(i-a,j+a,226,R.drawable.blackstar);
                            else
                                setButtonImage(i-a,j+a,226,R.drawable.redstar);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks whether the four same cells are neighbor in left cross
     * @return if finds same four neighbor cells, returns true
     */
    private boolean checkDiaLeft()
    {
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(isLegalPos(i-1,j-1) && isLegalPos(i-2,j-2) && isLegalPos(i-3,j-3)){
                    if( (gameCells[i][j] == 1 && gameCells[i-1][j-1] == 1//Four same neighbor player 1
                            && gameCells[i-2][j-2] == 1 && gameCells[i-3][j-3] == 1) ||

                            (gameCells[i][j] == 2 && gameCells[i-1][j-1] == 2//Four same neighbor player 2
                                    && gameCells[i-2][j-2] == 2 && gameCells[i-3][j-3] == 2)){
                        for(int a=0;a<4;a++){ // Converts them star disc
                            if(player % 2 == 1)
                                setButtonImage(i-a,j-a,226,R.drawable.blackstar);
                            else
                                setButtonImage(i-a,j-a,226,R.drawable.redstar);
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks All position for end of the game
     * @return true if finds same four neighbor cells otherwise return false
     */
    private boolean checkAll(){
        return checkHorizon() || checkVertical() ||
                checkDiaLeft() || checkDiaRight();
    }

    /**
     * Checks end of the game is draw,win or lose and sets score
     */
    private void endGame() {
        String endMessage = "DRAW";
        //if game ended with no one win,it means draw
        if(checkAll()){
            if(player % 2 == 1 && gameMode.equals("PvP")) {
                endMessage = "P1";
                ++p1Score;
                scoreImageFirst.setImageDrawable(scoreNum[p1Score]);
            }
            else if(player % 2 == 1 && gameMode.equals("PvC")) {
                endMessage = "WİN";
                ++p1Score;
                scoreImageFirst.setImageDrawable(scoreNum[p1Score]);
            }
            else if(player % 2 == 0 && gameMode.equals("PvP")) {
                endMessage = "P2";
                ++p2Score;
                scoreImageSecond.setImageDrawable(scoreNum[p2Score]);
            }
            else if(player % 2 == 0 && gameMode.equals("PvC")){
                endMessage = "LOSE";
                ++p2Score;
                scoreImageSecond.setImageDrawable(scoreNum[p2Score]);
            }
        }
        handleButtons(false);//Disable all buttons in end of the game
        endDialog(endMessage);

    }

    /**
     * Set score number images to array
     */
    private void setScoreImages() {

        scoreNum = new Drawable[4];

        scoreNum[0] = getDrawable(R.drawable.zero);
        scoreNum[1] = getDrawable(R.drawable.one);
        scoreNum[2] = getDrawable(R.drawable.two);
        scoreNum[3] = getDrawable(R.drawable.three);
    }

    /**
     * Adds a neighbor cell to a single cell of the computer
     * @return true if finds convenient position
     */
    private boolean isOneVertical(){
        for(int i=0;i<size;++i){
            for(int j=size-1;j>=0;--j){
                if(isLegalPos(j-1,i) && isLegalPos(j,i)){
                    if(gameCells[j][i] == 2 && gameCells[j-1][i] == 0){
                        //Checks whether there is a single cell and adds cell vertically
                        gameCells[j-1][i] = 2;
                        oldMoves.addElement(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Adds a neighbor cell to two cells of the computer vertically
     * @return true if finds convenient position
     */
    private boolean isTwoVertical(){
        for(int i=0;i<size;++i){
            for(int j=size-1;j>=0;--j){
                if(isLegalPos(j-2,i) && isLegalPos(j,i)){
                    if(gameCells[j][i] == 2 && gameCells[j-1][i] == 2 && gameCells[j-2][i] == 0 ){
                        //Checks whether there are two neigbor cells and adds cell vertically
                        gameCells[j-2][i] = 2;
                        oldMoves.addElement(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Adds a neighbor cell to three cells of the computer vertically
     * @return true if finds convenient position
     */
    private boolean isThreeVertical(){
        for(int i=0;i<size;++i){
            for(int j=size-1;j>=0;--j){
                if(isLegalPos(j-3,i) && isLegalPos(j,i)){
                    if(gameCells[j][i] == 2 && gameCells[j-1][i] == 2  && gameCells[j-2][i] == 2 && gameCells[j-3][i] == 0 ){
                        //Checks whether there are three neigbor cell and adds cell vertically
                        gameCells[j-3][i] = 2;
                        oldMoves.addElement(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }


    /**
     * Blocks opponents three neighbor cells vertically
     * @return true if finds convenient position
     */
    private boolean defend3Vertical(){
        for(int i=0;i<size;++i){
            for(int j=size-1;j>=0;--j){
                if(isLegalPos(j-3,i) && isLegalPos(j,i)){
                    if(gameCells[j][i] == 1 && gameCells[j-1][i] == 1  && gameCells[j-2][i] == 1 && gameCells[j-3][i] == 0 ){
                        //Checks whether there are three neighbor opponent's cell and adds 1 vertically
                        gameCells[j-3][i] = 2;
                        oldMoves.addElement(i);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Moves any empty cells
     * @return true if finds convenient position
     */
    private boolean defaultOne()
    {
        for(int i=size - 1;i>=0;--i){ // if there arent any neighbor,moves empty column
            for(int j=0;j<size;++j){
                if(gameCells[i][j] == 0){
                    gameCells[i][j] = 2;
                    oldMoves.addElement(j);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Adds a neighbor cell to a single cell of the computer
     * @return true if finds convenient position
     */
    private boolean isOne()
    {
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(isLegalPos(i,j+1) && isLegalPos(i,j)){
                    if(gameCells[i][j] == 2 && gameCells[i][j+1] == 0
                            &&  i == size -1){
                        //Checks whether there is a single cell and adds cell in horizontal right side
                        gameCells[i][j+1] = 2;
                        oldMoves.addElement(j+1);
                        return true;
                    }
                }
                if(isLegalPos(i+1,j+1) && isLegalPos(i,j)){
                    if(gameCells[i][j] == 2 && gameCells[i][j+1] == 0 &&
                            gameCells[i+1][j+1] != 0){
                        //Checks whether there is a single cell and adds cell in horizontal right side
                        gameCells[i][j+1] = 2;
                        oldMoves.addElement(j+1);
                        return true;
                    }
                }
                if(isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j] == 2 && gameCells[i][j-1] == 0 && i == size - 1 ){
                        //Checks whether there is a single cell and adds cell in horizontal left side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
                if(isLegalPos(i+1,j)&& isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j] == 2 && gameCells[i][j-1] == 0 &&
                            gameCells[i+1][j-1] != 0){
                        //Checks whether there is a single cell and adds cell in horizontal left side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Adds a neighbor cell to two cells of the computer
     * @return true if finds convenient position
     */
    private boolean isTwo()
    {
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(isLegalPos(i,j+2) && isLegalPos(i,j)){
                    if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 &&
                            gameCells[i][j+2] == 0 && i == size -1 ){
                        //Checks whether there are two neighbor cells and adds cell in horizontal right side
                        gameCells[i][j+2] = 2;
                        oldMoves.addElement(j+2);
                        return true;
                    }
                }
                if(isLegalPos(i+1,j+2) && isLegalPos(i,j)){
                    if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 &&
                            gameCells[i][j+2] == 0 && gameCells[i+1][j+2] != 0){
                        //Checks whether there are two neighbor cells and adds cell in horizontal right side
                        gameCells[i][j+2] = 2;
                        oldMoves.addElement(j+2);
                        return true;
                    }
                }
                if(isLegalPos(i,j+1) && isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 && gameCells[i][j-1] == 0 &&
                            i == size - 1 ){
                        //Checks whether there are two neighbor cells and adds cell in horizontal left side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
                if(isLegalPos(i+1,j+1) && isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 && gameCells[i][j-1] == 0 &&
                            gameCells[i+1][j-1] != 0){
                        //Checks whether there are two neighbor cells and adds cell in horizontal left side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
                if(isLegalPos(i-2,j+2) && isLegalPos(i,j)){
                    if(gameCells[i][j] == 2 && gameCells[i-1][j+1] == 2 &&
                            gameCells[i-2][j+2] == 0 && gameCells[i-1][j+2] != 0 ){
                        gameCells[i-2][j+2] = 2;
                        //Checks whether there are two neighbor cells and adds cell in right cross side
                        oldMoves.addElement(j+2);
                        return true;
                    }
                }
                if(isLegalPos(i-2,j-2) && isLegalPos(i,j)){
                    if(gameCells[i][j] == 2 && gameCells[i-1][j-1] == 2 &&
                            gameCells[i-2][j-2] == 0 && gameCells[i-1][j-2] != 0){
                        //Checks whether there are two neighbor cells and adds cell in left cross side
                        gameCells[i-2][j-2] = 2;
                        oldMoves.addElement(j-2);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Makes computer's move at the beginning of the game
     */
    private void computerFirstMove(){
        isFirstMove = false;
        if(gameCells[size - 1][size/2]== 0) {
            gameCells[size - 1][size / 2] = 2;
            oldMoves.addElement(size/2);
        }
        else {
            gameCells[size - 1][(size / 2) - 1] = 2;
            oldMoves.addElement(size/2 - 1);
        }
    }

    /**
     * Adds a neighbor cell to three cells of the computer
     * @return true if finds convenient position
     */
    private boolean isThree()
    {
        if(isFirstMove){//Computer first move
            computerFirstMove();
            return true;
        }
        else{
            for(int i=size - 1;i>=0;--i){
                for(int j=0;j<size;++j){
                    if(isLegalPos(i,j+3) && isLegalPos(i,j)){
                        if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 &&
                                gameCells[i][j+2] == 2 && gameCells[i][j+3] == 0 && i == size -1 ){
                            //Checks whether there are three neighbor cells and adds cell in horizontal right side
                            gameCells[i][j+3] = 2;
                            oldMoves.addElement(j+3);
                            return true;
                        }
                    }
                    if(isLegalPos(i+1,j+3) && isLegalPos(i,j)){
                        if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 &&
                                gameCells[i][j+2] == 2 && gameCells[i][j+3] == 0 && gameCells[i+1][j+3] != 0){
                            //Checks whether there are three neighbor cells and adds cell in horizontal right side
                            gameCells[i][j+3] = 2;
                            oldMoves.addElement(j+3);
                            return true;
                        }
                    }
                    if(isLegalPos(i,j+2) && isLegalPos(i,j) && isLegalPos(i,j-1)){
                        if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 &&
                                gameCells[i][j+2] == 2 && gameCells[i][j-1] == 0 &&
                                i == size - 1 ){
                            //Checks whether there are three neighbor cells and adds cell in horizontal left side
                            gameCells[i][j-1] = 2;
                            oldMoves.addElement(j-1);
                            return true;
                        }
                    }
                    if(isLegalPos(i+1,j+2) && isLegalPos(i,j) && isLegalPos(i,j-1)){
                        if(gameCells[i][j] == 2 && gameCells[i][j+1] == 2 && gameCells[i][j+2] == 2 &&
                                gameCells[i][j-1] == 0 && gameCells[i+1][j-1] != 0){
                            //Checks whether there are three neighbor cells and adds cell in horizontal left side
                            gameCells[i][j-1] = 2;
                            oldMoves.addElement(j-1);
                            return true;
                        }
                    }
                    if(isLegalPos(i-3,j+3) && isLegalPos(i,j)){
                        if(gameCells[i][j] == 2 && gameCells[i-1][j+1]== 2 && gameCells[i-2][j+2]== 2 &&
                                gameCells[i-3][j+3]== 0 && gameCells[i-2][j+3]!= 0 && isLegalPos(i-3,j+3)){
                            //Checks whether there are three neighbor cells and adds cell in right cross side
                            gameCells[i-3][j+3] = 2;
                            oldMoves.addElement(j+3);
                            return true;
                        }
                    }
                    if(isLegalPos(i-3,j-3) && isLegalPos(i,j)){
                        if(gameCells[i][j] == 2 && gameCells[i-1][j-1]== 2 && gameCells[i-2][j-2]== 2 &&
                                gameCells[i-3][j-3]== 0 && gameCells[i-2][j-3]!= 0 && isLegalPos(i-3,j-3)){
                            //Checks whether there are three neighbor cells and adds cell in left cross side
                            gameCells[i-3][j-3] = 2;
                            oldMoves.addElement(j-3);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    /**
     * Blocks opponents two neighbor cells horizontally
     * @return true if finds convenient position
     */
    private boolean defend2Horizon()
    {
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(isLegalPos(i,j+2) && isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j-1] == 0 && gameCells[i][j] == 1 && gameCells[i][j+1] == 1 &&
                            gameCells[i][j+2] == 0 &&  i == size -1 ){
                        //Checks whether there are two neighbor cells and adds cell in horizontal right side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    /**
     * Blocks opponents three neighbor cells horizontally
     * @return true if finds convenient position
     */
    private boolean defend3Horizon()
    {
        //Like isThree function,checks whether there are three opponent's neighbor cells and adds computer's cell around cell
        for(int i=size - 1;i>=0;--i){
            for(int j=0;j<size;++j){
                if(isLegalPos(i,j+3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i][j+1] == 1 &&
                            gameCells[i][j+2] == 1 && gameCells[i][j+3] == 0 && i == size -1 ){
                        //Checks whether there is a single cell and adds cell in horizontal right side
                        gameCells[i][j+3] = 2;
                        oldMoves.addElement(j+3);
                        return true;
                    }
                }
                if(isLegalPos(i+1,j+3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i][j+1] == 1 && gameCells[i][j+2] == 1
                            && gameCells[i][j+3] == 0 && gameCells[i+1][j+3] != 0){
                        //Checks whether there is a single cell and adds cell in horizontal right side
                        gameCells[i][j+3] = 2;
                        oldMoves.addElement(j+3);
                        return true;
                    }
                }
                if(isLegalPos(i,j+2)&& isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j] == 1 && gameCells[i][j+1] == 1 && gameCells[i][j+2] == 1
                            && gameCells[i][j-1] == 0 && i == size - 1 ){
                        //Checks whether there is a single cell and adds cell in horizontal left side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
                if(isLegalPos(i+1,j+2)&& isLegalPos(i,j) && isLegalPos(i,j-1)){
                    if(gameCells[i][j] == 1 && gameCells[i][j+1] == 1 && gameCells[i][j+2] == 1
                            && gameCells[i][j-1] == 0 && gameCells[i+1][j-1] != 0){
                        //Checks whether there is a single cell and adds cell in horizontal left side
                        gameCells[i][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
                if(isLegalPos(i,j+3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i][j+1] == 1 && gameCells[i][j+2] == 0
                            && gameCells[i][j+3] == 1 && i == size - 1){
                        //Checks whether there is a single cell and adds cell in horizontal left side
                        gameCells[i][j+2] = 2;
                        oldMoves.addElement(j+2);
                        return true;
                    }
                }
                if(isLegalPos(i,j+3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i][j+1] == 0 && gameCells[i][j+2] == 1
                            && gameCells[i][j+3] == 1 && i == size - 1){
                        //Checks whether there is a single cell and adds cell in horizontal left side
                        gameCells[i][j+1] = 2;
                        oldMoves.addElement(j+1);
                        return true;
                    }
                }

            }
        }
        return false;
    }

    /**
     * Blocks opponents three neighbor cells in right cross
     * @return true if finds convenient position
     */
    private boolean defend3DiaRight() {

        for (int i = size - 1; i >= 0; --i) {
            for (int j = 0; j < size; ++j) {
                if (isLegalPos(i - 3, j + 3) && isLegalPos(i, j)) {
                    if (gameCells[i][j] == 1 && gameCells[i - 1][j + 1] == 1 && gameCells[i - 2][j + 2] == 1
                            && gameCells[i - 3][j + 3] == 0 && gameCells[i - 2][j + 3] != 0) {
                        //Checks whether there are three neighbor cells and adds cell in right cross side
                        gameCells[i - 3][j + 3] = 2;
                        oldMoves.addElement(j + 3);
                        return true;
                    }
                }
                if (isLegalPos(i - 3, j + 3) && isLegalPos(i, j)) {
                    if (gameCells[i][j] == 0 && gameCells[i - 1][j + 1] == 1 && gameCells[i - 2][j + 2] == 1
                            && gameCells[i - 3][j + 3] == 1 && i == size - 1) {
                        //Checks whether there are three neighbor cells and adds cell in right cross side
                        gameCells[i][j] = 2;
                        oldMoves.addElement(j);
                        return true;
                    }
                }
                if (isLegalPos(i - 3, j + 3) && isLegalPos(i, j)) {
                    if (gameCells[i][j] == 1 && gameCells[i - 1][j + 1] == 1 && gameCells[i - 2][j + 2] == 0
                            && gameCells[i - 3][j + 3] == 1 && gameCells[i - 1][j + 2] != 0) {
                        //Checks whether there are three neighbor cells and adds cell in left cross side
                        gameCells[i - 2][j + 2] = 2;
                        oldMoves.addElement(j + 2);
                        return true;
                    }
                }
                if (isLegalPos(i - 3, j + 3) && isLegalPos(i, j)) {
                    if (gameCells[i][j] == 1 && gameCells[i - 1][j + 1] == 0 && gameCells[i - 2][j + 2] == 1
                            && gameCells[i - 3][j + 3] == 1 && gameCells[i][j + 1] != 0) {
                        //Checks whether there are three neighbor cells and adds cell in left cross side
                        gameCells[i - 1][j + 1] = 2;
                        oldMoves.addElement(j + 1);
                        return true;
                    }
                }
            }
        }
        return  false;
    }

    /**
     * Blocks opponents three neighbor cells in left cross
     * @return true if finds convenient position
     */
    private boolean defend3DiaLeft(){
        for(int i=size - 1;i>=0;--i) {
            for (int j = 0; j < size; ++j) {
                if(isLegalPos(i-3,j-3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i-1][j-1]== 1 && gameCells[i-2][j-2]== 1
                            && gameCells[i-3][j-3]== 0 && gameCells[i-2][j-3]!= 0){
                        //Checks whether there are three neighbor cells and adds cell in left cross side
                        gameCells[i-3][j-3] = 2;
                        oldMoves.addElement(j-3);
                        return true;
                    }
                }
                if(isLegalPos(i-3,j-3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 0 && gameCells[i-1][j-1]== 1 && gameCells[i-2][j-2]== 1
                            && gameCells[i-3][j-3]== 1 && i == size - 1){
                        //Checks whether there are three neighbor cells and adds cell in left cross side
                        gameCells[i][j] = 2;
                        oldMoves.addElement(j);
                        return true;
                    }
                }

                if(isLegalPos(i-3,j-3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i-1][j-1]== 1 && gameCells[i-2][j-2]== 0
                            && gameCells[i-3][j-3]== 1 && gameCells[i-1][j-2] != 0){
                        //Checks whether there are three neighbor cells and adds cell in left cross side
                        gameCells[i-2][j-2] = 2;
                        oldMoves.addElement(j-2);
                        return true;
                    }
                }
                if(isLegalPos(i-3,j-3)&& isLegalPos(i,j)){
                    if(gameCells[i][j] == 1 && gameCells[i-1][j-1]== 0 && gameCells[i-2][j-2]== 1
                            && gameCells[i-3][j-3]== 1 && gameCells[i][j-1] != 0){
                        //Checks whether there are three neighbor cells and adds cell in left cross side
                        gameCells[i-1][j-1] = 2;
                        oldMoves.addElement(j-1);
                        return true;
                    }
                }
            }

        }
        return false;
    }
}

