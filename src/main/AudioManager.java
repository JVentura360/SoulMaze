package main;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * AudioManager handles playback of sound effects and music.
 */
public class AudioManager {
    private Clip backgroundClip;
    private FloatControl bgVolumeControl;

    // Store multiple sound effects
    private Map<String, Clip> sfxMap = new HashMap<>();

    // === Load background music ===
    public void loadBackgroundMusic(String filepath) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filepath));
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(ais);

            if (backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                bgVolumeControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
            }
        } catch (Exception e) {
            System.err.println("Error loading background music: " + e.getMessage());
        }
    }

    public void playBackgroundMusic(boolean loop) {
        if (backgroundClip == null) return;
        backgroundClip.stop();
        backgroundClip.setFramePosition(0);
        if (loop) {
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            backgroundClip.start();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundClip != null && backgroundClip.isRunning()) {
            backgroundClip.stop();
        }
    }

    public void setBackgroundVolume(float volume) {
        // volume in dB, range depends on clip
        if (bgVolumeControl != null) {
            bgVolumeControl.setValue(volume);
        }
    }

    // === Load a sound effect ===
    public void loadSFX(String name, String filepath) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(new File(filepath));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            sfxMap.put(name, clip);
        } catch (Exception e) {
            System.err.println("Error loading SFX " + name + ": " + e.getMessage());
        }
    }

    // === Play a sound effect (one-shot) ===
    public void playSFX(String name, boolean loop) {
        Clip clip = sfxMap.get(name);
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
        else clip.start();
    }

    // === Stop a sound effect ===
    public void stopSFX(String name) {
        Clip clip = sfxMap.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // === Optional: check if SFX is playing ===
    public boolean isSFXPlaying(String name) {
        Clip clip = sfxMap.get(name);
        return clip != null && clip.isRunning();
    }
    
 // Return duration of sound in milliseconds
    public long getSFXDuration(String name) {
        Clip clip = sfxMap.get(name);
        if (clip == null) return 0;
        return (long)((clip.getMicrosecondLength()) / 1000.0);
    }

    // === Cleanup all audio resources ===
    public void cleanup() {
        stopBackgroundMusic();
        if (backgroundClip != null) backgroundClip.close();

        for (Clip clip : sfxMap.values()) {
            if (clip.isRunning()) clip.stop();
            clip.close();
        }
        sfxMap.clear();
    }
    
    public void fadeOutBackgroundMusic(int durationMs) {
        if (backgroundClip == null || bgVolumeControl == null || !backgroundClip.isRunning()) return;

        new Thread(() -> {
            try {
                float startVolume = bgVolumeControl.getValue();
                float minVolume = bgVolumeControl.getMinimum();
                int steps = 50; // number of fade steps
                long sleepTime = durationMs / steps;

                for (int i = 0; i < steps; i++) {
                    float volume = startVolume - (startVolume - minVolume) * ((float)i / steps);
                    bgVolumeControl.setValue(volume);
                    Thread.sleep(sleepTime);
                }

                // Ensure volume is minimum and stop playback
                bgVolumeControl.setValue(minVolume);
                backgroundClip.stop();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    public void fadeInBackgroundMusic(int durationMs, boolean loop) {
        if (backgroundClip == null || bgVolumeControl == null) return;

        new Thread(() -> {
            try {
                float minVolume = bgVolumeControl.getMinimum();
                float maxVolume = 0f; // typical "normal" dB volume
                int steps = 50; // number of steps in the fade
                long sleepTime = durationMs / steps;

                // Start from minimum volume
                bgVolumeControl.setValue(minVolume);
                backgroundClip.stop();
                backgroundClip.setFramePosition(0);

                if (loop) {
                    backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    backgroundClip.start();
                }

                for (int i = 0; i <= steps; i++) {
                    float volume = minVolume + (maxVolume - minVolume) * ((float)i / steps);
                    bgVolumeControl.setValue(volume);
                    Thread.sleep(sleepTime);
                }

                // Ensure final volume is max
                bgVolumeControl.setValue(maxVolume);

            } catch (InterruptedException e) {
            e.printStackTrace();
        }
        }).start();
}

    
}