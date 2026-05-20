package edu.upc.dsa.db.util;

import edu.upc.dsa.models.Producto;
import edu.upc.dsa.models.User;

import java.util.LinkedHashMap;

public class QueryHelper {

    public static String getTableName(Class theClass) {
        if (theClass == User.class) {
            return "users";
        }
        if (theClass == Producto.class) {
            return "shop";
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
}
