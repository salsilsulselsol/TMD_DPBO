package model;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import javax.swing.ImageIcon;
import java.net.URL;
import viewmodel.Constants; 

public class Harpoon extends GameObject { 
    private Player player;
    private boolean isFiring;
    private float currentTipX, currentTipY;
    private float targetX, targetY;
    private float speed = 12.0f;
    private float maxLength = 350f;
    private float currentLength;
    private GameObject hookedObject; // Diubah dari Jellyfish menjadi GameObject
    private Image harpoonImage;
    private int harpoonWidth, harpoonHeight;

    public Harpoon(Player player) { 
        super(player.getX(), player.getY(), 10, 10); 
        this.player = player;
        this.isFiring = false;
        this.hookedObject = null; // Diinisialisasi sebagai null
        loadHarpoonImage();
    }

    private void loadHarpoonImage() {
        try {
            URL imgUrl = getClass().getResource("/assets/images/harpoon.png"); 
            if (imgUrl == null) {
                 System.err.println("Gagal memuat gambar harpoon: /assets/images/harpoon.png (resource tidak ditemukan).");
                 return;
            }
            ImageIcon ii = new ImageIcon(imgUrl);
            this.harpoonImage = ii.getImage();
            if (this.harpoonImage != null) {
                this.harpoonWidth = harpoonImage.getWidth(null);
                this.harpoonHeight = harpoonImage.getHeight(null);
            } else {
                 System.err.println("ImageIcon harpoon berhasil dibuat tapi gambar null.");
            }
        } catch (Exception e) {
            System.err.println("Error saat memuat gambar harpoon: " + e.getMessage());
        }
    }

    public void fire(float clickX, float clickY) {
        if (!isFiring) {
            this.isFiring = true;
            this.hookedObject = null;
            this.currentTipX = player.getX() + player.getWidth() / 2f;
            this.currentTipY = player.getY() + player.getHeight() / 2f;
            this.targetX = clickX;
            this.targetY = clickY;
            this.currentLength = 0;
            updateCollisionBoxForTip();
        }
    }
    
    private void updateCollisionBoxForTip() {
        int boxSize = 10; 
        this.collisionBox.x = (int) currentTipX - boxSize / 2;
        this.collisionBox.y = (int) currentTipY - boxSize / 2;
        this.collisionBox.width = boxSize;
        this.collisionBox.height = boxSize;
    }

    @Override
    public void update() {
        if (!isFiring) return;

        float pCenterX = player.getX() + player.getWidth() / 2f;
        float pCenterY = player.getY() + player.getHeight() / 2f;

        if (hookedObject == null) { 
            float angle = (float) Math.atan2(targetY - pCenterY, targetX - pCenterX);
            currentTipX += speed * Math.cos(angle);
            currentTipY += speed * Math.sin(angle);
            currentLength = (float) Point.distance(pCenterX, pCenterY, currentTipX, currentTipY);
            updateCollisionBoxForTip();

            if (currentLength >= maxLength || 
                currentTipX < 0 || currentTipX > Constants.GAME_WIDTH ||
                currentTipY < 0 || currentTipY > Constants.GAME_HEIGHT) {
                retract(); 
            }
        } else { 
            float objCenterX = hookedObject.getX() + hookedObject.getWidth() / 2f;
            float objCenterY = hookedObject.getY() + hookedObject.getHeight() / 2f;
            float angleToPlayer = (float) Math.atan2(pCenterY - objCenterY, pCenterX - objCenterX);
            
            double distanceToPlayer = Point.distance(pCenterX, pCenterY, objCenterX, objCenterY);
            float pullSpeed = speed * 0.8f; 

            if (distanceToPlayer > pullSpeed) { 
                float moveX = (float) (pullSpeed * Math.cos(angleToPlayer));
                float moveY = (float) (pullSpeed * Math.sin(angleToPlayer));
                hookedObject.setX(hookedObject.getX() + moveX);
                hookedObject.setY(hookedObject.getY() + moveY);
            } else {
                hookedObject.setX(pCenterX - hookedObject.getWidth() / 2f);
                hookedObject.setY(pCenterY - hookedObject.getHeight() / 2f);
            }
            hookedObject.updateCollisionBox();

            currentTipX = hookedObject.getX() + hookedObject.getWidth() / 2f;
            currentTipY = hookedObject.getY() + hookedObject.getHeight() / 2f;
            updateCollisionBoxForTip(); 
        }
    }

    public void retract() {
        this.isFiring = false;
        this.currentLength = 0;
    }
    
    public void finishAttempt() {
        this.isFiring = false;
        this.hookedObject = null;
        this.currentLength = 0;
    }

    @Override
    public void render(Graphics g) {
        if (isFiring) {
            Graphics2D g2d = (Graphics2D) g.create(); 

            float playerCenterX = player.getX() + player.getWidth() / 2f;
            float playerCenterY = player.getY() + player.getHeight() / 2f;

            g2d.setColor(new Color(139, 69, 19, 200)); 
            g2d.setStroke(new java.awt.BasicStroke(3)); 
            g2d.drawLine((int) playerCenterX, (int) playerCenterY,
                         (int) currentTipX, (int) currentTipY);

            if (harpoonImage != null && harpoonWidth > 0 && harpoonHeight > 0) {
                double angleRad;
                if (hookedObject != null) { 
                    angleRad = Math.atan2(playerCenterY - currentTipY, playerCenterX - currentTipX);
                } else { 
                    float initialFireAngle = (float) Math.atan2(targetY - playerCenterY, targetX - playerCenterX);
                    angleRad = initialFireAngle;
                }

                AffineTransform oldTransform = g2d.getTransform();
                
                g2d.translate(currentTipX, currentTipY);
                g2d.rotate(angleRad); 
                g2d.drawImage(harpoonImage, -harpoonWidth / 2, -harpoonHeight / 2, harpoonWidth, harpoonHeight, null);
                
                g2d.setTransform(oldTransform);

            } else { 
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillOval((int) currentTipX - 5, (int) currentTipY - 5, 10, 10); 
            }
            g2d.dispose(); 
        }
    }

    public boolean isFiring() { return isFiring; } 
    public Rectangle getTipCollisionBox() { return collisionBox; } 

    // Metode diubah untuk menerima GameObject
    public void hookObject(GameObject obj) {
        if (this.hookedObject == null && isFiring) {
            if (!(obj instanceof Ghost)) { // Harpoon tidak bisa menangkap Ghost
                this.hookedObject = obj;
            }
        }
    }
    // Metode diubah untuk mengembalikan GameObject
    public GameObject getHookedObject() { 
        return hookedObject; 
    }
}