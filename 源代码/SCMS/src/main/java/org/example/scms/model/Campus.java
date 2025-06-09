package org.example.scms.model;

import java.time.LocalDateTime;

/**
 * 校区实体类
 */
public class Campus {
    private Long id;
    private String campusCode; // 校区编码
    private String campusName; // 校区名称
    private String address; // 校区地址
    private String description; // 校区描述
    private String status; // 状态：active-激活, inactive-未激活
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 构造函数
    public Campus() {
    }

    public Campus(String campusCode, String campusName, String address, String description) {
        this.campusCode = campusCode;
        this.campusName = campusName;
        this.address = address;
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

    public String getCampusCode() {
        return campusCode;
    }

    public void setCampusCode(String campusCode) {
        this.campusCode = campusCode;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    @Override
    public String toString() {
        return "Campus{" +
                "id=" + id +
                ", campusCode='" + campusCode + '\'' +
                ", campusName='" + campusName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
