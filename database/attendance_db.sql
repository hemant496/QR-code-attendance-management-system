-- Drop database if exists and recreate
DROP DATABASE IF EXISTS attendance_db;
CREATE DATABASE attendance_db;
USE attendance_db;

-- 1. Admin Table (with password_hash)
CREATE TABLE admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Faculty Table
CREATE TABLE faculty (
    faculty_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Course Table
CREATE TABLE course (
    course_id VARCHAR(20) PRIMARY KEY,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Subject Table
CREATE TABLE subject (
    subject_id VARCHAR(20) PRIMARY KEY,
    subject_code VARCHAR(20) NOT NULL UNIQUE,
    subject_name VARCHAR(100) NOT NULL,
    course_id VARCHAR(20) NOT NULL,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Student Table (Enhanced with photo_path, course relation)
CREATE TABLE student (
    student_id VARCHAR(20) PRIMARY KEY,
    roll_no VARCHAR(50) NOT NULL UNIQUE,
    registration_no VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    course_id VARCHAR(20) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    section VARCHAR(10) NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    email VARCHAR(100) NOT NULL,
    qr_path VARCHAR(255) NOT NULL,
    photo_path VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (course_id) REFERENCES course(course_id) ON DELETE RESTRICT,
    INDEX idx_student_name (name),
    INDEX idx_student_roll (roll_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Attendance Table (Enhanced with subject and faculty mapping)
CREATE TABLE attendance (
    attendance_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    subject_id VARCHAR(20) NOT NULL,
    faculty_id VARCHAR(20) NOT NULL,
    date DATE NOT NULL,
    time TIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'Present', -- 'Present', 'Late', 'Absent'
    FOREIGN KEY (student_id) REFERENCES student(student_id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subject(subject_id) ON DELETE CASCADE,
    -- Can link to faculty or admin, handled via code or relaxed constraint
    UNIQUE KEY uq_student_subject_date (student_id, subject_id, date),
    INDEX idx_attendance_date (date),
    INDEX idx_attendance_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Holiday Table
CREATE TABLE holiday (
    id INT AUTO_INCREMENT PRIMARY KEY,
    holiday_date DATE NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Audit Log Table
CREATE TABLE audit_log (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    user_role VARCHAR(20) NOT NULL, -- 'Admin', 'Faculty'
    action VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Settings Table
CREATE TABLE settings (
    settings_key VARCHAR(50) PRIMARY KEY,
    settings_value VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ==================== DEFAULT SEED DATA ====================

-- Default Admin: username 'admin', password 'admin123'
-- BCrypt hash for 'admin123' is '$2a$10$lHnKZJviXnwRW1V5Wh991OpHVn23mzMpuDWZumlasqGK.Qi6iLW2i'
INSERT INTO admin (username, password_hash, name, email) 
VALUES ('admin', '$2a$10$lHnKZJviXnwRW1V5Wh991OpHVn23mzMpuDWZumlasqGK.Qi6iLW2i', 'System Administrator', 'admin@attendance.com');

-- Default Courses
INSERT INTO course (course_id, course_code, course_name, department) VALUES
('CRS1001', 'CS-BTECH', 'Bachelor of Technology in Computer Science', 'Computer Science'),
('CRS1002', 'EC-BTECH', 'Bachelor of Technology in Electronics', 'Electronics'),
('CRS1003', 'ME-BTECH', 'Bachelor of Technology in Mechanical', 'Mechanical');

-- Default Subjects
INSERT INTO subject (subject_id, subject_code, subject_name, course_id) VALUES
('SUB1001', 'CS-301', 'Database Management Systems', 'CRS1001'),
('SUB1002', 'CS-302', 'Computer Networks', 'CRS1001'),
('SUB1003', 'CS-303', 'Software Engineering', 'CRS1001'),
('SUB1004', 'EC-301', 'Digital Signal Processing', 'CRS1002'),
('SUB1005', 'ME-301', 'Thermodynamics', 'CRS1003');

-- Default Faculty: username 'faculty1', password 'faculty123'
-- BCrypt hash for 'faculty123' is '$2a$10$WU7ugc3Gr20QRLcgnWPJU.0HhrCzRZ0s5GSfA8M0pTR.bfJxuI7Zq'
INSERT INTO faculty (faculty_id, username, password_hash, name, department, email, mobile) VALUES
('FAC1001', 'faculty1', '$2a$10$WU7ugc3Gr20QRLcgnWPJU.0HhrCzRZ0s5GSfA8M0pTR.bfJxuI7Zq', 'Dr. Rajesh Sharma', 'Computer Science', 'rajesh.sharma@attendance.com', '9876543210');

-- Default Settings
INSERT INTO settings (settings_key, settings_value) VALUES
('session_timeout_minutes', '15'),
('qr_expiration_minutes', '5'),
('attendance_late_threshold_minutes', '15');
