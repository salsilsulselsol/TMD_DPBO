package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics; // Untuk opsi rendering yang lebih baik
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit; // Untuk anti-aliasing
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import viewmodel.Constants;
import viewmodel.GameLogic;

public class GamePanel extends JPanel {
    private GameLogic gameLogic;

    // Variabel untuk gambar latar belakang
    private Image bgFar;            // Lapisan paling jauh
    private Image bgSand;           // Lapisan tengah (dasar laut)
    private Image bgForeground;     // Lapisan depan (elemen dekoratif)

    // Opsional: Posisi X untuk parallax sederhana jika game Anda memiliki pergerakan horizontal dominan
    // Untuk game dengan gerakan 4 arah, parallax sederhana bisa jadi tidak terlalu efektif
    // atau memerlukan logika yang lebih kompleks. Kita akan buat statis dulu.
    // private float bgFarX = 0;
    // private float bgSandX = 0;
    // private float bgForegroundX = 0;


    public GamePanel() {
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        setFocusable(true); // Agar panel bisa menerima input keyboard
        setBackground(new Color(0, 0, 50)); // Warna dasar jika gambar gagal dimuat

        loadBackgroundAssets(); // Memuat semua aset latar belakang
        
        // GameLogic membutuhkan referensi ke GamePanel untuk memanggil repaint()
        this.gameLogic = new GameLogic(this); 
    }

    private void loadBackgroundAssets() {
        try {
            // Muat lapisan paling jauh
            URL farUrl = getClass().getResource("/assets/images/far.png");
            if (farUrl != null) {
                bgFar = new ImageIcon(farUrl).getImage();
            } else {
                System.err.println("Aset tidak ditemukan: /assets/images/far.png");
            }

            // Muat lapisan pasir/dasar laut
            URL sandUrl = getClass().getResource("/assets/images/sand.png");
            if (sandUrl != null) {
                bgSand = new ImageIcon(sandUrl).getImage();
            } else {
                System.err.println("Aset tidak ditemukan: /assets/images/sand.png");
            }

            // Muat lapisan foreground (gunakan foregound-merged.png jika itu yang paling sesuai)
            URL fgUrl = getClass().getResource("/assets/images/foregound-merged.png");
            if (fgUrl != null) {
                bgForeground = new ImageIcon(fgUrl).getImage();
            } else {
                System.err.println("Aset tidak ditemukan: /assets/images/foregound-merged.png");
                // Anda bisa mencoba memuat foreground-1.png atau foreground-2.png sebagai alternatif
                // URL fg1Url = getClass().getResource("/assets/images/foreground-1.png");
                // if (fg1Url != null) bgForeground = new ImageIcon(fg1Url).getImage();
            }

        } catch (Exception e) {
            System.err.println("Error saat memuat aset latar belakang: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public GameLogic getGameLogic() {
        return gameLogic; // Agar InputHandler bisa mengakses GameLogic
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Penting untuk membersihkan panel sebelum menggambar ulang

        // Aktifkan anti-aliasing untuk teks dan bentuk yang lebih halus (opsional)
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // --- Menggambar Lapisan Latar Belakang ---
        // 1. Lapisan paling jauh (far.png)
        if (bgFar != null) {
            // Gambar agar memenuhi seluruh panel (bisa di-stretch atau di-tile)
            // Untuk tile, Anda perlu logika looping jika gambar lebih kecil dari panel
            g2d.drawImage(bgFar, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback jika gambar tidak ada
            g2d.setColor(new Color(20, 40, 80)); // Warna biru tua
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        // 2. Lapisan tengah (sand.png)
        if (bgSand != null) {
            // Asumsi sand.png adalah gambar yang bisa di-stretch atau memiliki ukuran yang pas
            // Anda mungkin ingin menempatkannya di bagian bawah layar atau sesuai desain.
            // Contoh: gambar memenuhi layar, di atas bgFar
            g2d.drawImage(bgSand, 0, 0, getWidth(), getHeight(), this);
            // Atau jika sand.png adalah untuk dasar laut saja:
            // int sandHeight = bgSand.getHeight(this);
            // g2d.drawImage(bgSand, 0, getHeight() - sandHeight, getWidth(), sandHeight, this);
        }

        // --- Menggambar Entitas Game (Player, Jellyfish, Net, Jar) ---
        if (gameLogic != null) {
            // GameLogic akan memanggil metode render dari masing-masing objek game
            // Ini memastikan objek game digambar di atas lapisan background dasar
            gameLogic.renderGame(g2d); 
        }

        // 3. Lapisan depan (foregound-merged.png)
        // Lapisan ini akan digambar di atas entitas game jika diinginkan, atau
        // di bawah entitas game jika merupakan bagian dari "arena" permainan.
        // Untuk contoh ini, kita gambar di bawah entitas game (sebelum drawUI),
        // yang berarti entitas game bisa bergerak "di depan" foreground ini.
        // Jika ingin efek sebaliknya (foreground menutupi sebagian pemain), gambar setelah gameLogic.renderGame().
        if (bgForeground != null) {
            // Asumsi foregound-merged.png adalah untuk bagian bawah layar
            int fgHeight = bgForeground.getHeight(this);
            // Sesuaikan skala lebar jika perlu, atau gunakan lebar asli gambar
            // g2d.drawImage(bgForeground, 0, getHeight() - fgHeight, bgForeground.getWidth(this), fgHeight, this);
            // Atau stretch lebarnya:
            g2d.drawImage(bgForeground, 0, getHeight() - fgHeight, getWidth(), fgHeight, this);
        }


        // --- Menggambar Elemen UI (Skor, Waktu, Hati, dll.) ---
        // UI selalu digambar paling akhir agar di atas segalanya.
        if (gameLogic != null) {
            drawUI(g2d); 
        }
        
        // Sinkronisasi toolkit (penting di beberapa sistem untuk animasi yang lebih halus)
        Toolkit.getDefaultToolkit().sync();
    }
    
    private void drawUI(Graphics2D g2d) { // Menggunakan Graphics2D
        if (gameLogic == null) return;

        // Atur font dan warna default untuk UI
        g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 22)); // Font yang lebih ceria
        g2d.setColor(new Color(255, 230, 150)); // Warna kuning pucat untuk teks UI

        // Menampilkan Skor dan Jumlah Ubur-ubur
        if (gameLogic.getJar() != null) {
            String scoreText = "Skor: " + gameLogic.getJar().getTotalScore();
            String countText = "Ubur-ubur: " + gameLogic.getJar().getCollectedCount();
            g2d.drawString(scoreText, 25, 40);
            g2d.drawString(countText, 25, 70);
        }

        // Menampilkan Waktu Tersisa (pojok kanan atas)
        String timeText = "Waktu: " + gameLogic.getRemainingTime();
        int timeTextWidth = g2d.getFontMetrics().stringWidth(timeText);
        g2d.drawString(timeText, Constants.GAME_WIDTH - timeTextWidth - 25, 40);

        // Menampilkan Hati Pemain
        if (gameLogic.getPlayer() != null) {
             String heartsText = "Hati: " + gameLogic.getPlayer().getHearts();
             int heartsTextWidth = g2d.getFontMetrics().stringWidth(heartsText);
             g2d.drawString(heartsText, Constants.GAME_WIDTH - heartsTextWidth - 25, 70);
        }

        // Menampilkan Progress Bar untuk "Struggle"
        if (gameLogic.getCurrentState() == GameLogic.GameState.STRUGGLING) {
            int barWidth = 280; // Sedikit lebih lebar
            int barHeight = 35;
            int barX = (Constants.GAME_WIDTH - barWidth) / 2;
            int barY = Constants.GAME_HEIGHT - 90; // Sedikit lebih ke atas
            float progress = gameLogic.getStruggleProgress();

            // Gambar border luar dan background bar dengan efek bayangan sederhana
            g2d.setColor(new Color(0, 0, 0, 100)); // Bayangan
            g2d.fillRoundRect(barX - 3, barY - 3, barWidth + 6, barHeight + 6, 20, 20);
            
            g2d.setColor(Color.DARK_GRAY); // Warna background bar
            g2d.fillRoundRect(barX, barY, barWidth, barHeight, 15, 15);
            
            g2d.setColor(new Color(30, 220, 30)); // Warna progress (hijau cerah)
            g2d.fillRoundRect(barX + 2, barY + 2, (int)((barWidth - 4) * progress), barHeight - 4, 10, 10); // Progress dengan padding
            
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Comic Sans MS", Font.BOLD, 20));
            String strugglePrompt = "TEKAN SPACE SEKUATNYA!";
            int promptWidth = g2d.getFontMetrics().stringWidth(strugglePrompt);
            g2d.drawString(strugglePrompt, barX + (barWidth - promptWidth) / 2 , barY - 15);
        }
        
        // Menampilkan Pesan Game Over (jika tidak ditangani oleh JOptionPane saja)
        // Jika JOptionPane sudah cukup, bagian ini bisa dihilangkan atau disederhanakan.
        if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER) {
            // Latar belakang semi-transparan untuk pesan
            g2d.setColor(new Color(0, 0, 0, 190)); // Hitam semi-transparan
            g2d.fillRect(0, Constants.GAME_HEIGHT / 2 - 70, Constants.GAME_WIDTH, 140);

            g2d.setColor(new Color(255, 50, 50)); // Merah terang untuk teks Game Over
            g2d.setFont(new Font("Impact", Font.BOLD, 80)); // Font yang mencolok
            String gameOverMsg = "GAME OVER";
            int msgWidth = g2d.getFontMetrics().stringWidth(gameOverMsg);
            g2d.drawString(gameOverMsg, (Constants.GAME_WIDTH - msgWidth) / 2, Constants.GAME_HEIGHT / 2 + 35);
        }
    }

    // Jika Anda ingin implementasi parallax scrolling sederhana:
    // Metode ini bisa dipanggil dari GameLogic setelah player bergerak
    /*
    public void updateBackgroundOnPlayerMove(float playerDeltaX, float playerDeltaY) {
        // Contoh: Hanya parallax horizontal, lapisan jauh bergerak lebih lambat
        // bgFarX -= playerDeltaX * 0.05f;
        // bgSandX -= playerDeltaX * 0.2f;
        // bgForegroundX -= playerDeltaX * 0.4f; // Jika foreground juga bergerak

        // Tambahkan logika untuk looping gambar jika tileable
        // if (bgFar != null && bgFar.getWidth(this) > 0) {
        //     int farW = bgFar.getWidth(this);
        //     bgFarX %= farW; // Loop posisi X
        // }
        // Lakukan hal serupa untuk bgSandX dan bgForegroundX jika perlu
    }
    */
}