package model;

import java.awt.Rectangle;
import java.util.Random;
import viewmodel.Constants;

// Kelas ini merepresentasikan objek musuh (Ghost) dalam permainan.
public class Ghost extends GameObject {
    // Kecepatan horizontal hantu.
    private float speedX;

    // Faktor skala untuk membuat hitbox Ghost lebih kecil dari gambar rendernya.
    private static final float HITBOX_SCALE_X = 0.6f; // Hitbox akan 60% dari lebar gambar.
    private static final float HITBOX_SCALE_Y = 0.8f; // Hitbox akan 80% dari tinggi gambar.

    // Konstruktor utama untuk membuat objek Ghost dengan semua detail.
    public Ghost(float x, float y, int renderWidth, int renderHeight, String spriteSheetPath, 
                 boolean movesLeftToRight, int spriteFrameW, int spriteFrameH, int totalFrames, int frameDelay) {
        // Panggil konstruktor parent (GameObject).
        super(x, y, renderWidth, renderHeight);
        // Muat sprite sheet animasi.
        loadSpriteSheet(spriteSheetPath, spriteFrameW, spriteFrameH, totalFrames, frameDelay);

        // Tentukan kecepatan dan arah gerak secara acak.
        Random rand = new Random();
        float baseSpeedX = 1.2f + rand.nextFloat() * 1.0f; 
        this.speedX = movesLeftToRight ? baseSpeedX : -baseSpeedX;
        
        // Atur arah hadap sprite sesuai arah gerak (default sprite menghadap kiri).
        this.isFacingRight_anim = !movesLeftToRight; 

        // Inisialisasi collision box yang sudah disesuaikan ukurannya saat objek dibuat.
        updateCollisionBox(); 
    }
    
    // Konstruktor praktis yang dipanggil oleh EntityHandler dengan nilai default.
    public Ghost(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // Panggil konstruktor utama dengan nilai-nilai yang sudah ditentukan.
        // Render size (47x66), sprite frame size (31x44), frame count (4), frame delay (200).
        this(x, y, 47, 66, spriteSheetPath, movesLeftToRight,
             31, 44, 4, 200); 
    }

    // Metode update utama untuk hantu, dipanggil setiap frame.
    @Override
    public void update() {
        x += speedX; // Gerakkan hantu secara horizontal.
        updateAnimation(); // Update frame animasi.
        updateCollisionBox(); // Pastikan posisi hitbox selalu mengikuti posisi hantu.
    }

    // Override metode updateCollisionBox untuk membuat hitbox Ghost lebih kecil dan presisi.
    @Override
    protected void updateCollisionBox() {
        // Hitung lebar dan tinggi hitbox yang baru berdasarkan skala.
        int hitboxWidth = (int) (this.width * HITBOX_SCALE_X);
        int hitboxHeight = (int) (this.height * HITBOX_SCALE_Y);

        // Hitung offset untuk memusatkan hitbox yang lebih kecil di dalam area render.
        int offsetX = (this.width - hitboxWidth) / 2;
        int offsetY = (this.height - hitboxHeight) / 2;

        // Atur ulang collision box dengan ukuran dan posisi yang baru.
        if (this.collisionBox == null) { // Inisialisasi jika null.
            this.collisionBox = new Rectangle((int)this.x + offsetX, (int)this.y + offsetY, hitboxWidth, hitboxHeight);
        } else {
            this.collisionBox.x = (int)this.x + offsetX;
            this.collisionBox.y = (int)this.y + offsetY;
            this.collisionBox.width = hitboxWidth;
            this.collisionBox.height = hitboxHeight;
        }
    }

    // Metode untuk mengecek apakah hantu sudah keluar dari batas layar.
    public boolean isOutOfBounds() {
        // Kembalikan true jika posisi hantu sudah jauh di luar layar kiri atau kanan.
        if (x + width < -20 && speedX < 0) return true;
        if (x > Constants.GAME_WIDTH + 20 && speedX > 0) return true;
        return false;
    }
}