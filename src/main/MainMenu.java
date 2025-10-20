package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.sound.sampled.*;

public class MainMenu extends JPanel implements MouseListener, MouseMotionListener {
    private JFrame parentFrame;
    private Image backgroundImage;
    private Image startButtonImage;
    private Image exitButtonImage;
    private Image startButtonHoverImage;
    private Image exitButtonHoverImage;
    
    private boolean startHovered = false;
    private boolean exitHovered = false;
    
    private Clip backgroundMusic;
    
    public MainMenu(JFrame parent) {
        this.parentFrame = parent;
        setPreferredSize(new Dimension(1280, 800));
        setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        loadImages();
        loadBackgroundMusic();
    }
    
    private void loadImages() {
        try {
            // Load background image
            backgroundImage = new ImageIcon("src/assets/Images/mainMenuImage.png").getImage();
            
            // Load button images
            startButtonImage = new ImageIcon("src/assets/Images/StartButton.png").getImage();
            exitButtonImage = new ImageIcon("src/assets/Images/ExitButton.png").getImage();
            
            // For hover effects, we'll create slightly brighter versions
            startButtonHoverImage = createHoverImage(startButtonImage);
            exitButtonHoverImage = createHoverImage(exitButtonImage);
            
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            // Fallback to colored rectangles if images fail to load
        }
    }
    
    private Image createHoverImage(Image original) {
        // Create a slightly brighter version for hover effect
        BufferedImage buffered = new BufferedImage(original.getWidth(null), original.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(original, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, original.getWidth(null), original.getHeight(null));
        g2d.dispose();
        return buffered;
    }
    
    private void loadBackgroundMusic() {
        try {
            // Try MP3 first, then fallback to WAV
            File musicFile = new File("src/assets/Music/MainMenuBackgroundSong.wav");
            if (!musicFile.exists()) {
                musicFile = new File("src/assets/Music/MainMenuBackgroundSong.wav");
            }
            
            if (musicFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioStream);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
                System.out.println("Background music loaded successfully");
            } else {
                System.out.println("No music file found at src/assets/Music/");
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
            System.out.println("MP3 format not supported. Please convert to WAV format or install MP3 support library.");
        }
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
        
        // Draw buttons
        drawButton(g2d, startButtonImage, startButtonHoverImage, startHovered, 400, 300, "");
        drawButton(g2d, exitButtonImage, exitButtonHoverImage, exitHovered, 400, 450, "");
    }
    
    private void drawButton(Graphics2D g2d, Image normalImage, Image hoverImage, boolean hovered, int x, int y, String text) {
        Image buttonImage = hovered ? hoverImage : normalImage;
        
        if (buttonImage != null) {
            g2d.drawImage(buttonImage, x, y, 200, 80, this);
        } else {
            // Fallback button drawing
            g2d.setColor(hovered ? new Color(100, 150, 255) : new Color(50, 100, 200));
            g2d.fillRoundRect(x, y, 200, 80, 10, 10);
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x, y, 200, 80, 10, 10);
        }
        
        // Draw button text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fm = g2d.getFontMetrics();
        int textX = x + (200 - fm.stringWidth(text)) / 2;
        int textY = y + 50;
        g2d.drawString(text, textX, textY);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        // Check if click is on Start Game button
        if (x >= 400 && x <= 600 && y >= 300 && y <= 380) {
            startGame();
        }
        // Check if click is on Exit button
        else if (x >= 400 && x <= 600 && y >= 450 && y <= 530) {
            exitGame();
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        boolean wasStartHovered = startHovered;
        boolean wasExitHovered = exitHovered;
        
        startHovered = (x >= 400 && x <= 600 && y >= 300 && y <= 380);
        exitHovered = (x >= 400 && x <= 600 && y >= 450 && y <= 530);
        
        if (startHovered != wasStartHovered || exitHovered != wasExitHovered) {
            repaint();
        }
    }
    
    @Override
    public void mousePressed(MouseEvent e) {}
    
    @Override
    public void mouseReleased(MouseEvent e) {}
    
    @Override
    public void mouseEntered(MouseEvent e) {}
    
    @Override
    public void mouseExited(MouseEvent e) {}
    
    @Override
    public void mouseDragged(MouseEvent e) {}
    
    private void startGame() {
        // Stop background music
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
        
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
    
    private void exitGame() {
        // Stop background music
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
        
        System.exit(0);
    }
}
