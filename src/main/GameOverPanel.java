package main;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {
    public interface GameOverListener {
        void onRetry();
        void onQuit();
    }

    private final GameOverListener listener;
    private final Image backgroundImage;
    private final JButton retryButton;
    private final JButton quitButton;

    public GameOverPanel(GameOverListener listener) {
        this.listener = listener;
        setLayout(null);
        setOpaque(false); // Transparent overlay

        // Load the background image
        backgroundImage = new ImageIcon("src/assets/Images/GameOverPanel.png").getImage();

        // -----------------------------
        // RETRY button (image version)
        // -----------------------------
        ImageIcon retryIcon = new ImageIcon("src/assets/Images/OverRetryButton.png");
        Image scaledRetry = retryIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledRetryIcon = new ImageIcon(scaledRetry);

        ImageIcon retryHoverIcon = new ImageIcon("src/assets/Images/OverRetryButtonHover.png");
        Image scaledRetryHover = retryHoverIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledRetryHoverIcon = new ImageIcon(scaledRetryHover);

        retryButton = new JButton(scaledRetryIcon);
        retryButton.setRolloverIcon(scaledRetryHoverIcon);
        retryButton.setBorderPainted(false);
        retryButton.setContentAreaFilled(false);
        retryButton.setFocusPainted(false);
        retryButton.setOpaque(false);
        retryButton.setBounds(380, 540, 150, 150); // Adjust position as needed
        retryButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                JPanel glassPane = (JPanel) frame.getGlassPane();
                glassPane.setVisible(false);
                glassPane.removeAll();
            }
            if (listener != null) listener.onRetry();
        });
        add(retryButton);

        // -----------------------------
        // QUIT button (image version)
        // -----------------------------
        ImageIcon quitIcon = new ImageIcon("src/assets/Images/OverQuitButton.png");
        Image scaledQuit = quitIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledQuitIcon = new ImageIcon(scaledQuit);

        ImageIcon quitHoverIcon = new ImageIcon("src/assets/Images/OverQuitButtonHover.png");
        Image scaledQuitHover = quitHoverIcon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        ImageIcon scaledQuitHoverIcon = new ImageIcon(scaledQuitHover);

        quitButton = new JButton(scaledQuitIcon);
        quitButton.setRolloverIcon(scaledQuitHoverIcon);
        quitButton.setBorderPainted(false);
        quitButton.setContentAreaFilled(false);
        quitButton.setFocusPainted(false);
        quitButton.setOpaque(false);
        quitButton.setBounds(670, 540, 150, 150); // Adjust position as needed
        quitButton.addActionListener(e -> {
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                JPanel glassPane = (JPanel) frame.getGlassPane();
                glassPane.setVisible(false);
                glassPane.removeAll();
            }
            if (listener != null) listener.onQuit();
        });
        add(quitButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dark semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw centered background image
        if (backgroundImage != null) {
            int imgWidth = backgroundImage.getWidth(this);
            int imgHeight = backgroundImage.getHeight(this);
            int x = (getWidth() - imgWidth) / 2;
            int y = (getHeight() - imgHeight) / 2;
            g.drawImage(backgroundImage, x, y, this);
        }
    }
}
