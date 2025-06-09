package org.example.scms.servlet;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.scms.model.Administrator;
import org.example.scms.model.OfficialReservation;
import org.example.scms.model.PublicReservation;
import org.example.scms.service.CampusService;
import org.example.scms.service.OfficialReservationService;
import org.example.scms.service.PublicReservationService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * 管理员仪表板Servlet
 */
@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends HttpServlet {
    private final PublicReservationService publicReservationService = new PublicReservationService();
    private final OfficialReservationService officialReservationService = new OfficialReservationService();
    private final CampusService campusService = new CampusService();    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Administrator admin = (Administrator) session.getAttribute("admin");

        if (admin == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }        try {
            // 获取统计数据
            LocalDate today = LocalDate.now();
            LocalDate startOfMonth = today.withDayOfMonth(1);
            LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

            Map<String, Integer> publicStats = publicReservationService.countReservationsByStatus(startOfMonth,
                    endOfMonth);
            Map<String, Integer> officialStats = officialReservationService.countReservationsByStatus(startOfMonth,
                    endOfMonth);

            // 根据管理员权限获取待审核的预约
            List<PublicReservation> pendingPublicReservations;
            List<OfficialReservation> pendingOfficialReservations;
              // 检查管理员类型和部门权限
            String adminType = admin.getAdminType();
            Long departmentId = admin.getDepartmentId();
            
            System.out.println("=== 调试仪表板数据获取 ===");
            System.out.println("管理员类型: " + adminType);
            System.out.println("管理员部门ID: " + departmentId);
              if ("system_admin".equals(adminType) || "school_admin".equals(adminType)) {
                // 系统管理员和校级管理员可以看到所有预约
                System.out.println("获取所有待审核预约...");
                pendingPublicReservations = publicReservationService.getPendingReservations();
                pendingOfficialReservations = officialReservationService.getPendingReservations();
                System.out.println("公众预约数量: " + (pendingPublicReservations != null ? pendingPublicReservations.size() : "null"));
                System.out.println("公务预约数量: " + (pendingOfficialReservations != null ? pendingOfficialReservations.size() : "null"));
            } else if ("department_admin".equals(adminType) && departmentId != null) {
                // 部门管理员只能看到本部门的公务预约，不能看到公众预约
                System.out.println("获取部门 " + departmentId + " 的待审核预约...");
                pendingPublicReservations = new ArrayList<>(); // 部门管理员不能审批公众预约
                pendingOfficialReservations = officialReservationService.getPendingReservationsByDepartment(departmentId);
                System.out.println("部门公务预约数量: " + (pendingOfficialReservations != null ? pendingOfficialReservations.size() : "null"));
            } else {
                // 其他情况不显示任何待审核预约
                System.out.println("管理员类型不匹配，不显示待审核预约");
                pendingPublicReservations = new ArrayList<>();
                pendingOfficialReservations = new ArrayList<>();
            }// 根据管理员权限获取今日预约 - 使用日期范围查询
            List<PublicReservation> todayPublicReservations;
            List<OfficialReservation> todayOfficialReservations;
              if ("system_admin".equals(adminType) || "school_admin".equals(adminType)) {
                // 系统管理员和校级管理员可以看到所有今日预约
                todayPublicReservations = publicReservationService.getReservationsByDateRange(today, today, "approved");
                todayOfficialReservations = officialReservationService.getReservationsByDateRange(today, today, "approved");
            } else if ("department_admin".equals(adminType) && departmentId != null) {
                // 部门管理员只能看到本部门的今日公务预约
                todayPublicReservations = new ArrayList<>();
                // 获取本部门今日已批准的公务预约
                List<OfficialReservation> allTodayOfficial = officialReservationService.getReservationsByDateRange(today, today, "approved");
                todayOfficialReservations = allTodayOfficial.stream()
                    .filter(r -> departmentId.equals(r.getHostDepartmentId()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            } else {
                todayPublicReservations = new ArrayList<>();
                todayOfficialReservations = new ArrayList<>();
            }

            // 根据管理员权限获取最近活动 - 使用日期范围查询最近7天的预约
            LocalDate weekAgo = today.minusDays(7);
            List<PublicReservation> recentPublicReservations;
            List<OfficialReservation> recentOfficialReservations;
              if ("system_admin".equals(adminType) || "school_admin".equals(adminType)) {
                // 系统管理员和校级管理员可以看到所有最近活动
                recentPublicReservations = publicReservationService.getReservationsByDateRange(weekAgo, today, null);
                recentOfficialReservations = officialReservationService.getReservationsByDateRange(weekAgo, today, null);
            } else if ("department_admin".equals(adminType) && departmentId != null) {
                // 部门管理员只能看到本部门的最近公务预约活动
                recentPublicReservations = new ArrayList<>();
                // 获取本部门最近的公务预约
                List<OfficialReservation> allRecentOfficial = officialReservationService.getReservationsByDateRange(weekAgo, today, null);
                recentOfficialReservations = allRecentOfficial.stream()
                    .filter(r -> departmentId.equals(r.getHostDepartmentId()))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            } else {
                recentPublicReservations = new ArrayList<>();
                recentOfficialReservations = new ArrayList<>();
            }            // 为JSP创建统一的数据结构
            List<Map<String, Object>> pendingReservations = new ArrayList<>();
            List<Map<String, Object>> todayReservations = new ArrayList<>();
            List<Map<String, Object>> recentActivity = new ArrayList<>();
            
            System.out.println("=== 开始转换待审核预约数据 ===");
            
            // 处理待审核预约
            for (PublicReservation pr : pendingPublicReservations) {
                System.out.println("处理公众预约: ID=" + pr.getId() + ", 姓名=" + pr.getVisitorName());
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("id", pr.getId());
                reservation.put("name", pr.getVisitorName());
                // 使用身份证哈希的前6位和后4位来模拟脱敏显示
                String idCardDisplay = "****";
                if (pr.getVisitorIdCardHash() != null && pr.getVisitorIdCardHash().length() >= 10) {
                    idCardDisplay = pr.getVisitorIdCardHash().substring(0, 6) + "****"
                            + pr.getVisitorIdCardHash().substring(pr.getVisitorIdCardHash().length() - 4);
                }
                reservation.put("idCard", idCardDisplay);
                reservation.put("reservationDate", pr.getVisitDate().atTime(pr.getVisitTimeStart()));
                reservation.put("purpose", pr.getVisitReason());
                reservation.put("status", pr.getStatus());
                reservation.put("createdAt", pr.getCreatedAt());
                reservation.put("type", "public");
                pendingReservations.add(reservation);
            }

            for (OfficialReservation or : pendingOfficialReservations) {
                System.out.println("处理公务预约: ID=" + or.getId() + ", 姓名=" + or.getVisitorName());
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("id", or.getId());
                reservation.put("name", or.getVisitorName());
                // 使用身份证哈希的前6位和后4位来模拟脱敏显示
                String idCardDisplay = "****";
                if (or.getVisitorIdCardHash() != null && or.getVisitorIdCardHash().length() >= 10) {
                    idCardDisplay = or.getVisitorIdCardHash().substring(0, 6) + "****"
                            + or.getVisitorIdCardHash().substring(or.getVisitorIdCardHash().length() - 4);
                }
                reservation.put("idCard", idCardDisplay);
                reservation.put("reservationDate", or.getVisitDate().atTime(or.getVisitTimeStart()));
                reservation.put("purpose", or.getVisitReason());
                reservation.put("status", or.getStatus());
                reservation.put("createdAt", or.getCreatedAt());
                reservation.put("type", "official");
                pendingReservations.add(reservation);
            }
            // 处理今日预约
            for (PublicReservation pr : todayPublicReservations) {
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("id", pr.getId());
                reservation.put("name", pr.getVisitorName());
                String idCardDisplay = "****";
                if (pr.getVisitorIdCardHash() != null && pr.getVisitorIdCardHash().length() >= 10) {
                    idCardDisplay = pr.getVisitorIdCardHash().substring(0, 6) + "****"
                            + pr.getVisitorIdCardHash().substring(pr.getVisitorIdCardHash().length() - 4);
                }
                reservation.put("idCard", idCardDisplay);
                reservation.put("reservationDate", pr.getVisitDate().atTime(pr.getVisitTimeStart()));
                reservation.put("purpose", pr.getVisitReason());
                reservation.put("status", pr.getStatus());
                reservation.put("createdAt", pr.getCreatedAt());
                reservation.put("type", "public");
                todayReservations.add(reservation);
            }

            for (OfficialReservation or : todayOfficialReservations) {
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("id", or.getId());
                reservation.put("name", or.getVisitorName());
                String idCardDisplay = "****";
                if (or.getVisitorIdCardHash() != null && or.getVisitorIdCardHash().length() >= 10) {
                    idCardDisplay = or.getVisitorIdCardHash().substring(0, 6) + "****"
                            + or.getVisitorIdCardHash().substring(or.getVisitorIdCardHash().length() - 4);
                }
                reservation.put("idCard", idCardDisplay);
                reservation.put("reservationDate", or.getVisitDate().atTime(or.getVisitTimeStart()));
                reservation.put("purpose", or.getVisitReason());
                reservation.put("status", or.getStatus());
                reservation.put("createdAt", or.getCreatedAt());
                reservation.put("type", "official");
                todayReservations.add(reservation);
            }
            // 处理最近活动
            for (PublicReservation pr : recentPublicReservations) {
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("id", pr.getId());
                reservation.put("name", pr.getVisitorName());
                String idCardDisplay = "****";
                if (pr.getVisitorIdCardHash() != null && pr.getVisitorIdCardHash().length() >= 10) {
                    idCardDisplay = pr.getVisitorIdCardHash().substring(0, 6) + "****"
                            + pr.getVisitorIdCardHash().substring(pr.getVisitorIdCardHash().length() - 4);
                }
                reservation.put("idCard", idCardDisplay);
                reservation.put("reservationDate", pr.getVisitDate().atTime(pr.getVisitTimeStart()));
                reservation.put("purpose", pr.getVisitReason());
                reservation.put("status", pr.getStatus());
                reservation.put("createdAt", pr.getCreatedAt());
                reservation.put("type", "public");
                recentActivity.add(reservation);
            }

            for (OfficialReservation or : recentOfficialReservations) {
                Map<String, Object> reservation = new HashMap<>();
                reservation.put("id", or.getId());
                reservation.put("name", or.getVisitorName());
                String idCardDisplay = "****";
                if (or.getVisitorIdCardHash() != null && or.getVisitorIdCardHash().length() >= 10) {
                    idCardDisplay = or.getVisitorIdCardHash().substring(0, 6) + "****"
                            + or.getVisitorIdCardHash().substring(or.getVisitorIdCardHash().length() - 4);
                }
                reservation.put("idCard", idCardDisplay);
                reservation.put("reservationDate", or.getVisitDate().atTime(or.getVisitTimeStart()));
                reservation.put("purpose", or.getVisitReason());
                reservation.put("status", or.getStatus());
                reservation.put("createdAt", or.getCreatedAt());
                reservation.put("type", "official");
                recentActivity.add(reservation);
            }

            // 计算统计数据
            int totalPublic = (publicStats.getOrDefault("pending", 0) +
                    publicStats.getOrDefault("approved", 0) +
                    publicStats.getOrDefault("rejected", 0));
            int totalOfficial = (officialStats.getOrDefault("pending", 0) +
                    officialStats.getOrDefault("approved", 0) +
                    officialStats.getOrDefault("rejected", 0));

            int totalReservations = totalPublic + totalOfficial;
            int todayCount = todayReservations.size();
            int pendingCount = pendingReservations.size();
            int approvedCount = publicStats.getOrDefault("approved", 0) + officialStats.getOrDefault("approved", 0);

            // 获取校区数量
            int totalCampuses = campusService.getAllCampuses().size();
            int activeCampuses = campusService.getActiveCampuses().size();            // 设置属性供JSP使用
            System.out.println("=== 设置JSP属性 ===");
            System.out.println("待审核预约数量: " + pendingReservations.size());
            System.out.println("今日预约数量: " + todayReservations.size());
            System.out.println("最近活动数量: " + recentActivity.size());
            
            request.setAttribute("pendingReservations", pendingReservations);
            request.setAttribute("todayReservations", todayReservations);
            request.setAttribute("recentActivity", recentActivity);
            request.setAttribute("totalReservations", totalReservations);
            request.setAttribute("todayCount", todayCount);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("approvedCount", approvedCount);
            request.setAttribute("totalCampuses", totalCampuses);
            request.setAttribute("activeCampuses", activeCampuses);
            request.setAttribute("admin", admin);
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "获取仪表板数据失败: " + e.getMessage());
            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
        }
    }
}
