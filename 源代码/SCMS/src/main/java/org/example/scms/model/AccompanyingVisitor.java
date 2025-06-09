package org.example.scms.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 随行人员实体类
 */
public class AccompanyingVisitor {
    private Long id;
    private String reservationType; // 预约类型: public-社会公众预约, official-公务预约
    private Long reservationId; // 预约ID
    private String name; // 姓名
    private String idCardEncrypted; // 加密的身份证号(SM4)
    private String idCardHash; // 身份证号哈希值(SM3)
    private String phoneEncrypted; // 加密的手机号(SM4)
    private String phoneHash; // 手机号哈希值(SM3)
    private String organization; // 所在单位
    private LocalDateTime createdAt;

    // 构造函数
    public AccompanyingVisitor() {
    }

    public AccompanyingVisitor(String reservationType, Long reservationId, String name,
            String idCardEncrypted, String idCardHash,
            String phoneEncrypted, String phoneHash, String organization) {
        this.reservationType = reservationType;
        this.reservationId = reservationId;
        this.name = name;
        this.idCardEncrypted = idCardEncrypted;
        this.idCardHash = idCardHash;
        this.phoneEncrypted = phoneEncrypted;
        this.phoneHash = phoneHash;
        this.organization = organization;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservationType() {
        return reservationType;
    }

    public void setReservationType(String reservationType) {
        this.reservationType = reservationType;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public void setReservationId(Long reservationId) {
        this.reservationId = reservationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdCardEncrypted() {
        return idCardEncrypted;
    }

    public void setIdCardEncrypted(String idCardEncrypted) {
        this.idCardEncrypted = idCardEncrypted;
    }

    public String getIdCardHash() {
        return idCardHash;
    }

    public void setIdCardHash(String idCardHash) {
        this.idCardHash = idCardHash;
    }

    public String getPhoneEncrypted() {
        return phoneEncrypted;
    }

    public void setPhoneEncrypted(String phoneEncrypted) {
        this.phoneEncrypted = phoneEncrypted;
    }

    public String getPhoneHash() {
        return phoneHash;
    }

    public void setPhoneHash(String phoneHash) {
        this.phoneHash = phoneHash;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AccompanyingVisitor{" +
                "id=" + id +
                ", reservationType='" + reservationType + '\'' +
                ", reservationId=" + reservationId +
                ", name='" + name + '\'' +
                '}';
    }
}
