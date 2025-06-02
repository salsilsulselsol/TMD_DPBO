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
        if (e.getButton() == MouseEvent.BUTTON1) { //
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
            if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER || 
                gameLogic.getCurrentState() == GameLogic.GameState.MENU) {
                return;
            }

            Player currentPlayer = gameLogic.getPlayer(); //
            if (currentPlayer == null) return; 

            int key = e.getKeyCode();

            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING || 
                gameLogic.getCurrentState() == GameLogic.GameState.HARPOON_FIRED) { 
                switch (key) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:    currentPlayer.setMoveUp(true); break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:  currentPlayer.setMoveDown(true); break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:  currentPlayer.setMoveLeft(true); break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT: currentPlayer.setMoveRight(true); break;
                }
            }

            if (key == KeyEvent.VK_SPACE) { 
                gameLogic.handleSpaceBarPress(); 
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

            Player currentPlayer = gameLogic.getPlayer(); //
            if (currentPlayer == null) return;

            int key = e.getKeyCode();
            if (gameLogic.getCurrentState() == GameLogic.GameState.PLAYING || 
                gameLogic.getCurrentState() == GameLogic.GameState.HARPOON_FIRED) { 
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