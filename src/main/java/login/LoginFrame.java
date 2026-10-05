package login;

import dao.AdminDAO;
import dao.FacultyDAO;
import dao.AuditDAO;
import dashboard.DashboardFrame;
import database.DatabaseConnection;
import model.Admin;
import model.Faculty;
import utils.ThemeManager;
import utils.ValidationUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JComboBox<String> cmbRole;
    private JButton btnLogin;
    private JButton btnReset;
    private JButton btnExit;
    private JLabel lblForgotPassword;
    
    private final AdminDAO adminDAO = new AdminDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();

    public LoginFrame() {
        // Initialize Look and Feel
        ThemeManager.initializeTheme();
        
        setTitle("QR Code Attendance System - Login");
        setSize(480, 420);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        initUI();
    }

    private void initUI() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title Header
        JLabel lblTitle = new JLabel("SECURE SYSTEM LOGIN", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(new Color(13, 110, 253)); // Corporate Blue
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(10, 8, 25, 8);
        mainPanel.add(lblTitle, gbc);

        // Reset insets
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.gridwidth = 1;

        // Role Dropdown
        JLabel lblRole = new JLabel("Login Role:");
        lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        mainPanel.add(lblRole, gbc);

        cmbRole = new JComboBox<>(new String[]{"Admin", "Faculty"});
        cmbRole.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        mainPanel.add(cmbRole, gbc);

        // Username
        gbc.gridwidth = 1;
        JLabel lblUsername = new JLabel("Username:");
        lblUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        mainPanel.add(lblUsername, gbc);

        txtUsername = new JTextField(15);
        txtUsername.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        mainPanel.add(txtUsername, gbc);

        // Password
        gbc.gridwidth = 1;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        mainPanel.add(lblPassword, gbc);

        txtPassword = new JPasswordField(15);
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        mainPanel.add(txtPassword, gbc);

        // Forgot Password link
        lblForgotPassword = new JLabel("<html><a href=''>Forgot Password? (Admin Only)</a></html>");
        lblForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblForgotPassword.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        mainPanel.add(lblForgotPassword, gbc);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnLogin = new JButton("Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnLogin.setBackground(new Color(13, 110, 253));
        btnLogin.setForeground(Color.WHITE);
        
        btnReset = new JButton("Reset");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        btnExit = new JButton("Exit");
        btnExit.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        buttonPanel.add(btnLogin);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnExit);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(20, 8, 10, 8);
        mainPanel.add(buttonPanel, gbc);

        setContentPane(mainPanel);

        // Event Handling
        btnLogin.addActionListener(this::handleLogin);
        btnReset.addActionListener(e -> clearFields());
        btnExit.addActionListener(e -> System.exit(0));
        
        // Mouse click on Forgot Password
        lblForgotPassword.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if ("Admin".equals(cmbRole.getSelectedItem())) {
                    openForgotPasswordDialog();
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Faculty password reset must be requested from the Administrator.", "Access Denied", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Enter key listeners
        txtUsername.addActionListener(this::handleLogin);
        txtPassword.addActionListener(this::handleLogin);

        // Keyboard shortcuts
        btnLogin.setMnemonic(KeyEvent.VK_L);
        btnReset.setMnemonic(KeyEvent.VK_R);
        btnExit.setMnemonic(KeyEvent.VK_X);
    }

    private void handleLogin(ActionEvent e) {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();
        String role = (String) cmbRole.getSelectedItem();

        if (ValidationUtility.isEmpty(username) || ValidationUtility.isEmpty(password)) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Test DB connection
        if (!DatabaseConnection.testConnection()) {
            JOptionPane.showMessageDialog(this, "Failed to connect to MySQL database! Please check connection settings and ensure MySQL service is running.", "Database Connection Failure", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if ("Admin".equals(role)) {
            Admin admin = adminDAO.validateAdmin(username, password);
            if (admin != null) {
                AuditDAO.log(admin.getUsername(), "Admin", "Logged in successfully");
                JOptionPane.showMessageDialog(this, "Welcome Admin: " + admin.getName() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                
                SwingUtilities.invokeLater(() -> {
                    DashboardFrame dashboard = new DashboardFrame(String.valueOf(admin.getAdminId()), admin.getName(), "Admin");
                    dashboard.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Admin Username or Password!", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            Faculty faculty = facultyDAO.authenticate(username, password);
            if (faculty != null) {
                AuditDAO.log(faculty.getFacultyId(), "Faculty", "Logged in successfully");
                JOptionPane.showMessageDialog(this, "Welcome Faculty: " + faculty.getName() + "!", "Login Success", JOptionPane.INFORMATION_MESSAGE);
                this.dispose();
                
                SwingUtilities.invokeLater(() -> {
                    DashboardFrame dashboard = new DashboardFrame(faculty.getFacultyId(), faculty.getName(), "Faculty");
                    dashboard.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Faculty Username or Password!", "Authentication Failed", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocus();
    }

    private void openForgotPasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setPreferredSize(new Dimension(350, 100));
        
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField newPasswordField = new JPasswordField();

        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Registered Email:"));
        panel.add(emailField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPasswordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Reset Password", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String newPassword = new String(newPasswordField.getPassword()).trim();

            if (ValidationUtility.isEmpty(username) || ValidationUtility.isEmpty(email) || ValidationUtility.isEmpty(newPassword)) {
                JOptionPane.showMessageDialog(this, "All fields are required to reset the password!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtility.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = adminDAO.resetPassword(username, email, newPassword);
            if (success) {
                AuditDAO.log(username, "Admin", "Reset password successfully");
                JOptionPane.showMessageDialog(this, "Password reset successfully! You can now log in with your new password.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Username and Email do not match our records!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
}
