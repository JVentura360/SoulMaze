package main;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.sound.sampled.*;

public class GameOverSequence {

    public static void show(JFrame frame, GameOverPanel.GameOverListener listener) {
        if (frame == null) {
            System.err.println("GameOverSequence: frame is null!");
            return;
        }

        // --- Prepare overlay ---
        JPanel glassPane = (JPanel) frame.getGlassPane();
        glassPane.removeAll();
        glassPane.setOpaque(true);
        glassPane.setBackground(Color.black);
        glassPane.setLayout(new BorderLayout());
        glassPane.setVisible(true);

        // --- Load and scale GIF to 1200x780 ---
        String gifPath = "src/assets/Images/gameOver.gif";
        File gifFile = new File(gifPath);
        if (!gifFile.exists()) {
            System.err.println("GameOverSequence: GIF not found at " + gifFile.getAbsolutePath());
            return;
        }

        ImageIcon originalIcon = new ImageIcon(gifPath);
        // Ensure animation plays smoothly by recreating ImageIcon as a new one
        Image scaledImage = originalIcon.getImage().getScaledInstance(1200, 780, Image.SCALE_DEFAULT);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        JLabel gifLabel = new JLabel(scaledIcon);
        gifLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gifLabel.setVerticalAlignment(SwingConstants.CENTER);
        glassPane.add(gifLabel, BorderLayout.CENTER);

        // Force repaint to show immediately
        glassPane.revalidate();
        glassPane.repaint();

        // --- Play SFX (non-blocking) ---
        playSound("src/assets/Music/gameOver.wav");

        // --- Keep GIF visible for full 9 seconds ---
        new javax.swing.Timer(9000, e -> {
            // After 9 seconds, remove GIF and show GameOverPanel
            ((javax.swing.Timer)e.getSource()).stop();
            glassPane.removeAll();
            glassPane.setLayout(new GridLayout(1, 1));

            GameOverPanel panel = new GameOverPanel(listener);
            glassPane.add(panel);
            glassPane.revalidate();
            glassPane.repaint();
        }).start();
    }

    // --- Simple SFX player ---
    private static void playSound(String path) {
        new Thread(() -> {
            try {
                File file = new File(path);
                if (!file.exists()) {
                    System.err.println("GameOverSequence: Sound file not found: " + path);
                    return;
                }

                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
