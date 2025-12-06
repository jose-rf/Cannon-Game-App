package com.example.cannongame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback {

    // IDs dos sons
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;

    private CannonThread cannonThread;
    public Activity activity;
    private boolean dialogIsDisplayed = false;

    private Cannon cannon;
    private Blocker blocker;
    private final ArrayList<Target> targets = new ArrayList<>();

    private int screenWidth;
    private int screenHeight;

    private double timeLeft = 10.0;
    private int shotsFired = 0;
    private double totalElapsedTime = 0.0;

    private SoundPool soundPool;
    private final SparseIntArray soundMap;

    private final Paint textPaint = new Paint();
    private final Paint backgroundPaint = new Paint();

    public boolean nightMode = false; // Extra da Pessoa 1 (mude para true = fundo preto)

    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity) context;
        getHolder().addCallback(this);

        // Desde minSdk 24, sempre pode usar o Builder
        soundPool = new SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .build())
                .build();

        soundMap = new SparseIntArray();
        // Sons do sistema que existem em todos os emuladores
        soundMap.put(TARGET_SOUND_ID,  soundPool.load(context, R.raw.target_hit, 1));
        soundMap.put(CANNON_SOUND_ID,  soundPool.load(context, R.raw.cannon_fire, 1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit, 1));   // erro / colisão

        backgroundPaint.setColor(nightMode ? Color.BLACK : Color.WHITE);
        textPaint.setColor(nightMode ? Color.WHITE : Color.BLACK);
        textPaint.setTextSize(80);
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
    }

    public int getScreenWidth() { return screenWidth; }
    public int getScreenHeight() { return screenHeight; }

    public void playSound(int soundId) {
        soundPool.play(soundMap.get(soundId), 1f, 1f, 1, 0, 1f);
    }

    public void newGame() {
        cannon = new Cannon(this,
                (int)(screenHeight * 0.075),   // raio da base
                (int)(screenWidth * 0.1),      // comprimento do cano
                (int)(screenHeight * 0.075));  // espessura do cano

        targets.clear();
        Random random = new Random();
        int x = (int) (screenWidth * 0.6);
        int y = (int) (screenHeight * 0.35);

        for (int i = 0; i < 9; i++) {
            float velocityY = (float) (screenHeight * (0.75 + random.nextDouble() * 0.75));
            if (random.nextBoolean()) velocityY = -velocityY;
            int color = random.nextBoolean() ? Color.BLUE : Color.YELLOW;

            targets.add(new Target(this, color, TARGET_SOUND_ID, x, y,
                    (int)(screenWidth * 0.025), (int)(screenHeight * 0.15), velocityY));
            x += (int)(screenWidth * 0.06);
        }

        blocker = new Blocker(this, Color.BLACK, BLOCKER_SOUND_ID,
                (int)(screenWidth * 0.5), (int)(screenHeight * 0.375),
                (int)(screenWidth * 0.025), (int)(screenHeight * 0.25),
                (float)(screenHeight * 1.0));

        timeLeft = 10.0;
        shotsFired = 0;
        totalElapsedTime = 0.0;

        if (cannonThread != null) cannonThread.setRunning(false);
        cannonThread = new CannonThread(getHolder());
        cannonThread.setRunning(true);
        cannonThread.start();
    }

    private void updatePositions(double elapsedTimeMS) {
        double interval = elapsedTimeMS / 1000.0;

        if (cannon.getCannonball() != null)
            cannon.getCannonball().update(interval);

        blocker.update(interval);
        for (Target t : targets) t.update(interval);

        timeLeft -= interval;

        if (timeLeft <= 0.0 || targets.isEmpty()) {
            cannonThread.setRunning(false);
            int title = (targets.isEmpty()) ? R.string.win : R.string.lose;
            showGameOverDialog(title);
        }
    }

    private void testCollisions() {
        CannonBall ball = cannon.getCannonball();
        if (ball == null || !ball.isOnScreen()) {
            cannon.removeCannonball();
            return;
        }

        // Colisão com alvos
        for (int i = 0; i < targets.size(); i++) {
            if (ball.collidesWith(targets.get(i))) {
                targets.get(i).playSound();
                timeLeft += 3; // bônus de tempo
                cannon.removeCannonball();
                targets.remove(i);
                return;
            }
        }

        // Colisão com blocker
        if (ball.collidesWith(blocker)) {
            blocker.playSound();
            ball.reverseVelocityX();
            timeLeft -= 2; // penalidade
        }
    }

    public void fireCannonball(MotionEvent event) {
        Point touchPoint = new Point((int) event.getX(), (int) event.getY());
        double angle = Math.atan2(touchPoint.x, screenHeight / 2.0 - touchPoint.y);
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
                    .setPositiveButton(R.string.reset_game, (dialog, which) -> newGame())
                    .setCancelable(false)
                    .show();
            dialogIsDisplayed = true;
        });
    }

    public void drawGameElements(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), backgroundPaint);

        String timeText = getResources().getString(R.string.time_remaining_format, timeLeft);
        canvas.drawText(timeText, 50, 100, textPaint);

        cannon.draw(canvas);

        if (cannon.getCannonball() != null && cannon.getCannonball().isOnScreen())
            cannon.getCannonball().draw(canvas);

        blocker.draw(canvas);
        for (Target t : targets) t.draw(canvas);
    }

    // ==================== CALLBACKS CORRIGIDOS (funcionam em todas as versões) ====================

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        if (!dialogIsDisplayed) {
            newGame();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        activity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        if (cannonThread != null) {
            cannonThread.setRunning(false);
        }
        while (retry) {
            try {
                if (cannonThread != null) {
                    cannonThread.join();
                }
                retry = false;
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
            fireCannonball(event);
        }

        // Acessibilidade: remove o aviso amarelo
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        // Chama o super para acessibilidade (leitores de tela, etc)
        super.performClick();
        return true;
    }

    public void stopGame() {
        if (cannonThread != null) {
            cannonThread.setRunning(false);
        }
    }

    public void releaseResources() {
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    // ==================== THREAD DO JOGO ====================
    private class CannonThread extends Thread {
        private final SurfaceHolder surfaceHolder;
        private boolean running = true;

        public CannonThread(SurfaceHolder holder) {
            surfaceHolder = holder;
            setName("CannonThread");
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        @Override
        public void run() {
            Canvas canvas = null;
            long previousTime = System.currentTimeMillis();

            while (running) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                    synchronized (surfaceHolder) {
                        long currentTime = System.currentTimeMillis();
                        double elapsedTimeMS = currentTime - previousTime;
                        totalElapsedTime += elapsedTimeMS / 1000.0;

                        updatePositions(elapsedTimeMS);
                        testCollisions();
                        drawGameElements(canvas);

                        previousTime = currentTime;
                    }
                } finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
}