package dao;

import database.DatabaseConnection;
import model.Admin;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AdminDAO {

    public Admin validateAdmin(String username, String password) {
        String query = "SELECT * FROM admin WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, hash)) {
                        Admin admin = new Admin();
                        admin.setAdminId(rs.getInt("admin_id"));
                        admin.setUsername(rs.getString("username"));
                        admin.setName(rs.getString("name"));
                        admin.setEmail(rs.getString("email"));
                        admin.setPasswordHash(hash);
                        return admin;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error validating admin: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Admin getAdminByUsername(String username) {
        String query = "SELECT * FROM admin WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setAdminId(rs.getInt("admin_id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setName(rs.getString("name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPasswordHash(rs.getString("password_hash"));
                    return admin;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting admin: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public Admin getAdminById(int id) {
        String query = "SELECT * FROM admin WHERE admin_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Admin admin = new Admin();
                    admin.setAdminId(rs.getInt("admin_id"));
                    admin.setUsername(rs.getString("username"));
                    admin.setName(rs.getString("name"));
                    admin.setEmail(rs.getString("email"));
                    admin.setPasswordHash(rs.getString("password_hash"));
                    return admin;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting admin by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        Admin admin = validateAdmin(username, oldPassword);
        if (admin == null) return false; // old password didn't match

        String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        String query = "UPDATE admin SET password_hash = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, newHash);
            ps.setString(2, username);
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean resetPassword(String username, String email, String newPassword) {
        // Check if username and email match first
        String queryCheck = "SELECT COUNT(*) FROM admin WHERE username = ? AND email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(queryCheck)) {
            psCheck.setString(1, username);
            psCheck.setString(2, email);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Update
                    String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    String queryUpdate = "UPDATE admin SET password_hash = ? WHERE username = ?";
                    try (PreparedStatement psUpdate = conn.prepareStatement(queryUpdate)) {
                        psUpdate.setString(1, newHash);
                        psUpdate.setString(2, username);
                        return psUpdate.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error resetting password: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAdminProfile(Admin admin) {
        String query = "UPDATE admin SET name = ?, email = ? WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            
            ps.setString(1, admin.getName());
            ps.setString(2, admin.getEmail());
            ps.setString(3, admin.getUsername());
            
            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            System.err.println("Error updating admin profile: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
}
