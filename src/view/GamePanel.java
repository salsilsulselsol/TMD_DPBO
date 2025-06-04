package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics; // Ditambahkan untuk pengukuran teks yang lebih akurat
import java.awt.Graphics; 
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit; 
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import viewmodel.Constants;
import viewmodel.FontManager;
import viewmodel.GameLogic; // IMPORT FONT MANAGER

public class GamePanel extends JPanel {
    private GameLogic gameLogic;

    private Image bgFar;            
    private Image bgSand;           
    private Image bgForeground;     

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        setFocusable(true); 
        setBackground(new Color(0, 0, 50)); 

        loadBackgroundAssets(); 
        
        this.gameLogic = new GameLogic(this);
    }

    private void loadBackgroundAssets() {
        try {
            URL farUrl = getClass().getResource("/assets/images/far.png");
            if (farUrl != null) {
                bgFar = new ImageIcon(farUrl).getImage();
            } else {
                System.err.println("Aset tidak ditemukan: /assets/images/far.png");
            }

            URL sandUrl = getClass().getResource("/assets/images/sand.png");
            if (sandUrl != null) {
                bgSand = new ImageIcon(sandUrl).getImage();
            } else {
                System.err.println("Aset tidak ditemukan: /assets/images/sand.png");
            }

            URL fgUrl = getClass().getResource("/assets/images/foregound-merged.png"); // Pastikan nama file ini benar
            if (fgUrl != null) {
                bgForeground = new ImageIcon(fgUrl).getImage();
            } else {
                System.err.println("Aset tidak ditemukan: /assets/images/foregound-merged.png");
            }

        } catch (Exception e) {
            System.err.println("Error saat memuat aset latar belakang: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public GameLogic getGameLogic() {
        return gameLogic; 
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Matikan anti-aliasing untuk font pixel agar lebih tajam
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF); 

        // --- Menggambar Lapisan Latar Belakang ---
        if (bgFar != null) {
            g2d.drawImage(bgFar, 0, 0, getWidth(), getHeight(), this);
        } else {
            g2d.setColor(new Color(20, 40, 80)); 
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }

        if (bgSand != null) {
            g2d.drawImage(bgSand, 0, 0, getWidth(), getHeight(), this);
        }

        // --- Menggambar Entitas Game ---
        if (gameLogic != null) {
            gameLogic.renderGame(g2d); // Ini akan merender player, harpoon, ikan, ghost, dan Jar (jika kondisinya terpenuhi)
        }

        // --- Menggambar Lapisan Depan (Foreground) ---
        // Ditempatkan setelah renderGame agar entitas game bisa berada "di belakang" foreground ini jika diinginkan,
        // atau sebelum renderGame jika entitas game berada "di depan" foreground.
        // Untuk contoh saat ini, kita gambar setelah entitas game.
        if (bgForeground != null) {
            // Asumsi foregound-merged.png adalah untuk bagian bawah layar atau elemen dekoratif depan
            // Gambar sesuai ukuran asli atau stretch. Contoh: stretch ke lebar panel, posisi di bawah.
            int fgHeight = bgForeground.getHeight(this); // Dapatkan tinggi asli gambar foreground
             // Jika ingin stretch lebarnya:
            g2d.drawImage(bgForeground, 0, getHeight() - fgHeight, getWidth(), fgHeight, this);
            // Atau jika ingin ukuran asli dan di tengah bawah:
            // int fgWidth = bgForeground.getWidth(this);
            // g2d.drawImage(bgForeground, (getWidth() - fgWidth) / 2, getHeight() - fgHeight, fgWidth, fgHeight, this);
        }


        // --- Menggambar Elemen UI ---
        if (gameLogic != null) {
            drawUI(g2d); 
        }
        
        Toolkit.getDefaultToolkit().sync();
    }
    
    private void drawUI(Graphics2D g2d) { 
        if (gameLogic == null) return;

        // Set font dan warna default untuk UI
        g2d.setFont(FontManager.getPressStart2PRegular(12f)); // Ukuran default, sesuaikan jika perlu
        g2d.setColor(new Color(255, 230, 150)); 

        // Menampilkan Skor dan Jumlah Ikan
        if (gameLogic.getCurrentState() != GameLogic.GameState.MENU) { 
            if (gameLogic.getJar() != null) {
                String scoreText = "Skor: " + gameLogic.getJar().getTotalScore();
                String countText = "Ikan: " + gameLogic.getJar().getCollectedCount();
                
                g2d.drawString(scoreText, 25, 40);
                g2d.drawString(countText, 25, 70);
            }
        }

        // Menampilkan Waktu Tersisa
        String timeText = "Waktu: " + gameLogic.getRemainingTime();
        FontMetrics fm = g2d.getFontMetrics(); // Dapatkan FontMetrics setelah font di-set
        int timeTextWidth = fm.stringWidth(timeText);
        g2d.drawString(timeText, Constants.GAME_WIDTH - timeTextWidth - 25, 40);

        // Menampilkan Hati Pemain
        if (gameLogic.getPlayer() != null) {
             String heartsText = "Hati: " + gameLogic.getPlayer().getHearts();
             // FontMetrics fm sudah didapatkan sebelumnya dan masih valid jika font sama
             int heartsTextWidth = fm.stringWidth(heartsText);
             g2d.drawString(heartsText, Constants.GAME_WIDTH - heartsTextWidth - 25, 70);
        }

        // Menampilkan Progress Bar untuk "Struggle"
        if (gameLogic.getCurrentState() == GameLogic.GameState.STRUGGLING) {
            int barWidth = 280; 
            int barHeight = 35;
            int barX = (Constants.GAME_WIDTH - barWidth) / 2;
            int barY = Constants.GAME_HEIGHT - 90; 
            float progress = gameLogic.getStruggleProgress();

            g2d.setColor(new Color(0, 0, 0, 100)); 
            g2d.fillRoundRect(barX - 3, barY - 3, barWidth + 6, barHeight + 6, 20, 20);
            
            g2d.setColor(Color.DARK_GRAY); 
            g2d.fillRoundRect(barX, barY, barWidth, barHeight, 15, 15);
            
            g2d.setColor(new Color(30, 220, 30)); 
            g2d.fillRoundRect(barX + 2, barY + 2, (int)((barWidth - 4) * progress), barHeight - 4, 10, 10); 
            
            // Teks untuk prompt struggle
            g2d.setFont(FontManager.getPressStart2PRegular(10f)); // Ukuran font lebih kecil untuk prompt
            g2d.setColor(Color.WHITE);
            String strugglePrompt = "TEKAN SPACE SEKUATNYA!";
            FontMetrics fmStruggle = g2d.getFontMetrics(); // Dapatkan FontMetrics untuk font prompt
            int promptWidth = fmStruggle.stringWidth(strugglePrompt);
            // Penyesuaian posisi Y untuk teks prompt agar di atas bar
            g2d.drawString(strugglePrompt, barX + (barWidth - promptWidth) / 2 , barY - fmStruggle.getDescent() - 5); // 5 adalah padding tambahan
        }
        
        // Menampilkan Pesan Game Over
        if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER) {
            g2d.setColor(new Color(0, 0, 0, 190)); 
            g2d.fillRect(0, Constants.GAME_HEIGHT / 2 - 70, Constants.GAME_WIDTH, 140);

            g2d.setFont(FontManager.getPressStart2PRegular(40f)); // Ukuran font besar untuk Game Over
            g2d.setColor(new Color(255, 50, 50)); 
            String gameOverMsg = "GAME OVER";
            FontMetrics fmGameOver = g2d.getFontMetrics(); // Dapatkan FontMetrics untuk font Game Over
            int msgWidth = fmGameOver.stringWidth(gameOverMsg);
            // Menengahkan teks Game Over secara vertikal dan horizontal
            int textAscent = fmGameOver.getAscent();
            int textDescent = fmGameOver.getDescent();
            int textHeight = textAscent + textDescent; // Lebih akurat menggunakan ascent + descent untuk tinggi
            g2d.drawString(gameOverMsg, 
                           (Constants.GAME_WIDTH - msgWidth) / 2, 
                           Constants.GAME_HEIGHT / 2 - (textHeight / 2) + textAscent - 5); // Penyesuaian Y agar lebih tengah
        }
    }
}