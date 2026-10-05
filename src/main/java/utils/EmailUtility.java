package utils;

import dao.SettingsDAO;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

public class EmailUtility {

    public static boolean sendReportWithAttachment(String recipientEmail, String subject, String bodyMessage, File attachment) {
        SettingsDAO settings = new SettingsDAO();
        
        // SMTP Configurations read dynamically from settings with standard fallback defaults
        final String smtpHost = settings.getSetting("smtp_host", "smtp.gmail.com");
        final String smtpPort = settings.getSetting("smtp_port", "587");
        final String senderEmail = settings.getSetting("sender_email", "noreply.qrcode.attendance@gmail.com");
        final String senderPassword = settings.getSetting("sender_password", "app_password_here"); // Using App Passwords for Gmail

        Properties prop = new Properties();
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        prop.put("mail.smtp.host", smtpHost);
        prop.put("mail.smtp.port", smtpPort);
        prop.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);

            // Create multi-part body
            Multipart multipart = new MimeMultipart();

            // Text part
            MimeBodyPart textBodyPart = new MimeBodyPart();
            textBodyPart.setText(bodyMessage);
            multipart.addBodyPart(textBodyPart);

            // Attachment part
            if (attachment != null && attachment.exists()) {
                MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachment);
                attachmentBodyPart.setDataHandler(new DataHandler(source));
                attachmentBodyPart.setFileName(attachment.getName());
                multipart.addBodyPart(attachmentBodyPart);
            }

            message.setContent(multipart);

            // Send
            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipientEmail);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
