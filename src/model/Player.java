package model;

/**
 * The player's health and score.
 *
 * ENCAPSULATION: the fields are private. Other classes cannot write
 * "player.score = 9999"; they must go through addPoints() and loseHealth(),
 * which protect the rules (no negative points, health never below zero).
 */
public class Player {

    private final int maxHealth;
    private int health;
    private int score;

    public Player(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
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

    /** OVERLOADING: the same name with no argument means "lose one health". */
    public void loseHealth() {
        loseHealth(1);
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public int getScore() {
        return score;
    }

    @Override
    public String toString() {
        return "Player(health " + health + "/" + maxHealth + ", score " + score + ")";
    }
}
