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

    private BufferedImage idleSpriteSheet;
    private BufferedImage swimmingSpriteSheet;
    private BufferedImage shootSpriteSheet; 
    private BufferedImage currentSpriteSheet;

    private int frameWidth;     
    private int frameHeight;    
    
    private int currentAnimFrame;
    private int totalIdleFrames;
    private int totalSwimmingFrames;
    private int totalShootFrames;       
    private int currentAnimationTotalFrames;

    private long lastFrameTime;
    private int frameDelayMs;
    private int shootFrameDelayMs;

    private boolean isMoving;
    private boolean isFacingRight = true;

    private enum AnimationState {
        IDLE,
        SWIMMING,
        SHOOTING
    }
    private AnimationState currentAnimationState;

    public Player(float x, float y, int renderWidth, int renderHeight, int initialHearts,
                  String idleSheetPath, String swimmingSheetPath, String shootSheetPath, 
                  int frameW, int frameH, 
                  int idleFrames, int swimmingFrames, int shootFrames, 
                  int frameDelay, int shootFrameDelay) {
        
        super(x, y, renderWidth, renderHeight); 
        this.hearts = initialHearts;
        
        this.frameWidth = frameW; 
        this.frameHeight = frameH; 
        this.totalIdleFrames = idleFrames;
        this.totalSwimmingFrames = swimmingFrames;
        this.totalShootFrames = shootFrames; 
        this.frameDelayMs = frameDelay;
        this.shootFrameDelayMs = shootFrameDelay;
        
        this.currentAnimFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isMoving = false;
        this.isFacingRight = true;

        loadSpriteSheets(idleSheetPath, swimmingSheetPath, shootSheetPath); 
        
        setAnimationState(AnimationState.IDLE); 
    }

    private void loadSpriteSheets(String idlePath, String swimmingPath, String shootPath) {
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

            URL shootUrl = getClass().getResource(shootPath); 
            if (shootUrl != null) {
                this.shootSpriteSheet = ImageIO.read(shootUrl);
            } else {
                System.err.println("Gagal memuat shoot sprite sheet: " + shootPath);
            }

        } catch (Exception e) {
            System.err.println("Error saat memuat sprite sheets: " + e.getMessage());
        }
    }

    private void setAnimationState(AnimationState newState) {
        if (this.currentAnimationState == newState && newState != AnimationState.SHOOTING) {
            return;
        }
        
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
            case SHOOTING:
                currentSpriteSheet = shootSpriteSheet;
                currentAnimationTotalFrames = totalShootFrames;
                break;
        }
    }

    private void updateAnimationLogic() { //
        if (currentSpriteSheet == null || currentAnimationTotalFrames == 0) return;

        long currentTime = System.currentTimeMillis();
        int currentFrameDelay = (currentAnimationState == AnimationState.SHOOTING) ? shootFrameDelayMs : frameDelayMs; // Pilih delay yang sesuai

        if (currentTime - lastFrameTime > currentFrameDelay) { // Gunakan currentFrameDelay
            currentAnimFrame++;
            if (currentAnimFrame >= currentAnimationTotalFrames) {
                if (currentAnimationState == AnimationState.SHOOTING) {
                    currentAnimFrame = 0; 
                    finishShootAnimation(); 
                } else {
                    currentAnimFrame = 0; 
                }
            }
            lastFrameTime = currentTime;
        }
    }

    @Override
    public void update() {
        float dx = 0, dy = 0;
        boolean currentlyMoving = false; 

        if (moveLeft) { dx -= speed; currentlyMoving = true; isFacingRight = false; }
        if (moveRight) { dx += speed; currentlyMoving = true; isFacingRight = true; }
        if (moveUp) { dy -= speed; currentlyMoving = true; }
        if (moveDown) { dy += speed; currentlyMoving = true; }
        
        if (currentAnimationState != AnimationState.SHOOTING) {
            if (currentlyMoving && currentAnimationState != AnimationState.SWIMMING) {
                setAnimationState(AnimationState.SWIMMING);
            } else if (!currentlyMoving && currentAnimationState != AnimationState.IDLE) {
                setAnimationState(AnimationState.IDLE);
            }
        }
        
        x += dx;
        y += dy;

        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - width;  //
        if (y + height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - height;  //

        updateCollisionBox();
        updateAnimationLogic(); 
    }

    public void playShootAnimation() {
        setAnimationState(AnimationState.SHOOTING);
    }

    private void finishShootAnimation() {
        if (isMoving) { 
            setAnimationState(AnimationState.SWIMMING);
        } else {
            setAnimationState(AnimationState.IDLE);
        }
    }
    
    @Override
    public void render(Graphics g) { //
        if (currentSpriteSheet != null && currentAnimationTotalFrames > 0) {
            int sx1 = currentAnimFrame * frameWidth;
            int sy1 = 0; 
            int sx2 = sx1 + frameWidth;
            int sy2 = frameHeight; 

            int dx1 = (int)x;
            int dy1 = (int)y;
            int dx2 = (int)x + width; 
            int dy2 = (int)y + height;

            if (!isFacingRight) {
                int tempSx1 = sx1;
                sx1 = sx2;
                sx2 = tempSx1;
            }
            g.drawImage(currentSpriteSheet, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        } else if (image != null) { 
             g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            g.setColor(Color.YELLOW); 
            g.fillRect((int)x, (int)y, width, height);
        }
    }

    public void loseHeart() { if (hearts > 0) hearts--; }
    public int getHearts() { return hearts; } //
    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void setMoveUp(boolean b) { moveUp = b; }
    public void setMoveDown(boolean b) { moveDown = b; }
}