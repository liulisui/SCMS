package org.example.scms.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 二维码生成工具类
 */
public class QRCodeUtil {
    /**
     * 生成二维码
     * 
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return Base64编码的图片字符串
     */
    public static String generateQRCode(String content, int width, int height) {
        return generateQRCodeWithColor(content, width, height, Color.BLACK, Color.WHITE);
    }

    /**
     * 生成带颜色的二维码
     * 
     * @param content         二维码内容
     * @param width           宽度
     * @param height          高度
     * @param foregroundColor 前景色（二维码图案颜色）
     * @param backgroundColor 背景色
     * @return Base64编码的图片字符串
     */
    public static String generateQRCodeWithColor(String content, int width, int height, Color foregroundColor,
            Color backgroundColor) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    image.setRGB(x, y, bitMatrix.get(x, y) ? foregroundColor.getRGB() : backgroundColor.getRGB());
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            System.err.println(
                    "QRCodeUtil.generateQRCodeWithColor() 异常: " + e.getClass().getSimpleName() + " - "
                            + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 根据通行码状态生成对应颜色的二维码
     * 
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @param status  通行码状态（valid, expired, early, invalid）
     * @return Base64编码的图片字符串
     */    public static String generateQRCodeByStatus(String content, int width, int height, String status) {
        Color foregroundColor;

        switch (status) {
            case "valid":
            case "approved":
                // 有效通行码 - 紫色 (类似图1-4)
                foregroundColor = new Color(102, 126, 234); // #667eea
                break;
            case "expired":
            case "invalid":
                // 无效/过期通行码 - 灰色 (类似图1-3)
                foregroundColor = new Color(107, 114, 128); // #6b7280
                break;
            case "early":
                // 未到时间 - 黄色
                foregroundColor = new Color(245, 158, 11); // #f59e0b
                break;
            default:
                // 默认黑色
                foregroundColor = Color.BLACK;
                break;
        }

        return generateQRCodeWithColor(content, width, height, foregroundColor, Color.WHITE);
    }

    /**
     * 生成二维码并返回带数据前缀的Base64字符串
     * 
     * @param content 二维码内容
     * @param width   宽度
     * @param height  高度
     * @return 带data:image/png;base64,前缀的Base64编码图片字符串
     */
    public static String generateQRCodeBase64(String content, int width, int height) {
        String base64 = generateQRCode(content, width, height);
        if (base64 != null) {
            return "data:image/png;base64," + base64;
        }
        return null;
    }

    /**
     * 生成通行码二维码
     * 
     * @param reservationId 预约ID
     * @param userName      用户姓名
     * @param phone         手机号（脱敏）
     * @param validTime     有效时间
     * @return Base64编码的二维码图片
     */
    public static String generatePassCode(String reservationId, String userName, String phone, String validTime) {
        String content = String.format("校园通行码\n预约ID：%s\n姓名：%s\n手机：%s\n有效期：%s",
                reservationId, userName, phone, validTime);
        return generateQRCode(content, 200, 200);
    }

    /**
     * 生成手机端二维码数据
     * 
     * @param realName      真实姓名
     * @param realIdCard    真实身份证号
     * @param phone         手机号
     * @param reservationId 预约ID
     * @return 二维码内容字符串
     */
    public static String generateMobileQRCodeData(String realName, String realIdCard, String phone, int reservationId) {
        // 处理姓名脱敏
        String maskedName = maskName(realName);

        // 处理身份证号脱敏 (出生日期用*代替)
        String maskedIdCard = maskIdCard(realIdCard);

        // 当前时间
        String generateTime = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return String.format("校园通行码\n预约ID：%d\n姓名：%s\n身份证：%s\n生成时间：%s",
                reservationId, maskedName, maskedIdCard, generateTime);
    }

    /**
     * 姓名脱敏处理
     * 
     * @param name 真实姓名
     * @return 脱敏后的姓名
     */
    private static String maskName(String name) {
        if (name == null || name.length() == 0) {
            return "";
        }

        if (name.length() == 1) {
            return name;
        } else if (name.length() == 2) {
            return name.charAt(0) + "*";
        } else {
            // 超过2个字的，中间全部用*代替
            StringBuilder masked = new StringBuilder();
            masked.append(name.charAt(0));
            for (int i = 1; i < name.length() - 1; i++) {
                masked.append("*");
            }
            masked.append(name.charAt(name.length() - 1));
            return masked.toString();
        }
    }

    /**
     * 身份证号脱敏处理（出生日期用*代替）
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    private static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return idCard;
        }

        // 身份证号格式：前6位地区码 + 8位出生日期 + 3位顺序码 + 1位校验码
        // 将出生日期部分(第7-14位)用*代替
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /**
     * 检查通行码是否在有效时间内
     * 
     * @param visitTime 访问时间
     * @param duration  停留时长
     * @return 是否有效
     */
    public static boolean isPassCodeValid(java.time.LocalDateTime visitTime, String duration) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        // 计算结束时间
        java.time.LocalDateTime endTime = calculateEndTime(visitTime, duration);

        // 检查当前时间是否在有效期内（提前30分钟开始有效）
        java.time.LocalDateTime startTime = visitTime.minusMinutes(30);

        return now.isAfter(startTime) && now.isBefore(endTime);
    }

    /**
     * 计算结束时间
     * 
     * @param visitTime 访问时间
     * @param duration  停留时长
     * @return 结束时间
     */
    private static java.time.LocalDateTime calculateEndTime(java.time.LocalDateTime visitTime, String duration) {
        if (duration == null) {
            return visitTime.plusHours(1); // 默认1小时
        }

        switch (duration) {
            case "1小时":
                return visitTime.plusHours(1);
            case "2小时":
                return visitTime.plusHours(2);
            case "4小时":
                return visitTime.plusHours(4);
            case "8小时":
                return visitTime.plusHours(8);
            case "1天":
                return visitTime.plusDays(1);
            default:
                return visitTime.plusHours(1);
        }
    }
}
