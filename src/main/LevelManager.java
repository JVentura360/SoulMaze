package main;

import java.util.List;
import java.util.ArrayList;

/**
 * Manages level progression, ghost count, and soul/grave generation
 * Levels 1-6: 1-6 souls/graves, 1 ghost
 * Levels 7-12: 1-6 souls/graves, 2 ghosts  
 * Levels 13-18: 1-6 souls/graves, 3 ghosts
 */
public class LevelManager {
    private int currentLevel = 1;
    private int maxLevel = 18;
    private int score = 0;
    
    public LevelManager() {
        // Start at level 1
    }
    
    /**
     * Get the number of souls/graves for current level (1-6)
     */
    public int getSoulsPerLevel() {
        return ((currentLevel - 1) % 6) + 1;
    }
    
    /**
     * Get the number of ghosts for current level
     */
    public int getGhostCount() {
        if (currentLevel <= 6) {
            return 1;
        } else if (currentLevel <= 12) {
            return 2;
        } else {
            return 3;
        }
    }
    
    /**
     * Get current level number
     */
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    /**
     * Get current score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Add points to score
     */
    public void addScore(int points) {
        score += points;
    }
    
    /**
     * Check if level is completed (all graves cleared)
     */
    public boolean isLevelCompleted(List<Grave> graves) {
        return graves.isEmpty();
    }
    
    /**
     * Advance to next level
     */
    public void nextLevel() {
        if (currentLevel < maxLevel) {
            currentLevel++;
        }
    }
    
    /**
     * Check if game is completed (all levels done)
     */
    public boolean isGameCompleted() {
        return currentLevel > maxLevel;
    }
    
    /**
     * Reset to level 1 (for restart)
     */
    public void reset() {
        currentLevel = 1;
        score = 0;
    }
    
    /**
     * Get level description for UI
     */
    public String getLevelDescription() {
        int souls = getSoulsPerLevel();
        int ghosts = getGhostCount();
        return String.format("Level %d: %d Souls, %d Ghosts", currentLevel, souls, ghosts);
    }
}
