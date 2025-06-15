import javax.swing.SwingUtilities;
import view.MenuScreen;
import viewmodel.SoundManager;

// Main class untuk menjalankan aplikasi game Monster Fish Hunt
public class Main {
    /*
        Saya Faisal Nur Qolbi dengan NIM 2311399 mengerjakan evaluasi Tugas Masa Depan 
        dalam mata kuliah Desain dan Pemrograman Berorientasi Objek untuk 
        keberkahanNya maka saya tidak melakukan kecurangan seperti 
        yang telah dispesifikasikan. Aamiin.
     */

    public static void main(String[] args) {
        // Menjalankan aplikasi pada thread event-dispatcher Swing
        SwingUtilities.invokeLater(() -> {
            // Inisialisasi sound manager
            SoundManager.init();
            // Membuat dan menampilkan menu utama
            MenuScreen menu = new MenuScreen();
            menu.setVisible(true);
        });
    }
}