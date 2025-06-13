package viewmodel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.Player;

public class InputHandler extends MouseAdapter { 
    private GameLogic gameLogic;
    private KeyControls keyControls; 

    public InputHandler(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        this.keyControls = new KeyControls(gameLogic);
    }

    public KeyAdapter getKeyControls() {
        return keyControls;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
             if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING) { 
                gameLogic.handlePlayerFireHarpoon(e.getX(), e.getY()); 
            }
        }
    }

    private static class KeyControls extends KeyAdapter {
        private GameLogic gameLogic;

        public KeyControls(GameLogic gameLogic) {
            this.gameLogic = gameLogic;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // --- DIMODIFIKASI: Izinkan input spasi saat Game Over ---
            if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gameLogic.skipGameOverDelay();
                }
                return; // Input lain diabaikan saat Game Over
            }
            // -----------------------------------------------------------

            if (gameLogic.getCurrentState() == GameLogic.GameState.MENU) {
                return;
            }

            Player currentPlayer = gameLogic.getPlayer();
            if (currentPlayer == null) return; 

            int key = e.getKeyCode();

            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING || 
                gameLogic.getCurrentState() == GameLogic.GameState.HARPOON_FIRED) { 
                switch (key) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> currentPlayer.setMoveUp(true);
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> currentPlayer.setMoveDown(true);
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> currentPlayer.setMoveLeft(true);
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> currentPlayer.setMoveRight(true);
                }
            }

            if (key == KeyEvent.VK_SPACE) { 
                gameLogic.handleSpaceBarPress(); 
            }
            
            if (key == KeyEvent.VK_Q || key == KeyEvent.VK_E) {
                gameLogic.handleStruggleKeyPress(key);
            }

            if (key == KeyEvent.VK_ESCAPE) {
                gameLogic.handleEscapeKeyPress(); 
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
            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING || 
                gameLogic.getCurrentState() == GameLogic.GameState.HARPOON_FIRED) { 
                switch (key) {
                    case KeyEvent.VK_W, KeyEvent.VK_UP -> currentPlayer.setMoveUp(false);
                    case KeyEvent.VK_S, KeyEvent.VK_DOWN -> currentPlayer.setMoveDown(false);
                    case KeyEvent.VK_A, KeyEvent.VK_LEFT -> currentPlayer.setMoveLeft(false);
                    case KeyEvent.VK_D, KeyEvent.VK_RIGHT -> currentPlayer.setMoveRight(false);
                }
            }
        }
    }
}