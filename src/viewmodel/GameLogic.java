package viewmodel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        MENU, PLAYING, NET_FIRED, STRUGGLING, GAME_OVER
    }

    private GamePanel gamePanel;
    private Timer gameTimerLoop;

    private Player player;
    private Net net;
    private Jar jar;
    private JellyfishHandler jellyfishHandler;
    private String currentUsername;
    private GameState currentState;

    private int remainingTimeSeconds;
    private Timer countdownTimer; // Timer per detik untuk waktu game

    private int struggleBarValue;
    private long struggleStartTimeMs;
    private Jellyfish jellyfishInStruggle;
    private Clip gameMusicClip; // Menyimpan clip musik game

    public GameLogic(GamePanel panel) {
        this.gamePanel = panel;
        // Timer untuk game loop utama (update dan render)
        gameTimerLoop = new Timer(1000 / 60, this); // Target ~60 FPS
        currentState = GameState.MENU; // Awalnya dikontrol oleh MenuScreen
    }

    private void initializeGameEntities() {
        // --- Inisialisasi Player dengan Animasi Sprite Sheet ---
        // GANTI NILAI-NILAI INI SESUAI DENGAN DETAIL ASET SPRITE SHEET PLAYER ANDA
        String idleSheetPath = "/assets/images/player-idle.png";         // Path ke sprite sheet idle
        String swimmingSheetPath = "/assets/images/player-swiming.png";   // Path ke sprite sheet swimming

        // Dimensi satu frame animasi dari sprite sheet (asumsikan sama untuk idle & swimming)
        int playerSpriteFrameWidth = 80;    // CONTOH: Lebar satu frame di sprite sheet (misal 32px)
        int playerSpriteFrameHeight = 80;   // CONTOH: Tinggi satu frame di sprite sheet (misal 32px)
        
        // Jumlah frame untuk masing-masing animasi
        int playerIdleFramesCount = 5;      // CONTOH: Jumlah frame dalam animasi idle
        int playerSwimmingFramesCount = 6;  // CONTOH: Jumlah frame dalam animasi swimming
        
        // Kecepatan animasi (durasi tampilan per frame dalam milidetik)
        int playerFrameDelayMs = 100;       // CONTOH: 150ms per frame

        player = new Player(
            Constants.PLAYER_START_X,           // Posisi X awal dari Constants
            Constants.PLAYER_START_Y,           // Posisi Y awal dari Constants
            Constants.PLAYER_WIDTH,             // Lebar render Player di layar (dari Constants)
            Constants.PLAYER_HEIGHT,            // Tinggi render Player di layar (dari Constants)
            Constants.PLAYER_INITIAL_HEARTS,    // Jumlah nyawa awal (dari Constants)
            idleSheetPath,                      // Path ke sprite sheet idle
            swimmingSheetPath,                  // Path ke sprite sheet swimming
            playerSpriteFrameWidth,             // Lebar satu frame animasi
            playerSpriteFrameHeight,            // Tinggi satu frame animasi
            playerIdleFramesCount,              // Jumlah frame untuk animasi idle
            playerSwimmingFramesCount,          // Jumlah frame untuk animasi swimming
            playerFrameDelayMs                  // Delay antar frame animasi
        );

        // --- Inisialisasi Objek Lain (Net, Jar, JellyfishHandler) ---
        net = new Net(player); // Net membutuhkan referensi ke objek Player

        // PASTIKAN ASET GAMBAR UNTUK JAR ADA DI PATH YANG BENAR
        jar = new Jar(
            Constants.JAR_X, 
            Constants.JAR_Y, 
            Constants.JAR_WIDTH, 
            Constants.JAR_HEIGHT, 
            "/assets/images/jar_image.png" // Path ke gambar Jar
        );

        jellyfishHandler = new JellyfishHandler();
        jellyfishHandler.reset(); // Pastikan handler ubur-ubur direset setiap game baru dimulai

        // --- Reset Variabel Status Game ---
        jellyfishInStruggle = null; // Tidak ada jellyfish yang sedang di-struggle di awal
        remainingTimeSeconds = Constants.INITIAL_GAME_TIME_SECONDS; // Reset waktu game
        struggleBarValue = 0; // Reset progress bar struggle
    }
    
    public void startGame(String username) {
        this.currentUsername = username;
        initializeGameEntities(); // Reset semua entitas game
        currentState = GameState.PLAYING;
        
        // Putar musik game (jika ada dan belum berjalan)
        if (gameMusicClip != null) SoundManager.stopSound(gameMusicClip); // Hentikan jika ada sisa
        gameMusicClip = SoundManager.playSound("assets/sounds/game_music.wav", true); // PASTIKAN ASET ADA
        
        // Setup timer per detik untuk waktu game
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1000, e -> { // Aksi setiap 1 detik
            if (currentState != GameState.GAME_OVER && currentState != GameState.MENU) {
                remainingTimeSeconds--;
                if (remainingTimeSeconds <= 0) {
                    remainingTimeSeconds = 0;
                    triggerGameOver("Waktu Habis!");
                }
            }
        });
        countdownTimer.start();

        if (!gameTimerLoop.isRunning()) { // Pastikan game loop utama berjalan
            gameTimerLoop.start();
        }
        gamePanel.requestFocusInWindow(); // Agar panel game bisa menerima input
    }

    @Override
    public void actionPerformed(ActionEvent e) { // Dipanggil oleh gameTimerLoop (60x per detik)
        if (currentState == GameState.PLAYING) {
            updatePlayingState();
        } else if (currentState == GameState.NET_FIRED) {
            updateNetFiredState();
        } else if (currentState == GameState.STRUGGLING) {
            updateStrugglingState();
        }
        // Tidak ada update khusus untuk GAME_OVER atau MENU di game loop ini
        
        if (currentState != GameState.MENU && currentState != GameState.GAME_OVER) {
             gamePanel.repaint(); // Panggil repaint setelah update state
        }
    }

    private void updatePlayingState() {
        player.update(); // Update posisi player berdasarkan input
        jellyfishHandler.updateJellyfish(); // Spawn dan gerakkan ubur-ubur
        // Net tidak di-update di state ini, hanya saat ditembakkan
    }

    private void updateNetFiredState() {
        player.update(); // Player mungkin masih bisa bergerak saat net ditembakkan
        net.update();    // Update posisi net (memanjang atau menarik)
        jellyfishHandler.updateJellyfish(); // Ubur-ubur lain tetap bergerak

        Jellyfish hooked = net.getHookedJellyfish();
        if (hooked == null) { // Jika net masih mencari target
            // Iterasi semua jellyfish untuk cek collision dengan ujung net
            for (Jellyfish jf : jellyfishHandler.getJellyfishes()) { // Ambil list copy
                if (net.getTipCollisionBox().intersects(jf.getCollisionBox()) && jf != net.getHookedJellyfish()) {
                    net.hookJellyfish(jf); // Net mengenai jellyfish
                    SoundManager.playSound("assets/sounds/catch_sound.wav", false); // Efek suara tangkap
                    break; // Hanya tangkap satu per tembakan
                }
            }
            if (!net.isFiring()) { // Jika net ditarik kembali (miss atau max length)
                currentState = GameState.PLAYING; // Kembali ke state normal
            }
        } else { // Jika net sudah menangkap dan sedang menarik
            // Cek apakah jellyfish yang ditarik sudah sampai di player
            if (player.getCollisionBox().intersects(hooked.getCollisionBox())) {
                startStruggle(hooked); // Mulai mekanisme struggle
            }
        }
    }
    
    private void startStruggle(Jellyfish caughtJellyfish) {
        currentState = GameState.STRUGGLING;
        this.jellyfishInStruggle = caughtJellyfish;
        this.struggleBarValue = 0;
        this.struggleStartTimeMs = System.currentTimeMillis();
        // Penting: Hapus jellyfish yang di-struggle dari handler utama agar tidak di-render atau di-update oleh handler lagi
        jellyfishHandler.removeJellyfish(caughtJellyfish); 
    }

    private void updateStrugglingState() {
        // Cek apakah waktu struggle sudah habis
        if (System.currentTimeMillis() - struggleStartTimeMs > Constants.STRUGGLE_TIME_LIMIT_MS) {
            failStruggle(); // Gagal jika waktu habis
        }
        // Pemain tidak bergerak saat struggle, hanya fokus pada input spacebar
    }

    // Dipanggil dari InputHandler saat player klik mouse
    public void handlePlayerFireNet(float targetX, float targetY) {
        if (currentState == GameState.PLAYING && !net.isFiring()) { // Net hanya bisa ditembakkan jika sedang PLAYING dan net tidak sedang aktif
            net.fire(targetX, targetY);
            currentState = GameState.NET_FIRED; // Ubah state menjadi net ditembakkan
        }
    }
    
    // Dipanggil dari InputHandler saat player tekan spacebar
    public void handleSpaceBarPress() {
        if (currentState == GameState.STRUGGLING) {
            struggleBarValue += Constants.STRUGGLE_TAP_VALUE; // Tambah nilai bar
            if (struggleBarValue >= Constants.STRUGGLE_BAR_MAX_VALUE) { // Jika bar penuh
                succeedStruggle(); // Berhasil
            }
        } else if (currentState == GameState.PLAYING || currentState == GameState.NET_FIRED) {
            // PDF: "Tombol space digunakan untuk menghentikan permainan dan kembali pada tampilan awal." [cite: 35, 81]
            returnToMenu(true); // true = simpan skor saat quit manual
        }
    }

    private void succeedStruggle() {
        if (jellyfishInStruggle != null) {
            jar.addToJar(jellyfishInStruggle); // Tambah ke keranjang [cite: 28, 38, 74, 84]
            remainingTimeSeconds += Constants.TIME_BONUS_PER_CATCH_SECONDS; // Tambah waktu
            SoundManager.playSound("assets/sounds/success_sound.wav", false); // Efek suara berhasil
        }
        net.finishAttempt(); // Reset net
        jellyfishInStruggle = null;
        currentState = GameState.PLAYING; // Kembali ke state normal
    }

    private void failStruggle() {
        player.loseHeart(); // Kurangi nyawa
        SoundManager.playSound("assets/sounds/fail_sound.wav", false); // Efek suara gagal
        
        net.finishAttempt(); // Reset net
        // jellyfishInStruggle sudah di-remove dari handler, jadi dia "hilang"
        jellyfishInStruggle = null;
        currentState = GameState.PLAYING; // Kembali ke state normal

        if (player.getHearts() <= 0) { // Jika nyawa habis
            triggerGameOver("Nyawa Habis!");
        }
    }
    
    private void triggerGameOver(String message) {
        if (currentState == GameState.GAME_OVER) return; // Hindari trigger ganda

        currentState = GameState.GAME_OVER;
        gameTimerLoop.stop(); // Hentikan game loop utama
        if (countdownTimer != null) countdownTimer.stop(); // Hentikan timer per detik
        SoundManager.stopSound(gameMusicClip); // Hentikan musik game
        SoundManager.playSound("assets/sounds/gameover_sound.wav", false); // Efek suara game over
        
        saveGameResult(); // Simpan skor ke database
        gamePanel.repaint(); // Panggil repaint sekali lagi untuk menunjukkan pesan Game Over di panel

        // Tampilkan dialog JOptionPane
        JOptionPane.showMessageDialog(gamePanel, 
            message + "\n\nUsername: " + currentUsername + 
            "\nSkor Akhir: " + jar.getTotalScore() + 
            "\nUbur-ubur Terkumpul: " + jar.getCollectedCount(), 
            "Game Over", 
            JOptionPane.INFORMATION_MESSAGE);
        
        returnToMenu(false); // false = game over otomatis, skor sudah disimpan
    }

    private void saveGameResult() {
        if (currentUsername != null && !currentUsername.isEmpty() && jar != null) {
            try (TableHasil th = new TableHasil()) { // Gunakan try-with-resources
                GameData gameData = new GameData(currentUsername, jar.getTotalScore(), jar.getCollectedCount());
                th.insertOrUpdateHasil(gameData); // Simpan skor ke DB [cite: 29, 32, 75, 78]
            } catch (Exception ex) { // Lebih umum untuk menangkap SQLException dari konstruktor TableHasil juga
                System.err.println("Error saat menyimpan skor: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void returnToMenu(boolean saveScoreOnManualQuit) {
        if (saveScoreOnManualQuit && currentState != GameState.GAME_OVER) { // Hanya simpan jika quit manual & bukan karena game over
            saveGameResult();
        }

        gameTimerLoop.stop();
        if (countdownTimer != null) countdownTimer.stop();
        currentState = GameState.MENU; // Set state agar tidak ada lagi pemrosesan game
        SoundManager.stopSound(gameMusicClip); // Hentikan musik game saat kembali ke menu

        // Menutup window game saat ini
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(gamePanel);
        if (currentFrame != null) {
            currentFrame.dispose(); // Hapus window game dari memori
        }
        
        // Membuka MenuScreen baru
        SwingUtilities.invokeLater(() -> {
            MenuScreen menu = new MenuScreen();
            menu.setVisible(true);
        });
    }

    // Metode untuk menggambar semua entitas game
    public void renderGame(Graphics g) {
        jar.render(g);
        jellyfishHandler.renderJellyfish(g);
        player.render(g);
        net.render(g);
        // Jika jellyfish yang sedang di-struggle perlu digambar secara khusus (misal, di atas player)
        if(jellyfishInStruggle != null && currentState == GameState.STRUGGLING) {
            // Atur posisi jellyfishInStruggle dekat player jika perlu
            // float struggleX = player.getX() + player.getWidth()/2f - jellyfishInStruggle.getWidth()/2f;
            // float struggleY = player.getY() - jellyfishInStruggle.getHeight() - 5; // Di atas player
            // jellyfishInStruggle.setX(struggleX);
            // jellyfishInStruggle.setY(struggleY);
            jellyfishInStruggle.render(g); 
        }
    }

    // Getter untuk diakses oleh View (GamePanel) dan InputHandler
    public Player getPlayer() { return player; }
    public GameState getCurrentState() { return currentState; }
    public int getRemainingTime() { return remainingTimeSeconds; }
    public Jar getJar() { return jar; }
    public float getStruggleProgress() {
        if (currentState != GameState.STRUGGLING) return 0f;
        return Math.min(1.0f, (float) struggleBarValue / Constants.STRUGGLE_BAR_MAX_VALUE); // Pastikan tidak lebih dari 1.0
    }
}