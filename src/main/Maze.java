package main;
import java.awt.*;

public class Maze {
    public int tileSize = 10; // each tile = 32 pixels

    // Use 'X' for walls and ' ' (space) for paths
    String[] mazeData = {
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "X                                                          XX                                                          X",
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
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X       X               X       X       XXXXXXXX        XXXXXXXX       X       X               X       X       X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X               X                      X               X               X               X",
        "X               X               X               X                      X               X               X               X",
        "X               X               X               X                      X               X               X               X",
        "XXXXXXXXXXXXXXXXXXX       XXXXXXX               X          XX          X               XXXXXXX       XXXXXXXXXXXXXXXXXXX",
        "XXXXXXXXXXXXXXXXXXX       XXXXXXX               X          XX          X               XXXXXXX       XXXXXXXXXXXXXXXXXXX",
        "X               X               X               X                      X               X               X               X",
        "X               X               X               X                      X               X               X               X",
        "X               X               X               X                      X               X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X               X               X       X       X                      X       X       X               X               X",
        "X       X       X               X       X       XXXXXXXX        XXXXXXXX       X       X               X       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
        "X       X                       X       X                                      X       X                       X       X",
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
        "X       XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX       |XXXXXXXXXXXXXXXXXXXXXX       XXXXXXXXXXXXXXXXXXXXXXXXX       X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "X                                                          XX                                                          X",
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
    };

    // Draw walls
    public void draw(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        for (int r = 0; r < mazeData.length; r++) {
            for (int c = 0; c < mazeData[r].length(); c++) {
                if (mazeData[r].charAt(c) == 'X') {
                    g.fillRect(c * tileSize, r * tileSize, tileSize, tileSize);
                }
            }
        }
    }

    // Check wall by pixel position (more strict)
    public boolean isWall(int x, int y, int width, int height) {
        // Treat out-of-bounds as walls
        if (x < 0 || y < 0 || x + width >= mazeData[0].length() * tileSize || y + height >= mazeData.length * tileSize) {
            return true;
        }

        // Check all 4 corners of the bounding box
        int leftCol = x / tileSize;
        int rightCol = (x + width - 1) / tileSize;
        int topRow = y / tileSize;
        int bottomRow = (y + height - 1) / tileSize;

        for (int r = topRow; r <= bottomRow; r++) {
            for (int c = leftCol; c <= rightCol; c++) {
                if (r < 0 || c < 0 || r >= mazeData.length || c >= mazeData[r].length()) return true;
                if (mazeData[r].charAt(c) == 'X') return true;
            }
        }

        return false;
    }

    // Check wall by tile index
    public boolean isWallTile(int row, int col) {
        if (row < 0 || col < 0 || row >= mazeData.length || col >= mazeData[row].length()) {
            return true;
        }
        return mazeData[row].charAt(col) == 'X';
    }

    public int getRows() { return mazeData.length; }
    public int getCols() { return mazeData[0].length(); }
}
