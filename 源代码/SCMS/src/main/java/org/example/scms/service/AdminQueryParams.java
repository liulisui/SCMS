package org.example.scms.service;

import java.time.LocalDate;

/**
 * 管理员查询参数类
 */
public class AdminQueryParams {
    private String reservationType; // 预约类型: public, official, all
    private String status; // 状态筛选
    private Long campusId; // 校区ID
    private Long departmentId; // 部门ID（仅公务预约）
    private LocalDate startDate; // 开始日期
    private LocalDate endDate; // 结束日期

    private String visitorName; // 访客姓名
    private String visitorIdCard; // 访客身份证号
    private int page = 1; // 页码
    private int pageSize = 20; // 每页数量
    
    public AdminQueryParams() {}
    
    // Getters and Setters
    public String getReservationType() {
        return reservationType;
    }
    
    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getCampusId() {
        return campusId;
    }
    
    public void setCampusId(Long campusId) {
        this.campusId = campusId;
    }
    
    public Long getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
  
    
    public String getVisitorName() {
        return visitorName;
    }
    
    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }
    
    public String getVisitorIdCard() {
        return visitorIdCard;
    }
    
    public void setVisitorIdCard(String visitorIdCard) {
        this.visitorIdCard = visitorIdCard;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = Math.max(1, page);
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = Math.max(1, Math.min(100, pageSize));
    }
}
