package main;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JPanel;

import java.util.List;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Handles jumpscare visuals and sound effects.
 * Supports multiple jumpscare types and ambient atmosphere sounds.
 */
public class Jumpscare {
    private AudioManager audioManager;

    // Active state
    private boolean active = false;
    private long startTime;
    private int durationMs;
    private BufferedImage currentImage;
    private String currentSoundKey;

    // Jumpscare assets
    private Map<String, BufferedImage[]> imageSets = new HashMap<>();
    private Map<String, String> soundMap = new HashMap<>();

    // Atmosphere SFX list
    private List<String> atmosphereKeys = Arrays.asList("riser1", "riser2", "whisper");

    private Random random = new Random();

    public Jumpscare(AudioManager audioManager, int durationMs) {
        this.audioManager = audioManager;
        this.durationMs = durationMs;
        loadAssets();
    }
    
 // --- new fields ---
    private JButton dismissButton;
    private Random rand = new Random();
    private JPanel parentPanel;
    private Runnable onFinish;

    /** Loads all jumpscare image and sound assets. */
    private void loadAssets() {
        try {
            // === GIRL jumpscare ===
            BufferedImage girl1 = ImageIO.read(new File("src/assets/Images/girl1.png"));
            BufferedImage girl2 = ImageIO.read(new File("src/assets/Images/girl2.png"));
            imageSets.put("girl", new BufferedImage[]{girl1, girl2});
            soundMap.put("girl", "girl");

            // === SKULL jumpscare ===
            BufferedImage skull = ImageIO.read(new File("src/assets/Images/skull.png"));
            imageSets.put("skull", new BufferedImage[]{skull});
            soundMap.put("skull", "skull");

            // === SMILE jumpscare ===
            BufferedImage smile1 = ImageIO.read(new File("src/assets/Images/smile1.png"));
            BufferedImage smile2 = ImageIO.read(new File("src/assets/Images/smile2.png"));
            BufferedImage smile3 = ImageIO.read(new File("src/assets/Images/smile3.png"));
            imageSets.put("smile", new BufferedImage[]{smile1, smile2, smile3});
            soundMap.put("smile", "smile");

            // === Preload atmosphere sounds ===
            audioManager.loadSFX("riser1", "src/assets/Music/riser1.wav");
            audioManager.loadSFX("riser2", "src/assets/Music/riser2.wav");
            audioManager.loadSFX("whisper", "src/assets/Music/whisper.wav");

            // === Preload jumpscare sounds ===
            audioManager.loadSFX("girl", "src/assets/Music/girl.wav");
            audioManager.loadSFX("skull", "src/assets/Music/skull.wav");
            audioManager.loadSFX("smile", "src/assets/Music/smile.wav");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading jumpscare assets: " + e.getMessage());
        }
    }

    /** Triggers a random image from a jumpscare category (girl/skull/smile). */
    public void trigger(String type) {
        if (!imageSets.containsKey(type)) {
            System.err.println("Unknown jumpscare type: " + type);
            return;
        }

        if (active) return; // prevent overlap
        active = true;
        startTime = System.currentTimeMillis();

        // Pick random image from that type
        BufferedImage[] imgs = imageSets.get(type);
        currentImage = imgs[random.nextInt(imgs.length)];
        currentSoundKey = soundMap.get(type);

        // Play sound
        audioManager.playSFX(currentSoundKey, false);
        
     // spawn dismiss button if parent exists
        if (parentPanel != null) {
            createDismissButton(parentPanel.getWidth(), parentPanel.getHeight());
        }
    }

    /** Play a random ambient/atmosphere sound effect (riser/whisper). */
    public void playRandomAtmosphere() {
        String key = atmosphereKeys.get(random.nextInt(atmosphereKeys.size()));
        audioManager.playSFX(key, false);
    }

    /** Draw the jumpscare overlay (called from paintComponent). */
    public void draw(Graphics g, int width, int height) {
        if (!active || currentImage == null) return;

        long elapsed = System.currentTimeMillis() - startTime;
        if (elapsed >= durationMs) {
            end(); // 👈 automatically call end(), which will handle the callback
            return;
        }

        g.drawImage(currentImage, 0, 0, width, height, null);
    }
    
    private void createDismissButton(int panelW, int panelH) {
    	if (dismissButton != null) parentPanel.remove(dismissButton);
        dismissButton = new JButton("Dismiss!");
        int bw = 120, bh = 40;

        int x = rand.nextInt(Math.max(1, panelW - bw));
        int y = rand.nextInt(Math.max(1, panelH - bh));
        dismissButton.setBounds(x, y, bw, bh);

        dismissButton.setFocusPainted(false);
        dismissButton.setBackground(new Color(120, 0, 0));
        dismissButton.setForeground(Color.WHITE);
        dismissButton.setFont(new Font("Chiller", Font.BOLD, 22));

        dismissButton.addActionListener(e -> end());

        // add it on top of the parent panel
        parentPanel.setLayout(null);
        parentPanel.add(dismissButton);
        parentPanel.repaint();
    }

    /** Attach to GamePanel so button can be displayed there */
    public void attachToPanel(JPanel panel) {
        this.parentPanel = panel;
    }

    /** Check if a jumpscare is currently showing. */
    public boolean isActive() {
        return active;
    }

    /** Forcefully stop the jumpscare. */
    public void end() {
        active = false;
        if (parentPanel != null && dismissButton != null) {
            parentPanel.remove(dismissButton);
            parentPanel.repaint();
            dismissButton = null;
        }
     // 🔔 Notify listener when jumpscare ends
        if (onFinish != null) {
            onFinish.run();
            onFinish = null; // prevent it from firing twice
        }
    }
    
    public void setOnFinish(Runnable onFinish) {
        this.onFinish = onFinish;
    }
}
