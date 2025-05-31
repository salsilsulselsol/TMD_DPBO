package view;

import model.TableHasil;
import viewmodel.InputHandler;
import viewmodel.SoundManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.sampled.Clip;
import java.net.URL; // Untuk background image

public class MenuScreen extends JFrame {
    private JTable scoreTable;
    private JTextField usernameField;
    private JButton playButton, quitButton;
    private JPanel mainPanel; // Panel utama yang akan jadi content pane
    private Clip menuMusicClip;
    private String selectedUsernameFromTable = "";

    public MenuScreen() {
        setTitle("Collect the Skill Balls - Menu"); // Sesuai PDF [cite: 31, 77]
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Coba load background image untuk mainPanel
        try {
            URL bgUrl = getClass().getResource("/assets/images/background_menu.png"); // PASTIKAN ASET ADA
            if (bgUrl != null) {
                JLabel backgroundLabel = new JLabel(new ImageIcon(bgUrl));
                backgroundLabel.setLayout(new BorderLayout()); // Agar bisa menampung komponen lain
                setContentPane(backgroundLabel); // Jadikan JLabel ini content pane
                
                // Ambil content pane yang baru (JLabel) dan set agar bisa menampung komponen UI
                Container layeredPane = getContentPane(); 
                layeredPane.setLayout(new BorderLayout()); // Atur layout untuk JLabel
                
                // Buat panel transparan untuk menampung UI agar background terlihat
                mainPanel = new JPanel(new BorderLayout(10, 10));
                mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
                mainPanel.setOpaque(false); // Penting agar background JLabel terlihat
                layeredPane.add(mainPanel, BorderLayout.CENTER);

            } else {
                System.err.println("Background menu tidak ditemukan, menggunakan warna solid.");
                mainPanel.setBackground(new Color(170, 210, 250)); // Warna fallback
                setContentPane(mainPanel);
            }
        } catch (Exception e) {
            System.err.println("Error memuat background menu: " + e.getMessage());
            mainPanel.setBackground(new Color(170, 210, 250));
            setContentPane(mainPanel);
        }

        // Title
        JLabel titleLabel = new JLabel("COLLECT THE SKILL BALLS", SwingConstants.CENTER); // Sesuai PDF [cite: 31, 77]
        titleLabel.setFont(new Font("Comic Sans MS", Font.BOLD, 38));
        titleLabel.setForeground(new Color(255, 215, 0)); // Warna Emas
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Center Panel: Username input dan Tabel Skor
        JPanel centerContentPanel = new JPanel(new BorderLayout(10, 15));
        centerContentPanel.setOpaque(false); // Transparan agar background utama terlihat
        
        JPanel usernameInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usernameInputPanel.setOpaque(false);
        JLabel usernameLabelText = new JLabel("Username:");
        usernameLabelText.setForeground(Color.WHITE); // Sesuaikan warna teks
        usernameLabelText.setFont(new Font("Arial", Font.BOLD, 14));
        usernameInputPanel.add(usernameLabelText);
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameInputPanel.add(usernameField);
        centerContentPanel.add(usernameInputPanel, BorderLayout.NORTH);

        // Tabel Skor
        try (TableHasil th = new TableHasil()) { // Try-with-resources untuk auto-close DB connection
            scoreTable = new JTable(th.getAllHasilForTable());
        } catch (Exception e) {
            e.printStackTrace();
            scoreTable = new JTable(new DefaultTableModel(new Object[]{"Username", "Skor", "Count"}, 0));
            JOptionPane.showMessageDialog(this, "Gagal memuat data skor dari database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        JScrollPane tableScrollPane = new JScrollPane(scoreTable); // Scroll jika data banyak [cite: 34, 80]
        // Atur tampilan tabel agar lebih menyatu dengan tema (opsional)
        tableScrollPane.getViewport().setOpaque(false);
        tableScrollPane.setOpaque(false);
        scoreTable.setOpaque(false);
        scoreTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        // ((javax.swing.table.DefaultTableCellRenderer)scoreTable.getDefaultRenderer(Object.class)).setOpaque(false); // Membuat sel transparan

        centerContentPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(centerContentPanel, BorderLayout.CENTER);

        // Bottom Panel: Tombol dan Kredit
        JPanel southAreaPanel = new JPanel(new BorderLayout(0, 5)); // Panel untuk tombol dan kredit
        southAreaPanel.setOpaque(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        buttonsPanel.setOpaque(false);
        playButton = new JButton("Play"); // Sesuai PDF [cite: 31, 77]
        quitButton = new JButton("Quit"); // Sesuai PDF [cite: 31, 77]
        Dimension buttonDim = new Dimension(130, 45);
        Font buttonFont = new Font("Arial", Font.BOLD, 16);
        playButton.setPreferredSize(buttonDim);
        playButton.setFont(buttonFont);
        quitButton.setPreferredSize(buttonDim);
        quitButton.setFont(buttonFont);
        buttonsPanel.add(playButton);
        buttonsPanel.add(quitButton);
        southAreaPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        JLabel creditTextLabel = new JLabel("Game by [Nama Anda] - Assets credited in-game/docs.", SwingConstants.CENTER); // Kredit aset [cite: 44, 90]
        creditTextLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        creditTextLabel.setForeground(Color.LIGHT_GRAY); // Warna kredit
        southAreaPanel.add(creditTextLabel, BorderLayout.SOUTH);
        
        mainPanel.add(southAreaPanel, BorderLayout.SOUTH);

        addListeners();

        menuMusicClip = SoundManager.playSound("assets/sounds/menu_music.wav", true); // Musik menu (bonus) [cite: 45, 91]

        pack();
        setLocationRelativeTo(null); // Tampilkan di tengah layar
    }

    private void addListeners() {
        playButton.addActionListener(e -> handlePlayButton());
        quitButton.addActionListener(e -> System.exit(0));
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
        
        // PDF: "Username disimpan jika tombol Play diklik"[cite: 33, 79]. 
        // Penyimpanan skor aktual terjadi saat game berakhir atau keluar.
        // Di sini, username hanya diteruskan ke game logic.
        
        if (menuMusicClip != null) SoundManager.stopSound(menuMusicClip);
        this.setVisible(false);
        this.dispose();

        GamePanel gamePanel = new GamePanel();
        InputHandler inputHandler = new InputHandler(gamePanel.getGameLogic());
        new GameWindow(gamePanel, inputHandler); // Buat window game
        gamePanel.getGameLogic().startGame(username); // Mulai game
    }
}