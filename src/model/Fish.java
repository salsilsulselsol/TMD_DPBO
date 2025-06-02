package model;

import java.util.Random;
import viewmodel.Constants;

public class Fish extends GameObject {
    protected int scoreValue; 
    protected float speedX;

    public Fish(float x, float y, int renderWidth, int renderHeight, int scoreValue, 
                String spriteSheetPath, boolean movesLeftToRight, float baseSpeedMultiplier,
                int spriteFrameW, int spriteFrameH, int totalFrames, int frameDelay) {
        super(x, y, renderWidth, renderHeight);
        this.scoreValue = scoreValue;
        loadSpriteSheet(spriteSheetPath, spriteFrameW, spriteFrameH, totalFrames, frameDelay);

        Random rand = new Random();
        float baseSpeed = (1.0f + rand.nextFloat() * 0.8f) * baseSpeedMultiplier; 
        
        this.speedX = movesLeftToRight ? baseSpeed : -baseSpeed;
        // Atur arah hadap sprite berdasarkan arah gerak awal.
        // Asumsi: sprite sheet ikan Anda secara default menghadap ke KANAN.
        // Jika ikan bergerak ke kiri (movesLeftToRight == false), maka isFacingRight_anim harus false.
        this.isFacingRight_anim = movesLeftToRight; 
    }

    // Konstruktor yang disederhanakan
    public Fish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // Path, render W, render H, score, speed mult, sprite W, sprite H, total frames, frame delay
        this(x, y, 32, 32, 10, spriteSheetPath, movesLeftToRight, 1.5f, 
             32, 32, 4, 150); 
    }

    @Override
    public void update() {
        x += speedX;
        updateAnimation(); 
        updateCollisionBox();
    }

    public int getScoreValue() { return scoreValue; }

    public boolean isOutOfBounds() {
        if (speedX < 0 && x + width < -20) return true; 
        if (speedX > 0 && x > Constants.GAME_WIDTH + 20) return true;
        return false;
    }
}