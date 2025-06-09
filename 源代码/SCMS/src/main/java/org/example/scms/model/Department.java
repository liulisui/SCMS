package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 部门实体类
 */
public class Department {
    private Long id;
    private String departmentCode; // 部门编号
    private String departmentName; // 部门名称
    private String departmentType; // 部门类型：ADMINISTRATIVE(行政部门)、DIRECT(直属部门)、COLLEGE(学院)
    private Long parentId; // 父部门ID
    private String description; // 部门描述
    private String status; // 状态：active-激活, inactive-未激活
    private Long createdBy; // 创建者ID
    private Long updatedBy; // 更新者ID
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Department() {
    }

    public Department(String departmentCode, String departmentName, String departmentType, String description) {
        this.departmentCode = departmentCode;
        this.departmentName = departmentName;
        this.departmentType = departmentType;
        this.description = description;
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

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getDepartmentType() {
        return departmentType;
    }

    public void setDepartmentType(String departmentType) {
        this.departmentType = departmentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Long getParentId() {
        return parentId;
    }    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    // 别名方法，为兼容Service层调用
    public String getName() {
        return departmentName;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + id +
                ", departmentCode='" + departmentCode + '\'' +
                ", departmentName='" + departmentName + '\'' +
                ", departmentType='" + departmentType + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}