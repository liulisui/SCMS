import org.example.scms.util.SM3HashUtil;

public class TestDoubleEncryption {
    public static void main(String[] args) {
        String originalPassword = "Abc!!!111";
        String givenHash = "10f95a0d8fccd30434260bc62a47f6b874278d9b6fadf229673dd88783a5b7b3";
        String givenSalt = "0482edd23817491ad197d59e9630444a";
        
        System.out.println("=== 验证双重加密问题 ===");
        System.out.println("原始密码: " + originalPassword);
        System.out.println("数据库中的哈希值: " + givenHash);
        System.out.println("数据库中的盐值: " + givenSalt);
        System.out.println();
        
        // 模拟创建管理员时的双重加密过程
        System.out.println("=== 模拟双重加密过程 ===");
        
        // 第一次加密（在Servlet中）
        String salt1 = SM3HashUtil.generateSalt(16);
        String hashedPassword1 = SM3HashUtil.hashWithSalt(originalPassword, salt1);
        System.out.println("第一次加密:");
        System.out.println("  盐值1: " + salt1);
        System.out.println("  哈希1: " + hashedPassword1);
        
        // 第二次加密（在Service中）
        String salt2 = SM3HashUtil.generateSalt(16);
        String hashedPassword2 = SM3HashUtil.hashWithSalt(hashedPassword1, salt2);
        System.out.println("第二次加密:");
        System.out.println("  盐值2: " + salt2);
        System.out.println("  哈希2: " + hashedPassword2);
        
        System.out.println();
        System.out.println("=== 验证登录时的单次加密 ===");
        
        // 登录时只进行一次加密
        String loginHash = SM3HashUtil.hashWithSalt(originalPassword, givenSalt);
        System.out.println("登录时计算的哈希: " + loginHash);
        System.out.println("与数据库哈希匹配: " + loginHash.equals(givenHash));
        
        System.out.println();
        System.out.println("=== 尝试反向验证 ===");
        
        // 如果数据库中存储的是双重加密的结果，我们需要找出第一次加密的结果
        // 因为第二次加密使用了不同的盐值，我们无法直接反推
        // 但我们可以验证：如果第一次加密的结果再次用给定盐值加密，能否得到数据库中的哈希
        
        // 我们知道最终结果，尝试看能否通过某种方式验证
        System.out.println("由于双重加密使用了两个不同的随机盐值，无法直接反推原始密码");
        System.out.println("但这解释了为什么原始密码无法通过正常验证流程");
    }
}
