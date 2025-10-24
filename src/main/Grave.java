package main;
import java.awt.*;
import java.util.*;
import java.util.List;

public class Grave {
	private static final int SIZE = 50;
    private static final Color[] COLORS = {
        Color.RED, Color.ORANGE, Color.YELLOW, 
        Color.BLUE, Color.GREEN, new Color(128, 0, 128) // purple
    };

    int x;
	int y;
    private Color color;

    public Grave(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillRoundRect(x, y, SIZE, SIZE, 10, 10);
        g.setColor(Color.BLACK);
        g.drawRoundRect(x, y, SIZE, SIZE, 10, 10);
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

        for (int i = 0; i < graveTiles.size() && graves.size() < count; i++) {
            Point p = graveTiles.get(i);
            int col = p.x / maze.tileSize;
            int row = p.y / maze.tileSize;

            // Skip invalid spots
            if (maze.isWall(row, col)) continue;

            // Check adjacency — ensure no existing grave is within 1 tile (8-neighbor safe zone)
            boolean tooClose = false;
            for (Grave g : graves) {
                int gr = g.y / maze.tileSize;
                int gc = g.x / maze.tileSize;

                int dr = Math.abs(row - gr);
                int dc = Math.abs(col - gc);

                if (dr <= 1 && dc <= 1) { // too close
                    tooClose = true;
                    break;
                }
            }

            if (tooClose) continue;

            // Choose a random color from pool
            Color color = COLORS[rand.nextInt(COLORS.length)];
            graves.add(new Grave(p.x, p.y, color));
        }

        return graves;
    }
    
    public Color getColor() {
        return color;
    }
}
