package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 部门-校区映射实体类
 * 用于管理部门对校区的预约管理权限
 */
public class DepartmentCampusMapping {
    private Long id;
    private Long departmentId; // 部门ID
    private Long campusId; // 校区ID
    private String permissionType; // 权限类型：public-公共预约, official-公务预约, both-全部
    private String status; // 状态：active-激活, inactive-未激活
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联对象
    private Department department;
    private Campus campus;

    // 构造函数
    public DepartmentCampusMapping() {
    }

    public DepartmentCampusMapping(Long departmentId, Long campusId, String permissionType) {
        this.departmentId = departmentId;
        this.campusId = campusId;
        this.permissionType = permissionType;
        this.status = "active";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getCampusId() {
        return campusId;
    }

    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }

    public String getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(String permissionType) {
        this.permissionType = permissionType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Campus getCampus() {
        return campus;
    }

    public void setCampus(Campus campus) {
        this.campus = campus;
    }

    @Override
    public String toString() {
        return "DepartmentCampusMapping{" +
                "id=" + id +
                ", departmentId=" + departmentId +
                ", campusId=" + campusId +
                ", permissionType='" + permissionType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
