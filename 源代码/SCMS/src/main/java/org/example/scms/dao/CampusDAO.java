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

import org.example.scms.model.Campus;
import org.example.scms.util.DBUtil;

/**
 * 校区数据访问对象
 */
public class CampusDAO {

    /**
     * 根据ID获取校区
     */
    public Campus getCampusById(Long id) {
        String sql = "SELECT * FROM campuses WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCampus(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据校区编码获取校区
     */
    public Campus getCampusByCode(String campusCode) {
        String sql = "SELECT * FROM campuses WHERE campus_code = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, campusCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCampus(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加校区
     */
    public Campus addCampus(Campus campus) {
        String sql = "INSERT INTO campuses (campus_code, campus_name, address, description, status, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, campus.getCampusCode());
            stmt.setString(2, campus.getCampusName());
            stmt.setString(3, campus.getAddress());
            stmt.setString(4, campus.getDescription());
            stmt.setString(5, campus.getStatus());
            stmt.setTimestamp(6, Timestamp.valueOf(now));
            stmt.setTimestamp(7, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加校区失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    campus.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加校区失败，未获取到ID。");
                }
            }

            return campus;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新校区
     */
    public boolean updateCampus(Campus campus) {
        String sql = "UPDATE campuses SET campus_name = ?, address = ?, description = ?, status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, campus.getCampusName());
            stmt.setString(2, campus.getAddress());
            stmt.setString(3, campus.getDescription());
            stmt.setString(4, campus.getStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(6, campus.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除校区
     */
    public boolean deleteCampus(Long id) {
        String sql = "DELETE FROM campuses WHERE id = ?";

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
     * 获取所有校区
     */
    public List<Campus> getAllCampuses() {
        List<Campus> campuses = new ArrayList<>();
        String sql = "SELECT * FROM campuses ORDER BY campus_code";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                campuses.add(mapResultSetToCampus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return campuses;
    }

    /**
     * 获取所有激活状态的校区
     */
    public List<Campus> getActiveCampuses() {
        List<Campus> campuses = new ArrayList<>();
        String sql = "SELECT * FROM campuses WHERE status = 'active' ORDER BY campus_code";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                campuses.add(mapResultSetToCampus(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return campuses;
    }

    /**
     * 将ResultSet映射为Campus对象
     */
    private Campus mapResultSetToCampus(ResultSet rs) throws SQLException {
        Campus campus = new Campus();
        campus.setId(rs.getLong("id"));
        campus.setCampusCode(rs.getString("campus_code"));
        campus.setCampusName(rs.getString("campus_name"));
        campus.setAddress(rs.getString("address"));
        campus.setDescription(rs.getString("description"));
        campus.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            campus.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            campus.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return campus;
    }
}
