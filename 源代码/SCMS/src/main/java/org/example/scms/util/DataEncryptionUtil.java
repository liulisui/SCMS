package org.example.scms.util;

import java.security.SecureRandom;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.bouncycastle.crypto.engines.SM4Engine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;

/**
 * 数据加密工具类
 * 使用SM4算法进行数据加密和解密
 */
public class DataEncryptionUtil {

    private static final String ALGORITHM = "SM4";
    private static final int KEY_SIZE = 128; // SM4密钥长度为128位
    private static final int IV_SIZE = 12; // GCM模式推荐IV长度为12字节

    // 默认密钥（实际应用中应从配置文件或密钥管理系统获取）
    private static final String DEFAULT_KEY = "1234567890abcdef1234567890abcdef";

    /**
     * 生成随机密钥
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES"); // 使用AES生成器生成随机密钥
            keyGen.init(KEY_SIZE);
            SecretKey secretKey = keyGen.generateKey();
            return bytesToHex(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("生成密钥失败", e);
        }
    }

    /**
     * 加密数据
     */
    public static String encrypt(String plainText) {
        return encrypt(plainText, DEFAULT_KEY);
    }

    /**
     * 加密数据（指定密钥）
     */
    public static String encrypt(String plainText, String keyHex) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            byte[] key = hexToBytes(keyHex);
            byte[] iv = generateIV();

            SM4Engine engine = new SM4Engine();
            GCMBlockCipher cipher = new GCMBlockCipher(engine);

            KeyParameter keyParam = new KeyParameter(key);
            ParametersWithIV params = new ParametersWithIV(keyParam, iv);

            cipher.init(true, params);

            byte[] plainBytes = plainText.getBytes("UTF-8");
            byte[] cipherBytes = new byte[cipher.getOutputSize(plainBytes.length)];

            int len = cipher.processBytes(plainBytes, 0, plainBytes.length, cipherBytes, 0);
            cipher.doFinal(cipherBytes, len);

            // 将IV和密文拼接，然后Base64编码
            byte[] result = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(cipherBytes, 0, result, iv.length, cipherBytes.length);

            return Base64.toBase64String(result);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /**
     * 解密数据
     */
    public static String decrypt(String cipherText) {
        return decrypt(cipherText, DEFAULT_KEY);
    }

    /**
     * 解密数据（指定密钥）
     */
    public static String decrypt(String cipherText, String keyHex) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }

        try {
            byte[] key = hexToBytes(keyHex);
            byte[] data = Base64.decode(cipherText);

            // 分离IV和密文
            byte[] iv = new byte[IV_SIZE];
            byte[] cipherBytes = new byte[data.length - IV_SIZE];
            System.arraycopy(data, 0, iv, 0, IV_SIZE);
            System.arraycopy(data, IV_SIZE, cipherBytes, 0, cipherBytes.length);

            SM4Engine engine = new SM4Engine();
            GCMBlockCipher cipher = new GCMBlockCipher(engine);

            KeyParameter keyParam = new KeyParameter(key);
            ParametersWithIV params = new ParametersWithIV(keyParam, iv);

            cipher.init(false, params);

            byte[] plainBytes = new byte[cipher.getOutputSize(cipherBytes.length)];
            int len = cipher.processBytes(cipherBytes, 0, cipherBytes.length, plainBytes, 0);
            cipher.doFinal(plainBytes, len);

            // 移除可能的填充
            String result = new String(plainBytes, "UTF-8");
            return result.trim();
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }

    /**
     * 生成随机IV
     */
    private static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 字节数组转十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 十六进制字符串转字节数组
     */
    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        String original = "这是一段需要加密的敏感信息";
        System.out.println("原文: " + original);

        String encrypted = encrypt(original);
        System.out.println("加密后: " + encrypted);

        String decrypted = decrypt(encrypted);
        System.out.println("解密后: " + decrypted);

        System.out.println("加密解密是否成功: " + original.equals(decrypted));
    }
}
