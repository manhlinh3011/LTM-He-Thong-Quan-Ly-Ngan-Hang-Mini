package btl;

import java.sql.*;

public class CauHinh {
    public static final String DB_URL = "jdbc:mysql://localhost:3306/bankmini?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    public static final String DB_USER = "linhmanhhoang"; // sửa theo máy bạn
    public static final String DB_PASS = "manhlinh2003"; // sửa theo máy bạn

    public static Connection ketNoi() throws SQLException {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); } catch (ClassNotFoundException e) { }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}
