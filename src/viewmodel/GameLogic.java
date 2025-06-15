package viewmodel;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import model.*;
import view.GamePanel;
import view.MenuScreen;

// Kelas ini adalah otak dari seluruh permainan, mengelola state, game loop, dan interaksi antar objek.
public class GameLogic implements ActionListener {
    // Enum untuk merepresentasikan semua kemungkinan state dalam permainan (State Machine).
    public enum GameState {
        MENU, PLAYING,
        HARPOON_FIRED,
        STRUGGLING,
        FISH_MOVING_TO_JAR,
        GAME_OVER
    }

    // Komponen utama View dan game loop
    private GamePanel gamePanel;
    private Timer gameTimerLoop; // Timer utama yang menjalankan game (sekitar 60 FPS).

    // Objek-objek utama dalam game (Model)
    private Player player;
    private Harpoon harpoon;
    private Jar jar;
    private EntityHandler entityHandler;
    private List<Effect> activeEffects; // Daftar efek visual sementara (cth: ledakan).

    // Variabel untuk state dan data permainan
    private char nextStruggleKey;
    private String currentUsername;
    private String gameOverMessage;
    private GameState currentState; // State permainan saat ini.
    private Timer returnToMenuTimer; // Timer untuk penundaan layar game over.

    // Variabel untuk waktu permainan
    private int remainingTimeSeconds;
    private Timer countdownTimer; // Timer yang berjalan setiap detik untuk mengurangi waktu.

    // Variabel untuk mekanisme 'Struggle'
    private int struggleBarValue;
    private long struggleStartTimeMs;
    private GameObject gameObjectInStruggle;

    // Variabel untuk animasi ikan ke keranjang
    private GameObject fishMovingToJar;
    private float fishToJarSpeed = 25.0f;

    // Konstruktor, menghubungkan GameLogic dengan GamePanel.
    public GameLogic(GamePanel panel) {
        this.gamePanel = panel;
        this.gameTimerLoop = new Timer(1000 / 60, this); // Set timer untuk refresh ~60x per detik.
        this.currentState = GameState.MENU; // State awal adalah menu.
    }

    // Menyiapkan semua objek yang diperlukan untuk sesi permainan baru.
    private void initializeGameEntities() {
        // Inisialisasi Player dengan semua properti animasinya.
        player = new Player(
            Constants.PLAYER_START_X, Constants.PLAYER_START_Y,
            Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT,
            Constants.PLAYER_INITIAL_HEARTS,
            "/assets/images/player-idle.png", "/assets/images/player-swiming.png", "/assets/images/player-hurt.png",
            80, 80, 6, 7, 5, 120, 100
        );

        // Inisialisasi objek-objek lain yang bergantung pada Player atau posisi default.
        harpoon = new Harpoon(player);
        jar = new Jar(Constants.JAR_X, Constants.JAR_Y, Constants.JAR_WIDTH, Constants.JAR_HEIGHT, "/assets/images/barrel.png");
        jar.reset(); // Mengosongkan keranjang.

        entityHandler = new EntityHandler();
        entityHandler.reset(); // Mengosongkan daftar entitas.
        
        activeEffects = new ArrayList<>();

        // Reset semua variabel state permainan ke nilai awal.
        gameObjectInStruggle = null;
        fishMovingToJar = null;
        remainingTimeSeconds = Constants.INITIAL_GAME_TIME_SECONDS;
        struggleBarValue = 0;
    }
    
    // Metode ini dipanggil dari luar (MenuScreen) untuk memulai permainan.
    public void startGame(String username) {
        this.currentUsername = username;
        initializeGameEntities(); // Buat dan reset semua objek game.
        currentState = GameState.PLAYING; // Ubah state menjadi PLAYING.
        
        // Setup dan mulai timer untuk hitung mundur waktu permainan.
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }
        countdownTimer = new Timer(1000, e -> { // Timer ini berjalan setiap 1 detik.
            // Kurangi waktu hanya jika game sedang berjalan aktif.
            if (currentState != GameState.GAME_OVER && currentState != GameState.MENU && currentState != GameState.FISH_MOVING_TO_JAR) {
                remainingTimeSeconds--;
                if (remainingTimeSeconds <= 0) {
                    remainingTimeSeconds = 0;
                    triggerGameOver("Waktu Habis!"); // Akhiri permainan jika waktu habis.
                }
            }
        });
        countdownTimer.start();

        // Mulai game loop utama jika belum berjalan.
        if (!gameTimerLoop.isRunning()) {
            gameTimerLoop.start();
        }
        gamePanel.requestFocusInWindow(); // Minta fokus agar input keyboard bisa diterima.
    }

    // Metode ini adalah jantung dari game loop, dipanggil oleh gameTimerLoop.
    @Override
    public void actionPerformed(ActionEvent e) {
        // Logika utama State Machine: jalankan metode update yang sesuai dengan state saat ini.
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
        
        updateEffects(); // Selalu update efek visual seperti ledakan.

        // Memicu penggambaran ulang layar jika tidak di menu.
        if (currentState != GameState.MENU) {
            gamePanel.repaint();
        }
    }

    // Update logika saat state normal (bermain).
    private void updatePlayingState() {
        player.update();
        entityHandler.updateEntities();
    }

    // Update logika saat harpun sudah ditembakkan.
    private void updateHarpoonFiredState() {
        player.update();
        harpoon.update();
        entityHandler.updateEntities();

        // Jika harpun belum mengenai apa-apa, cek tabrakan dengan ikan.
        if (harpoon.getHookedObject() == null) {
            List<GameObject> entities = entityHandler.getEntities();
            for (GameObject entity : entities) {
                if (entity instanceof Fish) {
                    if (harpoon.getTipCollisionBox().intersects(entity.getCollisionBox())) {
                        harpoon.hookObject(entity); // Kaitkan ikan pertama yang kena.
                        break;
                    }
                }
            }
            // Jika harpun sudah tidak bergerak (misal, mencapai batas), kembali ke state PLAYING.
            if (!harpoon.isFiring()) {
                currentState = GameState.PLAYING;
                if (player != null) player.resetMovementFlags();
            }
        } else { // Jika harpun sudah mengait ikan...
            // ...cek apakah ikan yang dikait sudah ditarik sampai ke player.
            if (player.getCollisionBox().intersects(harpoon.getHookedObject().getCollisionBox())) {
                startStruggle(harpoon.getHookedObject()); // Mulai mini-game struggle.
            }
        }
    }
    
    // Menyiapkan semua variabel untuk memulai mini-game struggle.
    private void startStruggle(GameObject caughtEntity) {
        if (player != null) player.resetMovementFlags();
        currentState = GameState.STRUGGLING; // Ubah state.
        this.gameObjectInStruggle = caughtEntity;
        this.fishMovingToJar = null;
        this.struggleBarValue = 0; // Reset progress bar struggle.
        this.struggleStartTimeMs = System.currentTimeMillis(); // Catat waktu mulai.
        this.nextStruggleKey = 'Q'; // Tentukan tombol pertama yang harus ditekan.
    }

    // Update logika saat state 'struggle'.
    private void updateStrugglingState() {
        // Jika waktu struggle sudah habis, gagalkan tangkapan.
        if (System.currentTimeMillis() - struggleStartTimeMs > Constants.STRUGGLE_TIME_LIMIT_MS) {
            failStruggle();
        }
    }

    // Update logika saat ikan yang berhasil ditangkap sedang bergerak ke keranjang.
    private void updateFishMovingToJarState() {
        if (fishMovingToJar == null || jar == null) {
            currentState = GameState.PLAYING; // Kembali ke state normal jika ada error.
            if (player != null) player.resetMovementFlags();
            return;
        }

        // Tentukan koordinat target di dekat keranjang.
        float targetX = jar.getX() + jar.getWidth() / 2f - fishMovingToJar.getWidth() / 2f;
        float targetY = jar.getY() + jar.getHeight() / 4f - fishMovingToJar.getHeight() / 2f;
        float currentX = fishMovingToJar.getX();
        float currentY = fishMovingToJar.getY();

        // Hitung jarak dan arah gerakan.
        float dx = targetX - currentX;
        float dy = targetY - currentY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Jika sudah sampai di target...
        if (distance < fishToJarSpeed) {
            if (fishMovingToJar instanceof Fish) {
                jar.addToJar((Fish) fishMovingToJar); // Tambahkan ikan ke keranjang.
                remainingTimeSeconds += Constants.TIME_BONUS_PER_CATCH_SECONDS; // Tambah bonus waktu.
                SoundManager.playSound("catch"); // Mainkan suara tangkapan.
            }
            fishMovingToJar = null;
            currentState = GameState.PLAYING; // Kembali ke state normal.
            if (player != null) player.resetMovementFlags();
        } else { // Jika belum sampai, gerakkan ikan menuju target.
            float moveX = (dx / (float)distance) * fishToJarSpeed;
            float moveY = (dy / (float)distance) * fishToJarSpeed;
            fishMovingToJar.setX(currentX + moveX);
            fishMovingToJar.setY(currentY + moveY);
            fishMovingToJar.update();
        }

        // Tetap update player dan entitas lain agar dunia game tetap hidup.
        if (player != null) player.update();
        entityHandler.updateEntities();
    }

    // Handler untuk input tembak dari InputHandler.
    public void handlePlayerFireHarpoon(float targetX, float targetY) {
        // Hanya bisa menembak saat state PLAYING dan harpun tidak sedang ditembakkan.
        if (currentState == GameState.PLAYING && !harpoon.isFiring()) {
            harpoon.fire(targetX, targetY);
            currentState = GameState.HARPOON_FIRED;
        }
    }
    
    // Handler untuk input spasi dari InputHandler (jeda permainan).
    public void handleSpaceBarPress() {
        if (currentState == GameState.PLAYING) {
            showPauseConfirmation();
        }
    }

    // Handler untuk input 'Esc' dari InputHandler (keluar ke menu).
    public void handleEscapeKeyPress() {
        if (currentState == GameState.PLAYING ||
            currentState == GameState.HARPOON_FIRED ||
            currentState == GameState.STRUGGLING ||
            currentState == GameState.FISH_MOVING_TO_JAR) {
            returnToMenu(true);
        }
    }
    
    // Handler untuk input 'Q' & 'E' saat struggle.
    public void handleStruggleKeyPress(int keyCode) {
        if (currentState != GameState.STRUGGLING) return; // Hanya proses jika state-nya STRUGGLING.

        // Cek apakah tombol yang ditekan sesuai dengan urutan (Q lalu E lalu Q dst.).
        if (keyCode == KeyEvent.VK_Q && nextStruggleKey == 'Q') {
            performStruggleAction();
            nextStruggleKey = 'E'; // Tombol berikutnya adalah E.
        } else if (keyCode == KeyEvent.VK_E && nextStruggleKey == 'E') {
            performStruggleAction();
            nextStruggleKey = 'Q'; // Tombol berikutnya adalah Q.
        }
    }

    // Logika jika berhasil memenangkan struggle.
    private void succeedStruggle() {
        if (gameObjectInStruggle != null && gameObjectInStruggle instanceof Fish) {
            entityHandler.removeEntity(gameObjectInStruggle); // Hapus ikan dari daftar entitas aktif.
            
            this.fishMovingToJar = gameObjectInStruggle; // Set ikan ini untuk dianimasikan ke keranjang.
            this.gameObjectInStruggle = null;
            currentState = GameState.FISH_MOVING_TO_JAR; // Ganti state.
        } else {
            currentState = GameState.PLAYING;
            if (player != null) player.resetMovementFlags();
        }
        harpoon.finishAttempt(); // Reset status harpun.
    }

    // Logika jika gagal dalam struggle (waktu habis atau salah pencet).
    private void failStruggle() {
        // Mainkan suara yang berbeda jika ini adalah pukulan terakhir yang membuat kalah.
        if (player.getHearts() > 1) {
            SoundManager.playSound("hit");
        } else {
            SoundManager.playSound("fail");
        }

        player.loseHeart(); // Kurangi nyawa player.
        player.playHurtAnimation(); // Mainkan animasi 'terluka'.

        // Buat efek visual 'hit' di posisi player.
        Effect hitEffect = new Effect(
            player.getX() + (player.getWidth() / 2f) - (64 / 2f),
            player.getY() + (player.getHeight() / 2f) - (64 / 2f),
            64, 64, "/assets/images/hit.png", 31, 32, 3, 100
        );
        activeEffects.add(hitEffect);

        // Reset semua status terkait struggle dan harpun.
        harpoon.finishAttempt();
        gameObjectInStruggle = null;
        currentState = GameState.PLAYING;
        if (player != null) player.resetMovementFlags();

        // Jika nyawa habis, picu game over.
        if (player.getHearts() <= 0) {
            triggerGameOver("Nyawa Habis!");
        }
    }

    // Cek tabrakan antara player dan hantu.
    private void checkPlayerGhostCollision() {
        if (player == null || entityHandler == null || currentState != GameState.PLAYING) return;
        List<GameObject> currentEntities = new ArrayList<>(entityHandler.getEntities());

        for (GameObject entity : currentEntities) {
            if (entity instanceof Ghost) {
                if (player.getCollisionBox().intersects(entity.getCollisionBox())) {
                    // Logika yang sama seperti failStruggle: kurangi nyawa, mainkan suara/animasi/efek.
                    if (player.getHearts() > 1) {
                        SoundManager.playSound("hit");
                    } else {
                        SoundManager.playSound("fail");
                    }

                    player.loseHeart();
                    player.playHurtAnimation();

                    Effect hitEffect = new Effect(
                        player.getX() + (player.getWidth() / 2f) - (64 / 2f),
                        player.getY() + (player.getHeight() / 2f) - (64 / 2f),
                        64, 64, "/assets/images/hit.png", 31, 32, 3, 100
                    );
                    activeEffects.add(hitEffect);

                    entityHandler.removeEntity(entity); // Hapus hantu yang menabrak.
                    if (player.getHearts() <= 0) {
                        triggerGameOver("Nyawa Habis karena Hantu!");
                    }
                    break; // Keluar dari loop setelah satu tabrakan diproses.
                }
            }
        }
    }
    
    // Memicu akhir permainan.
    private void triggerGameOver(String message) {
        if (currentState == GameState.GAME_OVER) return; // Hindari memicu game over berkali-kali.

        currentState = GameState.GAME_OVER;
        this.gameOverMessage = message;

        if (player != null) player.resetMovementFlags();
        
        // Hentikan semua timer dan musik.
        gameTimerLoop.stop();
        if (countdownTimer != null) countdownTimer.stop();
        SoundManager.stopBGM();
        SoundManager.playSound("fail");

        saveGameResult(); // Simpan skor ke database.
        gamePanel.repaint(); // Gambar ulang layar untuk menampilkan UI game over.

        // Atur timer untuk kembali ke menu secara otomatis setelah beberapa detik.
        returnToMenuTimer = new Timer(5000, e -> returnToMenu(false));
        returnToMenuTimer.setRepeats(false);
        returnToMenuTimer.start();
    }

    // Metode untuk melewati penundaan layar game over (dipanggil oleh InputHandler).
    public void skipGameOverDelay() {
        if (currentState == GameState.GAME_OVER && returnToMenuTimer != null && returnToMenuTimer.isRunning()) {
            returnToMenuTimer.stop(); // Hentikan timer penunda.
            returnToMenu(false);      // Langsung kembali ke menu.
        }
    }

    // Menyimpan hasil permainan ke database.
    private void saveGameResult() {
        if (currentUsername != null && !currentUsername.isEmpty() && jar != null) {
            // Gunakan try-with-resources agar koneksi database otomatis ditutup.
            try (TableHasil th = new TableHasil()) {
                GameData gameData = new GameData(currentUsername, jar.getTotalScore(), jar.getCollectedCount());
                th.insertOrUpdateHasil(gameData); // Panggil metode untuk insert/update skor.
            } catch (Exception ex) {
                System.err.println("Error saat menyimpan skor: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    // Membersihkan state saat ini dan kembali ke menu utama.
    private void returnToMenu(boolean saveScoreOnManualQuit) {
        // Simpan skor jika pemain keluar manual di tengah permainan.
        if (saveScoreOnManualQuit && currentState != GameState.GAME_OVER) {
            saveGameResult();
        }
        
        // Reset dan bersihkan semua objek dan state.
        if (player != null) player.resetMovementFlags();
        if (activeEffects != null) activeEffects.clear();
        gameTimerLoop.stop();
        if (countdownTimer != null) countdownTimer.stop();
        currentState = GameState.MENU;
        SoundManager.stopBGM();

        // Tutup window game saat ini.
        JFrame currentFrame = (JFrame) SwingUtilities.getWindowAncestor(gamePanel);
        if (currentFrame != null) {
            currentFrame.dispose();
        }
        // Buat dan tampilkan window menu yang baru.
        SwingUtilities.invokeLater(() -> {
            MenuScreen menu = new MenuScreen();
            menu.setVisible(true);
        });
    }

    // Mendelegasikan tugas render ke semua objek yang perlu digambar.
    public void renderGame(Graphics g) {
        entityHandler.renderEntities(g);
        player.render(g);
        harpoon.render(g);
        
        // Render keranjang hanya pada state tertentu.
        if (currentState == GameState.STRUGGLING || currentState == GameState.FISH_MOVING_TO_JAR) {
            if (jar != null) jar.render(g);
        }
        
        // Render ikan yang sedang di-struggle atau dianimasikan.
        if (currentState == GameState.STRUGGLING && gameObjectInStruggle != null) {
            gameObjectInStruggle.render(g);
        } else if (currentState == GameState.FISH_MOVING_TO_JAR && fishMovingToJar != null) {
            fishMovingToJar.render(g);
        }

        // Render semua efek visual yang aktif.
        for (Effect effect : activeEffects) {
            effect.render(g);
        }
    }

    // Melakukan aksi saat struggle (menambah progress bar).
    private void performStruggleAction() {
        float tapEffectiveness = Constants.STRUGGLE_TAP_VALUE;
        // Jika ikan punya faktor kesulitan, sesuaikan efektivitas tekanan tombol.
        if (gameObjectInStruggle instanceof Fish) {
            Fish strugglingFish = (Fish) gameObjectInStruggle;
            if (strugglingFish.getStruggleFactor() > 0) {
                tapEffectiveness /= strugglingFish.getStruggleFactor();
            }
        }
        struggleBarValue += tapEffectiveness;

        // Jika progress bar penuh, berhasil menangkap ikan.
        if (struggleBarValue >= Constants.STRUGGLE_BAR_MAX_VALUE) {
            succeedStruggle();
        }
    }

    // Menampilkan dialog konfirmasi saat permainan dijeda.
    private void showPauseConfirmation() {
        gameTimerLoop.stop(); // Hentikan sementara game loop.
        if (countdownTimer != null) countdownTimer.stop();

        int choice = JOptionPane.showConfirmDialog(
            gamePanel, "Apakah Anda yakin ingin kembali ke Menu Utama?",
            "Jeda Permainan", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE
        );

        // Jika pemain memilih 'Yes', kembali ke menu.
        if (choice == JOptionPane.YES_OPTION) {
            returnToMenu(true);
        } else { // Jika tidak, lanjutkan permainan.
            gameTimerLoop.start();
            if (countdownTimer != null) countdownTimer.start();
            gamePanel.requestFocusInWindow();
        }
    }
    
    // Mengupdate dan membersihkan efek visual yang sudah selesai.
    private void updateEffects() {
        Iterator<Effect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            Effect effect = iterator.next();
            effect.update();
            if (effect.isFinished()) {
                iterator.remove(); // Hapus efek dari daftar jika animasinya sudah selesai.
            }
        }
    }

    // Getter untuk mendapatkan data yang dibutuhkan oleh kelas lain (misal: GamePanel untuk UI).
    public long getStruggleStartTimeMs() { return struggleStartTimeMs; }
    public Player getPlayer() { return player; }
    public String getUsername() { return currentUsername; }
    public String getGameOverMessage() { return gameOverMessage; }
    public GameState getCurrentState() { return currentState; }
    public int getRemainingTime() { return remainingTimeSeconds; }
    public Jar getJar() { return jar; }
    public float getStruggleProgress() {
        if (currentState != GameState.STRUGGLING) return 0f;
        return Math.min(1.0f, (float) struggleBarValue / Constants.STRUGGLE_BAR_MAX_VALUE);
    }
}