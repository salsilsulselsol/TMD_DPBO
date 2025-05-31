package model;

import java.awt.Color;
import java.awt.Graphics;
import viewmodel.Constants;

public class Player extends GameObject {
    private float speed = 4.5f; // Kecepatan bisa disesuaikan
    private int hearts;
    private boolean moveLeft, moveRight, moveUp, moveDown;

    public Player(float x, float y, int width, int height, int initialHearts) {
        super(x, y, width, height);
        this.hearts = initialHearts;
        loadImage("/assets/images/spongebob.png"); // PASTIKAN ASET ADA
    }

    @Override
    public void update() {
        float dx = 0, dy = 0;
        if (moveLeft) dx -= speed;
        if (moveRight) dx += speed;
        if (moveUp) dy -= speed;
        if (moveDown) dy += speed;

        x += dx;
        y += dy;

        // Batasan layar
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - width;
        if (y + height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - height;

        updateCollisionBox();
    }

    @Override
    public void render(Graphics g) {
        if (image != null) {
            g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            g.setColor(Color.YELLOW); 
            g.fillRect((int)x, (int)y, width, height); // Gambar kotak jika aset gagal
        }
    }

    public void loseHeart() { if (hearts > 0) hearts--; }
    public int getHearts() { return hearts; }
    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void setMoveUp(boolean b) { moveUp = b; }
    public void setMoveDown(boolean b) { moveDown = b; }
}