package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage; // Lebih baik untuk memotong sprite sheet
import java.net.URL;
import javax.imageio.ImageIO;
import viewmodel.Constants;

public class Player extends GameObject {
    private float speed = 4.5f;
    private int hearts;
    private boolean moveLeft, moveRight, moveUp, moveDown;

    // Properti untuk Animasi Sprite Sheet
    private BufferedImage idleSpriteSheet;
    private BufferedImage swimmingSpriteSheet;
    private BufferedImage currentSpriteSheet; // Sprite sheet yang aktif saat ini

    private int frameWidth;        // Lebar satu frame animasi (asumsikan sama untuk idle & swimming)
    private int frameHeight;       // Tinggi satu frame animasi (asumsikan sama)
    
    private int currentAnimFrame;  // Frame animasi yang sedang ditampilkan (indeks kolom)
    private int totalIdleFrames;
    private int totalSwimmingFrames;
    private int currentAnimationTotalFrames; // Jumlah frame untuk animasi saat ini

    private long lastFrameTime;
    private int frameDelayMs;       // Durasi tampilan per frame (dalam milidetik)

    private boolean isMoving;       // Status apakah player sedang bergerak

    public Player(float x, float y, int renderWidth, int renderHeight, int initialHearts,
                  String idleSheetPath, String swimmingSheetPath,
                  int frameW, int frameH, 
                  int idleFrames, int swimmingFrames, int frameDelay) {
        
        super(x, y, renderWidth, renderHeight); // renderWidth/Height adalah ukuran player di layar
        this.hearts = initialHearts;
        
        this.frameWidth = frameW;
        this.frameHeight = frameH;
        this.totalIdleFrames = idleFrames;
        this.totalSwimmingFrames = swimmingFrames;
        this.frameDelayMs = frameDelay;
        
        this.currentAnimFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isMoving = false;

        loadSpriteSheets(idleSheetPath, swimmingSheetPath);
        
        // Set animasi awal ke idle
        setAnimation(false); // false untuk idle
    }

    private void loadSpriteSheets(String idlePath, String swimmingPath) {
        try {
            URL idleUrl = getClass().getResource(idlePath);
            if (idleUrl != null) {
                this.idleSpriteSheet = ImageIO.read(idleUrl);
            } else {
                System.err.println("Gagal memuat idle sprite sheet: " + idlePath);
            }

            URL swimmingUrl = getClass().getResource(swimmingPath);
            if (swimmingUrl != null) {
                this.swimmingSpriteSheet = ImageIO.read(swimmingUrl);
            } else {
                System.err.println("Gagal memuat swimming sprite sheet: " + swimmingPath);
            }
        } catch (Exception e) {
            System.err.println("Error saat memuat sprite sheets: " + e.getMessage());
        }
    }

    // Metode untuk mengganti animasi (dan sprite sheet yang digunakan)
    private void setAnimation(boolean moving) {
        this.isMoving = moving;
        if (this.isMoving) {
            if (currentSpriteSheet != swimmingSpriteSheet) { // Hanya ganti jika berbeda
                currentSpriteSheet = swimmingSpriteSheet;
                currentAnimationTotalFrames = totalSwimmingFrames;
                currentAnimFrame = 0; // Reset frame saat ganti animasi
            }
        } else {
            if (currentSpriteSheet != idleSpriteSheet) { // Hanya ganti jika berbeda
                currentSpriteSheet = idleSpriteSheet;
                currentAnimationTotalFrames = totalIdleFrames;
                currentAnimFrame = 0; // Reset frame
            }
        }
    }

    private void updateAnimationLogic() {
        if (currentSpriteSheet == null || currentAnimationTotalFrames == 0) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime > frameDelayMs) {
            currentAnimFrame++;
            if (currentAnimFrame >= currentAnimationTotalFrames) {
                currentAnimFrame = 0; // Kembali ke frame awal
            }
            lastFrameTime = currentTime;
        }
    }

    @Override
    public void update() {
        float dx = 0, dy = 0;
        boolean currentlyMoving = false; // Cek apakah ada input gerakan pada frame ini

        if (moveLeft) { dx -= speed; currentlyMoving = true; }
        if (moveRight) { dx += speed; currentlyMoving = true; }
        if (moveUp) { dy -= speed; currentlyMoving = true; }
        if (moveDown) { dy += speed; currentlyMoving = true; }

        // Ganti animasi jika status bergerak berubah
        if (currentlyMoving && !this.isMoving) {
            setAnimation(true); // Pindah ke animasi swimming
        } else if (!currentlyMoving && this.isMoving) {
            setAnimation(false); // Pindah ke animasi idle
        }
        
        x += dx;
        y += dy;

        // Batasan layar
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - width; // width adalah renderWidth
        if (y + height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - height; // height adalah renderHeight

        updateCollisionBox();
        updateAnimationLogic(); // Selalu update logika frame animasi
    }

    @Override
    public void render(Graphics g) {
        if (currentSpriteSheet != null && currentAnimationTotalFrames > 0) {
            // Asumsi semua animasi ada di baris ke-0 dari masing-masing sprite sheet
            // Jika sprite sheet punya beberapa baris untuk arah berbeda, perlu currentRow
            int sx1 = currentAnimFrame * frameWidth;
            int sy1 = 0; // Baris ke-0 pada sprite sheet
            int sx2 = sx1 + frameWidth;
            int sy2 = frameHeight; // Karena hanya satu baris

            // `width` dan `height` di sini adalah renderWidth dan renderHeight dari konstruktor super()
            g.drawImage(currentSpriteSheet, 
                        (int)x, (int)y, (int)x + width, (int)y + height, 
                        sx1, sy1, sx2, sy2, 
                        null);
        } else if (image != null) { // Fallback ke gambar statis jika ada (dari GameObject)
             g.drawImage(image, (int)x, (int)y, width, height, null);
        }
        else {
            // Fallback jika sprite sheet atau gambar statis gagal dimuat
            g.setColor(Color.YELLOW); 
            g.fillRect((int)x, (int)y, width, height);
        }
    }

    public void loseHeart() { if (hearts > 0) hearts--; }
    public int getHearts() { return hearts; }
    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void setMoveUp(boolean b) { moveUp = b; }
    public void setMoveDown(boolean b) { moveDown = b; }
}