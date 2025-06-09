package org.example.scms.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.example.scms.model.AdminPermission;
import org.example.scms.util.DBUtil;

/**
 * 管理员权限关联数据访问对象
 */
public class AdminPermissionDAO {

    /**
     * 添加管理员权限关联
     */
    public AdminPermission addAdminPermission(AdminPermission adminPermission) {
        String sql = "INSERT INTO admin_permissions (admin_id, permission_id, granted_by, granted_at, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setLong(1, adminPermission.getAdminId());
            stmt.setLong(2, adminPermission.getPermissionId());
            stmt.setLong(3, adminPermission.getGrantedBy());
            stmt.setTimestamp(4, Timestamp.valueOf(adminPermission.getGrantedAt()));
            stmt.setTimestamp(5, Timestamp.valueOf(now));
            stmt.setTimestamp(6, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加管理员权限关联失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    adminPermission.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加管理员权限关联失败，未获取到ID。");
                }
            }

            return adminPermission;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 删除管理员权限关联
     */
    public boolean deleteAdminPermission(Long id) {
        String sql = "DELETE FROM admin_permissions WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除管理员的所有权限关联
     */
    public boolean deleteAdminPermissionsByAdminId(Long adminId) {
        String sql = "DELETE FROM admin_permissions WHERE admin_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除管理员的特定权限关联
     */
    public boolean deleteAdminPermission(Long adminId, Long permissionId) {
        String sql = "DELETE FROM admin_permissions WHERE admin_id = ? AND permission_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);
            stmt.setLong(2, permissionId);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取管理员的所有权限关联
     */
    public List<AdminPermission> getAdminPermissionsByAdminId(Long adminId) {
        List<AdminPermission> adminPermissions = new ArrayList<>();
        String sql = "SELECT ap.*, p.permission_name, a.full_name as granted_by_name " +
                "FROM admin_permissions ap " +
                "JOIN permissions p ON ap.permission_id = p.id " +
                "JOIN administrators a ON ap.granted_by = a.id " +
                "WHERE ap.admin_id = ? " +
                "ORDER BY ap.granted_at";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    adminPermissions.add(mapResultSetToAdminPermission(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return adminPermissions;
    }

    /**
     * 检查管理员是否拥有特定权限
     */
    public boolean checkAdminHasPermission(Long adminId, Long permissionId) {
        String sql = "SELECT COUNT(*) FROM admin_permissions WHERE admin_id = ? AND permission_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);
            stmt.setLong(2, permissionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 批量添加管理员权限关联
     */
    public boolean batchAddAdminPermissions(Long adminId, List<Long> permissionIds, Long grantedBy) {
        String sql = "INSERT INTO admin_permissions (admin_id, permission_id, granted_by, granted_at, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 关闭自动提交事务，开始手动管理事务
            conn.setAutoCommit(false);

            LocalDateTime now = LocalDateTime.now();

            for (Long permissionId : permissionIds) {
                stmt.setLong(1, adminId);
                stmt.setLong(2, permissionId);
                stmt.setLong(3, grantedBy);
                stmt.setTimestamp(4, Timestamp.valueOf(now));
                stmt.setTimestamp(5, Timestamp.valueOf(now));
                stmt.setTimestamp(6, Timestamp.valueOf(now));
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            conn.commit();

            // 检查是否所有批处理操作都成功
            for (int result : results) {
                if (result <= 0) {
                    return false;
                }
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将ResultSet映射为AdminPermission对象
     */
    private AdminPermission mapResultSetToAdminPermission(ResultSet rs) throws SQLException {
        AdminPermission adminPermission = new AdminPermission();
        adminPermission.setId(rs.getLong("id"));
        adminPermission.setAdminId(rs.getLong("admin_id"));
        adminPermission.setPermissionId(rs.getLong("permission_id"));
        adminPermission.setGrantedBy(rs.getLong("granted_by"));

        Timestamp grantedAt = rs.getTimestamp("granted_at");
        if (grantedAt != null) {
            adminPermission.setGrantedAt(grantedAt.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            adminPermission.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            adminPermission.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return adminPermission;
    }
}
