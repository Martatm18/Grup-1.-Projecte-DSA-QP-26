package edu.upc.dsa.models;

import java.util.List;

public class ObjectiveResult {
    private boolean missionCompleted;
    private Integer nextObjectiveId;
    private String nextObjectiveTitle;
    private Integer nextMissionId;
    private String nextMissionTitle;
    private List<String> rewardObjects;
    private int rewardEcts;

    public ObjectiveResult() {}

    public boolean isMissionCompleted() { return missionCompleted; }
    public void setMissionCompleted(boolean missionCompleted) { this.missionCompleted = missionCompleted; }

    public Integer getNextObjectiveId() { return nextObjectiveId; }
    public void setNextObjectiveId(Integer nextObjectiveId) { this.nextObjectiveId = nextObjectiveId; }

    public String getNextObjectiveTitle() { return nextObjectiveTitle; }
    public void setNextObjectiveTitle(String nextObjectiveTitle) { this.nextObjectiveTitle = nextObjectiveTitle; }

    public Integer getNextMissionId() { return nextMissionId; }
    public void setNextMissionId(Integer nextMissionId) { this.nextMissionId = nextMissionId; }

    public String getNextMissionTitle() { return nextMissionTitle; }
    public void setNextMissionTitle(String nextMissionTitle) { this.nextMissionTitle = nextMissionTitle; }

    public List<String> getRewardObjects() { return rewardObjects; }
    public void setRewardObjects(List<String> rewardObjects) { this.rewardObjects = rewardObjects; }

    public int getRewardEcts() { return rewardEcts; }
    public void setRewardEcts(int rewardEcts) { this.rewardEcts = rewardEcts; }
}
