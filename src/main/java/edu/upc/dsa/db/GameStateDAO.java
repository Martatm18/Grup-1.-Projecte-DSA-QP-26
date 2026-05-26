package edu.upc.dsa.db;

import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
import edu.upc.dsa.models.UserGameState;

import java.util.List;

public class GameStateDAO {

    public UserGameState getOrCreateGameState(String username) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            UserGameState gameState = session.get(UserGameState.class, username);

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
        Session session = null;

        try {
            session = FactorySession.openSession();
            return session.getMissionsWithObjectives();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void fillCurrentNames(Session session, UserGameState gameState) {
        if (gameState.getCurrentMissionId() != null) {
            Mission mission = session.get(Mission.class, gameState.getCurrentMissionId());
            if (mission != null) {
                gameState.setCurrentMissionTitle(mission.getTitle());
            }
        }

        if (gameState.getCurrentObjectiveId() != null) {
            Objective objective = session.get(Objective.class, gameState.getCurrentObjectiveId());
            if (objective != null) {
                gameState.setCurrentObjectiveTitle(objective.getTitle());
            }
        }
    }
}
