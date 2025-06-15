package model;

// Kelas ini merepresentasikan jenis ikan 'BigFish', yang berukuran besar dan bernilai tinggi.
// Merupakan turunan dari kelas Fish.
public class BigFish extends Fish { 

    // Konstruktor untuk membuat objek BigFish.
    public BigFish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // Panggil konstruktor parent (Fish) dengan nilai-nilai spesifik untuk BigFish.
        // renderW, renderH, score, spritePath, LtoR, speedMult, spriteW, spriteH, totalFrames, frameDelay, struggleFactor
        super(x, y, 54, 49, 25, spriteSheetPath, movesLeftToRight, 1.2f, // Kecepatan relatif normal.
              54, 49, 4, 180,
              1.5f); // Lebih sulit ditangkap (struggle factor lebih besar).
    }
}