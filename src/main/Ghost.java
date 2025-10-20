package main;
import java.awt.Color;
import java.awt.Graphics;
import java.util.*;


public class Ghost {
    public int x, y;
    public int speed = 2;
    public int size = 25;
    private Color color = Color.RED;
    private Maze maze;

    // path (tile coordinates)
    private int[] pathRow, pathCol;
    private int pathIndex = 0;
    private long lastPathUpdate = 0;
    private Random rnd = new Random();

    public Ghost(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update(Player player) {
        int tileSize = maze.tileSize;

        int ghostRow = (y + size/2) / tileSize; // use center for tile mapping
        int ghostCol = (x + size/2) / tileSize;
        int playerRow = (player.y + player.size/2) / tileSize;
        int playerCol = (player.x + player.size/2) / tileSize;

        // Only recompute path when needed (or player moved to another tile)
        boolean needRecalc = false;
        if (pathRow == null) needRecalc = true;
        else if (pathIndex >= pathRow.length) needRecalc = true;
        else {
            // if player moved to a new tile, recompute
            int currentGoalRow = pathRow[pathRow.length-1];
            int currentGoalCol = pathCol[pathCol.length-1];
            if (currentGoalRow != playerRow || currentGoalCol != playerCol) needRecalc = true;
        }

        if (System.currentTimeMillis() - lastPathUpdate > 250) needRecalc = true;

        if (needRecalc) {
            computePath(ghostRow, ghostCol, playerRow, playerCol);
            lastPathUpdate = System.currentTimeMillis();
        }

        // follow path if exists
        if (pathRow != null && pathIndex < pathRow.length) {
            int targetTileRow = pathRow[pathIndex];
            int targetTileCol = pathCol[pathIndex];
            int targetX = targetTileCol * tileSize + (tileSize - size)/2; // center ghost in tile
            int targetY = targetTileRow * tileSize + (tileSize - size)/2;

            // move axis separately for smoothness
            if (Math.abs(targetX - x) <= speed) {
                x = targetX;
            } else if (x < targetX && !isColliding(x + speed, y)) {
                x += speed;
            } else if (x > targetX && !isColliding(x - speed, y)) {
                x -= speed;
            }

            if (Math.abs(targetY - y) <= speed) {
                y = targetY;
            } else if (y < targetY && !isColliding(x, y + speed)) {
                y += speed;
            } else if (y > targetY && !isColliding(x, y - speed)) {
                y -= speed;
            }

            // if close to tile center, advance to next path node
            if (Math.abs(targetX - x) <= 1 && Math.abs(targetY - y) <= 1) {
                pathIndex++;
            }
        } else {
            // No path found: small random wandering (prevents being stuck)
            int dir = rnd.nextInt(4);
            int nx = x, ny = y;
            if (dir == 0) nx += speed;
            if (dir == 1) nx -= speed;
            if (dir == 2) ny += speed;
            if (dir == 3) ny -= speed;
            if (!isColliding(nx, ny)) { x = nx; y = ny; }
        }
    }

    // BFS pathfinder using tile coords
    private void computePath(int startRow, int startCol, int goalRow, int goalCol) {
        int rows = maze.mazeData.length;
        int cols = maze.mazeData[0].length;

        // guard: invalid start/goal
        if (startRow < 0 || startCol < 0 || startRow >= rows || startCol >= cols) {
            pathRow = null; pathCol = null; pathIndex = 0; return;
        }
        if (goalRow < 0 || goalCol < 0 || goalRow >= rows || goalCol >= cols) {
            pathRow = null; pathCol = null; pathIndex = 0; return;
        }
        if (maze.isWallTile(startRow, startCol) || maze.isWallTile(goalRow, goalCol)) {
            pathRow = null; pathCol = null; pathIndex = 0; return;
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

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        boolean found = false;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];
            if (r == goalRow && c == goalCol) { found = true; break; }
            // neighbors in random order for variety
            Integer[] order = {0,1,2,3};
            Collections.shuffle(Arrays.asList(order), rnd);
            for (int k = 0; k < 4; k++) {
                int i = order[k];
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (nr < 0 || nc < 0 || nr >= rows || nc >= cols) continue;
                if (visited[nr][nc]) continue;
                if (maze.isWallTile(nr, nc)) continue;
                visited[nr][nc] = true;
                prevR[nr][nc] = r;
                prevC[nr][nc] = c;
                q.add(new int[]{nr, nc});
            }
        }

        if (!found) {
            // no path
            pathRow = null; pathCol = null; pathIndex = 0;
            return;
        }

        // reconstruct
        List<Integer> pr = new ArrayList<>();
        List<Integer> pc = new ArrayList<>();
        int r = goalRow, c = goalCol;
        while (!(r == startRow && c == startCol)) {
            pr.add(r); pc.add(c);
            int tr = prevR[r][c];
            int tc = prevC[r][c];
            r = tr; c = tc;
            if (r == -1 || c == -1) break;
        }
        // add start tile at the front
        pr.add(startRow); pc.add(startCol);

        Collections.reverse(pr);
        Collections.reverse(pc);
        pathRow = pr.stream().mapToInt(i -> i).toArray();
        pathCol = pc.stream().mapToInt(i -> i).toArray();

        // start following from next tile (so ghost doesn't target its own tile)
        pathIndex = Math.min(1, pathRow.length);
    }

    // 4-corner pixel collision
    private boolean isColliding(int newX, int newY) {
        return maze.isWall(newX, newY) ||
               maze.isWall(newX + size - 1, newY) ||
               maze.isWall(newX, newY + size - 1) ||
               maze.isWall(newX + size - 1, newY + size - 1);
    }

    public void draw(Graphics g) {
        g.setColor(color);
        g.fillOval(x, y, size, size);
    }

    public boolean collidesWith(Player p) {
        return Math.abs(p.x - x) < size && Math.abs(p.y - y) < size;
    }
}
