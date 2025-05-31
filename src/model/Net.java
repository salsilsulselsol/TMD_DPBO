package model;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import viewmodel.Constants; // Untuk batas layar

public class Net extends GameObject {
    private Player player;
    private boolean isFiring;
    private float currentTipX, currentTipY;
    private float targetX, targetY;
    private float speed = 12.0f; // Kecepatan net
    private float maxLength = 350f; // Jangkauan net
    private float currentLength;
    private Jellyfish hookedJellyfish;

    public Net(Player player) {
        super(player.getX(), player.getY(), 10, 10); // Ukuran untuk collision box ujung net
        this.player = player;
        this.isFiring = false;
        this.hookedJellyfish = null;
        // `image` untuk Net bisa berupa ikon kecil di ujung, atau tidak digunakan jika hanya garis
    }

    public void fire(float clickX, float clickY) {
        if (!isFiring) {
            this.isFiring = true;
            this.hookedJellyfish = null;
            this.currentTipX = player.getX() + player.getWidth() / 2f;
            this.currentTipY = player.getY() + player.getHeight() / 2f;
            this.targetX = clickX;
            this.targetY = clickY;
            this.currentLength = 0;
            updateCollisionBoxForTip();
        }
    }
    
    private void updateCollisionBoxForTip() {
        this.collisionBox.x = (int) currentTipX - width / 2;
        this.collisionBox.y = (int) currentTipY - height / 2;
    }

    @Override
    public void update() {
        if (!isFiring) return;

        float pCenterX = player.getX() + player.getWidth() / 2f;
        float pCenterY = player.getY() + player.getHeight() / 2f;

        if (hookedJellyfish == null) { // Net memanjang mencari target
            float angle = (float) Math.atan2(targetY - pCenterY, targetX - pCenterX);
            currentTipX += speed * Math.cos(angle);
            currentTipY += speed * Math.sin(angle);
            currentLength = (float) Point.distance(pCenterX, pCenterY, currentTipX, currentTipY);
            updateCollisionBoxForTip();

            // Cek jika net keluar batas layar atau mencapai panjang maksimal
            if (currentLength >= maxLength || 
                currentTipX < 0 || currentTipX > Constants.GAME_WIDTH ||
                currentTipY < 0 || currentTipY > Constants.GAME_HEIGHT) {
                retract(); // Tarik kembali jika miss atau keluar batas
            }
        } else { // Net sudah menangkap, menarik Jellyfish ke player
            float jCenterX = hookedJellyfish.getX() + hookedJellyfish.getWidth() / 2f;
            float jCenterY = hookedJellyfish.getY() + hookedJellyfish.getHeight() / 2f;
            float angle = (float) Math.atan2(pCenterY - jCenterY, pCenterX - jCenterX);
            
            // Jarak antara player dan jellyfish
            double distanceToPlayer = Point.distance(pCenterX, pCenterY, jCenterX, jCenterY);
            
            // Kecepatan menarik bisa lebih lambat atau sama
            float pullSpeed = speed * 0.8f; 

            // Hanya gerakkan jika jarak masih cukup besar
            if (distanceToPlayer > pullSpeed) { // pullSpeed bisa dianggap sebagai threshold kedekatan
                float moveX = (float) (pullSpeed * Math.cos(angle));
                float moveY = (float) (pullSpeed * Math.sin(angle));
                hookedJellyfish.setX(hookedJellyfish.getX() + moveX);
                hookedJellyfish.setY(hookedJellyfish.getY() + moveY);
            } else {
                // Jika sudah sangat dekat, set posisi jellyfish tepat di player untuk memicu struggle
                hookedJellyfish.setX(pCenterX - hookedJellyfish.getWidth() / 2f);
                hookedJellyfish.setY(pCenterY - hookedJellyfish.getHeight() / 2f);
            }
            hookedJellyfish.updateCollisionBox();

            // Ujung net mengikuti jellyfish yang ditarik
            currentTipX = hookedJellyfish.getX() + hookedJellyfish.getWidth() / 2f;
            currentTipY = hookedJellyfish.getY() + hookedJellyfish.getHeight() / 2f;
            // Transisi ke state STRUGGLING akan dihandle oleh GameLogic saat collision terdeteksi
        }
    }

    public void retract() {
        this.isFiring = false;
        // `hookedJellyfish` tidak di-null-kan di sini jika ini adalah akhir dari attempt (berhasil/gagal struggle)
        // GameLogic akan memanggil finishAttempt()
        this.currentLength = 0;
    }
    
    public void finishAttempt() {
        this.isFiring = false;
        this.hookedJellyfish = null;
        this.currentLength = 0;
    }

    @Override
    public void render(Graphics g) {
        if (isFiring) {
            g.setColor(new Color(50, 50, 50, 200)); // Warna tali lasso semi-transparan
            // Gambar garis dari tengah player ke ujung net (currentTipX, currentTipY)
            g.drawLine((int) (player.getX() + player.getWidth() / 2f),
                       (int) (player.getY() + player.getHeight() / 2f),
                       (int) currentTipX, (int) currentTipY);
            
            // Gambar ujung net (bisa berupa gambar ikon kecil)
            if (image != null) { // Jika ada aset gambar untuk ujung net
                 g.drawImage(image, (int) currentTipX - width/2, (int) currentTipY - height/2, width, height, null);
            } else { // Jika tidak, gambar lingkaran sederhana
                g.setColor(Color.DARK_GRAY);
                g.fillOval((int) currentTipX - width/2, (int) currentTipY - height/2, width, height);
            }
        }
    }

    public boolean isFiring() { return isFiring; }
    public Rectangle getTipCollisionBox() { return collisionBox; }

    public void hookJellyfish(Jellyfish jf) {
        if (this.hookedJellyfish == null && isFiring) { // Hanya bisa hook jika sedang memanjang dan belum ada yg terkait
            this.hookedJellyfish = jf;
        }
    }
    public Jellyfish getHookedJellyfish() { return hookedJellyfish; }
}