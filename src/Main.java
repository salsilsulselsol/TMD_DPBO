import javax.swing.SwingUtilities;
import view.MenuScreen;
import viewmodel.SoundManager;

public class Main {
    /*
        Saya Faisal Nur Qolbi dengan NIM 2311399 mengerjakan evaluasi Tugas Masa Depan 
        dalam mata kuliah Desain dan Pemrograman Berorientasi Objek untuk 
        keberkahanNya maka saya tidak melakukan kecurangan seperti 
        yang telah dispesifikasikan. Aamiin.
     */

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SoundManager.init();
            MenuScreen menu = new MenuScreen();
            menu.setVisible(true);
        });
    }
}