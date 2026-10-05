package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBackupUtility {

    private static String findMySQLBinary(String binaryName) {
        // Common paths for MySQL bin on Windows
        String[] commonPaths = {
            "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\",
            "C:\\Program Files\\MySQL\\MySQL Server 8.1\\bin\\",
            "C:\\Program Files\\MySQL\\MySQL Server 8.2\\bin\\",
            "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\",
            "C:\\xampp\\mysql\\bin\\",
            "" // rely on PATH
        };

        for (String path : commonPaths) {
            String fullPath = path + binaryName + (System.getProperty("os.name").toLowerCase().contains("win") ? ".exe" : "");
            if (path.isEmpty()) {
                // Just check if in path by running it with --version
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{binaryName, "--version"});
                    p.waitFor();
                    return binaryName;
                } catch (Exception ignored) {}
            } else {
                File file = new File(fullPath);
                if (file.exists()) {
                    return fullPath;
                }
            }
        }
        return binaryName; // Fallback
    }

    public static boolean backupDatabase(String backupFilePath, String dbName, String username, String password) {
        String mysqldumpPath = findMySQLBinary("mysqldump");
        System.out.println("Using mysqldump binary: " + mysqldumpPath);
        
        try {
            List<String> command = new ArrayList<>();
            command.add(mysqldumpPath);
            command.add("-u" + username);
            if (password != null && !password.trim().isEmpty()) {
                command.add("-p" + password);
            }
            command.add("--databases");
            command.add(dbName);
            command.add("-r");
            command.add(backupFilePath);

            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Read error stream in case it fails
            try (InputStream errStream = process.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(errStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("mysqldump stderr: " + line);
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Database backup error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean restoreDatabase(String restoreFilePath, String dbName, String username, String password) {
        String mysqlPath = findMySQLBinary("mysql");
        System.out.println("Using mysql binary: " + mysqlPath);

        try {
            // Check if source file exists
            File sourceFile = new File(restoreFilePath);
            if (!sourceFile.exists()) {
                System.err.println("Restore file does not exist: " + restoreFilePath);
                return false;
            }

            List<String> command = new ArrayList<>();
            command.add(mysqlPath);
            command.add("-u" + username);
            if (password != null && !password.trim().isEmpty()) {
                command.add("-p" + password);
            }
            command.add(dbName);
            
            // To restore, we pipe the SQL script into mysql stdin
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectInput(ProcessBuilder.Redirect.from(sourceFile));
            Process process = pb.start();

            try (InputStream errStream = process.getErrorStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(errStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println("mysql restore stderr: " + line);
                }
            }

            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            System.err.println("Database restore error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
