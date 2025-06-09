import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.scms.util.DBUtil;

/**
 * 调试预约记录查询问题
 */
public class DebugReservations {
    public static void main(String[] args) {
        try {
            // 1. 检查数据库连接
            System.out.println("=== 检查数据库连接 ===");
            try (Connection conn = DBUtil.getConnection()) {
                System.out.println("数据库连接成功");
            }

            // 2. 查看所有用户
            System.out.println("\n=== 查看所有用户 ===");
            String userSql = "SELECT id, username, full_name FROM users";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(userSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    System.out.printf("用户ID: %d, 用户名: %s, 真实姓名: %s%n",
                            rs.getLong("id"), rs.getString("username"), rs.getString("full_name"));
                }
            }

            // 3. 查看所有预约记录
            System.out.println("\n=== 查看所有预约记录 ===");
            String reservationSql = "SELECT id, user_id, user_name, status, create_time FROM reservations ORDER BY create_time DESC";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(reservationSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    System.out.printf("预约ID: %d, 用户ID: %s, 用户姓名: %s, 状态: %s, 创建时间: %s%n",
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("user_name"),
                            rs.getString("status"),
                            rs.getTimestamp("create_time"));
                }
            }

            // 4. 检查特定用户ID的预约记录
            System.out.println("\n=== 测试用户ID查询 ===");
            Long testUserId = 1L; // 假设测试用户ID为1
            String findByUserIdSql = "SELECT * FROM reservations WHERE user_id = ? ORDER BY create_time DESC";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(findByUserIdSql)) {

                pstmt.setLong(1, testUserId);
                ResultSet rs = pstmt.executeQuery();

                System.out.printf("查询用户ID %d 的预约记录:%n", testUserId);
                int count = 0;
                while (rs.next()) {
                    count++;
                    System.out.printf("  预约ID: %d, 用户ID: %s, 状态: %s%n",
                            rs.getInt("id"), rs.getString("user_id"), rs.getString("status"));
                }
                System.out.printf("共找到 %d 条记录%n", count);
            }

            // 5. 检查数据类型
            System.out.println("\n=== 检查表结构 ===");
            String describeSql = "DESCRIBE reservations";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(describeSql);
                    ResultSet rs = pstmt.executeQuery()) {

                System.out.println("reservations表结构:");
                while (rs.next()) {
                    String field = rs.getString("Field");
                    String type = rs.getString("Type");
                    if ("user_id".equals(field) || "id".equals(field)) {
                        System.out.printf("  %s: %s%n", field, type);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
