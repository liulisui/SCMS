import org.example.scms.util.SM3HashUtil;

public class ComprehensivePasswordTest {
    public static void main(String[] args) {
        // 给定的哈希值和盐值
        String givenHash = "10f95a0d8fccd30434260bc62a47f6b874278d9b6fadf229673dd88783a5b7b3";
        String givenSalt = "0482edd23817491ad197d59e9630444a";
        
        System.out.println("给定哈希值: " + givenHash);
        System.out.println("给定盐值: " + givenSalt);
        System.out.println("盐值长度: " + givenSalt.length());
        System.out.println();
        
        // 测试多种可能的密码
        String[] testPasswords = {
            "Abc!!!111",           // 你确认的密码
            "abc!!!111",           // 小写版本
            "ABC!!!111",           // 大写版本
            "Abc111!!!",           // 顺序不同
            "admin123",            // 常见默认密码
            "123456",              // 简单密码
            "password",            // 常见密码
            "admin",               // 简单管理员密码
            "Abc123!",             // 相似密码
            "Abc!!!",              // 部分密码
            "111",                 // 部分密码
            "123",                 // 常见数字
            "Abc123",              // 无特殊字符版本
            "Abc@123",             // 不同特殊字符
            "Abc#123",             // 不同特殊字符
            "departmentadmin",     // 部门管理员相关
            "dept123",             // 部门相关
            "test123",             // 测试密码
            "",                    // 空密码
            " Abc!!!111",          // 前导空格
            "Abc!!!111 ",          // 后置空格
            " Abc!!!111 "          // 前后空格
        };
        
        System.out.println("正在测试多种可能的密码...\n");
        
        for (String password : testPasswords) {
            System.out.println("测试密码: '" + password + "'");
            
            try {
                String calculatedHash = SM3HashUtil.hashWithSalt(password, givenSalt);
                boolean isMatch = calculatedHash.equals(givenHash);
                boolean isVerified = SM3HashUtil.verifyPassword(password, givenSalt, givenHash);
                
                System.out.println("  计算哈希: " + calculatedHash);
                System.out.println("  匹配结果: " + isMatch);
                System.out.println("  验证结果: " + isVerified);
                
                if (isMatch && isVerified) {
                    System.out.println("  ✅ 找到匹配密码！");
                    System.out.println("  🎉 正确密码是: '" + password + "'");
                    break;
                } else {
                    System.out.println("  ❌ 不匹配");
                }
                
            } catch (Exception e) {
                System.out.println("  ⚠️ 计算出错: " + e.getMessage());
            }
            
            System.out.println();
        }
        
        System.out.println("=== 附加信息 ===");
        System.out.println("如果所有密码都不匹配，可能的原因：");
        System.out.println("1. 数据库中的哈希值或盐值被手动修改过");
        System.out.println("2. 创建时使用了不同的加密方法");
        System.out.println("3. 数据库数据不完整或损坏");
        System.out.println("4. 存在字符编码问题");
        
        // 验证工具类本身是否正常工作
        System.out.println("\n=== 验证工具类功能 ===");
        String testPwd = "test123";
        String testSalt = SM3HashUtil.generateSalt(16);
        String testHash = SM3HashUtil.hashWithSalt(testPwd, testSalt);
        boolean testVerify = SM3HashUtil.verifyPassword(testPwd, testSalt, testHash);
        System.out.println("工具类功能测试 - 密码: " + testPwd);
        System.out.println("工具类功能测试 - 验证结果: " + testVerify);
        System.out.println("工具类" + (testVerify ? "正常工作" : "存在问题"));
    }
}
