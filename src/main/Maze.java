package main;
import java.awt.*;
import javax.swing.ImageIcon;

public class Maze {
    public int tileSize = 10; // each tile = 32 pixels
    
    // Ground background sprite
    private static final Image GROUND_IMG = new ImageIcon("src/assets/Images/Ground.png").getImage();

    // Use 'X' for walls and ' ' (space) for paths
    public String[] mazeData = {
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "X E                                                        XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X       XXXXXXXXXXXXXXXXXXXXXXXXX       XXXXXXXXXXXXXXXXXXXXXXX       XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX       X",
        "X       X                       X       X               X             X                                        X       X",
        "X       X                       X       X               X             X                                        X       X",
        "X       X                       X       X               X             X                                        X       X",
        "X       X                       X       X               X             X                                        X       X",
        "X       X                       X       X               X             X                                        X       X",
        "X       X                       X       X               X             X                                        X       X",
        "X       X                       X                                     X                                        X       X",
        "X       X             X         X               X                     X                          X             X       X",
        "X       X             X         X               X                     X                          X             X       X",
        "X       X             X         X               X                     X                          X             X       X",
        "X       X             X         X               X                     X                          X             X       X",
        "X       X             X         X               X                     X                          X             X       X",
        "X       X             X         X               X                     X                          X             X       X",
        "X       X       XXXXXXX         X       XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX       X         XXXXXXX       X       X",
        "X       X                       X       XGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG00000X       X                       X       X",
        "X       X                       X       XGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG00000X       X                       X       X",
        "X       X                       X       XGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG00000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X       X               X       X0000000XXXXXXXX00000000XXXXXXXX0000000X       X               X       X       X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X               X0000000000000000000000X               X               X               X",
        "X               X               X               X00000000P0000000000000X               X               X               X",
        "X               X               X               X0000000000000000000000X               X               X               X",
        "XXXXXXXXXXXXXXXXXXX       XXXXXXX               X0000000000000000000000X               XXXXXXX       XXXXXXXXXXXXXXXXXXX",
        "XXXXXXXXXXXXXXXXXXX       XXXXXXX               X0000000000000000000000X               XXXXXXX       XXXXXXXXXXXXXXXXXXX",
        "X               X               X               X0000000000000000000000X               X               X               X",
        "X               X               X               X0000000000000000000000X               X               X               X",
        "X               X               X               X0000000000000000000000X               X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X               X               X       X0000000X0000000000000000000000X0000000X       X               X               X",
        "X       X       X               X       X0000000XXXXXXXX00000000XXXXXXXX0000000X       X               X       X       X",
        "X       X                       X       XGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG00000X       X                       X       X",
        "X       X                       X       XGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG00000X       X                       X       X",
        "X       X                       X       XGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG00000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X                       X       X00000000000000000000000000000000000000X       X                       X       X",
        "X       X       XXXXXXX         X       XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX       X         XXXXXXX       X       X",
        "X       X             X                          X                     X               X         X             X       X",
        "X       X             X                          X                     X               X         X             X       X",
        "X       X             X                          X                     X               X         X             X       X",
        "X       X             X                          X                     X               X         X             X       X",
        "X       X             X                          X                     X               X         X             X       X",
        "X       X             X                          X                     X               X         X             X       X",
        "X       X                                        X                                     X                       X       X",
        "X       X                                        X             X               X       X                       X       X",
        "X       X                                        X             X               X       X                       X       X",
        "X       X                                        X             X               X       X                       X       X",
        "X       X                                        X             X               X       X                       X       X",
        "X       X                                        X             X               X       X                       X       X",
        "X       X                                        X             X               X       X                       X       X",
        "X       XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX        XXXXXXXXXXXXXXXXXXXXXX       XXXXXXXXXXXXXXXXXXXXXXXXX       X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    };

    public void draw(Graphics g) {
        // Draw ground background as full screen image (1024x804)
        if (GROUND_IMG != null) {
            g.drawImage(GROUND_IMG, 0, 0, 1200,781, null);
        } else {
            // Fallback to black background if sprite fails to load
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 1200,781 );
        }
        
        // Draw walls on top of ground
        g.setColor(Color.DARK_GRAY);
        for (int r = 0; r < mazeData.length; r++) {
            for (int c = 0; c < mazeData[r].length(); c++) {
                if (mazeData[r].charAt(c) == 'X') {
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    public boolean isWall(int row, int col) {
        if (row < 0 || col < 0 || row >= mazeData.length || col >= mazeData[row].length()) return true;
        return mazeData[row].charAt(col) == 'X';
    }

    public boolean isValidTile(int row, int col) {
        return !isWall(row, col);
    }

    public boolean isWallTile(int row, int col) {
        return isWall(row, col);
    }

    public int getRows() { return mazeData.length; }
    public int getCols() { return mazeData[0].length(); }
    
    public Point getPlayerSpawn() {
        for (int r = 0; r < mazeData.length; r++) {
            for (int c = 0; c < mazeData[r].length(); c++) {
                if (mazeData[r].charAt(c) == 'P') {
                    return new Point(c * tileSize, r * tileSize);
                }
            }
        }
        return null; // Default (none found)
    }

    public Point getGhostSpawn() {
        for (int r = 0; r < mazeData.length; r++) {
            for (int c = 0; c < mazeData[r].length(); c++) {
                if (mazeData[r].charAt(c) == 'E') {
                    return new Point(c * tileSize, r * tileSize);
                }
            }
        }
        return null;
    }
    
}


