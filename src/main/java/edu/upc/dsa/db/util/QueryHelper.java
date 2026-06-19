package edu.upc.dsa.db.util;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
import edu.upc.dsa.models.Puzzle;
import edu.upc.dsa.models.User;
import edu.upc.dsa.models.UserGameState;

import java.util.LinkedHashMap;

public class QueryHelper {

    public static String getTableName(Class theClass) {
        if (theClass == User.class) {
            return "users";
        }
        if (theClass == Producto.class) {
            return "shop";
        }
        if (theClass == UserGameState.class) {
            return "user_game_state";
        }
        if (theClass == Mission.class) {
            return "missions";
        }
        if (theClass == Objective.class) {
            return "objectives";
        }
        return theClass.getSimpleName().toLowerCase();
    }

    public static String getColumnName(Class theClass, String field) {
        if (theClass == User.class) {
            if (field.equals("id")) return "username";
            if (field.equals("nombre")) return "name";
            if (field.equals("password")) return "password_hash";
        }
        if (theClass == Producto.class) {
            if (field.equals("nombre")) return "name";
            if (field.equals("descripcion")) return "description";
        }
        if (theClass == UserGameState.class) {
            if (field.equals("id")) return "username";
            if (field.equals("currentMissionId")) return "current_mission_id";
            if (field.equals("currentObjectiveId")) return "current_objetive_id";
        }
        if (theClass == Mission.class) {
            if (field.equals("missionOrder")) return "mission_order";
            if (field.equals("active")) return "is_active";
        }
        if (theClass == Objective.class) {
            if (field.equals("missionId")) return "mission_id";
            if (field.equals("objectiveOrder")) return "objective_order";
        }
        return field;
    }

    public static String createSelectFindAll(Class theClass, LinkedHashMap<String, Object> params) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(getTableName(theClass));

        int i = 0;
        for (String key : params.keySet()) {
            if (i == 0) {
                sb.append(" WHERE ");
            } else {
                sb.append(" AND ");
            }

            sb.append(getColumnName(theClass, key));
            if (theClass == User.class && key.equals("password")) {
                sb.append("=SHA2(?, 256)");
            } else {
                sb.append("=?");
            }
            i++;
        }

        return sb.toString();
    }

    public static String createInsertUser() {
        return "INSERT INTO users (username, name, password_hash, email, ects, avatar) VALUES (?, ?, SHA2(?, 256), ?, ?, ?)";
    }

    public static String createUpdateUser() {
        return "UPDATE users SET name=?, email=?, ects=?, avatar=? WHERE username=?";
    }

    public static String createUpsertInventory() {
        return "INSERT INTO inventory (username, product_id, quantity) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + 1";
    }

    public static String createSelectInventoryQuantity() {
        return "SELECT quantity FROM inventory WHERE username=? AND product_id=?";
    }

    public static String createDecreaseInventoryQuantity() {
        return "UPDATE inventory SET quantity=quantity-1 WHERE username=? AND product_id=?";
    }

    public static String createDeleteInventoryProduct() {
        return "DELETE FROM inventory WHERE username=? AND product_id=?";
    }

    public static String createSelectInventory() {
        return "SELECT s.id, s.name, s.description, s.price, i.quantity " +
                "FROM inventory i " +
                "INNER JOIN shop s ON s.id = i.product_id " +
                "WHERE i.username=?";
    }

    public static String createSelectMissionObjects() {
        return "SELECT id, name, description, type, 0 AS obtenido, NULL AS content " +
                "FROM objects ORDER BY id ASC";
    }

    public static String createSelectMissionObjectsByUser() {
        return "SELECT o.id, o.name, o.description, o.type, " +
                "CASE WHEN earned.objects_id IS NULL THEN 0 ELSE 1 END AS obtenido, " +
                "CASE WHEN earned.objects_id IS NULL THEN NULL ELSE o.content END AS content " +
                "FROM objects o " +
                "LEFT JOIN ( " +
                "  SELECT DISTINCT r.objects_id FROM objective_rewards r " +
                "  JOIN user_objectives uo ON uo.objective_id=r.objective_id " +
                "  WHERE uo.username=? AND uo.completed=1 " +
                ") earned ON earned.objects_id=o.id " +
                "ORDER BY o.id ASC";
    }

    public static String createSelectRanking() {
        return "SELECT u.username, u.name, u.email, u.password_hash, u.ects, u.avatar, " +
                "g.health, g.max_health, g.current_mission_id, g.current_objetive_id, " +
                "m.title AS mission_title, o.title AS objective_title " +
                "FROM users u " +
                "LEFT JOIN user_game_state g ON g.username = u.username " +
                "LEFT JOIN missions m ON m.id = g.current_mission_id " +
                "LEFT JOIN objectives o ON o.id = g.current_objetive_id " +
                "ORDER BY COALESCE(g.current_mission_id, 0) DESC, " +
                "COALESCE(g.current_objetive_id, 0) DESC, u.ects DESC, u.username ASC";
    }

    public static String createSelectMissionsWithObjectives() {
        return "SELECT m.id AS mission_id, m.title AS mission_title, m.description AS mission_description, " +
                "m.mission_order, m.is_active, o.id AS objective_id, o.title AS objective_title, " +
                "o.description AS objective_description, o.objective_order, o.type, o.reference, o.reward " +
                "FROM missions m " +
                "LEFT JOIN objectives o ON o.mission_id = m.id " +
                "ORDER BY m.mission_order ASC, o.objective_order ASC";
    }

    public static String createInsertUserGameState() {
        return "INSERT INTO user_game_state (username, health, max_health, current_mission_id, current_objetive_id) " +
                "VALUES (?, ?, ?, ?, ?)";
    }

    // --- Progreso de misiones ---

    public static String createUpsertUserObjective() {
        return "INSERT INTO user_objectives (username, objective_id, completed) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE completed=1";
    }

    public static String createUpsertUserMission() {
        return "INSERT INTO user_missions (username, mission_id, completed) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE completed=1";
    }

    public static String createAddEcts() {
        return "UPDATE users SET ects=ects+? WHERE username=?";
    }

    public static String createUpdateGameState() {
        return "UPDATE user_game_state SET current_mission_id=?, current_objetive_id=? WHERE username=?";
    }

    public static String createUpdateGameStateObjective() {
        return "UPDATE user_game_state SET current_objetive_id=? WHERE username=?";
    }

    public static String createSelectObjectiveRewardNames() {
        return "SELECT s.name, s.id FROM objective_rewards r " +
                "JOIN objects o ON o.id=r.objects_id " +
                "JOIN shop s ON s.name=o.name " +
                "WHERE r.objective_id=?";
    }

    public static String createSelectMissionCompletionCheck() {
        return "SELECT COUNT(*) AS total, SUM(CASE WHEN uo.completed=1 THEN 1 ELSE 0 END) AS done " +
                "FROM objectives o " +
                "LEFT JOIN user_objectives uo ON uo.objective_id=o.id AND uo.username=? " +
                "WHERE o.mission_id=?";
    }

    public static String createSelectNextMission() {
        return "SELECT id, title, description, mission_order, is_active FROM missions " +
                "WHERE mission_order > (SELECT mission_order FROM missions WHERE id=?) AND is_active=1 " +
                "ORDER BY mission_order ASC LIMIT 1";
    }

    public static String createSelectFirstObjectiveOfMission() {
        return "SELECT id, mission_id, title, description, objective_order, type, reference, reward " +
                "FROM objectives WHERE mission_id=? ORDER BY objective_order ASC LIMIT 1";
    }

    public static String createSelectNextPendingObjective() {
        return "SELECT o.id, o.mission_id, o.title, o.description, o.objective_order, o.type, o.reference, o.reward " +
                "FROM objectives o " +
                "LEFT JOIN user_objectives uo ON uo.objective_id=o.id AND uo.username=? " +
                "WHERE o.mission_id=? AND (uo.completed IS NULL OR uo.completed=0) " +
                "ORDER BY o.objective_order ASC LIMIT 1";
    }

    public static String createSelectGameState() {
        return "SELECT g.username, g.health, g.max_health, g.current_mission_id, g.current_objetive_id, " +
                "m.title AS mission_title, o.title AS objective_title " +
                "FROM user_game_state g " +
                "LEFT JOIN missions m ON m.id=g.current_mission_id " +
                "LEFT JOIN objectives o ON o.id=g.current_objetive_id " +
                "WHERE g.username=?";
    }

    public static String createSelectCompletedRewardObjectNames() {
        return "SELECT DISTINCT o.name " +
                "FROM user_objectives uo " +
                "JOIN objective_rewards r ON r.objective_id=uo.objective_id " +
                "JOIN objects o ON o.id=r.objects_id " +
                "WHERE uo.username=? AND uo.completed=1";
    }

    public static String getTableName(Class theClass, boolean forPuzzle) {
        if (theClass == Puzzle.class) return "puzzles";
        return getTableName(theClass);
    }

    public static String getColumnNameForPuzzle(String field) {
        if (field.equals("id")) return "id";
        if (field.equals("missionId")) return "mission_id";
        if (field.equals("objectiveId")) return "objective_id";
        return field;
    }
}
