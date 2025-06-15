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

// Kelas ini merepresentasikan jendela menu utama permainan.
public class MenuScreen extends JFrame {
    
    // Deklarasi komponen-komponen UI yang akan digunakan.
    private JTable scoreTable;
    private JTextField usernameField;
    private JButton playButton, quitButton;
    private Clip menuMusicClip;
    private String selectedUsernameFromTable = ""; // Menyimpan username saat baris tabel diklik.
    private Timer animationTimer; // Timer untuk animasi karakter di menu.

    // Kelas internal untuk membuat panel dengan gambar latar belakang.
    class BackgroundPanel extends JPanel {
        private Image bgImageFar, bgImageSand;
        public BackgroundPanel() {
            // Memuat gambar latar belakang dari assets.
            try {
                URL farUrl = getClass().getResource("/assets/images/far.png");
                if (farUrl != null) bgImageFar = new ImageIcon(farUrl).getImage();
                URL sandUrl = getClass().getResource("/assets/images/sand.png");
                if (sandUrl != null) bgImageSand = new ImageIcon(sandUrl).getImage();
            } catch (Exception e) { e.printStackTrace(); }
        }
        // Override metode paintComponent untuk menggambar background.
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bgImageFar != null) g.drawImage(bgImageFar, 0, 0, getWidth(), getHeight(), this);
            if (bgImageSand != null) g.drawImage(bgImageSand, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Konstruktor utama untuk membangun seluruh tampilan menu.
    public MenuScreen() {
        setTitle("Monster Fish Hunt - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Mengatur panel utama dengan background kustom.
        BackgroundPanel backgroundPanel = new BackgroundPanel();
        backgroundPanel.setLayout(new BorderLayout(40, 0));
        backgroundPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        setContentPane(backgroundPanel);

        // Membuat dan menambahkan panel kiri (tabel skor) dan kanan (kontrol game).
        JPanel leftPanel = createLeftPanel();
        backgroundPanel.add(leftPanel, BorderLayout.WEST);

        JPanel rightPanel = createRightPanel();
        backgroundPanel.add(rightPanel, BorderLayout.CENTER);

        // Menambahkan listener untuk semua komponen interaktif.
        addListeners();
        // Mengatur ukuran window dan menampilkannya di tengah layar.
        setPreferredSize(new Dimension(Constants.GAME_WIDTH, Constants.GAME_HEIGHT));
        pack();
        setLocationRelativeTo(null);
        // Memutar musik latar menu.
        SoundManager.playBGM("/assets/sounds/menu-bgm.wav");
    }

    // Metode untuk membuat dan mengatur panel kiri yang berisi judul dan tabel skor.
    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setOpaque(false); // Dibuat transparan agar background terlihat.
        panel.setPreferredSize(new Dimension(320, 0));

        // Membuat label judul game.
        JLabel titleLabel = new JLabel("<html>MONSTER<br>FISH HUNT</html>");
        titleLabel.setFont(FontManager.getPressStart2PRegular(32f));
        titleLabel.setForeground(new Color(255, 215, 0));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(titleLabel, BorderLayout.NORTH);

        // Mengambil data skor dari database dan menampilkannya di JTable.
        try (TableHasil th = new TableHasil()) {
            scoreTable = new JTable(th.getAllHasilForTable());
        } catch (Exception e) {
            // Fallback jika koneksi database gagal.
            scoreTable = new JTable(new DefaultTableModel(new Object[]{"Username", "Skor", "Count"}, 0));
        }
        
        // Mengatur gaya visual tabel skor.
        scoreTable.setOpaque(false);
        scoreTable.setBackground(new Color(15, 20, 40, 150));
        scoreTable.setForeground(Color.WHITE);
        scoreTable.getTableHeader().setFont(FontManager.getPressStart2PRegular(9f));
        scoreTable.setFont(FontManager.getPressStart2PRegular(8f));
        scoreTable.setGridColor(new Color(255, 255, 255, 50));
        
        // Membungkus tabel dengan JScrollPane agar bisa di-scroll jika data banyak.
        JScrollPane tableScrollPane = new JScrollPane(scoreTable);
        tableScrollPane.setOpaque(false);
        tableScrollPane.getViewport().setOpaque(false);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 100)));
        panel.add(tableScrollPane, BorderLayout.CENTER);

        return panel;
    }

    // Metode untuk membuat dan mengatur panel kanan yang berisi input dan tombol.
    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Membuat semua sub-panel: animasi player, input username, tombol, dan info.
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

        // Menata semua sub-panel di panel kanan dengan spasi.
        panel.add(Box.createVerticalGlue());
        panel.add(playerPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(usernameInputPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(buttonsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(infoPanel);
        panel.add(Box.createVerticalGlue());
        
        // Menambahkan teks kredit di bagian bawah.
        JLabel creditTextLabel = new JLabel("Game by Faisal N.Q.", SwingConstants.CENTER);
        creditTextLabel.setFont(FontManager.getPressStart2PRegular(7f).deriveFont(Font.ITALIC));
        creditTextLabel.setForeground(Color.LIGHT_GRAY);
        creditTextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(creditTextLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        return panel;
    }

    // Metode untuk membuat panel yang menampilkan animasi player idle.
    private JPanel createAnimatedPlayerPanel() {
        // Menggunakan anonymous inner class untuk membuat panel dengan logika kustom.
        JPanel playerPanel = new JPanel() {
            private BufferedImage playerIdleSheet;
            private int currentPlayerFrame = 0;
            private final int frameWidth = 80, frameHeight = 80, frameCount = 6;
            
            // Blok inisialisasi untuk panel ini.
            {
                setOpaque(false);
                setAlignmentX(Component.CENTER_ALIGNMENT);
                Dimension panelSize = new Dimension(120, 120); 
                setPreferredSize(panelSize);
                setMaximumSize(panelSize);
                // Memuat sprite sheet animasi player.
                try {
                    URL url = getClass().getResource("/assets/images/player-idle.png");
                    if (url != null) playerIdleSheet = ImageIO.read(url);
                } catch (IOException e) { e.printStackTrace(); }
                
                // Membuat dan memulai timer untuk menggerakkan frame animasi.
                animationTimer = new Timer(120, e -> {
                    currentPlayerFrame = (currentPlayerFrame + 1) % frameCount; // Pindah ke frame berikutnya.
                    repaint(); // Gambar ulang panel untuk menampilkan frame baru.
                });
                animationTimer.start();
            }

            // Menggambar frame animasi saat ini.
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
    
    // Metode untuk membuat panel info di sisi kanan.
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        
        // Mengatur border dengan judul "Info".
        Border titledBorder = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), "Info", 0, 0, FontManager.getPressStart2PRegular(10f), Color.WHITE);
        Border paddedBorder = BorderFactory.createCompoundBorder(
            titledBorder, new EmptyBorder(5, 5, 5, 5));
        infoPanel.setBorder(paddedBorder);
        infoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Menambahkan setiap baris info (ikon dan teks) ke panel.
        infoPanel.add(createInfoRow("/assets/images/fish.png", 32, 32, "+ " + new Fish(0,0,"",false).getScoreValue() + " Poin"));
        infoPanel.add(createInfoRow("/assets/images/fish-dart.png", 39, 20, "+ " + new DartFish(0,0,"",false).getScoreValue() + " Poin"));
        infoPanel.add(createInfoRow("/assets/images/fish-big.png", 54, 49, "+ " + new BigFish(0,0,"",false).getScoreValue() + " Poin"));
        infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        infoPanel.add(createInfoRow("/assets/images/ghost.png", 31, 44, "-1 Hati"));
        infoPanel.add(createInfoRow("/assets/images/hit.png", 31, 32, "-1 Hati (Gagal)"));
        
        return infoPanel;
    }

    // Metode pembantu untuk membuat satu baris di dalam panel info.
    private JPanel createInfoRow(String imagePath, int frameW, int frameH, String text) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));

        // Wadah untuk ikon agar posisinya rapi dan terpusat.
        JPanel iconContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        iconContainer.setOpaque(false);
        iconContainer.setPreferredSize(new Dimension(48, 40));
        
        try {
            // Memuat, memotong, dan menskalakan ikon dari sprite sheet.
            URL url = getClass().getResource(imagePath);
            if (url != null) {
                BufferedImage sheet = ImageIO.read(url);
                BufferedImage icon = sheet.getSubimage(0, 0, frameW, frameH);
                
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

    // Metode untuk mendaftarkan semua listener ke komponen UI.
    private void addListeners() {
        // Listener untuk tombol Play, memanggil handlePlayButton.
        playButton.addActionListener(e -> handlePlayButton());
        // Listener untuk tombol Quit, menghentikan aplikasi.
        quitButton.addActionListener(e -> {
            animationTimer.stop();
            if (menuMusicClip != null) SoundManager.stopBGM();
            System.exit(0);
        });
        
        // Listener untuk tabel skor, memungkinkan user memilih username dengan klik.
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

    // Logika yang dijalankan saat tombol "Play" ditekan.
    private void handlePlayButton() {
        String username = usernameField.getText().trim();
        // Validasi input username.
        if (username.isEmpty()) {
            if (!selectedUsernameFromTable.isEmpty()) {
                username = selectedUsernameFromTable;
            } else {
                JOptionPane.showMessageDialog(this, "Username tidak boleh kosong!", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
        
        // Hentikan semua proses di menu.
        animationTimer.stop();
        if (menuMusicClip != null) SoundManager.stopBGM();
        this.dispose(); // Tutup window menu.

        // Alur untuk memulai permainan.
        // 1. Buat GamePanel, yang akan otomatis membuat GameLogic-nya sendiri.
        GamePanel gamePanel = new GamePanel();
        
        // 2. Ambil instance GameLogic dari GamePanel.
        GameLogic gameLogic = gamePanel.getGameLogic();
        
        // 3. Buat InputHandler menggunakan GameLogic yang benar.
        InputHandler inputHandler = new InputHandler(gameLogic);
        
        // 4. Buat GameWindow baru dan mulai permainannya.
        new GameWindow(gamePanel, inputHandler);
        gameLogic.startGame(username);
    }

}