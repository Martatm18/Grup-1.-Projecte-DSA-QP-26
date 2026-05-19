package edu.upc.dsa.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtils {
    public static final String DB_NAME = "protocolosigma";
    public static final String DB_HOST = "127.0.0.1";
    public static final String DB_USER = "root";
    public static final String DB_PASS = "Lluna1981";
    public static final String DB_PORT = "3306";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mariadb://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME +
                "?user=" + DB_USER + "&password=" + DB_PASS
        );
    }
}