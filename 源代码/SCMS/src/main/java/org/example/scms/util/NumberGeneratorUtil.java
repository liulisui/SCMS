package org.example.scms.util;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 编号生成器工具类
 * 用于生成预约编号、通行证编码等
 */
public class NumberGeneratorUtil {
    private static final String ALPHA_NUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Random RANDOM = new SecureRandom();

    /**
     * 生成预约编号
     * 格式：类型前缀(P/O) + 日期(yyyyMMdd) + 6位随机数字
     * P: 公共预约，O: 公务预约
     */
    public static String generateReservationNo(String type) {
        String prefix = type.equalsIgnoreCase("public") ? "P" : "O";
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%06d", RANDOM.nextInt(1000000));
        return prefix + date + random;
    }

    /**
     * 生成通行证编码
     * 格式：随机的10位字母数字组合
     */
    public static String generatePassCode() {
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int index = RANDOM.nextInt(ALPHA_NUMERIC.length());
            sb.append(ALPHA_NUMERIC.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成QR码数据
     * 格式：预约类型 + 预约ID + 通行证编码 + 时间戳
     * 用于后续验证扫码信息的有效性
     */
    public static String generateQrCodeData(String type, Long reservationId, String passCode) {
        long timestamp = System.currentTimeMillis();
        String data = type + ":" + reservationId + ":" + passCode + ":" + timestamp;

        // 添加HMAC-SM3签名，确保数据完整性和真实性
        String hmacKey = "1234567890ABCDEF1234567890ABCDEF"; // 实际应用中应从配置获取密钥
        String signature = SM3HashUtil.hmac(data, hmacKey);

        return data + ":" + signature;
    }

    /**
     * 验证QR码数据
     */
    public static boolean verifyQrCodeData(String qrCodeData) {
        String[] parts = qrCodeData.split(":");
        if (parts.length != 5) {
            return false;
        }

        String data = parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + parts[3];
        String signature = parts[4];

        String hmacKey = "1234567890ABCDEF1234567890ABCDEF"; // 实际应用中应从配置获取密钥
        return SM3HashUtil.verifyHmac(data, hmacKey, signature);
    }

    /**
     * 从QR码数据中提取预约ID
     */
    public static Long extractReservationIdFromQrCode(String qrCodeData) {
        if (!verifyQrCodeData(qrCodeData)) {
            return null;
        }
        String[] parts = qrCodeData.split(":");
        try {
            return Long.valueOf(parts[1]);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 从QR码数据中提取预约类型
     */
    public static String extractReservationTypeFromQrCode(String qrCodeData) {
        if (!verifyQrCodeData(qrCodeData)) {
            return null;
        }

        String[] parts = qrCodeData.split(":");
        return parts[0];
    }

    /**
     * 从QR码数据中提取通行证编码
     */
    public static String extractPassCodeFromQrCode(String qrCodeData) {
        if (!verifyQrCodeData(qrCodeData)) {
            return null;
        }

        String[] parts = qrCodeData.split(":");
        return parts[2];
    }

    /**
     * 检查QR码数据是否已过期（超过24小时）
     */
    public static boolean isQrCodeExpired(String qrCodeData) {
        if (!verifyQrCodeData(qrCodeData)) {
            return true;
        }

        String[] parts = qrCodeData.split(":");
        try {
            long timestamp = Long.parseLong(parts[3]);
            long currentTime = System.currentTimeMillis();
            long validityPeriod = 24 * 60 * 60 * 1000; // 24小时

            return (currentTime - timestamp) > validityPeriod;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * 生成公共预约编号
     * 格式：P + 日期(yyyyMMdd) + 6位随机数字
     */
    public static String generateReservationNumber() {
        return generateReservationNo("public");
    }

    /**
     * 生成公务预约编号
     * 格式：O + 日期(yyyyMMdd) + 6位随机数字
     */
    public static String generateOfficialReservationNumber() {
        return generateReservationNo("official");
    }

    /**
     * 测试方法
     */
    public static void main(String[] args) {
        System.out.println("公共预约编号示例: " + generateReservationNo("public"));
        System.out.println("公务预约编号示例: " + generateReservationNo("official"));
        System.out.println("通行证编码示例: " + generatePassCode());

        String passCode = generatePassCode();
        String qrData = generateQrCodeData("public", 12345L, passCode);
        System.out.println("\nQR码数据: " + qrData);
        System.out.println("QR码验证: " + verifyQrCodeData(qrData));
        System.out.println("预约ID: " + extractReservationIdFromQrCode(qrData));
        System.out.println("预约类型: " + extractReservationTypeFromQrCode(qrData));
        System.out.println("通行证编码: " + extractPassCodeFromQrCode(qrData));
        System.out.println("是否过期: " + isQrCodeExpired(qrData));
    }
}
