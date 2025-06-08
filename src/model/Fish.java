package model;

import java.util.Random;
import viewmodel.Constants;

public class Fish extends GameObject {
    protected int scoreValue; 
    protected float speedX;
    protected float struggleFactor; // BARU: Faktor kesulitan struggle

    public Fish(float x, float y, int renderWidth, int renderHeight, int scoreValue, 
                String spriteSheetPath, boolean movesLeftToRight, float baseSpeedMultiplier,
                int spriteFrameW, int spriteFrameH, int totalFrames, int frameDelay,
                float struggleDifficultyFactor) { // Parameter baru untuk struggle
        super(x, y, renderWidth, renderHeight);
        this.scoreValue = scoreValue;
        this.struggleFactor = struggleDifficultyFactor; // Inisialisasi struggleFactor
        loadSpriteSheet(spriteSheetPath, spriteFrameW, spriteFrameH, totalFrames, frameDelay);

        Random rand = new Random();
        float baseSpeed = (1.0f + rand.nextFloat() * 0.8f) * baseSpeedMultiplier; 
        
        this.speedX = movesLeftToRight ? baseSpeed : -baseSpeed;
        this.isFacingRight_anim = movesLeftToRight; 
    }

    // Konstruktor yang disederhanakan untuk Fish normal
    public Fish(float x, float y, String spriteSheetPath, boolean movesLeftToRight) {
        // renderW, renderH, score, spritePath, LtoR, speedMult, spriteW, spriteH, totalFrames, frameDelay, struggleFactor
        this(x, y, 32, 32, 10, spriteSheetPath, movesLeftToRight, 1.5f, 
             32, 32, 4, 150, 
             1.0f); // Struggle factor normal untuk ikan biasa
    }

    @Override
    public void update() {
        x += speedX;
        updateAnimation(); 
        updateCollisionBox();
    }

    public int getScoreValue() { return scoreValue; }
    public float getStruggleFactor() { return struggleFactor; } // Getter untuk struggleFactor

    public boolean isOutOfBounds() {
        if (speedX < 0 && x + width < -20) return true; 
        if (speedX > 0 && x > Constants.GAME_WIDTH + 20) return true;
        return false;
    }
}