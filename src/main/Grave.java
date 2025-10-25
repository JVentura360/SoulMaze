package main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Random;
import javax.swing.ImageIcon;

public class Grave {
	private static final int SIZE = 50;
    private static final Map<Color, Image> GRAVE_SPRITES = new HashMap<>();
    static {
        GRAVE_SPRITES.put(Color.BLUE, new ImageIcon("src/assets/Images/BlueGrave.png").getImage());
        GRAVE_SPRITES.put(Color.RED, new ImageIcon("src/assets/Images/RedGrave.png").getImage());
        GRAVE_SPRITES.put(Color.ORANGE, new ImageIcon("src/assets/Images/OrangeGrave.png").getImage());
        GRAVE_SPRITES.put(Color.YELLOW, new ImageIcon("src/assets/Images/YellowGrave.png").getImage());
        GRAVE_SPRITES.put(Color.GREEN, new ImageIcon("src/assets/Images/GreenGrave.png").getImage());
        GRAVE_SPRITES.put(new Color(128, 0, 128), new ImageIcon("src/assets/Images/PurpleGrave.png").getImage());
    }

    int x;
	int y;
    private Color color;

    public Grave(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics g) {
        Image img = GRAVE_SPRITES.getOrDefault(color, GRAVE_SPRITES.get(Color.BLUE));
        if (img != null) {
            g.drawImage(img, x, y, SIZE, SIZE, null);
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE);
    }

    public static List<Grave> generateGraves(Maze maze, int count) {
        List<Point> graveTiles = new ArrayList<>();
        List<Grave> graves = new ArrayList<>();
        Random rand = new Random();

        // Find all 'G' tiles
        for (int r = 0; r < maze.mazeData.length; r++) {
            for (int c = 0; c < maze.mazeData[r].length(); c++) {
                if (maze.mazeData[r].charAt(c) == 'G') {
                    graveTiles.add(new Point(c * maze.tileSize, r * maze.tileSize));
                }
            }
        }

        // Shuffle to randomize selection
        Collections.shuffle(graveTiles, rand);

        // Create a list of available colors for unique assignment
        Color[] COLORS = {
            Color.RED, Color.ORANGE, Color.YELLOW, Color.BLUE, Color.GREEN, new Color(128, 0, 128)
        };
        List<Color> availableColors = new ArrayList<>(Arrays.asList(COLORS));
        Collections.shuffle(availableColors, rand);

        for (int i = 0; i < graveTiles.size() && graves.size() < count; i++) {
            Point p = graveTiles.get(i);
            int col = p.x / maze.tileSize;
            int row = p.y / maze.tileSize;

            // Skip invalid spots
            if (maze.isWall(row, col)) continue;

            // Check adjacency — ensure no existing grave is within 2 tiles (larger safe zone)
            boolean tooClose = false;
            for (Grave g : graves) {
                int gr = g.y / maze.tileSize;
                int gc = g.x / maze.tileSize;

                int dr = Math.abs(row - gr);
                int dc = Math.abs(col - gc);

                if (dr <= 2 && dc <= 2) { // larger safe zone to prevent overlap
                    tooClose = true;
                    break;
                }
            }

            if (tooClose) continue;

            // Use next available unique color
            if (graves.size() < availableColors.size()) {
                Color color = availableColors.get(graves.size());
                graves.add(new Grave(p.x, p.y, color));
            }
        }

        return graves;
    }
    
    public Color getColor() {
        return color;
    }
}
