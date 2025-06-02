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

    private int frameWidth;     // Ini adalah LEBAR SATU FRAME dari sprite sheet (misal: 128)
    private int frameHeight;    // Ini adalah TINGGI SATU FRAME dari sprite sheet (misal: 128)
    
    private int currentAnimFrame;
    private int totalIdleFrames;
    private int totalSwimmingFrames;
    private int totalShootFrames;       
    private int currentAnimationTotalFrames;

    private long lastFrameTime;
    private int frameDelayMs; 
    private int shootFrameDelayMs; 

    private boolean isMoving;
    private boolean isFacingRight = true; // Untuk arah hadap player saat render

    private enum AnimationState {
        IDLE,
        SWIMMING,
        SHOOTING
    }
    private AnimationState currentAnimationState;


    public Player(float x, float y, int renderWidth, int renderHeight, int initialHearts,
                  String idleSheetPath, String swimmingSheetPath, String shootSheetPath, 
                  int spriteFrameW, int spriteFrameH, // Mengganti nama parameter agar jelas ini ukuran frame sprite
                  int idleFrames, int swimmingFrames, int shootFrames, 
                  int frameDelay, int shootFrameDelay) { 
        
        super(x, y, renderWidth, renderHeight); // renderWidth & renderHeight adalah ukuran player di layar
        this.hearts = initialHearts;
        
        // this.width dan this.height (dari GameObject) adalah ukuran render di layar
        // this.frameWidth dan this.frameHeight di sini adalah ukuran frame dari sprite sheet
        this.frameWidth = spriteFrameW; 
        this.frameHeight = spriteFrameH; 

        this.totalIdleFrames = idleFrames;
        this.totalSwimmingFrames = swimmingFrames;
        this.totalShootFrames = shootFrames; 
        this.frameDelayMs = frameDelay; 
        this.shootFrameDelayMs = shootFrameDelay; 
        
        this.currentAnimFrame = 0;
        this.lastFrameTime = System.currentTimeMillis();
        this.isMoving = false; // isMoving akan di-update di metode update()
        this.isFacingRight = true; // Arah hadap player default

        loadSpriteSheets(idleSheetPath, swimmingSheetPath, shootSheetPath); 
        
        setAnimationState(AnimationState.IDLE); 
        resetMovementFlags(); // Panggil di konstruktor untuk state awal yang bersih
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
            // Izinkan animasi SHOOTING dipicu ulang untuk reset frame
            if (newState == AnimationState.SHOOTING) {
                 currentAnimFrame = 0;
                 lastFrameTime = System.currentTimeMillis();
            }
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

    private void updateAnimationLogic() {
        if (currentSpriteSheet == null || currentAnimationTotalFrames == 0) return;

        long currentTime = System.currentTimeMillis();
        int currentFrameDelay = (currentAnimationState == AnimationState.SHOOTING) ? shootFrameDelayMs : frameDelayMs;

        if (currentTime - lastFrameTime > currentFrameDelay) { 
            currentAnimFrame++;
            if (currentAnimFrame >= currentAnimationTotalFrames) {
                if (currentAnimationState == AnimationState.SHOOTING) {
                    currentAnimFrame = 0; // Atau bisa juga currentAnimFrame = totalShootFrames - 1; jika ingin berhenti di frame terakhir
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
        // Tentukan apakah ada input gerakan AKTIF pada frame ini untuk mengupdate isMoving
        // isMoving di-set berdasarkan flag moveLeft/Right/Up/Down
        isMoving = moveLeft || moveRight || moveUp || moveDown;


        if (moveLeft) { dx -= speed; isFacingRight = false; }
        if (moveRight) { dx += speed; isFacingRight = true; }
        if (moveUp) { dy -= speed; }
        if (moveDown) { dy += speed; }
        
        // Hanya ubah animasi ke IDLE/SWIMMING jika tidak sedang SHOOTING
        if (currentAnimationState != AnimationState.SHOOTING) {
            if (isMoving && currentAnimationState != AnimationState.SWIMMING) {
                setAnimationState(AnimationState.SWIMMING);
            } else if (!isMoving && currentAnimationState != AnimationState.IDLE) {
                setAnimationState(AnimationState.IDLE);
            }
        }
        
        x += dx;
        y += dy;

        // Batasan layar (menggunakan this.width dan this.height dari GameObject untuk ukuran render)
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + this.width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - this.width;  
        if (y + this.height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - this.height;  

        updateCollisionBox(); // Menggunakan this.width dan this.height (ukuran render) untuk collision box
        updateAnimationLogic(); 
    }

    public void playShootAnimation() {
        setAnimationState(AnimationState.SHOOTING);
    }

    private void finishShootAnimation() {
        // Setelah animasi shoot selesai, kembali ke idle atau swimming berdasarkan status isMoving
        // isMoving sudah diupdate di awal metode update() berdasarkan flag moveLeft/Right/Up/Down
        if (isMoving) { 
            setAnimationState(AnimationState.SWIMMING);
        } else {
            setAnimationState(AnimationState.IDLE);
        }
    }
    
    @Override
    public void render(Graphics g) {
        if (currentSpriteSheet != null && currentAnimationTotalFrames > 0) {
            // sx1, sy1, sx2, sy2 adalah koordinat SUMBER dari sprite sheet
            // Menggunakan this.frameWidth dan this.frameHeight (ukuran satu frame di sprite)
            int sx1 = currentAnimFrame * this.frameWidth; 
            int sy1 = 0; // Asumsi semua frame dalam satu baris
            int sx2 = sx1 + this.frameWidth; 
            int sy2 = this.frameHeight; 

            // dx1, dy1, dx2, dy2 adalah koordinat TUJUAN di layar
            // Menggunakan this.width dan this.height (ukuran render objek di layar)
            int dx1 = (int)x;
            int dy1 = (int)y;
            int dx2 = (int)x + this.width; 
            int dy2 = (int)y + this.height;

            if (!isFacingRight) { // isFacingRight milik Player untuk arah hadap visual
                // Tukar koordinat sumber untuk membalik gambar secara horizontal
                int tempSx1 = sx1;
                sx1 = sx2;
                sx2 = tempSx1;
            }
            g.drawImage(currentSpriteSheet, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        } else if (image != null) { // Fallback jika tidak ada sprite sheet
             g.drawImage(image, (int)x, (int)y, this.width, this.height, null);
        } else { // Fallback jika tidak ada gambar sama sekali
            g.setColor(Color.YELLOW); 
            g.fillRect((int)x, (int)y, this.width, this.height);
        }
    }

    public void resetMovementFlags() {
        setMoveUp(false);
        setMoveDown(false);
        setMoveLeft(false);
        setMoveRight(false);
        // isMoving akan terupdate otomatis di awal metode update() berdasarkan flag ini
    }

    public void loseHeart() { if (hearts > 0) hearts--; }
    public int getHearts() { return hearts; }
    
    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void setMoveUp(boolean b) { moveUp = b; }
    public void setMoveDown(boolean b) { moveDown = b; }
}