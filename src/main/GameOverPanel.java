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

    // keep references so we can position them dynamically
    private final JLabel title;
    private final JButton retryButton;
    private final JButton quitButton;

    public GameOverPanel(GameOverListener listener) {
        this.listener = listener;
        setLayout(null);
        setOpaque(false); // We are drawing a background image, so the panel itself should be transparent

        // Load the background image
        backgroundImage = new ImageIcon("src/assets/Images/GameOverPanel.png").getImage();

        title = new JLabel("GAME OVER", SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 80));
        title.setForeground(Color.RED);
        add(title);

        retryButton = new JButton("RETRY");
        retryButton.addActionListener(e -> {
            // Get the top-level frame
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                // Get the glass pane, hide it, and remove our overlay
                JPanel glassPane = (JPanel) frame.getGlassPane();
                glassPane.setVisible(false);
                glassPane.removeAll();
            }
            if (listener != null) listener.onRetry();
        });
        add(retryButton);

        quitButton = new JButton("QUIT");
        quitButton.addActionListener(e -> {
            // Get the top-level frame
            JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                // Get the glass pane, hide it, and remove our overlay
                JPanel glassPane = (JPanel) frame.getGlassPane();
                glassPane.setVisible(false);
                glassPane.removeAll();
            }
            if (listener != null) listener.onQuit();
        });
        add(quitButton);
    }

    // layout components relative to current size so they remain visible on all resolutions
    @Override
    public void doLayout() {
        int w = getWidth();
        int h = getHeight();
        int titleW = Math.min(800, (int)(w * 0.7));
        int titleH = Math.min(120, (int)(h * 0.15));
        title.setBounds((w - titleW) / 2, Math.max(20, h / 6 - titleH / 2), titleW, titleH);

        int btnW = Math.min(160, (int)(w * 0.12));
        int btnH = Math.min(60, (int)(h * 0.08));
        int gap = 20;
        int totalW = btnW * 2 + gap;
        int startX = (w - totalW) / 2;
        int y = Math.min(h - btnH - 40, h / 2);

        retryButton.setBounds(startX, y, btnW, btnH);
        quitButton.setBounds(startX + btnW + gap, y, btnW, btnH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw a semi-transparent background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the background image on top
        if (backgroundImage != null) {
            // Center the image
            int imgWidth = backgroundImage.getWidth(this);
            int imgHeight = backgroundImage.getHeight(this);
            int x = (getWidth() - imgWidth) / 2;
            int y = (getHeight() - imgHeight) / 2;
            g.drawImage(backgroundImage, x, y, this);
        }
    }
}
