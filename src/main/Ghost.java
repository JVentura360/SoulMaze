package main;

import java.awt.*;
import java.util.List;
import java.util.*;
import javax.swing.ImageIcon;

public class Ghost {
	public int x, y;
    public double speed = 1.4;
    public int size = 50;
    private Maze maze;

    // Pathfinding
    private int[] pathRow, pathCol;
    private int pathIndex = 0;
    private Random rnd = new Random();

    // track player's last tile to avoid re-pathing on micro-movements
    private int lastPlayerRow = -1, lastPlayerCol = -1;
    
    // Ghost sprite
    private static final Image GHOST_IMG = new ImageIcon("src/assets/Images/Ghost.png").getImage();

    public Ghost(int x, int y, Maze maze) {
        this.x = x;
        this.y = y;
        this.maze = maze;
    }

    public void update(Player player) {
        int tileSize = maze.tileSize;

        // Tile coordinates (center-based) using Math.floor
        int ghostRow = getTileRow(y, size, tileSize);
        int ghostCol = getTileCol(x, size, tileSize);
        int playerRow = getTileCol(player.y, player.size, tileSize);
        int playerCol = getTileCol(player.x, player.size, tileSize);

        boolean needRecalc = false;

        // Recalculate if path is empty or player moved to a different tile
        if (pathRow == null || pathIndex >= pathRow.length) {
            needRecalc = true;
        } else {
            int goalR = pathRow[pathRow.length - 1];
            int goalC = pathCol[pathCol.length - 1];
            if (goalR != playerRow || goalC != playerCol)
                needRecalc = true;
        }

        // Only recompute when player tile changed (stops jitter when player touches walls)
        if (!needRecalc && (playerRow != lastPlayerRow || playerCol != lastPlayerCol)) {
            needRecalc = true;
        }

        if (needRecalc) {
            computePath(ghostRow, ghostCol, playerRow, playerCol);
            lastPlayerRow = playerRow;
            lastPlayerCol = playerCol;
        }

        // Follow path if valid
        if (pathRow != null && pathIndex < pathRow.length) {
            int targetRow = pathRow[pathIndex];
            int targetCol = pathCol[pathIndex];

            // Center target in tile (tile center minus half body so ghost centers)
            int targetX = targetCol * tileSize + tileSize / 2 - size / 2;
            int targetY = targetRow * tileSize + tileSize / 2 - size / 2;

            moveTowards(targetX, targetY);

            // Advance to next node if close enough (don't snap to avoid jitter)
            double dist = Math.hypot(targetX - x, targetY - y);
            if (dist < speed * 1.5) {
                pathIndex++;
            }
        } else {
            // Wander slightly if no path
            int dir = rnd.nextInt(4);
            int nx = x, ny = y;
            if (dir == 0) nx += speed;
            if (dir == 1) nx -= speed;
            if (dir == 2) ny += speed;
            if (dir == 3) ny -= speed;
            if (!isColliding(nx, ny)) {
                x = nx;
                y = ny;
            }
        }
    }

    // Diagonal/vector movement — moves toward target in straight line, collision-checked
    private void moveTowards(int targetX, int targetY) {
        double dx = targetX - x;
        double dy = targetY - y;
        double dist = Math.hypot(dx, dy);
        if (dist == 0) return;

        double nx = (dx / dist) * speed;
        double ny = (dy / dist) * speed;

        int newX = (int) Math.round(x + nx);
        int newY = (int) Math.round(y + ny);

        // If the direct diagonal step collides, try axis-aligned fallback to slide along wall
        if (!isColliding(newX, newY)) {
            x = newX;
            y = newY;
            return;
        }

        // try x only
        if (!isColliding(newX, y)) {
            x = newX;
        }
        // try y only
        if (!isColliding(x, newY)) {
            y = newY;
        }
        // otherwise remain in place (prevents clipping)
    }

    // BFS pathfinding
    private void computePath(int startRow, int startCol, int goalRow, int goalCol) {
        int rows = maze.getRows();
        int cols = maze.getCols();

        if (!isValidTile(startRow, startCol) || !isValidTile(goalRow, goalCol)) {
            pathRow = pathCol = null;
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

        int[] dr = {-1, 1, 0, 0};
        int[] dc = {0, 0, -1, 1};
        boolean found = false;

        while (!q.isEmpty()) {
            int[] cur = q.poll();
            int r = cur[0], c = cur[1];

            if (r == goalRow && c == goalCol) {
                found = true;
                break;
            }

            for (int i = 0; i < 4; i++) {
                int nr = r + dr[i];
                int nc = c + dc[i];
                if (!isValidTile(nr, nc)) continue;
                // keep safe tile check (buffer) — prevents pathing into impossibly narrow spots
                if (visited[nr][nc] || !isSafeTile(nr, nc)) continue;
                visited[nr][nc] = true;
                prevR[nr][nc] = r;
                prevC[nr][nc] = c;
                q.add(new int[]{nr, nc});
            }
        }

        if (!found) {
            pathRow = pathCol = null;
            pathIndex = 0;
            return;
        }

        // Reconstruct path
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
        return r >= 0 && c >= 0 && r < maze.getRows() && c < maze.getCols();
    }

    // collision uses Math.floor-based tile indices to match Player
    private boolean isColliding(int newX, int newY) {
        int left = (int) Math.floor(newX / (double) maze.tileSize);
        int right = (int) Math.floor((newX + size - 1) / (double) maze.tileSize);
        int top = (int) Math.floor(newY / (double) maze.tileSize);
        int bottom = (int) Math.floor((newY + size - 1) / (double) maze.tileSize);

        for (int r = top; r <= bottom; r++) {
            for (int c = left; c <= right; c++) {
                if (!isValidTile(r, c) || maze.isWallTile(r, c)) return true;
            }
        }
        return false;
    }

    // safety check for BFS (buffer helps avoid impossible spots)
    private boolean isSafeTile(int r, int c) {
        if (maze.isWallTile(r, c)) return false;
        int buffer = 3; // your requested buffer
        for (int dr = -buffer; dr <= buffer; dr++) {
            for (int dc = -buffer; dc <= buffer; dc++) {
                int rr = r + dr, cc = c + dc;
                if (!isValidTile(rr, cc)) continue;
                if (maze.isWallTile(rr, cc)) return false;
            }
        }
        return true;
    }
    
    // tile helpers (use floor to avoid rounding mismatch)
    private int getTileRow(int y, int size, int tileSize) {
        return (int) Math.floor((y + size / 2.0) / tileSize);
    }

    private int getTileCol(int x, int size, int tileSize) {
        return (int) Math.floor((x + size / 2.0) / tileSize);
    }

    public boolean collidesWith(Player p) {
        Rectangle ghostRect = new Rectangle(x, y, size, size);
        Rectangle playerRect = new Rectangle(p.x, p.y, p.size, p.size);

        Rectangle intersection = ghostRect.intersection(playerRect);
        // Check if intersection exists and is at least 30px in width or height
        return !intersection.isEmpty() && (intersection.width >= 30 && intersection.height >= 30);
    }

    public void draw(Graphics g) {
        // Draw ghost sprite
        if (GHOST_IMG != null) {
            g.drawImage(GHOST_IMG, x, y, size, size, null);
        } else {
            // Fallback to red rectangle if sprite fails to load
            g.setColor(Color.RED);
            g.fillRect(x, y, size, size);
        }

        // Visualize path
        if (pathRow != null && pathCol != null) {
            g.setColor(Color.CYAN);
            for (int i = 0; i < pathRow.length; i++) {
                int cx = pathCol[i] * maze.tileSize + maze.tileSize / 2;
                int cy = pathRow[i] * maze.tileSize + maze.tileSize / 2;
                g.fillOval(cx - 3, cy - 3, 6, 6);

                if (i > 0) {
                    int px = pathCol[i - 1] * maze.tileSize + maze.tileSize / 2;
                    int py = pathRow[i - 1] * maze.tileSize + maze.tileSize / 2;
                    g.drawLine(px, py, cx, cy);
                }
            }
            // mark current target
            if (pathIndex < pathRow.length) {
                g.setColor(Color.MAGENTA);
                int tx = pathCol[pathIndex] * maze.tileSize + maze.tileSize / 2;
                int ty = pathRow[pathIndex] * maze.tileSize + maze.tileSize / 2;
                g.fillOval(tx - 5, ty - 5, 10, 10);
            }
        }
    }
    
    public static Grave spawnRandom(Maze maze, List<Grave> existingGraves, Color color) {
        Random rand = new Random();
        int rows = maze.getRows();
        int cols = maze.getCols();
        int tileSize = maze.tileSize;

        List<Point> validSpots = new ArrayList<>();

        // Collect all open tiles that are not walls and not near existing graves
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (maze.mazeData[r].charAt(c) != ' ') continue;
                boolean blocked = false;

                // Check 1-tile radius buffer (so no grave is beside another)
                for (Grave g : existingGraves) {
                    int gr = g.y / tileSize;
                    int gc = g.x / tileSize;

                    int dr = Math.abs(r - gr);
                    int dc = Math.abs(c - gc);

                    if (dr <= 1 && dc <= 1) { // any grave in 8-neighbor area blocks spawn
                        blocked = true;
                        break;
                    }
                }

                if (!blocked) {
                    validSpots.add(new Point(c, r));
                }
            }
        }

        if (validSpots.isEmpty()) {
            System.out.println("⚠ No valid spots for grave!");
            return null;
        }

        // pick random valid spot
        Point chosen = validSpots.get(rand.nextInt(validSpots.size()));
        int x = chosen.x * tileSize;
        int y = chosen.y * tileSize;

        return new Grave(x, y, color);
    }
}