package view;

import javax.swing.JFrame;
import viewmodel.InputHandler;

public class GameWindow {
    private JFrame frame;

    public GameWindow(GamePanel gamePanel, InputHandler inputHandler) {
        frame = new JFrame("Collect the Skill Balls | Playing...");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        
        frame.add(gamePanel);
        
        gamePanel.addKeyListener(inputHandler.getKeyControls());
        gamePanel.addMouseListener(inputHandler); // InputHandler adalah MouseAdapter

        frame.pack(); 
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        gamePanel.requestFocusInWindow(); // Penting agar GamePanel dapat fokus input
    }

    // Tidak secara eksplisit dipanggil dalam alur saat ini, 
    // karena kembali ke menu akan membuat instance MenuScreen baru & dispose window lama.
    // public void closeWindow() { 
    //     frame.setVisible(false);
    //     frame.dispose();
    // }
}