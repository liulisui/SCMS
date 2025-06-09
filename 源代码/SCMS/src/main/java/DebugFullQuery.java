import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;
import org.example.scms.servlet.HistoryServlet.ReservationHistoryItem;

/**
 * 调试完整查询流程
 */
public class DebugFullQuery {
    public static void main(String[] args) {
        try {
            String testIdCard = "330602200405140510";
            System.out.println("=== 测试完整查询流程 ===");
            System.out.println("查询身份证号: " + testIdCard);

            // 1. 测试 PublicReservationService
            PublicReservationService publicService = new PublicReservationService();
            List<PublicReservation> publicReservations = publicService.getReservationsByIdCard(testIdCard);
            System.out.println("\n公众预约查询结果:");
            System.out.println("找到记录数: " + publicReservations.size());
            for (PublicReservation reservation : publicReservations) {
                System.out.printf("  ID: %d, 预约号: %s, 姓名: %s, 状态: %s, 日期: %s%n", 
                    reservation.getId(), 
                    reservation.getReservationNo(),
                    reservation.getVisitorName(),
                    reservation.getStatus(),
                    reservation.getVisitDate());
            }

            // 2. 测试 OfficialReservationService
            OfficialReservationService officialService = new OfficialReservationService();
            List<OfficialReservation> officialReservations = officialService.getReservationsByIdCard(testIdCard);
            System.out.println("\n公务预约查询结果:");
            System.out.println("找到记录数: " + officialReservations.size());
            for (OfficialReservation reservation : officialReservations) {
                System.out.printf("  ID: %d, 预约号: %s, 姓名: %s, 状态: %s, 日期: %s%n", 
                    reservation.getId(), 
                    reservation.getReservationNo(),
                    reservation.getVisitorName(),
                    reservation.getStatus(),
                    reservation.getVisitDate());
            }

            // 3. 测试 ReservationHistoryItem 包装
            List<ReservationHistoryItem> historyItems = new ArrayList<>();
            
            for (PublicReservation reservation : publicReservations) {
                historyItems.add(new ReservationHistoryItem(reservation, "public"));
            }
            
            for (OfficialReservation reservation : officialReservations) {
                historyItems.add(new ReservationHistoryItem(reservation, "official"));
            }

            System.out.println("\n包装后的历史项目:");
            System.out.println("总记录数: " + historyItems.size());
            for (ReservationHistoryItem item : historyItems) {
                System.out.printf("  类型: %s, ID: %d, 预约号: %s, 姓名: %s, 状态: %s%n",
                    item.getType(),
                    item.getId(),
                    item.getReservationNo(),
                    item.getVisitorName(),
                    item.getStatus());
            }

        } catch (Exception e) {
            System.err.println("测试过程出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
