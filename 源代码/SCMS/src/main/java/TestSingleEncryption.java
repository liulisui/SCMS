import org.example.scms.util.SM3HashUtil;

public class TestSingleEncryption {
    public static void main(String[] args) {
        String originalPassword = "Abc!!!111";
        System.out.println("=== 测试修正后的单次加密 ===");
        System.out.println("原始密码: " + originalPassword);
        System.out.println();
        
        // 模拟修正后的流程：只在Service层进行一次加密
        System.out.println("=== Service层单次加密 ===");
        String salt = SM3HashUtil.generateSalt(16);
        String hashedPassword = SM3HashUtil.hashWithSalt(originalPassword, salt);
        
        System.out.println("生成的盐值: " + salt);
        System.out.println("加密后的哈希: " + hashedPassword);
        System.out.println();
        
        // 验证登录时的密码验证
        System.out.println("=== 登录验证测试 ===");
        boolean isVerified = SM3HashUtil.verifyPassword(originalPassword, salt, hashedPassword);
        System.out.println("密码验证结果: " + isVerified);
        
        if (isVerified) {
            System.out.println("✅ 修正成功！现在使用单次加密，登录验证正常工作");
        } else {
            System.out.println("❌ 验证失败，仍有问题");
        }
        
        System.out.println();
        System.out.println("=== 与原问题的哈希值对比 ===");
        String problemHash = "10f95a0d8fccd30434260bc62a47f6b874278d9b6fadf229673dd88783a5b7b3";
        String problemSalt = "0482edd23817491ad197d59e9630444a";
        
        System.out.println("问题哈希值: " + problemHash);
        System.out.println("问题盐值: " + problemSalt);
        
        // 使用问题盐值测试原始密码
        String testHash = SM3HashUtil.hashWithSalt(originalPassword, problemSalt);
        System.out.println("使用问题盐值计算的哈希: " + testHash);
        System.out.println("与问题哈希匹配: " + testHash.equals(problemHash));
        
        if (!testHash.equals(problemHash)) {
            System.out.println("⚠️ 数据库中的记录确实是双重加密的结果，需要重置该用户密码");
        }
    }
}
