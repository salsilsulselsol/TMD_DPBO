package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import viewmodel.Constants;

public class Player extends GameObject {
    private float speed = 4.5f;
    private int hearts;
    private boolean moveLeft, moveRight, moveUp, moveDown;

    // Kumpulan sprite sheet untuk berbagai kondisi
    private BufferedImage idleSpriteSheet;
    private BufferedImage swimmingSpriteSheet;
    private BufferedImage hurtSpriteSheet; // Sprite sheet baru untuk animasi terluka
    private BufferedImage currentSpriteSheet;

    private int frameWidth;
    private int frameHeight;
    
    // Jumlah frame untuk setiap animasi
    private int totalIdleFrames;
    private int totalSwimmingFrames;
    private int totalHurtFrames; // Jumlah frame baru
    private int currentAnimationTotalFrames;

    private int currentAnimFrame;
    private long lastFrameTime;
    private int frameDelayMs;
    private int hurtFrameDelayMs; // Kecepatan animasi terluka mungkin berbeda

    private boolean isMoving;
    private boolean isFacingRight = true;
    private boolean isImmune = false; // Flag untuk menandakan player sedang dalam animasi hurt

    // Enum untuk state animasi, ditambah HURT dan dihapus SHOOTING
    private enum AnimationState {
        IDLE,
        SWIMMING,
        HURT 
    }
    private AnimationState currentAnimationState;

    // Konstruktor disesuaikan: hapus shoot, tambah hurt
    public Player(float x, float y, int renderWidth, int renderHeight, int initialHearts,
                  String idleSheetPath, String swimmingSheetPath, String hurtSheetPath,
                  int spriteFrameW, int spriteFrameH,
                  int idleFrames, int swimmingFrames, int hurtFrames,
                  int frameDelay, int hurtFrameDelay) {
        
        super(x, y, renderWidth, renderHeight);
        this.hearts = initialHearts;
        
        this.frameWidth = spriteFrameW;
        this.frameHeight = spriteFrameH;

        this.totalIdleFrames = idleFrames;
        this.totalSwimmingFrames = swimmingFrames;
        this.totalHurtFrames = hurtFrames;
        this.frameDelayMs = frameDelay;
        this.hurtFrameDelayMs = hurtFrameDelay;
        
        this.currentAnimFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isMoving = false;
        this.isFacingRight = true;

        loadSpriteSheets(idleSheetPath, swimmingSheetPath, hurtSheetPath);
        
        setAnimationState(AnimationState.IDLE);
        resetMovementFlags();
    }

    // Metode load disesuaikan: hapus shoot, tambah hurt
    private void loadSpriteSheets(String idlePath, String swimmingPath, String hurtPath) {
        try {
            URL idleUrl = getClass().getResource(idlePath);
            if (idleUrl != null) this.idleSpriteSheet = ImageIO.read(idleUrl);
            else System.err.println("Gagal memuat idle sprite sheet: " + idlePath);

            URL swimmingUrl = getClass().getResource(swimmingPath);
            if (swimmingUrl != null) this.swimmingSpriteSheet = ImageIO.read(swimmingUrl);
            else System.err.println("Gagal memuat swimming sprite sheet: " + swimmingPath);

            URL hurtUrl = getClass().getResource(hurtPath);
            if (hurtUrl != null) this.hurtSpriteSheet = ImageIO.read(hurtUrl);
            else System.err.println("Gagal memuat hurt sprite sheet: " + hurtPath);

        } catch (Exception e) {
            System.err.println("Error saat memuat sprite sheets: " + e.getMessage());
        }
    }

    // Metode set state disesuaikan: hapus shoot, tambah hurt
    private void setAnimationState(AnimationState newState) {
        if (this.currentAnimationState == newState) return;
        
        this.currentAnimationState = newState;
        this.currentAnimFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();

        switch (newState) {
            case IDLE:
                currentSpriteSheet = idleSpriteSheet;
                currentAnimationTotalFrames = totalIdleFrames;
                break;
            case SWIMMING:
                currentSpriteSheet = swimmingSpriteSheet;
                currentAnimationTotalFrames = totalSwimmingFrames;
                break;
            case HURT:
                currentSpriteSheet = hurtSpriteSheet;
                currentAnimationTotalFrames = totalHurtFrames;
                break;
        }
    }

    // Logika update animasi disesuaikan untuk menangani state HURT
    private void updateAnimationLogic() {
        if (currentSpriteSheet == null || currentAnimationTotalFrames == 0) return;

        long currentTime = System.currentTimeMillis();
        int currentFrameDelay = (currentAnimationState == AnimationState.HURT) ? hurtFrameDelayMs : frameDelayMs;

        if (currentTime - lastFrameTime > currentFrameDelay) {
            currentAnimFrame++;
            if (currentAnimFrame >= currentAnimationTotalFrames) {
                if (currentAnimationState == AnimationState.HURT) {
                    // Jika animasi HURT selesai, kembali ke state normal
                    finishHurtAnimation();
                } else {
                    currentAnimFrame = 0;
                }
            }
            lastFrameTime = currentTime;
        }
    }
    
    @Override
    public void update() {
        // Jika sedang dalam animasi 'terluka', jangan proses gerakan atau perubahan state lain
        if (isImmune) {
            updateAnimationLogic();
            return;
        }

        float dx = 0, dy = 0;
        isMoving = moveLeft || moveRight || moveUp || moveDown;

        if (moveLeft) { dx -= speed; isFacingRight = false; }
        if (moveRight) { dx += speed; isFacingRight = true; }
        if (moveUp) { dy -= speed; }
        if (moveDown) { dy += speed; }
        
        // Atur animasi berdasarkan gerakan
        if (isMoving && currentAnimationState != AnimationState.SWIMMING) {
            setAnimationState(AnimationState.SWIMMING);
        } else if (!isMoving && currentAnimationState != AnimationState.IDLE) {
            setAnimationState(AnimationState.IDLE);
        }
        
        x += dx;
        y += dy;

        // Batasan layar
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + this.width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - this.width;
        if (y + this.height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - this.height;

        updateCollisionBox();
        updateAnimationLogic();
    }

    // Metode baru untuk memutar animasi HURT
    public void playHurtAnimation() {
        if (!isImmune) {
            isImmune = true; // Set flag agar tidak bisa diganggu
            setAnimationState(AnimationState.HURT);
        }
    }

    // Metode baru yang dipanggil setelah animasi HURT selesai
    private void finishHurtAnimation() {
        isImmune = false; // Matikan flag
        // Kembali ke animasi idle/swimming tergantung status gerakan terakhir
        if (isMoving) {
            setAnimationState(AnimationState.SWIMMING);
        } else {
            setAnimationState(AnimationState.IDLE);
        }
    }
    
    // Metode render tidak perlu banyak diubah
    @Override
    public void render(Graphics g) {
        if (currentSpriteSheet != null && currentAnimationTotalFrames > 0) {
            int sx1 = currentAnimFrame * this.frameWidth;
            int sy1 = 0;
            int sx2 = sx1 + this.frameWidth;
            int sy2 = this.frameHeight;

            int dx1 = (int)x;
            int dy1 = (int)y;
            int dx2 = (int)x + this.width;
            int dy2 = (int)y + this.height;

            if (!isFacingRight) {
                int tempSx1 = sx1;
                sx1 = sx2;
                sx2 = tempSx1;
            }
            g.drawImage(currentSpriteSheet, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillRect((int)x, (int)y, this.width, this.height);
        }
    }

    public void resetMovementFlags() {
        setMoveUp(false);
        setMoveDown(false);
        setMoveLeft(false);
        setMoveRight(false);
    }

    public void loseHeart() { if (hearts > 0) hearts--; }
    public int getHearts() { return hearts; }
    
    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void setMoveUp(boolean b) { moveUp = b; }
    public void setMoveDown(boolean b) { moveDown = b; }
}