package edu.upc.dsa.db;

import edu.upc.dsa.models.User;
import edu.upc.dsa.db.orm.FactorySession;
import edu.upc.dsa.db.orm.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    //resgistrar usuari
    public int registerUser(String username, String nombre, String password, String email){
        Session session = null;

        try {
            session = FactorySession.openSession();

            User user = new User(username, nombre, password);
            user.setEmail(email);
            user.setEcts(100);

            session.save(user);

            return 201;
        } catch (Exception e) {
            return 409;
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
//login usuari
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