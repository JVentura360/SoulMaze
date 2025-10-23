package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

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
    private boolean gameOver = false;

    // === Constructor ===
    public GamePanel() {
        setPreferredSize(new Dimension(1200, 780));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        // Initialize core game objects
        maze = new Maze();
        player = new Player(640, 400, maze);
        ghost = new Ghost(15, 150, maze);

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
    }

    @Override
    public void keyReleased(KeyEvent e) {
        player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

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
