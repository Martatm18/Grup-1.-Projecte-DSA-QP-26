package edu.upc.dsa.models;

import java.util.List;

public class TeamResponse
{
    private String team;
    private List<TeamMember> members;

    public TeamResponse() {}

    public TeamResponse(String team, List<TeamMember> members)
    {
        this.team = team;
        this.members = members;
    }

    public String getTeam()
    {
        return team;
    }

    public void setTeam(String team)
    {
        this.team = team;
    }

    public List<TeamMember> getMembers()
    {
        return members;
    }

    public void setMembers(List<TeamMember> members)
    {
        this.members = members;
    }
}
