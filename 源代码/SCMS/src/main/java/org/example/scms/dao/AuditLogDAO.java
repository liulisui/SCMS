package org.example.scms.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.AuditLog;
import org.example.scms.util.DBUtil;

/**
 * 审计日志数据访问对象
 */
public class AuditLogDAO {

    /**
     * 添加审计日志
     */
    public AuditLog addAuditLog(AuditLog log) {
        String sql = "INSERT INTO admin_audit_logs (admin_id, action, resource_type, resource_id, " +
                "details, old_value, new_value, ip_address, user_agent, hmac_value, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // 处理可能为null的adminId
            if (log.getAdminId() != null) {
                stmt.setLong(1, log.getAdminId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getResourceType());

            if (log.getResourceId() != null) {
                stmt.setLong(4, log.getResourceId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }

            stmt.setString(5, log.getDetails());
            stmt.setString(6, log.getOldValue());
            stmt.setString(7, log.getNewValue());
            stmt.setString(8, log.getIpAddress());
            stmt.setString(9, log.getUserAgent());
            stmt.setString(10, log.getHmacValue());
            stmt.setTimestamp(11, Timestamp.valueOf(log.getCreatedAt()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加审计日志失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    log.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加审计日志失败，未获取到ID。");
                }
            }

            return log;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }    /**
     * 根据ID获取审计日志
     */
    public AuditLog getAuditLogById(Long id) {
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuditLog(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }    /**
     * 获取指定日期范围的审计日志
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE DATE(aal.created_at) BETWEEN ? AND ? ORDER BY aal.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }    /**
     * 获取指定管理员的审计日志
     */
    public List<AuditLog> getAuditLogsByAdminId(Long adminId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.admin_id = ? ORDER BY aal.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }    /**
     * 获取指定操作类型的审计日志
     */
    public List<AuditLog> getAuditLogsByAction(String action) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.action = ? ORDER BY aal.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, action);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }    /**
     * 获取指定资源类型的审计日志
     */
    public List<AuditLog> getAuditLogsByResourceType(String resourceType) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.resource_type = ? ORDER BY aal.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resourceType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }    /**
     * 获取指定资源ID的审计日志
     */
    public List<AuditLog> getAuditLogsByResourceId(String resourceType, Long resourceId) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.resource_type = ? AND aal.resource_id = ? ORDER BY aal.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, resourceType);
            stmt.setLong(2, resourceId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }/**
     * 高级查询审计日志
     */
    public List<AuditLog> searchAuditLogs(Long adminId, String action, String resourceType,
            LocalDate startDate, LocalDate endDate, String keyword) {
        List<AuditLog> logs = new ArrayList<>();        StringBuilder sqlBuilder = new StringBuilder("SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (adminId != null) {
            sqlBuilder.append(" AND aal.admin_id = ?");
            params.add(adminId);
        }

        if (action != null && !action.isEmpty()) {
            sqlBuilder.append(" AND aal.action = ?");
            params.add(action);
        }

        if (resourceType != null && !resourceType.isEmpty()) {
            sqlBuilder.append(" AND aal.resource_type = ?");
            params.add(resourceType);
        }

        if (startDate != null) {
            sqlBuilder.append(" AND DATE(aal.created_at) >= ?");
            params.add(startDate);
        }

        if (endDate != null) {
            sqlBuilder.append(" AND DATE(aal.created_at) <= ?");
            params.add(endDate);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sqlBuilder.append(" AND (aal.details LIKE ? OR aal.ip_address LIKE ? OR aal.user_agent LIKE ?)");
            String keywordPattern = "%" + keyword.trim() + "%";
            params.add(keywordPattern);
            params.add(keywordPattern);
            params.add(keywordPattern);
        }

        sqlBuilder.append(" ORDER BY aal.created_at DESC LIMIT 1000");

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof Long) {
                    stmt.setLong(i + 1, (Long) param);
                } else if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof LocalDate) {
                    stmt.setDate(i + 1, Date.valueOf((LocalDate) param));
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * 根据多个操作类型获取审计日志
     */
    public List<AuditLog> getAuditLogsByActions(List<String> actions, int limit) {
        if (actions == null || actions.isEmpty()) {
            return new ArrayList<>();
        }

        List<AuditLog> logs = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.action IN (");
        
        for (int i = 0; i < actions.size(); i++) {
            sqlBuilder.append("?");
            if (i < actions.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(") ORDER BY aal.created_at DESC LIMIT ?");

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < actions.size(); i++) {
                stmt.setString(i + 1, actions.get(i));
            }
            stmt.setInt(actions.size() + 1, limit);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * 根据操作类型和日期范围获取审计日志
     */
    public List<AuditLog> getAuditLogsByActionsAndDateRange(List<String> actions, LocalDate startDate, LocalDate endDate) {
        if (actions == null || actions.isEmpty()) {
            return new ArrayList<>();
        }

        List<AuditLog> logs = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.action IN (");
        
        for (int i = 0; i < actions.size(); i++) {
            sqlBuilder.append("?");
            if (i < actions.size() - 1) {
                sqlBuilder.append(",");
            }
        }
        sqlBuilder.append(") AND DATE(created_at) BETWEEN ? AND ? ORDER BY created_at DESC");

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sqlBuilder.toString())) {

            for (int i = 0; i < actions.size(); i++) {
                stmt.setString(i + 1, actions.get(i));
            }
            stmt.setDate(actions.size() + 1, Date.valueOf(startDate));
            stmt.setDate(actions.size() + 2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }    /**
     * 根据IP地址获取审计日志
     */
    public List<AuditLog> getAuditLogsByIpAddress(String ipAddress) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                "WHERE aal.ip_address = ? ORDER BY aal.created_at DESC LIMIT 500";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ipAddress);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapResultSetToAuditLog(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }/**
     * 获取可疑登录日志（失败次数较多的IP）
     */
    public List<AuditLog> getSuspiciousLoginLogs(LocalDate startDate, LocalDate endDate) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT ip_address FROM admin_audit_logs WHERE action = 'LOGIN_FAILED' " +
                "AND DATE(created_at) BETWEEN ? AND ? " +
                "GROUP BY ip_address HAVING COUNT(*) >= 5";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            List<String> suspiciousIps = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suspiciousIps.add(rs.getString("ip_address"));
                }
            }            // 为每个可疑IP获取详细的失败日志
            if (!suspiciousIps.isEmpty()) {
                StringBuilder detailSql = new StringBuilder(
                    "SELECT aal.*, a.username FROM admin_audit_logs aal " +
                    "LEFT JOIN administrators a ON aal.admin_id = a.id " +
                    "WHERE aal.action = 'LOGIN_FAILED' " +
                    "AND DATE(aal.created_at) BETWEEN ? AND ? AND aal.ip_address IN (");
                
                for (int i = 0; i < suspiciousIps.size(); i++) {
                    detailSql.append("?");
                    if (i < suspiciousIps.size() - 1) {
                        detailSql.append(",");
                    }
                }
                detailSql.append(") ORDER BY aal.created_at DESC LIMIT 100");

                try (PreparedStatement detailStmt = conn.prepareStatement(detailSql.toString())) {
                    detailStmt.setDate(1, Date.valueOf(startDate));
                    detailStmt.setDate(2, Date.valueOf(endDate));
                    
                    for (int i = 0; i < suspiciousIps.size(); i++) {
                        detailStmt.setString(i + 3, suspiciousIps.get(i));
                    }

                    try (ResultSet detailRs = detailStmt.executeQuery()) {
                        while (detailRs.next()) {
                            logs.add(mapResultSetToAuditLog(detailRs));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * 删除指定日期之前的日志
     */
    public int deleteLogsBefore(LocalDate cutoffDate) {
        String sql = "DELETE FROM admin_audit_logs WHERE DATE(created_at) < ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(cutoffDate));
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取审计日志统计信息
     */
    public org.example.scms.service.AuditLogService.AuditLogStatistics getAuditLogStatistics(LocalDate startDate, LocalDate endDate) {
        org.example.scms.service.AuditLogService.AuditLogStatistics stats = 
            new org.example.scms.service.AuditLogService.AuditLogStatistics();

        String sql = "SELECT " +
                "COUNT(*) as total_logs, " +
                "SUM(CASE WHEN action = 'LOGIN_SUCCESS' THEN 1 ELSE 0 END) as successful_logins, " +
                "SUM(CASE WHEN action = 'LOGIN_FAILED' THEN 1 ELSE 0 END) as failed_logins, " +
                "SUM(CASE WHEN action IN ('CREATE', 'UPDATE', 'DELETE', 'VIEW') THEN 1 ELSE 0 END) as data_operations, " +
                "SUM(CASE WHEN action IN ('GRANT_AUTHORIZATION', 'REVOKE_AUTHORIZATION') THEN 1 ELSE 0 END) as permission_changes " +
                "FROM admin_audit_logs WHERE DATE(created_at) BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.setTotalLogs(rs.getInt("total_logs"));
                    stats.setSuccessfulLogins(rs.getInt("successful_logins"));
                    stats.setFailedLogins(rs.getInt("failed_logins"));
                    stats.setDataOperations(rs.getInt("data_operations"));
                    stats.setPermissionChanges(rs.getInt("permission_changes"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取操作类型统计
        String actionSql = "SELECT action, COUNT(*) as count FROM admin_audit_logs " +
                "WHERE DATE(created_at) BETWEEN ? AND ? GROUP BY action";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(actionSql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.getActionCounts().put(rs.getString("action"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 获取资源类型统计
        String resourceSql = "SELECT resource_type, COUNT(*) as count FROM admin_audit_logs " +
                "WHERE DATE(created_at) BETWEEN ? AND ? GROUP BY resource_type";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(resourceSql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.getResourceTypeCounts().put(rs.getString("resource_type"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * 验证指定ID的日志HMAC值
     */
    public boolean verifyLogIntegrity(Long logId, String expectedHmac) {
        String sql = "SELECT hmac_value FROM admin_audit_logs WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, logId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHmac = rs.getString("hmac_value");
                    return storedHmac != null && storedHmac.equals(expectedHmac);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }    /**
     * 将ResultSet映射为AuditLog对象
     */
    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setId(rs.getLong("id"));

        long adminId = rs.getLong("admin_id");
        if (!rs.wasNull()) {
            log.setAdminId(adminId);
        }

        log.setAction(rs.getString("action"));
        log.setResourceType(rs.getString("resource_type"));

        long resourceId = rs.getLong("resource_id");
        if (!rs.wasNull()) {
            log.setResourceId(resourceId);
        }

        log.setDetails(rs.getString("details"));
        log.setOldValue(rs.getString("old_value"));
        log.setNewValue(rs.getString("new_value"));
        log.setIpAddress(rs.getString("ip_address"));
        log.setUserAgent(rs.getString("user_agent"));
        log.setHmacValue(rs.getString("hmac_value"));
        
        // 设置用户名（从JOIN查询中获取）
        log.setUsername(rs.getString("username"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            log.setCreatedAt(createdAt.toLocalDateTime());
        }

        return log;
    }
}
