package view;

import javax.swing.JFrame;
import viewmodel.InputHandler;

// Kelas ini bertanggung jawab untuk membuat dan menampilkan jendela utama (JFrame) permainan.
public class GameWindow {
    private JFrame frame; // Instance dari JFrame sebagai window utama.

    // Konstruktor untuk membuat window permainan.
    public GameWindow(GamePanel gamePanel, InputHandler inputHandler) {
        // Inisialisasi JFrame dengan judul window.
        frame = new JFrame("Monster Fish Hunt | Playing...");
        // Mengatur agar program berhenti saat window ditutup.
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Mengunci ukuran window agar tidak bisa diubah oleh pengguna.
        frame.setResizable(false);
        
        // Menambahkan GamePanel (tempat game digambar) ke dalam frame.
        frame.add(gamePanel);
        
        // Mendaftarkan listener keyboard dan mouse ke GamePanel.
        gamePanel.addKeyListener(inputHandler.getKeyControls());
        gamePanel.addMouseListener(inputHandler); // InputHandler juga bertindak sebagai MouseListener.

        // Mengatur ukuran window secara otomatis sesuai ukuran konten (GamePanel).
        frame.pack(); 
        // Menempatkan window di tengah layar.
        frame.setLocationRelativeTo(null);
        // Menampilkan window ke layar.
        frame.setVisible(true);
        
        // Meminta fokus ke GamePanel agar input keyboard bisa langsung diterima tanpa perlu klik.
        gamePanel.requestFocusInWindow(); 
    }
}