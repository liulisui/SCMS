import org.example.scms.util.SM3HashUtil;

public class TestSpecificPassword {
    public static void main(String[] args) {
        // 给定的哈希值和盐值
        String givenHash = "10f95a0d8fccd30434260bc62a47f6b874278d9b6fadf229673dd88783a5b7b3";
        String givenSalt = "0482edd23817491ad197d59e9630444a";
        
        // 要测试的密码
        String testPassword = "Abc!!!111";
        
        System.out.println("测试密码: " + testPassword);
        System.out.println("给定哈希值: " + givenHash);
        System.out.println("给定盐值: " + givenSalt);
        System.out.println();
        
        // 使用给定的盐值计算测试密码的哈希值
        String calculatedHash = SM3HashUtil.hashWithSalt(testPassword, givenSalt);
        System.out.println("计算得到的哈希值: " + calculatedHash);
        
        // 比较哈希值
        boolean isMatch = calculatedHash.equals(givenHash);
        System.out.println("哈希值匹配: " + isMatch);
        
        // 使用verifyPassword方法验证
        boolean isVerified = SM3HashUtil.verifyPassword(testPassword, givenSalt, givenHash);
        System.out.println("密码验证结果: " + isVerified);
        
        if (isMatch && isVerified) {
            System.out.println("\n✅ 成功！密码 '" + testPassword + "' 匹配给定的哈希值！");
        } else {
            System.out.println("\n❌ 密码 '" + testPassword + "' 不匹配给定的哈希值。");
        }
    }
}