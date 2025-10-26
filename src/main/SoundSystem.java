package main;

public class SoundSystem {
    private static final AudioManager heartbeat = new AudioManager("src/assets/Music/Heartbeat.wav");

    public static AudioManager getHeartbeat() {
        return heartbeat;
    }
}
