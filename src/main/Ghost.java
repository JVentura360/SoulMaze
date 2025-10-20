package main;
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;


public class Ghost {
	public double x, y; // use double for smooth motion
    public double speed = 2.0;
    public int size = 25;
    private Color color = Color.RED;
    private Maze maze;

    private int[] pathRow, pathCol;
    private int pathIndex = 0;
    private long lastPathUpdate = 0;
    private Random rnd = new Random();

    // smoothing memory
    private double vx = 0, vy = 0;

    public Ghost(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update(Player player) {
        int tileSize = maze.tileSize;

        int ghostRow = (int) ((y + size / 2) / tileSize);
        int ghostCol = (int) ((x + size / 2) / tileSize);
        int playerRow = (player.y + player.size / 2) / tileSize;
        int playerCol = (player.x + player.size / 2) / tileSize;

        boolean needRecalc = false;

        if (pathRow == null || pathIndex >= pathRow.length)
            needRecalc = true;
        else {
            int goalR = pathRow[pathRow.length - 1];
            int goalC = pathCol[pathCol.length - 1];
            if (goalR != playerRow || goalC != playerCol)
                needRecalc = true;
        }

        if (System.currentTimeMillis() - lastPathUpdate > 400)
            needRecalc = true;

        if (needRecalc) {
            computePath(ghostRow, ghostCol, playerRow, playerCol);
            lastPathUpdate = System.currentTimeMillis();
        }

        // smooth following
        if (pathRow != null && pathIndex < pathRow.length) {
            int targetTileRow = pathRow[pathIndex];
            int targetTileCol = pathCol[pathIndex];
            double targetX = targetTileCol * tileSize + (tileSize - size) / 2.0;
            double targetY = targetTileRow * tileSize + (tileSize - size) / 2.0;

            smoothMove(targetX, targetY);

            if (Math.abs(targetX - x) < 2 && Math.abs(targetY - y) < 2)
                pathIndex++;
        } else {
            wander();
        }
    }

    private void smoothMove(double targetX, double targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > 0.1) {
            double nx = dx / dist;
            double ny = dy / dist;

            // smooth velocity (lerp)
            vx = vx * 0.8 + nx * speed * 0.2;
            vy = vy * 0.8 + ny * speed * 0.2;

            double newX = x + vx;
            double newY = y + vy;

            if (!isColliding(newX, newY)) {
                x = newX;
                y = newY;
            }
        }
    }

    private void wander() {
        int dir = rnd.nextInt(8);
        double dx = 0, dy = 0;
        if (dir == 0) dx = 1;
        if (dir == 1) dx = -1;
        if (dir == 2) dy = 1;
        if (dir == 3) dy = -1;
        if (dir == 4) { dx = 1; dy = 1; }
        if (dir == 5) { dx = -1; dy = 1; }
        if (dir == 6) { dx = 1; dy = -1; }
        if (dir == 7) { dx = -1; dy = -1; }

        double newX = x + dx * speed;
        double newY = y + dy * speed;
        if (!isColliding(newX, newY)) {
            x = newX;
            y = newY;
        }
    }

    private void computePath(int startRow, int startCol, int goalRow, int goalCol) {
        int rows = maze.mazeData.length;
        int cols = maze.mazeData[0].length();

        if (!isValidTile(startRow, startCol) || !isValidTile(goalRow, goalCol)) {
            pathRow = null;
            pathCol = null;
            pathIndex = 0;
            return;
        }

        boolean[][] visited = new boolean[rows][cols];
        int[][] prevR = new int[rows][cols];
        int[][] prevC = new int[rows][cols];
        for (int r = 0; r < rows; r++) {
            Arrays.fill(prevR[r], -1);
            Arrays.fill(prevC[r], -1);
        }

        Queue<int[]> q = new ArrayDeque<>();
        q.add(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int[] dr = {-1, 1, 0, 0, -1, -1, 1, 1};
        int[] dc = {0, 0, -1, 1, -1, 1, -1, 1};

        boolean found = false;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];

            if (r == goalRow && c == goalCol) {
                found = true;
                break;
            }

            for (int i = 0; i < 8; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (!isValidTile(nr, nc)) continue;
                if (visited[nr][nc] || maze.isWallTile(nr, nc)) continue;
                visited[nr][nc] = true;
                prevR[nr][nc] = r;
                prevC[nr][nc] = c;
                q.add(new int[]{nr, nc});
            }
        }

        if (!found) {
            pathRow = null;
            pathCol = null;
            pathIndex = 0;
            return;
        }

        List<Integer> pr = new ArrayList<>();
        List<Integer> pc = new ArrayList<>();
        int r = goalRow, c = goalCol;
        while (r != -1 && c != -1) {
            pr.add(r);
            pc.add(c);
            int tr = prevR[r][c];
            int tc = prevC[r][c];
            r = tr;
            c = tc;
        }

        Collections.reverse(pr);
        Collections.reverse(pc);

        pathRow = pr.stream().mapToInt(i -> i).toArray();
        pathCol = pc.stream().mapToInt(i -> i).toArray();
        pathIndex = 1;
    }

    private boolean isValidTile(int r, int c) {
        return r >= 0 && c >= 0 && r < maze.mazeData.length && c < maze.mazeData[0].length();
    }

    private boolean isColliding(double newX, double newY) {
        return maze.isWall((int)newX, (int)newY)
                || maze.isWall((int)(newX + size - 1), (int)newY)
                || maze.isWall((int)newX, (int)(newY + size - 1))
                || maze.isWall((int)(newX + size - 1), (int)(newY + size - 1));
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval((int)x, (int)y, size, size);
    }

    public boolean collidesWith(Player p) {
        return Math.abs(p.x - x) < size && Math.abs(p.y - y) < size;
    }
}
