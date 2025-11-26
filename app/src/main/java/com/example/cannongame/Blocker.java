package com.example.cannongame;

// Obstáculo preto que rebate o tiro
public class Blocker extends GameElement {
    public Blocker(CannonView view, int color, int soundId, int x, int y,
                   int width, int height, float velocityY) {
        super(view, color, soundId, x, y, width, height, velocityY);
    }
}