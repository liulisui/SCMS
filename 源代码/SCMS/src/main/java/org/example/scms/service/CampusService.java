package org.example.scms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.example.scms.dao.AuditLogDAO;
import org.example.scms.dao.CampusDAO;
import org.example.scms.dao.OfficialReservationDAO;
import org.example.scms.dao.PublicReservationDAO;
import org.example.scms.model.AuditLog;
import org.example.scms.model.Campus;

/**
 * 校区服务类
 * 处理校区管理相关的业务逻辑
 */
public class CampusService {
    private final CampusDAO campusDAO;
    private final PublicReservationDAO publicReservationDAO;
    private final OfficialReservationDAO officialReservationDAO;
    private final AuditLogDAO auditLogDAO;

    public CampusService() {
        this.campusDAO = new CampusDAO();
        this.publicReservationDAO = new PublicReservationDAO();
        this.officialReservationDAO = new OfficialReservationDAO();
        this.auditLogDAO = new AuditLogDAO();
    }

    /**
     * 添加校区
     */
    public Campus addCampus(String campusCode, String campusName, String address, String description,
            Long operatorId, String ipAddress, String userAgent) {
        try {
            // 验证校区编码是否已存在
            Campus existingCampus = campusDAO.getCampusByCode(campusCode);
            if (existingCampus != null) {
                logAuditEvent(operatorId, "ADD_CAMPUS_FAILED", "Campus", null,
                        "校区编码已存在: " + campusCode, ipAddress, userAgent);
                return null;
            }

            // 验证必填字段
            if (campusCode == null || campusCode.trim().isEmpty() ||
                    campusName == null || campusName.trim().isEmpty()) {
                logAuditEvent(operatorId, "ADD_CAMPUS_FAILED", "Campus", null,
                        "校区编码和名称不能为空", ipAddress, userAgent);
                return null;
            }

            // 验证校区编码格式（只允许字母、数字和下划线）
            if (!isValidCampusCode(campusCode)) {
                logAuditEvent(operatorId, "ADD_CAMPUS_FAILED", "Campus", null,
                        "校区编码格式不正确: " + campusCode, ipAddress, userAgent);
                return null;
            }

            Campus campus = new Campus(campusCode.trim(), campusName.trim(),
                    address != null ? address.trim() : "",
                    description != null ? description.trim() : "");

            Campus savedCampus = campusDAO.addCampus(campus);
            if (savedCampus != null) {
                logAuditEvent(operatorId, "ADD_CAMPUS", "Campus", savedCampus.getId(),
                        "添加校区: " + campusName + " (" + campusCode + ")", ipAddress, userAgent);
                return savedCampus;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "ADD_CAMPUS_FAILED", "Campus", null,
                    "添加校区失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return null;
    }

    /**
     * 更新校区信息
     */
    public boolean updateCampus(Long campusId, String campusName, String address, String description,
            Long operatorId, String ipAddress, String userAgent) {
        try {
            Campus campus = campusDAO.getCampusById(campusId);
            if (campus == null) {
                logAuditEvent(operatorId, "UPDATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区不存在", ipAddress, userAgent);
                return false;
            }

            // 验证必填字段
            if (campusName == null || campusName.trim().isEmpty()) {
                logAuditEvent(operatorId, "UPDATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区名称不能为空", ipAddress, userAgent);
                return false;
            }

            // 记录修改前的信息
            String oldInfo = String.format("名称: %s, 地址: %s, 描述: %s",
                    campus.getCampusName(), campus.getAddress(), campus.getDescription());

            // 更新校区信息
            campus.setCampusName(campusName.trim());
            campus.setAddress(address != null ? address.trim() : "");
            campus.setDescription(description != null ? description.trim() : "");

            boolean success = campusDAO.updateCampus(campus);
            if (success) {
                String newInfo = String.format("名称: %s, 地址: %s, 描述: %s",
                        campusName.trim(),
                        address != null ? address.trim() : "",
                        description != null ? description.trim() : "");
                logAuditEvent(operatorId, "UPDATE_CAMPUS", "Campus", campusId,
                        "更新校区信息 - 修改前: " + oldInfo + " 修改后: " + newInfo, ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "UPDATE_CAMPUS_FAILED", "Campus", campusId,
                    "更新校区失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 启用校区
     */
    public boolean activateCampus(Long campusId, Long operatorId, String ipAddress, String userAgent) {
        try {
            Campus campus = campusDAO.getCampusById(campusId);
            if (campus == null) {
                logAuditEvent(operatorId, "ACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区不存在", ipAddress, userAgent);
                return false;
            }

            if ("active".equals(campus.getStatus())) {
                logAuditEvent(operatorId, "ACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区已经是激活状态", ipAddress, userAgent);
                return false;
            }

            campus.setStatus("active");
            boolean success = campusDAO.updateCampus(campus);
            if (success) {
                logAuditEvent(operatorId, "ACTIVATE_CAMPUS", "Campus", campusId,
                        "启用校区: " + campus.getCampusName(), ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "ACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                    "启用校区失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 禁用校区
     */
    public boolean deactivateCampus(Long campusId, Long operatorId, String ipAddress, String userAgent) {
        try {
            Campus campus = campusDAO.getCampusById(campusId);
            if (campus == null) {
                logAuditEvent(operatorId, "DEACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区不存在", ipAddress, userAgent);
                return false;
            }

            if ("inactive".equals(campus.getStatus())) {
                logAuditEvent(operatorId, "DEACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区已经是未激活状态", ipAddress, userAgent);
                return false;
            }

            // 检查是否有未完成的预约
            boolean hasActiveReservations = hasActiveReservations(campusId);
            if (hasActiveReservations) {
                logAuditEvent(operatorId, "DEACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                        "校区有未完成的预约，无法禁用", ipAddress, userAgent);
                return false;
            }

            campus.setStatus("inactive");
            boolean success = campusDAO.updateCampus(campus);
            if (success) {
                logAuditEvent(operatorId, "DEACTIVATE_CAMPUS", "Campus", campusId,
                        "禁用校区: " + campus.getCampusName(), ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "DEACTIVATE_CAMPUS_FAILED", "Campus", campusId,
                    "禁用校区失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 删除校区
     */
    public boolean deleteCampus(Long campusId, Long operatorId, String ipAddress, String userAgent) {
        try {
            Campus campus = campusDAO.getCampusById(campusId);
            if (campus == null) {
                logAuditEvent(operatorId, "DELETE_CAMPUS_FAILED", "Campus", campusId,
                        "校区不存在", ipAddress, userAgent);
                return false;
            }

            // 检查是否有任何预约记录
            boolean hasAnyReservations = hasAnyReservations(campusId);
            if (hasAnyReservations) {
                logAuditEvent(operatorId, "DELETE_CAMPUS_FAILED", "Campus", campusId,
                        "校区有预约记录，无法删除", ipAddress, userAgent);
                return false;
            }

            boolean success = campusDAO.deleteCampus(campusId);
            if (success) {
                logAuditEvent(operatorId, "DELETE_CAMPUS", "Campus", campusId,
                        "删除校区: " + campus.getCampusName() + " (" + campus.getCampusCode() + ")",
                        ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "DELETE_CAMPUS_FAILED", "Campus", campusId,
                    "删除校区失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 根据ID获取校区
     */
    public Campus getCampusById(Long id) {
        return campusDAO.getCampusById(id);
    }

    /**
     * 根据编码获取校区
     */
    public Campus getCampusByCode(String campusCode) {
        return campusDAO.getCampusByCode(campusCode);
    }

    /**
     * 获取所有校区
     */
    public List<Campus> getAllCampuses() {
        return campusDAO.getAllCampuses();
    }

    /**
     * 获取所有激活状态的校区
     */
    public List<Campus> getActiveCampuses() {
        return campusDAO.getActiveCampuses();
    }

    /**
     * 获取校区预约统计信息
     */
    public Map<String, Object> getCampusReservationStatistics(Long campusId, LocalDate startDate, LocalDate endDate) {
        try {
            Campus campus = campusDAO.getCampusById(campusId);
            if (campus == null) {
                return null;
            }

            Map<String, Object> statistics = new java.util.HashMap<>();

            // 基本信息
            statistics.put("campus", campus);
            statistics.put("startDate", startDate);
            statistics.put("endDate", endDate);

            // 社会公众预约统计
            Map<String, Integer> publicStats = publicReservationDAO.countReservationsByStatus(startDate, endDate);
            statistics.put("publicReservationStats", publicStats);

            // 公务预约统计
            Map<String, Integer> officialStats = officialReservationDAO.countReservationsByStatus(startDate, endDate);
            statistics.put("officialReservationStats", officialStats);

            // 总预约数
            int totalPublic = publicStats.values().stream().mapToInt(Integer::intValue).sum();
            int totalOfficial = officialStats.values().stream().mapToInt(Integer::intValue).sum();
            statistics.put("totalPublicReservations", totalPublic);
            statistics.put("totalOfficialReservations", totalOfficial);
            statistics.put("totalReservations", totalPublic + totalOfficial);

            return statistics;

        } catch (Exception e) {
            System.err.println("Failed to get campus statistics: " + e.getMessage());
            return null;
        }
    }

    /**
     * 检查校区是否有激活状态的预约
     */
    private boolean hasActiveReservations(Long campusId) {
        try {
            // 检查社会公众预约
            List<org.example.scms.model.PublicReservation> publicReservations = publicReservationDAO
                    .getReservationsByCampus(campusId, null);
            boolean hasActivePublic = publicReservations.stream()
                    .anyMatch(r -> "pending".equals(r.getStatus()) ||
                            "approved".equals(r.getStatus()) ||
                            (r.getCheckInTime() != null && r.getCheckOutTime() == null));

            if (hasActivePublic) {
                return true;
            }

            // 检查公务预约
            List<org.example.scms.model.OfficialReservation> officialReservations = officialReservationDAO
                    .getReservationsByCampus(campusId, null);
            boolean hasActiveOfficial = officialReservations.stream()
                    .anyMatch(r -> "pending".equals(r.getStatus()) ||
                            "approved".equals(r.getStatus()) ||
                            (r.getCheckInTime() != null && r.getCheckOutTime() == null));

            return hasActiveOfficial;

        } catch (Exception e) {
            System.err.println("Failed to check active reservations: " + e.getMessage());
            return true; // 保守策略，有错误时认为有活跃预约
        }
    }

    /**
     * 检查校区是否有任何预约记录
     */
    private boolean hasAnyReservations(Long campusId) {
        try {
            List<org.example.scms.model.PublicReservation> publicReservations = publicReservationDAO
                    .getReservationsByCampus(campusId, null);
            if (!publicReservations.isEmpty()) {
                return true;
            }

            List<org.example.scms.model.OfficialReservation> officialReservations = officialReservationDAO
                    .getReservationsByCampus(campusId, null);
            return !officialReservations.isEmpty();

        } catch (Exception e) {
            System.err.println("Failed to check any reservations: " + e.getMessage());
            return true; // 保守策略，有错误时认为有预约记录
        }
    }

    /**
     * 验证校区编码格式
     */
    private boolean isValidCampusCode(String campusCode) {
        if (campusCode == null || campusCode.trim().isEmpty()) {
            return false;
        }
        // 只允许字母、数字和下划线，长度2-20位
        String regex = "^[a-zA-Z0-9_]{2,20}$";
        return campusCode.matches(regex);
    }

    /**
     * 记录审计日志
     */
    private void logAuditEvent(Long adminId, String operation, String resourceType, Long resourceId,
            String details, String ipAddress, String userAgent) {
        try {
            AuditLog auditLog = new AuditLog(adminId, operation, resourceType, resourceId,
                    details, null, null, ipAddress, userAgent);
            auditLogDAO.addAuditLog(auditLog);
        } catch (Exception e) {
            // 记录审计日志失败，可以记录到文件日志或其他方式
            System.err.println("Failed to log audit event: " + e.getMessage());
        }
    }
}
