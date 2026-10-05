package qrcode;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import dao.AttendanceDAO;
import dao.StudentDAO;
import model.Student;
import utils.DateUtility;
import utils.SecurityUtility;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class QRScannerFrame extends JFrame {
    private Webcam webcam = null;
    private CameraPanel cameraPanel = null;
    private JComboBox<Webcam> cameraSelector;
    private ExecutorService executor;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    
    private final StudentDAO studentDAO = new StudentDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();
    private final ScannerCallback callback;
    
    private final String facultyId;
    private final String subjectId;

    public interface ScannerCallback {
        void onScanSuccess(String studentId, String studentName, String time);
    }

    public QRScannerFrame(String facultyId, String subjectId, ScannerCallback callback) {
        this.facultyId = facultyId;
        this.subjectId = subjectId;
        this.callback = callback;
        
        setTitle("QR Code Attendance Scanner");
        setSize(700, 580);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());

        // Header panel with Instructions and Camera selector
        JPanel controlHeader = new JPanel(new BorderLayout());
        controlHeader.setBackground(new Color(24, 28, 36));
        controlHeader.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Place QR Code in front of the Camera");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        controlHeader.add(titleLabel, BorderLayout.WEST);

        // Camera select dropdown
        JPanel cameraPanelWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        cameraPanelWrapper.setOpaque(false);
        JLabel camLabel = new JLabel("Select Camera: ");
        camLabel.setForeground(Color.WHITE);
        camLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cameraPanelWrapper.add(camLabel);

        List<Webcam> webcams = Webcam.getWebcams();
        cameraSelector = new JComboBox<>(webcams.toArray(new Webcam[0]));
        cameraSelector.setPreferredSize(new Dimension(200, 26));
        cameraSelector.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cameraSelector.addActionListener(e -> switchCamera((Webcam) cameraSelector.getSelectedItem()));
        cameraPanelWrapper.add(cameraSelector);

        controlHeader.add(cameraPanelWrapper, BorderLayout.EAST);
        add(controlHeader, BorderLayout.NORTH);

        // Main camera panel
        cameraPanel = new CameraPanel();
        add(cameraPanel, BorderLayout.CENTER);

        // Add window listener to release resources on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeScanner();
            }
        });

        // Initialize and start Webcam scanning
        if (!webcams.isEmpty()) {
            initWebcam(webcams.get(0));
        } else {
            JOptionPane.showMessageDialog(this, "No webcam detected on this system!", "Camera Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private synchronized void initWebcam(Webcam selectedWebcam) {
        try {
            webcam = selectedWebcam;
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            
            if (!webcam.open()) {
                JOptionPane.showMessageDialog(this, "Failed to open webcam! It might be in use.", "Camera Error", JOptionPane.ERROR_MESSAGE);
                dispose();
                return;
            }

            isRunning.set(true);
            executor = Executors.newSingleThreadExecutor();
            executor.submit(this::scanLoop);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error initializing camera: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            closeScanner();
            dispose();
        }
    }

    private synchronized void switchCamera(Webcam newWebcam) {
        if (webcam != null && webcam.isOpen()) {
            isRunning.set(false);
            if (executor != null) {
                executor.shutdown();
            }
            webcam.close();
        }
        initWebcam(newWebcam);
    }

    private void scanLoop() {
        int laserY = 150;
        int direction = 3;
        
        while (isRunning.get()) {
            if (webcam == null || !webcam.isOpen()) {
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
                continue;
            }

            BufferedImage image = webcam.getImage();
            if (image == null) {
                continue;
            }

            // Update UI with new frame
            cameraPanel.setImage(image);
            
            // Animate laser scanner line
            laserY += direction;
            if (laserY > 370 || laserY < 130) {
                direction = -direction;
            }
            cameraPanel.setLaserY(laserY);

            // Attempt to decode QR code
            try {
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(bitmap);
                
                // QR scanned successfully!
                String encryptedPayload = result.getText();
                processScannedToken(encryptedPayload);
                
            } catch (NotFoundException e) {
                // No QR Code found in this frame
            }

            try {
                Thread.sleep(25); // Faster scan response ~40 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processScannedToken(String encryptedToken) {
        // Pause scanning while processing
        isRunning.set(false);
        
        SwingUtilities.invokeLater(() -> {
            // Decrypt and validate token
            String studentId = SecurityUtility.validateQRToken(encryptedToken);
            if (studentId == null) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, 
                    "Invalid, tampered, or expired QR code detected!", 
                    "Security Breach Check", 
                    JOptionPane.ERROR_MESSAGE);
                
                // Resume scan
                isRunning.set(true);
                executor.submit(this::scanLoop);
                return;
            }

            Student student = studentDAO.getStudentById(studentId);
            if (student == null) {
                JOptionPane.showMessageDialog(this, "Student record (" + studentId + ") not found!", "Scan Error", JOptionPane.ERROR_MESSAGE);
                isRunning.set(true);
                executor.submit(this::scanLoop);
                return;
            }

            // Check if attendance already marked today for this subject
            if (attendanceDAO.isAttendanceMarkedToday(studentId, subjectId)) {
                Toolkit.getDefaultToolkit().beep();
                JOptionPane.showMessageDialog(this, 
                    "Student: " + student.getName() + " (" + studentId + ")\nAttendance already marked today for this subject!", 
                    "Duplicate Attendance", 
                    JOptionPane.WARNING_MESSAGE);
                isRunning.set(true);
                executor.submit(this::scanLoop);
                return;
            }

            // Check late threshold (e.g., if scan time is after threshold)
            String status = "Present";
            
            // Mark attendance
            boolean success = attendanceDAO.markAttendance(studentId, subjectId, facultyId, status);
            if (success) {
                // Sound beep for success
                Toolkit.getDefaultToolkit().beep();
                
                String timeStr = DateUtility.getCurrentTime();
                
                JOptionPane.showMessageDialog(this, 
                    "SUCCESS!\n\nID: " + studentId + 
                    "\nName: " + student.getName() + 
                    "\nCourse: " + student.getCourseName() +
                    "\nTime: " + timeStr + 
                    "\nStatus: " + status + "\n\nAttendance recorded successfully.", 
                    "Attendance Marked", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                // Notify parent dashboard and close scanner
                if (callback != null) {
                    callback.onScanSuccess(studentId, student.getName(), timeStr);
                }
                closeScanner();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to write attendance. Database error.", "Database Error", JOptionPane.ERROR_MESSAGE);
                isRunning.set(true);
                executor.submit(this::scanLoop);
            }
        });
    }

    public synchronized void closeScanner() {
        isRunning.set(false);
        if (executor != null) {
            executor.shutdown();
        }
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            System.out.println("Webcam connection closed.");
        }
    }

    // Custom Panel to draw webcam feed and scanning target overlay
    private static class CameraPanel extends JPanel {
        private BufferedImage currentFrame = null;
        private int laserY = 200;

        public CameraPanel() {
            setPreferredSize(new Dimension(640, 480));
            setBackground(Color.BLACK);
        }

        public void setImage(BufferedImage img) {
            this.currentFrame = img;
            repaint();
        }

        public void setLaserY(int y) {
            this.laserY = y;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (currentFrame != null) {
                int imgWidth = currentFrame.getWidth();
                int imgHeight = currentFrame.getHeight();
                int x = (getWidth() - imgWidth) / 2;
                int y = (getHeight() - imgHeight) / 2;
                
                // Draw camera frame
                g2.drawImage(currentFrame, x, y, this);

                // Draw overlay HUD (Scanning targeting box)
                int boxSize = 280;
                int boxX = (getWidth() - boxSize) / 2;
                int boxY = (getHeight() - boxSize) / 2;

                // Draw translucent mask outside target box
                g2.setColor(new Color(0, 0, 0, 130));
                g2.fillRect(0, 0, getWidth(), boxY);
                g2.fillRect(0, boxY + boxSize, getWidth(), getHeight() - (boxY + boxSize));
                g2.fillRect(0, boxY, boxX, boxSize);
                g2.fillRect(boxX + boxSize, boxY, getWidth() - (boxX + boxSize), boxSize);

                // Draw targeted scanning square borders
                g2.setColor(new Color(0, 255, 100)); // Bright Neon Green
                g2.setStroke(new BasicStroke(4));
                
                int cornerLen = 30;
                // Top-Left corner
                g2.drawLine(boxX, boxY, boxX + cornerLen, boxY);
                g2.drawLine(boxX, boxY, boxX, boxY + cornerLen);
                // Top-Right corner
                g2.drawLine(boxX + boxSize, boxY, boxX + boxSize - cornerLen, boxY);
                g2.drawLine(boxX + boxSize, boxY, boxX + boxSize, boxY + cornerLen);
                // Bottom-Left corner
                g2.drawLine(boxX, boxY + boxSize, boxX + cornerLen, boxY + boxSize);
                g2.drawLine(boxX, boxY + boxSize, boxX, boxY + boxSize - cornerLen);
                // Bottom-Right corner
                g2.drawLine(boxX + boxSize, boxY + boxSize, boxX + boxSize - cornerLen, boxY + boxSize);
                g2.drawLine(boxX + boxSize, boxY + boxSize, boxX + boxSize, boxY + boxSize - cornerLen);

                // Draw thin box border
                g2.setStroke(new BasicStroke(1));
                g2.setColor(new Color(0, 255, 100, 80));
                g2.drawRect(boxX, boxY, boxSize, boxSize);

                // Draw Laser scanning line
                g2.setColor(new Color(255, 50, 50, 210)); // Soft Red Laser
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(boxX + 2, laserY, boxX + boxSize - 2, laserY);
                
                // Laser glow
                g2.setColor(new Color(255, 50, 50, 50));
                g2.fillRect(boxX + 2, laserY - 4, boxSize - 4, 8);
            } else {
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                String loadingText = "Starting Webcam Engine...";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(loadingText)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(loadingText, x, y);
            }
        }
    }
}
