package utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class QRGenerator {
    private static final String DIRECTORY = "qr_codes";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 300;

    static {
        // Ensure qr_codes directory exists
        File dir = new File(DIRECTORY);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("Created qr_codes directory");
            }
        }
    }

    public static String generateQRCode(String studentId) throws WriterException, IOException {
        String fileName = studentId + ".png";
        String filePath = DIRECTORY + "/" + fileName;
        
        String payload = SecurityUtility.generateQRToken(studentId, true);
        
        MultiFormatWriter qrCodeWriter = new MultiFormatWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(payload, BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
        
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);
        
        System.out.println("QR Code generated successfully: " + filePath);
        return filePath;
    }

    public static File getQRFile(String studentId) {
        String filePath = DIRECTORY + "/" + studentId + ".png";
        return new File(filePath);
    }

    public static boolean deleteQRCode(String studentId) {
        File file = getQRFile(studentId);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}
