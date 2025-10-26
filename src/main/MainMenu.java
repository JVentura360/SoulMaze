package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class MainMenu extends JPanel implements MouseListener, MouseMotionListener {
    private JFrame parentFrame;
    private Image backgroundImage;
    private Image startButtonImage;
    private Image exitButtonImage;
    private Image startButtonHoverImage;
    private Image exitButtonHoverImage;
    
    private boolean startHovered = false;
    private boolean exitHovered = false;
    
    public MainMenu(JFrame parent) {
        this.parentFrame = parent;
        setPreferredSize(new Dimension(1280, 800));
        setBackground(Color.BLACK);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        loadImages();
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
        
        // Draw buttons horizontally aligned below title
        int buttonY = 650; // Position below title
        int startButtonX = (getWidth() - 200) / 2 - 120; // Left of center
        int exitButtonX = (getWidth() - 200) / 2 + 120;  // Right of center
        
        drawButton(g2d, startButtonImage, startButtonHoverImage, startHovered, startButtonX, buttonY, "");
        drawButton(g2d, exitButtonImage, exitButtonHoverImage, exitHovered, exitButtonX, buttonY, "");
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
        
        // Calculate button positions dynamically
        int buttonY = 650;
        int startButtonX = (getWidth() - 200) / 2 - 120;
        int exitButtonX = (getWidth() - 200) / 2 + 120;
        
        // Check if click is on Start Game button
        if (x >= startButtonX && x <= startButtonX + 200 && y >= buttonY && y <= buttonY + 80) {
            startGame();
        }
        // Check if click is on Exit button
        else if (x >= exitButtonX && x <= exitButtonX + 200 && y >= buttonY && y <= buttonY + 80) {
            exitGame();
        }
    }
    
    @Override
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        boolean wasStartHovered = startHovered;
        boolean wasExitHovered = exitHovered;
        
        // Calculate button positions dynamically
        int buttonY = 650;
        int startButtonX = (getWidth() - 200) / 2 - 120;
        int exitButtonX = (getWidth() - 200) / 2 + 120;
        
        startHovered = (x >= startButtonX && x <= startButtonX + 200 && y >= buttonY && y <= buttonY + 80);
        exitHovered = (x >= exitButtonX && x <= exitButtonX + 200 && y >= buttonY && y <= buttonY + 80);
        
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
    
    private void exitGame() {
        System.exit(0);
    }
}
