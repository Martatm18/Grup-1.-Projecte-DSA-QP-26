package edu.upc.dsa.models;

import java.util.ArrayList;
import java.util.List;

public class Mission {
    private Integer id;
    private String title;
    private String description;
    private int missionOrder;
    private boolean active;
    private List<Objective> objectives;

    public Mission() {}

    public Mission(Integer id, String title, String description, int missionOrder, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.missionOrder = missionOrder;
        this.active = active;
        this.objectives = new ArrayList<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public int getMissionOrder() {
        return missionOrder;
    }

    public void setMissionOrder(int missionOrder) {
        this.missionOrder = missionOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<Objective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<Objective> objectives) {
        this.objectives = objectives;
    }
}
