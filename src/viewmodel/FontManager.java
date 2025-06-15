package viewmodel;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

// Kelas ini bertanggung jawab untuk memuat (load) dan menyediakan font kustom untuk seluruh aplikasi.
public class FontManager {
    // Variabel statis untuk menyimpan satu instance dari font yang sudah dimuat.
    private static Font pressStart2PRegular;

    // Blok statis ini berjalan hanya sekali saat kelas pertama kali diakses.
    // Tujuannya adalah untuk memuat file font dari resources ke dalam memori.
    static {
        try {
            String fontPath = "/assets/fonts/PressStart2P-Regular.ttf";
            // Mengambil file font sebagai stream dari dalam classpath.
            InputStream is = FontManager.class.getResourceAsStream(fontPath);
            
            // Jika file font tidak ditemukan, gunakan font default sebagai fallback.
            if (is == null) {
                System.err.println("File font tidak ditemukan di: " + fontPath);
                pressStart2PRegular = new Font("Monospaced", Font.PLAIN, 12); 
            } else {
                // Buat objek Font dari file .ttf.
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
                // Daftarkan font ini ke sistem grafis Java agar bisa digunakan.
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                // Simpan instance font dengan ukuran dasar untuk digunakan nanti.
                pressStart2PRegular = customFont.deriveFont(12f);
                System.out.println("Font PressStart2P-Regular berhasil dimuat dan diregister.");
            }
        // Jika terjadi error saat memuat font (misal file korup), gunakan font default.
        } catch (IOException | FontFormatException e) {
            System.err.println("Error memuat font kustom: " + e.getMessage());
            e.printStackTrace();
            pressStart2PRegular = new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    // Mengembalikan font 'PressStart2P' dengan ukuran yang diinginkan.
    public static Font getPressStart2PRegular(float size) {
        if (pressStart2PRegular != null) {
            // Membuat variasi ukuran dari font yang sudah ada di memori (lebih efisien).
            return pressStart2PRegular.deriveFont(size);
        } else {
            // Fallback jika font utama gagal dimuat.
            return new Font("Monospaced", Font.PLAIN, (int)size);
        }
    }

    // Metode praktis untuk mendapatkan font dengan ukuran default.
    public static Font getPressStart2PRegular() {
        return getPressStart2PRegular(12f); // Memanggil metode di atas dengan ukuran default 12f.
    }
}