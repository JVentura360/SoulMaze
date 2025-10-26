package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

public class RankingPanel extends JDialog {
    private Font customFont;
    private Image backgroundImage;
    private List<PlayerScore> scores;
    
    public RankingPanel(JFrame parent) {
        super(parent, "", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true); // Remove title bar
        setBackground(new Color(0, 0, 0, 0)); // Transparent background
        
        // Load custom font
        loadCustomFont();
        
        // Load background image
        loadBackgroundImage();
        
        // Load scores
        loadScores();
        
        // Setup dialog
        setupDialog();
        
        // Center on parent
        setLocationRelativeTo(parent);
    }
    
    private void loadCustomFont() {
        try {
            // Load the custom font
            InputStream fontStream = getClass().getResourceAsStream("/assets/Font/belisa_plumilla.ttf");
            if (fontStream != null) {
                customFont = Font.createFont(Font.TRUETYPE_FONT, fontStream);
                fontStream.close();
                System.out.println("Custom font loaded successfully for ranking");
            } else {
                System.out.println("Font file not found, using default font");
                customFont = new Font("Arial", Font.BOLD, 16);
            }
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            customFont = new Font("Arial", Font.BOLD, 16);
        }
    }
    
    private void loadBackgroundImage() {
        try {
            backgroundImage = new ImageIcon("src/assets/Images/RankPanel.png").getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }
    
    private void loadScores() {
        scores = new ArrayList<>();
        // For now, add some sample scores. In a real implementation, you'd load from a file
        scores.add(new PlayerScore("Player1", 1500));
        scores.add(new PlayerScore("Player2", 1200));
        scores.add(new PlayerScore("Player3", 1000));
        scores.add(new PlayerScore("Player4", 800));
        scores.add(new PlayerScore("Player5", 600));
        
        // Sort by score (highest first)
        scores.sort((a, b) -> Integer.compare(b.score, a.score));
    }
    
    private void setupDialog() {
        setSize(600, 500);
        setLayout(null);
        getRootPane().setBorder(null); // Remove any default borders
        getRootPane().setOpaque(false); // Make root pane transparent
        
        // Create main panel with background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(50, 50, 80));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setBounds(0, 0, 600, 500);
        mainPanel.setBorder(null); // Remove panel borders
        mainPanel.setOpaque(false); // Make panel transparent
        add(mainPanel);
        
        // Create title label
        JLabel titleLabel = new JLabel("");
        titleLabel.setFont(customFont.deriveFont(28f));
        titleLabel.setForeground(new Color(255, 215, 0)); // Gold color for title
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(150, 50, 300, 40);
        mainPanel.add(titleLabel);
        
        // Display top 3 scores
        for (int i = 0; i < Math.min(3, scores.size()); i++) {
            PlayerScore playerScore = scores.get(i);
            
            // Rank number
            JLabel rankLabel = new JLabel("#" + (i + 1));
            rankLabel.setFont(customFont.deriveFont(20f));
            rankLabel.setForeground(new Color(255, 165, 0)); // Orange color
            rankLabel.setHorizontalAlignment(SwingConstants.CENTER);
            rankLabel.setBounds(100, 185 + i * 80, 50, 30);
            mainPanel.add(rankLabel);
            
            // Player name
            JLabel nameLabel = new JLabel(playerScore.name);
            nameLabel.setFont(customFont.deriveFont(24f)); // Increased from 18f to 24f
            nameLabel.setForeground(Color.WHITE);
            nameLabel.setHorizontalAlignment(SwingConstants.LEFT);
            nameLabel.setBounds(180, 185 + i * 80, 200, 40); // Increased height from 30 to 40
            mainPanel.add(nameLabel);
            
            // Player score
            JLabel scoreLabel = new JLabel(String.valueOf(playerScore.score));
            scoreLabel.setFont(customFont.deriveFont(24f)); // Increased from 18f to 24f
            scoreLabel.setForeground(new Color(144, 238, 144)); // Light green color
            scoreLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            scoreLabel.setBounds(320, 185 + i * 80, 100, 40); // Increased height from 30 to 40
            mainPanel.add(scoreLabel);
        }
        
        // Create Close button
        JButton closeButton = createCloseButton("CLOSE", 250, 400, 100, 100);
        closeButton.addActionListener(e -> {
            dispose();
        });
        mainPanel.add(closeButton);
    }
    
    private JButton createCloseButton(String text, int x, int y, int width, int height) {
        // Load and scale button images
        ImageIcon originalIcon = new ImageIcon("src/assets/Images/CloseButton.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        
        ImageIcon hoverIcon = new ImageIcon("src/assets/Images/CloseButtonHover.png");
        System.out.println("CLOSE hover icon loaded: " + (hoverIcon.getImage() != null));
        Image hoverScaled = hoverIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon scaledHoverIcon = new ImageIcon(hoverScaled);
        
        JButton button = new JButton(scaledIcon);
        button.setBounds(x, y, width, height);
        button.setRolloverIcon(scaledHoverIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setOpaque(false);
        
        return button;
    }
    
    // Inner class to represent a player score
    private static class PlayerScore {
        String name;
        int score;
        
        public PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
    }
}
