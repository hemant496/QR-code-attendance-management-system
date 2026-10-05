package model;

import java.sql.Timestamp;

public class Faculty {
    private String facultyId;
    private String username;
    private String passwordHash;
    private String name;
    private String department;
    private String email;
    private String mobile;
    private Timestamp createdAt;

    public Faculty() {}

    public Faculty(String facultyId, String username, String passwordHash, String name, String department, String email, String mobile) {
        this.facultyId = facultyId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.department = department;
        this.email = email;
        this.mobile = mobile;
    }

    public String getFacultyId() { return facultyId; }
    public void setFacultyId(String facultyId) { this.facultyId = facultyId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
