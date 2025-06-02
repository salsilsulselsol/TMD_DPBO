package viewmodel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.*; 
import view.GamePanel;
import view.MenuScreen; 

public class GameLogic implements ActionListener {
    public enum GameState {
        MENU, PLAYING, 
        HARPOON_FIRED, 
        STRUGGLING, GAME_OVER 
    }

    private GamePanel gamePanel;
    private Timer gameTimerLoop;

    private Player player;
    private Harpoon harpoon; 
    private Jar jar;
    private EntityHandler entityHandler; 
    private String currentUsername;
    private GameState currentState;

    private int remainingTimeSeconds;
    private Timer countdownTimer; 

    private int struggleBarValue;
    private long struggleStartTimeMs;
    private GameObject gameObjectInStruggle; 
    private Clip gameMusicClip;

    public GameLogic(GamePanel panel) {
        this.gamePanel = panel;
        gameTimerLoop = new Timer(1000 / 60, this); 
        currentState = GameState.MENU; 
    }

    private void initializeGameEntities() {
        String idleSheetPath = "/assets/images/idle-player.png";     
        String swimmingSheetPath = "/assets/images/swim-player.png"; 
        String shootSheetPath = "/assets/images/shoot-player.png";   

        int playerSpriteFrameWidth = 128;    // Ukuran satu frame di sprite sheet Player
        int playerSpriteFrameHeight = 128;   // Ukuran satu frame di sprite sheet Player
        
        int playerIdleFramesCount = 12;      
        int playerSwimmingFramesCount = 12;  
        int playerShootFramesCount = 12;     
        
        int playerFrameDelayMs = 100; 
        int playerShootFrameDelayMs = 50; 

        player = new Player(
            Constants.PLAYER_START_X,      
            Constants.PLAYER_START_Y,      
            Constants.PLAYER_WIDTH,        // Ukuran render Player di layar
            Constants.PLAYER_HEIGHT,       // Ukuran render Player di layar
            Constants.PLAYER_INITIAL_HEARTS, 
            idleSheetPath,                 
            swimmingSheetPath,             
            shootSheetPath,                
            playerSpriteFrameWidth,    // Diteruskan sebagai ukuran frame sprite    
            playerSpriteFrameHeight,   // Diteruskan sebagai ukuran frame sprite
            playerIdleFramesCount,         
            playerSwimmingFramesCount,     
            playerShootFramesCount,        
            playerFrameDelayMs,
            playerShootFrameDelayMs
        );
        // player.resetMovementFlags(); // Sudah dipanggil di konstruktor Player

        harpoon = new Harpoon(player); 
        jar = new Jar(
            Constants.JAR_X, 
            Constants.JAR_Y, 
            Constants.JAR_WIDTH, 
            Constants.JAR_HEIGHT, 
            "/assets/images/jar_image.png" 
        );
        jar.reset();

        entityHandler = new EntityHandler(); 
        entityHandler.reset(); 

        gameObjectInStruggle = null; 
        remainingTimeSeconds = Constants.INITIAL_GAME_TIME_SECONDS; 
        struggleBarValue = 0; 
    }
    
    public void startGame(String username) {
        this.currentUsername = username;
        initializeGameEntities(); // Akan memanggil player.resetMovementFlags() via konstruktor Player
        currentState = GameState.PLAYING;
        
        if (gameMusicClip != null) SoundManager.stopSound(gameMusicClip); 
        gameMusicClip = SoundManager.playSound("assets/sounds/game_music.wav", true);
        
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1000, e -> { 
            if (currentState != GameState.GAME_OVER && currentState != GameState.MENU) {
                remainingTimeSeconds--;
                if (remainingTimeSeconds <= 0) {
                    remainingTimeSeconds = 0;
                    triggerGameOver("Waktu Habis!");
                }
            }
        });
        countdownTimer.start();

        if (!gameTimerLoop.isRunning()) { 
            gameTimerLoop.start();
        }
        gamePanel.requestFocusInWindow(); 
    }

    @Override
    public void actionPerformed(ActionEvent e) { 
        if (currentState == GameState.PLAYING) {
            updatePlayingState();
            checkPlayerGhostCollision(); 
        } else if (currentState == GameState.HARPOON_FIRED) { 
            updateHarpoonFiredState(); 
        } else if (currentState == GameState.STRUGGLING) {
            updateStrugglingState();
        }
        
        if (currentState != GameState.MENU && currentState != GameState.GAME_OVER) {
             gamePanel.repaint(); 
        }
    }

    private void updatePlayingState() {
        player.update(); 
        entityHandler.updateEntities(); 
    }

    private void updateHarpoonFiredState() { 
        player.update();
        harpoon.update(); 
        entityHandler.updateEntities(); 

        GameObject hooked = harpoon.getHookedObject(); 
        if (hooked == null) { 
            List<GameObject> entities = entityHandler.getEntities(); 
            for (GameObject entity : entities) {
                if (entity instanceof Fish) { 
                    if (harpoon.getTipCollisionBox().intersects(entity.getCollisionBox())) { 
                        harpoon.hookObject(entity); 
                        SoundManager.playSound("assets/sounds/catch_sound.wav", false);
                        break; 
                    }
                }
            }
            if (!harpoon.isFiring()) { 
                currentState = GameState.PLAYING;
                 // Jika harpoon ditarik kembali tanpa mengenai, reset gerakan juga untuk antisipasi
                if (player != null) {
                    player.resetMovementFlags();
                }
            }
        } else { 
            if (player.getCollisionBox().intersects(hooked.getCollisionBox())) { 
                startStruggle(hooked); 
            }
        }
    }
    
    private void startStruggle(GameObject caughtEntity) { 
        // Saat struggle dimulai, input gerakan player diabaikan karena kondisi state di InputHandler
        // Namun, untuk memastikan, kita bisa juga reset di sini.
        if (player != null) {
            player.resetMovementFlags();
        }
        currentState = GameState.STRUGGLING;
        this.gameObjectInStruggle = caughtEntity;
        this.struggleBarValue = 0;
        this.struggleStartTimeMs = System.currentTimeMillis();
        entityHandler.removeEntity(caughtEntity); 
    }

    private void updateStrugglingState() {
        if (System.currentTimeMillis() - struggleStartTimeMs > Constants.STRUGGLE_TIME_LIMIT_MS) {
            failStruggle(); 
        }
    }

    public void handlePlayerFireHarpoon(float targetX, float targetY) { 
        if (currentState == GameState.PLAYING && !harpoon.isFiring()) { 
            harpoon.fire(targetX, targetY); 
            if (player != null) {
                player.playShootAnimation();
            }
            currentState = GameState.HARPOON_FIRED; 
        }
    }
    
    public void handleSpaceBarPress() {
        if (currentState == GameState.STRUGGLING) {
            struggleBarValue += Constants.STRUGGLE_TAP_VALUE;
            if (struggleBarValue >= Constants.STRUGGLE_BAR_MAX_VALUE) {
                succeedStruggle();
            }
        }
    }

    public void handleEscapeKeyPress() {
        if (currentState == GameState.PLAYING || 
            currentState == GameState.HARPOON_FIRED ||
            currentState == GameState.STRUGGLING) { 
            returnToMenu(true); 
        }
    }

    private void succeedStruggle() {
        if (gameObjectInStruggle != null && gameObjectInStruggle instanceof Fish) { 
            Fish caughtFish = (Fish) gameObjectInStruggle; 
            jar.addToJar(caughtFish); 
            remainingTimeSeconds += Constants.TIME_BONUS_PER_CATCH_SECONDS; 
            SoundManager.playSound("assets/sounds/success_sound.wav", false);
        }
        harpoon.finishAttempt(); 
        gameObjectInStruggle = null;
        currentState = GameState.PLAYING;
        if (player != null) {
            player.resetMovementFlags(); // Reset gerakan setelah struggle
        }
    }

    private void failStruggle() {
        player.loseHeart(); 
        SoundManager.playSound("assets/sounds/fail_sound.wav", false);
        
        harpoon.finishAttempt(); 
        gameObjectInStruggle = null;
        currentState = GameState.PLAYING;
        if (player != null) {
            player.resetMovementFlags(); // Reset gerakan setelah struggle
        }

        if (player.getHearts() <= 0) { 
            triggerGameOver("Nyawa Habis!");
        }
    }

    private void checkPlayerGhostCollision() {
        if (player == null || entityHandler == null || currentState != GameState.PLAYING) return;
        List<GameObject> currentEntities = new ArrayList<>(entityHandler.getEntities()); 

        for (GameObject entity : currentEntities) {
            if (entity instanceof Ghost) {
                if (player.getCollisionBox().intersects(entity.getCollisionBox())) { 
                    player.loseHeart(); 
                    SoundManager.playSound("assets/sounds/fail_sound.wav", false); 
                    entityHandler.removeEntity(entity); 
                    if (player.getHearts() <= 0) { 
                        triggerGameOver("Nyawa Habis karena Hantu!");
                    }
                    break; 
                }
            }
        }
    }
    
    private void triggerGameOver(String message) {
        if (currentState == GameState.GAME_OVER) return; 
        currentState = GameState.GAME_OVER;
        if (player != null) {
            player.resetMovementFlags(); // Reset gerakan saat game over
        }
        gameTimerLoop.stop(); 
        if (countdownTimer != null) countdownTimer.stop(); 
        SoundManager.stopSound(gameMusicClip); 
        SoundManager.playSound("assets/sounds/gameover_sound.wav", false);
        saveGameResult(); 
        gamePanel.repaint(); 
        JOptionPane.showMessageDialog(gamePanel, 
            message + "\n\nUsername: " + currentUsername + 
            "\nSkor Akhir: " + jar.getTotalScore() + 
            "\nIkan Terkumpul: " + jar.getCollectedCount(), 
            "Game Over", 
            JOptionPane.INFORMATION_MESSAGE);
        returnToMenu(false); 
    }

    private void saveGameResult() {
        if (currentUsername != null && !currentUsername.isEmpty() && jar != null) {
            try (TableHasil th = new TableHasil()) { 
                GameData gameData = new GameData(currentUsername, jar.getTotalScore(), jar.getCollectedCount()); 
                th.insertOrUpdateHasil(gameData); 
            } catch (Exception ex) { 
                System.err.println("Error saat menyimpan skor: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void returnToMenu(boolean saveScoreOnManualQuit) {
        if (saveScoreOnManualQuit && currentState != GameState.GAME_OVER) { 
            saveGameResult();
        }
        if (player != null) {
            player.resetMovementFlags(); // Reset gerakan saat kembali ke menu
        }
        gameTimerLoop.stop();
        if (countdownTimer != null) countdownTimer.stop();
        currentState = GameState.MENU; 
        SoundManager.stopSound(gameMusicClip); 
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(gamePanel);
        if (currentFrame != null) {
            currentFrame.dispose(); 
        }
        SwingUtilities.invokeLater(() -> {
            MenuScreen menu = new MenuScreen(); 
            menu.setVisible(true);
        });
    }

    public void renderGame(Graphics g) {
        entityHandler.renderEntities(g); 
        player.render(g); 
        harpoon.render(g); 
        
        if (currentState == GameState.STRUGGLING) {
            if (jar != null) {
                jar.render(g); 
            }
            if (gameObjectInStruggle != null) {
                gameObjectInStruggle.render(g); 
            }
        }
    }

    public Player getPlayer() { return player; } 
    public GameState getCurrentState() { return currentState; } 
    public int getRemainingTime() { return remainingTimeSeconds; } 
    public Jar getJar() { return jar; } 
    public float getStruggleProgress() { 
        if (currentState != GameState.STRUGGLING) return 0f;
        return Math.min(1.0f, (float) struggleBarValue / Constants.STRUGGLE_BAR_MAX_VALUE); 
    }
}