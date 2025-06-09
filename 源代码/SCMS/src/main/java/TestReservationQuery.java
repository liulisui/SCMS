import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.example.scms.dao.ReservationDAO;
import org.example.scms.dao.UserDAO;
import org.example.scms.model.Reservation;
import org.example.scms.model.User;
import org.example.scms.util.DBUtil;

/**
 * 测试使用身份证号查询预约记录的功能
 */
public class TestReservationQuery {
    public static void main(String[] args) {
        try {
            // 1. 检查数据库连接
            System.out.println("=== 测试数据库连接 ===");
            try (Connection conn = DBUtil.getConnection()) {
                System.out.println("数据库连接成功");
            }

            // 2. 查看所有用户的身份证号信息
            System.out.println("\n=== 查看所有用户信息 ===");
            UserDAO userDAO = new UserDAO();
            String userSql = "SELECT id, username, full_name, real_id_card FROM users";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(userSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    System.out.printf("用户ID: %d, 用户名: %s, 真实姓名: %s, 身份证号: %s%n",
                            rs.getLong("id"), rs.getString("username"),
                            rs.getString("full_name"), rs.getString("real_id_card"));
                }
            }

            // 3. 查看所有预约记录的身份证号信息
            System.out.println("\n=== 查看所有预约记录的身份证号 ===");
            String reservationSql = "SELECT id, user_id, user_name, real_id_card, status FROM reservations ORDER BY create_time DESC";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(reservationSql);
                    ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    System.out.printf("预约ID: %d, 用户ID: %s, 用户姓名: %s, 身份证号: %s, 状态: %s%n",
                            rs.getInt("id"),
                            rs.getString("user_id"),
                            rs.getString("user_name"),
                            rs.getString("real_id_card"),
                            rs.getString("status"));
                }
            }

            // 4. 测试通过身份证号查询预约记录
            System.out.println("\n=== 测试通过身份证号查询预约记录 ===");
            ReservationDAO reservationDAO = new ReservationDAO();

            // 获取第一个有身份证号的预约记录来测试
            String testIdCard = null;
            String getTestIdCardSql = "SELECT real_id_card FROM reservations WHERE real_id_card IS NOT NULL AND real_id_card != '' LIMIT 1";
            try (Connection conn = DBUtil.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(getTestIdCardSql);
                    ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    testIdCard = rs.getString("real_id_card");
                }
            }

            if (testIdCard != null) {
                System.out.printf("测试身份证号: %s%n", testIdCard);
                List<Reservation> reservations = reservationDAO.findByRealIdCard(testIdCard);
                System.out.printf("找到 %d 条预约记录%n", reservations.size());

                for (Reservation reservation : reservations) {
                    System.out.printf("  预约ID: %d, 用户姓名: %s, 身份证号: %s, 状态: %s%n",
                            reservation.getId(), reservation.getUserName(),
                            reservation.getRealIdCard(), reservation.getStatus());
                }
            } else {
                System.out.println("没有找到包含身份证号的预约记录");
            }

            // 5. 测试用户查询
            System.out.println("\n=== 测试用户查询 ===");
            String testUsername = "student1";
            User user = userDAO.findByUsername(testUsername);
            if (user != null) {
                System.out.printf("用户: %s, 身份证号: %s%n", user.getUsername(), user.getRealIdCard());

                if (user.getRealIdCard() != null && !user.getRealIdCard().isEmpty()) {
                    List<Reservation> userReservations = reservationDAO.findByRealIdCard(user.getRealIdCard());
                    System.out.printf("该用户通过身份证号查询到 %d 条预约记录%n", userReservations.size());
                } else {
                    System.out.println("该用户没有设置身份证号");
                }
            } else {
                System.out.printf("未找到用户: %s%n", testUsername);
            }

        } catch (SQLException e) {
            System.err.println("数据库操作失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
