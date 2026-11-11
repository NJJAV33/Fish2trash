package model;

/**
 * The player's health and score.
 *
 * ENCAPSULATION: both fields are private. Other classes cannot write
 * "player.score = 9999"; they must go through addPoints() and loseHealth(),
 * which protect the rules (no negative points, health never below zero).
 */
public class Player {

    private int health;
    private int score;

    public Player(int startingHealth) {
        this.health = startingHealth;
        this.score = 0;
    }

    public void addPoints(int points) {
        if (points > 0) {
            score += points;
        }
    }

    public void loseHealth(int amount) {
        if (amount > 0) {
            health = Math.max(0, health - amount);
        }
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getHealth() {
        return health;
    }

    public int getScore() {
        return score;
    }
}
