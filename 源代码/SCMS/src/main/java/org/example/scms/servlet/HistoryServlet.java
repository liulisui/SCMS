package org.example.scms.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;
import org.example.scms.util.AuditLogUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 历史预约查询Servlet
 */
@WebServlet("/user/history")
public class HistoryServlet extends HttpServlet {

    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String idCard = request.getParameter("idCard");
        String queryType = request.getParameter("type"); // public, official, 或 all

        // 如果没有查询参数，只显示查询页面
        if (idCard == null || idCard.trim().isEmpty()) {
            request.getRequestDispatcher("/user/history.jsp").forward(request, response);
            return;
        }

        // 验证身份证号格式
        if (!isValidIdCard(idCard.trim())) {
            request.setAttribute("error", "请输入正确的18位身份证号码");
            request.getRequestDispatcher("/user/history.jsp").forward(request, response);
            return;
        }        if (queryType == null || queryType.trim().isEmpty()) {
            queryType = "all";
        }try {
            System.out.println("[调试] 收到查询请求: 身份证=" + idCard + ", 类型=" + queryType);
            List<ReservationHistoryItem> historyItems = new ArrayList<>();

            // 根据查询类型获取历史预约记录
            if ("all".equals(queryType) || "public".equals(queryType)) {
                System.out.println("[调试] 调用PublicReservationService.getReservationsByIdCard(" + idCard.trim() + ")");
                List<PublicReservation> publicReservations = publicReservationService
                        .getReservationsByIdCard(idCard.trim());
                System.out.println("[调试] 获取到 " + publicReservations.size() + " 条公众预约记录");
                for (PublicReservation reservation : publicReservations) {
                    historyItems.add(new ReservationHistoryItem(reservation, "public"));
                    System.out.println("[调试] 添加公众预约: ID=" + reservation.getId() + ", 名称=" + reservation.getVisitorName() + ", 状态=" + reservation.getStatus());
                }
            }

            if ("all".equals(queryType) || "official".equals(queryType)) {
                System.out.println("[调试] 调用OfficialReservationService.getReservationsByIdCard(" + idCard.trim() + ")");
                List<OfficialReservation> officialReservations = officialReservationService
                        .getReservationsByIdCard(idCard.trim());
                System.out.println("[调试] 获取到 " + officialReservations.size() + " 条公务预约记录");
                for (OfficialReservation reservation : officialReservations) {
                    historyItems.add(new ReservationHistoryItem(reservation, "official"));
                    System.out.println("[调试] 添加公务预约: ID=" + reservation.getId() + ", 名称=" + reservation.getVisitorName() + ", 状态=" + reservation.getStatus());
                }
            }            // 按创建时间降序排序
            historyItems.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

            // 记录查询历史预约的审计日志
            Map<String, Object> queryData = new HashMap<>();
            queryData.put("idCard", idCard.trim());
            queryData.put("queryType", queryType);
            queryData.put("resultCount", historyItems.size());
            AuditLogUtil.logQuery(request, "reservation_history", queryData);

            request.setAttribute("reservations", historyItems);
            request.setAttribute("queryIdCard", idCard.trim());
            request.setAttribute("queryType", queryType);
            request.getRequestDispatcher("/user/history.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "查询过程中发生错误，请稍后重试");
            request.getRequestDispatcher("/user/history.jsp").forward(request, response);
        }
    }    /**
     * 验证身份证号格式
     */
    private boolean isValidIdCard(String idCard) {
        System.out.println("[调试] 验证身份证: " + idCard);
        
        if (idCard.length() != 18) {
            System.out.println("[调试] 身份证长度不是18: " + idCard.length());
            return false;
        }

        // 简单的身份证号码校验正则表达式
        String pattern = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$";
        boolean isValid = idCard.matches(pattern);
        System.out.println("[调试] 身份证格式验证结果: " + isValid);
        return isValid;
    }

    /**
     * 历史预约项包装类
     */
    public static class ReservationHistoryItem {
        private final Object reservation;
        private final String type;

        public ReservationHistoryItem(Object reservation, String type) {
            this.reservation = reservation;
            this.type = type;
        }

        public Object getReservation() {
            return reservation;
        }

        public String getType() {
            return type;
        }

        public String getReservationNo() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getReservationNo();
            } else {
                return ((OfficialReservation) reservation).getReservationNo();
            }
        }

        public String getVisitorName() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getVisitorName();
            } else {
                return ((OfficialReservation) reservation).getVisitorName();
            }
        }

        public String getStatus() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getStatus();
            } else {
                return ((OfficialReservation) reservation).getStatus();
            }
        }

        public java.time.LocalDateTime getCreatedAt() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getCreatedAt();
            } else {
                return ((OfficialReservation) reservation).getCreatedAt();
            }
        }

        public java.time.LocalDate getVisitDate() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getVisitDate();
            } else {
                return ((OfficialReservation) reservation).getVisitDate();
            }
        }

        public java.time.LocalTime getVisitTimeStart() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getVisitTimeStart();
            } else {
                return ((OfficialReservation) reservation).getVisitTimeStart();
            }
        }        public java.time.LocalTime getVisitTimeEnd() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getVisitTimeEnd();
            } else {
                return ((OfficialReservation) reservation).getVisitTimeEnd();
            }
        }

        public Long getId() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getId();
            } else {
                return ((OfficialReservation) reservation).getId();
            }
        }
    }
}
