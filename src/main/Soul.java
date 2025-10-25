package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javax.swing.ImageIcon;

public class Soul {
    private static final int SIZE = 50;
    private static final Map<Color, Image> SOUL_SPRITES = new HashMap<>();
    static {
        SOUL_SPRITES.put(Color.BLUE, new ImageIcon(Soul.class.getResource("/assets/Images/BlueSoul.png")).getImage());
        SOUL_SPRITES.put(Color.RED, new ImageIcon(Soul.class.getResource("/assets/Images/RedSoul.png")).getImage());
        SOUL_SPRITES.put(Color.ORANGE, new ImageIcon(Soul.class.getResource("/assets/Images/OrangeSoul.png")).getImage());
        SOUL_SPRITES.put(Color.YELLOW, new ImageIcon(Soul.class.getResource("/assets/Images/YellowSoul.png")).getImage());
        SOUL_SPRITES.put(Color.GREEN, new ImageIcon(Soul.class.getResource("/assets/Images/GreenSoul.png")).getImage());
        SOUL_SPRITES.put(new Color(128, 0, 128), new ImageIcon(Soul.class.getResource("/assets/Images/PurpleSoul.png")).getImage());
        System.out.println("Soul Image Path: " + Soul.class.getResource("/assets/Images/BlueSoul.png"));
    }

    private int x, y;
    private Color color;

    public Soul(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics g) {
        Image img = SOUL_SPRITES.getOrDefault(color, SOUL_SPRITES.get(Color.BLUE));
        if (img != null) {
            g.drawImage(img, x, y, SIZE, SIZE, null);
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    /** Draw the soul at a different location (used when player is holding it) */
    public void drawAt(Graphics g, int drawX, int drawY) {
        Image img = SOUL_SPRITES.getOrDefault(color, SOUL_SPRITES.get(Color.BLUE));
        if (img != null) {
            g.drawImage(img, drawX, drawY, SIZE, SIZE, null);
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect(drawX, drawY, SIZE, SIZE);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public Color getColor() { return color; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    // Animation stub—does nothing if you don't want glow. Used by main GamePanel.
    public void update() {}

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

                for (int rowIdx = row; rowIdx < row + (SIZE / maze.tileSize) + 1; rowIdx++) {
                    for (int colIdx = col; colIdx < col + (SIZE / maze.tileSize) + 1; colIdx++) {
                        if (maze.isWall(rowIdx, colIdx)) {
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
}
