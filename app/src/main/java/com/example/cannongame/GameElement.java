package com.example.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

// Classe base para todos os objetos que aparecem na tela
public class GameElement {
    protected CannonView view;       // referência para a tela
    protected Paint paint = new Paint();
    protected Rect shape;            // posição e tamanho
    private float velocityY;         // velocidade vertical (usado por Target e Blocker)
    private int soundId;             // som ao ser atingido

    public GameElement(CannonView view, int color, int soundId, int x, int y, int width, int height, float velocityY) {
        this.view = view;
        this.paint.setColor(color);
        this.soundId = soundId;
        this.velocityY = velocityY;
        shape = new Rect(x, y, x + width, y + height);
    }

    // Atualiza a posição (chamado a cada frame)
    public void update(double interval) {
        shape.offset(0, (int)(velocityY * interval));

        // Rebate nas bordas superior e inferior
        if (shape.top < 0 || shape.bottom > view.getScreenHeight()) {
            velocityY *= -1;
            shape.offset(0, (int)(velocityY * interval)); // corrige posição
        }
    }

    // Desenha o elemento
    public void draw(Canvas canvas) {
        canvas.drawRect(shape, paint);
    }

    // Toca o som
    public void playSound() {
        view.playSound(soundId);
    }

    // Verifica colisão com outro elemento
    public boolean collidesWith(GameElement other) {
        return Rect.intersects(shape, other.shape);
    }
}