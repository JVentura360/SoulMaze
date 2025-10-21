package main;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player {
    public int x, y;
    public int speed = 4;
    public int size = 20;
    private Maze maze;

    boolean up, down, left, right;

    public Player(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update() {
        int nextX = x;
        int nextY = y;

        if (up) nextY -= speed;
        if (down) nextY += speed;
        if (left) nextX -= speed;
        if (right) nextX += speed;

        // Check horizontal move first
        if (canMove(nextX, y)) x = nextX;

        // Then vertical move separately
        if (canMove(x, nextY)) y = nextY;
    }

    /** 
     * Checks if the player can move to (newX, newY) 
     * using strict 4-corner collision detection.
     */
    private boolean canMove(int newX, int newY) {
        int s = size;
        int left = newX;
        int right = newX + s - 1;
        int top = newY;
        int bottom = newY + s - 1;

        // Check all tiles the player overlaps
        for (int row = top / maze.tileSize; row <= bottom / maze.tileSize; row++) {
            for (int col = left / maze.tileSize; col <= right / maze.tileSize; col++) {
                if (maze.isWallTile(row, col)) {
                    return false;
                }
            }
        }
        return true;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        // Slightly smaller so it doesn’t appear inside wall edges visually
        g.fillOval(x + 1, y + 1, size - 2, size - 2);
    }

    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) up = true;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) down = true;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) left = true;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) right = true;
    }

    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (k == KeyEvent.VK_W || k == KeyEvent.VK_UP) up = false;
        if (k == KeyEvent.VK_S || k == KeyEvent.VK_DOWN) down = false;
        if (k == KeyEvent.VK_A || k == KeyEvent.VK_LEFT) left = false;
        if (k == KeyEvent.VK_D || k == KeyEvent.VK_RIGHT) right = false;
    }

    public void stop() {
        up = down = left = right = false;
    }
}
