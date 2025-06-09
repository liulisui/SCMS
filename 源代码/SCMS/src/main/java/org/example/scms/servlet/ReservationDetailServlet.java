package org.example.scms.servlet;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

import org.example.scms.dao.CampusDAO;
import org.example.scms.dao.DepartmentDAO;
import org.example.scms.model.Administrator;
import org.example.scms.model.Campus;
import org.example.scms.model.Department;
import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 预约详情查看Servlet
 * 为管理员提供预约详细信息查看功能
 */
@WebServlet("/admin/reservation-detail")
public class ReservationDetailServlet extends HttpServlet {
    
    private OfficialReservationService officialService;
    private PublicReservationService publicService;
    private CampusDAO campusDAO;
    private DepartmentDAO departmentDAO;
    
    @Override
    public void init() throws ServletException {
        this.officialService = new OfficialReservationService();
        this.publicService = new PublicReservationService();
        this.campusDAO = new CampusDAO();
        this.departmentDAO = new DepartmentDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 验证管理员登录
            HttpSession session = request.getSession();
            Administrator admin = (Administrator) session.getAttribute("admin");
            if (admin == null) {
                response.getWriter().write("<div style='text-align: center; color: red;'>未登录或登录已过期</div>");
                return;
            }
            
            // 获取参数
            String idStr = request.getParameter("id");
            String type = request.getParameter("type");
            
            if (idStr == null || type == null) {
                response.getWriter().write("<div style='text-align: center; color: red;'>参数错误</div>");
                return;
            }
            
            Long id = Long.parseLong(idStr);
            
            // 根据类型获取预约详情
            if ("official".equals(type)) {
                showOfficialReservationDetail(response, id);
            } else if ("public".equals(type)) {
                showPublicReservationDetail(response, id);
            } else {
                response.getWriter().write("<div style='text-align: center; color: red;'>无效的预约类型</div>");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("<div style='text-align: center; color: red;'>获取详情失败: " + e.getMessage() + "</div>");
        }
    }
    
    /**
     * 显示公务预约详情
     */
    private void showOfficialReservationDetail(HttpServletResponse response, Long id) throws IOException {
        OfficialReservation reservation = officialService.getReservationById(id);
        if (reservation == null) {
            response.getWriter().write("<div style='text-align: center; color: red;'>预约记录不存在</div>");
            return;
        }
        
        // 获取关联信息
        Campus campus = campusDAO.getCampusById(reservation.getCampusId());
        Department department = departmentDAO.getDepartmentById(reservation.getHostDepartmentId());
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder html = new StringBuilder();
        html.append("<div style='line-height: 1.6;'>");
        
        // 基本信息
        html.append("<h3 style='color: #333; border-bottom: 2px solid #667eea; padding-bottom: 0.5rem; margin-bottom: 1rem;'>公务预约详情</h3>");
        
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>预约编号：</strong>").append(reservation.getReservationNo()).append("</div>");
        html.append("<div><strong>预约状态：</strong><span style='color: ");
        switch (reservation.getStatus()) {
            case "pending": html.append("#ffc107'>待审核"); break;
            case "approved": html.append("#28a745'>已通过"); break;
            case "rejected": html.append("#dc3545'>已拒绝"); break;
            case "cancelled": html.append("#6c757d'>已取消"); break;
            case "completed": html.append("#17a2b8'>已完成"); break;
            default: html.append("#6c757d'>").append(reservation.getStatus());
        }
        html.append("</span></div>");
        html.append("</div>");
        
        // 访客信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>访客信息</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>访客姓名：</strong>").append(reservation.getVisitorName()).append("</div>");
        html.append("<div><strong>所在单位：</strong>").append(reservation.getVisitorOrganization() != null ? reservation.getVisitorOrganization() : "-").append("</div>");
        html.append("</div>");
        
        // 预约信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>预约信息</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>访问校区：</strong>").append(campus != null ? campus.getCampusName() : "未知校区").append("</div>");
        html.append("<div><strong>接待部门：</strong>").append(department != null ? department.getDepartmentName() : "未知部门").append("</div>");
        html.append("<div><strong>访问日期：</strong>").append(reservation.getVisitDate().format(dateFormatter)).append("</div>");
        html.append("<div><strong>访问时间：</strong>").append(reservation.getVisitTimeStart().format(timeFormatter))
               .append(" - ").append(reservation.getVisitTimeEnd().format(timeFormatter)).append("</div>");
        html.append("</div>");
        
        // 接待信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>接待信息</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>接待人：</strong>").append(reservation.getHostName()).append("</div>");
        html.append("<div><strong>联系电话：</strong>").append(reservation.getHostPhone()).append("</div>");
        html.append("<div style='grid-column: 1 / -1;'><strong>来访事由：</strong>").append(reservation.getVisitReason() != null ? reservation.getVisitReason() : "-").append("</div>");
        html.append("</div>");
        
        // 其他信息
        if (reservation.getAccompanyingPersons() != null && reservation.getAccompanyingPersons() > 0) {
            html.append("<div style='margin-bottom: 1rem;'><strong>随行人数：</strong>").append(reservation.getAccompanyingPersons()).append("人</div>");
        }
        if (reservation.getVehicleNumber() != null && !reservation.getVehicleNumber().trim().isEmpty()) {
            html.append("<div style='margin-bottom: 1rem;'><strong>车牌号：</strong>").append(reservation.getVehicleNumber()).append("</div>");
        }
        
        // 审批信息
        if (reservation.getApprovedAt() != null || (reservation.getApprovalComment() != null && !reservation.getApprovalComment().trim().isEmpty())) {
            html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>审批信息</h4>");
            if (reservation.getApprovedAt() != null) {
                html.append("<div style='margin-bottom: 1rem;'><strong>审批时间：</strong>").append(reservation.getApprovedAt().format(dateTimeFormatter)).append("</div>");
            }
            if (reservation.getApprovalComment() != null && !reservation.getApprovalComment().trim().isEmpty()) {
                html.append("<div style='margin-bottom: 1rem;'><strong>审批意见：</strong>").append(reservation.getApprovalComment()).append("</div>");
            }
        }
        
        // 时间信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>时间记录</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>申请时间：</strong>").append(reservation.getCreatedAt().format(dateTimeFormatter)).append("</div>");
        html.append("<div><strong>更新时间：</strong>").append(reservation.getUpdatedAt().format(dateTimeFormatter)).append("</div>");
        if (reservation.getCheckInTime() != null) {
            html.append("<div><strong>入校时间：</strong>").append(reservation.getCheckInTime().format(dateTimeFormatter)).append("</div>");
        }
        if (reservation.getCheckOutTime() != null) {
            html.append("<div><strong>离校时间：</strong>").append(reservation.getCheckOutTime().format(dateTimeFormatter)).append("</div>");
        }
        html.append("</div>");
        
        html.append("</div>");
        
        response.getWriter().write(html.toString());
    }
    
    /**
     * 显示公众预约详情
     */
    private void showPublicReservationDetail(HttpServletResponse response, Long id) throws IOException {
        PublicReservation reservation = publicService.getReservationById(id);
        if (reservation == null) {
            response.getWriter().write("<div style='text-align: center; color: red;'>预约记录不存在</div>");
            return;
        }
        
        // 获取关联信息
        Campus campus = campusDAO.getCampusById(reservation.getCampusId());
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        StringBuilder html = new StringBuilder();
        html.append("<div style='line-height: 1.6;'>");
        
        // 基本信息
        html.append("<h3 style='color: #333; border-bottom: 2px solid #667eea; padding-bottom: 0.5rem; margin-bottom: 1rem;'>社会公众预约详情</h3>");
        
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>预约编号：</strong>").append(reservation.getReservationNo()).append("</div>");
        html.append("<div><strong>预约状态：</strong><span style='color: ");
        switch (reservation.getStatus()) {
            case "pending": html.append("#ffc107'>待审核"); break;
            case "approved": html.append("#28a745'>已通过"); break;
            case "rejected": html.append("#dc3545'>已拒绝"); break;
            case "cancelled": html.append("#6c757d'>已取消"); break;
            case "completed": html.append("#17a2b8'>已完成"); break;
            default: html.append("#6c757d'>").append(reservation.getStatus());
        }
        html.append("</span></div>");
        html.append("</div>");
        
        // 访客信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>访客信息</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>访客姓名：</strong>").append(reservation.getVisitorName()).append("</div>");
        if (reservation.getAccompanyingPersons() != null && reservation.getAccompanyingPersons() > 0) {
            html.append("<div><strong>随行人数：</strong>").append(reservation.getAccompanyingPersons()).append("人</div>");
        }
        html.append("</div>");
        
        // 预约信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>预约信息</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>访问校区：</strong>").append(campus != null ? campus.getCampusName() : "未知校区").append("</div>");
        html.append("<div><strong>访问日期：</strong>").append(reservation.getVisitDate().format(dateFormatter)).append("</div>");        html.append("<div><strong>访问时间：</strong>").append(reservation.getVisitTimeStart().format(timeFormatter))
               .append(" - ").append(reservation.getVisitTimeEnd().format(timeFormatter)).append("</div>");
        html.append("<div style='grid-column: 1 / -1;'><strong>访问目的：</strong>").append(reservation.getVisitReason() != null ? reservation.getVisitReason() : "-").append("</div>");
        html.append("</div>");
        
        // 审批信息
        if (reservation.getApprovedAt() != null || (reservation.getApprovalComment() != null && !reservation.getApprovalComment().trim().isEmpty())) {
            html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>审批信息</h4>");
            if (reservation.getApprovedAt() != null) {
                html.append("<div style='margin-bottom: 1rem;'><strong>审批时间：</strong>").append(reservation.getApprovedAt().format(dateTimeFormatter)).append("</div>");
            }
            if (reservation.getApprovalComment() != null && !reservation.getApprovalComment().trim().isEmpty()) {
                html.append("<div style='margin-bottom: 1rem;'><strong>审批意见：</strong>").append(reservation.getApprovalComment()).append("</div>");
            }
        }
        
        // 时间信息
        html.append("<h4 style='color: #555; margin: 1.5rem 0 1rem 0;'>时间记录</h4>");
        html.append("<div style='display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-bottom: 1.5rem;'>");
        html.append("<div><strong>申请时间：</strong>").append(reservation.getCreatedAt().format(dateTimeFormatter)).append("</div>");
        html.append("<div><strong>更新时间：</strong>").append(reservation.getUpdatedAt().format(dateTimeFormatter)).append("</div>");
        if (reservation.getCheckInTime() != null) {
            html.append("<div><strong>入校时间：</strong>").append(reservation.getCheckInTime().format(dateTimeFormatter)).append("</div>");
        }
        if (reservation.getCheckOutTime() != null) {
            html.append("<div><strong>离校时间：</strong>").append(reservation.getCheckOutTime().format(dateTimeFormatter)).append("</div>");
        }
        html.append("</div>");
        
        html.append("</div>");
        
        response.getWriter().write(html.toString());
    }
}