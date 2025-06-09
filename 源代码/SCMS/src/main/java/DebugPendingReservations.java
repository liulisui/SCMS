import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.example.scms.util.DBUtil;

public class DebugPendingReservations {
    public static void main(String[] args) {
        System.out.println("=== 调试待审核预约数据 ===");
        
        // 1. 检查所有公务预约的状态
        checkOfficialReservationStatuses();
        
        // 2. 检查所有公众预约的状态
        checkPublicReservationStatuses();
        
        // 3. 尝试查询待审核的预约
        queryPendingReservations();
    }
    
    private static void checkOfficialReservationStatuses() {
        System.out.println("\n--- 检查公务预约状态 ---");
        String sql = "SELECT id, visitor_name, status, created_at FROM official_reservations ORDER BY created_at DESC LIMIT 10";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("ID\t访客姓名\t状态\t创建时间");
            System.out.println("----------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%d\t%s\t'%s'\t%s%n", 
                    rs.getLong("id"),
                    rs.getString("visitor_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"));
            }
            
        } catch (SQLException e) {
            System.err.println("查询公务预约失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkPublicReservationStatuses() {
        System.out.println("\n--- 检查公众预约状态 ---");
        String sql = "SELECT id, visitor_name, status, created_at FROM public_reservations ORDER BY created_at DESC LIMIT 10";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            System.out.println("ID\t访客姓名\t状态\t创建时间");
            System.out.println("----------------------------------------");
            
            while (rs.next()) {
                System.out.printf("%d\t%s\t'%s'\t%s%n", 
                    rs.getLong("id"),
                    rs.getString("visitor_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"));
            }
            
        } catch (SQLException e) {
            System.err.println("查询公众预约失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void queryPendingReservations() {
        System.out.println("\n--- 查询待审核预约 ---");
        
        // 尝试不同的状态值
        String[] statusValues = {"pending", "PENDING", "Pending"};
        
        for (String status : statusValues) {
            System.out.println("\n尝试状态值: '" + status + "'");
            
            // 公务预约
            String officialSql = "SELECT COUNT(*) FROM official_reservations WHERE status = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(officialSql)) {
                
                stmt.setString(1, status);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("  公务预约数量: " + count);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("查询失败: " + e.getMessage());
            }
            
            // 公众预约
            String publicSql = "SELECT COUNT(*) FROM public_reservations WHERE status = ?";
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(publicSql)) {
                
                stmt.setString(1, status);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("  公众预约数量: " + count);
                    }
                }
                
            } catch (SQLException e) {
                System.err.println("查询失败: " + e.getMessage());
            }
        }
    }
}
