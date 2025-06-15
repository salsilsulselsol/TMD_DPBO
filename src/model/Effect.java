package model;

// Kelas ini merepresentasikan efek visual sementara yang akan hilang setelah animasinya selesai.
// Contoh: efek 'hit' saat pemain terluka.
public class Effect extends GameObject {
    
    // Flag untuk menandakan apakah animasi efek ini sudah selesai atau belum.
    private boolean isFinished = false;

    // Konstruktor untuk membuat objek efek visual.
    public Effect(float x, float y, int renderWidth, int renderHeight, 
                  String spriteSheetPath, int frameW, int frameH, int totalFrames, int frameDelay) {
        
        // Panggil konstruktor parent dan muat sprite sheet untuk animasi.
        super(x, y, renderWidth, renderHeight);
        loadSpriteSheet(spriteSheetPath, frameW, frameH, totalFrames, frameDelay);
    }

    // Metode update utama untuk efek, dipanggil setiap frame.
    @Override
    public void update() {
        // Efek visual tidak bergerak, jadi hanya perlu mengupdate animasinya.
        updateAnimation();
    }
    
    // Override metode updateAnimation dari GameObject agar tidak looping (berulang).
    // Animasi akan berhenti di frame terakhir dan menandai dirinya sebagai selesai.
    @Override
    protected void updateAnimation() {
        // Jika animasi sudah selesai atau tidak ada, hentikan proses.
        if (!animated || spriteSheet == null || totalAnimFrames == 0 || isFinished) return;

        long currentTime = System.currentTimeMillis();
        // Cek apakah sudah waktunya untuk pindah ke frame berikutnya.
        if (currentTime - lastFrameTime_anim > frameDelayMs_anim) {
            lastFrameTime_anim = currentTime;
            currentAnimFrame++;
            
            // Jika sudah mencapai frame terakhir...
            if (currentAnimFrame >= totalAnimFrames) {
                // Jangan kembali ke frame 0, tapi tandai sebagai selesai.
                isFinished = true;
                // Atur ukuran menjadi 0 agar efek langsung hilang dari layar.
                this.width = 0; 
                this.height = 0;
            }
        }
    }

    // Metode untuk mengecek apakah animasi efek ini sudah selesai.
    // Digunakan oleh GameLogic untuk menghapus efek dari daftar.
    public boolean isFinished() {
        return isFinished;
    }
}