package edu.upc.dsa.models;

import java.util.List;

public class TeamInfo {
    private String team;
    private List<TeamMember> members;

    public TeamInfo() {}

    public TeamInfo(String team, List<TeamMember> members) {
        this.team = team;
        this.members = members;
    }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public List<TeamMember> getMembers() { return members; }
    public void setMembers(List<TeamMember> members) { this.members = members; }

    public static class TeamMember {
        private String name;
        private String avatar;
        private int points;

        public TeamMember() {}

        public TeamMember(String name, String avatar, int points) {
            this.name = name;
            this.avatar = avatar;
            this.points = points;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }

        public int getPoints() { return points; }
        public void setPoints(int points) { this.points = points; }
    }
}
