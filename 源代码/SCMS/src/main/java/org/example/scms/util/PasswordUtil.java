package org.example.scms.util;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.Security;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 * 密码加密工具类 - 使用国密SM3算法
 */
public class PasswordUtil {

    static {
        // 如果还没有注册，则注册BouncyCastle提供者
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    /**
     * 生成随机盐值
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return new String(Hex.encode(salt));
    }

    /**
     * 使用SM3算法加密密码
     */
    public static String encryptPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SM3", BouncyCastleProvider.PROVIDER_NAME);
            String saltedPassword = password + salt;
            byte[] data = saltedPassword.getBytes(StandardCharsets.UTF_8);
            digest.update(data);
            byte[] result = digest.digest();
            return new String(Hex.encode(result));
        } catch (Exception e) {
            throw new RuntimeException("加密密码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        String encrypted = encryptPassword(password, salt);
        return encrypted.equals(hashedPassword);
    }
}
