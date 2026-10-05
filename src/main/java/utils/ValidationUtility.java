package utils;

import java.util.regex.Pattern;

public class ValidationUtility {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidMobile(String mobile) {
        if (isEmpty(mobile)) return false;
        return MOBILE_PATTERN.matcher(mobile).matches();
    }

    public static boolean isValidNumber(String numberStr) {
        if (isEmpty(numberStr)) return false;
        try {
            Double.parseDouble(numberStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
