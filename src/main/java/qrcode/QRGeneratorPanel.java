package qrcode;

import dao.StudentDAO;
import model.Student;
import utils.QRGenerator;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class QRGeneratorPanel extends JPanel {
    private JTable tblStudents;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JLabel lblQRDisplay;
    private JLabel lblStudentInfo;
    
    private final StudentDAO studentDAO = new StudentDAO();
    private Student selectedStudent = null;

    public QRGeneratorPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
        refreshTable();
    }

    private void initUI() {
        // Left Column: Table of students to select
        JPanel leftPanel = new JPanel(new BorderLayout(5, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Select Student", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));

        // Search
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        txtSearch = new JTextField();
        txtSearch.putClientProperty("JTextField.placeholderText", "Search student...");
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchStudents(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchStudents(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchStudents(); }
        });
        searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
        searchPanel.add(txtSearch, BorderLayout.CENTER);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Student ID", "Roll No", "Name", "Department"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblStudents = new JTable(tableModel);
        tblStudents.setRowHeight(22);
        tblStudents.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tblStudents.getSelectedRow();
                if (row >= 0) {
                    String id = tableModel.getValueAt(row, 0).toString();
                    selectedStudent = studentDAO.getStudentById(id);
                    displayQR();
                }
            }
        });
        leftPanel.add(new JScrollPane(tblStudents), BorderLayout.CENTER);

        // Right Column: Display QR and Actions
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "QR Preview & Print", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        lblStudentInfo = new JLabel("No student selected", JLabel.CENTER);
        lblStudentInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        gbc.gridy = 0;
        rightPanel.add(lblStudentInfo, gbc);

        lblQRDisplay = new JLabel("QR Code Preview Area", JLabel.CENTER);
        lblQRDisplay.setPreferredSize(new Dimension(260, 260));
        lblQRDisplay.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        rightPanel.add(lblQRDisplay, gbc);

        // Button Panel for Actions
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        JButton btnSave = new JButton("Save QR Image");
        btnSave.setBackground(new Color(13, 110, 253));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.addActionListener(e -> saveQRFile());

        JButton btnPrint = new JButton("Print QR Code");
        btnPrint.setBackground(new Color(25, 135, 84));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnPrint.addActionListener(e -> printQRCode());

        btnPanel.add(btnSave);
        btnPanel.add(btnPrint);

        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        rightPanel.add(btnPanel, gbc);

        // Add to main SplitPane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setDividerLocation(360);
        add(split, BorderLayout.CENTER);
    }

    private void displayQR() {
        if (selectedStudent == null) return;
        
        lblStudentInfo.setText("<html><center>ID: " + selectedStudent.getStudentId() + "<br>Name: " + selectedStudent.getName() + "</center></html>");
        
        File file = QRGenerator.getQRFile(selectedStudent.getStudentId());
        if (file.exists()) {
            ImageIcon icon = new ImageIcon(file.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(240, 240, Image.SCALE_SMOOTH);
            lblQRDisplay.setIcon(new ImageIcon(img));
            lblQRDisplay.setText("");
        } else {
            // Regenerate QR code if it was deleted
            try {
                String path = QRGenerator.generateQRCode(selectedStudent.getStudentId());
                selectedStudent.setQrPath(path);
                studentDAO.updateStudent(selectedStudent);
                displayQR();
            } catch (Exception e) {
                lblQRDisplay.setIcon(null);
                lblQRDisplay.setText("Error generating QR");
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Student> list = studentDAO.getAllStudents();
        for (Student s : list) {
            tableModel.addRow(new Object[]{s.getStudentId(), s.getRollNo(), s.getName(), s.getDepartment()});
        }
    }

    private void searchStudents() {
        String q = txtSearch.getText().trim();
        tableModel.setRowCount(0);
        List<Student> list = studentDAO.getFilteredStudents(q, "All", "All", "All", "All", 100, 0);
        for (Student s : list) {
            tableModel.addRow(new Object[]{s.getStudentId(), s.getRollNo(), s.getName(), s.getDepartment()});
        }
    }

    private void saveQRFile() {
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please select a student first!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File qrFile = QRGenerator.getQRFile(selectedStudent.getStudentId());
        if (!qrFile.exists()) {
            JOptionPane.showMessageDialog(this, "QR Code file not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save QR Code Image");
        fileChooser.setSelectedFile(new File(selectedStudent.getStudentId() + "_QR.png"));

        int choice = fileChooser.showSaveDialog(this);
        if (choice == JFileChooser.APPROVE_OPTION) {
            File destFile = fileChooser.getSelectedFile();
            try (FileChannel sourceChannel = new FileInputStream(qrFile).getChannel();
                 FileChannel destChannel = new FileOutputStream(destFile).getChannel()) {
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                JOptionPane.showMessageDialog(this, "QR Code saved successfully to:\n" + destFile.getAbsolutePath(), "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to copy file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void printQRCode() {
        if (selectedStudent == null) {
            JOptionPane.showMessageDialog(this, "Please select a student first!", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        File qrFile = QRGenerator.getQRFile(selectedStudent.getStudentId());
        if (!qrFile.exists()) {
            JOptionPane.showMessageDialog(this, "QR Code file not found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
                if (pageIndex > 0) {
                    return NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pf.getImageableX(), pf.getImageableY());

                // Draw Title / Headers
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2d.drawString("STUDENT QR IDENTITY CARD", 80, 80);

                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g2d.drawString("ID: " + selectedStudent.getStudentId(), 80, 110);
                g2d.drawString("Name: " + selectedStudent.getName(), 80, 130);
                g2d.drawString("Roll No: " + selectedStudent.getRollNo(), 80, 150);
                g2d.drawString("Department: " + selectedStudent.getDepartment(), 80, 170);

                // Draw QR Code Image
                Image qrImage = Toolkit.getDefaultToolkit().getImage(qrFile.getAbsolutePath());
                g2d.drawImage(qrImage, 80, 200, 200, 200, null);

                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
