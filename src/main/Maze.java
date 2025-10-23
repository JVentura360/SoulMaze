package main;
import java.awt.*;

public class Maze {
    public int tileSize = 10; // each tile = 32 pixels

    // Use 'X' for walls and ' ' (space) for paths
    public String[] mazeData = {
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
    
}
