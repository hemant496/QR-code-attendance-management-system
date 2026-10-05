package utils;

import dao.SettingsDAO;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SecurityUtility {
    // 128-bit key (16 characters) for symmetric AES encryption
    private static final String AES_KEY = "QrAttSytemSecretKey123#"; // 16-byte aligned key helper
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(
            AES_KEY.substring(0, 16).getBytes(StandardCharsets.UTF_8), "AES"
    );

    public static String encrypt(String strToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, SECRET_KEY);
            return Base64.getUrlEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.err.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(String strToDecrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, SECRET_KEY);
            return new String(cipher.doFinal(Base64.getUrlDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Decryption failure (invalid or tampered QR code)
            System.err.println("Error while decrypting token: " + e.getMessage());
        }
        return null;
    }

    /**
     * Generates a signed, encrypted QR payload containing studentId and timestamp
     */
    public static String generateQRToken(String studentId, boolean isStatic) {
        if (isStatic) {
            return encrypt(studentId + "|STATIC");
        } else {
            long currentTimestampMillis = System.currentTimeMillis();
            String rawPayload = studentId + "|" + currentTimestampMillis;
            return encrypt(rawPayload);
        }
    }

    /**
     * Validates a signed QR payload. Returns studentId if valid, otherwise null.
     */
    public static String validateQRToken(String encryptedToken) {
        if (encryptedToken == null || encryptedToken.trim().isEmpty()) {
            return null;
        }
        String decryptedPayload = decrypt(encryptedToken);
        if (decryptedPayload == null) {
            return null;
        }

        String[] parts = decryptedPayload.split("\\|");
        if (parts.length != 2) {
            return null;
        }

        String studentId = parts[0];
        String timeOrStatic = parts[1];

        if ("STATIC".equalsIgnoreCase(timeOrStatic)) {
            return studentId; // Static cards never expire
        }

        long tokenTimestamp;
        try {
            tokenTimestamp = Long.parseLong(timeOrStatic);
        } catch (NumberFormatException e) {
            System.err.println("Invalid timestamp in QR Token payload: " + timeOrStatic);
            return null;
        }

        // Expiration check
        SettingsDAO settingsDAO = new SettingsDAO();
        int expiryMinutes = Integer.parseInt(settingsDAO.getSetting("qr_expiration_minutes", "5"));
        
        long diffMillis = System.currentTimeMillis() - tokenTimestamp;
        long maxDiffMillis = (long) expiryMinutes * 60 * 1000;

        if (diffMillis < 0 || diffMillis > maxDiffMillis) {
            System.out.println("QR Token has expired for Student: " + studentId);
            return null; // Expired
        }

        return studentId;
    }
}
