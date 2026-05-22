package edu.upc.dsa.db;

import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
import edu.upc.dsa.models.UserGameState;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GameStateDAO {

    public UserGameState getOrCreateGameState(String username) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            UserGameState gameState = (UserGameState) session.get(UserGameState.class, username);

            if (gameState == null) {
                gameState = new UserGameState(username);
                session.save(gameState);
            }

            fillCurrentNames(session, gameState);
            return gameState;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public void createInitialGameState(String username) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            if (session.get(UserGameState.class, username) == null) {
                session.save(new UserGameState(username));
            }
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public List<Mission> getMissionsWithObjectives() {
        Map<Integer, Mission> missions = new LinkedHashMap<>();
        String sql = "SELECT m.id AS mission_id, m.title AS mission_title, m.description AS mission_description, " +
                "m.mission_order, m.is_active, o.id AS objective_id, o.title AS objective_title, " +
                "o.description AS objective_description, o.objective_order, o.type, o.reference, o.reward " +
                "FROM missions m " +
                "LEFT JOIN objectives o ON o.mission_id = m.id " +
                "ORDER BY m.mission_order ASC, o.objective_order ASC";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                Integer missionId = rs.getInt("mission_id");
                Mission mission = missions.get(missionId);
                if (mission == null) {
                    mission = new Mission(
                            missionId,
                            rs.getString("mission_title"),
                            rs.getString("mission_description"),
                            rs.getInt("mission_order"),
                            rs.getInt("is_active") == 1
                    );
                    mission.setObjectives(new ArrayList<Objective>());
                    missions.put(missionId, mission);
                }

                if (rs.getObject("objective_id") != null) {
                    mission.getObjectives().add(new Objective(
                            rs.getInt("objective_id"),
                            missionId,
                            rs.getString("objective_title"),
                            rs.getString("objective_description"),
                            rs.getInt("objective_order"),
                            rs.getString("type"),
                            rs.getString("reference"),
                            rs.getInt("reward")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return new ArrayList<>(missions.values());
    }

    private void fillCurrentNames(Session session, UserGameState gameState) {
        if (gameState.getCurrentMissionId() != null) {
            Mission mission = (Mission) session.get(Mission.class, gameState.getCurrentMissionId());
            if (mission != null) {
                gameState.setCurrentMissionTitle(mission.getTitle());
            }
        }

        if (gameState.getCurrentObjectiveId() != null) {
            Objective objective = (Objective) session.get(Objective.class, gameState.getCurrentObjectiveId());
            if (objective != null) {
                gameState.setCurrentObjectiveTitle(objective.getTitle());
            }
        }
    }
}
