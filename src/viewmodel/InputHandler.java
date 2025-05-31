package viewmodel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.Player;

public class InputHandler extends MouseAdapter { // MouseAdapter untuk klik net
    private GameLogic gameLogic;
    private KeyControls keyControls; // Inner class untuk keyboard

    public InputHandler(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.keyControls = new KeyControls(gameLogic);
    }

    // Metode untuk mendapatkan KeyListener agar bisa di-add ke GamePanel
    public KeyAdapter getKeyControls() {
        return keyControls;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Hanya proses jika klik kiri mouse [cite: 41, 87] (diasumsikan klik kiri)
        if (e.getButton() == MouseEvent.BUTTON1) { 
             if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING) { // Hanya bisa menembak saat PLAYING
                gameLogic.handlePlayerFireNet(e.getX(), e.getY()); // Teruskan koordinat klik
            }
        }
    }

    // Inner class untuk menangani input keyboard
    private static class KeyControls extends KeyAdapter {
        private GameLogic gameLogic;

        public KeyControls(GameLogic gameLogic) {
            this.gameLogic = gameLogic;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Jangan proses input jika game sudah berakhir atau di menu
            if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER || 
                gameLogic.getCurrentState() == GameLogic.GameState.MENU) {
                return;
            }

            Player currentPlayer = gameLogic.getPlayer(); // Ambil referensi player dari GameLogic
            if (currentPlayer == null) return; // Jika player belum ada

            int key = e.getKeyCode();

            // Proses pergerakan player jika state mengizinkan [cite: 26, 36, 37, 72, 82, 83]
            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING || 
                gameLogic.getCurrentState() == GameLogic.GameState.NET_FIRED) { // Player bisa bergerak saat net ditembakkan
                switch (key) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    currentPlayer.setMoveUp(true); break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  currentPlayer.setMoveDown(true); break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  currentPlayer.setMoveLeft(true); break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: currentPlayer.setMoveRight(true); break;
                }
            }

            // Tombol Space untuk struggle atau quit ke menu [cite: 35, 81]
            if (key == KeyEvent.VK_SPACE) {
                gameLogic.handleSpaceBarPress();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
             if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER || 
                 gameLogic.getCurrentState() == GameLogic.GameState.MENU) {
                return;
            }

            Player currentPlayer = gameLogic.getPlayer();
            if (currentPlayer == null) return;

            int key = e.getKeyCode();
            // Hanya proses pergerakan jika state PLAYING atau NET_FIRED
            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING || 
                gameLogic.getCurrentState() == GameLogic.GameState.NET_FIRED) {
                switch (key) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    currentPlayer.setMoveUp(false); break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  currentPlayer.setMoveDown(false); break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  currentPlayer.setMoveLeft(false); break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: currentPlayer.setMoveRight(false); break;
                }
            }
        }
    }
}