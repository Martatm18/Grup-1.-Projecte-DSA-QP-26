package edu.upc.dsa.db;

import edu.upc.dsa.models.User;
import edu.upc.dsa.models.UserGameState;
import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserDAO {
    public static final int USERNAME_EXISTS = 409;
    public static final int EMAIL_EXISTS = 410;

    // Registrar usuario
    public int registerUser(String username, String nombre, String password, String email) {
        Session session = null;

        try {
            session = FactorySession.openSession();

            if (session.get(User.class, username) != null) {
                return USERNAME_EXISTS;
            }

            if (email != null && !email.trim().isEmpty()) {
                LinkedHashMap<String, Object> params = new LinkedHashMap<>();
                params.put("email", email.trim().toLowerCase());

                if (!session.findAll(User.class, params).isEmpty()) {
                    return EMAIL_EXISTS;
                }
            }

            User user = new User(username, nombre, password);
            user.setEmail(email == null ? null : email.trim().toLowerCase());
            user.setEcts(100);
            user.setAvatar("avatar_1");

            session.save(user);
            session.save(new UserGameState(username));

            return 201;
        } catch (Exception e) {
            e.printStackTrace();
            return 500;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // Login por usuario y password
    public User loginUser(String username, String password) {
        Session session = null;

        try {
            session = FactorySession.openSession();

            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("id", username);
            params.put("password", password);

            List<Object> result = session.findAll(User.class, params);

            if (result.isEmpty()) {
                return null;
            }

            return (User) result.get(0);

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // Login por email y password
    public User loginUserByEmail(String email, String password) {
        Session session = null;

        try {
            session = FactorySession.openSession();

            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("email", email.trim().toLowerCase());
            params.put("password", password);

            List<Object> result = session.findAll(User.class, params);

            if (result.isEmpty()) {
                return null;
            }

            return (User) result.get(0);

        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    // Obtener usuario por id
    public User getUser(String username) {
        Session session = null;

        try {
            session = FactorySession.openSession();
            return (User) session.get(User.class, username);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    public int updateAvatar(String username, String avatar) {
        String sql = "UPDATE users SET avatar=? WHERE username=?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, avatar);
            pstm.setString(2, username);

            int updatedRows = pstm.executeUpdate();
            return updatedRows == 0 ? 404 : 204;
        } catch (SQLException e) {
            e.printStackTrace();
            return 500;
        }
    }

    public List<User> getRanking() {
        List<User> ranking = new ArrayList<>();
        String sql = "SELECT u.username, u.name, u.email, u.password, u.ects, u.avatar, " +
                "g.health, g.max_health, g.current_mission_id, g.current_objetive_id, " +
                "m.title AS mission_title, o.title AS objective_title " +
                "FROM users u " +
                "LEFT JOIN user_game_state g ON g.username = u.username " +
                "LEFT JOIN missions m ON m.id = g.current_mission_id " +
                "LEFT JOIN objectives o ON o.id = g.current_objetive_id " +
                "ORDER BY COALESCE(g.current_mission_id, 0) DESC, " +
                "COALESCE(g.current_objetive_id, 0) DESC, u.ects DESC, u.username ASC";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql);
             ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                User user = buildUser(rs);
                UserGameState gameState = new UserGameState();
                gameState.setUsername(user.getId());
                gameState.setHealth(rs.getInt("health"));
                gameState.setMaxHealth(rs.getInt("max_health"));
                gameState.setCurrentMissionId(rs.getObject("current_mission_id") == null ? null : rs.getInt("current_mission_id"));
                gameState.setCurrentObjectiveId(rs.getObject("current_objetive_id") == null ? null : rs.getInt("current_objetive_id"));
                gameState.setCurrentMissionTitle(rs.getString("mission_title"));
                gameState.setCurrentObjectiveTitle(rs.getString("objective_title"));
                user.setGameState(gameState);
                ranking.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return ranking;
    }

    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("password")
        );

        user.setEmail(rs.getString("email"));
        user.setEcts(rs.getInt("ects"));
        user.setAvatar(rs.getString("avatar"));

        return user;
    }
}
