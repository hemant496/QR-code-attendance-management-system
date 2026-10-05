package dao;

import database.DatabaseConnection;
import model.Subject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public List<Subject> getAllSubjects() {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name FROM subject s " +
                     "JOIN course c ON s.course_id = c.course_id " +
                     "ORDER BY s.subject_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Subject sub = new Subject(
                        rs.getString("subject_id"),
                        rs.getString("subject_code"),
                        rs.getString("subject_name"),
                        rs.getString("course_id")
                );
                sub.setCourseName(rs.getString("course_name"));
                list.add(sub);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Subject> getSubjectsByCourseId(String courseId) {
        List<Subject> list = new ArrayList<>();
        String sql = "SELECT s.*, c.course_name FROM subject s " +
                     "JOIN course c ON s.course_id = c.course_id " +
                     "WHERE s.course_id = ? ORDER BY s.subject_code";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Subject sub = new Subject(
                            rs.getString("subject_id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getString("course_id")
                    );
                    sub.setCourseName(rs.getString("course_name"));
                    list.add(sub);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insertSubject(Subject s) {
        String sql = "INSERT INTO subject (subject_id, subject_code, subject_name, course_id) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSubjectId());
            ps.setString(2, s.getSubjectCode());
            ps.setString(3, s.getSubjectName());
            ps.setString(4, s.getCourseId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSubject(Subject s) {
        String sql = "UPDATE subject SET subject_code = ?, subject_name = ?, course_id = ? WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getSubjectCode());
            ps.setString(2, s.getSubjectName());
            ps.setString(3, s.getCourseId());
            ps.setString(4, s.getSubjectId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSubject(String id) {
        String sql = "DELETE FROM subject WHERE subject_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
