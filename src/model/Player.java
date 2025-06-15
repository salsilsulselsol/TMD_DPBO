package model;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.net.URL;
import javax.imageio.ImageIO;
import viewmodel.Constants;

// Kelas ini merepresentasikan objek pemain yang dikontrol oleh pengguna.
public class Player extends GameObject {
    // Properti dasar pemain.
    private float speed = 4.5f;
    private int hearts;
    // Flag untuk menandakan arah gerakan (dikontrol oleh InputHandler).
    private boolean moveLeft, moveRight, moveUp, moveDown;

    // Kumpulan sprite sheet untuk setiap kondisi animasi.
    private BufferedImage idleSpriteSheet;
    private BufferedImage swimmingSpriteSheet;
    private BufferedImage hurtSpriteSheet;
    private BufferedImage currentSpriteSheet; // Sprite sheet yang sedang aktif.

    // Properti untuk mengelola logika animasi.
    private int frameWidth;
    private int frameHeight;
    private int totalIdleFrames;
    private int totalSwimmingFrames;
    private int totalHurtFrames;
    private int currentAnimationTotalFrames;
    private int currentAnimFrame; // Frame saat ini yang sedang ditampilkan.
    private long lastFrameTime;
    private int frameDelayMs;
    private int hurtFrameDelayMs;

    // Variabel untuk status internal pemain.
    private boolean isMoving;
    private boolean isFacingRight = true;
    private boolean isImmune = false; // Status kebal sesaat setelah terluka.

    // Enum untuk mengelola state animasi pemain.
    private enum AnimationState { IDLE, SWIMMING, HURT }
    private AnimationState currentAnimationState;

    // Faktor skala untuk hitbox Player agar lebih presisi.
    private static final float HITBOX_SCALE_X = 0.4f;
    private static final float HITBOX_SCALE_Y = 0.6f;

    // Konstruktor untuk membuat objek Player.
    public Player(float x, float y, int renderWidth, int renderHeight, int initialHearts,
                  String idleSheetPath, String swimmingSheetPath, String hurtSheetPath,
                  int spriteFrameW, int spriteFrameH,
                  int idleFrames, int swimmingFrames, int hurtFrames,
                  int frameDelay, int hurtFrameDelay) {
        
        super(x, y, renderWidth, renderHeight); // Panggil konstruktor parent.
        this.hearts = initialHearts;
        
        // Inisialisasi semua properti animasi dari parameter.
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

        // Memuat semua gambar sprite sheet yang dibutuhkan.
        loadSpriteSheets(idleSheetPath, swimmingSheetPath, hurtSheetPath);
        
        // Mengatur state awal pemain.
        setAnimationState(AnimationState.IDLE);
        resetMovementFlags();
        updateCollisionBox();
    }

    // Metode internal untuk memuat semua file gambar sprite sheet.
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

    // Mengganti state animasi pemain.
    private void setAnimationState(AnimationState newState) {
        // Hindari reset animasi jika state-nya sama.
        if (this.currentAnimationState == newState) return;
        
        this.currentAnimationState = newState;
        this.currentAnimFrame = 0; // Reset frame ke awal.
        this.lastFrameTime = System.currentTimeMillis();

        // Pilih sprite sheet yang sesuai dengan state baru.
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

    // Mengupdate frame animasi berdasarkan waktu.
    private void updateAnimationLogic() {
        if (currentSpriteSheet == null || currentAnimationTotalFrames == 0) return;

        long currentTime = System.currentTimeMillis();
        // Gunakan delay yang berbeda untuk animasi 'terluka'.
        int currentFrameDelay = (currentAnimationState == AnimationState.HURT) ? hurtFrameDelayMs : frameDelayMs;

        // Pindah ke frame berikutnya jika waktu delay sudah terlewati.
        if (currentTime - lastFrameTime > currentFrameDelay) {
            currentAnimFrame++;
            if (currentAnimFrame >= currentAnimationTotalFrames) {
                // Jika animasi 'terluka' selesai, kembali ke state normal.
                if (currentAnimationState == AnimationState.HURT) {
                    finishHurtAnimation();
                } else {
                    // Untuk animasi lain, looping kembali ke frame 0.
                    currentAnimFrame = 0;
                }
            }
            lastFrameTime = currentTime;
        }
    }
    
    // Override metode untuk membuat hitbox Player lebih kecil dan presisi.
    @Override
    protected void updateCollisionBox() {
        // Hitung lebar, tinggi, dan posisi baru untuk hitbox.
        int hitboxWidth = (int) (this.width * HITBOX_SCALE_X);
        int hitboxHeight = (int) (this.height * HITBOX_SCALE_Y);
        int offsetX = (this.width - hitboxWidth) / 2;
        int offsetY = (this.height - hitboxHeight) / 2;

        // Atur ulang collision box dengan nilai yang baru.
        if (this.collisionBox == null) {
            this.collisionBox = new Rectangle((int)this.x + offsetX, (int)this.y + offsetY, hitboxWidth, hitboxHeight);
        } else {
            this.collisionBox.x = (int)this.x + offsetX;
            this.collisionBox.y = (int)this.y + offsetY;
            this.collisionBox.width = hitboxWidth;
            this.collisionBox.height = hitboxHeight;
        }
    }

    // Metode update utama untuk pemain, dipanggil setiap frame.
    @Override
    public void update() {
        // Jika sedang kebal (animasi terluka), jangan proses gerakan.
        if (isImmune) {
            updateAnimationLogic();
            return;
        }

        // Hitung perubahan posisi berdasarkan flag gerakan.
        float dx = 0, dy = 0;
        isMoving = moveLeft || moveRight || moveUp || moveDown;

        if (moveLeft) { dx -= speed; isFacingRight = false; }
        if (moveRight) { dx += speed; isFacingRight = true; }
        if (moveUp) { dy -= speed; }
        if (moveDown) { dy += speed; }
        
        // Tentukan state animasi berdasarkan status gerakan.
        if (isMoving && currentAnimationState != AnimationState.SWIMMING) {
            setAnimationState(AnimationState.SWIMMING);
        } else if (!isMoving && currentAnimationState != AnimationState.IDLE) {
            setAnimationState(AnimationState.IDLE);
        }
        
        // Update posisi pemain.
        x += dx;
        y += dy;

        // Jaga agar pemain tidak keluar dari batas layar.
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + this.width > Constants.GAME_WIDTH) x = Constants.GAME_WIDTH - this.width;
        if (y + this.height > Constants.GAME_HEIGHT) y = Constants.GAME_HEIGHT - this.height;

        // Panggil update lainnya di akhir.
        updateCollisionBox();
        updateAnimationLogic();
    }

    // Memulai animasi 'terluka' dan membuat pemain kebal sementara.
    public void playHurtAnimation() {
        if (!isImmune) {
            isImmune = true;
            setAnimationState(AnimationState.HURT);
        }
    }

    // Dipanggil setelah animasi 'terluka' selesai.
    private void finishHurtAnimation() {
        isImmune = false; // Matikan status kebal.
        // Kembali ke animasi idle atau swimming tergantung kondisi.
        if (isMoving) {
            setAnimationState(AnimationState.SWIMMING);
        } else {
            setAnimationState(AnimationState.IDLE);
        }
    }
    
    // Metode untuk menggambar pemain ke layar.
    @Override
    public void render(Graphics g) {
        if (currentSpriteSheet != null && currentAnimationTotalFrames > 0) {
            // Tentukan area sumber (sx) dari sprite sheet yang akan digambar.
            int sx1 = currentAnimFrame * this.frameWidth;
            int sy1 = 0;
            int sx2 = sx1 + this.frameWidth;
            int sy2 = this.frameHeight;

            // Tentukan area tujuan (dx) di layar.
            int dx1 = (int)x;
            int dy1 = (int)y;
            int dx2 = (int)x + this.width;
            int dy2 = (int)y + this.height;

            // Jika pemain menghadap ke kiri, balik gambar secara horizontal.
            if (!isFacingRight) {
                int tempSx1 = sx1;
                sx1 = sx2;
                sx2 = tempSx1;
            }
            g.drawImage(currentSpriteSheet, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        } else {
            // Fallback jika gambar gagal dimuat.
            g.setColor(Color.YELLOW);
            g.fillRect((int)x, (int)y, this.width, this.height);
        }
        
        // Kode untuk menggambar hitbox secara langsung saat mode debug aktif.
        if (DEBUG_DRAW_HITBOX && collisionBox != null) {
            Graphics2D g2d = (Graphics2D) g.create(); 
            g2d.setColor(new Color(255, 0, 0, 80)); 
            g2d.fillRect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            g2d.setColor(Color.RED); 
            g2d.drawRect(collisionBox.x, collisionBox.y, collisionBox.width, collisionBox.height);
            g2d.dispose();
        }
    }

    // Mereset semua flag gerakan menjadi false.
    public void resetMovementFlags() {
        setMoveUp(false);
        setMoveDown(false);
        setMoveLeft(false);
        setMoveRight(false);
    }

    // Metode untuk mengurangi nyawa pemain.
    public void loseHeart() { if (hearts > 0) hearts--; }
    // Getter untuk mendapatkan jumlah nyawa saat ini.
    public int getHearts() { return hearts; }
    
    // Kelompok setter untuk flag pergerakan.
    public void setMoveLeft(boolean b) { moveLeft = b; }
    public void setMoveRight(boolean b) { moveRight = b; }
    public void setMoveUp(boolean b) { moveUp = b; }
    public void setMoveDown(boolean b) { moveDown = b; }
}