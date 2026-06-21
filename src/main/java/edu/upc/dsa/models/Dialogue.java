package edu.upc.dsa.models;

public class Dialogue {
    private int id;
    private String npcId;
    private Integer missionId;
    private Integer objectiveId;
    private String triggerCondition;
    private int sequenceOrder;
    private String text;

    public Dialogue() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNpcId() { return npcId; }
    public void setNpcId(String npcId) { this.npcId = npcId; }

    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }

    public Integer getObjectiveId() { return objectiveId; }
    public void setObjectiveId(Integer objectiveId) { this.objectiveId = objectiveId; }

    public String getTriggerCondition() { return triggerCondition; }
    public void setTriggerCondition(String triggerCondition) { this.triggerCondition = triggerCondition; }

    public int getSequenceOrder() { return sequenceOrder; }
    public void setSequenceOrder(int sequenceOrder) { this.sequenceOrder = sequenceOrder; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
