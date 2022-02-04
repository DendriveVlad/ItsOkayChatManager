package itsokay.chatmanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataBase {
    private static final String url = "jdbc:mysql://109.195.243.172:3306/vladbase?useSSL=false";
    private static final String user = "vladi4ka";
    private static final String password = "RajG6xsb9sMzJ82A";

    public static Connection con;
    private long unixTime = System.currentTimeMillis() / 1000L / 360L;

    public void connect() throws SQLException, ClassNotFoundException {
        Class.forName("com.mysql.jdbc.Driver");
        con = DriverManager.getConnection(url, user, password);
    }

    public PreparedStatement doGet(String request) throws SQLException, ClassNotFoundException {
        if (con == null || con.isClosed() || System.currentTimeMillis() / 1000L / 360L - unixTime > 2) {
            connect();
        }
        unixTime = System.currentTimeMillis() / 1000L / 360L;
        return con.prepareStatement(request);
    }
}
