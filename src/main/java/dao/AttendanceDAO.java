package dao;

import database.DatabaseConnection;
import model.Attendance;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AttendanceDAO {

    public boolean markAttendance(String studentId, String subjectId, String facultyId, String status) {
        String query = "INSERT INTO attendance (student_id, subject_id, faculty_id, date, time, status) VALUES (?, ?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE time = VALUES(time), status = VALUES(status)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, studentId);
            ps.setString(2, subjectId);
            ps.setString(3, facultyId);
            ps.setDate(4, Date.valueOf(LocalDate.now()));
            ps.setTime(5, Time.valueOf(LocalTime.now()));
            ps.setString(6, status);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                AuditDAO.log(facultyId, "Faculty", "Marked attendance for student " + studentId + " (Status: " + status + ")");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error marking attendance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean isAttendanceMarkedToday(String studentId, String subjectId) {
        String query = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND subject_id = ? AND date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, studentId);
            ps.setString(2, subjectId);
            ps.setDate(3, Date.valueOf(LocalDate.now()));
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking today's attendance: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Attendance> getAttendanceReport(String searchTerm, String courseId, String subjectId, Date startDate, Date endDate) {
        List<Attendance> list = new ArrayList<>();
        StringBuilder query = new StringBuilder(
            "SELECT a.*, s.name AS student_name, sub.subject_name, f.name AS faculty_name FROM attendance a " +
            "JOIN student s ON a.student_id = s.student_id " +
            "JOIN subject sub ON a.subject_id = sub.subject_id " +
            "JOIN faculty f ON a.faculty_id = f.faculty_id " +
            "WHERE 1=1 "
        );
        
        List<Object> params = new ArrayList<>();
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            query.append("AND (a.student_id LIKE ? OR s.name LIKE ?) ");
            String wildCard = "%" + searchTerm.trim() + "%";
            params.add(wildCard);
            params.add(wildCard);
        }
        if (courseId != null && !courseId.equals("All")) {
            query.append("AND s.course_id = ? ");
            params.add(courseId);
        }
        if (subjectId != null && !subjectId.equals("All")) {
            query.append("AND a.subject_id = ? ");
            params.add(subjectId);
        }
        if (startDate != null) {
            query.append("AND a.date >= ? ");
            params.add(startDate);
        }
        if (endDate != null) {
            query.append("AND a.date <= ? ");
            params.add(endDate);
        }
        
        query.append("ORDER BY a.date DESC, a.time DESC");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    ps.setString(i + 1, (String) p);
                } else if (p instanceof Date) {
                    ps.setDate(i + 1, (Date) p);
                }
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Attendance att = new Attendance();
                    att.setAttendanceId(rs.getInt("attendance_id"));
                    att.setStudentId(rs.getString("student_id"));
                    att.setStudentName(rs.getString("student_name"));
                    att.setSubjectId(rs.getString("subject_id"));
                    att.setSubjectName(rs.getString("subject_name"));
                    att.setFacultyId(rs.getString("faculty_id"));
                    att.setFacultyName(rs.getString("faculty_name"));
                    att.setDate(rs.getDate("date"));
                    att.setTime(rs.getTime("time"));
                    att.setStatus(rs.getString("status"));
                    list.add(att);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting attendance report: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateAttendanceStatus(int id, String status) {
        String sql = "UPDATE attendance SET status = ? WHERE attendance_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public int getTodayAttendanceCount(String status) {
        String query = "SELECT COUNT(*) FROM attendance WHERE date = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            ps.setString(2, status);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting count: " + e.getMessage());
        }
        return 0;
    }

    public int getTodayTotalMarked() {
        String query = "SELECT COUNT(*) FROM attendance WHERE date = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getStudentAttendancePercentage(String studentId) {
        String totalDaysQuery = "SELECT COUNT(DISTINCT date) FROM attendance WHERE student_id IN (SELECT student_id FROM student WHERE course_id = (SELECT course_id FROM student WHERE student_id = ?))";
        String presentDaysQuery = "SELECT COUNT(*) FROM attendance WHERE student_id = ? AND (status = 'Present' OR status = 'Late')";
        
        double totalDays = 0;
        double presentDays = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(totalDaysQuery)) {
                ps1.setString(1, studentId);
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next()) totalDays = rs1.getInt(1);
                }
            }
            try (PreparedStatement ps2 = conn.prepareStatement(presentDaysQuery)) {
                ps2.setString(1, studentId);
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next()) presentDays = rs2.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        if (totalDays == 0) return 100.0;
        return (presentDays / totalDays) * 100.0;
    }

    public Map<String, Integer> getWeeklyTrend() {
        Map<String, Integer> trend = new LinkedHashMap<>();
        String query = "SELECT date, COUNT(*) as present_count FROM attendance " +
                       "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                       "GROUP BY date ORDER BY date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                trend.put(rs.getDate("date").toString(), rs.getInt("present_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return trend;
    }

    public List<Map<String, Object>> getTopAttendanceStudents(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.name, (COUNT(CASE WHEN a.status IN ('Present', 'Late') THEN 1 END) / COUNT(a.attendance_id) * 100) as percentage " +
                     "FROM attendance a JOIN student s ON a.student_id = s.student_id " +
                     "GROUP BY s.student_id, s.name HAVING percentage >= 75 ORDER BY percentage DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getString("student_id"));
                    map.put("name", rs.getString("name"));
                    map.put("percentage", rs.getDouble("percentage"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Map<String, Object>> getLowAttendanceStudents(int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = "SELECT s.student_id, s.name, (COUNT(CASE WHEN a.status IN ('Present', 'Late') THEN 1 END) / COUNT(a.attendance_id) * 100) as percentage " +
                     "FROM attendance a JOIN student s ON a.student_id = s.student_id " +
                     "GROUP BY s.student_id, s.name HAVING percentage < 75 ORDER BY percentage ASC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", rs.getString("student_id"));
                    map.put("name", rs.getString("name"));
                    map.put("percentage", rs.getDouble("percentage"));
                    list.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Map<String, Integer> getDepartmentWiseStats() {
        Map<String, Integer> stats = new HashMap<>();
        String query = "SELECT c.department, COUNT(a.attendance_id) AS present_count " +
                       "FROM attendance a " +
                       "JOIN student s ON a.student_id = s.student_id " +
                       "JOIN course c ON s.course_id = c.course_id " +
                       "WHERE a.date = ? AND a.status IN ('Present', 'Late') " +
                       "GROUP BY c.department";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setDate(1, Date.valueOf(LocalDate.now()));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stats.put(rs.getString("department"), rs.getInt("present_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stats;
    }
}
