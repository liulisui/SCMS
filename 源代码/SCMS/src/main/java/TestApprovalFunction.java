import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.example.scms.util.DBUtil;
import org.example.scms.service.OfficialReservationService;

public class TestApprovalFunction {
    public static void main(String[] args) {
        System.out.println("=== 测试审批功能 ===");
        
        // 1. 查询当前待审核的预约
        Long pendingReservationId = findPendingReservation();
        if (pendingReservationId == null) {
            System.out.println("没有找到待审核的预约，创建一个测试预约");
            return;
        }
        
        System.out.println("找到待审核预约ID: " + pendingReservationId);
        
        // 2. 显示审批前的状态
        showReservationStatus(pendingReservationId, "审批前");
        
        // 3. 执行审批操作
        OfficialReservationService service = new OfficialReservationService();
        boolean success = service.approveReservation(
            pendingReservationId, 
            1L, // 假设管理员ID为1
            "approved", 
            "测试审批", 
            "127.0.0.1", 
            "Test-User-Agent"
        );
        
        System.out.println("审批操作结果: " + (success ? "成功" : "失败"));
        
        // 4. 显示审批后的状态
        showReservationStatus(pendingReservationId, "审批后");
        
        // 5. 验证状态是否从pending变为approved
        verifyStatusChange(pendingReservationId);
    }
    
    private static Long findPendingReservation() {
        String sql = "SELECT id FROM official_reservations WHERE status = 'pending' ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong("id");
            }
            
        } catch (SQLException e) {
            System.err.println("查询待审核预约失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    private static void showReservationStatus(Long reservationId, String label) {
        String sql = "SELECT id, visitor_name, status, approved_by, approved_at FROM official_reservations WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, reservationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n" + label + "状态:");
                    System.out.println("  ID: " + rs.getLong("id"));
                    System.out.println("  访客姓名: " + rs.getString("visitor_name"));
                    System.out.println("  状态: " + rs.getString("status"));
                    System.out.println("  审批人: " + rs.getObject("approved_by"));
                    System.out.println("  审批时间: " + rs.getTimestamp("approved_at"));
                } else {
                    System.out.println(label + "未找到预约记录!");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("查询预约状态失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void verifyStatusChange(Long reservationId) {
        String sql = "SELECT status FROM official_reservations WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, reservationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String currentStatus = rs.getString("status");
                    System.out.println("\n验证结果:");
                    System.out.println("  当前状态: " + currentStatus);
                    
                    if ("approved".equals(currentStatus)) {
                        System.out.println("  ✓ 审批功能正常，状态已更新为approved");
                    } else {
                        System.out.println("  ✗ 审批功能异常，状态仍为: " + currentStatus);
                    }
                } else {
                    System.out.println("  ✗ 预约记录不存在!");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("验证状态失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
