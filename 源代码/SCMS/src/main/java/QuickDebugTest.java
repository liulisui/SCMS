import java.util.List;

import org.example.scms.model.PublicReservation;
import org.example.scms.service.PublicReservationService;
import org.example.scms.util.SM3HashUtil;

/**
 * 快速调试测试
 */
public class QuickDebugTest {
    public static void main(String[] args) {
        String testIdCard = "330602200405140510";
        
        System.out.println("=== 快速调试测试 ===");
        System.out.println("测试身份证号: " + testIdCard);
        
        // 1. 直接测试哈希计算
        String hash = SM3HashUtil.hash(testIdCard);
        System.out.println("计算的哈希值: " + hash);
        
        // 2. 测试Service层查询
        PublicReservationService service = new PublicReservationService();
        try {
            List<PublicReservation> reservations = service.getReservationsByIdCard(testIdCard);
            System.out.println("Service层查询到 " + reservations.size() + " 条记录");
            
            if (reservations.isEmpty()) {
                System.out.println("【问题】Service层返回空列表，但直接SQL能查到数据");
                
                // 测试trim后的查询
                List<PublicReservation> reservations2 = service.getReservationsByIdCard(testIdCard.trim());
                System.out.println("trim后查询到 " + reservations2.size() + " 条记录");
                
                // 测试不同的身份证号格式
                System.out.println("测试不同格式的身份证号...");
                String[] variants = {
                    testIdCard,
                    testIdCard.trim(),
                    " " + testIdCard,
                    testIdCard + " ",
                    " " + testIdCard + " "
                };
                
                for (String variant : variants) {
                    String variantHash = SM3HashUtil.hash(variant);
                    System.out.println("身份证: '" + variant + "' -> 哈希: " + variantHash);
                    if (!variantHash.equals(hash)) {
                        System.out.println("【发现】不同格式产生不同哈希值！");
                    }
                }
                
            } else {
                for (PublicReservation r : reservations) {
                    System.out.println("找到记录 ID=" + r.getId() + ", 姓名=" + r.getVisitorName());
                }
            }
        } catch (Exception e) {
            System.err.println("查询出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
