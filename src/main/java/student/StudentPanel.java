package student;

import dao.CourseDAO;
import dao.StudentDAO;
import model.Course;
import model.Student;
import utils.IDGenerator;
import utils.QRGenerator;
import utils.ValidationUtility;
import utils.ExcelImporter;
import utils.ExcelExporter;
import utils.PDFExporter;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class StudentPanel extends JPanel {
    private JTextField txtStudentId;
    private JTextField txtRollNo;
    private JTextField txtRegNo;
    private JTextField txtName;
    private JComboBox<Course> cbCourse;
    private JComboBox<String> cbSemester;
    private JComboBox<String> cbSection;
    private JComboBox<String> cbStatus;
    private JTextField txtMobile;
    private JTextField txtEmail;
    
    private JLabel lblPhoto;
    private JLabel lblQRCode;
    private File selectedPhotoFile = null;

    private JTextField txtSearch;
    private JComboBox<String> filterCourse;
    private JComboBox<String> filterSemester;
    private JComboBox<String> filterSection;
    private JComboBox<String> filterStatus;
    
    private JTable tblStudents;
    private DefaultTableModel tableModel;

    // Pagination variables
    private int currentPage = 1;
    private final int PAGE_SIZE = 15;
    private JLabel lblPageInfo;
    private JButton btnPrev;
    private JButton btnNext;

    private final StudentDAO studentDAO = new StudentDAO();
    private final CourseDAO courseDAO = new CourseDAO();
    private String selectedStudentId = null;

    public StudentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Ensure photo directory exists
        File photoDir = new File("images/photos");
        if (!photoDir.exists()) {
            photoDir.mkdirs();
        }

        initUI();
        refreshTable();
        clearForm();
    }

    private void initUI() {
        // --- Split Panel: Left Form, Right Table ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(430);
        splitPane.setContinuousLayout(true);

        // --- LEFT PANEL: CRUD Form ---
        JPanel formPanel = new JPanel(new BorderLayout(5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Student Registration Form", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Student ID (Auto Generated)
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Student ID:"), gbc);
        txtStudentId = new JTextField(15);
        txtStudentId.setEditable(false);
        txtStudentId.setFont(new Font("Segoe UI", Font.BOLD, 13));
        txtStudentId.setForeground(Color.RED);
        gbc.gridx = 1;
        fieldsPanel.add(txtStudentId, gbc);

        // Roll No
        gbc.gridx = 0; gbc.gridy = 1;
        fieldsPanel.add(new JLabel("Roll No:"), gbc);
        txtRollNo = new JTextField(15);
        gbc.gridx = 1;
        fieldsPanel.add(txtRollNo, gbc);

        // Reg No
        gbc.gridx = 0; gbc.gridy = 2;
        fieldsPanel.add(new JLabel("Registration No:"), gbc);
        txtRegNo = new JTextField(15);
        gbc.gridx = 1;
        fieldsPanel.add(txtRegNo, gbc);

        // Name
        gbc.gridx = 0; gbc.gridy = 3;
        fieldsPanel.add(new JLabel("Name:"), gbc);
        txtName = new JTextField(15);
        gbc.gridx = 1;
        fieldsPanel.add(txtName, gbc);

        // Course lookup from DB
        gbc.gridx = 0; gbc.gridy = 4;
        fieldsPanel.add(new JLabel("Course:"), gbc);
        List<Course> courses = courseDAO.getAllCourses();
        cbCourse = new JComboBox<>(courses.toArray(new Course[0]));
        gbc.gridx = 1;
        fieldsPanel.add(cbCourse, gbc);

        // Semester
        gbc.gridx = 0; gbc.gridy = 5;
        fieldsPanel.add(new JLabel("Semester:"), gbc);
        cbSemester = new JComboBox<>(new String[]{"1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        gbc.gridx = 1;
        fieldsPanel.add(cbSemester, gbc);

        // Section
        gbc.gridx = 0; gbc.gridy = 6;
        fieldsPanel.add(new JLabel("Section:"), gbc);
        cbSection = new JComboBox<>(new String[]{"A", "B", "C", "D"});
        gbc.gridx = 1;
        fieldsPanel.add(cbSection, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 7;
        fieldsPanel.add(new JLabel("Status:"), gbc);
        cbStatus = new JComboBox<>(new String[]{"Active", "Inactive"});
        gbc.gridx = 1;
        fieldsPanel.add(cbStatus, gbc);

        // Mobile
        gbc.gridx = 0; gbc.gridy = 8;
        fieldsPanel.add(new JLabel("Mobile:"), gbc);
        txtMobile = new JTextField(15);
        gbc.gridx = 1;
        fieldsPanel.add(txtMobile, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 9;
        fieldsPanel.add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(15);
        gbc.gridx = 1;
        fieldsPanel.add(txtEmail, gbc);

        // Student Profile Photo chooser
        gbc.gridx = 0; gbc.gridy = 10;
        fieldsPanel.add(new JLabel("Student Photo:"), gbc);
        JPanel photoWrapper = new JPanel(new BorderLayout(5, 0));
        lblPhoto = new JLabel("[ No Image Selected ]", JLabel.CENTER);
        lblPhoto.setPreferredSize(new Dimension(80, 80));
        lblPhoto.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        JButton btnChoosePhoto = new JButton("Choose...");
        btnChoosePhoto.addActionListener(e -> choosePhoto());
        photoWrapper.add(lblPhoto, BorderLayout.CENTER);
        photoWrapper.add(btnChoosePhoto, BorderLayout.EAST);
        gbc.gridx = 1;
        fieldsPanel.add(photoWrapper, gbc);

        // QR Code Display area
        gbc.gridx = 0; gbc.gridy = 11;
        fieldsPanel.add(new JLabel("QR Preview:"), gbc);
        lblQRCode = new JLabel("Save Student to generate QR", JLabel.CENTER);
        lblQRCode.setPreferredSize(new Dimension(100, 100));
        lblQRCode.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        gbc.gridx = 1;
        fieldsPanel.add(lblQRCode, gbc);

        formPanel.add(new JScrollPane(fieldsPanel), BorderLayout.CENTER);

        // CRUD Action Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        
        JButton btnAdd = new JButton("Add Student");
        btnAdd.setBackground(new Color(25, 135, 84));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.addActionListener(e -> addStudent());

        JButton btnUpdate = new JButton("Update");
        btnUpdate.setBackground(new Color(13, 110, 253));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.addActionListener(e -> updateStudent());

        JButton btnDelete = new JButton("Delete");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.addActionListener(e -> deleteStudent());

        JButton btnClear = new JButton("Clear / Reset");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnClear.addActionListener(e -> clearForm());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);

        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        splitPane.setLeftComponent(formPanel);

        // --- RIGHT PANEL: Filters, Search Bar & JTable ---
        JPanel tableContainer = new JPanel(new BorderLayout(5, 10));
        tableContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Student Database Records", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        // Filters Panel
        JPanel filterPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        filterPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        filterCourse = new JComboBox<>();
        filterCourse.addItem("All");
        for (Course c : courses) {
            filterCourse.addItem(c.getCourseId());
        }
        filterSemester = new JComboBox<>(new String[]{"All", "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th"});
        filterSection = new JComboBox<>(new String[]{"All", "A", "B", "C", "D"});
        filterStatus = new JComboBox<>(new String[]{"All", "Active", "Inactive"});

        filterPanel.add(new JLabel("Filter Course:"));
        filterPanel.add(filterCourse);
        filterPanel.add(new JLabel("Filter Semester:"));
        filterPanel.add(filterSemester);
        filterPanel.add(new JLabel("Filter Section:"));
        filterPanel.add(filterSection);
        filterPanel.add(new JLabel("Filter Status:"));
        filterPanel.add(filterStatus);

        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Search by ID, Name, Roll No, Reg No...");
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JButton btnSearchClear = new JButton("Reset Filters");
        btnSearchClear.addActionListener(e -> {
            txtSearch.setText("");
            filterCourse.setSelectedIndex(0);
            filterSemester.setSelectedIndex(0);
            filterSection.setSelectedIndex(0);
            filterStatus.setSelectedIndex(0);
            refreshTable();
        });

        // Trigger filters when selected index changes
        filterCourse.addActionListener(e -> refreshTable());
        filterSemester.addActionListener(e -> refreshTable());
        filterSection.addActionListener(e -> refreshTable());
        filterStatus.addActionListener(e -> refreshTable());
        
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshTable(); }
        });

        searchPanel.add(new JLabel("Search Text: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        searchPanel.add(btnSearchClear, BorderLayout.EAST);

        // Header Wrapper Panel
        JPanel headerWrapper = new JPanel(new BorderLayout(5, 5));
        headerWrapper.add(filterPanel, BorderLayout.NORTH);
        headerWrapper.add(searchPanel, BorderLayout.SOUTH);
        tableContainer.add(headerWrapper, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Roll No", "Reg No", "Name", "Course ID", "Semester", "Section", "Status", "Mobile", "Email"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tblStudents = new JTable(tableModel);
        tblStudents.setRowHeight(24);
        tblStudents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tblStudents.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        tblStudents.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = tblStudents.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromRow(selectedRow);
                }
            }
        });

        tableContainer.add(new JScrollPane(tblStudents), BorderLayout.CENTER);

        // Bottom Controls: Pagination & Excel Buttons
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 5, 5, 5));

        // Excel Import / Export + PDF Export
        JPanel excelPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton btnImportExcel = new JButton("Import from Excel");
        JButton btnExportExcel = new JButton("Export Excel");
        JButton btnExportProfilePDF = new JButton("Export PDF Profile");

        btnImportExcel.setBackground(new Color(23, 162, 184));
        btnImportExcel.setForeground(Color.WHITE);
        btnExportExcel.setBackground(new Color(108, 117, 125));
        btnExportExcel.setForeground(Color.WHITE);

        excelPanel.add(btnImportExcel);
        excelPanel.add(btnExportExcel);
        excelPanel.add(btnExportProfilePDF);
        bottomPanel.add(excelPanel, BorderLayout.WEST);

        // Pagination buttons
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPrev = new JButton("< Prev");
        btnNext = new JButton("Next >");
        lblPageInfo = new JLabel("Page 1");
        lblPageInfo.setFont(new Font("Segoe UI", Font.BOLD, 12));

        btnPrev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                refreshTable();
            }
        });
        btnNext.addActionListener(e -> {
            currentPage++;
            refreshTable();
        });

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);
        bottomPanel.add(paginationPanel, BorderLayout.EAST);

        tableContainer.add(bottomPanel, BorderLayout.SOUTH);
        splitPane.setRightComponent(tableContainer);

        add(splitPane, BorderLayout.CENTER);

        // Excel Import Listener
        btnImportExcel.addActionListener(e -> importExcelData());
        btnExportExcel.addActionListener(e -> exportExcelData());
        btnExportProfilePDF.addActionListener(e -> exportStudentPDF());
    }

    private void choosePhoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            selectedPhotoFile = fileChooser.getSelectedFile();
            ImageIcon icon = new ImageIcon(selectedPhotoFile.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblPhoto.setIcon(new ImageIcon(img));
            lblPhoto.setText("");
        }
    }

    private void populateFormFromRow(int row) {
        selectedStudentId = tableModel.getValueAt(row, 0).toString();
        txtStudentId.setText(selectedStudentId);
        txtRollNo.setText(tableModel.getValueAt(row, 1).toString());
        txtRegNo.setText(tableModel.getValueAt(row, 2).toString());
        txtName.setText(tableModel.getValueAt(row, 3).toString());
        
        // Select Course object
        String courseId = tableModel.getValueAt(row, 4).toString();
        for (int i = 0; i < cbCourse.getItemCount(); i++) {
            if (cbCourse.getItemAt(i).getCourseId().equals(courseId)) {
                cbCourse.setSelectedIndex(i);
                break;
            }
        }

        cbSemester.setSelectedItem(tableModel.getValueAt(row, 5).toString());
        cbSection.setSelectedItem(tableModel.getValueAt(row, 6).toString());
        cbStatus.setSelectedItem(tableModel.getValueAt(row, 7).toString());
        txtMobile.setText(tableModel.getValueAt(row, 8).toString());
        txtEmail.setText(tableModel.getValueAt(row, 9).toString());

        // Load photo preview
        Student s = studentDAO.getStudentById(selectedStudentId);
        if (s != null && s.getPhotoPath() != null && new File(s.getPhotoPath()).exists()) {
            ImageIcon icon = new ImageIcon(s.getPhotoPath());
            Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            lblPhoto.setIcon(new ImageIcon(img));
            lblPhoto.setText("");
            selectedPhotoFile = new File(s.getPhotoPath());
        } else {
            lblPhoto.setIcon(null);
            lblPhoto.setText("[ No Image Selected ]");
            selectedPhotoFile = null;
        }

        loadQRCodeImage(selectedStudentId);
    }

    private void loadQRCodeImage(String studentId) {
        File file = QRGenerator.getQRFile(studentId);
        if (file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
            lblQRCode.setIcon(new ImageIcon(img));
            lblQRCode.setText("");
        } else {
            lblQRCode.setIcon(null);
            lblQRCode.setText("No QR Code");
        }
    }

    private void clearForm() {
        selectedStudentId = null;
        txtStudentId.setText(IDGenerator.generateNextStudentId());
        txtRollNo.setText("");
        txtRegNo.setText("");
        txtName.setText("");
        cbCourse.setSelectedIndex(0);
        cbSemester.setSelectedIndex(0);
        cbSection.setSelectedIndex(0);
        cbStatus.setSelectedIndex(0);
        txtMobile.setText("");
        txtEmail.setText("");
        
        lblPhoto.setIcon(null);
        lblPhoto.setText("[ No Image Selected ]");
        selectedPhotoFile = null;

        lblQRCode.setIcon(null);
        lblQRCode.setText("Save Student to generate QR");
        tblStudents.clearSelection();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);

        String search = txtSearch.getText().trim();
        String cId = filterCourse.getSelectedItem() != null ? filterCourse.getSelectedItem().toString() : "All";
        String sem = filterSemester.getSelectedItem() != null ? filterSemester.getSelectedItem().toString() : "All";
        String sec = filterSection.getSelectedItem() != null ? filterSection.getSelectedItem().toString() : "All";
        String stat = filterStatus.getSelectedItem() != null ? filterStatus.getSelectedItem().toString() : "All";

        int offset = (currentPage - 1) * PAGE_SIZE;

        List<Student> students = studentDAO.getFilteredStudents(search, cId, sem, sec, stat, offset, PAGE_SIZE);
        for (Student s : students) {
            tableModel.addRow(new Object[]{
                    s.getStudentId(), s.getRollNo(), s.getRegistrationNo(), s.getName(),
                    s.getCourseId(), s.getSemester(), s.getSection(), s.getStatus(), s.getMobile(), s.getEmail()
            });
        }

        // Toggle pagination buttons
        btnPrev.setEnabled(currentPage > 1);
        btnNext.setEnabled(students.size() == PAGE_SIZE); // disable if page isn't full
        lblPageInfo.setText("Page " + currentPage);
    }

    private boolean validateInputs(boolean isUpdate) {
        String rollNo = txtRollNo.getText().trim();
        String regNo = txtRegNo.getText().trim();
        String name = txtName.getText().trim();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();

        if (ValidationUtility.isEmpty(rollNo) || ValidationUtility.isEmpty(regNo) || 
            ValidationUtility.isEmpty(name) || ValidationUtility.isEmpty(mobile) || ValidationUtility.isEmpty(email)) {
            JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!ValidationUtility.isValidMobile(mobile)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid mobile number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!ValidationUtility.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!isUpdate) {
            if (studentDAO.isRollNoExists(rollNo)) {
                JOptionPane.showMessageDialog(this, "Roll number already exists in database!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (studentDAO.isRegNoExists(regNo)) {
                JOptionPane.showMessageDialog(this, "Registration number already exists in database!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } else {
            if (studentDAO.isRollNoExists(rollNo, selectedStudentId)) {
                JOptionPane.showMessageDialog(this, "Roll number already exists for another student!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
            if (studentDAO.isRegNoExists(regNo, selectedStudentId)) {
                JOptionPane.showMessageDialog(this, "Registration number already exists for another student!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        return true;
    }

    private void addStudent() {
        if (!validateInputs(false)) return;

        String id = txtStudentId.getText();
        String rollNo = txtRollNo.getText().trim();
        String regNo = txtRegNo.getText().trim();
        String name = txtName.getText().trim();
        
        Course course = (Course) cbCourse.getSelectedItem();
        String courseId = course != null ? course.getCourseId() : "";

        String sem = cbSemester.getSelectedItem().toString();
        String sec = cbSection.getSelectedItem().toString();
        String stat = cbStatus.getSelectedItem().toString();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();

        try {
            // Save photo if selected
            String photoPath = savePhotoFile(id);

            // Generate QR Code File
            String qrPath = QRGenerator.generateQRCode(id);

            Student student = new Student(id, rollNo, regNo, name, courseId, sem, sec, mobile, email, qrPath, photoPath, stat);

            boolean success = studentDAO.addStudent(student);
            if (success) {
                JOptionPane.showMessageDialog(this, "Student added successfully!\nQR Code generated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save student record to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
                QRGenerator.deleteQRCode(id);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error writing data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateStudent() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to update!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!validateInputs(true)) return;

        String rollNo = txtRollNo.getText().trim();
        String regNo = txtRegNo.getText().trim();
        String name = txtName.getText().trim();
        
        Course course = (Course) cbCourse.getSelectedItem();
        String courseId = course != null ? course.getCourseId() : "";

        String sem = cbSemester.getSelectedItem().toString();
        String sec = cbSection.getSelectedItem().toString();
        String stat = cbStatus.getSelectedItem().toString();
        String mobile = txtMobile.getText().trim();
        String email = txtEmail.getText().trim();

        try {
            String photoPath = savePhotoFile(selectedStudentId);

            File qrFile = QRGenerator.getQRFile(selectedStudentId);
            String qrPath = qrFile.exists() ? qrFile.getPath() : QRGenerator.generateQRCode(selectedStudentId);

            Student student = new Student(selectedStudentId, rollNo, regNo, name, courseId, sem, sec, mobile, email, qrPath, photoPath, stat);

            boolean success = studentDAO.updateStudent(student);
            if (success) {
                JOptionPane.showMessageDialog(this, "Student record updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save updates to database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteStudent() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table to delete!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete student: " + selectedStudentId + "?\nAll attendance records will be lost.",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = studentDAO.deleteStudent(selectedStudentId);
            if (success) {
                QRGenerator.deleteQRCode(selectedStudentId);
                // Also delete local photo
                Student s = studentDAO.getStudentById(selectedStudentId);
                if (s != null && s.getPhotoPath() != null) {
                    new File(s.getPhotoPath()).delete();
                }
                JOptionPane.showMessageDialog(this, "Student deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
                clearForm();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete student from database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String savePhotoFile(String studentId) {
        if (selectedPhotoFile == null) return null;
        
        // If photo file already in local directory, return path
        if (selectedPhotoFile.getParentFile().getName().equals("photos") && selectedPhotoFile.getParentFile().getParentFile().getName().equals("images")) {
            return selectedPhotoFile.getPath();
        }

        String extension = getFileExtension(selectedPhotoFile);
        File dest = new File("images/photos/" + studentId + extension);
        try {
            Files.copy(selectedPhotoFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return dest.getPath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIdx = name.lastIndexOf('.');
        if (lastIdx > 0) return name.substring(lastIdx);
        return ".png";
    }

    private void importExcelData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Excel File", "xlsx"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            List<Student> list = ExcelImporter.importStudentsFromExcel(selectedFile);
            if (list.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid student records found in the sheet! Please check the column layout.", "Import Cancelled", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int count = 0;
            for (Student s : list) {
                try {
                    s.setStudentId(IDGenerator.generateNextStudentId());
                    String qrPath = QRGenerator.generateQRCode(s.getStudentId());
                    s.setQrPath(qrPath);
                    if (studentDAO.addStudent(s)) {
                        count++;
                    } else {
                        QRGenerator.deleteQRCode(s.getStudentId());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            JOptionPane.showMessageDialog(this, "Import Complete!\nSuccessfully added " + count + " of " + list.size() + " students.", "Excel Import Done", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        }
    }

    private void exportExcelData() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Student_Records_Export.xlsx"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean success = ExcelExporter.exportToExcel(tblStudents, file.getAbsolutePath(), "Students");
            if (success) {
                JOptionPane.showMessageDialog(this, "Excel records exported successfully to: " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export Excel file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportStudentPDF() {
        if (selectedStudentId == null) {
            JOptionPane.showMessageDialog(this, "Select a student row to export their PDF profile!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student s = studentDAO.getStudentById(selectedStudentId);
        if (s == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("Profile_" + s.getStudentId() + ".pdf"));
        int res = chooser.showSaveDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            boolean success = PDFExporter.exportStudentProfile(s, file);
            if (success) {
                JOptionPane.showMessageDialog(this, "Student profile PDF exported successfully to: " + file.getName(), "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to export PDF profile.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
