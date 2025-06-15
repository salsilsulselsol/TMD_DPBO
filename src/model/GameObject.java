package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

// Kelas abstrak ini adalah 'cetakan' atau 'blueprint' untuk semua objek di dalam game.
// Semua entitas seperti Player, Fish, dan Ghost adalah turunan dari kelas ini.
public abstract class GameObject {
    // Properti dasar yang dimiliki semua objek game: posisi dan ukuran.
    protected float x, y;
    protected int width, height; // Ukuran render objek di layar.
    // Kotak deteksi tabrakan untuk objek ini.
    protected Rectangle collisionBox;
    // Gambar untuk objek yang tidak beranimasi (statis).
    protected Image image; 
    
    // Kumpulan properti untuk objek yang memiliki animasi dari sprite sheet.
    protected BufferedImage spriteSheet;
    protected int frameWidth_sprite;     
    protected int frameHeight_sprite;    
    protected int currentAnimFrame;
    protected int totalAnimFrames;
    protected long lastFrameTime_anim;
    protected int frameDelayMs_anim;
    protected boolean animated; 
    protected boolean isFacingRight_anim = true; // Arah hadap sprite (default kanan).

    // Variabel statis untuk mengontrol apakah hitbox digambar atau tidak (untuk debugging).
    public static boolean DEBUG_DRAW_HITBOX = false;

    // Konstruktor dasar untuk semua GameObject.
    public GameObject(float x, float y, int renderWidth, int renderHeight) {
        this.x = x;
        this.y = y;
        this.width = renderWidth; 
        this.height = renderHeight;
        // Inisialisasi collision box dengan ukuran yang sama dengan ukuran render.
        this.collisionBox = new Rectangle((int)x, (int)y, renderWidth, renderHeight);
        this.animated = false; // Defaultnya, objek tidak beranimasi.
    }

    // Metode untuk memuat gambar statis (non-animasi).
    protected void loadImage(String imagePath) {
        try {
            URL imgUrl = getClass().getResource(imagePath);
            if (imgUrl == null) {
                 System.err.println("Gagal memuat gambar: " + imagePath + " (resource tidak ditemukan).");
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

    // Metode untuk memuat gambar sprite sheet untuk animasi.
    protected void loadSpriteSheet(String spriteSheetPath, int frameW, int frameH, int totalFrames, int frameDelay) {
        try {
            URL sheetUrl = getClass().getResource(spriteSheetPath);
            if (sheetUrl == null) {
                System.err.println("Gagal memuat sprite sheet: " + spriteSheetPath + " (resource tidak ditemukan).");
                this.animated = false;
                return;
            }
            this.spriteSheet = ImageIO.read(sheetUrl);
            if (this.spriteSheet != null) {
                // Inisialisasi semua variabel yang dibutuhkan untuk logika animasi.
                this.frameWidth_sprite = frameW;
                this.frameHeight_sprite = frameH;
                this.totalAnimFrames = totalFrames;
                this.frameDelayMs_anim = frameDelay;
                this.currentAnimFrame = 0;
                this.lastFrameTime_anim = System.currentTimeMillis();
                this.animated = true; // Tandai objek ini sebagai objek beranimasi.
            } else {
                System.err.println("Sprite sheet berhasil dibaca tapi null: " + spriteSheetPath);
                this.animated = false;
            }
        } catch (Exception e) {
            System.err.println("Error saat memuat sprite sheet " + spriteSheetPath + ": " + e.getMessage());
            this.animated = false;
        }
    }
    
    // Logika default untuk mengupdate frame animasi.
    protected void updateAnimation() {
        if (!animated || spriteSheet == null || totalAnimFrames == 0) return;

        // Cek apakah sudah waktunya untuk pindah ke frame berikutnya.
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime_anim > frameDelayMs_anim) {
            currentAnimFrame++;
            // Jika sudah mencapai frame terakhir, kembali ke awal (looping).
            if (currentAnimFrame >= totalAnimFrames) {
                currentAnimFrame = 0; 
            }
            lastFrameTime_anim = currentTime;
        }
    }

    // Metode default untuk mengupdate posisi collision box agar sama dengan posisi objek.
    // Metode ini di-override oleh kelas turunan seperti Player dan Ghost.
    protected void updateCollisionBox() {
        this.collisionBox.x = (int)x;
        this.collisionBox.y = (int)y;
        this.collisionBox.width = this.width;
        this.collisionBox.height = this.height;
    }

    // Metode untuk menggambar objek ke layar.
    public void render(Graphics g) {
        // Jika objek beranimasi, gambar frame saat ini dari sprite sheet.
        if (animated && spriteSheet != null) {
            int sx1_src = currentAnimFrame * frameWidth_sprite;
            int sy1_src = 0; 
            int sx2_src = sx1_src + frameWidth_sprite;
            int sy2_src = frameHeight_sprite;

            // Logika untuk membalik gambar jika menghadap ke kiri.
            if (!isFacingRight_anim) {
                int temp = sx1_src;
                sx1_src = sx2_src;
                sx2_src = temp;
            }

            // Gambar bagian dari sprite sheet ke layar.
            g.drawImage(spriteSheet, 
                        (int)x, (int)y, (int)x + width, (int)y + height, 
                        sx1_src, sy1_src, sx2_src, sy2_src, 
                        null);
        } else if (image != null) { // Jika objek tidak beranimasi, gambar image statisnya.
             g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            // Fallback: jika tidak ada gambar sama sekali, gambar kotak magenta untuk debugging.
            Color defaultColor = g.getColor();
            g.setColor(Color.MAGENTA); 
            g.fillRect((int)x, (int)y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect((int)x, (int)y, width, height);
            g.setColor(defaultColor);
        }

        // Jika mode debug aktif, gambar kotak collision box (hitbox) transparan.
        if (DEBUG_DRAW_HITBOX && collisionBox != null) {
            Graphics2D g2d = (Graphics2D) g.create(); 
            g2d.setColor(new Color(255, 0, 0, 80)); 
            g2d.fillRect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            g2d.setColor(Color.RED); 
            g2d.drawRect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            g2d.dispose();
        }
    }

    // Kumpulan getter dan setter untuk properti dasar objek.
    public Rectangle getCollisionBox() { return collisionBox; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // Metode abstrak 'update' yang WAJIB diimplementasikan oleh semua kelas turunan.
    // Ini mendefinisikan bagaimana setiap objek berperilaku di setiap frame.
    public abstract void update();
}