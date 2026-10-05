package utils;

import dao.CourseDAO;
import model.Course;
import model.Student;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelImporter {

    public static List<Student> importStudentsFromExcel(File file) {
        List<Student> students = new ArrayList<>();
        CourseDAO courseDAO = new CourseDAO();
        List<Course> courses = courseDAO.getAllCourses();

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            // Iterate rows, skip header (row 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Expected columns: 
                // Col 0: Roll No, Col 1: Reg No, Col 2: Name, Col 3: Course Code, Col 4: Semester, Col 5: Section, Col 6: Mobile, Col 7: Email
                String rollNo = formatter.formatCellValue(row.getCell(0)).trim();
                String regNo = formatter.formatCellValue(row.getCell(1)).trim();
                String name = formatter.formatCellValue(row.getCell(2)).trim();
                String courseCode = formatter.formatCellValue(row.getCell(3)).trim();
                String semester = formatter.formatCellValue(row.getCell(4)).trim();
                String section = formatter.formatCellValue(row.getCell(5)).trim();
                String mobile = formatter.formatCellValue(row.getCell(6)).trim();
                String email = formatter.formatCellValue(row.getCell(7)).trim();

                // Skip incomplete rows
                if (rollNo.isEmpty() || name.isEmpty() || courseCode.isEmpty()) {
                    continue;
                }

                // Map courseCode to courseId
                String courseId = null;
                for (Course c : courses) {
                    if (c.getCourseCode().equalsIgnoreCase(courseCode)) {
                        courseId = c.getCourseId();
                        break;
                    }
                }

                // Fallback to first course if not found, or skip
                if (courseId == null) {
                    System.out.println("Course code not found: " + courseCode + " - skipping row " + i);
                    continue;
                }

                Student s = new Student();
                // Generate a temporary student ID which StudentDAO will auto-overwrite with the sequential ID
                s.setStudentId(""); 
                s.setRollNo(rollNo);
                s.setRegistrationNo(regNo);
                s.setName(name);
                s.setCourseId(courseId);
                s.setSemester(semester);
                s.setSection(section);
                s.setMobile(mobile);
                s.setEmail(email);
                s.setStatus("Active");

                students.add(s);
            }

        } catch (Exception e) {
            System.err.println("Error importing students from Excel: " + e.getMessage());
            e.printStackTrace();
        }
        return students;
    }
}
