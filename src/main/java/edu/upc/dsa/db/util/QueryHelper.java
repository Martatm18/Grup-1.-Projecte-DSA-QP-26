package edu.upc.dsa.db.util;
import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.Mission;
import edu.upc.dsa.models.Objective;
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

            sb.append(getColumnName(theClass, key)).append("=?");
            i++;
        }

        return sb.toString();
    }

    public static String createInsertUser() {
        return "INSERT INTO users (username, name, password, email, ects, avatar) VALUES (?, ?, ?, ?, ?, ?)";
    }

    public static String createUpdateUser() {
        return "UPDATE users SET name=?, password=?, email=?, ects=?, avatar=? WHERE username=?";
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

    public static String createSelectRanking() {
        return "SELECT u.username, u.name, u.email, u.password, u.ects, u.avatar, " +
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
}
