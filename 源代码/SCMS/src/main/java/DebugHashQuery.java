import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;

import org.example.scms.util.DBUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 调试身份证哈希查询问题
 */
public class DebugHashQuery {
    public static void main(String[] args) {        try {
            // 1. 测试哈希计算
            String testIdCard = "330602200405140510"; // 用户输入的身份证号
            String idCardHash = SM3HashUtil.hash(testIdCard);
            System.out.println("=== 哈希计算测试 ===");
            System.out.println("原始身份证号: " + testIdCard);
            System.out.println("SM3哈希值: " + idCardHash);
            System.out.println("哈希值长度: " + idCardHash.length());

            // 2. 检查数据库中是否有对应的哈希值
            System.out.println("\n=== 检查数据库中的哈希值 ===");
            String sql1 = "SELECT visitor_id_card_hash, visitor_name, created_at FROM public_reservations LIMIT 5";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql1);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("public_reservations表中的哈希值:");
                while (rs.next()) {
                    String hash = rs.getString("visitor_id_card_hash");
                    String name = rs.getString("visitor_name");
                    Timestamp created = rs.getTimestamp("created_at");
                    System.out.printf("  姓名: %s, 哈希: %s, 创建时间: %s%n", name, hash, created);
                }
            }

            String sql2 = "SELECT visitor_id_card_hash, visitor_name, created_at FROM official_reservations LIMIT 5";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql2);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("\nofficial_reservations表中的哈希值:");
                while (rs.next()) {
                    String hash = rs.getString("visitor_id_card_hash");
                    String name = rs.getString("visitor_name");
                    Timestamp created = rs.getTimestamp("created_at");
                    System.out.printf("  姓名: %s, 哈希: %s, 创建时间: %s%n", name, hash, created);
                }
            }            // 3. 测试不同身份证号的哈希值
            System.out.println("\n=== 测试多个身份证号的哈希值 ===");
            String[] testIds = {
                "330602200405140510", // 用户输入的身份证号
                "110101199001011234",
                "320101199501012345",
                "330602200405140510" // 重复测试，确保哈希一致性
            };
            
            for (String id : testIds) {
                String hash = SM3HashUtil.hash(id);
                System.out.printf("身份证: %s -> 哈希: %s%n", id, hash);
            }

            // 4. 直接用已知哈希值查询
            System.out.println("\n=== 直接查询测试 ===");
            String queryHash = "SELECT * FROM public_reservations WHERE visitor_id_card_hash = ? LIMIT 1";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(queryHash)) {
                
                pstmt.setString(1, idCardHash);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("找到匹配记录:");
                        System.out.println("  ID: " + rs.getLong("id"));
                        System.out.println("  姓名: " + rs.getString("visitor_name"));
                        System.out.println("  哈希: " + rs.getString("visitor_id_card_hash"));
                    } else {
                        System.out.println("使用哈希值 " + idCardHash + " 未找到记录");
                    }
                }
            }

            // 5. 检查是否有数据但哈希不匹配
            System.out.println("\n=== 检查数据存在性 ===");
            String countSql = "SELECT COUNT(*) as total FROM public_reservations";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(countSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("public_reservations总记录数: " + rs.getInt("total"));
                }
            }

            String countSql2 = "SELECT COUNT(*) as total FROM official_reservations";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(countSql2);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("official_reservations总记录数: " + rs.getInt("total"));
                }
            }

        } catch (Exception e) {
            System.err.println("调试过程出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
