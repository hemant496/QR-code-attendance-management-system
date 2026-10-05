package attendance;

import dao.AttendanceDAO;
import dao.StudentDAO;
import dao.CourseDAO;
import dao.SubjectDAO;
import model.Attendance;
import model.Student;
import model.Course;
import model.Subject;
import utils.DateUtility;
import utils.ExcelExporter;
import utils.PDFExporter;
import utils.EmailUtility;
import utils.ValidationUtility;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class AttendancePanel extends JPanel {
    private JTable tblAttendance;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    
    private JComboBox<String> cbCourseFilter;
    private JComboBox<String> cbSubjectFilter;
    private JComboBox<String> cbStatusFilter;
    
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    private final String sessionUserId;
    private final String sessionUserRole;
    private List<Attendance> currentFilteredList = new ArrayList<>();

    public AttendancePanel(String userId, String userRole) {
        this.sessionUserId = userId;
        this.sessionUserRole = userRole;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        initUI();
        searchAttendance(); // Load all logs initially
    }

    private void initUI() {
        // --- 1. Filter Options Panel (Top) ---
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Search and Filter Attendance Logs", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search text field
        gbc.gridx = 0; gbc.gridy = 0;
        filterPanel.add(new JLabel("Search Student:"), gbc);
        txtSearch = new JTextField(12);
        txtSearch.putClientProperty("JTextField.placeholderText", "ID or Name...");
        gbc.gridx = 1;
        filterPanel.add(txtSearch, gbc);

        // Course filter
        gbc.gridx = 2;
        filterPanel.add(new JLabel("Course:"), gbc);
        cbCourseFilter = new JComboBox<>();
        cbCourseFilter.addItem("All");
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            cbCourseFilter.addItem(c.getCourseId());
        }
        gbc.gridx = 3;
        filterPanel.add(cbCourseFilter, gbc);

        // Subject filter
        gbc.gridx = 4;
        filterPanel.add(new JLabel("Subject:"), gbc);
        cbSubjectFilter = new JComboBox<>();
        cbSubjectFilter.addItem("All");
        List<Subject> subjects = subjectDAO.getAllSubjects();
        for (Subject s : subjects) {
            cbSubjectFilter.addItem(s.getSubjectId());
        }
        gbc.gridx = 5;
        filterPanel.add(cbSubjectFilter, gbc);

        // Start Date (YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = 1;
        filterPanel.add(new JLabel("Start Date:"), gbc);
        txtStartDate = new JTextField(10);
        txtStartDate.setText(DateUtility.getCurrentDate());
        gbc.gridx = 1;
        filterPanel.add(txtStartDate, gbc);

        // End Date (YYYY-MM-DD)
        gbc.gridx = 2;
        filterPanel.add(new JLabel("End Date:"), gbc);
        txtEndDate = new JTextField(10);
        txtEndDate.setText(DateUtility.getCurrentDate());
        gbc.gridx = 3;
        filterPanel.add(txtEndDate, gbc);

        // Status Combo
        gbc.gridx = 4;
        filterPanel.add(new JLabel("Status:"), gbc);
        cbStatusFilter = new JComboBox<>(new String[]{"All", "Present", "Late", "Absent"});
        gbc.gridx = 5;
        filterPanel.add(cbStatusFilter, gbc);

        // Toolbar Buttons panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnFilter = new JButton("Apply Filters");
        btnFilter.setBackground(new Color(13, 110, 253));
        btnFilter.setForeground(Color.WHITE);
        btnFilter.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFilter.addActionListener(e -> searchAttendance());

        JButton btnManualMark = new JButton("Mark Manually");
        btnManualMark.setBackground(new Color(25, 135, 84));
        btnManualMark.setForeground(Color.WHITE);
        btnManualMark.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnManualMark.addActionListener(e -> openManualMarkDialog());

        JButton btnEditStatus = new JButton("Edit Selected");
        btnEditStatus.setBackground(new Color(255, 193, 7));
        btnEditStatus.setForeground(Color.BLACK);
        btnEditStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEditStatus.addActionListener(e -> editSelectedStatus());

        btnPanel.add(btnFilter);
        btnPanel.add(btnManualMark);
        btnPanel.add(btnEditStatus);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.insets = new Insets(12, 8, 4, 8);
        filterPanel.add(btnPanel, gbc);

        add(filterPanel, BorderLayout.NORTH);

        // --- 2. Table Panel (Center) ---
        String[] columns = {"Log ID", "Student ID", "Student Name", "Subject", "Date", "Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblAttendance = new JTable(tableModel);
        tblAttendance.setRowHeight(22);
        tblAttendance.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblAttendance.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(tblAttendance);
        scroll.setBorder(BorderFactory.createTitledBorder("Attendance Log Database"));
        add(scroll, BorderLayout.CENTER);

        // --- 3. Footer Options Panel (Bottom) ---
        JPanel footerToolbar = new JPanel(new BorderLayout());
        footerToolbar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnExportExcel = new JButton("Export to Excel");
        JButton btnExportPDF = new JButton("Export to PDF");
        JButton btnEmailReport = new JButton("Email PDF Report");

        btnExportExcel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportPDF.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEmailReport.setFont(new Font("Segoe UI", Font.BOLD, 12));

        exportPanel.add(btnExportExcel);
        exportPanel.add(btnExportPDF);
        exportPanel.add(btnEmailReport);
        footerToolbar.add(exportPanel, BorderLayout.WEST);

        add(footerToolbar, BorderLayout.SOUTH);

        // Footer Listeners
        btnExportExcel.addActionListener(e -> exportLogsToExcel());
        btnExportPDF.addActionListener(e -> exportLogsToPDF());
        btnEmailReport.addActionListener(e -> emailPDFReport());
    }

    public void searchAttendance() {
        String search = txtSearch.getText().trim();
        String courseId = cbCourseFilter.getSelectedItem() != null ? cbCourseFilter.getSelectedItem().toString() : "All";
        String subjectId = cbSubjectFilter.getSelectedItem() != null ? cbSubjectFilter.getSelectedItem().toString() : "All";
        String startStr = txtStartDate.getText().trim();
        String endStr = txtEndDate.getText().trim();
        String status = cbStatusFilter.getSelectedItem().toString();

        Date startDate = null;
        Date endDate = null;

        try {
            if (!ValidationUtility.isEmpty(startStr)) {
                startDate = Date.valueOf(startStr);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Start Date format must be YYYY-MM-DD", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (!ValidationUtility.isEmpty(endStr)) {
                endDate = Date.valueOf(endStr);
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "End Date format must be YYYY-MM-DD", "Date Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tableModel.setRowCount(0);
        currentFilteredList.clear();

        List<Attendance> logs = attendanceDAO.getAttendanceReport(search, courseId, subjectId, startDate, endDate);
        for (Attendance log : logs) {
            if (status.equals("All") || log.getStatus().equalsIgnoreCase(status)) {
                currentFilteredList.add(log);
                tableModel.addRow(new Object[]{
                        log.getAttendanceId(),
                        log.getStudentId(),
                        log.getStudentName(),
                        log.getSubjectName(),
                        DateUtility.formatDate(log.getDate()),
                        DateUtility.formatTime(log.getTime()),
                        log.getStatus()
                });
            }
        }
    }

    private void openManualMarkDialog() {
        JPanel dialogPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        dialogPanel.setPreferredSize(new Dimension(350, 130));

        JTextField txtStuId = new JTextField();
        
        List<Subject> subjects = subjectDAO.getAllSubjects();
        JComboBox<Subject> cbSubject = new JComboBox<>(subjects.toArray(new Subject[0]));
        JComboBox<String> cbStatus = new JComboBox<>(new String[]{"Present", "Late", "Absent"});

        dialogPanel.add(new JLabel("Student ID:"));
        dialogPanel.add(txtStuId);
        dialogPanel.add(new JLabel("Subject:"));
        dialogPanel.add(cbSubject);
        dialogPanel.add(new JLabel("Attendance Status:"));
        dialogPanel.add(cbStatus);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel, "Mark Manual Attendance", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String stuId = txtStuId.getText().trim();
            Subject sub = (Subject) cbSubject.getSelectedItem();
            String status = cbStatus.getSelectedItem().toString();

            if (ValidationUtility.isEmpty(stuId) || sub == null) {
                JOptionPane.showMessageDialog(this, "Student ID and Subject are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Student s = studentDAO.getStudentById(stuId);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Student record " + stuId + " does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (attendanceDAO.isAttendanceMarkedToday(stuId, sub.getSubjectId())) {
                JOptionPane.showMessageDialog(this, "Attendance already marked today for this student & subject!", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = attendanceDAO.markAttendance(stuId, sub.getSubjectId(), sessionUserId, status);
            if (success) {
                JOptionPane.showMessageDialog(this, "Attendance marked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                searchAttendance();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to record manual attendance.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedStatus() {
        int row = tblAttendance.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select an attendance row from the table first!", "Row Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int logId = (int) tableModel.getValueAt(row, 0);
        String currentStatus = (String) tableModel.getValueAt(row, 6);

        String[] statuses = {"Present", "Late", "Absent"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(currentStatus);

        int response = JOptionPane.showConfirmDialog(this, cbStatus, "Select New Status Override", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (response == JOptionPane.OK_OPTION) {
            String newStatus = (String) cbStatus.getSelectedItem();
            if (newStatus != null && !newStatus.equals(currentStatus)) {
                if (attendanceDAO.updateAttendanceStatus(logId, newStatus)) {
                    JOptionPane.showMessageDialog(this, "Log status updated successfully!", "Status Changed", JOptionPane.INFORMATION_MESSAGE);
                    searchAttendance();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update status in database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void exportLogsToExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available in the table to export!", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Attendance_Logs_" + DateUtility.getCurrentDate() + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            boolean success = ExcelExporter.exportToExcel(tblAttendance, filePath, "Attendance Records Report");
            if (success) {
                JOptionPane.showMessageDialog(this, "Excel logs exported successfully!", "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void exportLogsToPDF() {
        if (currentFilteredList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No data available in the table to export!", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Attendance_Report_" + DateUtility.getCurrentDate() + ".pdf"));

        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File destFile = chooser.getSelectedFile();
            if (!destFile.getName().endsWith(".pdf")) {
                destFile = new File(destFile.getParentFile(), destFile.getName() + ".pdf");
            }
            boolean success = PDFExporter.exportAttendanceReport(currentFilteredList, destFile);
            if (success) {
                JOptionPane.showMessageDialog(this, "PDF report compiled successfully!", "PDF Exported", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void emailPDFReport() {
        if (currentFilteredList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No logs available to email! Filter records first.", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String recipient = JOptionPane.showInputDialog(this, "Enter Recipient Email Address:", "Email Report", JOptionPane.QUESTION_MESSAGE);
        if (recipient == null || recipient.trim().isEmpty()) {
            return;
        }

        if (!ValidationUtility.isValidEmail(recipient)) {
            JOptionPane.showMessageDialog(this, "Invalid email address format!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Show a loading cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            // Compile temporary PDF
            File tempPdf = File.createTempFile("attendance_report_temp_", ".pdf");
            boolean compiled = PDFExporter.exportAttendanceReport(currentFilteredList, tempPdf);
            if (!compiled) {
                JOptionPane.showMessageDialog(this, "Failed to compile temporary PDF file.", "Error", JOptionPane.ERROR_MESSAGE);
                setCursor(Cursor.getDefaultCursor());
                return;
            }

            // Email report
            String mailSubject = "Academic Attendance Report - " + DateUtility.getCurrentDate();
            String mailBody = "Hello,\n\nPlease find attached the compiled attendance report in PDF format.\n\nBest Regards,\nQR Code Attendance Management System";
            
            boolean sent = EmailUtility.sendReportWithAttachment(recipient.trim(), mailSubject, mailBody, tempPdf);
            
            // Clean up temp file
            tempPdf.delete();

            setCursor(Cursor.getDefaultCursor());

            if (sent) {
                JOptionPane.showMessageDialog(this, "PDF Attendance Report emailed successfully to: " + recipient, "Mail Dispatched", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send email. Check SMTP server database configurations.", "SMTP Network Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Error during mail dispatch: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
