package view;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import javax.sound.sampled.Clip;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import model.TableHasil;
import viewmodel.FontManager;
import viewmodel.InputHandler;
import viewmodel.SoundManager;

public class MenuScreen extends JFrame {
    private JTable scoreTable;
    private JTextField usernameField;
    private JButton playButton, quitButton;
    private JPanel mainPanel; 
    private Clip menuMusicClip;
    private String selectedUsernameFromTable = "";

    public MenuScreen() {
        setTitle("Collect the Skill Balls - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        try {
            URL bgUrl = getClass().getResource("/assets/images/background_menu.png");
            if (bgUrl != null) {
                JLabel backgroundLabel = new JLabel(new ImageIcon(bgUrl));
                backgroundLabel.setLayout(new BorderLayout()); 
                setContentPane(backgroundLabel); 
                
                Container layeredPane = getContentPane(); 
                layeredPane.setLayout(new BorderLayout()); 
                
                mainPanel = new JPanel(new BorderLayout(10, 10));
                mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
                mainPanel.setOpaque(false); 
                layeredPane.add(mainPanel, BorderLayout.CENTER);

            } else {
                System.err.println("Background menu tidak ditemukan, menggunakan warna solid.");
                mainPanel.setBackground(new Color(170, 210, 250)); 
                setContentPane(mainPanel);
            }
        } catch (Exception e) {
            System.err.println("Error memuat background menu: " + e.getMessage());
            mainPanel.setBackground(new Color(170, 210, 250));
            setContentPane(mainPanel);
        }

        JLabel titleLabel = new JLabel("COLLECT THE SKILL BALLS", SwingConstants.CENTER);
        titleLabel.setFont(FontManager.getPressStart2PRegular(28f)); // Menggunakan Font Kustom
        titleLabel.setForeground(new Color(255, 215, 0)); 
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel centerContentPanel = new JPanel(new BorderLayout(10, 15));
        centerContentPanel.setOpaque(false); 
        
        JPanel usernameInputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        usernameInputPanel.setOpaque(false);
        JLabel usernameLabelText = new JLabel("Username:");
        usernameLabelText.setForeground(Color.WHITE); 
        usernameLabelText.setFont(FontManager.getPressStart2PRegular(10f)); // Menggunakan Font Kustom
        usernameInputPanel.add(usernameLabelText);
        usernameField = new JTextField(20);
        usernameField.setFont(FontManager.getPressStart2PRegular(10f)); // Menggunakan Font Kustom
        usernameInputPanel.add(usernameField);
        centerContentPanel.add(usernameInputPanel, BorderLayout.NORTH);

        try (TableHasil th = new TableHasil()) { //
            scoreTable = new JTable(th.getAllHasilForTable()); //
        } catch (Exception e) {
            e.printStackTrace();
            scoreTable = new JTable(new DefaultTableModel(new Object[]{"Username", "Skor", "Count"}, 0));
            JOptionPane.showMessageDialog(this, "Gagal memuat data skor dari database: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        JScrollPane tableScrollPane = new JScrollPane(scoreTable);
        tableScrollPane.getViewport().setOpaque(false);
        tableScrollPane.setOpaque(false);
        scoreTable.setOpaque(false);
        scoreTable.getTableHeader().setFont(FontManager.getPressStart2PRegular(9f)); // Menggunakan Font Kustom
        scoreTable.setFont(FontManager.getPressStart2PRegular(8f)); // Menggunakan Font Kustom

        centerContentPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(centerContentPanel, BorderLayout.CENTER);

        JPanel southAreaPanel = new JPanel(new BorderLayout(0, 5)); 
        southAreaPanel.setOpaque(false);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 10));
        buttonsPanel.setOpaque(false);
        playButton = new JButton("Play");
        quitButton = new JButton("Quit");
        Dimension buttonDim = new Dimension(130, 45);
        Font buttonFont = FontManager.getPressStart2PRegular(12f); // Menggunakan Font Kustom
        playButton.setPreferredSize(buttonDim);
        playButton.setFont(buttonFont);
        quitButton.setPreferredSize(buttonDim);
        quitButton.setFont(buttonFont);
        buttonsPanel.add(playButton);
        buttonsPanel.add(quitButton);
        southAreaPanel.add(buttonsPanel, BorderLayout.CENTER);
        
        // Anda bisa mengganti "[Nama Anda]" dengan nama Anda
        JLabel creditTextLabel = new JLabel("Game by Faisal N.Q. - Assets credited in-game/docs.", SwingConstants.CENTER);
        creditTextLabel.setFont(FontManager.getPressStart2PRegular(7f).deriveFont(Font.ITALIC)); // Menggunakan Font Kustom
        creditTextLabel.setForeground(Color.LIGHT_GRAY); 
        southAreaPanel.add(creditTextLabel, BorderLayout.SOUTH);
        
        mainPanel.add(southAreaPanel, BorderLayout.SOUTH);

        addListeners();

        menuMusicClip = SoundManager.playSound("assets/sounds/menu_music.wav", true); //

        pack();
        setLocationRelativeTo(null); 
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
        
        if (menuMusicClip != null) SoundManager.stopSound(menuMusicClip); //
        this.setVisible(false);
        this.dispose();

        GamePanel gamePanel = new GamePanel();
        InputHandler inputHandler = new InputHandler(gamePanel.getGameLogic()); //
        new GameWindow(gamePanel, inputHandler); //
        gamePanel.getGameLogic().startGame(username); //
    }
}