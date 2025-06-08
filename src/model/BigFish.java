package model;

public class BigFish extends Fish { 

    public BigFish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // renderW, renderH, score, spritePath, LtoR, speedMult, spriteW, spriteH, totalFrames, frameDelay, struggleFactor
        super(x, y, 54, 49, 25, spriteSheetPath, movesLeftToRight, 1.2f, // Kecepatan "normal"
              54, 49, 4, 180,
              1.5f); // Struggle factor lebih besar (lebih sulit)
    }
}