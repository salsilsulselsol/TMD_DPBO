package model;

import java.awt.Color;
import java.awt.Graphics;

public class Jar extends GameObject {
    private int collectedCount;
    private int totalScore;

    public Jar(float x, float y, int width, int height, String imagePath) {
        super(x, y, width, height);
        this.collectedCount = 0;
        this.totalScore = 0;
        loadImage(imagePath); 
    }

    // Diubah untuk menerima objek Fish (atau turunannya)
    public void addToJar(Fish fish) { // Sebelumnya Jellyfish
        if (fish != null) {
            this.collectedCount++;
            this.totalScore += fish.getScoreValue();
        }
    }

    @Override
    public void update() { /* Jar statis */ }

    @Override
    public void render(Graphics g) { //
        if (image != null) {
            g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            g.setColor(new Color(139, 69, 19)); 
            g.fillRect((int)x, (int)y, width, height);
        }
    }

    public int getCollectedCount() { return collectedCount; } //
    public int getTotalScore() { return totalScore; } //
    public void reset() { //
        this.collectedCount = 0;
        this.totalScore = 0;
    }
}