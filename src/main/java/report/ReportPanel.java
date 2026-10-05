package report;

import dao.AttendanceDAO;
import dao.StudentDAO;
import model.Attendance;
import model.Student;
import utils.DateUtility;
import utils.ExcelExporter;
import utils.ValidationUtility;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.List;

public class ReportPanel extends JPanel {
    private JComboBox<String> cbReportType;
    private JTextField txtStudentId;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JLabel lblReportSummary;
    private JTable tblReports;
    private DefaultTableModel tableModel;

    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final StudentDAO studentDAO = new StudentDAO();

    public ReportPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
        cbReportType.setSelectedIndex(0); // Trigger today's report initially
    }

    private void initUI() {
        // --- 1. Filter Settings ---
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Report Configurations", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Report Type
        gbc.gridx = 0; gbc.gridy = 0;
        configPanel.add(new JLabel("Report Type:"), gbc);
        
        String[] types = {"Today's Attendance", "Weekly Attendance", "Monthly Attendance", "Student-wise Attendance", "Overall Percentage Summary", "Custom Date Range"};
        cbReportType = new JComboBox<>(types);
        cbReportType.addActionListener(e -> handleReportTypeChange());
        gbc.gridx = 1;
        configPanel.add(cbReportType, gbc);

        // Student ID (Conditional)
        gbc.gridx = 2;
        configPanel.add(new JLabel("Student ID:"), gbc);
        txtStudentId = new JTextField(10);
        txtStudentId.setEnabled(false);
        gbc.gridx = 3;
        configPanel.add(txtStudentId, gbc);

        // Start Date (Conditional)
        gbc.gridx = 0; gbc.gridy = 1;
        configPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        txtStartDate = new JTextField(10);
        txtStartDate.setEnabled(false);
        gbc.gridx = 1;
        configPanel.add(txtStartDate, gbc);

        // End Date (Conditional)
        gbc.gridx = 2;
        configPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        txtEndDate = new JTextField(10);
        txtEndDate.setEnabled(false);
        gbc.gridx = 3;
        configPanel.add(txtEndDate, gbc);

        // Action Buttons panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton btnGenerate = new JButton("Generate Report");
        btnGenerate.setBackground(new Color(13, 110, 253));
        btnGenerate.setForeground(Color.WHITE);
        btnGenerate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnGenerate.addActionListener(e -> generateReport());
        actionPanel.add(btnGenerate);

        JButton btnPrint = new JButton("Print Report");
        btnPrint.setBackground(new Color(25, 135, 84));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrint.addActionListener(e -> printReport());
        actionPanel.add(btnPrint);

        JButton btnExport = new JButton("Export to Excel");
        btnExport.setBackground(new Color(108, 117, 125));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.addActionListener(e -> exportReportToExcel());
        actionPanel.add(btnExport);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 4;
        configPanel.add(actionPanel, gbc);

        add(configPanel, BorderLayout.NORTH);

        // --- 2. Report Details & Table Area ---
        JPanel resultsPanel = new JPanel(new BorderLayout(5, 10));
        
        lblReportSummary = new JLabel("Summary: Please select configurations and click Generate Report", JLabel.LEFT);
        lblReportSummary.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblReportSummary.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblReportSummary.setForeground(new Color(13, 110, 253));
        resultsPanel.add(lblReportSummary, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel();
        tblReports = new JTable(tableModel);
        tblReports.setRowHeight(22);
        tblReports.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblReports.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        resultsPanel.add(new JScrollPane(tblReports), BorderLayout.CENTER);
        add(resultsPanel, BorderLayout.CENTER);
    }

    private void handleReportTypeChange() {
        int index = cbReportType.getSelectedIndex();
        // Reset states
        txtStudentId.setEnabled(false);
        txtStartDate.setEnabled(false);
        txtEndDate.setEnabled(false);

        LocalDate today = LocalDate.now();

        switch (index) {
            case 0: // Today
                txtStartDate.setText(today.toString());
                txtEndDate.setText(today.toString());
                break;
            case 1: // Weekly
                txtStartDate.setText(today.minusDays(7).toString());
                txtEndDate.setText(today.toString());
                break;
            case 2: // Monthly
                txtStartDate.setText(today.minusMonths(1).toString());
                txtEndDate.setText(today.toString());
                break;
            case 3: // Student-wise
                txtStudentId.setEnabled(true);
                txtStartDate.setText("");
                txtEndDate.setText("");
                break;
            case 4: // Overall Summary
                txtStartDate.setText("");
                txtEndDate.setText("");
                break;
            case 5: // Custom Range
                txtStartDate.setEnabled(true);
                txtEndDate.setEnabled(true);
                txtStartDate.setText("");
                txtEndDate.setText("");
                break;
        }
    }

    private void generateReport() {
        int index = cbReportType.getSelectedIndex();
        
        if (index == 4) { // Overall Percentage Summary
            generateOverallSummaryReport();
            return;
        }

        String stuId = txtStudentId.getText().trim();
        String startStr = txtStartDate.getText().trim();
        String endStr = txtEndDate.getText().trim();

        Date start = null;
        Date end = null;

        if (index == 3) { // Student-wise
            if (ValidationUtility.isEmpty(stuId)) {
                JOptionPane.showMessageDialog(this, "Student ID is required for student-wise report!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Student s = studentDAO.getStudentById(stuId);
            if (s == null) {
                JOptionPane.showMessageDialog(this, "Student ID " + stuId + " not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else {
            try {
                if (!ValidationUtility.isEmpty(startStr)) start = Date.valueOf(startStr);
                if (!ValidationUtility.isEmpty(endStr)) end = Date.valueOf(endStr);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Dates must be in YYYY-MM-DD format", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Configure table columns for log list
        String[] cols = {"Student ID", "Name", "Date", "Time", "Status"};
        tableModel.setColumnIdentifiers(cols);
        tableModel.setRowCount(0);

        List<Attendance> logs = attendanceDAO.getAttendanceReport(index == 3 ? stuId : null, "All", "All", start, end);
        int presentCount = 0;
        for (Attendance log : logs) {
            if (log.getStatus().equalsIgnoreCase("Present")) presentCount++;
            tableModel.addRow(new Object[]{
                    log.getStudentId(),
                    log.getStudentName(),
                    DateUtility.formatDate(log.getDate()),
                    DateUtility.formatTime(log.getTime()),
                    log.getStatus()
            });
        }

        int totalLogs = logs.size();
        double pct = totalLogs == 0 ? 100.0 : ((double) presentCount / totalLogs) * 100.0;

        if (index == 3) {
            double overallPct = attendanceDAO.getStudentAttendancePercentage(stuId);
            lblReportSummary.setText(String.format("Report for %s: Total Classes Recorded: %d, Present Days: %d. Attendance Rate (Selected Period): %.1f%%. Career Attendance: %.1f%%",
                    stuId, totalLogs, presentCount, pct, overallPct));
        } else {
            lblReportSummary.setText(String.format("Report: Total logs found: %d. Total Present: %d. Attendance Rate: %.1f%%",
                    totalLogs, presentCount, pct));
        }
    }

    private void generateOverallSummaryReport() {
        String[] cols = {"Student ID", "Roll No", "Name", "Course ID", "Semester", "Attendance Percentage"};
        tableModel.setColumnIdentifiers(cols);
        tableModel.setRowCount(0);

        List<Student> students = studentDAO.getAllStudents();
        int belowThreshold = 0;

        for (Student s : students) {
            double pct = attendanceDAO.getStudentAttendancePercentage(s.getStudentId());
            if (pct < 75.0) belowThreshold++;
            tableModel.addRow(new Object[]{
                    s.getStudentId(),
                    s.getRollNo(),
                    s.getName(),
                    s.getCourseId(),
                    s.getSemester(),
                    String.format("%.2f%%", pct)
            });
        }

        lblReportSummary.setText(String.format("Overall Summary: Total Students: %d. Students below 75%% Attendance: %d.",
                students.size(), belowThreshold));
    }

    private void printReport() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available to print!", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            MessageFormat header = new MessageFormat("Attendance Report - " + cbReportType.getSelectedItem().toString());
            MessageFormat footer = new MessageFormat("Page {0,number,integer}");
            
            boolean printed = tblReports.print(JTable.PrintMode.FIT_WIDTH, header, footer);
            if (printed) {
                JOptionPane.showMessageDialog(this, "Document sent to printer successfully.", "Printing Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Printing Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void exportReportToExcel() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available in the table to export!", "No Data", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new File("Attendance_Report_" + cbReportType.getSelectedItem().toString().replace(" ", "_") + ".xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String filePath = fileToSave.getAbsolutePath();
            if (!filePath.endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            boolean success = ExcelExporter.exportToExcel(tblReports, filePath, cbReportType.getSelectedItem().toString() + " Report");
            if (success) {
                JOptionPane.showMessageDialog(this, "Report exported successfully to:\n" + filePath, "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export report to Excel.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
