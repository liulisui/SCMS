package org.example.scms.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.example.scms.dao.AuditLogDAO;
import org.example.scms.model.AuditLog;

/**
 * 审计日志服务类
 * 处理审计日志相关的业务逻辑
 */
public class AuditLogService {
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * 根据日期范围获取审计日志
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        return auditLogDAO.getAuditLogsByDateRange(startDate, endDate);
    }

    /**
     * 根据管理员ID获取审计日志
     */
    public List<AuditLog> getAuditLogsByAdminId(Long adminId) {
        return auditLogDAO.getAuditLogsByAdminId(adminId);
    }

    /**
     * 根据操作类型获取审计日志
     */
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogDAO.getAuditLogsByAction(action);
    }

    /**
     * 根据资源类型获取审计日志
     */
    public List<AuditLog> getAuditLogsByResourceType(String resourceType) {
        return auditLogDAO.getAuditLogsByResourceType(resourceType);
    }

    /**
     * 多条件查询审计日志
     */
    public List<AuditLog> searchAuditLogs(Long adminId, String action, String resourceType, 
                                         LocalDate startDate, LocalDate endDate, String keyword) {
        return auditLogDAO.searchAuditLogs(adminId, action, resourceType, startDate, endDate, keyword);
    }

    /**
     * 获取最近的重要操作日志
     */
    public List<AuditLog> getRecentImportantLogs(int limit) {
        List<String> importantActions = Arrays.asList(
            "LOGIN_SUCCESS", "LOGIN_FAILED", "ACCOUNT_LOCKED", "ACCOUNT_UNLOCKED",
            "PASSWORD_CHANGED", "PASSWORD_RESET", "CREATE", "UPDATE", "DELETE",
            "ACTIVATE", "DEACTIVATE", "GRANT_AUTHORIZATION", "REVOKE_AUTHORIZATION"
        );
        
        return auditLogDAO.getAuditLogsByActions(importantActions, limit);
    }

    /**
     * 获取登录相关的审计日志
     */
    public List<AuditLog> getLoginRelatedLogs(LocalDate startDate, LocalDate endDate) {
        List<String> loginActions = Arrays.asList("LOGIN_SUCCESS", "LOGIN_FAILED", "ACCOUNT_LOCKED");
        return auditLogDAO.getAuditLogsByActionsAndDateRange(loginActions, startDate, endDate);
    }

    /**
     * 获取权限变更相关的审计日志
     */
    public List<AuditLog> getPermissionRelatedLogs(LocalDate startDate, LocalDate endDate) {
        List<String> permissionActions = Arrays.asList(
            "GRANT_AUTHORIZATION", "REVOKE_AUTHORIZATION", "CREATE", "UPDATE", "DELETE"
        );
        return auditLogDAO.getAuditLogsByActionsAndDateRange(permissionActions, startDate, endDate);
    }

    /**
     * 获取数据操作相关的审计日志
     */
    public List<AuditLog> getDataOperationLogs(LocalDate startDate, LocalDate endDate) {
        List<String> dataActions = Arrays.asList("VIEW", "CREATE", "UPDATE", "DELETE", "EXPORT");
        return auditLogDAO.getAuditLogsByActionsAndDateRange(dataActions, startDate, endDate);
    }

    /**
     * 根据IP地址获取审计日志
     */
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress) {
        return auditLogDAO.getAuditLogsByIpAddress(ipAddress);
    }

    /**
     * 获取异常登录日志（可疑IP、频繁失败等）
     */
    public List<AuditLog> getSuspiciousLoginLogs(LocalDate startDate, LocalDate endDate) {
        return auditLogDAO.getSuspiciousLoginLogs(startDate, endDate);
    }

    /**
     * 清理过期的审计日志（保留指定天数）
     */
    public int cleanupExpiredLogs(int retentionDays) {
        LocalDate cutoffDate = LocalDate.now().minusDays(retentionDays);
        return auditLogDAO.deleteLogsBefore(cutoffDate);
    }

    /**
     * 获取审计日志统计信息
     */
    public AuditLogStatistics getAuditLogStatistics(LocalDate startDate, LocalDate endDate) {
        return auditLogDAO.getAuditLogStatistics(startDate, endDate);
    }

    /**
     * 根据ID获取审计日志
     */
    public AuditLog getAuditLogById(Long id) {
        return auditLogDAO.getAuditLogById(id);
    }

    /**
     * 获取所有审计日志
     */
    public List<AuditLog> getAllAuditLogs() {
        // 获取最近1000条日志，避免数据量过大
        return auditLogDAO.getAuditLogsByActions(
            Arrays.asList("LOGIN_SUCCESS", "LOGIN_FAILED", "CREATE", "UPDATE", "DELETE", 
                         "VIEW", "QUERY", "APPROVE", "REJECT", "ACTIVATE", "DEACTIVATE"), 
            1000
        );
    }

    /**
     * 获取最近的审计日志（用于仪表板预览）
     */
    public List<AuditLog> getRecentAuditLogs(int limit) {
        return getRecentImportantLogs(limit);
    }

    /**
     * 审计日志统计信息内部类
     */
    public static class AuditLogStatistics {
        private int totalLogs;
        private int successfulLogins;
        private int failedLogins;
        private int dataOperations;
        private int permissionChanges;
        private java.util.Map<String, Integer> actionCounts;
        private java.util.Map<String, Integer> resourceTypeCounts;

        // Constructors, getters and setters
        public AuditLogStatistics() {
            this.actionCounts = new java.util.HashMap<>();
            this.resourceTypeCounts = new java.util.HashMap<>();
        }

        // Getters and setters
        public int getTotalLogs() { return totalLogs; }
        public void setTotalLogs(int totalLogs) { this.totalLogs = totalLogs; }

        public int getSuccessfulLogins() { return successfulLogins; }
        public void setSuccessfulLogins(int successfulLogins) { this.successfulLogins = successfulLogins; }

        public int getFailedLogins() { return failedLogins; }
        public void setFailedLogins(int failedLogins) { this.failedLogins = failedLogins; }

        public int getDataOperations() { return dataOperations; }
        public void setDataOperations(int dataOperations) { this.dataOperations = dataOperations; }

        public int getPermissionChanges() { return permissionChanges; }
        public void setPermissionChanges(int permissionChanges) { this.permissionChanges = permissionChanges; }

        public java.util.Map<String, Integer> getActionCounts() { return actionCounts; }
        public void setActionCounts(java.util.Map<String, Integer> actionCounts) { this.actionCounts = actionCounts; }

        public java.util.Map<String, Integer> getResourceTypeCounts() { return resourceTypeCounts; }
        public void setResourceTypeCounts(java.util.Map<String, Integer> resourceTypeCounts) { this.resourceTypeCounts = resourceTypeCounts; }
    }
}
