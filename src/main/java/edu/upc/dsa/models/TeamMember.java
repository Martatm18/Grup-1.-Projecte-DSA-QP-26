package edu.upc.dsa.models;

public class TeamMember
{
    private String name;
    private String avatar;
    private int points;

    public TeamMember() {}

    public TeamMember(String name, String avatar, int points)
    {
        this.name = name;
        this.avatar = avatar;
        this.points = points;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAvatar()
    {
        return avatar;
    }

    public void setAvatar(String avatar)
    {
        this.avatar = avatar;
    }

    public int getPoints()
    {
        return points;
    }

    public void setPoints(int points)
    {
        this.points = points;
    }
}
