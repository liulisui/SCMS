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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.example.scms.model.ReservationAuthorization;
import org.example.scms.util.DBUtil;

/**
 * 预约授权数据访问对象
 */
public class ReservationAuthorizationDAO {

    private static final Logger logger = Logger.getLogger(ReservationAuthorizationDAO.class.getName());

    /**
     * 根据ID获取预约授权
     */
    public ReservationAuthorization getAuthorizationById(Long id) {
        String sql = "SELECT * FROM reservation_authorizations WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAuthorization(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据ID获取预约授权失败 - ID: " + id, e);
        }
        return null;
    }

    /**
     * 添加预约授权
     */
    public ReservationAuthorization addAuthorization(ReservationAuthorization auth) {
        String sql = "INSERT INTO reservation_authorizations (department_id, reservation_type, granted_by, granted_at, "
                +
                "status, description, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setLong(1, auth.getDepartmentId());
            stmt.setString(2, auth.getReservationType());
            stmt.setLong(3, auth.getGrantedBy());
            stmt.setTimestamp(4, Timestamp.valueOf(auth.getGrantedAt()));
            stmt.setString(5, auth.getStatus());
            stmt.setString(6, auth.getDescription());
            stmt.setTimestamp(7, Timestamp.valueOf(now));
            stmt.setTimestamp(8, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加预约授权失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    auth.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加预约授权失败，未获取到ID。");
                }
            }
            return auth;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "添加预约授权失败", e);
        }
        return null;
    }

    /**
     * 撤销预约授权
     */
    public boolean revokeAuthorization(Long id, LocalDateTime revokedAt) {
        String sql = "UPDATE reservation_authorizations SET status = 'revoked', revoked_at = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(revokedAt));
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, id);
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "撤销预约授权失败 - ID: " + id, e);
        }
        return false;
    }

    /**
     * 获取部门的所有有效授权
     */
    public List<ReservationAuthorization> getActiveAuthorizationsByDepartmentId(Long departmentId) {
        List<ReservationAuthorization> authorizations = new ArrayList<>();
        String sql = "SELECT * FROM reservation_authorizations WHERE department_id = ? AND status = 'active'";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, departmentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    authorizations.add(mapResultSetToAuthorization(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取部门有效授权失败 - 部门ID: " + departmentId, e);
        }
        return authorizations;
    }

    /**
     * 获取特定类型预约的所有有效授权部门ID
     */
    public List<Long> getAuthorizedDepartmentIds(String reservationType) {
        List<Long> departmentIds = new ArrayList<>();
        String sql = "SELECT department_id FROM reservation_authorizations WHERE " +
                "(reservation_type = ? OR reservation_type = 'both') AND status = 'active'";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservationType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departmentIds.add(rs.getLong("department_id"));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取授权部门ID失败 - 预约类型: " + reservationType, e);
        }
        return departmentIds;
    }

    /**
     * 检查部门是否有特定类型预约的授权
     */
    public boolean isDepartmentAuthorized(Long departmentId, String reservationType) {
        String sql = "SELECT COUNT(*) FROM reservation_authorizations WHERE department_id = ? " +
                "AND (reservation_type = ? OR reservation_type = 'both') AND status = 'active'";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, departmentId);
            stmt.setString(2, reservationType);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "检查部门授权失败 - 部门ID: " + departmentId + ", 预约类型: " + reservationType, e);
        }
        return false;
    }

    /**
     * 获取所有授权记录
     */
    public List<ReservationAuthorization> getAllAuthorizations() {
        List<ReservationAuthorization> authorizations = new ArrayList<>();
        String sql = "SELECT ra.*, d.department_name, a.full_name as granted_by_name " +
                "FROM reservation_authorizations ra " +
                "JOIN departments d ON ra.department_id = d.id " +
                "JOIN administrators a ON ra.granted_by = a.id " +
                "ORDER BY ra.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                authorizations.add(mapResultSetToAuthorization(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取所有授权记录失败", e);
        }
        return authorizations;
    }

    /**
     * 获取部门的所有授权记录（包括active和revoked状态）
     */
    public List<ReservationAuthorization> getAuthorizationsByDepartment(Long departmentId) {
        List<ReservationAuthorization> authorizations = new ArrayList<>();
        String sql = "SELECT * FROM reservation_authorizations WHERE department_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, departmentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    authorizations.add(mapResultSetToAuthorization(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取部门授权记录失败 - 部门ID: " + departmentId, e);
        }
        return authorizations;
    }

    /**
     * 将ResultSet映射为ReservationAuthorization对象
     */
    private ReservationAuthorization mapResultSetToAuthorization(ResultSet rs) throws SQLException {
        ReservationAuthorization auth = new ReservationAuthorization();
        auth.setId(rs.getLong("id"));
        auth.setDepartmentId(rs.getLong("department_id"));
        auth.setReservationType(rs.getString("reservation_type"));
        auth.setGrantedBy(rs.getLong("granted_by"));

        Timestamp grantedAt = rs.getTimestamp("granted_at");
        if (grantedAt != null) {
            auth.setGrantedAt(grantedAt.toLocalDateTime());
        }

        Timestamp revokedAt = rs.getTimestamp("revoked_at");
        if (revokedAt != null) {
            auth.setRevokedAt(revokedAt.toLocalDateTime());
        }

        auth.setStatus(rs.getString("status"));
        auth.setDescription(rs.getString("description"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            auth.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            auth.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return auth;
    }
}
