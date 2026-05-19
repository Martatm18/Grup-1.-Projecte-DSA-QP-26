package edu.upc.dsa.db;

import edu.upc.dsa.models.User;
import edu.upc.dsa.db.util.FactorySession;
import edu.upc.dsa.db.util.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

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

            session.save(user);

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

    private User buildUser(ResultSet rs) throws SQLException {
        User user = new User(
                rs.getString("username"),
                rs.getString("name"),
                rs.getString("password")
        );

        user.setEmail(rs.getString("email"));
        user.setEcts(rs.getInt("ects"));

        return user;
    }
}
