package com.example.cannongame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
public class Cannon {
     //variaveis cannon
    private final Paint pincel = new Paint(); //pincel
    private final Point canoFinal = new Point(); //ponta
    private final CannonView view; // a tela do jogo
    private final int baseRaio; //tamanho do raio da base
    private final int canoComprimento; //comprimento do cano
    private double canoAngulo; //angulo atual do cano em radianos

    //variaveis de posicao fixa
    private final float baseCenterX = 0f; //o canhao fica na borda esquerda (x=0)
    private final float baseCenterY; //o canhao fica no meio da altura (Y=screenHeight/2)


    
}
