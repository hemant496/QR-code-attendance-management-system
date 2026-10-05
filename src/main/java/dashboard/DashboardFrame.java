package dashboard;

import login.LoginFrame;
import student.StudentPanel;
import faculty.FacultyPanel;
import course.CourseSubjectPanel;
import holiday.HolidayPanel;
import qrcode.QRGeneratorPanel;
import qrcode.QRScannerFrame;
import attendance.AttendancePanel;
import report.ReportPanel;
import dao.SubjectDAO;
import model.Subject;
import utils.DateUtility;
import utils.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final String currentUserId;
    private final String currentUserName;
    private final String currentUserRole; // "Admin" or "Faculty"
    
    // UI Elements
    private JLabel lblLiveTime;
    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    
    // Nav Buttons
    private JButton btnDash;
    private JButton btnStudent;
    private JButton btnFaculty;
    private JButton btnCourseSubject;
    private JButton btnHoliday;
    private JButton btnQRGen;
    private JButton btnQRScan;
    private JButton btnAttendance;
    private JButton btnReports;
    private JButton btnAudit;
    private JButton btnSettings;
    private JButton btnLogout;

    // View Panels
    private DashboardStatsPanel statsPanel;
    private StudentPanel studentPanel;
    private FacultyPanel facultyPanel;
    private CourseSubjectPanel courseSubjectPanel;
    private HolidayPanel holidayPanel;
    private QRGeneratorPanel qrGenPanel;
    private AttendancePanel attendancePanel;
    private ReportPanel reportPanel;
    private AuditPanel auditPanel;
    private SettingsPanel settingsPanel;

    public DashboardFrame(String userId, String userName, String role) {
        this.currentUserId = userId;
        this.currentUserName = userName;
        this.currentUserRole = role;
        
        setTitle("QR Code Attendance Management System - " + role + " Dashboard");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1150, 750));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initUI();
        startClock();
        applyRolePermissions();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- 1. Top Header Panel ---
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(new Color(24, 28, 36));
        headerPanel.setPreferredSize(new Dimension(getWidth(), 65));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel lblTitle = new JLabel("QR CODE ATTENDANCE SYSTEM (" + currentUserRole.toUpperCase() + ")", JLabel.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        // Header Right Panel (Welcome text, Time, Theme quick switch)
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
        headerRight.setOpaque(false);

        JLabel lblWelcome = new JLabel("Welcome, " + currentUserName, JLabel.RIGHT);
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblWelcome.setForeground(new Color(13, 110, 253));

        lblLiveTime = new JLabel(DateUtility.getCurrentDateTime(), JLabel.RIGHT);
        lblLiveTime.setFont(new Font("Consolas", Font.BOLD, 14));
        lblLiveTime.setForeground(Color.LIGHT_GRAY);

        JButton btnQuickTheme = new JButton(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
        btnQuickTheme.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnQuickTheme.addActionListener(e -> {
            ThemeManager.toggleTheme(this);
            btnQuickTheme.setText(ThemeManager.isDarkMode() ? "Light Mode" : "Dark Mode");
            refreshAllPanels();
        });

        headerRight.add(lblWelcome);
        headerRight.add(lblLiveTime);
        headerRight.add(btnQuickTheme);
        headerPanel.add(headerRight, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- 2. Left Sidebar Navigation Panel ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(33, 37, 41));
        sidebarPanel.setPreferredSize(new Dimension(240, getHeight()));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        btnDash = createNavButton("Dashboard Home");
        btnStudent = createNavButton("Student Management");
        btnFaculty = createNavButton("Faculty Management");
        btnCourseSubject = createNavButton("Courses & Subjects");
        btnHoliday = createNavButton("Holiday Calendar");
        btnQRGen = createNavButton("Generate QR Codes");
        btnQRScan = createNavButton("Scan Attendance");
        // Give scan button an accent color
        btnQRScan.setBackground(new Color(13, 110, 253));
        btnQRScan.setForeground(Color.WHITE);
        
        btnAttendance = createNavButton("Attendance Logs");
        btnReports = createNavButton("Attendance Reports");
        btnAudit = createNavButton("Security Audit Logs");
        btnSettings = createNavButton("System Settings");
        
        btnLogout = createNavButton("Logout");
        btnLogout.setBackground(new Color(220, 53, 69));
        btnLogout.setForeground(Color.WHITE);

        // Add to sidebar with spacing
        sidebarPanel.add(btnDash); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnStudent); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnFaculty); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnCourseSubject); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnHoliday); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnQRGen); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnQRScan); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnAttendance); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnReports); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnAudit); sidebarPanel.add(Box.createVerticalStrut(6));
        sidebarPanel.add(btnSettings); sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.add(btnLogout);

        add(sidebarPanel, BorderLayout.WEST);

        // --- 3. Center Content Panel (CardLayout) ---
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        
        statsPanel = new DashboardStatsPanel();
        studentPanel = new StudentPanel();
        facultyPanel = new FacultyPanel();
        courseSubjectPanel = new CourseSubjectPanel();
        holidayPanel = new HolidayPanel();
        qrGenPanel = new QRGeneratorPanel();
        attendancePanel = new AttendancePanel(currentUserId, currentUserRole);
        reportPanel = new ReportPanel();
        auditPanel = new AuditPanel();
        settingsPanel = new SettingsPanel(currentUserId, currentUserRole, this);

        mainContentPanel.add(statsPanel, "Dashboard");
        mainContentPanel.add(studentPanel, "Student");
        mainContentPanel.add(facultyPanel, "Faculty");
        mainContentPanel.add(courseSubjectPanel, "CourseSubject");
        mainContentPanel.add(holidayPanel, "Holiday");
        mainContentPanel.add(qrGenPanel, "QRGen");
        mainContentPanel.add(attendancePanel, "Attendance");
        mainContentPanel.add(reportPanel, "Reports");
        mainContentPanel.add(auditPanel, "Audit");
        mainContentPanel.add(settingsPanel, "Settings");

        add(mainContentPanel, BorderLayout.CENTER);

        // --- 4. Bottom Footer Panel ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setPreferredSize(new Dimension(getWidth(), 30));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        
        JLabel lblStatus = new JLabel("System Status: Online | Connected to local database (attendance_db) via JDBC", JLabel.LEFT);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblStatus.setForeground(Color.GRAY);
        
        JLabel lblCopyright = new JLabel("© 2026 QR Attendance System. All Rights Reserved.", JLabel.RIGHT);
        lblCopyright.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblCopyright.setForeground(Color.GRAY);

        footerPanel.add(lblStatus, BorderLayout.WEST);
        footerPanel.add(lblCopyright, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);

        // Navigation Actions
        btnDash.addActionListener(e -> switchView("Dashboard"));
        btnStudent.addActionListener(e -> switchView("Student"));
        btnFaculty.addActionListener(e -> switchView("Faculty"));
        btnCourseSubject.addActionListener(e -> switchView("CourseSubject"));
        btnHoliday.addActionListener(e -> switchView("Holiday"));
        btnQRGen.addActionListener(e -> switchView("QRGen"));
        btnAttendance.addActionListener(e -> switchView("Attendance"));
        btnReports.addActionListener(e -> switchView("Reports"));
        btnAudit.addActionListener(e -> switchView("Audit"));
        btnSettings.addActionListener(e -> switchView("Settings"));
        btnLogout.addActionListener(e -> handleLogout());
        
        btnQRScan.addActionListener(e -> openQRScanner());
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(220, 36));
        btn.setPreferredSize(new Dimension(220, 36));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.setBackground(new Color(52, 58, 64));
        btn.setForeground(Color.WHITE);
        return btn;
    }

    private void applyRolePermissions() {
        if ("Faculty".equals(currentUserRole)) {
            // Hide administrative modules
            btnStudent.setVisible(false);
            btnFaculty.setVisible(false);
            btnCourseSubject.setVisible(false);
            btnHoliday.setVisible(false);
            btnQRGen.setVisible(false);
            btnAudit.setVisible(false);
            btnSettings.setVisible(false);
        }
    }

    private void switchView(String cardName) {
        cardLayout.show(mainContentPanel, cardName);
        refreshPanelData(cardName);
    }

    private void refreshPanelData(String cardName) {
        switch (cardName) {
            case "Dashboard":
                statsPanel.refreshStats();
                break;
            case "Attendance":
                attendancePanel.searchAttendance();
                break;
            case "Faculty":
                facultyPanel.refreshTable();
                break;
            case "CourseSubject":
                courseSubjectPanel.refreshData();
                break;
            case "Holiday":
                holidayPanel.refreshTable();
                break;
            case "Audit":
                auditPanel.refreshTable();
                break;
        }
    }

    private void refreshAllPanels() {
        statsPanel.refreshStats();
        attendancePanel.searchAttendance();
    }

    private void openQRScanner() {
        // Query subjects from DB
        SubjectDAO subjectDAO = new SubjectDAO();
        List<Subject> subjects = subjectDAO.getAllSubjects();
        if (subjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No subjects found! Set up subjects in Course & Subjects management first.", "Scanning Halted", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Subject selector dialog
        JComboBox<Subject> cmbSubject = new JComboBox<>(subjects.toArray(new Subject[0]));
        int response = JOptionPane.showConfirmDialog(this, cmbSubject, "Select Subject for Attendance Logging", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        
        if (response != JOptionPane.OK_OPTION) {
            return; // cancelled
        }

        Subject selectedSubject = (Subject) cmbSubject.getSelectedItem();
        if (selectedSubject == null) return;

        // Open Webcam Scan Frame
        QRScannerFrame scannerFrame = new QRScannerFrame(currentUserId, selectedSubject.getSubjectId(), (studentId, studentName, time) -> {
            System.out.println("Scan marked: " + studentId + " for subject " + selectedSubject.getSubjectName());
            statsPanel.refreshStats();
            attendancePanel.searchAttendance();
        });
        scannerFrame.setVisible(true);
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to log out?", "Logout Confirmation", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                login.setVisible(true);
            });
        }
    }

    private void startClock() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Timer timer = new Timer(1000, (ActionEvent e) -> {
            lblLiveTime.setText(sdf.format(new Date()));
        });
        timer.start();
    }
}
