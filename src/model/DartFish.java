package model;

public class DartFish extends Fish {

    public DartFish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // renderW, renderH, score, spritePath, LtoR, speedMult, spriteW, spriteH, totalFrames, frameDelay, struggleFactor
        super(x, y, 39, 20, 15, spriteSheetPath, movesLeftToRight, 2.8f, // Kecepatan tinggi
              39, 20, 4, 100,
              1.2f); // Struggle factor sedikit lebih besar dari normal
    }
}