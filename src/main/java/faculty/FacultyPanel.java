package faculty;

import dao.FacultyDAO;
import model.Faculty;
import org.mindrot.jbcrypt.BCrypt;
import utils.ValidationUtility;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FacultyPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private final FacultyDAO facultyDAO = new FacultyDAO();

    public FacultyPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Faculty Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // CRUD Button Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addBtn = new JButton("Add Faculty");
        JButton editBtn = new JButton("Edit Selected");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        editBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        addBtn.setBackground(new Color(40, 167, 69));
        addBtn.setForeground(Color.WHITE);

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);
        headerPanel.add(toolbar, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table Model Setup
        String[] columnNames = {"Faculty ID", "Username", "Name", "Department", "Email", "Mobile"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // Event Handlers
        addBtn.addActionListener(e -> openAddDialog());
        editBtn.addActionListener(e -> openEditDialog());
        deleteBtn.addActionListener(e -> handleDelete());
        refreshBtn.addActionListener(e -> refreshTable());

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Faculty> list = facultyDAO.getAllFaculty();
        for (Faculty f : list) {
            tableModel.addRow(new Object[]{
                    f.getFacultyId(),
                    f.getUsername(),
                    f.getName(),
                    f.getDepartment(),
                    f.getEmail(),
                    f.getMobile()
            });
        }
    }

    private void openAddDialog() {
        JPanel form = new JPanel(new GridLayout(7, 2, 10, 10));
        form.setPreferredSize(new Dimension(400, 250));

        JTextField idField = new JTextField();
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JTextField nameField = new JTextField();
        JTextField deptField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField mobField = new JTextField();

        form.add(new JLabel("Faculty ID:")); form.add(idField);
        form.add(new JLabel("Username:")); form.add(userField);
        form.add(new JLabel("Password:")); form.add(passField);
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Department:")); form.add(deptField);
        form.add(new JLabel("Email:")); form.add(emailField);
        form.add(new JLabel("Mobile:")); form.add(mobField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add Faculty Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();
            String email = emailField.getText().trim();
            String mob = mobField.getText().trim();

            if (id.isEmpty() || user.isEmpty() || pass.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields (except mobile/department) are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!ValidationUtility.isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String hash = BCrypt.hashpw(pass, BCrypt.gensalt());
            Faculty f = new Faculty(id, user, hash, name, dept, email, mob);
            
            if (facultyDAO.insertFaculty(f)) {
                JOptionPane.showMessageDialog(this, "Faculty account created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create account. ID or Username might already exist.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty record to edit!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) table.getValueAt(selectedRow, 0);
        Faculty f = facultyDAO.getFacultyById(id);
        if (f == null) return;

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 10));
        form.setPreferredSize(new Dimension(400, 220));

        JTextField userField = new JTextField(f.getUsername());
        JTextField nameField = new JTextField(f.getName());
        JTextField deptField = new JTextField(f.getDepartment());
        JTextField emailField = new JTextField(f.getEmail());
        JTextField mobField = new JTextField(f.getMobile());

        form.add(new JLabel("Faculty ID (Read-only):")); form.add(new JLabel(id));
        form.add(new JLabel("Username:")); form.add(userField);
        form.add(new JLabel("Name:")); form.add(nameField);
        form.add(new JLabel("Department:")); form.add(deptField);
        form.add(new JLabel("Email:")); form.add(emailField);
        form.add(new JLabel("Mobile:")); form.add(mobField);

        int res = JOptionPane.showConfirmDialog(this, form, "Edit Faculty Account", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String user = userField.getText().trim();
            String name = nameField.getText().trim();
            String dept = deptField.getText().trim();
            String email = emailField.getText().trim();
            String mob = mobField.getText().trim();

            if (user.isEmpty() || name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Required fields cannot be left empty!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            f.setUsername(user);
            f.setName(name);
            f.setDepartment(dept);
            f.setEmail(email);
            f.setMobile(mob);

            if (facultyDAO.updateFaculty(f)) {
                JOptionPane.showMessageDialog(this, "Faculty profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update record.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a faculty record to delete!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = (String) table.getValueAt(selectedRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Faculty ID: " + id + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (facultyDAO.deleteFaculty(id)) {
                JOptionPane.showMessageDialog(this, "Faculty account deleted successfully!", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete account. Active database constraint issue.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
