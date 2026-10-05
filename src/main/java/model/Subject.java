package model;

public class Subject {
    private String subjectId;
    private String subjectCode;
    private String subjectName;
    private String courseId;
    private String courseName; // Helper JOIN

    public Subject() {}

    public Subject(String subjectId, String subjectCode, String subjectName, String courseId) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.courseId = courseId;
    }

    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    
    @Override
    public String toString() {
        return subjectCode + " - " + subjectName;
    }
}
