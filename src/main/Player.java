package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;
import java.awt.event.KeyEvent;

public class Player {
	public int x, y;
    public int speed = 4;
    public int normalSpeed = 4;
    public int slowSpeed = 2;
    public int size = 60;
    private Maze maze;
    private Soul heldSoul = null;
    boolean isHoldingSoul() { return heldSoul != null; }
    public Soul getHeldSoul() { return heldSoul; }
 // === Bleeding state ===
    private int hitCount = 0; // 0 = normal, 1 = bleeding, 2 = dead
    private long bleedStartTime = 0;               // Time when bleeding started
    private final int IMMUNE_DURATION = 5000;      // 5 seconds
    
    private static final int GAP = 2; // solid 2px gap between player and wall

    boolean up, down, left, right;

    private static final String SPRITE_DIR = "src/assets/Images/";
    private static final Image PLAYER_UP = new ImageIcon(SPRITE_DIR + "PlayerUp.png").getImage();
    private static final Image PLAYER_DOWN = new ImageIcon(SPRITE_DIR + "PlayerDown.png").getImage();
    private static final Image PLAYER_LEFT = new ImageIcon(SPRITE_DIR + "PlayerLeft.png").getImage();
    private static final Image PLAYER_RIGHT = new ImageIcon(SPRITE_DIR + "PlayerRight.png").getImage();

    public Player(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update() {
        int nextX = x;
        int nextY = y;

        // movement logic
        if (left) nextX -= speed;
        if (right) nextX += speed;
        if (canMove(nextX, y)) x = nextX;

        if (up) nextY -= speed;
        if (down) nextY += speed;
        if (canMove(x, nextY)) y = nextY;

        // restore speed if immunity expired
        if (hitCount == 1) {
            long now = System.currentTimeMillis();
            if (now - bleedStartTime >= IMMUNE_DURATION) {
                speed = normalSpeed;
            }
        }
    }
    
    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
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
    
    public void pickUpSoul(Soul soul) {
        heldSoul = soul;
    }

    public void dropSoul(int x, int y) {
        if (heldSoul != null) {
            heldSoul.setPosition(x, y);
            heldSoul = null;
        }
    }

    public void removeHeldSoul() {
        heldSoul = null;
    }

    public void draw(Graphics g) {
        Image sprite = PLAYER_DOWN;
        if (up) sprite = PLAYER_UP;
        else if (down) sprite = PLAYER_DOWN;
        else if (left) sprite = PLAYER_LEFT;
        else if (right) sprite = PLAYER_RIGHT;
        if (sprite != null) {
            g.drawImage(sprite, x, y, size, size, null);
        } else {
            g.setColor(Color.GREEN);
            g.fillRect(x, y, size, size);
        }
        if (heldSoul != null) {
            heldSoul.drawAt(g, x + size / 4, y - 20);
        }
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
    
    public void collideWithGhost() {
        long now = System.currentTimeMillis();

        if (hitCount == 0) {
            // First collision → trigger bleeding
            hitCount = 1;
            speed = slowSpeed;
            bleedStartTime = now;
        } else if (hitCount == 1) {
            // Second collision only counts if immunity expired
            if (now - bleedStartTime >= IMMUNE_DURATION) {
                hitCount = 2; // dead
            }
        }
    }
    public boolean isBleeding() {
        return hitCount == 1;
    }

    public boolean isDead() {
        return hitCount >= 2;
    }
    
    public boolean canBeHit() {
        if (hitCount == 0) return true; // first hit always counts
        if (hitCount == 1) {
            long now = System.currentTimeMillis();
            return now - bleedStartTime >= IMMUNE_DURATION; // only count if immunity expired
        }
        return false; // dead already
    }

    // Optional: reset state (for restarting the game)
    public void reset() {
        speed = normalSpeed;
        hitCount = 0;
        x = 640;
        y = 420;
        stop();
    }
}
