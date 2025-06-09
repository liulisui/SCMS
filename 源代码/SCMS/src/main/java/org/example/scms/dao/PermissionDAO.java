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

import org.example.scms.model.Permission;
import org.example.scms.util.DBUtil;

/**
 * 权限数据访问对象
 */
public class PermissionDAO {

    /**
     * 根据ID获取权限
     */
    public Permission getPermissionById(Long id) {
        String sql = "SELECT * FROM permissions WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据权限编码获取权限
     */
    public Permission getPermissionByCode(String permissionCode) {
        String sql = "SELECT * FROM permissions WHERE permission_code = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, permissionCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPermission(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加权限
     */
    public Permission addPermission(Permission permission) {
        String sql = "INSERT INTO permissions (permission_code, permission_name, description, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, permission.getPermissionCode());
            stmt.setString(2, permission.getPermissionName());
            stmt.setString(3, permission.getDescription());
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setTimestamp(5, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加权限失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    permission.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加权限失败，未获取到ID。");
                }
            }

            return permission;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新权限
     */
    public boolean updatePermission(Permission permission) {
        String sql = "UPDATE permissions SET permission_name = ?, description = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, permission.getPermissionName());
            stmt.setString(2, permission.getDescription());
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(4, permission.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除权限
     */
    public boolean deletePermission(Long id) {
        String sql = "DELETE FROM permissions WHERE id = ?";

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
     * 获取所有权限
     */
    public List<Permission> getAllPermissions() {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM permissions ORDER BY permission_code";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                permissions.add(mapResultSetToPermission(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * 获取管理员的所有权限ID
     */
    public List<Long> getPermissionIdsByAdminId(Long adminId) {
        List<Long> permissionIds = new ArrayList<>();
        String sql = "SELECT permission_id FROM admin_permissions WHERE admin_id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permissionIds.add(rs.getLong("permission_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissionIds;
    }

    /**
     * 获取管理员的所有权限
     */
    public List<Permission> getPermissionsByAdminId(Long adminId) {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT p.* FROM permissions p JOIN admin_permissions ap ON p.id = ap.permission_id " +
                "WHERE ap.admin_id = ? ORDER BY p.permission_code";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    permissions.add(mapResultSetToPermission(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return permissions;
    }

    /**
     * 检查管理员是否有指定权限
     */
    public boolean hasPermission(Long adminId, String permissionCode) {
        String sql = "SELECT COUNT(*) FROM admin_permissions ap JOIN permissions p ON ap.permission_id = p.id " +
                "WHERE ap.admin_id = ? AND p.permission_code = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, adminId);
            stmt.setString(2, permissionCode);

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
     * 将ResultSet映射为Permission对象
     */
    private Permission mapResultSetToPermission(ResultSet rs) throws SQLException {
        Permission permission = new Permission();
        permission.setId(rs.getLong("id"));
        permission.setPermissionCode(rs.getString("permission_code"));
        permission.setPermissionName(rs.getString("permission_name"));
        permission.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            permission.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            permission.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return permission;
    }
}
