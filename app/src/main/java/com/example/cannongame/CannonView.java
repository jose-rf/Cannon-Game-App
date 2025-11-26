package com.example.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;

    private CannonThread cannonThread;
    public Activity activity;
    private boolean dialogIsDisplayed = false;

    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets = new ArrayList<>();

    private int screenWidth, screenHeight;
    private double timeLeft = 10.0;
    private int shotsFired = 0;
    private double totalElapsedTime = 0.0;

    private SoundPool soundPool;
    private SparseIntArray soundMap;

    private Paint textPaint = new Paint();
    private Paint backgroundPaint = new Paint();
    public boolean nightMode = false; // Extra da Pessoa 1

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
        getHolder().addCallback(this);

        // SoundPool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().setMaxStreams(3)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME).build()).build();
        } else {
            soundPool = new SoundPool(3, 3, 0);
        }
        soundMap = new SparseIntArray();

        // Sons que funcionam em qualquer Android 6.0+ (emulador incluso), comentado por enquanto
        //soundMap.put(TARGET_SOUND_ID,  soundPool.load(context, android.R.raw.snd_default, 1));
        //soundMap.put(CANNON_SOUND_ID,  soundPool.load(context, android.R.raw.snd_default, 1));
        //soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, android.R.raw.snd_default, 1));

        backgroundPaint.setColor(nightMode ? Color.BLACK : Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setColor(nightMode ? Color.WHITE : Color.BLACK);
        textPaint.setAntiAlias(true);
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w; screenHeight = h;
    }

    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }
    public void playSound(int id) {
        //soundPool.play(soundMap.get(id), 1, 1, 1, 0, 1f); } comentado por enquanto ate arrumar o som
    }
    public void newGame() {
        cannon = new Cannon(this, screenHeight / 13, screenWidth / 10, screenHeight / 13);
        targets.clear();
        Random r = new Random();
        int x = (int)(screenWidth * 0.6);
        int y = screenHeight / 3;

        for (int i = 0; i < 9; i++) {
            float vel = (float)(screenHeight * (0.75 + r.nextDouble() * 0.75));
            if (r.nextBoolean()) vel = -vel;
            int color = r.nextBoolean() ? Color.BLUE : Color.YELLOW;
            targets.add(new Target(this, color, TARGET_SOUND_ID, x, y,
                    screenWidth / 40, screenHeight / 7, vel));
            x += screenWidth / 16;
        }

        blocker = new Blocker(this, Color.BLACK, BLOCKER_SOUND_ID,
                screenWidth / 2, screenHeight / 3,
                screenWidth / 40, screenHeight / 4,
                (float)screenHeight);

        timeLeft = 10.0; shotsFired = 0; totalElapsedTime = 0.0;

        if (cannonThread != null) cannonThread.setRunning(false);
        cannonThread = new CannonThread(getHolder());
        cannonThread.setRunning(true);
        cannonThread.start();
    }

    private void updatePositions(double elapsed) {
        double interval = elapsed / 1000.0;
        if (cannon.getCannonball() != null) cannon.getCannonball().update(interval);
        blocker.update(interval);
        for (Target t : targets) t.update(interval);
        timeLeft -= interval;

        if (timeLeft <= 0 || targets.isEmpty()) {
            cannonThread.setRunning(false);
            showGameOverDialog(timeLeft <= 0 ? R.string.lose : R.string.win);
        }
    }

    private void testCollisions() {
        CannonBall ball = cannon.getCannonball();
        if (ball == null || !ball.isOnScreen()) { cannon.removeCannonball(); return; }

        for (int i = 0; i < targets.size(); i++) {
            if (ball.collidesWith(targets.get(i))) {
                targets.get(i).playSound();
                timeLeft += 3;
                cannon.removeCannonball();
                targets.remove(i);
                return;
            }
        }
        if (ball.collidesWith(blocker)) {
            blocker.playSound();
            ball.reverseVelocityX();
            timeLeft -= 2;
        }
    }

    public void fireCannonball(MotionEvent e) {
        Point p = new Point((int)e.getX(), (int)e.getY());
        double angle = Math.atan2(p.x, screenHeight/2.0 - p.y);
        cannon.align(angle);
        if (cannon.getCannonball() == null || !cannon.getCannonball().isOnScreen()) {
            cannon.fireCannonball();
            shotsFired++;
        }
    }

    private void showGameOverDialog(int titleId) {
        activity.runOnUiThread(() -> {
            new AlertDialog.Builder(activity)
                    .setTitle(titleId)
                    .setMessage(getResources().getString(R.string.results_format, shotsFired, totalElapsedTime))
                    .setPositiveButton(R.string.reset_game, (d, w) -> newGame())
                    .setCancelable(false)
                    .show();
            dialogIsDisplayed = true;
        });
    }

    public void drawGameElements(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);
        canvas.drawText("Tempo: " + String.format("%.1f", timeLeft), 50, 100, textPaint);
        cannon.draw(canvas);
        if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen())
            cannon.getCannonball().draw(canvas);
        blocker.draw(canvas);
        for (Target t : targets) t.draw(canvas);
    }

    @Override public void surfaceCreated(SurfaceHolder h) { if (!dialogIsDisplayed) newGame(); }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        if (cannonThread != null) cannonThread.setRunning(false);
        while (retry) { try { cannonThread.join(); retry = false; } catch (Exception ignored) {} }
    }

    @Override public boolean onTouchEvent(MotionEvent e) {
        if (e.getAction() == MotionEvent.ACTION_DOWN || e.getAction() == MotionEvent.ACTION_MOVE)
            fireCannonball(e);
        return true;
    }

    public void stopGame() { if (cannonThread != null) cannonThread.setRunning(false); }
    public void releaseResources() { if (soundPool != null) soundPool.release(); }

    private class CannonThread extends Thread {
        private SurfaceHolder holder;
        private boolean running = true;

        public CannonThread(SurfaceHolder h) { holder = h; setName("CannonThread"); }
        public void setRunning(boolean r) { running = r; }

        @Override public void run() {
            Canvas canvas = null;
            long prev = System.currentTimeMillis();
            while (running) {
                try {
                    canvas = holder.lockCanvas(null);
                    synchronized (holder) {
                        long now = System.currentTimeMillis();
                        double elapsed = now - prev;
                        totalElapsedTime += elapsed / 1000.0;
                        updatePositions(elapsed);
                        testCollisions();
                        drawGameElements(canvas);
                        prev = now;
                    }
                } finally {
                    if (canvas != null) holder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}