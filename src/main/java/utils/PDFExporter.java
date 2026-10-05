package utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import model.Attendance;
import model.Student;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class PDFExporter {

    public static boolean exportAttendanceReport(List<Attendance> list, File file) {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            
            // Header and Footer Event Handler
            writer.setPageEvent(new PdfPageEventHelper() {
                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    cb.saveState();
                    cb.beginText();
                    try {
                        cb.setFontAndSize(BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false), 8);
                    } catch (Exception ignored) {}
                    cb.setColorFill(Color.GRAY);
                    cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, "Page " + writer.getPageNumber(), 559, 18, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "QR Code Attendance System Report", 36, 18, 0);
                    cb.endText();
                    cb.restoreState();
                }
            });

            document.open();

            // Document Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.DARK_GRAY);
            Paragraph title = new Paragraph("Attendance Logs Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Summary Info
            Font metaFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Paragraph meta = new Paragraph("Total Records: " + list.size() + "\nDate Generated: " + DateUtility.getCurrentDate(), metaFont);
            meta.setSpacingAfter(15);
            document.add(meta);

            // Table Creation
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{15f, 25f, 25f, 15f, 10f, 10f});

            // Table Header Styles
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Color headerBg = new Color(24, 28, 36);

            String[] headers = {"Student ID", "Student Name", "Subject", "Date", "Time", "Status"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(headerBg);
                cell.setPadding(8);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            }

            // Table Body
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            for (Attendance a : list) {
                table.addCell(createTableCell(a.getStudentId(), bodyFont, Element.ALIGN_CENTER));
                table.addCell(createTableCell(a.getStudentName(), bodyFont, Element.ALIGN_LEFT));
                table.addCell(createTableCell(a.getSubjectName(), bodyFont, Element.ALIGN_LEFT));
                table.addCell(createTableCell(a.getDate().toString(), bodyFont, Element.ALIGN_CENTER));
                table.addCell(createTableCell(a.getTime().toString(), bodyFont, Element.ALIGN_CENTER));

                // Color code Status cell
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
                PdfPCell statusCell = new PdfPCell(new Phrase(a.getStatus(), statusFont));
                statusCell.setPadding(6);
                statusCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if ("Present".equalsIgnoreCase(a.getStatus())) {
                    statusFont.setColor(new Color(0, 150, 0));
                    statusCell.setBackgroundColor(new Color(220, 245, 220));
                } else if ("Late".equalsIgnoreCase(a.getStatus())) {
                    statusFont.setColor(new Color(180, 100, 0));
                    statusCell.setBackgroundColor(new Color(255, 240, 210));
                } else {
                    statusFont.setColor(new Color(180, 0, 0));
                    statusCell.setBackgroundColor(new Color(255, 220, 220));
                }
                table.addCell(statusCell);
            }

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean exportStudentProfile(Student student, File file) {
        Document document = new Document(PageSize.A4, 54, 54, 54, 54);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Document Header / Logo
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new Color(24, 28, 36));
            Paragraph header = new Paragraph("Student Academic Profile", headerFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(30);
            document.add(header);

            // Two Column Layout for Profile details and Images
            PdfPTable layoutTable = new PdfPTable(2);
            layoutTable.setWidthPercentage(100);
            layoutTable.setWidths(new float[]{65f, 35f});
            layoutTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

            // Details Column
            PdfPCell detailsCell = new PdfPCell();
            detailsCell.setBorder(Rectangle.NO_BORDER);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);

            detailsCell.addElement(new Paragraph("STUDENT ID: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getStudentId(), valueFont));
            detailsCell.addElement(new Paragraph("\nNAME: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getName(), valueFont));
            detailsCell.addElement(new Paragraph("\nROLL NUMBER: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getRollNo(), valueFont));
            detailsCell.addElement(new Paragraph("\nREGISTRATION NO: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getRegistrationNo(), valueFont));
            detailsCell.addElement(new Paragraph("\nCOURSE: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getCourseName(), valueFont));
            detailsCell.addElement(new Paragraph("\nDEPARTMENT: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getDepartment(), valueFont));
            detailsCell.addElement(new Paragraph("\nSEMESTER / SECTION: ", labelFont));
            detailsCell.addElement(new Paragraph(student.getSemester() + " - " + student.getSection(), valueFont));
            detailsCell.addElement(new Paragraph("\nCONTACT DETAILS: ", labelFont));
            detailsCell.addElement(new Paragraph("Mobile: " + student.getMobile() + " | Email: " + student.getEmail(), valueFont));

            layoutTable.addCell(detailsCell);

            // Image Column (Photo & QR Code)
            PdfPCell imageCell = new PdfPCell();
            imageCell.setBorder(Rectangle.NO_BORDER);
            imageCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            // Student Photo
            if (student.getPhotoPath() != null && new File(student.getPhotoPath()).exists()) {
                Image photo = Image.getInstance(student.getPhotoPath());
                photo.scaleToFit(120, 150);
                photo.setAlignment(Element.ALIGN_CENTER);
                photo.setBorder(Rectangle.BOX);
                photo.setBorderWidth(1);
                photo.setBorderColor(Color.LIGHT_GRAY);
                imageCell.addElement(new Paragraph("Student Photo:", labelFont));
                imageCell.addElement(photo);
            } else {
                imageCell.addElement(new Paragraph("Student Photo:\n[ No Photo Uploaded ]\n\n", labelFont));
            }

            // QR Code Card
            if (student.getQrPath() != null && new File(student.getQrPath()).exists()) {
                imageCell.addElement(new Paragraph("\nSecurity QR Card:", labelFont));
                Image qr = Image.getInstance(student.getQrPath());
                qr.scaleToFit(120, 120);
                qr.setAlignment(Element.ALIGN_CENTER);
                imageCell.addElement(qr);
            }

            layoutTable.addCell(imageCell);
            document.add(layoutTable);

            // Footer Notice
            Paragraph notice = new Paragraph("\n\n* This is a computer-generated academic record from the QR Code Attendance Management System.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY));
            notice.setAlignment(Element.ALIGN_CENTER);
            document.add(notice);

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static PdfPCell createTableCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setPadding(6);
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
}
