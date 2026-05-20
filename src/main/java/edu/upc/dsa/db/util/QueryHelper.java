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

    public static String createUpdateUserEcts() {
        return "UPDATE users SET ects=? WHERE username=?";
    }

    public static String createUpsertInventory() {
        return "INSERT INTO inventory (username, product_id, quantity) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE quantity = quantity + 1";
    }

    public static String createInsertUserGameState() {
        return "INSERT INTO user_game_state (username, health, max_health, current_mission_id, current_objetive_id) " +
                "VALUES (?, ?, ?, ?, ?)";
    }
}
