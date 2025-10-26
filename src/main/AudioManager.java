package main;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioManager {
	private Clip heartbeatClip;
    private FloatControl volumeControl;
    private FloatControl panControl;

    private long lastBeatTime = 0;
    private double currentSpeed = 1.0;
    private double targetSpeed = 1.0;

    private float currentPan = 0f;
    private float targetPan = 0f;

    private float currentVol = -20f; // dB
    private float targetVol = -20f;

    public AudioManager(String filepath) {
        try {
            AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filepath));
            heartbeatClip = AudioSystem.getClip();
            heartbeatClip.open(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void adjustHeartbeat(double distance, double direction) {
        // --- speed mapping ---
        double minDist = 50.0;
        double maxDist = 700.0;
        double normalized = Math.max(0, Math.min(1, (distance - minDist) / (maxDist - minDist)));
        targetSpeed = 2.0 - normalized; // 2x close → 1x far

        // --- volume mapping ---
        // closer = louder
        float maxVol = 0f;    // full
        float minVol = -25f;  // quiet
        targetVol = (float) (maxVol - (normalized * (maxVol - minVol)));

        // --- panning mapping ---
        targetPan = (float) Math.max(-1f, Math.min(1f, direction));
    }

    public void update(long currentTime) {
        // --- smooth lerp ---
        currentSpeed += (targetSpeed - currentSpeed) * 0.05;
        currentPan += (targetPan - currentPan) * 0.1;
        currentVol += (targetVol - currentVol) * 0.05;

        // --- play heartbeats ---
        long interval = (long) (800 / currentSpeed);
        if (currentTime - lastBeatTime >= interval) {
            playHeartbeat();
            lastBeatTime = currentTime;
        }
    }

    private void playHeartbeat() {
        try {
            if (heartbeatClip == null) return;

            if (heartbeatClip.isRunning()) {
                heartbeatClip.stop();
            }
            heartbeatClip.setFramePosition(0);

            if (heartbeatClip.isControlSupported(FloatControl.Type.PAN))
                panControl = (FloatControl) heartbeatClip.getControl(FloatControl.Type.PAN);
            if (heartbeatClip.isControlSupported(FloatControl.Type.MASTER_GAIN))
                volumeControl = (FloatControl) heartbeatClip.getControl(FloatControl.Type.MASTER_GAIN);

            if (panControl != null) panControl.setValue(currentPan);
            if (volumeControl != null) volumeControl.setValue(currentVol);

            heartbeatClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
