package dao;

import database.DatabaseConnection;
import model.Student;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class StudentDAO {

    public boolean addStudent(Student student) {
        String query = "INSERT INTO student (student_id, roll_no, registration_no, name, course_id, semester, section, mobile, email, qr_path, photo_path, status) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getRollNo());
            ps.setString(3, student.getRegistrationNo());
            ps.setString(4, student.getName());
            ps.setString(5, student.getCourseId());
            ps.setString(6, student.getSemester());
            ps.setString(7, student.getSection());
            ps.setString(8, student.getMobile());
            ps.setString(9, student.getEmail());
            ps.setString(10, student.getQrPath());
            ps.setString(11, student.getPhotoPath());
            ps.setString(12, student.getStatus() == null ? "Active" : student.getStatus());
            
            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                AuditDAO.log("System", "Admin", "Added student " + student.getStudentId() + " (" + student.getName() + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error adding student: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateStudent(Student student) {
        String query = "UPDATE student SET roll_no = ?, registration_no = ?, name = ?, course_id = ?, semester = ?, section = ?, mobile = ?, email = ?, qr_path = ?, photo_path = ?, status = ? " +
                       "WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, student.getRollNo());
            ps.setString(2, student.getRegistrationNo());
            ps.setString(3, student.getName());
            ps.setString(4, student.getCourseId());
            ps.setString(5, student.getSemester());
            ps.setString(6, student.getSection());
            ps.setString(7, student.getMobile());
            ps.setString(8, student.getEmail());
            ps.setString(9, student.getQrPath());
            ps.setString(10, student.getPhotoPath());
            ps.setString(11, student.getStatus());
            ps.setString(12, student.getStudentId());
            
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                AuditDAO.log("System", "Admin", "Updated student " + student.getStudentId());
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteStudent(String studentId) {
        String query = "DELETE FROM student WHERE student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, studentId);
            int rowsDeleted = ps.executeUpdate();
            if (rowsDeleted > 0) {
                AuditDAO.log("System", "Admin", "Deleted student " + studentId);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public Student getStudentById(String studentId) {
        String query = "SELECT s.*, c.course_name, c.department FROM student s " +
                       "JOIN course c ON s.course_id = c.course_id " +
                       "WHERE s.student_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStudent(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching student by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String query = "SELECT s.*, c.course_name, c.department FROM student s " +
                       "JOIN course c ON s.course_id = c.course_id " +
                       "ORDER BY s.student_id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all students: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }

    public List<Student> getFilteredStudents(String searchTerm, String courseId, String semester, String section, String status, int limit, int offset) {
        List<Student> students = new ArrayList<>();
        StringBuilder query = new StringBuilder(
                "SELECT s.*, c.course_name, c.department FROM student s " +
                "JOIN course c ON s.course_id = c.course_id WHERE 1=1 "
        );

        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.append("AND (s.student_id LIKE ? OR s.name LIKE ? OR s.roll_no LIKE ? OR s.registration_no LIKE ? OR s.email LIKE ?) ");
            String wildCard = "%" + searchTerm.trim() + "%";
            for (int i = 0; i < 5; i++) params.add(wildCard);
        }
        if (courseId != null && !courseId.equals("All")) {
            query.append("AND s.course_id = ? ");
            params.add(courseId);
        }
        if (semester != null && !semester.equals("All")) {
            query.append("AND s.semester = ? ");
            params.add(semester);
        }
        if (section != null && !section.equals("All")) {
            query.append("AND s.section = ? ");
            params.add(section);
        }
        if (status != null && !status.equals("All")) {
            query.append("AND s.status = ? ");
            params.add(status);
        }

        query.append("ORDER BY s.student_id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return students;
    }

    public int getFilteredStudentCount(String searchTerm, String courseId, String semester, String section, String status) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM student s WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.append("AND (s.student_id LIKE ? OR s.name LIKE ? OR s.roll_no LIKE ? OR s.registration_no LIKE ? OR s.email LIKE ?) ");
            String wildCard = "%" + searchTerm.trim() + "%";
            for (int i = 0; i < 5; i++) params.add(wildCard);
        }
        if (courseId != null && !courseId.equals("All")) {
            query.append("AND s.course_id = ? ");
            params.add(courseId);
        }
        if (semester != null && !semester.equals("All")) {
            query.append("AND s.semester = ? ");
            params.add(semester);
        }
        if (section != null && !section.equals("All")) {
            query.append("AND s.section = ? ");
            params.add(section);
        }
        if (status != null && !status.equals("All")) {
            query.append("AND s.status = ? ");
            params.add(status);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public String getLastStudentId() {
        String query = "SELECT student_id FROM student ORDER BY CAST(SUBSTRING(student_id, 4) AS UNSIGNED) DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                return rs.getString("student_id");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching last student ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean isRollNoExists(String rollNo) {
        String query = "SELECT COUNT(*) FROM student WHERE roll_no = ?";
        return checkExists(query, rollNo);
    }

    public boolean isRollNoExists(String rollNo, String excludeStudentId) {
        String query = "SELECT COUNT(*) FROM student WHERE roll_no = ? AND student_id != ?";
        return checkExists(query, rollNo, excludeStudentId);
    }

    public boolean isRegNoExists(String regNo) {
        String query = "SELECT COUNT(*) FROM student WHERE registration_no = ?";
        return checkExists(query, regNo);
    }

    public boolean isRegNoExists(String regNo, String excludeStudentId) {
        String query = "SELECT COUNT(*) FROM student WHERE registration_no = ? AND student_id != ?";
        return checkExists(query, regNo, excludeStudentId);
    }

    public int getStudentCount() {
        String query = "SELECT COUNT(*) FROM student";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private boolean checkExists(String query, String... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            for (int i = 0; i < params.length; i++) {
                ps.setString(i + 1, params[i]);
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setStudentId(rs.getString("student_id"));
        student.setRollNo(rs.getString("roll_no"));
        student.setRegistrationNo(rs.getString("registration_no"));
        student.setName(rs.getString("name"));
        student.setCourseId(rs.getString("course_id"));
        student.setCourseName(rs.getString("course_name"));
        student.setDepartment(rs.getString("department"));
        student.setSemester(rs.getString("semester"));
        student.setSection(rs.getString("section"));
        student.setMobile(rs.getString("mobile"));
        student.setEmail(rs.getString("email"));
        student.setQrPath(rs.getString("qr_path"));
        student.setPhotoPath(rs.getString("photo_path"));
        student.setStatus(rs.getString("status"));
        student.setCreatedAt(rs.getTimestamp("created_at"));
        return student;
    }
}
