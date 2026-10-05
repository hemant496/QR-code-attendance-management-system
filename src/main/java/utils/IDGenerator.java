package utils;

import dao.StudentDAO;

public class IDGenerator {
    private static final String PREFIX = "STU";
    private static final int STARTING_NUMBER = 1001;

    public static String generateNextStudentId() {
        StudentDAO studentDAO = new StudentDAO();
        String lastId = studentDAO.getLastStudentId();
        
        if (lastId == null || lastId.length() <= PREFIX.length()) {
            return PREFIX + STARTING_NUMBER;
        }
        
        try {
            String numberPart = lastId.substring(PREFIX.length());
            int nextNumber = Integer.parseInt(numberPart) + 1;
            return PREFIX + nextNumber;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing last ID: " + lastId + ", resetting sequence.");
            return PREFIX + STARTING_NUMBER;
        }
    }
}
