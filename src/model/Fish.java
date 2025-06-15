package model;

import java.util.Random;
import viewmodel.Constants;

// Kelas ini adalah 'blueprint' dasar untuk semua jenis ikan di dalam game.
// Merupakan turunan dari GameObject dan akan diturunkan lagi oleh jenis ikan spesifik.
public class Fish extends GameObject {
    // Nilai skor yang didapat jika berhasil menangkap ikan ini.
    protected int scoreValue; 
    // Kecepatan gerak horizontal ikan.
    protected float speedX;
    // Faktor kesulitan saat mini-game struggle (semakin tinggi, semakin sulit).
    protected float struggleFactor;

    // Konstruktor utama yang paling detail untuk membuat objek ikan.
    public Fish(float x, float y, int renderWidth, int renderHeight, int scoreValue, 
                String spriteSheetPath, boolean movesLeftToRight, float baseSpeedMultiplier,
                int spriteFrameW, int spriteFrameH, int totalFrames, int frameDelay,
                float struggleDifficultyFactor) {
        // Panggil konstruktor parent (GameObject).
        super(x, y, renderWidth, renderHeight);
        // Inisialisasi properti spesifik ikan.
        this.scoreValue = scoreValue;
        this.struggleFactor = struggleDifficultyFactor;
        // Muat sprite sheet untuk animasi ikan.
        loadSpriteSheet(spriteSheetPath, spriteFrameW, spriteFrameH, totalFrames, frameDelay);

        // Atur kecepatan dan arah gerak secara acak berdasarkan multiplier.
        Random rand = new Random();
        float baseSpeed = (1.0f + rand.nextFloat() * 0.8f) * baseSpeedMultiplier; 
        
        this.speedX = movesLeftToRight ? baseSpeed : -baseSpeed;
        // Atur arah hadap sprite agar sesuai dengan arah gerakan.
        this.isFacingRight_anim = movesLeftToRight; 
    }

    // Konstruktor praktis untuk membuat ikan 'normal' (dipanggil oleh EntityHandler).
    public Fish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // Panggil konstruktor utama dengan nilai-nilai default untuk ikan biasa.
        this(x, y, 32, 32, 10, spriteSheetPath, movesLeftToRight, 1.5f, 
             32, 32, 4, 150, 
             1.0f); // Struggle factor normal untuk ikan ini.
    }

    // Metode update utama untuk ikan, dipanggil setiap frame.
    @Override
    public void update() {
        x += speedX; // Gerakkan ikan secara horizontal.
        updateAnimation(); // Update frame animasi.
        updateCollisionBox(); // Update posisi hitbox.
    }

    // Getter untuk mendapatkan nilai skor ikan ini.
    public int getScoreValue() { return scoreValue; }
    // Getter untuk mendapatkan faktor kesulitan struggle ikan ini.
    public float getStruggleFactor() { return struggleFactor; }

    // Metode untuk mengecek apakah ikan sudah keluar dari batas layar.
    public boolean isOutOfBounds() {
        // Kembalikan true jika posisi ikan sudah jauh di luar layar kiri atau kanan.
        if (speedX < 0 && x + width < -20) return true; 
        if (speedX > 0 && x > Constants.GAME_WIDTH + 20) return true;
        return false;
    }
}