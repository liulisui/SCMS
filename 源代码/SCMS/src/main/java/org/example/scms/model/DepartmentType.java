package org.example.scms.model;

/**
 * 部门类型枚举
 */
public enum DepartmentType {
    ADMINISTRATIVE("ADMINISTRATIVE", "行政部门", "学校的行政管理部门"),
    DIRECT("DIRECT", "直属部门", "学校的直属机构"),
    COLLEGE("COLLEGE", "学院", "学校的教学学院");

    private final String code;
    private final String name;
    private final String description;

    DepartmentType(String code, String name, String description) {
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
    public static DepartmentType getByCode(String code) {
        for (DepartmentType type : DepartmentType.values()) {
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
