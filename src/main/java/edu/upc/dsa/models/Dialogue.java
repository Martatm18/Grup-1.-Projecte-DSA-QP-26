package edu.upc.dsa.models;

public class Dialogue {
    private Integer id;
    private String npcId;
    private Integer missionId;
    private Integer objectiveId;
    private String triggerCondition;
    private Integer sequenceOrder;
    private String text;

    public Dialogue() {}

    public Dialogue(Integer id, String npcId, Integer missionId, Integer objectiveId,
                    String triggerCondition, Integer sequenceOrder, String text) {
        this.id = id;
        this.npcId = npcId;
        this.missionId = missionId;
        this.objectiveId = objectiveId;
        this.triggerCondition = triggerCondition;
        this.sequenceOrder = sequenceOrder;
        this.text = text;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNpcId() {
        return npcId;
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }

    public Integer getMissionId() {
        return missionId;
    }

    public void setMissionId(Integer missionId) {
        this.missionId = missionId;
    }

    public Integer getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(Integer objectiveId) {
        this.objectiveId = objectiveId;
    }

    public String getTriggerCondition() {
        return triggerCondition;
    }

    public void setTriggerCondition(String triggerCondition) {
        this.triggerCondition = triggerCondition;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
