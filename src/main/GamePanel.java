package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

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
    private int fogRadius = 180; // radius around player to clear
    private double fogPulse = 0;
    private AudioManager audioManager;
    private Jumpscare jumpscare;
    private String playerName; // Default player name
    private boolean heartbeatBleeding = false;
    private Point girlJumpscareSpot;
    private long nextJumpscareTime;
    private Random rand = new Random();
    // === Constructor ===
    public GamePanel(LevelManager levelManager, String playerName) {
        if (playerName == null || playerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Player name must not be null or empty.");
        }
        setPreferredSize(new Dimension(1200, 780));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        // Use provided level manager or create new one
        this.levelManager = levelManager;
        this.playerName = playerName;
        System.out.println("GamePanel initialized with player name: " + this.playerName);
        // --- Audio setup ---
        audioManager = new AudioManager();
        //jumpscare
        jumpscare = new Jumpscare(audioManager, 3500);
        jumpscare.attachToPanel(this);
        // Load BGM
        audioManager.loadBackgroundMusic("src/assets/Music/Gameplay.wav");
        audioManager.fadeInBackgroundMusic(2000, true); // loop gameplay BGM

        // Load heartbeats
        audioManager.loadSFX("heartbeatNormal", "src/assets/Music/HeartbeatNormal.wav");
        audioManager.loadSFX("heartbeatFast", "src/assets/Music/HeartbeatFast.wav");

        // Play normal heartbeat immediately
        heartbeatBleeding = false; // ensure state is consistent
        audioManager.playSFX("heartbeatNormal", true); // looped
        
        // Initialize core game objects
        initializeLevel();
     // Jumpscare setup
        girlJumpscareSpot = maze.getRandomOpenTile();
        scheduleNextJumpscare();
        
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

        // --- Fog & player update ---
        fogPulse += 0.05;
        int baseRadius = 150;
        int targetRadius = player.isHoldingSoul() ? baseRadius + 60 + (int)(Math.sin(fogPulse) * 20)
                                                  : baseRadius + (int)(Math.sin(fogPulse) * 10);
        fogRadius += (targetRadius - fogRadius) * 0.1;
        player.update();

     // --- Ghost updates & collision ---
        for (Ghost ghost : ghosts) {
            ghost.update(player);

            if (ghost.collidesWith(player)) {
                if (player.canBeHit()) {
                    player.collideWithGhost(); // triggers bleeding or death
                    if (player.isDead()) {
                        handleGameOver();
                    } else if (player.isBleeding()) {
                        System.out.println("Player hit! Speed reduced to " + player.speed);
                    }
                }
                // else: still immune, ignore collision
            }
        }
        
        updateHeartbeat();
        
        // --- Trigger girl jumpscare when player is near her spot ---
        if (girlJumpscareSpot != null && !jumpscare.isActive()) {
            double dx = player.x - girlJumpscareSpot.x;
            double dy = player.y - girlJumpscareSpot.y;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 80) { // 80 pixels detection range
                jumpscare.trigger("girl");
                scheduleNextJumpscare(); // move to a new random spot
            }
        }
        
        // --- Check level completion ---
        if (levelManager.isLevelCompleted(graves)) {
            handleLevelCompletion();
        }
    }

    private void handleGameOver() {
    	audioManager.fadeOutBackgroundMusic(2000);
        // Stop both heartbeats
        audioManager.fadeOutSFX("heartbeatNormal",1000);
        audioManager.fadeOutSFX("heartbeatFast",1000);
        gameOver = true;
        running = false; // stop the loop if you want
        if (gameThread != null && gameThread.isAlive()) {
            try {
                gameThread.join(100); // wait up to 0.1s for it to end cleanly
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Save the player's score
        int finalScore = levelManager.getScore();
        ScoreManager.saveScore(playerName, finalScore);
        System.out.println("Game Over! Final score for " + playerName + ": " + finalScore);
        
        if (audioManager != null) {
            audioManager.fadeOutBackgroundMusic(2000); // fade out over 2 seconds
        }
        // Stop both heartbeats
        audioManager.fadeOutSFX("heartbeatNormal",2000);
        audioManager.fadeOutSFX("heartbeatFast",2000);

        // Show custom Game Over Panel
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        GameOverPanel.GameOverListener listener = new GameOverPanel.GameOverListener() {
        	
            public void onRetry() {
            	// Stop both heartbeats
                audioManager.fadeOutSFX("heartbeatNormal",2000);
                audioManager.fadeOutSFX("heartbeatFast",2000);
                // Restart game with same LevelManager and playerName
                parentFrame.getContentPane().removeAll();
                GamePanel retryPanel = new GamePanel(levelManager, playerName);
                parentFrame.add(retryPanel);
                parentFrame.pack();
                parentFrame.revalidate();
                parentFrame.repaint();
                retryPanel.requestFocusInWindow();
            }
            public void onQuit() {
                // Return to MainMenu
                parentFrame.getContentPane().removeAll();
                MainMenu menuPanel = new MainMenu(parentFrame);
                parentFrame.add(menuPanel);
                parentFrame.pack();
                parentFrame.revalidate();
                parentFrame.repaint();
            }
        };
        // ...existing code...
SwingUtilities.invokeLater(() -> {
    JFrame parentFrame1 = (JFrame) SwingUtilities.getWindowAncestor(this);
    if (parentFrame == null) {
        System.out.println("handleGameOver: parentFrame is null");
        return;
    }

    // Create the overlay panel
    GameOverPanel overlay = new GameOverPanel(listener);

    // Get the glass pane and set its layout
    JPanel glassPane = (JPanel) parentFrame1.getGlassPane();
    glassPane.setLayout(new GridLayout(1, 1));
    glassPane.removeAll(); // Clear previous components
    glassPane.add(overlay);

    // Make the glass pane visible
    glassPane.setVisible(true);

    // Revalidate and repaint to show the overlay
    parentFrame1.revalidate();
    parentFrame1.repaint();
});

        
    }
    
    private void handleLevelCompletion() {
        levelCompleted = true;
        levelManager.addScore(100); // Bonus points for completing level
        // Stop both heartbeats
        audioManager.fadeOutBackgroundMusic(2000);
        audioManager.fadeOutSFX("heartbeatNormal",2000);
        audioManager.fadeOutSFX("heartbeatFast",2000);
        
        // Check if game is completed
        if (levelManager.isGameCompleted()) {
            // Game completed - show victory screen
            gameOver = true;
            running = false;
        } else {
        	// Show level up transition when advancing to levels 2-18
        	if (levelManager.getCurrentLevel() == 1) {
        	    if (rand.nextFloat() < 1.0f) {
        	        System.out.println("🎃 Triggering Smile Jumpscare instead of level up panel!");

        	        // Wait for jumpscare to finish before transition
        	        jumpscare.setOnFinish(() -> {
        	            System.out.println("😈 Jumpscare finished — proceeding to level up transition!");
        	            SwingUtilities.invokeLater(this::showLevelUpTransition);
        	        });

        	        jumpscare.trigger("smile");
        	        return; // prevent transition from running immediately
        	    }

        	    // No jumpscare triggered, continue as normal
        	    showLevelUpTransition();
        	} else {
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
        GamePanel nextLevelPanel = new GamePanel(levelManager, playerName);
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
        
        // Draw all ghosts
        for (Ghost ghost : ghosts) {
            ghost.draw(g);
        }
        
        for (Grave grave : graves) {
            grave.draw(g);
        }
        player.draw(g);
        
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
        for (Soul s : souls) {
            s.update();
            s.draw(g);
        }
                Graphics2D g2 = (Graphics2D) g;

        g2.setFont(new Font("Chiller", Font.BOLD, 20));

        // Bloody red gradient
        GradientPaint gp = new GradientPaint(0, 0, Color.RED, 0, 50, Color.BLACK, true);
        g2.setPaint(gp);

        // Add shadow for creepiness
        g2.drawString("Score: " + levelManager.getScore(), 20, 30);
        g2.drawString(levelManager.getLevelDescription(), 20, 50);
        g2.setColor(Color.RED);
        g2.drawString("Score: " + levelManager.getScore(), 19, 29);
        g2.drawString(levelManager.getLevelDescription(), 19, 49);
        
       
 
        jumpscare.draw(g, getWidth(), getHeight());
    }

    // === Input Handling ===
    @Override
    public void keyPressed(KeyEvent e) {
        player.keyPressed(e);
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
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
                    // Wrong grave ' soul respawns elsewhere
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

     // === Smooth circular reveal around player (center clear ' edge dark) ===
        int px = player.x + player.size / 2;
        int py = player.y + player.size / 2;
        int radius = fogRadius;

        // The gradient starts fully transparent (clear) in the center, and darkens toward the edge
        RadialGradientPaint gradient = new RadialGradientPaint(
            new Point(px, py),
            radius,
            new float[]{0.1f, 0.8f, 1.0f},
            new Color[]{
                new Color(0f, 0f, 0f, 1f),   // 0.0 ' fully opaque black (we'll "erase" this later)
                new Color(0f, 0f, 0f, 0.1f), // middle fade
                new Color(0f, 0f, 0f, 0f)    // 1.0 ' transparent outer edge
            }
        );

        // Use this gradient as the 'erase' mask
        g2.setPaint(gradient);
        g2.setComposite(AlphaComposite.DstOut);
        g2.fillOval(px - radius, py - radius, radius * 2, radius * 2);

        // Use the gradient as an erase mask
        g2.setPaint(gradient);
        g2.setComposite(AlphaComposite.DstOut);
        g2.fillOval(px - radius, py - radius, radius * 2, radius * 2);

        g2.dispose();

        // Draw the fog layer over the main scene
        g.drawImage(fogLayer, 0, 0, null);
        
     // --- Overlay bleeding effect ---
        if (player.isBleeding()) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setColor(new Color(255, 0, 0, 80)); // Red with alpha 80 (out of 255)
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }
    
    private void updateHeartbeat() {
    	if (gameOver || levelCompleted) return;
        boolean shouldBeBleeding = player.isBleeding();

        if (shouldBeBleeding != heartbeatBleeding) {
            // State changed: switch heartbeat
            heartbeatBleeding = shouldBeBleeding;

         // Stop both heartbeats
            audioManager.stopSFX("heartbeatNormal");
            audioManager.stopSFX("heartbeatFast");

            // Play correct heartbeat loop
            if (heartbeatBleeding) {
            	// === Trigger skull jumpscare once when bleeding starts ===
                if (!jumpscare.isActive()) {
                    jumpscare.trigger("skull");
                }
                audioManager.playSFX("heartbeatFast", true);
            } else {
                audioManager.playSFX("heartbeatNormal", true);
            }

            System.out.println("Switched heartbeat to " + (heartbeatBleeding ? "Fast" : "Normal"));
        }
    }
    
 // Schedule the next jumpscare and pick a centered open spot
    private void scheduleNextJumpscare() {
        long now = System.currentTimeMillis();
        long delay = 30000 + rand.nextInt(30000); // 30–60 seconds
        nextJumpscareTime = now + delay;

        girlJumpscareSpot = getCentralOpenTile();
    }

    // Picks an open tile with space on all sides (not touching walls)
    private Point getCentralOpenTile() {
        int attempts = 0;
        while (attempts < 1000) {
            Point p = maze.getRandomOpenTile();
            int row = p.y / maze.tileSize;
            int col = p.x / maze.tileSize;

            // Check surrounding tiles to ensure it's not near walls
            boolean safe = true;
            for (int dr = -1; dr <= 1; dr++) {
                for (int dc = -1; dc <= 1; dc++) {
                    if (maze.isWall(row + dr, col + dc)) {
                        safe = false;
                        break;
                    }
                }
                if (!safe) break;
            }

            if (safe) {
                // Offset to the center of the tile
                return new Point(col * maze.tileSize + maze.tileSize / 2,
                                 row * maze.tileSize + maze.tileSize / 2);
            }

            attempts++;
        }

        // fallback (should rarely happen)
        return maze.getRandomOpenTile();
    }
    
    private int durationAfterJumpscare() {
        return 3500; // milliseconds (same duration as your jumpscare)
    }
}
