package main;

import java.awt.*;
import java.util.*;

public class Ghost {
    public double x, y;
    private double speed = 1.8; // smooth following speed
    private int size = 20;
    private Color color = Color.RED;
    private Maze maze;

    private double vx = 0, vy = 0;
    private double steerStrength = 0.1;
    private int safeDistance = 1;

    private ArrayList<Point> path = new ArrayList<>();
    private int pathIndex = 0;
    private long lastPathTime = 0;
    private static final int PATH_UPDATE_INTERVAL = 400; // ms

    public Ghost(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update(Player player) {
        long now = System.currentTimeMillis();

        int tileSize = maze.tileSize;
        int ghostRow = (int) ((y + size / 2) / tileSize);
        int ghostCol = (int) ((x + size / 2) / tileSize);
        int playerRow = (player.y + player.size / 2) / tileSize;
        int playerCol = (player.x + player.size / 2) / tileSize;

        // Recalculate path occasionally or if lost
        if (path.isEmpty() || pathIndex >= path.size() || now - lastPathTime > PATH_UPDATE_INTERVAL) {
            computePath(ghostRow, ghostCol, playerRow, playerCol);
            pathIndex = 0;
            lastPathTime = now;
        }

        // Follow the path smoothly
        if (!path.isEmpty() && pathIndex < path.size()) {
            Point next = path.get(pathIndex);
            double targetX = next.x * tileSize + (tileSize - size) / 2.0;
            double targetY = next.y * tileSize + (tileSize - size) / 2.0;

            double dx = targetX - x;
            double dy = targetY - y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < 2) {
                pathIndex++;
            } else {
                // Smooth steering toward next path node
                dx /= dist;
                dy /= dist;
                vx += (dx * speed - vx) * steerStrength;
                vy += (dy * speed - vy) * steerStrength;

                double newX = x + vx;
                double newY = y + vy;

                // Move with collision checking
                if (!isColliding(newX, y)) x = newX;
                else vx = 0;
                if (!isColliding(x, newY)) y = newY;
                else vy = 0;
            }
        }
    }

    private void computePath(int startRow, int startCol, int goalRow, int goalCol) {
        int rows = maze.getRows();
        int cols = maze.getCols();

        boolean[][] visited = new boolean[rows][cols];
        Map<Point, Point> parent = new HashMap<>();
        Queue<Point> queue = new LinkedList<>();

        Point start = new Point(startCol, startRow);
        Point goal = new Point(goalCol, goalRow);

        queue.add(start);
        visited[startRow][startCol] = true;

        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        while (!queue.isEmpty()) {
            Point current = queue.poll();
            if (current.equals(goal)) break;

            for (int[] d : dirs) {
                int nr = current.y + d[1];
                int nc = current.x + d[0];

                if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                if (visited[nr][nc] || maze.isWallTile(nr, nc)) continue;

                visited[nr][nc] = true;
                Point next = new Point(nc, nr);
                parent.put(next, current);
                queue.add(next);
            }
        }

        // Reconstruct path
        path.clear();
        Point cur = goal;
        while (parent.containsKey(cur)) {
            path.add(0, cur);
            cur = parent.get(cur);
        }
    }

    private boolean isColliding(double newX, double newY) {
        int s = size;
        int margin = safeDistance;
        int left = (int) (newX + margin);
        int right = (int) (newX + s - 1 - margin);
        int top = (int) (newY + margin);
        int bottom = (int) (newY + s - 1 - margin);

        for (int row = top / maze.tileSize; row <= bottom / maze.tileSize; row++) {
            for (int col = left / maze.tileSize; col <= right / maze.tileSize; col++) {
                if (maze.isWallTile(row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval((int) x, (int) y, size, size);
    }

    public boolean collidesWith(Player p) {
        Rectangle ghostRect = new Rectangle((int) x, (int) y, size, size);
        Rectangle playerRect = new Rectangle(p.x, p.y, p.size, p.size);
        return ghostRect.intersects(playerRect);
    }
}
