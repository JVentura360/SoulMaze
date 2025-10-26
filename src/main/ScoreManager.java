package main;

import java.io.*;
import java.util.*;

public class ScoreManager {
    private static final String SCORES_FILE = "scores.txt";
    
    public static class PlayerScore implements Comparable<PlayerScore> {
        public String name;
        public int score;
        
        public PlayerScore(String name, int score) {
            this.name = name;
            this.score = score;
        }
        
        @Override
        public int compareTo(PlayerScore other) {
            // Sort by score in descending order (highest first)
            return Integer.compare(other.score, this.score);
        }
        
        @Override
        public String toString() {
            return name + ":" + score;
        }
    }
    
    public static void saveScore(String playerName, int score) {
        try {
            // Read existing scores
            List<PlayerScore> scores = loadScores();
            
            // Add new score
            scores.add(new PlayerScore(playerName, score));
            
            // Sort by score (highest first)
            Collections.sort(scores);
            
            // Write back to file
            try (PrintWriter writer = new PrintWriter(new FileWriter(SCORES_FILE))) {
                for (PlayerScore playerScore : scores) {
                    writer.println(playerScore.toString());
                }
            }
            
            System.out.println("Score saved: " + playerName + " - " + score);
        } catch (IOException e) {
            System.err.println("Error saving score: " + e.getMessage());
        }
    }
    
    public static List<PlayerScore> loadScores() {
        List<PlayerScore> scores = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(SCORES_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    try {
                        String name = parts[0];
                        int score = Integer.parseInt(parts[1]);
                        scores.add(new PlayerScore(name, score));
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid score format: " + line);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // File doesn't exist yet, return empty list
            System.out.println("Scores file not found, starting fresh");
        } catch (IOException e) {
            System.err.println("Error loading scores: " + e.getMessage());
        }
        
        // Sort by score (highest first)
        Collections.sort(scores);
        return scores;
    }
    
    public static List<PlayerScore> getTopScores(int count) {
        List<PlayerScore> allScores = loadScores();
        return allScores.subList(0, Math.min(count, allScores.size()));
    }
}
