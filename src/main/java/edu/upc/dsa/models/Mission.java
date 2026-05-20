package edu.upc.dsa.models;

public class Mission {
    private Integer id;
    private String title;
    private String description;
    private int missionOrder;
    private boolean active;

    public Mission() {}

    public Mission(Integer id, String title, String description, int missionOrder, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.missionOrder = missionOrder;
        this.active = active;
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
}
