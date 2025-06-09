import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.example.scms.util.DBUtil;
import org.example.scms.util.SM3HashUtil;

/**
 * 测试密码验证
 */
public class TestPasswordVerification {
    
    public static void main(String[] args) {
        String testUsername = "admin";
        String testPassword = "Abc!@#123";
        
        try {
            // 从数据库获取管理员信息
            String sql = "SELECT username, password, salt FROM administrators WHERE username = ?";
            
            try (Connection conn = DBUtil.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, testUsername);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String dbUsername = rs.getString("username");
                        String dbPassword = rs.getString("password");
                        String dbSalt = rs.getString("salt");
                        
                        System.out.println("数据库中的用户信息:");
                        System.out.println("用户名: " + dbUsername);
                        System.out.println("密码哈希: " + dbPassword);
                        System.out.println("盐值: " + dbSalt);
                        
                        // 使用输入密码计算哈希
                        String inputPasswordHash = SM3HashUtil.hashWithSalt(testPassword, dbSalt);
                        System.out.println("\n输入密码计算的哈希: " + inputPasswordHash);
                        
                        // 验证密码
                        boolean isValid = SM3HashUtil.verifyPassword(testPassword, dbSalt, dbPassword);
                        System.out.println("密码验证结果: " + isValid);
                        
                        // 手动比较
                        boolean manualCheck = inputPasswordHash.equals(dbPassword);
                        System.out.println("手动比较结果: " + manualCheck);
                        
                    } else {
                        System.out.println("未找到用户: " + testUsername);
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试密码验证时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
