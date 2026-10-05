package dao;

import database.DatabaseConnection;
import model.Faculty;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FacultyDAO {

    public Faculty authenticate(String username, String password) {
        String sql = "SELECT * FROM faculty WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, hash)) {
                        return new Faculty(
                                rs.getString("faculty_id"),
                                rs.getString("username"),
                                hash,
                                rs.getString("name"),
                                rs.getString("department"),
                                rs.getString("email"),
                                rs.getString("mobile")
                        );
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Faculty> getAllFaculty() {
        List<Faculty> list = new ArrayList<>();
        String sql = "SELECT * FROM faculty ORDER BY name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Faculty(
                        rs.getString("faculty_id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("name"),
                        rs.getString("department"),
                        rs.getString("email"),
                        rs.getString("mobile")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public Faculty getFacultyById(String id) {
        String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Faculty(
                            rs.getString("faculty_id"),
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("name"),
                            rs.getString("department"),
                            rs.getString("email"),
                            rs.getString("mobile")
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertFaculty(Faculty f) {
        String sql = "INSERT INTO faculty (faculty_id, username, password_hash, name, department, email, mobile) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getFacultyId());
            ps.setString(2, f.getUsername());
            ps.setString(3, f.getPasswordHash());
            ps.setString(4, f.getName());
            ps.setString(5, f.getDepartment());
            ps.setString(6, f.getEmail());
            ps.setString(7, f.getMobile());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateFaculty(Faculty f) {
        String sql = "UPDATE faculty SET username = ?, name = ?, department = ?, email = ?, mobile = ? WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, f.getUsername());
            ps.setString(2, f.getName());
            ps.setString(3, f.getDepartment());
            ps.setString(4, f.getEmail());
            ps.setString(5, f.getMobile());
            ps.setString(6, f.getFacultyId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteFaculty(String id) {
        String sql = "DELETE FROM faculty WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String id, String newPasswordHash) {
        String sql = "UPDATE faculty SET password_hash = ? WHERE faculty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
