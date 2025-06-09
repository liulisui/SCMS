import org.example.scms.model.Administrator;
import org.example.scms.service.AdministratorService;

/**
 * 测试管理员登录功能
 */
public class TestAdminLogin {
    public static void main(String[] args) {
        System.out.println("测试管理员登录功能...");
        
        try {
            AdministratorService adminService = new AdministratorService();
            
            // 测试每个管理员账户
            String[] usernames = {"admin", "sysadmin", "auditadmin", "schooladmin"};
            String password = "Abc!@#123";
            
            for (String username : usernames) {
                System.out.println("\n测试用户: " + username);
                try {
                    Administrator admin = adminService.login(username, password, "127.0.0.1", "Test-Agent");
                    if (admin != null) {
                        System.out.println("✓ 登录成功! 管理员ID: " + admin.getId() + ", 类型: " + admin.getAdminType());
                    } else {
                        System.out.println("✗ 登录失败 - 用户名或密码错误");
                    }
                } catch (Exception e) {
                    System.out.println("✗ 登录异常: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
        } catch (Exception e) {
            System.err.println("测试过程中发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
