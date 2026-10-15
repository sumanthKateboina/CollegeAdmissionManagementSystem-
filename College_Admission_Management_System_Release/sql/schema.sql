-- =============================================================
-- College Admission Management System - Database Schema (MySQL)
-- =============================================================

CREATE DATABASE IF NOT EXISTS college_admission_db;
USE college_admission_db;

-- 1. Courses Table
CREATE TABLE IF NOT EXISTS courses (
    course_id VARCHAR(10) PRIMARY KEY,
    course_name VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    available_seats INT NOT NULL,
    cutoff_score DECIMAL(5, 2) NOT NULL,
    duration_years INT DEFAULT 4
);

-- 2. Students Table
CREATE TABLE IF NOT EXISTS students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(10) NOT NULL,
    category VARCHAR(20) DEFAULT 'General',
    score_10th DECIMAL(5, 2) NOT NULL,
    score_12th DECIMAL(5, 2) NOT NULL,
    entrance_score DECIMAL(5, 2) NOT NULL,
    merit_score DECIMAL(5, 2) NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Applications Table
CREATE TABLE IF NOT EXISTS applications (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    course_id VARCHAR(10) NOT NULL,
    preference_order INT NOT NULL DEFAULT 1,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED, ALLOCATED, WAITLISTED
    remarks VARCHAR(255) DEFAULT 'Application under review',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decision_date TIMESTAMP NULL,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE,
    UNIQUE KEY uq_student_course (student_id, course_id)
);

-- =============================================================
-- Initial Seed Data
-- =============================================================

-- Seed Courses
INSERT INTO courses (course_id, course_name, total_seats, available_seats, cutoff_score, duration_years) VALUES
('CS101', 'Computer Science & Engineering', 5, 5, 85.00, 4),
('IT102', 'Information Technology', 5, 5, 80.00, 4),
('EC103', 'Electronics & Communication', 4, 4, 75.00, 4),
('ME104', 'Mechanical Engineering', 3, 3, 70.00, 4),
('DS105', 'Data Science & Artificial Intelligence', 4, 4, 88.00, 4)
ON DUPLICATE KEY UPDATE course_name = VALUES(course_name);

-- Seed Sample Students
INSERT INTO students (full_name, email, phone, dob, gender, category, score_10th, score_12th, entrance_score, merit_score) VALUES
('Aarav Sharma', 'aarav.sharma@example.com', '9876543210', '2005-04-12', 'Male', 'General', 92.50, 94.00, 91.00, 92.20),
('Diya Patel', 'diya.patel@example.com', '9876543211', '2005-08-25', 'Female', 'OBC', 88.00, 89.50, 86.00, 87.45),
('Rohan Verma', 'rohan.verma@example.com', '9876543212', '2004-11-15', 'Male', 'General', 85.00, 82.00, 78.00, 80.60),
('Ananya Iyer', 'ananya.iyer@example.com', '9876543213', '2005-01-30', 'Female', 'General', 95.00, 96.00, 94.50, 95.05),
('Vikram Singh', 'vikram.singh@example.com', '9876543214', '2004-06-18', 'Male', 'SC', 76.00, 74.50, 72.00, 73.55),
('Pooja Nair', 'pooja.nair@example.com', '9876543215', '2005-09-09', 'Female', 'OBC', 82.00, 85.00, 81.00, 82.40)
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- Seed Sample Applications
INSERT INTO applications (student_id, course_id, preference_order, status, remarks) VALUES
(1, 'CS101', 1, 'PENDING', 'Applied for CSE'),
(2, 'CS101', 1, 'PENDING', 'Applied for CSE'),
(3, 'IT102', 1, 'PENDING', 'Applied for IT'),
(4, 'CS101', 1, 'PENDING', 'Applied for CSE'),
(5, 'ME104', 1, 'PENDING', 'Applied for ME'),
(6, 'EC103', 1, 'PENDING', 'Applied for ECE')
ON DUPLICATE KEY UPDATE status = VALUES(status);
