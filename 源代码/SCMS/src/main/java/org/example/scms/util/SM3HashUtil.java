package org.example.scms.util;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.encoders.Hex;

/**
 * SM3哈希工具类
 * 用于密码哈希和数据完整性验证
 */
public class SM3HashUtil {

    /**
     * 生成随机盐值
     * 
     * @param length 盐值长度（字节数）
     * @return 十六进制盐值字符串
     */
    public static String generateSalt(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return Hex.toHexString(salt);
    }

    /**
     * 使用SM3计算哈希值
     * 
     * @param data 待哈希数据
     * @return 哈希值（十六进制）
     */
    public static String hash(String data) {
        SM3Digest digest = new SM3Digest();
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        digest.update(dataBytes, 0, dataBytes.length);
        byte[] hashBytes = new byte[digest.getDigestSize()];
        digest.doFinal(hashBytes, 0);
        return Hex.toHexString(hashBytes);
    }

    /**
     * 使用SM3计算带盐的哈希值（用于密码存储）
     * 
     * @param data 待哈希数据
     * @param salt 盐值（十六进制）
     * @return 哈希值（十六进制）
     */
    public static String hashWithSalt(String data, String salt) {
        byte[] saltBytes = Hex.decode(salt);
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        // 混合数据和盐值
        byte[] mixed = new byte[dataBytes.length + saltBytes.length];
        System.arraycopy(dataBytes, 0, mixed, 0, dataBytes.length);
        System.arraycopy(saltBytes, 0, mixed, dataBytes.length, saltBytes.length);

        SM3Digest digest = new SM3Digest();
        digest.update(mixed, 0, mixed.length);
        byte[] hashBytes = new byte[digest.getDigestSize()];
        digest.doFinal(hashBytes, 0);
        return Hex.toHexString(hashBytes);
    }

    /**
     * 验证密码
     * 
     * @param password       待验证的原始密码
     * @param salt           盐值（十六进制）
     * @param hashedPassword 已存储的哈希密码（十六进制）
     * @return 是否匹配
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        String newHash = hashWithSalt(password, salt);
        return newHash.equals(hashedPassword);
    }    /**
     * 使用HMAC-SM3计算消息认证码
     * 
     * @param data 原始数据
     * @param key  密钥（十六进制）
     * @return HMAC值（十六进制）
     */
    public static String hmac(String data, String key) {
        try {
            byte[] keyBytes = Hex.decode(key);
            return hmacWithBytes(data, keyBytes);
        } catch (Exception e) {
            // 如果十六进制解码失败，则将密钥作为字符串处理
            return hmacWithStringKey(data, key);
        }
    }

    /**
     * 使用HMAC-SM3计算消息认证码（字符串密钥）
     * 
     * @param data 原始数据
     * @param key  密钥（字符串）
     * @return HMAC值（十六进制）
     */
    public static String hmacWithStringKey(String data, String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        return hmacWithBytes(data, keyBytes);
    }

    /**
     * 使用HMAC-SM3计算消息认证码（字节数组密钥）
     * 
     * @param data 原始数据
     * @param keyBytes  密钥字节数组
     * @return HMAC值（十六进制）
     */
    private static String hmacWithBytes(String data, byte[] keyBytes) {
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);

        KeyParameter keyParameter = new KeyParameter(keyBytes);
        SM3Digest digest = new SM3Digest();
        HMac hmac = new HMac(digest);

        hmac.init(keyParameter);
        hmac.update(dataBytes, 0, dataBytes.length);

        byte[] hmacBytes = new byte[hmac.getMacSize()];
        hmac.doFinal(hmacBytes, 0);

        return Hex.toHexString(hmacBytes);
    }

    /**
     * 验证HMAC-SM3完整性
     * 
     * @param data         原始数据
     * @param key          密钥（十六进制）
     * @param expectedHmac 期望的HMAC值（十六进制）
     * @return 是否匹配
     */
    public static boolean verifyHmac(String data, String key, String expectedHmac) {
        String calculatedHmac = hmac(data, key);
        return calculatedHmac.equals(expectedHmac);
    }

    /**
     * 生成随机密钥（用于HMAC）
     * 
     * @param length 密钥长度（字节数）
     * @return 十六进制密钥字符串
     */
    public static String generateKey(int length) {
        byte[] key = new byte[length];
        new SecureRandom().nextBytes(key);
        return Hex.toHexString(key);
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        // 测试基本哈希功能
        String data = "测试SM3哈希功能";
        String hash = hash(data);
        System.out.println("原始数据: " + data);
        System.out.println("SM3哈希: " + hash);

        // 测试密码哈希和验证
        String password = "很复杂的p@ssw0rd!";
        String salt = generateSalt(16); // 128位盐
        String hashedPassword = hashWithSalt(password, salt);
        System.out.println("\n原始密码: " + password);
        System.out.println("盐值: " + salt);
        System.out.println("哈希密码: " + hashedPassword);
        System.out.println("密码验证结果: " + verifyPassword(password, salt, hashedPassword));
        System.out.println("错误密码验证结果: " + verifyPassword("wrong", salt, hashedPassword));

        // 测试HMAC功能
        String hmacKey = generateKey(16); // 128位密钥
        String message = "需要保证完整性的数据";
        String messageHmac = hmac(message, hmacKey);
        System.out.println("\n消息: " + message);
        System.out.println("HMAC密钥: " + hmacKey);
        System.out.println("消息HMAC: " + messageHmac);
        System.out.println("HMAC验证结果: " + verifyHmac(message, hmacKey, messageHmac));
        System.out.println("篡改消息HMAC验证结果: " + verifyHmac("篡改的数据", hmacKey, messageHmac));
    }
}
