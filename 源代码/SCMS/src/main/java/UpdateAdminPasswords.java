import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.util.DBUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 管理员密码批量更新工具
 * 将所有管理员密码重新设置为"Abc!@#123"，使用SM3加密保存
 */
public class UpdateAdminPasswords {
    
    public static void main(String[] args) {
        System.out.println("开始更新管理员密码...");
        
        try {
            // 获取所有管理员ID和用户名
            List<AdminInfo> admins = getAllAdmins();
            System.out.println("找到 " + admins.size() + " 个管理员账户需要更新密码");
            
            // 新密码信息
            String newPassword = "Abc!@#123";
            int updatedCount = 0;
            
            // 更新每个管理员的密码
            for (AdminInfo admin : admins) {
                // 生成新的盐值
                String salt = SM3HashUtil.generateSalt(16);
                
                // 使用SM3对密码进行加盐哈希
                String hashedPassword = SM3HashUtil.hashWithSalt(newPassword, salt);
                
                // 更新数据库
                boolean success = updateAdminPassword(admin.id, hashedPassword, salt);
                
                if (success) {
                    updatedCount++;
                    System.out.println("已更新管理员 [" + admin.username + "] 的密码");
                } else {
                    System.err.println("更新管理员 [" + admin.username + "] 密码失败");
                }
            }
            
            System.out.println("密码更新完成: " + updatedCount + "/" + admins.size() + " 个管理员密码已更新");
            System.out.println("新密码为: " + newPassword + " (请确保记录此密码)");
            
        } catch (Exception e) {
            System.err.println("更新管理员密码时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 获取所有管理员信息
     */
    private static List<AdminInfo> getAllAdmins() throws Exception {
        List<AdminInfo> admins = new ArrayList<>();
        String sql = "SELECT id, username FROM administrators";
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                AdminInfo admin = new AdminInfo();
                admin.id = rs.getLong("id");
                admin.username = rs.getString("username");
                admins.add(admin);
            }
        }
        
        return admins;
    }
      /**
     * 更新管理员密码
     */
    private static boolean updateAdminPassword(Long adminId, String hashedPassword, String salt) {
        String sql = "UPDATE administrators SET password = ?, salt = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, hashedPassword);
            stmt.setString(2, salt);
            stmt.setLong(3, adminId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("更新管理员ID " + adminId + " 的密码时出错: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * 管理员信息类
     */
    static class AdminInfo {
        long id;
        String username;
    }
}
