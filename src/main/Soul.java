package main;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Soul {
	private static final int SIZE = 40;

    private int x, y;
    private Color color;

    // Animation
    private double glowPhase = Math.random() * Math.PI * 2;
    private static final double GLOW_SPEED = 0.05;
    private static final int GLOW_RADIUS = 15;

    public Soul(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void update() {
        glowPhase += GLOW_SPEED;
        if (glowPhase > Math.PI * 2) glowPhase -= Math.PI * 2;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // --- Glow effect ---
        float alpha = (float) ((Math.sin(glowPhase) + 1) / 2 * 0.3 + 0.2);
        int glowSize = SIZE + GLOW_RADIUS;
        int glowX = x - (glowSize - SIZE) / 2;
        int glowY = y - (glowSize - SIZE) / 2;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(color);
        g2d.fillOval(glowX, glowY, glowSize, glowSize);

        // Reset opacity
        g2d.setComposite(AlphaComposite.SrcOver);

        // --- Core soul ---
        g2d.setColor(color);
        g2d.fillOval(x, y, SIZE, SIZE);

        // --- Outline ---
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, SIZE, SIZE);
    }
    
    /**
     * Draw the soul at an arbitrary position (used when the player is holding it).
     * This reuses the same glow logic as draw().
     */
    public void drawAt(Graphics g, int drawX, int drawY) {
        Graphics2D g2d = (Graphics2D) g;

        // Glow
        float alpha = (float) ((Math.sin(glowPhase) + 1) / 2 * 0.3 + 0.2);
        int glowSize = SIZE + GLOW_RADIUS;
        int glowX = drawX - (glowSize - SIZE) / 2;
        int glowY = drawY - (glowSize - SIZE) / 2;

        Composite oldComp = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(color);
        g2d.fillOval(glowX, glowY, glowSize, glowSize);
        g2d.setComposite(oldComp);

        // Core soul
        g2d.setColor(color);
        g2d.fillOval(drawX, drawY, SIZE, SIZE);

        // Outline
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(drawX, drawY, SIZE, SIZE);
    }

    /**
     * Respawn an existing soul somewhere else in the maze on an empty tile.
     * Keeps the soul's color and repositions it.
     */
    public static void respawn(Soul soul, Maze maze) {
        Random rand = new Random();
        int rows = maze.getRows();
        int cols = maze.getCols();

        // Collect empty tile positions (pixel coordinates)
        List<Point> empty = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (maze.mazeData[r].charAt(c) == ' ') {
                    int px = c * maze.tileSize;
                    int py = r * maze.tileSize;
                    empty.add(new Point(px, py));
                }
            }
        }

        if (empty.isEmpty()) return; // nowhere to put it

        // pick random available tile that does not overlap walls (safety)
        Collections.shuffle(empty, rand);
        for (Point p : empty) {
            int row = p.y / maze.tileSize;
            int col = p.x / maze.tileSize;
            if (!maze.isWall(row, col)) {
                soul.setPosition(p.x, p.y);
                return;
            }
        }

        // fallback: place at first empty tile
        Point p = empty.get(0);
        soul.setPosition(p.x, p.y);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    /**
     * Generates one Soul per Grave color.
     * Souls spawn only on empty ' ' tiles and do not overlap with each other or walls.
     */
    public static List<Soul> generateSouls(Maze maze, List<Grave> graves) {
        List<Point> emptyTiles = new ArrayList<>();
        List<Soul> souls = new ArrayList<>();
        Random rand = new Random();

        // Collect all empty floor spaces
        for (int r = 0; r < maze.mazeData.length; r++) {
            for (int c = 0; c < maze.mazeData[r].length(); c++) {
                if (maze.mazeData[r].charAt(c) == ' ') {
                    emptyTiles.add(new Point(c * maze.tileSize, r * maze.tileSize));
                }
            }
        }

        // Shuffle possible spawn tiles
        Collections.shuffle(emptyTiles, rand);

        for (Grave grave : graves) {
            Color color = grave.getColor();
            for (int i = 0; i < emptyTiles.size(); i++) {
                Point p = emptyTiles.get(i);
                Rectangle rect = new Rectangle(p.x, p.y, SIZE, SIZE);

                boolean overlap = false;
                
                // Check collision with other souls
                for (Soul s : souls) {
                    if (s.getBounds().intersects(rect)) {
                        overlap = true;
                        break;
                    }
                }
                
                // Check collision with graves
                if (!overlap) {
                    for (Grave g : graves) {
                        if (g.getBounds().intersects(rect)) {
                            overlap = true;
                            break;
                        }
                    }
                }

                // Check if the soul would be on a wall tile
                boolean onWall = false;
                int col = p.x / maze.tileSize;
                int row = p.y / maze.tileSize;
                
                // Check all tiles the soul would occupy
                for (int r = row; r < row + (SIZE / maze.tileSize) + 1; r++) {
                    for (int c = col; c < col + (SIZE / maze.tileSize) + 1; c++) {
                        if (maze.isWall(r, c)) {
                            onWall = true;
                            break;
                        }
                    }
                    if (onWall) break;
                }
                
                if (!overlap && !onWall) {
                    souls.add(new Soul(p.x, p.y, color));
                    emptyTiles.remove(i);
                    break;
                }
            }
        }

        return souls;
    }

    public Color getColor() {
        return color;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
