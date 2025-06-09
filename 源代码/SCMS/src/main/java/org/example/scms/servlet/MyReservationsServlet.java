package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
 * 我的预约查询Servlet
 */
@WebServlet("/user/my-reservations")
public class MyReservationsServlet extends HttpServlet {

    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String type = request.getParameter("type"); // public, official, 或 all
        if (type == null) {
            type = "all"; // 默认显示所有类型
        }

        try {
            List<ReservationWrapper> wrappers = new ArrayList<>();

            // 获取一个较大的日期范围来包含所有可能的预约记录
            LocalDate startDate = LocalDate.now().minusYears(1); // 一年前
            LocalDate endDate = LocalDate.now().plusMonths(6); // 半年后

            // 根据类型参数获取预约数据
            if ("all".equals(type) || "public".equals(type)) {
                List<PublicReservation> publicReservations = publicReservationService.getReservationsByDateRange(
                        startDate, endDate, null); // null表示获取所有状态的预约
                wrappers.addAll(publicReservations.stream()
                        .map(this::wrapPublicReservation)
                        .collect(Collectors.toList()));
            }

            if ("all".equals(type) || "official".equals(type)) {
                List<OfficialReservation> officialReservations = officialReservationService.getReservationsByDateRange(
                        startDate, endDate, null); // null表示获取所有状态的预约
                wrappers.addAll(officialReservations.stream()
                        .map(this::wrapOfficialReservation)
                        .collect(Collectors.toList()));
            }

            // 为每个预约记录计算状态（是否在有效时间内）
            LocalDateTime now = LocalDateTime.now();
            for (ReservationWrapper wrapper : wrappers) {
                if ("approved".equals(wrapper.getStatus())) {
                    LocalDateTime visitDateTime = wrapper.getVisitDateTime();
                    LocalDateTime endDateTime = wrapper.getEndDateTime();

                    // 计算通行码状态
                    String accessCodeStatus = "invalid";
                    if (now.isBefore(visitDateTime.minusMinutes(30))) {
                        accessCodeStatus = "early"; // 未到时间
                    } else if (now.isAfter(visitDateTime.minusMinutes(30)) && now.isBefore(endDateTime)) {
                        accessCodeStatus = "valid"; // 有效
                    } else if (now.isAfter(endDateTime)) {
                        accessCodeStatus = "expired"; // 已过期
                    }

                    wrapper.setAccessCodeStatus(accessCodeStatus);
                }            }

            // 记录查询我的预约的审计日志
            Map<String, Object> queryData = new HashMap<>();
            queryData.put("queryType", type);
            queryData.put("resultCount", wrappers.size());
            queryData.put("dateRange", "past 1 year to next 6 months");
            AuditLogUtil.logQuery(request, "my_reservations", queryData);

            request.setAttribute("reservations", wrappers);
            request.setAttribute("currentType", type);
            request.setAttribute("currentTime", now);
            request.getRequestDispatcher("/user/my-reservations.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "查询预约记录时发生错误：" + e.getMessage());
            request.getRequestDispatcher("/user/dashboard.jsp").forward(request, response);
        }
    }

    /**
     * 包装公众预约为统一的包装类
     */
    private ReservationWrapper wrapPublicReservation(PublicReservation reservation) {
        return new ReservationWrapper(reservation, "public");
    }

    /**
     * 包装公务预约为统一的包装类
     */
    private ReservationWrapper wrapOfficialReservation(OfficialReservation reservation) {
        return new ReservationWrapper(reservation, "official");
    }

    /**
     * 预约包装类，用于统一处理不同类型的预约
     */
    public static class ReservationWrapper {
        private final Object reservation;
        private final String type;
        private String accessCodeStatus;

        public ReservationWrapper(Object reservation, String type) {
            this.reservation = reservation;
            this.type = type;
        }

        public Object getReservation() {
            return reservation;
        }

        public String getType() {
            return type;
        }

        public String getAccessCodeStatus() {
            return accessCodeStatus;
        }

        public void setAccessCodeStatus(String accessCodeStatus) {
            this.accessCodeStatus = accessCodeStatus;
        }

        public String getStatus() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getStatus();
            } else {
                return ((OfficialReservation) reservation).getStatus();
            }
        }

        public LocalDateTime getVisitDateTime() {
            if ("public".equals(type)) {
                PublicReservation pub = (PublicReservation) reservation;
                return LocalDateTime.of(pub.getVisitDate(), pub.getVisitTimeStart());
            } else {
                OfficialReservation off = (OfficialReservation) reservation;
                return LocalDateTime.of(off.getVisitDate(), off.getVisitTimeStart());
            }
        }

        public LocalDateTime getEndDateTime() {
            if ("public".equals(type)) {
                PublicReservation pub = (PublicReservation) reservation;
                return LocalDateTime.of(pub.getVisitDate(), pub.getVisitTimeEnd());
            } else {
                OfficialReservation off = (OfficialReservation) reservation;
                return LocalDateTime.of(off.getVisitDate(), off.getVisitTimeEnd());
            }
        }

        public String getVisitorName() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getVisitorName();
            } else {
                return ((OfficialReservation) reservation).getVisitorName();
            }
        }

        public String getReservationNo() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getReservationNo();
            } else {
                return ((OfficialReservation) reservation).getReservationNo();
            }
        }

        public Long getId() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getId();
            } else {
                return ((OfficialReservation) reservation).getId();
            }
        }

        public String getPassCode() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getPassCode();
            } else {
                return ((OfficialReservation) reservation).getPassCode();
            }
        }

        public String getVisitReason() {
            if ("public".equals(type)) {
                return ((PublicReservation) reservation).getVisitReason();
            } else {
                return ((OfficialReservation) reservation).getVisitReason();
            }
        }
    }
}
