package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

/**
 * GamePanel handles the main gameplay loop, drawing, and logic.
 * It manages the maze, player, ghost, and checkpoint/question system.
 */
public class GamePanel extends JPanel implements KeyListener, Runnable {
    private Thread gameThread;
    private boolean running = true;

    private Player player;
    private Ghost ghost;
    private Maze maze;
    private boolean gameOver = false;
    private boolean gameOverShown = false;

    // Checkpoint / Question system
    private final ArrayList<Checkpoint> checkpoints = new ArrayList<>();
    private final Random rnd = new Random();
    private boolean awaitingAnswer = false;
    private Checkpoint activeCheckpoint = null;
    private JDialog activeDialog = null;

    // === Constructor ===
    public GamePanel() {
        setPreferredSize(new Dimension(1200, 780));
        setBackground(Color.black);
        setFocusable(true);
        addKeyListener(this);

        // Initialize core game objects
        maze = new Maze();
        player = new Player(640, 400, maze);
        ghost = new Ghost(40, 150, maze);

        initCheckpoints();

        // Start main game loop
        gameThread = new Thread(this);
        gameThread.start();
    }

    // === Game Loop ===
    @Override
    public void run() {
        while (running) {
            update();
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException ignored) {}
        }
    }

    // === Game Logic ===
    private void update() {
        if (gameOver) return;

        if (!awaitingAnswer) {
            player.update();
            checkCheckpointTrigger();
        }

        ghost.update(player);

        if (ghost.collidesWith(player)) {
            handleGameOver();
        }
    }

    private void handleGameOver() {
        gameOver = true;

        // Close question dialog if open
        if (activeDialog != null) {
            SwingUtilities.invokeLater(() -> {
                try {
                    activeDialog.dispose();
                } catch (Exception ignore) {}
                activeDialog = null;
            });
        }

        awaitingAnswer = false;
        activeCheckpoint = null;

        if (!gameOverShown) {
            gameOverShown = true;
            SwingUtilities.invokeLater(this::showGameOverDialog);
        }
    }

    // === Drawing ===
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        maze.draw(g);
        for (Checkpoint cp : checkpoints) cp.draw(g);
        player.draw(g);
        ghost.draw(g);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", 480, 380);
        }
    }

    // === Input Handling ===
    @Override
    public void keyPressed(KeyEvent e) {
        if (!awaitingAnswer) player.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (!awaitingAnswer) player.keyReleased(e);
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    // === Checkpoint / Question System ===
    private void initCheckpoints() {
        ArrayList<Question> all = readQuestions();
        if (all.isEmpty()) return;

        Collections.shuffle(all, rnd);
        int needed = Math.min(20, all.size());

        // Collect open tiles from the maze
        ArrayList<int[]> openTiles = new ArrayList<>();
        for (int r = 0; r < maze.mazeData.length; r++) {
            for (int c = 0; c < maze.mazeData[0].length(); c++) {
                if (maze.mazeData[r].charAt(c) == ' ')
                    openTiles.add(new int[]{r, c});
            }
        }
        Collections.shuffle(openTiles, rnd);

        int placed = 0;
        int i = 0;
        while (placed < needed && i < openTiles.size()) {
            int[] rc = openTiles.get(i++);
            int tileX = rc[1] * maze.tileSize;
            int tileY = rc[0] * maze.tileSize;

            // Avoid placing checkpoints near the player’s start position
            if (overlaps(tileX, tileY, maze.tileSize, maze.tileSize,
                         player.x, player.y, player.size, player.size))
                continue;

            Question q = all.get(placed);
            checkpoints.add(new Checkpoint(tileX, tileY, maze.tileSize, q));
            placed++;
        }
    }

    private ArrayList<Question> readQuestions() {
        ArrayList<Question> list = readQuestionsFromFile(new File("CheckpointQuestions.txt"));
        if (list.isEmpty()) list = readQuestionsFromFile(new File("src/main/CheckpointQuestions.txt"));
        if (list.isEmpty()) list = readQuestionsFromFile(new File("../CheckpointQuestions.txt"));
        if (list.isEmpty()) list = defaultQuestions();
        return list;
    }

    private ArrayList<Question> readQuestionsFromFile(File file) {
        ArrayList<Question> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                list.add(Question.fromLine(line));
            }
        } catch (IOException ignored) {}
        return list;
    }

    private ArrayList<Question> defaultQuestions() {
        String[] raw = new String[]{
            "1) Capital of France? - Paris",
            "2) Largest planet? - Jupiter",
            "3) Fastest land animal? - Cheetah",
            "4) Longest river? - Nile",
            "5) Tallest mountain? - Everest",
            "6) H2O name? - Water",
            "7) 12 × 12? - 144",
            "8) Pi (2dp)? - 3.14",
            "9) Binary for two? - 10",
            "10) Red planet? - Mars",
            "11) Sun's primary fuel? - Hydrogen",
            "12) Photosynthesis gas release? - Oxygen",
            "13) Photosynthesis gas intake? - Carbon Dioxide",
            "14) Opposite of hot? - Cold",
            "15) Square root of 81? - 9",
            "16) Symbol for gold? - Au",
            "17) DNA shape? - Double Helix",
            "18) Unit of force? - Newton",
            "19) SI unit of current? - Ampere",
            "20) Gravity (m/s^2)? - 9.8",
            "21) Capital of Philippines? - Manila or Maynila",
            "22) National hero (PH)? - Rizal",
            "23) National tree (PH)? - Narra",
            "24) National bird (PH)? - Eagle",
            "25) Largest Philippine island? - Luzon",
            "26) Longest Philippine river? - Cagayan",
            "27) Highest Philippine mountain? - Apo",
            "28) Manila's main river? - Pasig",
            "29) Chocolate Hills island? - Bohol",
            "30) Taal is a? - Volcano"
        };
        ArrayList<Question> list = new ArrayList<>();
        for (String s : raw) list.add(Question.fromLine(s));
        return list;
    }

    private void checkCheckpointTrigger() {
        for (Checkpoint cp : checkpoints) {
            if (cp.solved) continue;
            if (overlaps(player.x, player.y, player.size, player.size,
                         cp.x, cp.y, cp.size, cp.size)) {
                activeCheckpoint = cp;
                awaitingAnswer = true;
                showQuestionDialog(cp);
                break;
            }
        }
    }

    private boolean overlaps(int ax, int ay, int aw, int ah,
                             int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private void showQuestionDialog(Checkpoint cp) {
        final String prompt = cp.question.prompt;
        SwingUtilities.invokeLater(() -> {
            if (activeDialog != null) {
                try { activeDialog.dispose(); } catch (Exception ignore) {}
                activeDialog = null;
            }

            Window parent = SwingUtilities.getWindowAncestor(this);
            JDialog dialog = new JDialog(parent, "Checkpoint", Dialog.ModalityType.APPLICATION_MODAL);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(8, 8));
            JLabel label = new JLabel(prompt);
            JTextField field = new JTextField();
            JLabel feedback = new JLabel(" ");
            feedback.setForeground(Color.RED);
            JButton submit = new JButton("Submit");

            JPanel bottom = new JPanel(new BorderLayout(6, 6));
            bottom.add(field, BorderLayout.CENTER);
            bottom.add(submit, BorderLayout.EAST);

            panel.add(label, BorderLayout.NORTH);
            panel.add(bottom, BorderLayout.CENTER);
            panel.add(feedback, BorderLayout.SOUTH);

            submit.addActionListener(ev -> {
                String input = field.getText().trim();
                if (cp.question.matches(input)) {
                    cp.solved = true;
                    awaitingAnswer = false;
                    activeCheckpoint = null;
                    dialog.dispose();
                    activeDialog = null;
                } else {
                    feedback.setText("Incorrect. Try again.");
                }
            });

            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(parent);

            // Disable ESC
            JRootPane root = dialog.getRootPane();
            InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = root.getActionMap();
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "noop");
            am.put("noop", new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {}
            });
            root.setDefaultButton(submit);

            player.stop();
            activeDialog = dialog;
            dialog.setVisible(true);
        });
    }

    private void showGameOverDialog() {
        Window parent = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(parent, "Game Over", Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel msg = new JLabel("GAME OVER", SwingConstants.CENTER);
        JPanel buttons = new JPanel();
        JButton retry = new JButton("Play Again");
        JButton close = new JButton("Close");

        buttons.add(retry);
        buttons.add(close);
        panel.add(msg, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);

        retry.addActionListener(e -> {
            dialog.dispose();
            resetGame();
        });

        close.addActionListener(e -> {
            try {
                if (parent instanceof Window w) w.dispose();
            } catch (Exception ignore) {}
            System.exit(0);
        });

        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.getRootPane().setDefaultButton(retry);
        dialog.setVisible(true);
    }

    private void resetGame() {
        // reset flags
        gameOver = false;
        gameOverShown = false;
        awaitingAnswer = false;
        activeCheckpoint = null;

        // reset entities
        player.x = 640;
        player.y = 420;
        player.stop();

        // safely reset ghost if method exists

        // regenerate checkpoints
        checkpoints.clear();
        initCheckpoints();

        requestFocusInWindow();
    }
}
