package edu.upc.dsa.models;

public class UserGameState {
    private String username;
    private int health;
    private int maxHealth;
    private Integer currentMissionId;
    private Integer currentObjectiveId;
    private String currentMissionTitle;
    private String currentObjectiveTitle;

    public UserGameState() {}

    public UserGameState(String username) {
        this.username = username;
        this.health = 100;
        this.maxHealth = 100;
        this.currentMissionId = 1;
        this.currentObjectiveId = 1;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }

    public Integer getCurrentMissionId() {
        return currentMissionId;
    }

    public void setCurrentMissionId(Integer currentMissionId) {
        this.currentMissionId = currentMissionId;
    }

    public Integer getCurrentObjectiveId() {
        return currentObjectiveId;
    }

    public void setCurrentObjectiveId(Integer currentObjectiveId) {
        this.currentObjectiveId = currentObjectiveId;
    }

    public String getCurrentMissionTitle() {
        return currentMissionTitle;
    }

    public void setCurrentMissionTitle(String currentMissionTitle) {
        this.currentMissionTitle = currentMissionTitle;
    }

    public String getCurrentObjectiveTitle() {
        return currentObjectiveTitle;
    }

    public void setCurrentObjectiveTitle(String currentObjectiveTitle) {
        this.currentObjectiveTitle = currentObjectiveTitle;
    }
}
