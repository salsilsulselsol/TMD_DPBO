package model;

// Kelas ini merepresentasikan jenis ikan 'DartFish', yang bergerak cepat.
// Merupakan turunan dari kelas Fish.
public class DartFish extends Fish {

    // Konstruktor untuk membuat objek DartFish.
    public DartFish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // Panggil konstruktor parent (Fish) dengan nilai-nilai spesifik untuk DartFish.
        // renderW, renderH, score, spritePath, LtoR, speedMult, spriteW, spriteH, totalFrames, frameDelay, struggleFactor
        super(x, y, 39, 20, 15, spriteSheetPath, movesLeftToRight, 2.8f, // Kecepatan tinggi.
              39, 20, 4, 100,
              1.2f); // Sedikit lebih sulit ditangkap (struggle factor > 1.0).
    }
}