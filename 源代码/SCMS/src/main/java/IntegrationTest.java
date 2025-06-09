import java.util.List;

import org.example.scms.model.PublicReservation;
import org.example.scms.service.PublicReservationService;
import org.example.scms.servlet.HistoryServlet.ReservationHistoryItem;

/**
 * 集成测试 - 模拟完整的查询流程
 */
public class IntegrationTest {
    public static void main(String[] args) {
        String testIdCard = "330602200405140510";
        
        System.out.println("=== 集成测试 ===");
        System.out.println("测试身份证号: " + testIdCard);
        
        try {
            // 1. 测试Service层 - 这是Servlet会调用的方法
            PublicReservationService service = new PublicReservationService();
            List<PublicReservation> reservations = service.getReservationsByIdCard(testIdCard);
            
            System.out.println("Service层查询结果: " + reservations.size() + " 条记录");
            
            // 2. 模拟Servlet中的处理逻辑 - 创建HistoryItem
            if (!reservations.isEmpty()) {
                System.out.println("转换为HistoryItem:");
                for (PublicReservation reservation : reservations) {
                    ReservationHistoryItem item = new ReservationHistoryItem(reservation, "public");
                    System.out.println("  ID=" + item.getId() + 
                                     ", 姓名=" + item.getVisitorName() + 
                                     ", 状态=" + item.getStatus() +
                                     ", 类型=" + item.getType());
                }
                
                System.out.println("\n【结论】查询流程正常，应该能在页面显示 " + reservations.size() + " 条记录");
            } else {
                System.out.println("【问题】Service层返回空结果");
            }
            
        } catch (Exception e) {
            System.err.println("测试出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
