package viewmodel;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.io.InputStream;

public class FontManager {
    private static Font pressStart2PRegular;

    static {
        try {
            // Path ke file font Anda, relatif terhadap classpath (folder src)
            String fontPath = "/assets/fonts/PressStart2P-Regular.ttf"; // Sesuaikan jika nama file atau path berbeda
            InputStream is = FontManager.class.getResourceAsStream(fontPath);
            
            if (is == null) {
                System.err.println("File font tidak ditemukan di: " + fontPath);
                // Fallback ke font default jika font kustom tidak ditemukan
                pressStart2PRegular = new Font("Monospaced", Font.PLAIN, 12); 
            } else {
                Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(customFont);
                // Setelah diregister, Anda bisa membuat instance dengan nama atau langsung dari customFont
                // Untuk kemudahan, kita simpan instance yang sudah dibuat dengan ukuran dasar
                pressStart2PRegular = customFont.deriveFont(12f); // Ukuran dasar, bisa diubah dengan deriveFont lagi
                System.out.println("Font PressStart2P-Regular berhasil dimuat dan diregister.");
            }
        } catch (IOException | FontFormatException e) {
            System.err.println("Error memuat font kustom: " + e.getMessage());
            e.printStackTrace();
            // Fallback ke font default jika terjadi error
            pressStart2PRegular = new Font("Monospaced", Font.PLAIN, 12);
        }
    }

    /**
     * Mendapatkan instance font PressStart2P-Regular dengan ukuran tertentu.
     * @param size Ukuran font yang diinginkan.
     * @return Objek Font.
     */
    public static Font getPressStart2PRegular(float size) {
        if (pressStart2PRegular != null) {
            return pressStart2PRegular.deriveFont(size);
        } else {
            // Fallback jika pressStart2PRegular gagal dimuat di static block
            return new Font("Monospaced", Font.PLAIN, (int)size);
        }
    }

    /**
     * Mendapatkan instance font PressStart2P-Regular dengan ukuran default (12pt).
     * @return Objek Font.
     */
    public static Font getPressStart2PRegular() {
        return getPressStart2PRegular(12f); // Ukuran default
    }
}