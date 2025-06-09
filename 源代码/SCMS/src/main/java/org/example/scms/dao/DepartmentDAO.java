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

import org.example.scms.model.Department;
import org.example.scms.util.DBUtil;

/**
 * 部门数据访问对象
 */
public class DepartmentDAO {

    private static final Logger logger = Logger.getLogger(DepartmentDAO.class.getName());

    /**
     * 根据ID获取部门
     */
    public Department getDepartmentById(Long id) {
        String sql = "SELECT * FROM departments WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取部门失败 - ID: " + id, e);
        }
        return null;
    }    /**
     * 根据部门编号获取部门
     */
    public Department getDepartmentByCode(String departmentCode) {
        String sql = "SELECT * FROM departments WHERE dept_code = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, departmentCode);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据编号获取部门失败 - 部门编号: " + departmentCode, e);
        }
        return null;
    }/**
     * 添加部门
     */
    public Department addDepartment(Department department) {
        String sql = "INSERT INTO departments (dept_code, dept_name, dept_type, description, status, created_by, created_at, updated_at) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();

            stmt.setString(1, department.getDepartmentCode());
            stmt.setString(2, department.getDepartmentName());
            stmt.setString(3, department.getDepartmentType());
            stmt.setString(4, department.getDescription());
            stmt.setString(5, department.getStatus());
            Long createdBy = department.getCreatedBy();
            stmt.setLong(6, createdBy != null ? createdBy : 1L); // 默认使用管理员ID 1
            stmt.setTimestamp(7, Timestamp.valueOf(now));
            stmt.setTimestamp(8, Timestamp.valueOf(now));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("添加部门失败，没有行被插入。");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    department.setId(generatedKeys.getLong(1));
                } else {
                    throw new SQLException("添加部门失败，未获取到ID。");
                }
            }
            return department;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "添加部门失败", e);
        }
        return null;
    }

    /**
     * 更新部门
     */    public boolean updateDepartment(Department department) {
        String sql = "UPDATE departments SET dept_name = ?, dept_type = ?, description = ?, " +
                "status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, department.getDepartmentName());
            stmt.setString(2, department.getDepartmentType());
            stmt.setString(3, department.getDescription());
            stmt.setString(4, department.getStatus());
            stmt.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(6, department.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "更新部门失败 - ID: " + department.getId(), e);
        }
        return false;
    }

    /**
     * 删除部门
     */
    public boolean deleteDepartment(Long id) {
        String sql = "DELETE FROM departments WHERE id = ?";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "删除部门失败 - ID: " + id, e);
        }
        return false;
    }

    /**
     * 获取所有部门
     */    public List<Department> getAllDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments ORDER BY dept_type, dept_code";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取所有部门失败", e);
        }
        return departments;
    }

    /**
     * 根据部门类型获取部门
     */    public List<Department> getDepartmentsByType(String departmentType) {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE dept_type = ? ORDER BY dept_code";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, departmentType);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departments.add(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据类型获取部门失败 - 部门类型: " + departmentType, e);
        }
        return departments;
    }

    /**
     * 获取所有激活状态的部门
     */    public List<Department> getActiveDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE status = 'active' ORDER BY dept_type, dept_code";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取激活状态部门失败", e);
        }
        return departments;
    }/**
     * 根据部门名称获取部门 (Service层调用的方法名)
     */
    public Department getDepartmentByName(String name) {
        String sql = "SELECT * FROM departments WHERE dept_name = ?";
        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDepartment(rs);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据名称获取部门失败 - 部门名称: " + name, e);
        }
        return null;
    }

    /**
     * 根据父部门ID获取子部门列表 (Service层调用的方法名)
     */    public List<Department> getDepartmentsByParentId(Long parentId) {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE parent_id = ? ORDER BY dept_code";

        try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, parentId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    departments.add(mapResultSetToDepartment(rs));
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "根据父部门ID获取子部门失败 - 父部门ID: " + parentId, e);
        }
        return departments;
    }

    /**
     * 获取顶级部门列表 (Service层调用的方法名)
     */    public List<Department> getTopLevelDepartments() {
        List<Department> departments = new ArrayList<>();
        String sql = "SELECT * FROM departments WHERE parent_id IS NULL ORDER BY dept_type, dept_code";

        try (Connection conn = DBUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                departments.add(mapResultSetToDepartment(rs));
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "获取顶级部门列表失败", e);
        }
        return departments;
    }/**
     * 将ResultSet映射为Department对象
     */
    private Department mapResultSetToDepartment(ResultSet rs) throws SQLException {
        Department department = new Department();
        department.setId(rs.getLong("id"));
        department.setDepartmentCode(rs.getString("dept_code"));
        department.setDepartmentName(rs.getString("dept_name"));
        department.setDepartmentType(rs.getString("dept_type"));
        department.setDescription(rs.getString("description"));
        department.setStatus(rs.getString("status"));

        // 设置父部门ID（如果存在）
        Long parentId = rs.getLong("parent_id");
        if (!rs.wasNull()) {
            department.setParentId(parentId);
        }

        // 设置创建者和更新者ID（如果存在）
        Long createdBy = rs.getLong("created_by");
        if (!rs.wasNull()) {
            department.setCreatedBy(createdBy);
        }
        
        Long updatedBy = rs.getLong("updated_by");
        if (!rs.wasNull()) {
            department.setUpdatedBy(updatedBy);
        }

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
}
