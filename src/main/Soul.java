package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Point;
import java.awt.RadialGradientPaint;
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
    private float glowPhase = 0; // animation phase
    private static final float GLOW_SPEED = 0.05f; // how fast it pulses

    public Soul(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        Image img = SOUL_SPRITES.getOrDefault(color, SOUL_SPRITES.get(Color.BLUE));

        // --- Pulsing glow setup ---
        float pulse = (float) ((Math.sin(glowPhase) + 1) / 2); // oscillates 0–1
        int baseGlowSize = SIZE + 30;
        int glowSize = (int) (baseGlowSize + pulse * 10); // expand slightly
        int glowOffset = (glowSize - SIZE) / 2;

        int alpha = (int) (80 + pulse * 100); // fade in/out brightness
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        // Smooth drawing
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new RadialGradientPaint(
            new Point(x + SIZE / 2, y + SIZE / 2),
            glowSize / 2f,
            new float[]{0f, 1f},
            new Color[]{glowColor, new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(x - glowOffset, y - glowOffset, glowSize, glowSize);

        // --- Draw the soul sprite ---
        if (img != null) {
            g2.drawImage(img, x, y, SIZE, SIZE, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect(x, y, SIZE, SIZE);
        }

        g2.dispose();
    }


    /** Draw the soul at a different location (used when player is holding it) */
    public void drawAt(Graphics g, int drawX, int drawY) {
        Graphics2D g2 = (Graphics2D) g.create();
        Image img = SOUL_SPRITES.getOrDefault(color, SOUL_SPRITES.get(Color.BLUE));

        // --- Pulsing glow setup ---
        float pulse = (float) ((Math.sin(glowPhase) + 1) / 2);

        // Brighter and bigger when held
        int baseGlowSize = SIZE + 45; // +30 when dropped, +45 when held
        int glowSize = (int) (baseGlowSize + pulse * 12);
        int glowOffset = (glowSize - SIZE) / 2;

        int alpha = (int) (120 + pulse * 135); // stronger brightness (120–255)
        Color glowColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);

        // Smooth glowing aura
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new RadialGradientPaint(
            new Point(drawX + SIZE / 2, drawY + SIZE / 2),
            glowSize / 2f,
            new float[]{0f, 1f},
            new Color[]{glowColor, new Color(0, 0, 0, 0)}
        ));
        g2.fillOval(drawX - glowOffset, drawY - glowOffset, glowSize, glowSize);

        // --- Draw the soul sprite ---
        if (img != null) {
            g2.drawImage(img, drawX, drawY, SIZE, SIZE, null);
        } else {
            g2.setColor(Color.MAGENTA);
            g2.fillRect(drawX, drawY, SIZE, SIZE);
        }

        g2.dispose();
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public Color getColor() { return color; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }

    public void update() {
        glowPhase += GLOW_SPEED;
        if (glowPhase > Math.PI * 2) glowPhase -= Math.PI * 2;
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
