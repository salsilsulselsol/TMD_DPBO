package model;

import java.awt.Graphics;
import java.awt.Color;
import java.util.Random;
import viewmodel.Constants;

public class Jellyfish extends GameObject {
    private int scoreValue;
    private float speedX;

    public Jellyfish(float x, float y, int width, int height, int scoreValue, String imagePath, boolean movesLeftToRight) {
        super(x, y, width, height);
        this.scoreValue = scoreValue;
        loadImage(imagePath); // PASTIKAN ASET ADA

        Random rand = new Random();
        // Kecepatan bisa lebih bervariasi
        float baseSpeed = 1.2f + rand.nextFloat() * 1.8f; // antara 1.2 dan 3.0
        if (movesLeftToRight) {
            this.speedX = baseSpeed;
        } else {
            this.speedX = -baseSpeed;
        }
    }

    @Override
    public void update() {
        x += speedX;
        updateCollisionBox();
    }

    @Override
    public void render(Graphics g) {
        if (image != null) {
            g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            g.setColor(Color.PINK); 
            g.fillOval((int)x, (int)y, width, height); // Gambar oval jika aset gagal
        }
    }

    public int getScoreValue() { return scoreValue; }
    public boolean isOutOfBounds() {
        if (speedX < 0 && x + width < -10) return true; // Beri sedikit margin
        if (speedX > 0 && x > Constants.GAME_WIDTH + 10) return true;
        return false;
    }
}