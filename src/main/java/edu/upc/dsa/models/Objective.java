package edu.upc.dsa.models;

public class Objective {
    private Integer id;
    private Integer missionId;
    private String title;
    private String description;
    private int objectiveOrder;
    private String type;
    private String reference;
    private int reward;

    public Objective() {}

    public Objective(Integer id, Integer missionId, String title, String description, int objectiveOrder, String type, String reference, int reward) {
        this.id = id;
        this.missionId = missionId;
        this.title = title;
        this.description = description;
        this.objectiveOrder = objectiveOrder;
        this.type = type;
        this.reference = reference;
        this.reward = reward;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMissionId() {
        return missionId;
    }

    public void setMissionId(Integer missionId) {
        this.missionId = missionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getObjectiveOrder() {
        return objectiveOrder;
    }

    public void setObjectiveOrder(int objectiveOrder) {
        this.objectiveOrder = objectiveOrder;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public int getReward() {
        return reward;
    }

    public void setReward(int reward) {
        this.reward = reward;
    }
}
