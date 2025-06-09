package org.example.scms.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.example.scms.dao.AuditLogDAO;
import org.example.scms.dao.DepartmentDAO;
import org.example.scms.dao.ReservationAuthorizationDAO;
import org.example.scms.model.AuditLog;
import org.example.scms.model.Department;
import org.example.scms.model.ReservationAuthorization;

/**
 * 授权服务类
 * 处理预约授权相关的业务逻辑
 */
public class AuthorizationService {
    private final ReservationAuthorizationDAO authorizationDAO;
    private final DepartmentDAO departmentDAO;
    private final AuditLogDAO auditLogDAO;

    public AuthorizationService() {
        this.authorizationDAO = new ReservationAuthorizationDAO();
        this.departmentDAO = new DepartmentDAO();
        this.auditLogDAO = new AuditLogDAO();
    }

    /**
     * 授予部门预约权限
     */
    public boolean grantAuthorization(Long departmentId, String reservationType, Long operatorId,
            String description, String ipAddress, String userAgent) {
        try {
            // 验证部门是否存在
            Department department = departmentDAO.getDepartmentById(departmentId);
            if (department == null) {
                logAuditEvent(operatorId, "GRANT_AUTHORIZATION_FAILED", "ReservationAuthorization", null,
                        "部门不存在: " + departmentId, ipAddress, userAgent);
                return false;
            }

            if (!"active".equals(department.getStatus())) {
                logAuditEvent(operatorId, "GRANT_AUTHORIZATION_FAILED", "ReservationAuthorization", null,
                        "部门未激活: " + departmentId, ipAddress, userAgent);
                return false;
            }

            // 验证预约类型
            if (!isValidReservationType(reservationType)) {
                logAuditEvent(operatorId, "GRANT_AUTHORIZATION_FAILED", "ReservationAuthorization", null,
                        "无效的预约类型: " + reservationType, ipAddress, userAgent);
                return false;
            }

            // 检查是否已有相同类型的权限
            List<ReservationAuthorization> existingAuths = authorizationDAO.getAuthorizationsByDepartment(departmentId);
            boolean hasConflict = existingAuths.stream()
                    .anyMatch(auth -> "active".equals(auth.getStatus()) &&
                            (reservationType.equals(auth.getReservationType()) ||
                                    "both".equals(auth.getReservationType()) ||
                                    ("both".equals(reservationType) &&
                                            ("public".equals(auth.getReservationType())
                                                    || "official".equals(auth.getReservationType())))));

            if (hasConflict) {
                logAuditEvent(operatorId, "GRANT_AUTHORIZATION_FAILED", "ReservationAuthorization", null,
                        "部门已有相同或冲突的预约权限: " + departmentId + ", " + reservationType,
                        ipAddress, userAgent);
                return false;
            }

            // 创建授权记录
            ReservationAuthorization authorization = new ReservationAuthorization(
                    departmentId, reservationType, operatorId, description);

            ReservationAuthorization savedAuth = authorizationDAO.addAuthorization(authorization);
            if (savedAuth != null) {
                logAuditEvent(operatorId, "GRANT_AUTHORIZATION", "ReservationAuthorization", savedAuth.getId(),
                        String.format("授予部门预约权限: %s (%s) - %s",
                                department.getDepartmentName(), reservationType, description),
                        ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "GRANT_AUTHORIZATION_FAILED", "ReservationAuthorization", null,
                    "授予权限失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 撤销部门预约权限
     */
    public boolean revokeAuthorization(Long authorizationId, Long operatorId,
            String ipAddress, String userAgent) {
        try {
            ReservationAuthorization authorization = authorizationDAO.getAuthorizationById(authorizationId);
            if (authorization == null) {
                logAuditEvent(operatorId, "REVOKE_AUTHORIZATION_FAILED", "ReservationAuthorization", authorizationId,
                        "授权记录不存在", ipAddress, userAgent);
                return false;
            }

            if (!"active".equals(authorization.getStatus())) {
                logAuditEvent(operatorId, "REVOKE_AUTHORIZATION_FAILED", "ReservationAuthorization", authorizationId,
                        "授权已经被撤销", ipAddress, userAgent);
                return false;
            }

            // 获取部门信息用于日志
            Department department = departmentDAO.getDepartmentById(authorization.getDepartmentId());
            String departmentName = department != null ? department.getDepartmentName() : "未知部门";

            boolean success = authorizationDAO.revokeAuthorization(authorizationId, LocalDateTime.now());
            if (success) {
                logAuditEvent(operatorId, "REVOKE_AUTHORIZATION", "ReservationAuthorization", authorizationId,
                        String.format("撤销部门预约权限: %s (%s)",
                                departmentName, authorization.getReservationType()),
                        ipAddress, userAgent);
                return true;
            }

        } catch (Exception e) {
            logAuditEvent(operatorId, "REVOKE_AUTHORIZATION_FAILED", "ReservationAuthorization", authorizationId,
                    "撤销权限失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 检查部门是否有特定类型的预约权限
     */
    public boolean checkDepartmentPermission(Long departmentId, String reservationType) {
        try {
            return authorizationDAO.isDepartmentAuthorized(departmentId, reservationType);
        } catch (Exception e) {
            System.err.println("Failed to check department permission: " + e.getMessage());
            return false;
        }
    }

    /**
     * 获取部门的所有有效授权
     */
    public List<ReservationAuthorization> getDepartmentAuthorizations(Long departmentId) {
        return authorizationDAO.getActiveAuthorizationsByDepartmentId(departmentId);
    }

    /**
     * 获取特定类型预约的所有授权部门ID
     */
    public List<Long> getAuthorizedDepartmentIds(String reservationType) {
        return authorizationDAO.getAuthorizedDepartmentIds(reservationType);
    }

    /**
     * 获取所有授权记录
     */
    public List<ReservationAuthorization> getAllAuthorizations() {
        return authorizationDAO.getAllAuthorizations();
    }

    /**
     * 获取授权详情
     */
    public ReservationAuthorization getAuthorizationById(Long id) {
        return authorizationDAO.getAuthorizationById(id);
    }

    /**
     * 获取授权统计信息
     */
    public Map<String, Object> getAuthorizationStatistics() {
        try {
            List<ReservationAuthorization> allAuths = authorizationDAO.getAllAuthorizations();
            Map<String, Object> statistics = new java.util.HashMap<>();

            // 按状态统计
            Map<String, Long> statusStats = allAuths.stream()
                    .collect(Collectors.groupingBy(
                            ReservationAuthorization::getStatus,
                            Collectors.counting()));
            statistics.put("statusStatistics", statusStats);

            // 按预约类型统计
            Map<String, Long> typeStats = allAuths.stream()
                    .filter(auth -> "active".equals(auth.getStatus()))
                    .collect(Collectors.groupingBy(
                            ReservationAuthorization::getReservationType,
                            Collectors.counting()));
            statistics.put("typeStatistics", typeStats);

            // 总数统计
            statistics.put("totalAuthorizations", allAuths.size());
            statistics.put("activeAuthorizations", statusStats.getOrDefault("active", 0L));
            statistics.put("revokedAuthorizations", statusStats.getOrDefault("revoked", 0L));

            // 有权限的部门数量
            long authorizedDepartments = allAuths.stream()
                    .filter(auth -> "active".equals(auth.getStatus()))
                    .map(ReservationAuthorization::getDepartmentId)
                    .distinct()
                    .count();
            statistics.put("authorizedDepartmentCount", authorizedDepartments);

            return statistics;

        } catch (Exception e) {
            System.err.println("Failed to get authorization statistics: " + e.getMessage());
            return new java.util.HashMap<>();
        }
    }

    /**
     * 获取有预约权限的部门列表（带详细信息）
     */
    public List<Map<String, Object>> getAuthorizedDepartmentsWithDetails() {
        try {
            List<ReservationAuthorization> activeAuths = authorizationDAO.getAllAuthorizations().stream()
                    .filter(auth -> "active".equals(auth.getStatus()))
                    .collect(Collectors.toList());

            return activeAuths.stream()
                    .map(auth -> {
                        Map<String, Object> detail = new java.util.HashMap<>();
                        detail.put("authorization", auth);

                        Department department = departmentDAO.getDepartmentById(auth.getDepartmentId());
                        detail.put("department", department);

                        return detail;
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            System.err.println("Failed to get authorized departments with details: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * 批量授权（为多个部门授予相同权限）
     */
    public Map<Long, Boolean> batchGrantAuthorization(List<Long> departmentIds, String reservationType,
            Long operatorId, String description,
            String ipAddress, String userAgent) {
        Map<Long, Boolean> results = new java.util.HashMap<>();

        for (Long departmentId : departmentIds) {
            boolean success = grantAuthorization(departmentId, reservationType, operatorId,
                    description, ipAddress, userAgent);
            results.put(departmentId, success);
        }

        // 记录批量操作日志
        int successCount = (int) results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        logAuditEvent(operatorId, "BATCH_GRANT_AUTHORIZATION", "ReservationAuthorization", null,
                String.format("批量授权: %d个部门，成功%d个，失败%d个，权限类型: %s",
                        departmentIds.size(), successCount, departmentIds.size() - successCount, reservationType),
                ipAddress, userAgent);

        return results;
    }

    /**
     * 批量撤销授权
     */
    public Map<Long, Boolean> batchRevokeAuthorization(List<Long> authorizationIds, Long operatorId,
            String ipAddress, String userAgent) {
        Map<Long, Boolean> results = new java.util.HashMap<>();

        for (Long authorizationId : authorizationIds) {
            boolean success = revokeAuthorization(authorizationId, operatorId, ipAddress, userAgent);
            results.put(authorizationId, success);
        }

        // 记录批量操作日志
        int successCount = (int) results.values().stream().mapToLong(b -> b ? 1 : 0).sum();
        logAuditEvent(operatorId, "BATCH_REVOKE_AUTHORIZATION", "ReservationAuthorization", null,
                String.format("批量撤销授权: %d个授权，成功%d个，失败%d个",
                        authorizationIds.size(), successCount, authorizationIds.size() - successCount),
                ipAddress, userAgent);

        return results;
    }

    /**
     * 验证预约类型是否有效
     */
    private boolean isValidReservationType(String reservationType) {
        return "public".equals(reservationType) ||
                "official".equals(reservationType) ||
                "both".equals(reservationType);
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
