package main;

import javax.sound.sampled.*;
import java.io.File;

public class AudioManager {
    private Clip heartbeatClip;
    private FloatControl volumeControl;
    private FloatControl panControl;

    private long lastBeatTime = 0;
    private double currentSpeed = 1.0;
    private double targetSpeed = 1.0;

    private float currentPan = 0f;
    private float targetPan = 0f;

    private float currentVol = -10f; // start louder
    private float targetVol = -10f;

    // loop start/end points (in seconds)
    private static final double LOOP_START_SEC = 4.0;
    private static final double LOOP_END_SEC = 60.0;

    public AudioManager(String filepath) {
        try {
            File audioFile = new File(filepath);
            if (!audioFile.exists()) {
                System.err.println("Heartbeat file not found: " + filepath);
                return;
            }

            AudioInputStream stream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = stream.getFormat();

            // Convert to PCM format if needed
            if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
                );
                stream = AudioSystem.getAudioInputStream(targetFormat, stream);
                format = targetFormat;
                System.out.println("Converted audio format to PCM");
            }

            heartbeatClip = AudioSystem.getClip();
            heartbeatClip.open(stream);

            // Set loop points for 0:04 – 1:00 segment
            int startFrame = (int) (LOOP_START_SEC * format.getFrameRate());
            int endFrame = (int) (LOOP_END_SEC * format.getFrameRate());
            heartbeatClip.setLoopPoints(startFrame, endFrame);
            heartbeatClip.loop(Clip.LOOP_CONTINUOUSLY);

            // Prepare controls
            if (heartbeatClip.isControlSupported(FloatControl.Type.MASTER_GAIN))
                volumeControl = (FloatControl) heartbeatClip.getControl(FloatControl.Type.MASTER_GAIN);
            if (heartbeatClip.isControlSupported(FloatControl.Type.PAN))
                panControl = (FloatControl) heartbeatClip.getControl(FloatControl.Type.PAN);

            // Start softly
            if (volumeControl != null) volumeControl.setValue(currentVol);
            if (panControl != null) panControl.setValue(currentPan);

            heartbeatClip.start();
            System.out.println("Heartbeat loaded and looping (0:04–1:00) from: " + filepath);

        } catch (Exception e) {
            System.err.println("Failed to load heartbeat audio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adjust heartbeat intensity based on ghost distance and direction.
     */
    public void adjustHeartbeat(double distance, double direction) {
        // Distance range mapping
        double minDist = 50.0;
        double maxDist = 700.0;
        double normalized = Math.max(0, Math.min(1, (distance - minDist) / (maxDist - minDist)));

        // --- Speed mapping ---
        targetSpeed = 3.0 - (normalized * 2.0); // Faster when closer

        // --- Volume mapping ---
        float maxVol = 6f;   // louder
        float minVol = -25f; // quieter
        targetVol = (float) (maxVol - (normalized * (maxVol - minVol)));

        // --- Panning mapping ---
        targetPan = (float) Math.max(-1f, Math.min(1f, direction));
    }

    /**
     * Smoothly updates the volume/pan/speed transitions.
     */
    public void update(long currentTime) {
        currentSpeed += (targetSpeed - currentSpeed) * 0.1;
        currentPan += (targetPan - currentPan) * 0.15;
        currentVol += (targetVol - currentVol) * 0.1;

        if (heartbeatClip != null && heartbeatClip.isOpen()) {
            if (volumeControl == null && heartbeatClip.isControlSupported(FloatControl.Type.MASTER_GAIN))
                volumeControl = (FloatControl) heartbeatClip.getControl(FloatControl.Type.MASTER_GAIN);
            if (panControl == null && heartbeatClip.isControlSupported(FloatControl.Type.PAN))
                panControl = (FloatControl) heartbeatClip.getControl(FloatControl.Type.PAN);

            if (volumeControl != null) volumeControl.setValue(currentVol);
            if (panControl != null) panControl.setValue(currentPan);
        }

        // Heartbeat timing — restart the beat rhythmically
        long interval = (long) (600 / currentSpeed);
        if (currentTime - lastBeatTime >= interval) {
            restartSegment();
            lastBeatTime = currentTime;
        }
    }

    /**
     * Rewinds the clip slightly to maintain looping heartbeat rhythm.
     */
    private void restartSegment() {
        if (heartbeatClip != null && heartbeatClip.isOpen()) {
            int startFrame = heartbeatClip.getLoopStartPoint();
            heartbeatClip.setFramePosition(startFrame);
        }
    }

    public void stop() {
        if (heartbeatClip != null && heartbeatClip.isRunning()) {
            heartbeatClip.stop();
        }
    }

    public void start() {
        if (heartbeatClip != null && !heartbeatClip.isRunning()) {
            heartbeatClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
}
