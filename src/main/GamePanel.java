package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private Ghost ghost;
    private Maze maze;
    private List<Grave> graves;
    private List<Soul> souls;
    private int score = 0;
    private boolean gameOver = false;

    // === Constructor ===
    public GamePanel() {
        setPreferredSize(new Dimension(1200, 780));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

     // Initialize core game objects
        maze = new Maze();

        // Find spawn points from the maze
        Point playerSpawn = maze.getPlayerSpawn();
        Point ghostSpawn = maze.getGhostSpawn();

        // Fallback defaults if not found
        if (playerSpawn == null) playerSpawn = new Point(640, 400);
        if (ghostSpawn == null) ghostSpawn = new Point(100, 100);

        // Initialize player and ghost at their spawn points
        player = new Player(playerSpawn.x, playerSpawn.y, maze);
        ghost = new Ghost(ghostSpawn.x, ghostSpawn.y, maze);
        graves = Grave.generateGraves(maze, 3);
        souls = Soul.generateSouls(maze, graves);
        // Start main game loop
        gameThread = new Thread(this);
        gameThread.start();

        // Make sure panel gets keyboard focus
        requestFocusInWindow();
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
        if (gameOver) return;

        // Update player and ghost
        player.update();
        ghost.update(player);

        // Check collision between ghost and player
        if (ghost.collidesWith(player)) {
            handleGameOver();
        }
    }

    private void handleGameOver() {
        gameOver = true;
        running = false; // stop the loop if you want
    }

    // === Drawing ===
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        maze.draw(g);
        player.draw(g);
        ghost.draw(g);
        for (Grave grave : graves) {
            grave.draw(g);
        }
        for (Soul s : souls) {
            s.update();
            s.draw(g);
        }
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 20, 20);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 480, 380);
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
                    score++;
                    // Spawn new pair
                    graves.addAll(Grave.generateGraves(maze, 1));
                    souls.addAll(Soul.generateSouls(maze, graves.subList(graves.size() - 1, graves.size())));
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
