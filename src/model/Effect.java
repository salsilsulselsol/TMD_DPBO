package model;

/**
 * Kelas ini merepresentasikan efek visual sementara yang akan hilang 
 * setelah animasinya selesai diputar satu kali.
 */
public class Effect extends GameObject {
    
    private boolean isFinished = false;

    public Effect(float x, float y, int renderWidth, int renderHeight, 
                  String spriteSheetPath, int frameW, int frameH, int totalFrames, int frameDelay) {
        
        super(x, y, renderWidth, renderHeight);
        loadSpriteSheet(spriteSheetPath, frameW, frameH, totalFrames, frameDelay);
    }

    @Override
    public void update() {
        // Hanya perlu mengupdate animasi, tidak ada pergerakan.
        updateAnimation();
    }
    
    /**
     * Override metode updateAnimation dari GameObject agar tidak looping.
     * Animasi akan berhenti di frame terakhir dan menandai dirinya sebagai selesai.
     */
    @Override
    protected void updateAnimation() {
        if (!animated || spriteSheet == null || totalAnimFrames == 0 || isFinished) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastFrameTime_anim > frameDelayMs_anim) {
            lastFrameTime_anim = currentTime;
            currentAnimFrame++;
            
            if (currentAnimFrame >= totalAnimFrames) {
                // Jangan kembali ke frame 0, cukup tandai sebagai selesai.
                isFinished = true;
                // Kita buat tidak terlihat agar langsung hilang dari layar
                this.width = 0; 
                this.height = 0;
            }
        }
    }

    /**
     * Cek apakah animasi efek ini sudah selesai.
     * @return true jika sudah selesai, false jika belum.
     */
    public boolean isFinished() {
        return isFinished;
    }
}