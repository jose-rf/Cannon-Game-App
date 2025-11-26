package com.example.cannongame;

// Alvo colorido que se move verticalmente
public class Target extends GameElement {
    public Target(CannonView view, int color, int soundId, int x, int y,
                  int width, int height, float velocityY) {
        super(view, color, soundId, x, y, width, height, velocityY);
    }
}