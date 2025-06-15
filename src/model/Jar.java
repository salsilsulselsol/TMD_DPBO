package model;

import java.awt.Color;
import java.awt.Graphics;

// Kelas ini merepresentasikan objek keranjang (Jar) yang menampung skor dan jumlah ikan.
public class Jar extends GameObject {
    // Jumlah ikan yang berhasil dikumpulkan.
    private int collectedCount;
    // Total skor yang diperoleh.
    private int totalScore;

    // Konstruktor untuk membuat objek Jar.
    public Jar(float x, float y, int width, int height, String imagePath) {
        // Panggil konstruktor parent untuk mengatur posisi dan ukuran.
        super(x, y, width, height);
        // Reset skor dan hitungan awal ke 0.
        this.collectedCount = 0;
        this.totalScore = 0;
        // Muat gambar untuk keranjang.
        loadImage(imagePath); 
    }

    // Metode untuk menambahkan ikan ke dalam keranjang.
    public void addToJar(Fish fish) {
        // Pastikan objek ikan tidak null sebelum memproses.
        if (fish != null) {
            this.collectedCount++; // Tambah jumlah ikan yang ditangkap.
            this.totalScore += fish.getScoreValue(); // Tambah skor total.
        }
    }

    // Override metode update, tapi dibiarkan kosong karena keranjang adalah objek statis.
    @Override
    public void update() { /* Jar tidak bergerak atau berubah, jadi tidak ada logika update. */ }

    // Metode untuk menggambar keranjang ke layar.
    @Override
    public void render(Graphics g) {
        // Jika gambar berhasil dimuat, gambar keranjangnya.
        if (image != null) {
            g.drawImage(image, (int)x, (int)y, width, height, null);
        } else {
            // Jika gambar gagal dimuat, gambar kotak coklat sebagai pengganti.
            g.setColor(new Color(139, 69, 19)); 
            g.fillRect((int)x, (int)y, width, height);
        }
    }

    // Getter untuk mendapatkan jumlah ikan yang terkumpul.
    public int getCollectedCount() { return collectedCount; }
    // Getter untuk mendapatkan total skor.
    public int getTotalScore() { return totalScore; }
    // Metode untuk mereset skor dan hitungan ke nol (saat memulai game baru).
    public void reset() {
        this.collectedCount = 0;
        this.totalScore = 0;
    }
}