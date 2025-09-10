package btl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class KetNoiCSDL {
    private static final String URL = "jdbc:mysql://localhost:3306/bankmini"; // đúng tên DB
    private static final String USER = "linhmanhhoang"; // hoặc user bạn đã tạo
    private static final String PASSWORD = "manhlinh2003"; // mật khẩu của user

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Thêm hàm main để chạy test
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ Kết nối MySQL thành công!");
            }
        } catch (Exception e) {
            System.out.println("❌ Kết nối thất bại!");
            e.printStackTrace();
        }
    }
}
