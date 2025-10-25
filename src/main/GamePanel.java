package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

/**
 * GamePanel handles the main gameplay loop, drawing, and logic.
 * It manages the maze, player, ghost, and checkpoint/question system.
 */
public class GamePanel extends JPanel implements KeyListener, Runnable {
	private Thread gameThread;
    private boolean running = true;

    private Player player;
    private List<Ghost> ghosts;
    private Maze maze;
    private List<Grave> graves;
    private List<Soul> souls;
    private LevelManager levelManager;
    private boolean gameOver = false;
    private boolean levelCompleted = false;
    private Image fogImage = new ImageIcon("src/assets/Images/fog.png").getImage();
    private int fogRadius = 200; // radius around player to clear
    private double fogPulse = 0;
    // === Constructor ===
    public GamePanel() {
        this(new LevelManager());
    }
    
    public GamePanel(LevelManager levelManager) {
        setPreferredSize(new Dimension(1200, 780));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        // Use provided level manager or create new one
        this.levelManager = levelManager;
        
        // Initialize core game objects
        initializeLevel();
        
        // Start main game loop
        gameThread = new Thread(this);
        gameThread.start();

        // Make sure panel gets keyboard focus
        requestFocusInWindow();
    }
    
    private void initializeLevel() {
        maze = new Maze();
        
        // Find spawn points from the maze
        Point playerSpawn = maze.getPlayerSpawn();
        Point ghostSpawn = maze.getGhostSpawn();

        // Fallback defaults if not found
        if (playerSpawn == null) playerSpawn = new Point(640, 400);
        if (ghostSpawn == null) ghostSpawn = new Point(100, 100);

        // Initialize player
        player = new Player(playerSpawn.x, playerSpawn.y, maze);
        
        // Initialize ghosts based on level with specific positions
        ghosts = new ArrayList<>();
        int ghostCount = levelManager.getGhostCount();
        
        // Define specific ghost spawn positions (adjusted to avoid walls)
        Point[] ghostPositions = {
            new Point(10, 50),      // Upper left (safer position)
            new Point(1120, 50),     // Upper right (safer position)
            new Point(600, 650)      // Lower middle (safer position)
        };
        
        for (int i = 0; i < ghostCount; i++) {
            Point ghostPos = ghostPositions[i];
            
            // Ensure ghost doesn't spawn on wall
            int col = ghostPos.x / maze.tileSize;
            int row = ghostPos.y / maze.tileSize;
            
            // If position is on wall, find nearest valid position
            if (maze.isWall(row, col)) {
                // Try to find a nearby valid position
                boolean foundValid = false;
                for (int offset = 1; offset <= 5 && !foundValid; offset++) {
                    for (int dr = -offset; dr <= offset && !foundValid; dr++) {
                        for (int dc = -offset; dc <= offset && !foundValid; dc++) {
                            int newRow = row + dr;
                            int newCol = col + dc;
                            if (!maze.isWall(newRow, newCol)) {
                                ghostPos = new Point(newCol * maze.tileSize, newRow * maze.tileSize);
                                foundValid = true;
                            }
                        }
                    }
                }
            }
            
            ghosts.add(new Ghost(ghostPos.x, ghostPos.y, maze));
        }
        
        // Generate souls and graves based on level
        int soulsPerLevel = levelManager.getSoulsPerLevel();
        graves = Grave.generateGraves(maze, soulsPerLevel);
        souls = Soul.generateSouls(maze, graves);
    }

    // === Game Loop ===
    @Override
    public void run() {
        while (running) {
            update();
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException ignored) {}
        }
    }

    // === Game Logic ===
    private void update() {
        if (gameOver || levelCompleted) return;
        
	// Pulse fog radius gently
        fogPulse += 0.05;
        fogRadius = 150 + (int)(Math.sin(fogPulse) * 10);
        
        // Update player
        player.update();
        
        // Update all ghosts
        for (Ghost ghost : ghosts) {
            ghost.update(player);
            
            // Check collision between ghost and player
            if (ghost.collidesWith(player)) {
                handleGameOver();
                return;
            }
        }
        
        // Check level completion
        if (levelManager.isLevelCompleted(graves)) {
            handleLevelCompletion();
        }
    }

    private void handleGameOver() {
        gameOver = true;
        running = false; // stop the loop if you want
    }
    
    private void handleLevelCompletion() {
        levelCompleted = true;
        levelManager.addScore(100); // Bonus points for completing level
        
        // Check if game is completed
        if (levelManager.isGameCompleted()) {
            // Game completed - show victory screen
            gameOver = true;
            running = false;
        } else {
            // Show level up transition when advancing to levels 2-18
            if (levelManager.getCurrentLevel() == 1) {
                // We're completing level 1, so show transition before going to level 2
                showLevelUpTransition();
            } else {
                // For other levels, show transition
                showLevelUpTransition();
            }
        }
    }
    
    private void showLevelUpTransition() {
        // Stop the game loop temporarily
        running = false;
        
        // Get parent frame before removing this panel
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        
        // Show level up transition
        parentFrame.getContentPane().removeAll();
        TransitionPanel transitionPanel = new TransitionPanel(parentFrame, "src/assets/Images/levelUpPanel.png", new Runnable() {
            @Override
            public void run() {
                // Transition complete, create new GamePanel for next level
                createNextLevel(parentFrame);
            }
        });
        parentFrame.add(transitionPanel);
        parentFrame.pack();
        parentFrame.revalidate();
        parentFrame.repaint();
    }
    
    private void createNextLevel(JFrame parentFrame) {
        // Advance to next level
        levelManager.nextLevel();
        
        // Create new GamePanel for the next level with the same level manager
        parentFrame.getContentPane().removeAll();
        GamePanel nextLevelPanel = new GamePanel(levelManager);
        parentFrame.add(nextLevelPanel);
        parentFrame.pack();
        parentFrame.revalidate();
        parentFrame.repaint();
        
        // Request focus for keyboard input
        nextLevelPanel.requestFocusInWindow();
    }

    // === Drawing ===
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        maze.draw(g);
        player.draw(g);
        
        // Draw all ghosts
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }
        
        for (Grave grave : graves) {
            grave.draw(g);
        }
        for (Soul s : souls) {
            s.update();
            s.draw(g);
        }
        
        // Draw UI
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + levelManager.getScore(), 20, 30);
        g.drawString(levelManager.getLevelDescription(), 20, 50);
        
        // === Draw fog overlay ===
        drawFog(g);
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            if (levelManager.isGameCompleted()) {
                g.drawString("VICTORY!", 500, 380);
            } else {
                g.drawString("GAME OVER", 480, 380);
            }
        }
        
        if (levelCompleted) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("LEVEL COMPLETED!", 400, 400);
        }
    }

    // === Input Handling ===
    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            handleSoulInteraction();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}
    
    
    private void handleSoulInteraction() {
        if (player == null || maze == null) return;

        // --- PICKUP logic ---
        if (!player.isHoldingSoul()) {
            for (Soul soul : souls) {
                if (player.getBounds().intersects(soul.getBounds())) {
                    player.pickUpSoul(soul);
                    souls.remove(soul);
                    break;
                }
            }
            return;
        }

        // --- DROPPING logic ---
        Soul held = player.getHeldSoul();
        Rectangle playerRect = player.getBounds();

        // Check if dropped on grave
        for (Iterator<Grave> it = graves.iterator(); it.hasNext();) {
            Grave grave = it.next();
            if (playerRect.intersects(grave.getBounds())) {
                if (grave.getColor().equals(held.getColor())) {
                    // Correct match
                    it.remove(); // remove grave
                    player.removeHeldSoul();
                    levelManager.addScore(50); // Points for correct match
                } else {
                    // Wrong grave — soul respawns elsewhere
                    player.removeHeldSoul();
                    Soul.respawn(held, maze);
                    souls.add(held);
                }
                return;
            }
        }

        // --- Dropping on empty space ---
        int row = player.y / maze.tileSize;
        int col = player.x / maze.tileSize;
        if (!maze.isWall(row, col)) {
            held.setPosition(player.x, player.y);
            souls.add(held);
            player.removeHeldSoul();
        }
    }
    
    private void drawFog(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        // Create a transparent buffer
        BufferedImage fogLayer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = fogLayer.createGraphics();

        // Draw the fog background (semi-transparent dark layer)
        g2.setColor(new Color(0, 0, 0, 210)); // 210 = dark but not fully opaque
        g2.fillRect(0, 0, w, h);

        // Optional: overlay fog texture for atmosphere
        g2.setComposite(AlphaComposite.SrcOver);
        g2.drawImage(fogImage, 0, 0, w, h, null);

        // === Smooth circular reveal around player ===
        int px = player.x + player.size / 2;
        int py = player.y + player.size / 2;
        int radius = fogRadius;

        // Create a radial gradient that goes from full clear (center) to opaque (edges)
        RadialGradientPaint gradient = new RadialGradientPaint(
            new Point(px, py),
            radius,
            new float[]{0.7f, 0.8f, 1f}, // smooth fade
            new Color[]{
                new Color(0f, 0f, 0f, 1f),  // center fully erased
                new Color(0f, 0f, 0f, 0.3f), // mid fade
                new Color(0f, 0f, 0f, 0f)   // outer transparent
            }
        );

        // Use the gradient as an erase mask
        g2.setPaint(gradient);
        g2.setComposite(AlphaComposite.DstOut);
        g2.fillOval(px - radius, py - radius, radius * 2, radius * 2);

        g2.dispose();

        // Draw the fog layer over the main scene
        g.drawImage(fogLayer, 0, 0, null);
    }
    
    // === Reset Game (optional, for restart) ===
    private void resetGame() {
        gameOver = false;
        player.x = 640;
        player.y = 420;
        player.stop();
        running = true;
        requestFocusInWindow();

        // Restart loop if needed
        if (gameThread == null || !gameThread.isAlive()) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
}
