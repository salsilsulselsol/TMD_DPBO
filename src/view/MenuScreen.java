package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.BigFish;
import model.DartFish;
import model.Fish;
import model.TableHasil;
import viewmodel.Constants;
import viewmodel.FontManager;
import viewmodel.GameLogic;
import viewmodel.InputHandler;
import viewmodel.SoundManager;

public class MenuScreen extends JFrame {
    
    private JTable scoreTable;
    private JTextField usernameField;
    private JButton playButton, quitButton;
    private Clip menuMusicClip;
    private String selectedUsernameFromTable = "";
    private Timer animationTimer;

    class BackgroundPanel extends JPanel {
        private Image bgImageFar, bgImageSand;
        public BackgroundPanel() {
            try {
                URL farUrl = getClass().getResource("/assets/images/far.png");
                if (farUrl != null) bgImageFar = new ImageIcon(farUrl).getImage();
                URL sandUrl = getClass().getResource("/assets/images/sand.png");
                if (sandUrl != null) bgImageSand = new ImageIcon(sandUrl).getImage();
            } catch (Exception e) { e.printStackTrace(); }
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImageFar != null) g.drawImage(bgImageFar, 0, 0, getWidth(), getHeight(), this);
            if (bgImageSand != null) g.drawImage(bgImageSand, 0, 0, getWidth(), getHeight(), this);
        }
    }

    public MenuScreen() {
        setTitle("Monster Fish Hunt - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout(40, 0));
        backgroundPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(backgroundPanel);

        JPanel leftPanel = createLeftPanel();
        backgroundPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = createRightPanel();
        backgroundPanel.add(rightPanel, BorderLayout.CENTER);

        addListeners();
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        pack();
        setLocationRelativeTo(null);
        SoundManager.playBGM("/assets/sounds/menu-bgm.wav");
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(320, 0));

        JLabel titleLabel = new JLabel("<html>MONSTER<br>FISH HUNT</html>");
        titleLabel.setFont(FontManager.getPressStart2PRegular(32f));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        try (TableHasil th = new TableHasil()) {
            scoreTable = new JTable(th.getAllHasilForTable());
        } catch (Exception e) {
            scoreTable = new JTable(new DefaultTableModel(new Object[]{"Username", "Skor", "Count"}, 0));
        }
        
        scoreTable.setOpaque(false);
        scoreTable.setBackground(new Color(15, 20, 40, 150));
        scoreTable.setForeground(Color.WHITE);
        scoreTable.getTableHeader().setFont(FontManager.getPressStart2PRegular(9f));
        scoreTable.setFont(FontManager.getPressStart2PRegular(8f));
        scoreTable.setGridColor(new Color(255, 255, 255, 50));
        
        JScrollPane tableScrollPane = new JScrollPane(scoreTable);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100)));
        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel playerPanel = createAnimatedPlayerPanel();
        JPanel usernameInputPanel = new JPanel();
        usernameInputPanel.setOpaque(false);
        JLabel usernameLabelText = new JLabel("Username: ");
        usernameLabelText.setForeground(Color.WHITE);
        usernameLabelText.setFont(FontManager.getPressStart2PRegular(12f));
        usernameField = new JTextField(15);
        usernameField.setFont(FontManager.getPressStart2PRegular(12f));
        usernameInputPanel.add(usernameLabelText);
        usernameInputPanel.add(usernameField);
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        playButton = new JButton("Play");
        quitButton = new JButton("Quit");
        Dimension buttonDim = new Dimension(120, 40);
        Font buttonFont = FontManager.getPressStart2PRegular(12f);
        playButton.setPreferredSize(buttonDim);
        playButton.setFont(buttonFont);
        quitButton.setPreferredSize(buttonDim);
        quitButton.setFont(buttonFont);
        buttonsPanel.add(playButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        buttonsPanel.add(quitButton);
        
        JPanel infoPanel = createInfoPanel();

        panel.add(Box.createVerticalGlue());
        panel.add(playerPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(usernameInputPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(buttonsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(infoPanel);
        panel.add(Box.createVerticalGlue());
        
        JLabel creditTextLabel = new JLabel("Game by Faisal N.Q.", SwingConstants.CENTER);
        creditTextLabel.setFont(FontManager.getPressStart2PRegular(7f).deriveFont(Font.ITALIC));
        creditTextLabel.setForeground(Color.LIGHT_GRAY);
        creditTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(creditTextLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        return panel;
    }

    private JPanel createAnimatedPlayerPanel() {
        JPanel playerPanel = new JPanel() {
            private BufferedImage playerIdleSheet;
            private int currentPlayerFrame = 0;
            private final int frameWidth = 80, frameHeight = 80, frameCount = 6;
            
            {
                setOpaque(false);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                Dimension panelSize = new Dimension(120, 120); 
                setPreferredSize(panelSize);
                setMaximumSize(panelSize);
                try {
                    URL url = getClass().getResource("/assets/images/player-idle.png");
                    if (url != null) playerIdleSheet = ImageIO.read(url);
                } catch (IOException e) { e.printStackTrace(); }
                
                animationTimer = new Timer(120, e -> {
                    currentPlayerFrame = (currentPlayerFrame + 1) % frameCount;
                    repaint();
                });
                animationTimer.start();
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (playerIdleSheet != null) {
                    int frameX = currentPlayerFrame * frameWidth;
                    g.drawImage(playerIdleSheet, 0, 0, 120, 120, 
                                frameX, 0, frameX + frameWidth, frameHeight, this);
                }
            }
        };
        return playerPanel;
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        Border titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "Info", 0, 0, FontManager.getPressStart2PRegular(10f), Color.WHITE);
        Border paddedBorder = BorderFactory.createCompoundBorder(
            titledBorder, new EmptyBorder(5, 5, 5, 5));
        infoPanel.setBorder(paddedBorder);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(createInfoRow("/assets/images/fish.png", 32, 32, "+ " + new Fish(0,0,"",false).getScoreValue() + " Poin"));
        infoPanel.add(createInfoRow("/assets/images/fish-dart.png", 39, 20, "+ " + new DartFish(0,0,"",false).getScoreValue() + " Poin"));
        infoPanel.add(createInfoRow("/assets/images/fish-big.png", 54, 49, "+ " + new BigFish(0,0,"",false).getScoreValue() + " Poin"));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(createInfoRow("/assets/images/ghost.png", 31, 44, "-1 Hati"));
        infoPanel.add(createInfoRow("/assets/images/hit.png", 31, 32, "-1 Hati (Gagal)"));
        
        return infoPanel;
    }

    // --- METODE INI DIMODIFIKASI TOTAL UNTUK PERATAAN (ALIGNMENT) ---
    private JPanel createInfoRow(String imagePath, int frameW, int frameH, String text) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        // Panel khusus untuk menampung dan menengahkan ikon
        JPanel iconContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(48, 40)); // Ukuran wadah tetap untuk semua ikon
        
        try {
            URL url = getClass().getResource(imagePath);
            if (url != null) {
                BufferedImage sheet = ImageIO.read(url);
                BufferedImage icon = sheet.getSubimage(0, 0, frameW, frameH);
                
                // Skala gambar dengan mempertahankan rasio aspek, tinggi maksimal 32px
                int newHeight = 32;
                int newWidth = (int) (frameW * ((double) newHeight / frameH));
                
                JLabel imageLabel = new JLabel(new ImageIcon(icon.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)));
                iconContainer.add(imageLabel);
            }
        } catch (IOException e) { e.printStackTrace(); }
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(FontManager.getPressStart2PRegular(10f));
        textLabel.setForeground(Color.WHITE);

        rowPanel.add(iconContainer, BorderLayout.WEST);
        rowPanel.add(textLabel, BorderLayout.CENTER);

        return rowPanel;
    }

    private void addListeners() {
        playButton.addActionListener(e -> handlePlayButton());
        quitButton.addActionListener(e -> {
            animationTimer.stop();
            if (menuMusicClip != null) SoundManager.stopBGM();
            System.exit(0);
        });
        
        scoreTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = scoreTable.getSelectedRow();
                if (selectedRow != -1) {
                    selectedUsernameFromTable = scoreTable.getValueAt(selectedRow, 0).toString();
                    usernameField.setText(selectedUsernameFromTable);
                }
            }
        });
    }

    private void handlePlayButton() {
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            if (!selectedUsernameFromTable.isEmpty()) {
                username = selectedUsernameFromTable;
            } else {
                JOptionPane.showMessageDialog(this, "Username tidak boleh kosong!", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        animationTimer.stop();
        if (menuMusicClip != null) SoundManager.stopBGM();
        this.dispose();

        // --- LOGIKA YANG DIPERBAIKI ---
        // 1. Buat GamePanel. GamePanel akan otomatis membuat GameLogic-nya sendiri.
        GamePanel gamePanel = new GamePanel();
        
        // 2. Ambil GameLogic yang sudah dibuat oleh GamePanel.
        GameLogic gameLogic = gamePanel.getGameLogic();
        
        // 3. Buat InputHandler menggunakan GameLogic yang benar.
        InputHandler inputHandler = new InputHandler(gameLogic);
        
        // 4. Buat GameWindow dan mulai permainan.
        new GameWindow(gamePanel, inputHandler);
        gameLogic.startGame(username);
    }

}