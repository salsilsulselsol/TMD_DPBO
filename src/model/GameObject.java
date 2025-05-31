package model;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.net.URL;
import javax.swing.ImageIcon;

public abstract class GameObject {
    protected float x, y;
    protected int width, height;
    protected Rectangle collisionBox;
    protected Image image;

    public GameObject(float x, float y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.collisionBox = new Rectangle((int)x, (int)y, width, height);
    }

    protected void loadImage(String imagePath) {
        try {
            URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl == null) {
                 System.err.println("Gagal memuat gambar: " + imagePath + " (resource tidak ditemukan). Pastikan path diawali '/' dan relatif dari root classpath (folder src).");
                 return;
            }
            ImageIcon ii = new ImageIcon(imgUrl);
            this.image = ii.getImage();
            if (this.image == null || ii.getIconWidth() == -1) {
                 System.err.println("ImageIcon berhasil dibuat tapi gambar null atau error: " + imagePath);
            }
        } catch (Exception e) {
            System.err.println("Error saat memuat gambar " + imagePath + ": " + e.getMessage());
        }
    }

    protected void updateCollisionBox() {
        this.collisionBox.x = (int)x;
        this.collisionBox.y = (int)y;
    }

    public Rectangle getCollisionBox() { return collisionBox; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public abstract void render(Graphics g);
    public abstract void update();
}