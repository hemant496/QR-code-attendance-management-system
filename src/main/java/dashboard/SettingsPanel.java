package dashboard;

import dao.AdminDAO;
import dao.FacultyDAO;
import dao.SettingsDAO;
import model.Admin;
import model.Faculty;
import org.mindrot.jbcrypt.BCrypt;
import utils.DatabaseBackupUtility;
import utils.ThemeManager;
import utils.ValidationUtility;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class SettingsPanel extends JPanel {
    private final String sessionUserId;
    private final String sessionUserRole;
    private final JFrame parentFrame;

    private final AdminDAO adminDAO = new AdminDAO();
    private final FacultyDAO facultyDAO = new FacultyDAO();
    private final SettingsDAO settingsDAO = new SettingsDAO();

    private JPasswordField txtOldPassword;
    private JPasswordField txtNewPassword;
    private JPasswordField txtConfirmPassword;

    // Config Fields
    private JTextField txtQRExpiry;
    private JTextField txtSmtpHost;
    private JTextField txtSmtpPort;
    private JTextField txtSenderEmail;
    private JPasswordField txtSenderPassword;

    public SettingsPanel(String userId, String userRole, JFrame parentFrame) {
        this.sessionUserId = userId;
        this.sessionUserRole = userRole;
        this.parentFrame = parentFrame;
        
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        initUI();
    }

    private void initUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;

        // Row 0: Theme Settings
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        themePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Theme Settings", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));
        JLabel lblTheme = new JLabel("Application Mode:");
        lblTheme.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton btnToggleTheme = new JButton(ThemeManager.isDarkMode() ? "Switch to Light Mode" : "Switch to Dark Mode");
        btnToggleTheme.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnToggleTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(parentFrame);
            btnToggleTheme.setText(ThemeManager.isDarkMode() ? "Switch to Light Mode" : "Switch to Dark Mode");
        });
        themePanel.add(lblTheme);
        themePanel.add(btnToggleTheme);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weighty = 0.05;
        add(themePanel, gbc);

        // Row 1: System Config Parameters (Only editable by Admin, but visible to Faculty as read-only or hidden)
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "System Configurations", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));
        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.insets = new Insets(5, 5, 5, 5);
        cgbc.anchor = GridBagConstraints.WEST;
        cgbc.fill = GridBagConstraints.HORIZONTAL;

        cgbc.gridx = 0; cgbc.gridy = 0;
        configPanel.add(new JLabel("QR Expiration (Minutes):"), cgbc);
        txtQRExpiry = new JTextField(settingsDAO.getSetting("qr_expiration_minutes", "5"), 10);
        cgbc.gridx = 1;
        configPanel.add(txtQRExpiry, cgbc);

        cgbc.gridx = 2;
        configPanel.add(new JLabel("SMTP Host:"), cgbc);
        txtSmtpHost = new JTextField(settingsDAO.getSetting("smtp_host", "smtp.gmail.com"), 15);
        cgbc.gridx = 3;
        configPanel.add(txtSmtpHost, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 1;
        configPanel.add(new JLabel("SMTP Port:"), cgbc);
        txtSmtpPort = new JTextField(settingsDAO.getSetting("smtp_port", "587"), 10);
        cgbc.gridx = 1;
        configPanel.add(txtSmtpPort, cgbc);

        cgbc.gridx = 2;
        configPanel.add(new JLabel("Sender Email:"), cgbc);
        txtSenderEmail = new JTextField(settingsDAO.getSetting("sender_email", "noreply.qrcode.attendance@gmail.com"), 15);
        cgbc.gridx = 3;
        configPanel.add(txtSenderEmail, cgbc);

        cgbc.gridx = 0; cgbc.gridy = 2;
        configPanel.add(new JLabel("Sender Password / Key:"), cgbc);
        txtSenderPassword = new JPasswordField(settingsDAO.getSetting("sender_password", ""), 10);
        cgbc.gridx = 1;
        configPanel.add(txtSenderPassword, cgbc);

        JButton btnSaveConfig = new JButton("Save Configurations");
        btnSaveConfig.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSaveConfig.addActionListener(e -> saveConfigurations());
        cgbc.gridx = 3; cgbc.gridy = 2;
        cgbc.anchor = GridBagConstraints.EAST;
        configPanel.add(btnSaveConfig, cgbc);

        gbc.gridy = 1; gbc.weighty = 0.2;
        add(configPanel, gbc);

        // Row 2: Database Backup & Restore Section (Admin Only)
        JPanel dbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        dbPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Database Actions (Admin Only)", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));
        JButton btnBackup = new JButton("Backup DB to SQL");
        btnBackup.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnBackup.setBackground(new Color(25, 135, 84));
        btnBackup.setForeground(Color.WHITE);
        btnBackup.addActionListener(this::handleBackup);

        JButton btnRestore = new JButton("Restore DB from SQL");
        btnRestore.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRestore.setBackground(new Color(13, 110, 253));
        btnRestore.setForeground(Color.WHITE);
        btnRestore.addActionListener(this::handleRestore);

        dbPanel.add(btnBackup);
        dbPanel.add(btnRestore);

        gbc.gridy = 2; gbc.weighty = 0.15;
        add(dbPanel, gbc);

        // Row 3: Change Password Section (Admin or Faculty)
        JPanel pwPanel = new JPanel(new GridBagLayout());
        pwPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Change User Account Password", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13)
        ));
        GridBagConstraints pgbc = new GridBagConstraints();
        pgbc.insets = new Insets(5, 5, 5, 5);
        pgbc.anchor = GridBagConstraints.WEST;

        pgbc.gridx = 0; pgbc.gridy = 0;
        pwPanel.add(new JLabel("Old Password:"), pgbc);
        txtOldPassword = new JPasswordField(20);
        pgbc.gridx = 1;
        pwPanel.add(txtOldPassword, pgbc);

        pgbc.gridx = 0; pgbc.gridy = 1;
        pwPanel.add(new JLabel("New Password:"), pgbc);
        txtNewPassword = new JPasswordField(20);
        pgbc.gridx = 1;
        pwPanel.add(txtNewPassword, pgbc);

        pgbc.gridx = 0; pgbc.gridy = 2;
        pwPanel.add(new JLabel("Confirm New Password:"), pgbc);
        txtConfirmPassword = new JPasswordField(20);
        pgbc.gridx = 1;
        pwPanel.add(txtConfirmPassword, pgbc);

        JButton btnChangePassword = new JButton("Update Password");
        btnChangePassword.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnChangePassword.setBackground(new Color(220, 53, 69));
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.addActionListener(this::handleChangePassword);
        pgbc.gridx = 1; pgbc.gridy = 3;
        pgbc.anchor = GridBagConstraints.EAST;
        pwPanel.add(btnChangePassword, pgbc);

        gbc.gridy = 3; gbc.weighty = 0.5;
        add(pwPanel, gbc);

        // Disable elements based on permissions
        if ("Faculty".equals(sessionUserRole)) {
            btnBackup.setEnabled(false);
            btnRestore.setEnabled(false);
            txtQRExpiry.setEditable(false);
            txtSmtpHost.setEditable(false);
            txtSmtpPort.setEditable(false);
            txtSenderEmail.setEditable(false);
            txtSenderPassword.setEditable(false);
            btnSaveConfig.setEnabled(false);
        }
    }

    private void saveConfigurations() {
        String expiry = txtQRExpiry.getText().trim();
        String host = txtSmtpHost.getText().trim();
        String port = txtSmtpPort.getText().trim();
        String email = txtSenderEmail.getText().trim();
        String pass = new String(txtSenderPassword.getPassword()).trim();

        if (expiry.isEmpty() || host.isEmpty() || port.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Configuration values cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(expiry);
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "QR Expiry and SMTP Port must be numeric values!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        settingsDAO.updateSetting("qr_expiration_minutes", expiry);
        settingsDAO.updateSetting("smtp_host", host);
        settingsDAO.updateSetting("smtp_port", port);
        settingsDAO.updateSetting("sender_email", email);
        if (!pass.isEmpty()) {
            settingsDAO.updateSetting("sender_password", pass);
        }

        JOptionPane.showMessageDialog(this, "System configurations updated successfully!", "Config Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    private void handleBackup(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("attendance_db_backup.sql"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.endsWith(".sql")) {
                path += ".sql";
            }

            String finalPath = path;
            SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() {
                    return DatabaseBackupUtility.backupDatabase(finalPath, "attendance_db", "root", "admin123");
                }

                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(SettingsPanel.this, "Database backed up successfully to:\n" + finalPath, "Backup Success", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(SettingsPanel.this, "Database backup failed!\nEnsure mysqldump is installed and database service credentials are correct.", "Backup Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(SettingsPanel.this, "Backup process error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        }
    }

    private void handleRestore(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("SQL Script Files (*.sql)", "sql"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            String path = fileToOpen.getAbsolutePath();

            int confirm = JOptionPane.showConfirmDialog(this, 
                "WARNING: Restoring the database will overwrite all current tables and histories.\nAre you sure you want to proceed?", 
                "Confirm Database Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (confirm == JOptionPane.YES_OPTION) {
                SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Boolean doInBackground() {
                        return DatabaseBackupUtility.restoreDatabase(path, "attendance_db", "root", "admin123");
                    }

                    @Override
                    protected void done() {
                        try {
                            if (get()) {
                                JOptionPane.showMessageDialog(SettingsPanel.this, "Database restored successfully!", "Restore Success", JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                JOptionPane.showMessageDialog(SettingsPanel.this, "Database restore failed!\nEnsure mysql command line is configured.", "Restore Error", JOptionPane.ERROR_MESSAGE);
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(SettingsPanel.this, "Restore process error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };
                worker.execute();
            }
        }
    }

    private void handleChangePassword(ActionEvent e) {
        String oldPassword = new String(txtOldPassword.getPassword()).trim();
        String newPassword = new String(txtNewPassword.getPassword()).trim();
        String confirmPassword = new String(txtConfirmPassword.getPassword()).trim();

        if (ValidationUtility.isEmpty(oldPassword) || ValidationUtility.isEmpty(newPassword) || ValidationUtility.isEmpty(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "All password fields must be filled!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New Password and Confirmation do not match!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = false;
        if ("Admin".equals(sessionUserRole)) {
            Admin admin = adminDAO.getAdminById(Integer.parseInt(sessionUserId));
            if (admin != null) {
                success = adminDAO.updatePassword(admin.getUsername(), oldPassword, newPassword);
            }
        } else {
            Faculty faculty = facultyDAO.getFacultyById(sessionUserId);
            if (faculty != null && BCrypt.checkpw(oldPassword, faculty.getPasswordHash())) {
                String newHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                success = facultyDAO.updatePassword(sessionUserId, newHash);
            }
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Password updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            txtOldPassword.setText("");
            txtNewPassword.setText("");
            txtConfirmPassword.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Current Password entered is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
