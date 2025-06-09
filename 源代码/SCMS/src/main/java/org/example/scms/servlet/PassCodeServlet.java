package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;
import org.example.scms.util.AuditLogUtil;
import org.example.scms.util.QRCodeUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 通行码显示Servlet
 */
@WebServlet("/user/passcode")
public class PassCodeServlet extends HttpServlet {

    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String type = request.getParameter("type"); // public or official
        String idStr = request.getParameter("id");

        if (type == null || idStr == null) {
            response.sendRedirect(request.getContextPath() + "/user/dashboard");
            return;
        }

        try {
            Long reservationId = Long.valueOf(idStr);

            switch (type) {
                case "public":
                    handlePublicReservationPassCode(request, response, reservationId);
                    break;
                case "official":
                    handleOfficialReservationPassCode(request, response, reservationId);
                    break;
                default:
                    response.sendRedirect(request.getContextPath() + "/user/dashboard");
            }

        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/user/dashboard");
        }
    }    /**
     * 处理社会公众预约的通行码
     */
    private void handlePublicReservationPassCode(HttpServletRequest request, HttpServletResponse response,
            Long reservationId)
            throws ServletException, IOException {

        PublicReservation reservation = publicReservationService.getReservationById(reservationId);

        if (reservation == null) {
            request.setAttribute("error", "未找到预约记录");
            request.getRequestDispatcher("/user/passcode.jsp").forward(request, response);
            return;
        }

        // 检查预约状态，只有已通过审核的预约才能查看通行码
        if (!"approved".equals(reservation.getStatus())) {
            String statusText = "pending".equals(reservation.getStatus()) ? "等待审核" : 
                               "rejected".equals(reservation.getStatus()) ? "已被拒绝" : "状态异常";
            request.setAttribute("error", "预约" + statusText + "，无法查看通行码");
            request.setAttribute("reservation", reservation);
            request.setAttribute("status", reservation.getStatus());
            request.getRequestDispatcher("/user/passcode.jsp").forward(request, response);
            return;
        }

        processPassCodeGeneration(request, response, reservation, "public");
    }    /**
     * 处理公务预约的通行码
     */
    private void handleOfficialReservationPassCode(HttpServletRequest request, HttpServletResponse response,
            Long reservationId)
            throws ServletException, IOException {

        OfficialReservation reservation = officialReservationService.getReservationById(reservationId);

        if (reservation == null) {
            request.setAttribute("error", "未找到预约记录");
            request.getRequestDispatcher("/user/passcode.jsp").forward(request, response);
            return;
        }

        // 检查预约状态，只有已通过审核的预约才能查看通行码
        if (!"approved".equals(reservation.getStatus())) {
            String statusText = "pending".equals(reservation.getStatus()) ? "等待审核" : 
                               "rejected".equals(reservation.getStatus()) ? "已被拒绝" : "状态异常";
            request.setAttribute("error", "预约" + statusText + "，无法查看通行码");
            request.setAttribute("reservation", reservation);
            request.setAttribute("status", reservation.getStatus());
            request.getRequestDispatcher("/user/passcode.jsp").forward(request, response);
            return;
        }

        processPassCodeGeneration(request, response, reservation, "official");
    }    /**
     * 处理通行码生成逻辑 - 通用方法
     */
    private void processPassCodeGeneration(HttpServletRequest request, HttpServletResponse response,
            Object reservation, String type) throws ServletException, IOException {
        
        if (reservation == null) {
            request.setAttribute("error", "预约信息不存在");
            request.getRequestDispatcher("/user/passcode.jsp").forward(request, response);
            return;
        }

        LocalDateTime visitDateTime;
        LocalDateTime endDateTime;
        String qrCodeData;

        if ("public".equals(type)) {
            PublicReservation pubRes = (PublicReservation) reservation;
            visitDateTime = LocalDateTime.of(pubRes.getVisitDate(), pubRes.getVisitTimeStart());
            endDateTime = LocalDateTime.of(pubRes.getVisitDate(), pubRes.getVisitTimeEnd());
            qrCodeData = pubRes.getQrCodeData();
        } else {
            OfficialReservation offRes = (OfficialReservation) reservation;
            visitDateTime = LocalDateTime.of(offRes.getVisitDate(), offRes.getVisitTimeStart());
            endDateTime = LocalDateTime.of(offRes.getVisitDate(), offRes.getVisitTimeEnd());
            qrCodeData = offRes.getQrCodeData();
        }

        // 时间验证和状态判断
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = visitDateTime.minusMinutes(30); // 提前30分钟生效        // 生成格式化时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String validTime = visitDateTime.format(formatter) + " 至 " + endDateTime.format(formatter);
        
        // 判断通行码状态
        String status;
        String statusColor;
        String statusText;
        if (now.isBefore(startTime)) {
            status = "early";
            statusColor = "#ffc107"; // 黄色
            statusText = "还未生效";
        } else if (now.isAfter(startTime) && now.isBefore(endDateTime)) {
            status = "valid";
            statusColor = "#28a745"; // 绿色
            statusText = "有效";
        } else {
            status = "expired";
            statusColor = "#dc3545"; // 红色
            statusText = "已过期";
        }
          // 生成QR码（如果数据库中没有存储或需要重新生成）
        String qrCode;
        if (qrCodeData != null && !qrCodeData.isEmpty()) {
            // 根据状态使用不同颜色生成二维码
            qrCode = QRCodeUtil.generateQRCodeByStatus(qrCodeData, 200, 200, status);
        } else {
            // 如果没有QR码数据，则根据预约信息生成一个新的
            Long reservationId = (reservation instanceof PublicReservation) ? 
                ((PublicReservation) reservation).getId() : ((OfficialReservation) reservation).getId();
            String content = "校园通行码\n预约ID：" + reservationId +
                "\n类型：" + (type.equals("public") ? "校园参观" : "公务来访") + 
                "\n有效期：" + validTime;
            
            // 使用状态对应的颜色
            qrCode = QRCodeUtil.generateQRCodeByStatus(content, 200, 200, status);
        }
        // 调试输出
        System.out.println("PassCodeServlet - QR码数据: " + qrCodeData);
        System.out.println("PassCodeServlet - 状态: " + status + " (有效时应为紫色)");
        System.out.println("PassCodeServlet - QR码生成结果: " + (qrCode != null ? "成功" : "失败(null)"));        // 记录查看通行码的审计日志
        Long reservationId = (reservation instanceof PublicReservation) ? 
            ((PublicReservation) reservation).getId() : ((OfficialReservation) reservation).getId();
        Map<String, Object> viewData = new HashMap<>();
        viewData.put("reservationType", type);
        viewData.put("reservationId", reservationId);
        viewData.put("passCodeStatus", status);
        viewData.put("statusText", statusText);
        AuditLogUtil.logView(request, "passcode_access", reservationId, viewData);

        request.setAttribute("reservation", reservation);
        request.setAttribute("reservationType", type);
        request.setAttribute("qrCodeData", qrCode);  // 修改属性名为qrCodeData以匹配JSP中的获取方式
        request.setAttribute("status", status);
        request.setAttribute("statusColor", statusColor);
        request.setAttribute("statusText", statusText);
        request.setAttribute("validTime", validTime);
        request.getRequestDispatcher("/user/passcode.jsp").forward(request, response);
    }
}
