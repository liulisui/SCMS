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

import org.example.scms.model.Campus;
import org.example.scms.model.Department;
import org.example.scms.model.DepartmentCampusMapping;
import org.example.scms.util.DBUtil;

/**
 * 部门-校区映射数据访问对象
 */
public class DepartmentCampusMappingDAO {
    
    private static final Logger logger = Logger.getLogger(DepartmentCampusMappingDAO.class.getName());

    /**
     * 添加部门-校区映射
     */
    public DepartmentCampusMapping addMapping(DepartmentCampusMapping mapping) {
        String sql = "INSERT INTO department_campus_mapping (department_id, campus_id, permission_type, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            LocalDateTime now = LocalDateTime.now();
            
            stmt.setLong(1, mapping.getDepartmentId());
            stmt.setLong(2, mapping.getCampusId());
            stmt.setString(3, mapping.getPermissionType());
            stmt.setString(4, mapping.getStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(now));
            stmt.setTimestamp(6, Timestamp.valueOf(now));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("添加部门-校区映射失败，没有行被插入。");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    mapping.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加部门-校区映射失败，未获取到ID。");
                }
            }
            
            return mapping;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "添加部门-校区映射失败", e);
        }
        return null;
    }

    /**
     * 根据部门ID获取校区列表
     */
    public List<Campus> getCampusesByDepartmentId(Long departmentId, String permissionType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT c.* FROM campuses c ")
           .append("INNER JOIN department_campus_mapping dcm ON c.id = dcm.campus_id ")
           .append("WHERE dcm.department_id = ? AND dcm.status = 'active'");
        
        if (permissionType != null && !permissionType.isEmpty()) {
            sql.append(" AND (dcm.permission_type = ? OR dcm.permission_type = 'both')");
        }
        
        sql.append(" ORDER BY c.campus_code");
        
        List<Campus> campuses = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setLong(1, departmentId);
            if (permissionType != null && !permissionType.isEmpty()) {
                stmt.setString(2, permissionType);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    campuses.add(mapResultSetToCampus(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据部门ID获取校区列表失败 - 部门ID: " + departmentId, e);
        }
        
        return campuses;
    }

    /**
     * 根据校区ID获取有权限的部门列表
     */
    public List<Department> getDepartmentsByCampusId(Long campusId, String permissionType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.* FROM departments d ")
           .append("INNER JOIN department_campus_mapping dcm ON d.id = dcm.department_id ")
           .append("WHERE dcm.campus_id = ? AND dcm.status = 'active' AND d.status = 'active'");
        
        if (permissionType != null && !permissionType.isEmpty()) {
            sql.append(" AND (dcm.permission_type = ? OR dcm.permission_type = 'both')");
        }
        
        sql.append(" ORDER BY d.department_code");
        
        List<Department> departments = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            stmt.setLong(1, campusId);
            if (permissionType != null && !permissionType.isEmpty()) {
                stmt.setString(2, permissionType);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departments.add(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据校区ID获取部门列表失败 - 校区ID: " + campusId, e);
        }
        
        return departments;
    }

    /**
     * 检查部门是否有校区权限
     */
    public boolean hasCampusPermission(Long departmentId, Long campusId, String permissionType) {
        String sql = "SELECT COUNT(*) FROM department_campus_mapping " +
                    "WHERE department_id = ? AND campus_id = ? AND status = 'active' " +
                    "AND (permission_type = ? OR permission_type = 'both')";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, departmentId);
            stmt.setLong(2, campusId);
            stmt.setString(3, permissionType);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "检查部门校区权限失败", e);
        }
        
        return false;
    }

    /**
     * 获取部门的所有映射关系
     */
    public List<DepartmentCampusMapping> getMappingsByDepartmentId(Long departmentId) {
        String sql = "SELECT dcm.*, d.department_name, c.campus_name " +
                    "FROM department_campus_mapping dcm " +
                    "LEFT JOIN departments d ON dcm.department_id = d.id " +
                    "LEFT JOIN campuses c ON dcm.campus_id = c.id " +
                    "WHERE dcm.department_id = ? ORDER BY c.campus_code";
        
        List<DepartmentCampusMapping> mappings = new ArrayList<>();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, departmentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mappings.add(mapResultSetToMapping(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取部门映射关系失败 - 部门ID: " + departmentId, e);
        }
        
        return mappings;
    }

    /**
     * 更新映射状态
     */
    public boolean updateMappingStatus(Long mappingId, String status) {
        String sql = "UPDATE department_campus_mapping SET status = ?, updated_at = ? WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, mappingId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新映射状态失败 - ID: " + mappingId, e);
        }
        
        return false;
    }

    /**
     * 删除映射关系
     */
    public boolean deleteMapping(Long mappingId) {
        String sql = "DELETE FROM department_campus_mapping WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, mappingId);
            
            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "删除映射关系失败 - ID: " + mappingId, e);
        }
        
        return false;
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

    /**
     * 将ResultSet映射为Department对象
     */
    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getLong("id"));
        department.setDepartmentCode(rs.getString("department_code"));
        department.setDepartmentName(rs.getString("department_name"));
        department.setDepartmentType(rs.getString("department_type"));
        department.setDescription(rs.getString("description"));
        department.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            department.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            department.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return department;
    }

    /**
     * 将ResultSet映射为DepartmentCampusMapping对象
     */
    private DepartmentCampusMapping mapResultSetToMapping(ResultSet rs) throws SQLException {
        DepartmentCampusMapping mapping = new DepartmentCampusMapping();
        mapping.setId(rs.getLong("id"));
        mapping.setDepartmentId(rs.getLong("department_id"));
        mapping.setCampusId(rs.getLong("campus_id"));
        mapping.setPermissionType(rs.getString("permission_type"));
        mapping.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            mapping.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            mapping.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        // 设置关联对象信息（如果查询包含）
        try {
            String departmentName = rs.getString("department_name");
            if (departmentName != null) {
                Department dept = new Department();
                dept.setId(mapping.getDepartmentId());
                dept.setDepartmentName(departmentName);
                mapping.setDepartment(dept);
            }
        } catch (SQLException ignored) {}
        
        try {
            String campusName = rs.getString("campus_name");
            if (campusName != null) {
                Campus campus = new Campus();
                campus.setId(mapping.getCampusId());
                campus.setCampusName(campusName);
                mapping.setCampus(campus);
            }
        } catch (SQLException ignored) {}
        
        return mapping;
    }
}
