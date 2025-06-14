package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D; // MODIFIKASI: Tambahkan import ini
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import viewmodel.Constants;

public class Player extends GameObject {
    private float speed = 4.5f;
    private int hearts;
    private boolean moveLeft, moveRight, moveUp, moveDown;

    // Kumpulan sprite sheet
    private BufferedImage idleSpriteSheet;
    private BufferedImage swimmingSpriteSheet;
    private BufferedImage hurtSpriteSheet;
    private BufferedImage currentSpriteSheet;

    // Properti animasi
    private int frameWidth;
    private int frameHeight;
    private int totalIdleFrames;
    private int totalSwimmingFrames;
    private int totalHurtFrames;
    private int currentAnimationTotalFrames;
    private int currentAnimFrame;
    private long lastFrameTime;
    private int frameDelayMs;
    private int hurtFrameDelayMs;

    // Status player
    private boolean isMoving;
    private boolean isFacingRight = true;
    private boolean isImmune = false;

    private enum AnimationState { IDLE, SWIMMING, HURT }
    private AnimationState currentAnimationState;

    // Faktor skala untuk hitbox Player
    private static final float HITBOX_SCALE_X = 0.4f;
    private static final float HITBOX_SCALE_Y = 0.6f;

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
        updateCollisionBox();
    }

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

    private void updateAnimationLogic() {
        if (currentSpriteSheet == null || currentAnimationTotalFrames == 0) return;

        long currentTime = System.currentTimeMillis();
        int currentFrameDelay = (currentAnimationState == AnimationState.HURT) ? hurtFrameDelayMs : frameDelayMs;

        if (currentTime - lastFrameTime > currentFrameDelay) {
            currentAnimFrame++;
            if (currentAnimFrame >= currentAnimationTotalFrames) {
                if (currentAnimationState == AnimationState.HURT) {
                    finishHurtAnimation();
                } else {
                    currentAnimFrame = 0;
                }
            }
            lastFrameTime = currentTime;
        }
    }
    
    @Override
    protected void updateCollisionBox() {
        int hitboxWidth = (int) (this.width * HITBOX_SCALE_X);
        int hitboxHeight = (int) (this.height * HITBOX_SCALE_Y);
        int offsetX = (this.width - hitboxWidth) / 2;
        int offsetY = (this.height - hitboxHeight) / 2;

        if (this.collisionBox == null) {
            this.collisionBox = new Rectangle((int)this.x + offsetX, (int)this.y + offsetY, hitboxWidth, hitboxHeight);
        } else {
            this.collisionBox.x = (int)this.x + offsetX;
            this.collisionBox.y = (int)this.y + offsetY;
            this.collisionBox.width = hitboxWidth;
            this.collisionBox.height = hitboxHeight;
        }
    }

    @Override
    public void update() {
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
        
        if (isMoving && currentAnimationState != AnimationState.SWIMMING) {
            setAnimationState(AnimationState.SWIMMING);
        } else if (!isMoving && currentAnimationState != AnimationState.IDLE) {
            setAnimationState(AnimationState.IDLE);
        }
        
        x += dx;
        y += dy;

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + this.width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - this.width;
        if (y + this.height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - this.height;

        updateCollisionBox();
        updateAnimationLogic();
    }

    public void playHurtAnimation() {
        if (!isImmune) {
            isImmune = true;
            setAnimationState(AnimationState.HURT);
        }
    }

    private void finishHurtAnimation() {
        isImmune = false;
        if (isMoving) {
            setAnimationState(AnimationState.SWIMMING);
        } else {
            setAnimationState(AnimationState.IDLE);
        }
    }
    
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
        
        // --- MODIFIKASI DIMULAI ---
        // Hapus `super.render(g);` dan ganti dengan kode untuk menggambar hitbox secara langsung.
        if (DEBUG_DRAW_HITBOX && collisionBox != null) {
            Graphics2D g2d = (Graphics2D) g.create(); 
            g2d.setColor(new Color(255, 0, 0, 80)); 
            g2d.fillRect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            g2d.setColor(Color.RED); 
            g2d.drawRect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            g2d.dispose();
        }
        // --- MODIFIKASI SELESAI ---
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