# QR Code Based Attendance Management System

A professional, 100% standalone desktop application built using **Java Swing (GUI)** and **MySQL (Database)**, implementing a clean MVC (Model-View-Controller) architecture. The system facilitates student registration, automated sequential ID generation, unique QR code generation for each student, webcam-based continuous QR scanning for marking attendance, robust data validations, detailed attendance reporting, and data exporting to Microsoft Excel using Apache POI.

---

## 🚀 Key Features

*   **Modern Swing GUI**: Built with the **FlatLaf** theme (supporting instant Light and Dark mode switching).
*   **Webcam QR Scanner**: Uses the system webcam to continuously capture frames and decode student ID QR codes via the **Sarxos Webcam Capture** and **ZXing** libraries.
*   **Automated ID & QR Generation**: Automatically increments student IDs (e.g., `STU1001`, `STU1002`) and writes a 300x300 PNG QR Code saved locally.
*   **Report Generation**: Includes options for Today's, Weekly, Monthly, and Student-specific attendance percentage reports.
*   **Excel Export**: Exports data directly from JTables into custom, styled Excel Spreadsheets using **Apache POI**.
*   **Database Management**: Features direct database Backup and Restore options from inside the Admin Settings screen.
*   **Security**: Prevents SQL injection attacks by strictly using `PreparedStatement` parameterized JDBC queries.
*   **Forgot Password Dialog**: Verifies administrative credentials and registered email addresses to reset system passwords.

---

## 🛠 Tech Stack

*   **Language**: Java 17+ (supports Java 25 as well)
*   **GUI Framework**: Java Swing (with FlatLaf FlatDarcula & FlatIntellij themes)
*   **Database**: MySQL 8.0
*   **Database Connector**: JDBC (MySQL Connector/J)
*   **QR Libraries**: Google ZXing (Core & JavaSE)
*   **Webcam Interface**: Sarxos Webcam Capture Library
*   **Reporting Excel Engine**: Apache POI (poi & poi-ooxml)
*   **Build Tool**: Maven

---

## 📂 Project Structure

```text
QRCodeAttendanceSystem/
├── src/
│   └── main/
│       ├── java/
│       │   ├── model/                  # Data structures (POJOs)
│       │   │   ├── Admin.java
│       │   │   ├── Student.java
│       │   │   └── Attendance.java
│       │   ├── dao/                    # Database Operations (CRUD)
│       │   │   ├── AdminDAO.java
│       │   │   ├── StudentDAO.java
│       │   │   └── AttendanceDAO.java
│       │   ├── database/               # JDBC Connection Utility
│       │   │   └── DatabaseConnection.java
│       │   ├── login/                  # Login and Forgot Password Frame
│       │   │   └── LoginFrame.java
│       │   ├── dashboard/              # Navigation & Home Panels
│       │   │   ├── DashboardFrame.java
│       │   │   ├── DashboardStatsPanel.java
│       │   │   └── SettingsPanel.java
│       │   ├── student/                # Student CRUD Forms and JTable
│       │   │   └── StudentPanel.java
│       │   ├── attendance/             # Logs Viewer and Manual Entry Panel
│       │   │   └── AttendancePanel.java
│       │   ├── qrcode/                 # Webcam Scanning and Printing GUI
│       │   │   ├── QRScannerFrame.java
│       │   │   └── QRGeneratorPanel.java
│       │   ├── report/                 # Today, Weekly, Monthly Excel Panel
│       │   │   └── ReportPanel.java
│       │   └── utils/                  # Core utility classes
│       │       ├── DatabaseBackupUtility.java
│       │       ├── DateUtility.java
│       │       ├── ExcelExporter.java
│       │       ├── IDGenerator.java
│       │       ├── QRGenerator.java
│       │       ├── ThemeManager.java
│       │       └── ValidationUtility.java
│       └── resources/                  # Configuration Properties
│           └── db.properties
├── database/                           # MySQL schemas and backup directory
│   └── attendance_db.sql
├── qr_codes/                           # Generated student QR PNG images
├── images/                             # Application graphics/resources
├── pom.xml                             # Maven configuration
└── README.md                           # Documentation
```

---

## ⚙️ Installation & Setup

### 1. Database Configuration (MySQL)
1. Open your MySQL Command Line Client or any UI (like MySQL Workbench / phpMyAdmin).
2. Execute the schema script located at `database/attendance_db.sql`:
   ```sql
   source database/attendance_db.sql;
   ```
   *This automatically creates the database `attendance_db`, the three main tables (`admin`, `student`, `attendance`), and inserts the default Administrator credentials:*
   *   **Username**: `admin`
   *   **Password**: `admin123`

3. Verify connection properties inside `src/main/resources/db.properties`:
   ```properties
   db.url=jdbc:mysql://localhost:3306/attendance_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.username=root
   db.password=YOUR_MYSQL_PASSWORD_HERE
   ```

### 2. VS Code Configuration & Setup
1. Open **VS Code**.
2. Install the **Extension Pack for Java** (published by Microsoft).
3. Select **File > Open Folder...** and choose the `QRCodeAttendanceSystem` root directory (containing `pom.xml`).
4. VS Code's Java Language Server will automatically detect `pom.xml` and resolve/download all Maven dependencies.
5. In the file explorer, navigate to `src/main/java/login/LoginFrame.java`.
6. Click **Run** or **Debug** directly above the `main` method (or press `F5`).

### 3. Build & Run via Command Line (Maven)
If you prefer running using a command line shell:
```bash
# Clean previous builds and compile the application
mvn clean compile

# Run the program directly using Maven Exec Plugin
mvn exec:java -Dexec.mainClass="login.LoginFrame"

# Build a standalone shaded executable JAR (output inside target/)
mvn package
java -jar target/QRCodeAttendanceSystem-1.0-SNAPSHOT.jar
```

---

## 🖥️ Expected User Interface (UI) Description

1.  **System Login Screen**: A clean, centered card design featuring fields for Username and Password. Below the fields is a "Forgot Password?" hyperlink. Clicking this link opens a dialog requesting the Admin Username, Registered Email, and the New Password to reset the credentials.
2.  **Dashboard Home**: Highlights four top statistic blocks with top accent highlights (Total Students, Present Today, Absent Today, Attendance Rate). The center displays live dynamic JFreeChart visuals:
    *   *Left Chart*: A pie chart illustrating the ratio of present vs. absent students for the current day.
    *   *Right Chart*: A bar chart showing the count of present students broken down by department.
3.  **Student Management**: Features a split-pane layout. The left side holds a input form with dropdown lists for Department, Semester, and Section. It also displays a QR preview box. The right side contains a real-time search box and a styled JTable showing all records.
4.  **QR Generator & Print**: Displays registered students in a tabular view. Selecting any row renders the QR Code on the right side at a larger scale. Buttons allow saving the QR PNG file locally or printing it in a pre-formatted ID card template.
5.  **Webcam Scanner**: Opens a camera feed window containing a targeted scanning box overlay and an animated scanning laser. Once a student's QR is presented and successfully scanned, the camera closes, marks the student "Present" in the database, and displays a confirmation dialog.
6.  **Attendance Log / Reports**: Features a tabular report displaying dates, timestamps, names, and statuses. Allows filtering by start/end date ranges and exporting the clean grid into a formatted Excel sheet.

---

## ⚠️ Notes and Validation Rules
*   **Duplicate Attendance Prevention**: The database enforces a `UNIQUE(student_id, date)` constraint. If a student attempts to scan their QR code more than once on the same day, the scanner will sound a warning beep and display: *"Attendance already marked today!"*
*   **CRUD Input Validations**:
    *   *Email*: Enforces `user@domain.xyz` pattern.
    *   *Mobile*: Requires numbers only, between 10 to 15 digits.
    *   *Roll/Reg No*: Must be unique across all students.
