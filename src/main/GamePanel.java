package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GamePanel extends JPanel implements KeyListener, Runnable{
	private Thread gameThread;
    private boolean running = true;
    private Player player;
    private Ghost ghost;
    private Maze maze;
    private boolean gameOver = false;

    public GamePanel() {
        setPreferredSize(new Dimension(1280, 800));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);
        maze = new Maze();
        player = new Player(640, 420, maze);
        ghost = new Ghost(40, 150, maze);

        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        while (running) {
            update();
            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {}
        }
    }

    private void update() {
        if (!gameOver) {
            player.update();
            ghost.update(player);

            if (ghost.collidesWith(player)) {
                gameOver = true;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        maze.draw(g);
        player.draw(g);
        ghost.draw(g);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 220, 240);
        }
    }

    public void keyPressed(KeyEvent e) { player.keyPressed(e); }
    public void keyReleased(KeyEvent e) { player.keyReleased(e); }
    public void keyTyped(KeyEvent e) {}
}
