package main;
import java.awt.*;

public class Maze {
	public int tileSize = 20; // each tile = 32 pixels

    // Use 'X' for walls and ' ' (space) for paths
    String[] mazeData = {
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "X                                                          X",
        "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
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

    // Check wall by pixel position
    public boolean isWall(int x, int y) {
        int col = x / tileSize;
        int row = y / tileSize;
        if (row < 0 || col < 0 || row >= mazeData.length || col >= mazeData[row].length()) {
            return true;
        }
        return mazeData[row].charAt(col) == 'X';
    }

    // Check wall by tile index
    public boolean isWallTile(int row, int col) {
        if (row < 0 || col < 0 || row >= mazeData.length || col >= mazeData[row].length()) {
            return true;
        }
        return mazeData[row].charAt(col) == 'X';
    }
}



