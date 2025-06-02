package model;

import java.awt.Rectangle;
import java.util.Random;
import viewmodel.Constants; // Impor Rectangle jika belum ada

public class Ghost extends GameObject {
    private float speedX;

    // Faktor skala untuk hitbox Ghost (misalnya, 0.7 berarti 70% dari ukuran render)
    private static final float HITBOX_SCALE_X = 0.5f; // 70% dari lebar render
    private static final float HITBOX_SCALE_Y = 0.6f; // 80% dari tinggi render (mungkin ingin lebih tinggi dari lebar)

    public Ghost(float x, float y, int renderWidth, int renderHeight, String spriteSheetPath, 
                 boolean movesLeftToRight, int spriteFrameW, int spriteFrameH, int totalFrames, int frameDelay) {
        super(x, y, renderWidth, renderHeight);
        loadSpriteSheet(spriteSheetPath, spriteFrameW, spriteFrameH, totalFrames, frameDelay); //

        Random rand = new Random();
        float baseSpeedX = 1.2f + rand.nextFloat() * 1.0f; 
        this.speedX = movesLeftToRight ? baseSpeedX : -baseSpeedX;
        
        // Logika untuk arah hadap Ghost (default sprite kiri)
        this.isFacingRight_anim = !movesLeftToRight; 

        // Inisialisasi collision box awal yang disesuaikan
        updateCollisionBox(); 
    }
    
    public Ghost(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        this(x, y, 31, 44, spriteSheetPath, movesLeftToRight,
             31, 44, 4, 200); 
    }

    @Override
    public void update() {
        x += speedX;
        updateAnimation(); 
        updateCollisionBox(); // Pastikan ini dipanggil untuk memperbarui posisi hitbox
    }

    // Override updateCollisionBox untuk Ghost agar lebih kecil
    @Override
    protected void updateCollisionBox() {
        // Hitung lebar dan tinggi hitbox yang baru berdasarkan skala
        int hitboxWidth = (int) (this.width * HITBOX_SCALE_X);
        int hitboxHeight = (int) (this.height * HITBOX_SCALE_Y);

        // Hitung offset untuk memusatkan hitbox yang lebih kecil di dalam area render
        int offsetX = (this.width - hitboxWidth) / 2;
        int offsetY = (this.height - hitboxHeight) / 2;

        // Set collision box dengan ukuran dan posisi yang baru
        if (this.collisionBox == null) { // Inisialisasi jika null
            this.collisionBox = new Rectangle((int)this.x + offsetX, (int)this.y + offsetY, hitboxWidth, hitboxHeight);
        } else {
            this.collisionBox.x = (int)this.x + offsetX;
            this.collisionBox.y = (int)this.y + offsetY;
            this.collisionBox.width = hitboxWidth;
            this.collisionBox.height = hitboxHeight;
        }
    }

    public boolean isOutOfBounds() {
        if (x + width < -20 && speedX < 0) return true;
        if (x > Constants.GAME_WIDTH + 20 && speedX > 0) return true;
        return false;
    }
}