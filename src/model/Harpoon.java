package model;

import java.awt.Color;
import java.awt.Graphics; // Diperlukan untuk rotasi
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle; // Untuk gambar harpoon
import java.awt.geom.AffineTransform; // Untuk rotasi
import java.net.URL; // Untuk memuat gambar
import javax.swing.ImageIcon;
import viewmodel.Constants; 

public class Harpoon extends GameObject {
    private Player player;
    private boolean isFiring;
    private float currentTipX, currentTipY;
    private float targetX, targetY;
    private float speed = 12.0f; 
    private float maxLength = 350f; 
    private float currentLength;
    private Jellyfish hookedJellyfish;
    private Image harpoonImage; // Baru: Untuk menyimpan gambar harpoon
    private int harpoonWidth, harpoonHeight; // Baru: Dimensi gambar harpoon

    public Harpoon(Player player) {
        // Ukuran GameObject (width, height) bisa kita gunakan untuk collision box ujung net,
        // atau kita set berdasarkan ukuran harpoon nanti.
        // Untuk sekarang, kita set placeholder, dan akan diupdate setelah gambar dimuat.
        super(player.getX(), player.getY(), 10, 10); 
        this.player = player;
        this.isFiring = false;
        this.hookedJellyfish = null;
        
        loadHarpoonImage(); // Panggil metode untuk memuat gambar harpoon
        
        // Setelah gambar dimuat, kita bisa set width dan height GameObject
        // jika ingin collision box ujung net sesuai ukuran harpoon.
        // Namun, collision box di Net lebih ke deteksi ujung, jadi ukuran kecil mungkin tetap ok.
        // this.width = harpoonWidth;
        // this.height = harpoonHeight;
    }

    private void loadHarpoonImage() {
        try {
            // PASTIKAN PATH INI BENAR dan file harpoon.png ada di sana
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
                // Anda bisa set width dan height GameObject di sini jika perlu
                // this.width = harpoonWidth; 
                // this.height = harpoonHeight;
            } else {
                 System.err.println("ImageIcon harpoon berhasil dibuat tapi gambar null.");
            }
        } catch (Exception e) {
            System.err.println("Error saat memuat gambar harpoon: " + e.getMessage());
        }
    }


    public void fire(float clickX, float clickY) { //
        if (!isFiring) {
            this.isFiring = true;
            this.hookedJellyfish = null;
            // Ujung net dimulai dari tengah player
            this.currentTipX = player.getX() + player.getWidth() / 2f;
            this.currentTipY = player.getY() + player.getHeight() / 2f;
            this.targetX = clickX;
            this.targetY = clickY;
            this.currentLength = 0;
            updateCollisionBoxForTip();
        }
    }
    
    private void updateCollisionBoxForTip() {
        // Collision box untuk ujung net (bisa lebih kecil dari gambar harpoon)
        // Kita posisikan di currentTipX, currentTipY
        int boxSize = 10; // Ukuran collision box kecil untuk ujungnya
        this.collisionBox.x = (int) currentTipX - boxSize / 2;
        this.collisionBox.y = (int) currentTipY - boxSize / 2;
        this.collisionBox.width = boxSize;
        this.collisionBox.height = boxSize;
    }

    @Override
    public void update() { //
        if (!isFiring) return;

        float pCenterX = player.getX() + player.getWidth() / 2f;
        float pCenterY = player.getY() + player.getHeight() / 2f;

        if (hookedJellyfish == null) { 
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
            float jCenterX = hookedJellyfish.getX() + hookedJellyfish.getWidth() / 2f;
            float jCenterY = hookedJellyfish.getY() + hookedJellyfish.getHeight() / 2f;
            float angleToPlayer = (float) Math.atan2(pCenterY - jCenterY, pCenterX - jCenterX);
            
            double distanceToPlayer = Point.distance(pCenterX, pCenterY, jCenterX, jCenterY);
            float pullSpeed = speed * 0.8f; 

            if (distanceToPlayer > pullSpeed) { 
                float moveX = (float) (pullSpeed * Math.cos(angleToPlayer));
                float moveY = (float) (pullSpeed * Math.sin(angleToPlayer));
                hookedJellyfish.setX(hookedJellyfish.getX() + moveX);
                hookedJellyfish.setY(hookedJellyfish.getY() + moveY);
            } else {
                hookedJellyfish.setX(pCenterX - hookedJellyfish.getWidth() / 2f);
                hookedJellyfish.setY(pCenterY - hookedJellyfish.getHeight() / 2f);
            }
            hookedJellyfish.updateCollisionBox();

            currentTipX = hookedJellyfish.getX() + hookedJellyfish.getWidth() / 2f;
            currentTipY = hookedJellyfish.getY() + hookedJellyfish.getHeight() / 2f;
            updateCollisionBoxForTip(); // Update posisi collision box agar mengikuti jellyfish yang ditarik
        }
    }

    public void retract() { //
        this.isFiring = false;
        this.currentLength = 0;
        // GameLogic akan memanggil finishAttempt untuk mereset hookedJellyfish jika perlu
    }
    
    public void finishAttempt() { //
        this.isFiring = false;
        this.hookedJellyfish = null;
        this.currentLength = 0;
    }

    @Override
    public void render(Graphics g) { //
        if (isFiring) {
            Graphics2D g2d = (Graphics2D) g.create(); // Buat salinan Graphics agar transformasi tidak mempengaruhi elemen lain

            float playerCenterX = player.getX() + player.getWidth() / 2f;
            float playerCenterY = player.getY() + player.getHeight() / 2f;

            // 1. Gambar Tali
            // Tali ditarik dari tengah player ke posisi ujung net saat ini (currentTipX, currentTipY)
            // atau ke pangkal harpoon jika kita bisa menghitungnya dengan presisi.
            // Untuk simpelnya, kita tarik ke currentTipX, currentTipY.
            g2d.setColor(new Color(139, 69, 19, 200)); // Warna coklat kayu, semi-transparan
            g2d.setStroke(new java.awt.BasicStroke(3)); // Ketebalan tali
            g2d.drawLine((int) playerCenterX, (int) playerCenterY,
                         (int) currentTipX, (int) currentTipY);

            // 2. Gambar Harpoon di Ujung Net (currentTipX, currentTipY)
            if (harpoonImage != null && harpoonWidth > 0 && harpoonHeight > 0) {
                // Hitung sudut dari player ke ujung net untuk rotasi harpoon
                // atau, jika sedang menarik, dari jellyfish (ujung net) ke player
                double angleRad;
                if (hookedJellyfish != null) { // Jika menarik jellyfish, harpoon menghadap player
                    angleRad = Math.atan2(playerCenterY - currentTipY, playerCenterX - currentTipX);
                } else { // Jika memanjang, harpoon menghadap target
                     // Sudut dari titik awal tembakan (tengah player) ke target mouse
                    float initialFireAngle = (float) Math.atan2(targetY - playerCenterY, targetX - playerCenterX);
                    angleRad = initialFireAngle;
                }

                // Simpan transformasi lama
                AffineTransform oldTransform = g2d.getTransform();
                
                // Pindahkan origin ke posisi ujung net untuk rotasi di sekitar titik tersebut
                g2d.translate(currentTipX, currentTipY);
                g2d.rotate(angleRad); 
                // Gambar harpoon. Ujung harpoon (misalnya, tengah depan) harus di (0,0) dalam koordinat lokal ini.
                // Jadi, kita gambar dengan offset setengah lebar dan tinggi.
                g2d.drawImage(harpoonImage, -harpoonWidth / 2, -harpoonHeight / 2, harpoonWidth, harpoonHeight, null);
                
                // Kembalikan transformasi ke semula
                g2d.setTransform(oldTransform);

            } else { 
                // Fallback jika gambar harpoon gagal dimuat: gambar lingkaran sederhana di ujung
                g2d.setColor(Color.DARK_GRAY);
                g2d.fillOval((int) currentTipX - 5, (int) currentTipY - 5, 10, 10); // Ukuran placeholder
            }
            g2d.dispose(); // Hapus salinan Graphics
        }
    }

    public boolean isFiring() { return isFiring; } //
    public Rectangle getTipCollisionBox() { return collisionBox; } //

    public void hookJellyfish(Jellyfish jf) { //
        if (this.hookedJellyfish == null && isFiring) { 
            this.hookedJellyfish = jf;
            // Saat ini, currentTipX dan currentTipY akan mengikuti jellyfish yang di-hook
            // karena diupdate() saat hookedJellyfish != null
        }
    }
    public Jellyfish getHookedJellyfish() { return hookedJellyfish; } //
}