package viewmodel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.*; // Mencakup semua kelas model baru
import view.GamePanel;
import view.MenuScreen;
import java.util.List; // Untuk iterasi entitas
import java.util.ArrayList;


public class GameLogic implements ActionListener {
    public enum GameState {
        MENU, PLAYING, HARPOON_FIRED, STRUGGLING, GAME_OVER // HARPOON_FIRED bisa dipertimbangkan jadi HARPOON_FIRED
    }

    private GamePanel gamePanel;
    private Timer gameTimerLoop;

    private Player player;
    private Harpoon harpoon; // Diubah dari Net
    private Jar jar;
    private EntityHandler entityHandler; // Diubah dari JellyfishHandler
    private String currentUsername;
    private GameState currentState;

    private int remainingTimeSeconds;
    private Timer countdownTimer; 

    private int struggleBarValue;
    private long struggleStartTimeMs;
    private GameObject gameObjectInStruggle; // Diubah dari Jellyfish menjadi GameObject
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

        int playerSpriteFrameWidth = 128;    
        int playerSpriteFrameHeight = 128;   
        
        int playerIdleFramesCount = 12;      
        int playerSwimmingFramesCount = 12;  
        int playerShootFramesCount = 12;     
        
        int playerFrameDelayMs = 100; 
        int playerShootFrameDelayMs = 50; 

        player = new Player(
            Constants.PLAYER_START_X,      
            Constants.PLAYER_START_Y,      
            Constants.PLAYER_WIDTH,        
            Constants.PLAYER_HEIGHT,       
            Constants.PLAYER_INITIAL_HEARTS, 
            idleSheetPath,                 
            swimmingSheetPath,             
            shootSheetPath,                
            playerSpriteFrameWidth,        
            playerSpriteFrameHeight,       
            playerIdleFramesCount,         
            playerSwimmingFramesCount,     
            playerShootFramesCount,        
            playerFrameDelayMs,
            playerShootFrameDelayMs
        );

        harpoon = new Harpoon(player); 
        jar = new Jar(
            Constants.JAR_X, 
            Constants.JAR_Y, 
            Constants.JAR_WIDTH, 
            Constants.JAR_HEIGHT, 
            "/assets/images/jar_image.png" 
        );
        jar.reset(); // Pastikan jar direset

        entityHandler = new EntityHandler(); // Menggunakan EntityHandler
        entityHandler.reset(); 

        gameObjectInStruggle = null; 
        remainingTimeSeconds = Constants.INITIAL_GAME_TIME_SECONDS; 
        struggleBarValue = 0; 
    }
    
    public void startGame(String username) { //
        this.currentUsername = username;
        initializeGameEntities(); 
        currentState = GameState.PLAYING;
        
        if (gameMusicClip != null) SoundManager.stopSound(gameMusicClip); 
        gameMusicClip = SoundManager.playSound("assets/sounds/game_music.wav", true); //
        
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
            checkPlayerGhostCollision(); // Periksa kolisi dengan ghost
        } else if (currentState == GameState.HARPOON_FIRED) { // Mungkin ganti nama state ini
            updateHarpoonFiredState(); // Nama metode disesuaikan
        } else if (currentState == GameState.STRUGGLING) {
            updateStrugglingState();
        }
        
        if (currentState != GameState.MENU && currentState != GameState.GAME_OVER) {
             gamePanel.repaint(); 
        }
    }

    private void updatePlayingState() {
        player.update(); 
        entityHandler.updateEntities(); // Menggunakan EntityHandler
    }

    // Metode disesuaikan untuk Harpoon dan berbagai GameObject
    private void updateHarpoonFiredState() { 
        player.update();
        harpoon.update(); 
        entityHandler.updateEntities(); 

        GameObject hooked = harpoon.getHookedObject(); 
        if (hooked == null) { 
            List<GameObject> entities = entityHandler.getEntities(); // Ambil daftar entitas
            for (GameObject entity : entities) {
                // Hanya periksa hook dengan Fish (dan turunannya), bukan Ghost
                if (entity instanceof Fish) { // Ini akan menangkap Fish, BigFish, DartFish
                    if (harpoon.getTipCollisionBox().intersects(entity.getCollisionBox())) {
                        harpoon.hookObject(entity); 
                        SoundManager.playSound("assets/sounds/catch_sound.wav", false);
                        break; 
                    }
                }
            }
            if (!harpoon.isFiring()) { 
                currentState = GameState.PLAYING;
            }
        } else { 
            if (player.getCollisionBox().intersects(hooked.getCollisionBox())) {
                startStruggle(hooked); 
            }
        }
    }
    
    // Parameter diubah menjadi GameObject
    private void startStruggle(GameObject caughtEntity) { 
        currentState = GameState.STRUGGLING;
        this.gameObjectInStruggle = caughtEntity;
        this.struggleBarValue = 0;
        this.struggleStartTimeMs = System.currentTimeMillis();
        // Hapus entitas dari handler utama
        entityHandler.removeEntity(caughtEntity); 
    }

    private void updateStrugglingState() {
        if (System.currentTimeMillis() - struggleStartTimeMs > Constants.STRUGGLE_TIME_LIMIT_MS) { //
            failStruggle(); 
        }
    }

    public void handlePlayerFireHarpoon(float targetX, float targetY) { // Nama bisa jadi handlePlayerFireHarpoon
        if (currentState == GameState.PLAYING && !harpoon.isFiring()) { 
            harpoon.fire(targetX, targetY); 
            if (player != null) {
                player.playShootAnimation();
            }
            currentState = GameState.HARPOON_FIRED; // Bisa jadi HARPOON_FIRED
        }
    }
    
    public void handleSpaceBarPress() { //
        if (currentState == GameState.STRUGGLING) {
            struggleBarValue += Constants.STRUGGLE_TAP_VALUE; //
            if (struggleBarValue >= Constants.STRUGGLE_BAR_MAX_VALUE) { //
                succeedStruggle(); 
            }
        } else if (currentState == GameState.PLAYING || currentState == GameState.HARPOON_FIRED) {
            returnToMenu(true); 
        }
    }

    private void succeedStruggle() {
        if (gameObjectInStruggle != null && gameObjectInStruggle instanceof Fish) { // Pastikan itu adalah tipe Fish
            Fish caughtFish = (Fish) gameObjectInStruggle; // Casting
            jar.addToJar(caughtFish); // addToJar perlu diubah untuk menerima Fish, atau ambil skornya di sini
            // Jika Jar.addToJar(Jellyfish jellyfish) -> ubah Jar.addToJar(Fish fish)
            // atau buat Jar.addScore(int scoreValue)
            // Untuk sekarang, asumsikan Jar.addToJar bisa menangani Fish atau kita modif Jar:
            // jar.addCapturedFish(caughtFish.getScoreValue()); // Contoh jika Jar punya metode ini

            remainingTimeSeconds += Constants.TIME_BONUS_PER_CATCH_SECONDS; 
            SoundManager.playSound("assets/sounds/success_sound.wav", false);
        }
        harpoon.finishAttempt(); 
        gameObjectInStruggle = null;
        currentState = GameState.PLAYING;
    }

    private void failStruggle() {
        player.loseHeart(); 
        SoundManager.playSound("assets/sounds/fail_sound.wav", false);
        
        harpoon.finishAttempt(); 
        gameObjectInStruggle = null;
        currentState = GameState.PLAYING;

        if (player.getHearts() <= 0) { 
            triggerGameOver("Nyawa Habis!");
        }
    }

    private void checkPlayerGhostCollision() {
        if (player == null || entityHandler == null || currentState != GameState.PLAYING) return; // Hanya cek saat playing

        // Buat iterator yang aman atau salin list jika ada potensi ConcurrentModificationException
        // Untuk keamanan, kita iterasi pada salinan
        List<GameObject> currentEntities = new ArrayList<>(entityHandler.getEntities()); 

        for (GameObject entity : currentEntities) {
            if (entity instanceof Ghost) {
                if (player.getCollisionBox().intersects(entity.getCollisionBox())) {
                    player.loseHeart();
                    SoundManager.playSound("assets/sounds/fail_sound.wav", false); 
                    
                    entityHandler.removeEntity(entity); // Hapus ghost setelah kena

                    if (player.getHearts() <= 0) {
                        triggerGameOver("Nyawa Habis karena Hantu!");
                    }
                    // Mungkin tambahkan sedikit delay atau invincibility untuk player di sini
                    break; 
                }
            }
        }
    }
    
    private void triggerGameOver(String message) { //
        if (currentState == GameState.GAME_OVER) return; 

        currentState = GameState.GAME_OVER;
        gameTimerLoop.stop(); 
        if (countdownTimer != null) countdownTimer.stop(); 
        SoundManager.stopSound(gameMusicClip); 
        SoundManager.playSound("assets/sounds/gameover_sound.wav", false); //
        
        saveGameResult(); 
        gamePanel.repaint(); 

        JOptionPane.showMessageDialog(gamePanel, 
            message + "\n\nUsername: " + currentUsername + 
            "\nSkor Akhir: " + jar.getTotalScore() + 
            "\nIkan Terkumpul: " + jar.getCollectedCount(), // Ubah teks "Ubur-ubur" menjadi "Ikan"
            "Game Over", 
            JOptionPane.INFORMATION_MESSAGE);
        
        returnToMenu(false); 
    }

    private void saveGameResult() { //
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

    private void returnToMenu(boolean saveScoreOnManualQuit) { //
        if (saveScoreOnManualQuit && currentState != GameState.GAME_OVER) { 
            saveGameResult();
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
        jar.render(g);
        entityHandler.renderEntities(g); // Menggunakan EntityHandler
        player.render(g);
        harpoon.render(g); 
        
        if(gameObjectInStruggle != null && currentState == GameState.STRUGGLING) {
            gameObjectInStruggle.render(g); // Render objek yang di-struggle
        }
    }

    public Player getPlayer() { return player; } //
    public GameState getCurrentState() { return currentState; } //
    public int getRemainingTime() { return remainingTimeSeconds; } //
    public Jar getJar() { return jar; } //
    public float getStruggleProgress() { //
        if (currentState != GameState.STRUGGLING) return 0f;
        return Math.min(1.0f, (float) struggleBarValue / Constants.STRUGGLE_BAR_MAX_VALUE); 
    }
}