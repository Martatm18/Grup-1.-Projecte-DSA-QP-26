package edu.upc.dsa.models;

public class UseItemResult {
    private String used;
    private String effect;
    private Integer health;
    private boolean objectiveCompleted;
    private ObjectiveResult objectiveProgress;

    public String getUsed() { return used; }
    public void setUsed(String used) { this.used = used; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public Integer getHealth() { return health; }
    public void setHealth(Integer health) { this.health = health; }

    public boolean isObjectiveCompleted() { return objectiveCompleted; }
    public void setObjectiveCompleted(boolean objectiveCompleted) { this.objectiveCompleted = objectiveCompleted; }

    public ObjectiveResult getObjectiveProgress() { return objectiveProgress; }
    public void setObjectiveProgress(ObjectiveResult objectiveProgress) { this.objectiveProgress = objectiveProgress; }
}
