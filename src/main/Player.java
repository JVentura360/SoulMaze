package main;
import java.awt.*;
import java.awt.event.KeyEvent;

public class Player {
	public int x, y;
    public int speed = 4;
    public int size = 60;
    private Maze maze;

    private static final int GAP = 2; // solid 2px gap between player and wall

    boolean up, down, left, right;

    public Player(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update() {
        int nextX = x;
        int nextY = y;

        // Horizontal movement
        if (left) nextX -= speed;
        if (right) nextX += speed;
        if (canMove(nextX, y)) x = nextX;

        // Vertical movement
        if (up) nextY -= speed;
        if (down) nextY += speed;
        if (canMove(x, nextY)) y = nextY;
    }

    /**
     * Checks wall collisions keeping a strict 2px gap outside the player.
     */
    private boolean canMove(int newX, int newY) {
        int s = size;
        int ts = maze.tileSize;

        // expand the collision area slightly to enforce a 2px gap
        int left = newX - GAP;
        int right = newX + s + GAP - 1;
        int top = newY - GAP;
        int bottom = newY + s + GAP - 1;

        // convert to tile indices
        int leftCol = (int) Math.floor(left / (double) ts);
        int rightCol = (int) Math.floor(right / (double) ts);
        int topRow = (int) Math.floor(top / (double) ts);
        int bottomRow = (int) Math.floor(bottom / (double) ts);

        // check all tiles touching the expanded hitbox
        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (maze.isWallTile(row, col)) {
                    // If any wall tile intersects within this expanded range, block movement
                    return false;
                }
            }
        }
        return true;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, size, size);
    }

    // Input handling
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

    // Tile conversion helpers
    public int getTileRow() {
        return (int) Math.floor((y + size / 2.0) / maze.tileSize);
    }

    public int getTileCol() {
        return (int) Math.floor((x + size / 2.0) / maze.tileSize);
    }
}
