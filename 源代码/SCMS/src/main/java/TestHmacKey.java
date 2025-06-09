public class TestHmacKey {
    public static void main(String[] args) {
        String hmacKey = "123456789ABCDEF0123456789ABCDEF01";
        System.out.println("HMAC Key: " + hmacKey);
        System.out.println("Key length: " + hmacKey.length());
        System.out.println("Key bytes: ");
        for (int i = 0; i < hmacKey.length(); i++) {
            System.out.print(hmacKey.charAt(i) + " ");
        }
        System.out.println();
        
        // 检查是否包含非十六进制字符
        boolean isValidHex = hmacKey.matches("[0-9A-Fa-f]+");
        System.out.println("Is valid hex: " + isValidHex);
        
        // 尝试手动解码
        try {
            org.bouncycastle.util.encoders.Hex.decode(hmacKey);
            System.out.println("Decode successful!");
        } catch (Exception e) {
            System.out.println("Decode failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
