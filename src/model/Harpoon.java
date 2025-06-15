package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.net.URL;
import javax.swing.ImageIcon;
import viewmodel.Constants; 

// Kelas ini merepresentasikan objek harpun (tali lasso) yang digunakan pemain.
public class Harpoon extends GameObject { 
    // Referensi ke objek Player untuk mengetahui posisi awal tembakan.
    private Player player;
    // Status apakah harpun sedang ditembakkan atau tidak.
    private boolean isFiring;
    // Koordinat ujung harpun saat ini.
    private float currentTipX, currentTipY;
    // Koordinat target (lokasi klik mouse).
    private float targetX, targetY;
    // Kecepatan gerak harpun.
    private float speed = 12.0f;
    // Jarak maksimal yang bisa dicapai harpun.
    private float maxLength = 350f;
    private float currentLength;
    // Objek yang berhasil dikait oleh harpun.
    private GameObject hookedObject;
    // Gambar sprite untuk harpun.
    private Image harpoonImage;
    private int harpoonWidth, harpoonHeight;

    // Konstruktor, menginisialisasi harpun dengan referensi ke player.
    public Harpoon(Player player) { 
        super(player.getX(), player.getY(), 10, 10); // Hitbox awal hanya untuk ujungnya.
        this.player = player;
        this.isFiring = false;
        this.hookedObject = null; // Awalnya tidak mengait apa-apa.
        loadHarpoonImage();
    }

    // Metode internal untuk memuat gambar sprite harpun.
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

    // Metode untuk memulai tembakan harpun.
    public void fire(float clickX, float clickY) {
        // Hanya bisa menembak jika tidak sedang dalam kondisi menembak.
        if (!isFiring) {
            this.isFiring = true;
            this.hookedObject = null; // Reset objek yang dikait.
            // Atur posisi awal tembakan dari tengah player.
            this.currentTipX = player.getX() + player.getWidth() / 2f;
            this.currentTipY = player.getY() + player.getHeight() / 2f;
            this.targetX = clickX;
            this.targetY = clickY;
            this.currentLength = 0;
            updateCollisionBoxForTip(); // Pindahkan hitbox ke ujung harpun.
        }
    }
    
    // Helper untuk mengupdate posisi hitbox agar selalu di ujung harpun.
    private void updateCollisionBoxForTip() {
        int boxSize = 10; 
        this.collisionBox.x = (int) currentTipX - boxSize / 2;
        this.collisionBox.y = (int) currentTipY - boxSize / 2;
        this.collisionBox.width = boxSize;
        this.collisionBox.height = boxSize;
    }

    // Metode update utama untuk harpun, dipanggil setiap frame.
    @Override
    public void update() {
        if (!isFiring) return; // Jika tidak sedang menembak, tidak ada yang perlu diupdate.

        float pCenterX = player.getX() + player.getWidth() / 2f;
        float pCenterY = player.getY() + player.getHeight() / 2f;

        // Logika jika harpun sedang bergerak keluar (belum mengait apapun).
        if (hookedObject == null) { 
            // Hitung sudut menuju target dan gerakkan ujung harpun.
            float angle = (float) Math.atan2(targetY - pCenterY, targetX - pCenterX);
            currentTipX += speed * Math.cos(angle);
            currentTipY += speed * Math.sin(angle);
            currentLength = (float) Point.distance(pCenterX, pCenterY, currentTipX, currentTipY);
            updateCollisionBoxForTip();

            // Jika harpun mencapai jarak maksimal atau keluar layar, tarik kembali.
            if (currentLength >= maxLength || 
                currentTipX < 0 || currentTipX > Constants.GAME_WIDTH ||
                currentTipY < 0 || currentTipY > Constants.GAME_HEIGHT) {
                retract(); 
            }
        } else { // Logika jika harpun sudah mengait objek dan sedang menariknya.
            float objCenterX = hookedObject.getX() + hookedObject.getWidth() / 2f;
            float objCenterY = hookedObject.getY() + hookedObject.getHeight() / 2f;
            // Hitung sudut dari objek menuju player.
            float angleToPlayer = (float) Math.atan2(pCenterY - objCenterY, pCenterX - objCenterX);
            
            double distanceToPlayer = Point.distance(pCenterX, pCenterY, objCenterX, objCenterY);
            float pullSpeed = speed * 0.8f; // Kecepatan menarik sedikit lebih lambat.

            // Gerakkan objek yang dikait menuju player.
            if (distanceToPlayer > pullSpeed) { 
                float moveX = (float) (pullSpeed * Math.cos(angleToPlayer));
                float moveY = (float) (pullSpeed * Math.sin(angleToPlayer));
                hookedObject.setX(hookedObject.getX() + moveX);
                hookedObject.setY(hookedObject.getY() + moveY);
            } else {
                // Jika sudah sangat dekat, 'tempelkan' objek ke player.
                hookedObject.setX(pCenterX - hookedObject.getWidth() / 2f);
                hookedObject.setY(pCenterY - hookedObject.getHeight() / 2f);
            }
            hookedObject.updateCollisionBox();

            // Ujung harpun sekarang menempel pada objek yang dikait.
            currentTipX = hookedObject.getX() + hookedObject.getWidth() / 2f;
            currentTipY = hookedObject.getY() + hookedObject.getHeight() / 2f;
            updateCollisionBoxForTip(); 
        }
    }

    // Menarik kembali harpun (jika gagal mengenai target).
    public void retract() {
        this.isFiring = false;
        this.currentLength = 0;
    }
    
    // Menyelesaikan percobaan menembak (setelah struggle selesai).
    public void finishAttempt() {
        this.isFiring = false;
        this.hookedObject = null;
        this.currentLength = 0;
    }

    // Metode untuk menggambar harpun dan talinya.
    @Override
    public void render(Graphics g) {
        if (isFiring) {
            Graphics2D g2d = (Graphics2D) g.create(); // Buat salinan Graphics2D agar tidak mengganggu komponen lain.

            float playerCenterX = player.getX() + player.getWidth() / 2f;
            float playerCenterY = player.getY() + player.getHeight() / 2f;

            // Gambar tali harpun dari player ke ujung harpun.
            g2d.setColor(new Color(139, 69, 19, 200)); 
            g2d.setStroke(new java.awt.BasicStroke(3)); 
            g2d.drawLine((int) playerCenterX, (int) playerCenterY,
                         (int) currentTipX, (int) currentTipY);

            // Jika gambar harpun ada, gambar sprite harpun di ujung tali.
            if (harpoonImage != null && harpoonWidth > 0 && harpoonHeight > 0) {
                // Tentukan sudut rotasi agar harpun menghadap arah yang benar.
                double angleRad;
                if (hookedObject != null) { // Jika menarik objek, harpun menghadap ke player.
                    angleRad = Math.atan2(playerCenterY - currentTipY, playerCenterX - currentTipX);
                } else { // Jika bergerak keluar, harpun menghadap ke target.
                    float initialFireAngle = (float) Math.atan2(targetY - playerCenterY, targetX - playerCenterX);
                    angleRad = initialFireAngle;
                }

                AffineTransform oldTransform = g2d.getTransform();
                
                // Lakukan transformasi (translasi dan rotasi) untuk menggambar harpun.
                g2d.translate(currentTipX, currentTipY);
                g2d.rotate(angleRad); 
                g2d.drawImage(harpoonImage, -harpoonWidth / 2, -harpoonHeight / 2, harpoonWidth, harpoonHeight, null);
                
                g2d.setTransform(oldTransform); // Kembalikan transformasi ke kondisi semula.

            } else { // Fallback jika gambar gagal dimuat, gambar lingkaran sederhana.
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillOval((int) currentTipX - 5, (int) currentTipY - 5, 10, 10); 
            }
            g2d.dispose(); // Hapus salinan Graphics2D.
        }
    }

    // Getter untuk status harpun.
    public boolean isFiring() { return isFiring; } 
    public Rectangle getTipCollisionBox() { return collisionBox; } 

    // Metode untuk mengaitkan sebuah objek.
    public void hookObject(GameObject obj) {
        if (this.hookedObject == null && isFiring) {
            // Harpun tidak bisa menangkap Ghost.
            if (!(obj instanceof Ghost)) {
                this.hookedObject = obj;
            }
        }
    }

    // Getter untuk mendapatkan objek yang sedang dikait.
    public GameObject getHookedObject() { 
        return hookedObject; 
    }
}