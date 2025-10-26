package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GameOverPanel extends JDialog {
    public interface GameOverListener {
        void onRetry();
        void onQuit();
    }

    public GameOverPanel(JFrame parent, GameOverListener callback) {
        super(parent, "", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setSize(1024, 805);
        setLocationRelativeTo(parent);
        setBackground(new Color(0, 0, 0, 0));
        setLayout(null);

        // Background
        JPanel bgPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Image img = new ImageIcon("src/assets/Images/GameOverPanel.png").getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            }
        };
        bgPanel.setLayout(null);
        bgPanel.setBounds(0, 0, 1024, 805);
        bgPanel.setOpaque(false);
        add(bgPanel);

        // Retry button
        ImageIcon retryIcon = new ImageIcon("src/assets/Images/RetryButton.png");
        ImageIcon retryHoverIcon = new ImageIcon("src/assets/Images/ReturnButtonHover.png");
        JButton retryButton = new JButton(new ImageIcon(retryIcon.getImage().getScaledInstance(150, 80, Image.SCALE_SMOOTH)));
        retryButton.setBounds(90, 340, 150, 80);
        retryButton.setRolloverIcon(new ImageIcon(retryHoverIcon.getImage().getScaledInstance(150, 80, Image.SCALE_SMOOTH)));
        retryButton.setBorderPainted(false);
        retryButton.setContentAreaFilled(false);
        retryButton.setFocusPainted(false);
        retryButton.setOpaque(false);
        retryButton.addActionListener(e -> {
            dispose();
            callback.onRetry();
        });
        bgPanel.add(retryButton);

        // Quit button
        ImageIcon quitIcon = new ImageIcon("src/assets/Images/QuitButton.png");
        ImageIcon quitHoverIcon = new ImageIcon("src/assets/Images/QuitButtonHover.png");
        JButton quitButton = new JButton(new ImageIcon(quitIcon.getImage().getScaledInstance(150, 80, Image.SCALE_SMOOTH)));
        quitButton.setBounds(360, 340, 150, 80);
        quitButton.setRolloverIcon(new ImageIcon(quitHoverIcon.getImage().getScaledInstance(150, 80, Image.SCALE_SMOOTH)));
        quitButton.setBorderPainted(false);
        quitButton.setContentAreaFilled(false);
        quitButton.setFocusPainted(false);
        quitButton.setOpaque(false);
        quitButton.addActionListener(e -> {
            dispose();
            callback.onQuit();
        });
        bgPanel.add(quitButton);
    }
}
