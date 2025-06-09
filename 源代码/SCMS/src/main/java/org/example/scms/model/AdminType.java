package org.example.scms.model;

/**
 * 管理员类型枚举
 */
public enum AdminType {
    SCHOOL("SCHOOL", "学校管理员", "具有最高权限的学校管理员"),
    DEPARTMENT("DEPARTMENT", "部门管理员", "管理本部门事务的部门管理员"),
    AUDIT("AUDIT", "审计管理员", "负责查看审计日志的审计管理员"),
    SYSTEM("SYSTEM", "系统管理员", "负责系统维护的系统管理员");

    private final String code;
    private final String name;
    private final String description;

    AdminType(String code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // 根据code获取枚举
    public static AdminType getByCode(String code) {
        for (AdminType type : AdminType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}