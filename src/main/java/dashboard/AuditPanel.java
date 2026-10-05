package dashboard;

import dao.AuditDAO;
import model.AuditLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AuditPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private final AuditDAO auditDAO = new AuditDAO();

    public AuditPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("System Security Audit Logs");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Logs");
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        refreshBtn.addActionListener(e -> refreshTable());
        headerPanel.add(refreshBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table Model Setup
        String[] columnNames = {"Log ID", "User ID", "Role", "Action Description", "Timestamp"};
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

        refreshTable();
    }

    public void refreshTable() {
        tableModel.setRowCount(0);
        List<AuditLog> list = auditDAO.getAllLogs();
        for (AuditLog log : list) {
            tableModel.addRow(new Object[]{
                    log.getLogId(),
                    log.getUserId(),
                    log.getUserRole(),
                    log.getAction(),
                    log.getTimestamp()
            });
        }
    }
}
