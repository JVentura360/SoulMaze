package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MainMenu extends JPanel {
    private JFrame parentFrame;
    private Image backgroundImage;
    private JButton playButton;
    private JButton rankButton;
    private JButton exitButton;
 // AudioManager instance
    private AudioManager audioManager;
    public MainMenu(JFrame parent) {
        this.parentFrame = parent;
        setLayout(null);
        setPreferredSize(new Dimension(1280, 800));
        setBackground(Color.BLACK);
     // Initialize AudioManager and play BGM
        audioManager = new AudioManager();
        audioManager.loadBackgroundMusic("src/assets/Music/MainMenu.wav"); // <-- your BGM file
        audioManager.playBackgroundMusic(true); // loop continuously
        loadImages();
        setupButtons();
    }
    
    private void loadImages() {
        try {
            // Load background image
            backgroundImage = new ImageIcon("src/assets/Images/mainMenuImage.png").getImage();
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
    }
    
    private void setupButtons() {
        // Load and scale Start/Play button
        ImageIcon originalStartIcon = new ImageIcon("src/assets/Images/StartButton.png");
        Image scaledStartImage = originalStartIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        ImageIcon scaledStartIcon = new ImageIcon(scaledStartImage);
        
        ImageIcon hoverStartIcon = new ImageIcon("src/assets/Images/StartButtonHover.png");
        System.out.println("Start hover icon loaded: " + (hoverStartIcon.getImage() != null));
        Image hoverStartScaled = hoverStartIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        ImageIcon scaledHoverStartIcon = new ImageIcon(hoverStartScaled);
        
        playButton = new JButton(scaledStartIcon);
        playButton.setBounds(410, 650, 125, 125);
        playButton.setRolloverIcon(scaledHoverStartIcon);
        playButton.setBorderPainted(false);
        playButton.setContentAreaFilled(false);
        playButton.setFocusPainted(false);
        playButton.setOpaque(false);
        playButton.addActionListener(e -> startGame());
        add(playButton);
        
        // Load and scale Rank button
        ImageIcon originalRankIcon = new ImageIcon("src/assets/Images/RankButton.png");
        Image scaledRankImage = originalRankIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        ImageIcon scaledRankIcon = new ImageIcon(scaledRankImage);
        
        ImageIcon hoverRankIcon = new ImageIcon("src/assets/Images/RankButtonHover.png");
        System.out.println("Rank hover icon loaded: " + (hoverRankIcon.getImage() != null));
        Image hoverRankScaled = hoverRankIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        ImageIcon scaledHoverRankIcon = new ImageIcon(hoverRankScaled);
        
        rankButton = new JButton(scaledRankIcon);
        rankButton.setBounds(580, 650, 125, 125);
        rankButton.setRolloverIcon(scaledHoverRankIcon);
        rankButton.setBorderPainted(false);
        rankButton.setContentAreaFilled(false);
        rankButton.setFocusPainted(false);
        rankButton.setOpaque(false);
        rankButton.addActionListener(e -> showRankings());
        add(rankButton);
        
        // Load and scale Exit button
        ImageIcon originalExitIcon = new ImageIcon("src/assets/Images/ExitButton.png");
        Image scaledExitImage = originalExitIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        ImageIcon scaledExitIcon = new ImageIcon(scaledExitImage);
        
        ImageIcon hoverExitIcon = new ImageIcon("src/assets/Images/ExitButtonHover.png");
        System.out.println("Exit hover icon loaded: " + (hoverExitIcon.getImage() != null));
        Image hoverExitScaled = hoverExitIcon.getImage().getScaledInstance(125, 125, Image.SCALE_SMOOTH);
        ImageIcon scaledHoverExitIcon = new ImageIcon(hoverExitScaled);
        
        exitButton = new JButton(scaledExitIcon);
        exitButton.setBounds(760, 650, 125, 125);
        exitButton.setRolloverIcon(scaledHoverExitIcon);
        exitButton.setBorderPainted(false);
        exitButton.setContentAreaFilled(false);
        exitButton.setFocusPainted(false);
        exitButton.setOpaque(false);
        exitButton.addActionListener(e -> exitGame());
        add(exitButton);
    }
    
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Draw background image
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback background
            g2d.setColor(new Color(20, 20, 40));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Draw title
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        FontMetrics fm = g2d.getFontMetrics();
        String title = "";
        int titleX = (getWidth() - fm.stringWidth(title)) / 2;
        int titleY = 150;
        g2d.drawString(title, titleX, titleY);
        
        // Buttons are now JButtons, drawn by the panel
    }
    

    
    private void startGame() {
    	audioManager.stopBackgroundMusic(); // Stop menu BGM before starting game
        // Show StartGamePanel transition
        showStartGameTransition();
    }
    
    private void showStartGameTransition() {
        // Switch to transition panel
        parentFrame.getContentPane().removeAll();
        TransitionPanel transitionPanel = new TransitionPanel(parentFrame, "src/assets/Images/StartGamePanel.png", new Runnable() {
            @Override
            public void run() {
                // Transition complete, start the game
                startActualGame();
            }
        });
        parentFrame.add(transitionPanel);
        parentFrame.pack();
        parentFrame.revalidate();
        parentFrame.repaint();
    }
    
    private void startActualGame() {
        // Switch to game panel
        parentFrame.getContentPane().removeAll();
        GamePanel gamePanel = new GamePanel();
        parentFrame.add(gamePanel);
        parentFrame.pack();
        parentFrame.revalidate();
        parentFrame.repaint();
        
        // Request focus for keyboard input
        gamePanel.requestFocusInWindow();
    }
    
    private void showRankings() {
        // TODO: Implement rankings/leaderboard functionality
        JOptionPane.showMessageDialog(this, "Rankings feature coming soon!", "Rankings", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void exitGame() {
    	audioManager.cleanup(); // release audio resources
        System.exit(0);
    }
}
