package edu.upc.dsa.db;

import edu.upc.dsa.models.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    //resgistrar usuari
    public int registerUser(String username, String nombre, String password, String email){
        String sql = "INSERT INTO users (username, name, password, email, ects) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            pstm.setString(1, username);
            pstm.setString(2, nombre);
            pstm.setString(3, password);
            pstm.setString(4, email);
            pstm.setInt(5,100);
            
            pstm.executeUpdate();
            return 201;
        } catch (SQLException e) {
            return 409;
        }
    }
//login usuari
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        
        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {
            
            pstm.setString(1, username);
            pstm.setString(2, password);
            
            ResultSet rs = pstm.executeQuery();
            
            if (rs.next()) {
                
                return buildUser(rs);
                
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public User getUser(String username) {
        String sql = "SELECT * FROM users WHERE username=?";

        try (Connection conn = DBUtils.getConnection();
             PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, username);

            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return buildUser(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
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