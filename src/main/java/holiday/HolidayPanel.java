package holiday;

import dao.HolidayDAO;
import model.Holiday;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.util.List;

public class HolidayPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private final HolidayDAO holidayDAO = new HolidayDAO();

    public HolidayPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Holiday Calendar");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Toolbar Buttons
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addBtn = new JButton("Add Holiday");
        JButton deleteBtn = new JButton("Delete Selected");
        JButton refreshBtn = new JButton("Refresh");

        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));

        addBtn.setBackground(new Color(40, 167, 69));
        addBtn.setForeground(Color.WHITE);

        toolbar.add(addBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);
        headerPanel.add(toolbar, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table Model Setup
        String[] columnNames = {"ID", "Holiday Date (YYYY-MM-DD)", "Description / Event"};
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

        // Actions
        addBtn.addActionListener(e -> openAddDialog());
        deleteBtn.addActionListener(e -> handleDelete());
        refreshBtn.addActionListener(e -> refreshTable());

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<Holiday> holidays = holidayDAO.getAllHolidays();
        for (Holiday h : holidays) {
            tableModel.addRow(new Object[]{
                    h.getId(),
                    h.getHolidayDate().toString(),
                    h.getDescription()
            });
        }
    }

    private void openAddDialog() {
        JPanel form = new JPanel(new GridLayout(2, 2, 10, 10));
        form.setPreferredSize(new Dimension(380, 80));

        JTextField dateField = new JTextField();
        dateField.setToolTipText("Format: YYYY-MM-DD");
        // Set default text as current date for user convenience
        dateField.setText(new java.sql.Date(System.currentTimeMillis()).toString());
        JTextField descField = new JTextField();

        form.add(new JLabel("Holiday Date (YYYY-MM-DD):")); form.add(dateField);
        form.add(new JLabel("Description:")); form.add(descField);

        int res = JOptionPane.showConfirmDialog(this, form, "Add Institutional Holiday", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res == JOptionPane.OK_OPTION) {
            String dateText = dateField.getText().trim();
            String desc = descField.getText().trim();

            if (dateText.isEmpty() || desc.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Date sqlDate = Date.valueOf(dateText);
                Holiday h = new Holiday(0, sqlDate, desc);
                if (holidayDAO.insertHoliday(h)) {
                    JOptionPane.showMessageDialog(this, "Holiday registered successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to register holiday. Date might already be marked.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, "Invalid date format! Please use YYYY-MM-DD.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a holiday record to delete!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) table.getValueAt(selectedRow, 0);
        String dateStr = (String) table.getValueAt(selectedRow, 1);
        int choice = JOptionPane.showConfirmDialog(this, "Delete Holiday scheduled on " + dateStr + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            if (holidayDAO.deleteHoliday(id)) {
                JOptionPane.showMessageDialog(this, "Holiday removed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            }
        }
    }
}
