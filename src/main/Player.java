package main;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player {
    public int x, y;
    public int speed = 4;
    public int size = 25;
    Maze maze;

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

        if (!isColliding(nextX, nextY)) {
            x = nextX;
            y = nextY;
        } else {
            // Try move axis-separately for smoother sliding
            if (!isColliding(nextX, y)) x = nextX;
            else if (!isColliding(x, nextY)) y = nextY;
        }
    }

    private boolean isColliding(int newX, int newY) {
        return maze.isWall(newX, newY) ||
               maze.isWall(newX + size - 1, newY) ||
               maze.isWall(newX, newY + size - 1) ||
               maze.isWall(newX + size - 1, newY + size - 1);
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillOval(x, y, size, size);
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
}
