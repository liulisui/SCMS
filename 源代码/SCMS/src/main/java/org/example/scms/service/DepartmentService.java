package org.example.scms.service;

import java.time.LocalDateTime;
import java.util.List;

import org.example.scms.dao.AuditLogDAO;
import org.example.scms.dao.DepartmentDAO;
import org.example.scms.dao.ReservationAuthorizationDAO;
import org.example.scms.model.AuditLog;
import org.example.scms.model.Department;
import org.example.scms.model.ReservationAuthorization;

/**
 * 部门服务类
 * 处理部门管理相关的业务逻辑
 */
public class DepartmentService {
    private DepartmentDAO departmentDAO;
    private AuditLogDAO auditLogDAO;
    private ReservationAuthorizationDAO authorizationDAO;

    public DepartmentService() {
        this.departmentDAO = new DepartmentDAO();
        this.auditLogDAO = new AuditLogDAO();
        this.authorizationDAO = new ReservationAuthorizationDAO();
    }

    /**
     * 添加部门
     */
    public boolean addDepartment(Department department, Long operatorId, String ipAddress, String userAgent) {
        try {
            Department newDepartment = departmentDAO.addDepartment(department);
            if (newDepartment != null) {
                logAuditEvent(operatorId, "CREATE", "Department", newDepartment.getId(),
                        "创建部门: " + department.getName(), ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "CREATE_FAILED", "Department", null,
                    "创建部门失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 更新部门信息
     */
    public boolean updateDepartment(Department department, Long operatorId, String ipAddress, String userAgent) {
        try {
            Department originalDepartment = departmentDAO.getDepartmentById(department.getId());
            if (originalDepartment == null) {
                return false;
            }

            boolean success = departmentDAO.updateDepartment(department);
            if (success) {
                logAuditEvent(operatorId, "UPDATE", "Department", department.getId(),
                        "更新部门信息: " + department.getName(), ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "UPDATE_FAILED", "Department", department.getId(),
                    "更新部门失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 删除部门
     */
    public boolean deleteDepartment(Long departmentId, Long operatorId, String ipAddress, String userAgent) {
        try {
            Department department = departmentDAO.getDepartmentById(departmentId);
            if (department == null) {
                return false;
            }

            // 检查是否有子部门
            List<Department> childDepartments = departmentDAO.getDepartmentsByParentId(departmentId);
            if (!childDepartments.isEmpty()) {
                logAuditEvent(operatorId, "DELETE_FAILED", "Department", departmentId,
                        "删除部门失败: 存在子部门", ipAddress, userAgent);
                return false;
            }

            // 检查是否有预约授权
            List<ReservationAuthorization> authorizations = authorizationDAO
                    .getAuthorizationsByDepartment(departmentId);
            if (!authorizations.isEmpty()) {
                logAuditEvent(operatorId, "DELETE_FAILED", "Department", departmentId,
                        "删除部门失败: 存在预约授权", ipAddress, userAgent);
                return false;
            }

            boolean success = departmentDAO.deleteDepartment(departmentId);
            if (success) {
                logAuditEvent(operatorId, "DELETE", "Department", departmentId,
                        "删除部门: " + department.getName(), ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "DELETE_FAILED", "Department", departmentId,
                    "删除部门失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 根据ID获取部门
     */
    public Department getDepartmentById(Long id) {
        return departmentDAO.getDepartmentById(id);
    }

    /**
     * 根据名称获取部门
     */
    public Department getDepartmentByName(String name) {
        return departmentDAO.getDepartmentByName(name);
    }

    /**
     * 获取所有部门
     */
    public List<Department> getAllDepartments() {
        return departmentDAO.getAllDepartments();
    }

    /**
     * 根据父部门ID获取子部门列表
     */
    public List<Department> getDepartmentsByParentId(Long parentId) {
        return departmentDAO.getDepartmentsByParentId(parentId);
    }

    /**
     * 根据部门类型获取部门列表
     */
    public List<Department> getDepartmentsByType(String type) {
        return departmentDAO.getDepartmentsByType(type);
    }

    /**
     * 获取顶级部门列表
     */
    public List<Department> getTopLevelDepartments() {
        return departmentDAO.getTopLevelDepartments();
    }

    /**
     * 获取活跃的部门列表
     */
    public List<Department> getActiveDepartments() {
        return departmentDAO.getActiveDepartments();
    }

    /**
     * 激活部门
     */
    public boolean activateDepartment(Long departmentId, Long operatorId, String ipAddress, String userAgent) {
        try {
            Department department = departmentDAO.getDepartmentById(departmentId);
            if (department == null) {
                return false;
            }

            department.setStatus("active");
            department.setUpdatedAt(LocalDateTime.now());
            
            boolean success = departmentDAO.updateDepartment(department);
            if (success) {
                logAuditEvent(operatorId, "ACTIVATE", "Department", departmentId,
                        "激活部门: " + department.getName(), ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "ACTIVATE_FAILED", "Department", departmentId,
                    "激活部门失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 停用部门
     */
    public boolean deactivateDepartment(Long departmentId, Long operatorId, String ipAddress, String userAgent) {
        try {
            Department department = departmentDAO.getDepartmentById(departmentId);
            if (department == null) {
                return false;
            }

            department.setStatus("inactive");
            department.setUpdatedAt(LocalDateTime.now());
            
            boolean success = departmentDAO.updateDepartment(department);
            if (success) {
                logAuditEvent(operatorId, "DEACTIVATE", "Department", departmentId,
                        "停用部门: " + department.getName(), ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "DEACTIVATE_FAILED", "Department", departmentId,
                    "停用部门失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }    /**
     * 获取部门树形结构
     */
    public List<Department> getDepartmentTree() {
        List<Department> allDepartments = departmentDAO.getAllDepartments();
        return buildDepartmentTree(allDepartments, null);
    }

    /**
     * 构建部门树形结构
     */
    private List<Department> buildDepartmentTree(List<Department> allDepartments, Long parentId) {
        return allDepartments.stream()
                .filter(dept -> (parentId == null && dept.getParentId() == null) ||
                        (parentId != null && parentId.equals(dept.getParentId())))
                .peek(dept -> {
                    // 递归构建子部门树（如果需要在Department中设置children字段，可以在这里处理）
                    buildDepartmentTree(allDepartments, dept.getId());
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 检查部门是否有预约权限
     */
    public boolean hasReservationPermission(Long departmentId, String reservationType) {
        List<ReservationAuthorization> authorizations = authorizationDAO.getAuthorizationsByDepartment(departmentId);
        return authorizations.stream()
                .anyMatch(auth -> "active".equals(auth.getStatus()) &&
                        (reservationType.equals(auth.getReservationType()) ||
                                "both".equals(auth.getReservationType())));
    }

    /**
     * 授予部门预约权限
     */
    public boolean grantReservationPermission(Long departmentId, String reservationType,
            Long operatorId, String description,
            String ipAddress, String userAgent) {
        try {
            // 检查是否已有相同类型的权限
            List<ReservationAuthorization> existingAuths = authorizationDAO.getAuthorizationsByDepartment(departmentId);
            boolean hasExisting = existingAuths.stream()
                    .anyMatch(auth -> "active".equals(auth.getStatus()) &&
                            (reservationType.equals(auth.getReservationType()) ||
                                    "both".equals(auth.getReservationType())));

            if (hasExisting) {
                logAuditEvent(operatorId, "GRANT_PERMISSION_FAILED", "ReservationAuthorization", departmentId,
                        "部门已有该类型预约权限: " + reservationType, ipAddress, userAgent);
                return false;
            }

            ReservationAuthorization authorization = new ReservationAuthorization(
                    departmentId, reservationType, operatorId, description);

            ReservationAuthorization newAuth = authorizationDAO.addAuthorization(authorization);
            if (newAuth != null) {
                logAuditEvent(operatorId, "GRANT_PERMISSION", "ReservationAuthorization", newAuth.getId(),
                        "授予部门预约权限: " + reservationType, ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "GRANT_PERMISSION_FAILED", "ReservationAuthorization", departmentId,
                    "授予预约权限失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 撤销部门预约权限
     */
    public boolean revokeReservationPermission(Long authorizationId, Long operatorId,
            String ipAddress, String userAgent) {
        try {
            ReservationAuthorization authorization = authorizationDAO.getAuthorizationById(authorizationId);
            if (authorization == null) {
                return false;
            }

            boolean success = authorizationDAO.revokeAuthorization(authorizationId, LocalDateTime.now());
            if (success) {
                logAuditEvent(operatorId, "REVOKE_PERMISSION", "ReservationAuthorization", authorizationId,
                        "撤销部门预约权限", ipAddress, userAgent);
                return true;
            }
        } catch (Exception e) {
            logAuditEvent(operatorId, "REVOKE_PERMISSION_FAILED", "ReservationAuthorization", authorizationId,
                    "撤销预约权限失败: " + e.getMessage(), ipAddress, userAgent);
        }
        return false;
    }

    /**
     * 获取部门的预约授权列表
     */
    public List<ReservationAuthorization> getDepartmentAuthorizations(Long departmentId) {
        return authorizationDAO.getAuthorizationsByDepartment(departmentId);
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
