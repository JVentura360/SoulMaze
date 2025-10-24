package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Handles transition panels with fade in/out effects
 * Used for StartGamePanel.png and levelUpPanel.png
 */
public class TransitionPanel extends JPanel implements ActionListener {
    private Image backgroundImage;
    private float alpha = 0.0f;
    private boolean fadingIn = true;
    private boolean transitionComplete = false;
    private Timer fadeTimer;
    private JFrame parentFrame;
    private Runnable onComplete;
    
    public TransitionPanel(JFrame parent, String imagePath, Runnable completionCallback) {
        this.parentFrame = parent;
        this.onComplete = completionCallback;
        
        setPreferredSize(new Dimension(1280, 800));
        setBackground(Color.BLACK);
        
        // Load the transition image
        try {
            backgroundImage = new ImageIcon(imagePath).getImage();
        } catch (Exception e) {
            System.err.println("Error loading transition image: " + e.getMessage());
        }
        
        // Start fade in
        startFadeIn();
    }
    
    private void startFadeIn() {
        fadingIn = true;
        alpha = 0.0f;
        transitionComplete = false;
        
        fadeTimer = new Timer(50, this); // 20 FPS for smooth fade
        fadeTimer.start();
    }
    
    private void startFadeOut() {
        fadingIn = false;
        alpha = 1.0f;
        
        fadeTimer = new Timer(50, this);
        fadeTimer.start();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (fadingIn) {
            alpha += 0.05f; // Fade in speed
            if (alpha >= 1.0f) {
                alpha = 1.0f;
                fadeTimer.stop();
                
                // Wait 2 seconds before starting fade out
                Timer waitTimer = new Timer(2000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        startFadeOut();
                    }
                });
                waitTimer.setRepeats(false);
                waitTimer.start();
            }
        } else {
            alpha -= 0.05f; // Fade out speed
            if (alpha <= 0.0f) {
                alpha = 0.0f;
                fadeTimer.stop();
                transitionComplete = true;
                
                // Call completion callback
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        }
        
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Set alpha for fade effect
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        
        // Draw background image
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            // Fallback if image fails to load
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
        
        // Reset alpha
        g2d.setComposite(AlphaComposite.SrcOver);
    }
    
    public boolean isTransitionComplete() {
        return transitionComplete;
    }
}
