package org.example.scms.service;

import java.util.List;

/**
 * 管理员查询结果类
 */
public class AdminQueryResult {
    private List<ReservationQueryItem> items;
    private int total;
    private int page;
    private int pageSize;
    private int totalPages;
    private String error;
    
    public AdminQueryResult() {}
    
    // Getters and Setters
    public List<ReservationQueryItem> getItems() {
        return items;
    }
    
    public void setItems(List<ReservationQueryItem> items) {
        this.items = items;
    }
    
    public int getTotal() {
        return total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public int getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}
