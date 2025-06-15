package viewmodel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.Player;

// Kelas ini menangani semua input dari pengguna (mouse dan keyboard) dan meneruskannya ke GameLogic.
public class InputHandler extends MouseAdapter {
    private GameLogic gameLogic; // Referensi ke otak permainan.
    private KeyControls keyControls; // Kelas internal khusus untuk input keyboard.

    // Konstruktor untuk menghubungkan InputHandler dengan GameLogic.
    public InputHandler(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.keyControls = new KeyControls(gameLogic);
    }

    // Mengembalikan instance KeyAdapter agar bisa didaftarkan ke GamePanel.
    public KeyAdapter getKeyControls() {
        return keyControls;
    }

    // Dipanggil ketika tombol mouse ditekan.
    @Override
    public void mousePressed(MouseEvent e) {
        // Hanya proses klik kiri mouse dan jika state game sedang 'PLAYING'.
        if (e.getButton() == MouseEvent.BUTTON1) {
             if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING) {
                // Meneruskan perintah tembak ke GameLogic dengan koordinat mouse.
                gameLogic.handlePlayerFireHarpoon(e.getX(), e.getY());
            }
        }
    }

    // Kelas internal statis yang khusus menangani input dari keyboard.
    private static class KeyControls extends KeyAdapter {
        private GameLogic gameLogic;

        public KeyControls(GameLogic gameLogic) {
            this.gameLogic = gameLogic;
        }

        // Dipanggil setiap kali sebuah tombol keyboard ditekan.
        @Override
        public void keyPressed(KeyEvent e) {
            // Logika input khusus saat layar Game Over.
            if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER) {
                // Jika menekan spasi, lewati delay dan kembali ke menu.
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gameLogic.skipGameOverDelay();
                }
                return; // Abaikan semua input lain.
            }

            // Abaikan input jika sedang di menu.
            if (gameLogic.getCurrentState() == GameLogic.GameState.MENU) {
                return;
            }

            // Pastikan player ada sebelum memproses input lebih lanjut.
            Player currentPlayer = gameLogic.getPlayer();
            if (currentPlayer == null) return;

            int key = e.getKeyCode();

            // Logika untuk pergerakan player (hanya aktif di state tertentu).
            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING ||
                gameLogic.getCurrentState() == GameLogic.GameState.HARPOON_FIRED) {
                switch (key) {
                    // Mengatur flag pergerakan di objek Player menjadi true untuk mulai bergerak.
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> currentPlayer.setMoveUp(true);
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> currentPlayer.setMoveDown(true);
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> currentPlayer.setMoveLeft(true);
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> currentPlayer.setMoveRight(true);
                }
            }

            // Meneruskan penekanan tombol aksi ke GameLogic.
            if (key == KeyEvent.VK_SPACE) {
                gameLogic.handleSpaceBarPress(); // Jeda permainan.
            }

            if (key == KeyEvent.VK_Q || key == KeyEvent.VK_E) {
                gameLogic.handleStruggleKeyPress(key); // Aksi saat struggle.
            }

            if (key == KeyEvent.VK_ESCAPE) {
                gameLogic.handleEscapeKeyPress(); // Kembali ke menu utama.
            }
        }

        // Dipanggil setiap kali sebuah tombol keyboard dilepas.
        @Override
        public void keyReleased(KeyEvent e) {
             // Abaikan input jika game sudah berakhir atau masih di menu.
             if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER ||
                 gameLogic.getCurrentState() == GameLogic.GameState.MENU) {
                return;
            }

            Player currentPlayer = gameLogic.getPlayer();
            if (currentPlayer == null) return;

            int key = e.getKeyCode();
            // Logika untuk menghentikan pergerakan player saat tombol dilepas.
            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING ||
                gameLogic.getCurrentState() == GameLogic.GameState.HARPOON_FIRED) {
                switch (key) {
                    // Mengatur flag pergerakan di objek Player menjadi false untuk berhenti.
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> currentPlayer.setMoveUp(false);
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> currentPlayer.setMoveDown(false);
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> currentPlayer.setMoveLeft(false);
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> currentPlayer.setMoveRight(false);
                }
            }
        }
    }
}