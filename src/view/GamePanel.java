package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
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
import viewmodel.GameLogic;

public class GamePanel extends JPanel {
    private GameLogic gameLogic;

    private Image bgFar;
    private Image bgSand;
    private Image bgForeground;
    private Image heartFullImage;
    private Image heartEmptyImage;

    public GamePanel() {
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        setFocusable(true);
        setBackground(new Color(0, 0, 50));
        
        // GamePanel membuat GameLogic-nya sendiri, ini cara yang lebih stabil
        this.gameLogic = new GameLogic(this); 
        
        loadAssets();
    }

    private void loadAssets() {
        try {
            URL farUrl = getClass().getResource("/assets/images/far.png");
            if (farUrl != null) bgFar = new ImageIcon(farUrl).getImage();
            
            URL sandUrl = getClass().getResource("/assets/images/sand.png");
            if (sandUrl != null) bgSand = new ImageIcon(sandUrl).getImage();
            
            URL fgUrl = getClass().getResource("/assets/images/foregound-merged.png");
            if (fgUrl != null) bgForeground = new ImageIcon(fgUrl).getImage();

            URL heartFullUrl = getClass().getResource("/assets/images/heart-full.png");
            if (heartFullUrl != null) heartFullImage = new ImageIcon(heartFullUrl).getImage();

            URL heartEmptyUrl = getClass().getResource("/assets/images/heart-empty.png");
            if (heartEmptyUrl != null) heartEmptyImage = new ImageIcon(heartEmptyUrl).getImage();

        } catch (Exception e) {
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
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        if (bgFar != null) g2d.drawImage(bgFar, 0, 0, getWidth(), getHeight(), this);
        if (bgSand != null) g2d.drawImage(bgSand, 0, 0, getWidth(), getHeight(), this);

        if (gameLogic != null) {
            gameLogic.renderGame(g2d);
        }

        if (bgForeground != null) {
            int fgHeight = bgForeground.getHeight(this);
            g2d.drawImage(bgForeground, 0, getHeight() - fgHeight, getWidth(), fgHeight, this);
        }

        if (gameLogic != null) {
            drawUI(g2d);
        }

        Toolkit.getDefaultToolkit().sync();
    }

    private void drawUI(Graphics2D g2d) {
        // ... (seluruh isi metode drawUI tetap sama seperti sebelumnya, tidak perlu diubah)
        if (gameLogic == null) return;

        g2d.setFont(FontManager.getPressStart2PRegular(12f));
        g2d.setColor(new Color(255, 230, 150));

        if (gameLogic.getCurrentState() != GameLogic.GameState.MENU && gameLogic.getCurrentState() != GameLogic.GameState.GAME_OVER) {
             if (gameLogic.getJar() != null) {
                String scoreText = "Skor: " + gameLogic.getJar().getTotalScore();
                String countText = "Ikan: " + gameLogic.getJar().getCollectedCount();
                g2d.drawString(scoreText, 25, 40);
                g2d.drawString(countText, 25, 70);
            }
            String timeText = "Waktu: " + gameLogic.getRemainingTime();
            FontMetrics fm = g2d.getFontMetrics();
            int timeTextWidth = fm.stringWidth(timeText);
            g2d.drawString(timeText, Constants.GAME_WIDTH - timeTextWidth - 25, 40);

            if (gameLogic.getPlayer() != null && heartFullImage != null && heartEmptyImage != null) {
                int currentHearts = gameLogic.getPlayer().getHearts();
                int maxHearts = Constants.PLAYER_INITIAL_HEARTS;
                int heartSize = 45;
                int padding = -3;
                int margin = 15;
                int y = 40;

                for (int i = 0; i < maxHearts; i++) {
                    int x = Constants.GAME_WIDTH - margin - (i + 1) * (heartSize + padding);
                    if (i < currentHearts) {
                        g2d.drawImage(heartFullImage, x, y, heartSize, heartSize, this);
                    } else {
                        g2d.drawImage(heartEmptyImage, x, y, heartSize, heartSize, this);
                    }
                }
            }
        }

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

            long startTime = gameLogic.getStruggleStartTimeMs();
            if (startTime > 0) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                float remainingSeconds = (Constants.STRUGGLE_TIME_LIMIT_MS - elapsedTime) / 1000.0f;
                remainingSeconds = Math.max(0.0f, remainingSeconds);

                String countdownText = String.format("%.1f", remainingSeconds);

                g2d.setFont(FontManager.getPressStart2PRegular(16f));
                g2d.setColor(Color.ORANGE);
                FontMetrics fmCountdown = g2d.getFontMetrics();
                int countdownWidth = fmCountdown.stringWidth(countdownText);
                g2d.drawString(countdownText, barX + (barWidth - countdownWidth) / 2, barY - 45);
            }
            
            g2d.setFont(FontManager.getPressStart2PRegular(10f));
            g2d.setColor(Color.WHITE);
            String strugglePrompt = "TEKAN Q & E BERGANTIAN!";
            FontMetrics fmStruggle = g2d.getFontMetrics();
            int promptWidth = fmStruggle.stringWidth(strugglePrompt);
            g2d.drawString(strugglePrompt, barX + (barWidth - promptWidth) / 2, barY - 20);
        }

        if (gameLogic.getCurrentState() == GameLogic.GameState.GAME_OVER) {
            g2d.setColor(new Color(0, 0, 0, 190));
            g2d.fillRect(0, 0, Constants.GAME_WIDTH, Constants.GAME_HEIGHT);

            g2d.setFont(FontManager.getPressStart2PRegular(40f));
            g2d.setColor(new Color(255, 50, 50));
            String gameOverMsg = "GAME OVER";
            FontMetrics fmGameOver = g2d.getFontMetrics();
            int msgWidth = fmGameOver.stringWidth(gameOverMsg);
            g2d.drawString(gameOverMsg, (Constants.GAME_WIDTH - msgWidth) / 2, Constants.GAME_HEIGHT / 2 - 100);

            g2d.setFont(FontManager.getPressStart2PRegular(14f));
            g2d.setColor(Color.WHITE);
            
            String reasonMsg = gameLogic.getGameOverMessage();
            FontMetrics fmReason = g2d.getFontMetrics();
            int reasonWidth = fmReason.stringWidth(reasonMsg);
            g2d.drawString(reasonMsg, (Constants.GAME_WIDTH - reasonWidth) / 2, Constants.GAME_HEIGHT / 2 - 50);

            String username = "Username: " + gameLogic.getUsername();
            String finalScore = "Skor Akhir: " + gameLogic.getJar().getTotalScore();
            String finalCount = "Ikan Terkumpul: " + gameLogic.getJar().getCollectedCount();

            FontMetrics fmDetails = g2d.getFontMetrics();
            g2d.drawString(username, (Constants.GAME_WIDTH - fmDetails.stringWidth(username)) / 2, Constants.GAME_HEIGHT / 2 + 20);
            g2d.drawString(finalScore, (Constants.GAME_WIDTH - fmDetails.stringWidth(finalScore)) / 2, Constants.GAME_HEIGHT / 2 + 50);
            g2d.drawString(finalCount, (Constants.GAME_WIDTH - fmDetails.stringWidth(finalCount)) / 2, Constants.GAME_HEIGHT / 2 + 80);
            
            g2d.setFont(FontManager.getPressStart2PRegular(12f));
            g2d.setColor(Color.WHITE);
            String skipMsg = "Tekan SPACE untuk Kembali ke Menu";
            int skipMsgWidth = g2d.getFontMetrics().stringWidth(skipMsg);
            g2d.drawString(skipMsg, (Constants.GAME_WIDTH - skipMsgWidth) / 2, Constants.GAME_HEIGHT - 50);
        }
    }
}