package com.example.cannongame;

import android.graphics.Canvas;

// O projétil (bola preta)
public class CannonBall extends GameElement {
    private float velocityX;
    private boolean onScreen = true;

    public CannonBall(CannonView view, int color, int soundId, int x, int y, int radius,
                      float velocityX, float velocityY) {
        super(view, color, soundId, x, y, 2 * radius, 2 * radius, velocityY);
        this.velocityX = velocityX;
    }

    @Override
    public void update(double interval) {
        super.update(interval);
        shape.offset((int)(velocityX * interval), 0);

        // Saiu da tela?
        if (shape.right < 0 || shape.left > view.getScreenWidth() ||
                shape.top < 0 || shape.bottom > view.getScreenHeight()) {
            onScreen = false;
        }
    }

    @Override
    public void draw(Canvas canvas) {
        float radius = shape.width() / 2f;
        canvas.drawCircle(shape.left + radius, shape.top + radius, radius, paint);
    }

    public boolean isOnScreen() { return onScreen; }
    public void reverseVelocityX() { velocityX *= -1; }
}