package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用户仪表板Servlet
 */
@WebServlet("/user/dashboard")
public class UserDashboardServlet extends HttpServlet {

    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            // 获取最近的预约数据用于仪表板显示
            LocalDate startDate = LocalDate.now().minusMonths(3); // 3个月前
            LocalDate endDate = LocalDate.now().plusMonths(3); // 3个月后

            // 获取公众预约列表
            List<PublicReservation> publicReservations = publicReservationService.getReservationsByDateRange(
                    startDate, endDate, null);

            // 获取公务预约列表
            List<OfficialReservation> officialReservations = officialReservationService.getReservationsByDateRange(
                    startDate, endDate, null);

            // 统计公众预约数量
            int publicTotal = publicReservations.size();
            int publicPending = (int) publicReservations.stream().filter(r -> "pending".equals(r.getStatus())).count();
            int publicApproved = (int) publicReservations.stream().filter(r -> "approved".equals(r.getStatus()))
                    .count();
            int publicRejected = (int) publicReservations.stream().filter(r -> "rejected".equals(r.getStatus()))
                    .count();

            // 统计公务预约数量
            int officialTotal = officialReservations.size();
            int officialPending = (int) officialReservations.stream().filter(r -> "pending".equals(r.getStatus()))
                    .count();
            int officialApproved = (int) officialReservations.stream().filter(r -> "approved".equals(r.getStatus()))
                    .count();
            int officialRejected = (int) officialReservations.stream().filter(r -> "rejected".equals(r.getStatus()))
                    .count();

            // 总体统计
            int totalReservations = publicTotal + officialTotal;
            int totalPending = publicPending + officialPending;
            int totalApproved = publicApproved + officialApproved;
            int totalRejected = publicRejected + officialRejected;

            // 获取最近的预约记录（最多显示5条）
            List<Object> recentReservations = new ArrayList<>();
            publicReservations.stream()
                    .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                    .limit(3)
                    .forEach(recentReservations::add);
            officialReservations.stream()
                    .sorted((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()))
                    .limit(2)
                    .forEach(recentReservations::add);

            // 设置属性
            request.setAttribute("publicReservations", publicReservations);
            request.setAttribute("officialReservations", officialReservations);
            request.setAttribute("recentReservations", recentReservations);

            // 公众预约统计
            request.setAttribute("publicTotal", publicTotal);
            request.setAttribute("publicPending", publicPending);
            request.setAttribute("publicApproved", publicApproved);
            request.setAttribute("publicRejected", publicRejected);

            // 公务预约统计
            request.setAttribute("officialTotal", officialTotal);
            request.setAttribute("officialPending", officialPending);
            request.setAttribute("officialApproved", officialApproved);
            request.setAttribute("officialRejected", officialRejected);

            // 总体统计
            request.setAttribute("totalReservations", totalReservations);
            request.setAttribute("totalPending", totalPending);
            request.setAttribute("totalApproved", totalApproved);
            request.setAttribute("totalRejected", totalRejected);
            request.setAttribute("currentTime", LocalDateTime.now());

            request.getRequestDispatcher("/user/dashboard.jsp").forward(request, response);

        } catch (Exception e) {
            request.setAttribute("error", "加载仪表板数据时发生错误：" + e.getMessage());
            request.getRequestDispatcher("/user/dashboard.jsp").forward(request, response);
        }
    }
}
