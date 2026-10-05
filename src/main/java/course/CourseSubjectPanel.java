package course;

import dao.CourseDAO;
import dao.SubjectDAO;
import model.Course;
import model.Subject;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CourseSubjectPanel extends JPanel {
    private JTable courseTable;
    private DefaultTableModel courseModel;
    private JTable subjectTable;
    private DefaultTableModel subjectModel;

    private final CourseDAO courseDAO = new CourseDAO();
    private final SubjectDAO subjectDAO = new SubjectDAO();

    public CourseSubjectPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Master title
        JLabel titleLabel = new JLabel("Courses & Subjects Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Split Pane Layout
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        splitPane.setResizeWeight(0.5);

        // --- Left Side: Course Management ---
        JPanel coursePanel = new JPanel(new BorderLayout(10, 10));
        coursePanel.setBorder(BorderFactory.createTitledBorder("Courses"));

        courseModel = new DefaultTableModel(new String[]{"ID", "Code", "Name", "Department"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        courseTable = new JTable(courseModel);
        courseTable.setRowHeight(24);
        courseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedCourseSubjects();
            }
        });
        coursePanel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel courseToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton addCourseBtn = new JButton("Add Course");
        JButton editCourseBtn = new JButton("Edit Course");
        JButton deleteCourseBtn = new JButton("Delete Course");
        courseToolbar.add(addCourseBtn);
        courseToolbar.add(editCourseBtn);
        courseToolbar.add(deleteCourseBtn);
        coursePanel.add(courseToolbar, BorderLayout.SOUTH);

        // --- Right Side: Subject Management ---
        JPanel subjectPanel = new JPanel(new BorderLayout(10, 10));
        subjectPanel.setBorder(BorderFactory.createTitledBorder("Subjects (Select a Course to View)"));

        subjectModel = new DefaultTableModel(new String[]{"Subject ID", "Code", "Subject Name"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        subjectTable = new JTable(subjectModel);
        subjectTable.setRowHeight(24);
        subjectPanel.add(new JScrollPane(subjectTable), BorderLayout.CENTER);

        JPanel subjectToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        JButton addSubjectBtn = new JButton("Add Subject");
        JButton editSubjectBtn = new JButton("Edit Subject");
        JButton deleteSubjectBtn = new JButton("Delete Subject");
        subjectToolbar.add(addSubjectBtn);
        subjectToolbar.add(editSubjectBtn);
        subjectToolbar.add(deleteSubjectBtn);
        subjectPanel.add(subjectToolbar, BorderLayout.SOUTH);

        splitPane.setLeftComponent(coursePanel);
        splitPane.setRightComponent(subjectPanel);
        add(splitPane, BorderLayout.CENTER);

        // Button Listeners
        addCourseBtn.addActionListener(e -> openAddCourseDialog());
        editCourseBtn.addActionListener(e -> openEditCourseDialog());
        deleteCourseBtn.addActionListener(e -> handleDeleteCourse());

        addSubjectBtn.addActionListener(e -> openAddSubjectDialog());
        editSubjectBtn.addActionListener(e -> openEditSubjectDialog());
        deleteSubjectBtn.addActionListener(e -> handleDeleteSubject());

        refreshData();
    }

    public void refreshData() {
        courseModel.setRowCount(0);
        subjectModel.setRowCount(0);
        List<Course> courses = courseDAO.getAllCourses();
        for (Course c : courses) {
            courseModel.addRow(new Object[]{
                    c.getCourseId(), c.getCourseCode(), c.getCourseName(), c.getDepartment()
            });
        }
    }

    private void loadSelectedCourseSubjects() {
        subjectModel.setRowCount(0);
        int row = courseTable.getSelectedRow();
        if (row != -1) {
            String courseId = (String) courseTable.getValueAt(row, 0);
            List<Subject> subjects = subjectDAO.getSubjectsByCourseId(courseId);
            for (Subject s : subjects) {
                subjectModel.addRow(new Object[]{
                        s.getSubjectId(), s.getSubjectCode(), s.getSubjectName()
                });
            }
        }
    }

    // --- Course CRUD Actions ---
    private void openAddCourseDialog() {
        JPanel form = new JPanel(new GridLayout(4, 2, 8, 8));
        form.setPreferredSize(new Dimension(380, 150));
        JTextField idField = new JTextField();
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();

        form.add(new JLabel("Course ID (e.g. CRS1004):")); form.add(idField);
        form.add(new JLabel("Course Code (e.g. CS-MCA):")); form.add(codeField);
        form.add(new JLabel("Course Name:")); form.add(nameField);
        form.add(new JLabel("Department:")); form.add(deptField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Course", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();

            if (id.isEmpty() || code.isEmpty() || name.isEmpty() || dept.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Course c = new Course(id, code, name, dept);
            if (courseDAO.insertCourse(c)) {
                JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to insert course. ID or Code might be duplicates.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEditCourseDialog() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a course to edit!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) courseTable.getValueAt(row, 0);
        Course c = courseDAO.getCourseById(id);
        if (c == null) return;

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setPreferredSize(new Dimension(380, 120));
        JTextField codeField = new JTextField(c.getCourseCode());
        JTextField nameField = new JTextField(c.getCourseName());
        JTextField deptField = new JTextField(c.getDepartment());

        form.add(new JLabel("Course Code:")); form.add(codeField);
        form.add(new JLabel("Course Name:")); form.add(nameField);
        form.add(new JLabel("Department:")); form.add(deptField);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit Course Details", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            c.setCourseCode(codeField.getText().trim());
            c.setCourseName(nameField.getText().trim());
            c.setDepartment(deptField.getText().trim());

            if (c.getCourseCode().isEmpty() || c.getCourseName().isEmpty() || c.getDepartment().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Required fields cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (courseDAO.updateCourse(c)) {
                JOptionPane.showMessageDialog(this, "Course updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }
        }
    }

    private void handleDeleteCourse() {
        int row = courseTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a course to delete!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) courseTable.getValueAt(row, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Delete course " + id + "? All registered students and subjects will be affected.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (courseDAO.deleteCourse(id)) {
                JOptionPane.showMessageDialog(this, "Course deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete. Course has active references.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Subject CRUD Actions ---
    private void openAddSubjectDialog() {
        int courseRow = courseTable.getSelectedRow();
        if (courseRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a Course from the left table to attach a new Subject!", "Course Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String courseId = (String) courseTable.getValueAt(courseRow, 0);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 8));
        form.setPreferredSize(new Dimension(380, 110));
        JTextField idField = new JTextField();
        JTextField codeField = new JTextField();
        JTextField nameField = new JTextField();

        form.add(new JLabel("Subject ID (e.g. SUB1006):")); form.add(idField);
        form.add(new JLabel("Subject Code (e.g. CS-304):")); form.add(codeField);
        form.add(new JLabel("Subject Name:")); form.add(nameField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add Subject", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String code = codeField.getText().trim();
            String name = nameField.getText().trim();

            if (id.isEmpty() || code.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Subject s = new Subject(id, code, name, courseId);
            if (subjectDAO.insertSubject(s)) {
                JOptionPane.showMessageDialog(this, "Subject added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSelectedCourseSubjects();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add subject. ID or Code duplicate.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEditSubjectDialog() {
        int subRow = subjectTable.getSelectedRow();
        if (subRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a subject to edit!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) subjectTable.getValueAt(subRow, 0);
        String code = (String) subjectTable.getValueAt(subRow, 1);
        String name = (String) subjectTable.getValueAt(subRow, 2);

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setPreferredSize(new Dimension(380, 80));
        JTextField codeField = new JTextField(code);
        JTextField nameField = new JTextField(name);

        form.add(new JLabel("Subject Code:")); form.add(codeField);
        form.add(new JLabel("Subject Name:")); form.add(nameField);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit Subject", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String nCode = codeField.getText().trim();
            String nName = nameField.getText().trim();

            if (nCode.isEmpty() || nName.isEmpty()) return;

            int courseRow = courseTable.getSelectedRow();
            String courseId = (String) courseTable.getValueAt(courseRow, 0);

            Subject s = new Subject(id, nCode, nName, courseId);
            if (subjectDAO.updateSubject(s)) {
                JOptionPane.showMessageDialog(this, "Subject updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSelectedCourseSubjects();
            }
        }
    }

    private void handleDeleteSubject() {
        int subRow = subjectTable.getSelectedRow();
        if (subRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a subject to delete!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String id = (String) subjectTable.getValueAt(subRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Subject: " + id + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (subjectDAO.deleteSubject(id)) {
                JOptionPane.showMessageDialog(this, "Subject deleted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadSelectedCourseSubjects();
            }
        }
    }
}
