package edu.upc.dsa.models;

public class Puzzle {
    private Integer id;
    private Integer missionId;
    private Integer objectiveId;
    private String title;
    private String description;
    private String solution;

    public Puzzle() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getMissionId() { return missionId; }
    public void setMissionId(Integer missionId) { this.missionId = missionId; }

    public Integer getObjectiveId() { return objectiveId; }
    public void setObjectiveId(Integer objectiveId) { this.objectiveId = objectiveId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
}
