package com.example.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

// O canhão fixo na esquerda da tela
public class Cannon {
    private int baseRadius;
    private int barrelLength;
    private Point barrelEnd = new Point();
    private double barrelAngle;
    private CannonBall cannonball;
    private Paint paint = new Paint();
    private CannonView view;

    public Cannon(CannonView view, int baseRadius, int barrelLength, int barrelWidth) {
        this.view = view;
        this.baseRadius = baseRadius;
        this.barrelLength = barrelLength;
        paint.setStrokeWidth(barrelWidth);
        paint.setColor(Color.BLACK);
        align(Math.PI / 2); // começa apontando para cima
    }

    public void align(double angle) {
        barrelAngle = angle;
        barrelEnd.x = (int)(barrelLength * Math.sin(angle));
        barrelEnd.y = view.getScreenHeight() / 2 - (int)(barrelLength * Math.cos(angle));
    }

    public void fireCannonball() {
        if (cannonball == null || !cannonball.isOnScreen()) {
            int velocityX = (int)(500 * Math.sin(barrelAngle));
            int velocityY = (int)(-500 * Math.cos(barrelAngle));
            int radius = view.getScreenWidth() / 36;

            cannonball = new CannonBall(view, Color.BLACK,
                    CannonView.CANNON_SOUND_ID,
                    barrelEnd.x - radius, barrelEnd.y - radius,
                    radius, velocityX, velocityY);

            view.playSound(CannonView.CANNON_SOUND_ID);
        }
    }

    public void draw(Canvas canvas) {
        // cano
        canvas.drawLine(0, view.getScreenHeight() / 2,
                barrelEnd.x, barrelEnd.y, paint);
        // base
        canvas.drawCircle(0, view.getScreenHeight() / 2, baseRadius, paint);
    }

    public CannonBall getCannonball() { return cannonball; }
    public void removeCannonball() { cannonball = null; }
}