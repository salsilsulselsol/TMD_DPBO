package viewmodel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
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
        STRUGGLING,
        FISH_MOVING_TO_JAR,
        GAME_OVER
    }

    private GamePanel gamePanel;
    private Timer gameTimerLoop;

    private Player player;
    private Harpoon harpoon;
    private Jar jar;
    private EntityHandler entityHandler;
    private List<Effect> activeEffects; // List untuk menampung efek visual
    private String currentUsername;
    private GameState currentState;

    private int remainingTimeSeconds;
    private Timer countdownTimer;

    private int struggleBarValue;
    private long struggleStartTimeMs;
    private GameObject gameObjectInStruggle;
    private GameObject fishMovingToJar;
    private Clip gameMusicClip;
    private float fishToJarSpeed = 25.0f;


    public GameLogic(GamePanel panel) {
        this.gamePanel = panel;
        gameTimerLoop = new Timer(1000 / 60, this);
        currentState = GameState.MENU;
    }

    // DIMODIFIKASI: Inisialisasi Player disesuaikan dengan sprite baru
    private void initializeGameEntities() {
        String idleSheetPath = "/assets/images/player-idle.png";
        String swimmingSheetPath = "/assets/images/player-swiming.png";
        String hurtSheetPath = "/assets/images/player-hurt.png";

        int playerSpriteFrameWidth = 80;
        int playerSpriteFrameHeight = 80;
        
        int playerIdleFramesCount = 6;
        int playerSwimmingFramesCount = 7;
        int playerHurtFramesCount = 5;
        
        int playerFrameDelayMs = 120;
        int playerHurtFrameDelayMs = 100;

        player = new Player(
            Constants.PLAYER_START_X,
            Constants.PLAYER_START_Y,
            Constants.PLAYER_WIDTH,
            Constants.PLAYER_HEIGHT,
            Constants.PLAYER_INITIAL_HEARTS,
            idleSheetPath,
            swimmingSheetPath,
            hurtSheetPath,
            playerSpriteFrameWidth,
            playerSpriteFrameHeight,
            playerIdleFramesCount,
            playerSwimmingFramesCount,
            playerHurtFramesCount,
            playerFrameDelayMs,
            playerHurtFrameDelayMs
        );

        harpoon = new Harpoon(player);
        jar = new Jar(
            Constants.JAR_X,
            Constants.JAR_Y,
            Constants.JAR_WIDTH,
            Constants.JAR_HEIGHT,
            "/assets/images/barrel.png"
        );
        jar.reset();

        entityHandler = new EntityHandler();
        entityHandler.reset();
        
        activeEffects = new ArrayList<>(); // Inisialisasi list efek

        gameObjectInStruggle = null;
        fishMovingToJar = null;
        remainingTimeSeconds = Constants.INITIAL_GAME_TIME_SECONDS;
        struggleBarValue = 0;
    }
    
    public void startGame(String username) {
        this.currentUsername = username;
        initializeGameEntities();
        currentState = GameState.PLAYING;
        
        if (gameMusicClip != null) SoundManager.stopSound(gameMusicClip);
        gameMusicClip = SoundManager.playSound("assets/sounds/game_music.wav", true);
        
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1000, e -> {
            if (currentState != GameState.GAME_OVER && currentState != GameState.MENU && currentState != GameState.FISH_MOVING_TO_JAR) {
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

    // DIMODIFIKASI: Game loop utama sekarang juga mengupdate efek
    @Override
    public void actionPerformed(ActionEvent e) {
        if (currentState == GameState.PLAYING) {
            updatePlayingState();
            checkPlayerGhostCollision();
        } else if (currentState == GameState.HARPOON_FIRED) {
            updateHarpoonFiredState();
        } else if (currentState == GameState.STRUGGLING) {
            updateStrugglingState();
        } else if (currentState == GameState.FISH_MOVING_TO_JAR) {
            updateFishMovingToJarState();
        }
        
        updateEffects(); // Update semua efek visual aktif

        if (currentState != GameState.MENU) {
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
        if (player != null) {
            player.resetMovementFlags();
        }
        currentState = GameState.STRUGGLING;
        this.gameObjectInStruggle = caughtEntity;
        this.fishMovingToJar = null;
        this.struggleBarValue = 0;
        this.struggleStartTimeMs = System.currentTimeMillis();
    }

    private void updateStrugglingState() {
        if (System.currentTimeMillis() - struggleStartTimeMs > Constants.STRUGGLE_TIME_LIMIT_MS) {
            failStruggle();
        }
    }

    private void updateFishMovingToJarState() {
        if (fishMovingToJar == null || jar == null) {
            currentState = GameState.PLAYING;
            if (player != null) player.resetMovementFlags();
            return;
        }

        float targetX = jar.getX() + jar.getWidth() / 2f - fishMovingToJar.getWidth() / 2f;
        float targetY = jar.getY() + jar.getHeight() / 4f - fishMovingToJar.getHeight() / 2f;

        float currentX = fishMovingToJar.getX();
        float currentY = fishMovingToJar.getY();

        float dx = targetX - currentX;
        float dy = targetY - currentY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < fishToJarSpeed) {
            if (fishMovingToJar instanceof Fish) {
                jar.addToJar((Fish) fishMovingToJar);
                remainingTimeSeconds += Constants.TIME_BONUS_PER_CATCH_SECONDS;
                SoundManager.playSound("assets/sounds/success_sound.wav", false);
            }
            fishMovingToJar = null;
            currentState = GameState.PLAYING;
            if (player != null) player.resetMovementFlags();
        } else {
            float moveX = (dx / (float)distance) * fishToJarSpeed;
            float moveY = (dy / (float)distance) * fishToJarSpeed;
            fishMovingToJar.setX(currentX + moveX);
            fishMovingToJar.setY(currentY + moveY);
            fishMovingToJar.update();
        }
        if (player != null) player.update();
        entityHandler.updateEntities();
    }

    // DIMODIFIKASI: Menghapus pemanggilan animasi shoot
    public void handlePlayerFireHarpoon(float targetX, float targetY) {
        if (currentState == GameState.PLAYING && !harpoon.isFiring()) {
            harpoon.fire(targetX, targetY);
            currentState = GameState.HARPOON_FIRED;
        }
    }
    
    // DIMODIFIKASI: Logika tombol spasi disesuaikan
    public void handleSpaceBarPress() {
        if (currentState == GameState.PLAYING) {
            showPauseConfirmation();
        } else if (currentState == GameState.STRUGGLING) {
            performStruggleAction();
        }
    }

    public void handleEscapeKeyPress() {
        if (currentState == GameState.PLAYING ||
            currentState == GameState.HARPOON_FIRED ||
            currentState == GameState.STRUGGLING ||
            currentState == GameState.FISH_MOVING_TO_JAR) {
            returnToMenu(true);
        }
    }

    private void succeedStruggle() {
        if (gameObjectInStruggle != null && gameObjectInStruggle instanceof Fish) {
            entityHandler.removeEntity(gameObjectInStruggle);
            
            this.fishMovingToJar = gameObjectInStruggle;
            this.gameObjectInStruggle = null;
            currentState = GameState.FISH_MOVING_TO_JAR;
        } else {
            currentState = GameState.PLAYING;
            if (player != null) player.resetMovementFlags();
        }
        harpoon.finishAttempt();
    }

    private void failStruggle() {
        player.loseHeart();
        player.playHurtAnimation(); // Mainkan animasi terluka saat gagal
        SoundManager.playSound("assets/sounds/fail_sound.wav", false);
        
        int effectRenderSize = 64;
        Effect hitEffect = new Effect(
            player.getX() + (player.getWidth() / 2f) - (effectRenderSize / 2f),
            player.getY() + (player.getHeight() / 2f) - (effectRenderSize / 2f),
            effectRenderSize, effectRenderSize,
            "/assets/images/hit.png",
            31, 32, 3, 100
        );
        activeEffects.add(hitEffect);

        harpoon.finishAttempt();
        gameObjectInStruggle = null;
        currentState = GameState.PLAYING;
        if (player != null) {
            player.resetMovementFlags();
        }

        if (player.getHearts() <= 0) {
            triggerGameOver("Nyawa Habis!");
        }
    }

    // DIMODIFIKASI: Pemicu animasi hurt dan efek hit
    private void checkPlayerGhostCollision() {
        if (player == null || entityHandler == null || currentState != GameState.PLAYING) return;
        List<GameObject> currentEntities = new ArrayList<>(entityHandler.getEntities());

        for (GameObject entity : currentEntities) {
            if (entity instanceof Ghost) {
                if (player.getCollisionBox().intersects(entity.getCollisionBox())) {
                    player.loseHeart();
                    player.playHurtAnimation(); // Mainkan animasi terluka player
                    SoundManager.playSound("assets/sounds/fail_sound.wav", false);
                    
                    int effectRenderSize = 64;
                    Effect hitEffect = new Effect(
                        player.getX() + (player.getWidth() / 2f) - (effectRenderSize / 2f),
                        player.getY() + (player.getHeight() / 2f) - (effectRenderSize / 2f),
                        effectRenderSize, effectRenderSize,
                        "/assets/images/hit.png",
                        31, 32, 3, 100
                    );
                    activeEffects.add(hitEffect);

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
            player.resetMovementFlags();
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
            player.resetMovementFlags();
        }
        if (activeEffects != null) {
            activeEffects.clear(); // Bersihkan sisa efek saat kembali ke menu
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

    // DIMODIFIKASI: Render game sekarang juga merender efek
    public void renderGame(Graphics g) {
        entityHandler.renderEntities(g);
        player.render(g);
        harpoon.render(g);
        
        if (currentState == GameState.STRUGGLING || currentState == GameState.FISH_MOVING_TO_JAR) {
            if (jar != null) {
                jar.render(g);
            }
        }
        
        if (currentState == GameState.STRUGGLING && gameObjectInStruggle != null) {
            gameObjectInStruggle.render(g);
        } else if (currentState == GameState.FISH_MOVING_TO_JAR && fishMovingToJar != null) {
            fishMovingToJar.render(g);
        }

        for (Effect effect : activeEffects) {
            effect.render(g);
        }
    }

    // --- METODE-METODE BARU ---

    private void performStruggleAction() {
        float tapEffectiveness = Constants.STRUGGLE_TAP_VALUE;
        if (gameObjectInStruggle instanceof Fish) {
            Fish strugglingFish = (Fish) gameObjectInStruggle;
            if (strugglingFish.getStruggleFactor() > 0) {
                tapEffectiveness /= strugglingFish.getStruggleFactor();
            }
        }
        struggleBarValue += tapEffectiveness;

        if (struggleBarValue >= Constants.STRUGGLE_BAR_MAX_VALUE) {
            succeedStruggle();
        }
    }

    private void showPauseConfirmation() {
        gameTimerLoop.stop();
        if (countdownTimer != null) countdownTimer.stop();

        int choice = JOptionPane.showConfirmDialog(
            gamePanel,
            "Apakah Anda yakin ingin kembali ke Menu Utama?",
            "Jeda Permainan",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            returnToMenu(true);
        } else {
            gameTimerLoop.start();
            if (countdownTimer != null) countdownTimer.start();
            gamePanel.requestFocusInWindow();
        }
    }
    
    private void updateEffects() {
        Iterator<Effect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            effect.update();
            if (effect.isFinished()) {
                iterator.remove();
            }
        }
    }

    // --- GETTER ---
    public Player getPlayer() { return player; }
    public GameState getCurrentState() { return currentState; }
    public int getRemainingTime() { return remainingTimeSeconds; }
    public Jar getJar() { return jar; }
    public float getStruggleProgress() {
        if (currentState != GameState.STRUGGLING) return 0f;
        return Math.min(1.0f, (float) struggleBarValue / Constants.STRUGGLE_BAR_MAX_VALUE);
    }
}