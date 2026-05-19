package edu.upc.dsa.db.orm;

import edu.upc.dsa.db.DBUtils;

import java.sql.Connection;
import java.sql.SQLException;

public class FactorySession {
    public static Session openSession() { //abre conexion con ORM
        try {
            Connection conn = DBUtils.getConnection();
            return new SessionImpl(conn);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}