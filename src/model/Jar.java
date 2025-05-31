package model;

import java.awt.Color;
import java.awt.Graphics;
// import java.awt.Font; // Tidak perlu jika skor ditampilkan oleh GamePanel

public class Jar extends GameObject {
    private int collectedCount;
    private int totalScore;

    public Jar(float x, float y, int width, int height, String imagePath) {
        super(x, y, width, height);
        this.collectedCount = 0;
        this.totalScore = 0;
        loadImage(imagePath); // PASTIKAN ASET ADA
    }

    public void addToJar(Jellyfish jellyfish) {
        this.collectedCount++;
        this.totalScore += jellyfish.getScoreValue();
    }

    @Override
    public void update() { /* Jar statis */ }

    @Override
    public void render(Graphics g) {
        if (image != null) {
            g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            g.setColor(new Color(139, 69, 19)); // Coklat untuk placeholder Jar
            g.fillRect((int)x, (int)y, width, height);
        }
        // Tampilan skor dan count akan dihandle oleh GamePanel.drawUI()
    }

    public int getCollectedCount() { return collectedCount; }
    public int getTotalScore() { return totalScore; }
    public void reset() {
        this.collectedCount = 0;
        this.totalScore = 0;
    }
}