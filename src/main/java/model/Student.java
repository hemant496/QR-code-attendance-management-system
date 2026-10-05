package model;

import java.sql.Timestamp;

public class Student {
    private String studentId;
    private String rollNo;
    private String registrationNo;
    private String name;
    private String courseId;      // FK to course
    private String courseName;    // derived from JOIN
    private String department;    // derived from course JOIN
    private String semester;
    private String section;
    private String mobile;
    private String email;
    private String qrPath;
    private String photoPath;
    private String status;        // Active, Inactive
    private Timestamp createdAt;

    public Student() {}

    public Student(String studentId, String rollNo, String registrationNo, String name, String courseId, 
                   String semester, String section, String mobile, String email, String qrPath, String photoPath, String status) {
        this.studentId = studentId;
        this.rollNo = rollNo;
        this.registrationNo = registrationNo;
        this.name = name;
        this.courseId = courseId;
        this.semester = semester;
        this.section = section;
        this.mobile = mobile;
        this.email = email;
        this.qrPath = qrPath;
        this.photoPath = photoPath;
        this.status = status;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getRegistrationNo() {
        return registrationNo;
    }

    public void setRegistrationNo(String registrationNo) {
        this.registrationNo = registrationNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getQrPath() {
        return qrPath;
    }

    public void setQrPath(String qrPath) {
        this.qrPath = qrPath;
    }

    public String getPhotoPath() {
        return photoPath;
    }

    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
