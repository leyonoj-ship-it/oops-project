package com.smartdine.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * OOP INHERITANCE:
 * Admin extends Person, providing platform-wide administrative controls,
 * system oversight, user suspension, and analytics auditing.
 */
@Entity
@Table(name = "admins")
public class Admin extends Person {

    @Column(name = "admin_department", length = 100)
    private String adminDepartment = "Platform Administration";

    @Column(name = "clearance_level")
    private int clearanceLevel = 1;

    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }

    public Admin(String name, String email, String phone, String department, int clearanceLevel) {
        super(name, email, phone, UserRole.ADMIN);
        this.adminDepartment = department;
        this.clearanceLevel = clearanceLevel;
    }

    @Override
    public String getRoleDescription() {
        return "Admin: Platform Administrator with system-level access to users, establishments, turnover, and reviews.";
    }

    @Override
    public boolean canPerformAdminActions() {
        return true;
    }

    @Override
    public boolean canManageEstablishment() {
        return true;
    }

    public String getAdminDepartment() {
        return adminDepartment;
    }

    public void setAdminDepartment(String adminDepartment) {
        this.adminDepartment = adminDepartment;
    }

    public int getClearanceLevel() {
        return clearanceLevel;
    }

    public void setClearanceLevel(int clearanceLevel) {
        this.clearanceLevel = clearanceLevel;
    }
}
