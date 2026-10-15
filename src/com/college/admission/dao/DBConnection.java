package com.college.admission.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Manages database connectivity with support for MySQL and seamless SQLite fallback.
 * Loads settings from db.properties and automatically creates schemas/tables.
 */
public class DBConnection {

    private static final String PROPERTIES_FILE = "db.properties";
    private static Properties properties = new Properties();
    private static String activeDbType = "unknown";
    private static String activeStatusMessage = "Not connected";

    static {
        loadProperties();
    }

    public static synchronized void loadProperties() {
        File file = new File(PROPERTIES_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("[DBConnection] Warning: Could not read " + PROPERTIES_FILE + ": " + e.getMessage());
            }
        } else {
            // Default properties
            properties.setProperty("db.type", "mysql");
            properties.setProperty("db.mysql.host", "localhost");
            properties.setProperty("db.mysql.port", "3306");
            properties.setProperty("db.mysql.name", "college_admission_db");
            properties.setProperty("db.mysql.user", "root");
            properties.setProperty("db.mysql.password", "123456");
            properties.setProperty("db.sqlite.file", "college_admission.db");
            saveProperties();
        }
    }

    public static synchronized void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(PROPERTIES_FILE)) {
            properties.store(fos, "College Admission Management System DB Config");
        } catch (IOException e) {
            System.err.println("[DBConnection] Warning: Could not save " + PROPERTIES_FILE + ": " + e.getMessage());
        }
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    private static boolean mysqlFailed = false;

    public static synchronized void resetConnectionState() {
        mysqlFailed = false;
    }

    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
        mysqlFailed = false;
    }

    /**
     * Obtains a live database connection, trying MySQL first (if configured),
     * and falling back gracefully to SQLite if MySQL is unreachable or denied.
     */
    public static synchronized Connection getConnection() throws SQLException {
        String dbType = properties.getProperty("db.type", "mysql").trim().toLowerCase();

        if ("mysql".equals(dbType) && !mysqlFailed) {
            try {
                Connection conn = getMySQLConnection();
                activeDbType = "MySQL";
                activeStatusMessage = "Connected to MySQL (" + properties.getProperty("db.mysql.name") + ")";
                return conn;
            } catch (SQLException e) {
                mysqlFailed = true;
                System.err.println("[DBConnection] MySQL connection failed (" + e.getMessage() + "). Falling back to SQLite...");
                try {
                    Connection sqliteConn = getSQLiteConnection();
                    activeDbType = "SQLite (Fallback)";
                    activeStatusMessage = "Using SQLite Fallback (MySQL unavailable: " + e.getMessage() + ")";
                    return sqliteConn;
                } catch (SQLException ex) {
                    activeStatusMessage = "Connection failed: " + ex.getMessage();
                    throw ex;
                }
            }
        } else {
            Connection sqliteConn = getSQLiteConnection();
            activeDbType = "mysql".equals(dbType) ? "SQLite (Fallback)" : "SQLite";
            if (!"mysql".equals(dbType)) {
                activeStatusMessage = "Connected to SQLite (" + properties.getProperty("db.sqlite.file", "college_admission.db") + ")";
            }
            return sqliteConn;
        }
    }

    private static Connection getMySQLConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found in classpath: " + e.getMessage());
        }

        String host = properties.getProperty("db.mysql.host", "localhost");
        String port = properties.getProperty("db.mysql.port", "3306");
        String dbName = properties.getProperty("db.mysql.name", "college_admission_db");
        String user = properties.getProperty("db.mysql.user", "root");
        String pass = properties.getProperty("db.mysql.password", "");

        // First attempt connecting to MySQL server directly to ensure DB exists
        String serverUrl = "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try (Connection serverConn = DriverManager.getConnection(serverUrl, user, pass);
             Statement stmt = serverConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
        } catch (SQLException e) {
            // If user doesn't have CREATE DATABASE permission or server check fails, try connecting directly to DB
        }

        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return DriverManager.getConnection(dbUrl, user, pass);
    }

    private static Connection getSQLiteConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC Driver not found in classpath: " + e.getMessage());
        }

        String dbFile = properties.getProperty("db.sqlite.file", "college_admission.db");
        String url = "jdbc:sqlite:" + dbFile;
        return DriverManager.getConnection(url);
    }

    /**
     * Initializes tables and default seed data on the active connection.
     */
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            boolean isSqlite = isSQLiteActive();

            if (isSqlite) {
                // SQLite Table definitions
                stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                        "course_id TEXT PRIMARY KEY, " +
                        "course_name TEXT NOT NULL, " +
                        "total_seats INTEGER NOT NULL, " +
                        "available_seats INTEGER NOT NULL, " +
                        "cutoff_score REAL NOT NULL, " +
                        "duration_years INTEGER DEFAULT 4)");

                stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                        "student_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "full_name TEXT NOT NULL, " +
                        "email TEXT NOT NULL UNIQUE, " +
                        "phone TEXT NOT NULL, " +
                        "dob TEXT NOT NULL, " +
                        "gender TEXT NOT NULL, " +
                        "category TEXT DEFAULT 'General', " +
                        "score_10th REAL NOT NULL, " +
                        "score_12th REAL NOT NULL, " +
                        "entrance_score REAL NOT NULL, " +
                        "merit_score REAL NOT NULL, " +
                        "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                        "application_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "student_id INTEGER NOT NULL, " +
                        "course_id TEXT NOT NULL, " +
                        "preference_order INTEGER NOT NULL DEFAULT 1, " +
                        "status TEXT DEFAULT 'PENDING', " +
                        "remarks TEXT DEFAULT 'Application under review', " +
                        "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "decision_date TIMESTAMP, " +
                        "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE, " +
                        "UNIQUE (student_id, course_id))");

                // Seed Courses if empty
                stmt.execute("INSERT OR IGNORE INTO courses (course_id, course_name, total_seats, available_seats, cutoff_score, duration_years) VALUES " +
                        "('CS101', 'Computer Science & Engineering', 5, 5, 85.00, 4), " +
                        "('IT102', 'Information Technology', 5, 5, 80.00, 4), " +
                        "('EC103', 'Electronics & Communication', 4, 4, 75.00, 4), " +
                        "('ME104', 'Mechanical Engineering', 3, 3, 70.00, 4), " +
                        "('DS105', 'Data Science & Artificial Intelligence', 4, 4, 88.00, 4)");

                // Seed sample students if empty
                stmt.execute("INSERT OR IGNORE INTO students (student_id, full_name, email, phone, dob, gender, category, score_10th, score_12th, entrance_score, merit_score) VALUES " +
                        "(1, 'Aarav Sharma', 'aarav.sharma@example.com', '9876543210', '2005-04-12', 'Male', 'General', 92.50, 94.00, 91.00, 92.20), " +
                        "(2, 'Diya Patel', 'diya.patel@example.com', '9876543211', '2005-08-25', 'Female', 'OBC', 88.00, 89.50, 86.00, 87.45), " +
                        "(3, 'Rohan Verma', 'rohan.verma@example.com', '9876543212', '2004-11-15', 'Male', 'General', 85.00, 82.00, 78.00, 80.60), " +
                        "(4, 'Ananya Iyer', 'ananya.iyer@example.com', '9876543213', '2005-01-30', 'Female', 'General', 95.00, 96.00, 94.50, 95.05), " +
                        "(5, 'Vikram Singh', 'vikram.singh@example.com', '9876543214', '2004-06-18', 'Male', 'SC', 76.00, 74.50, 72.00, 73.55), " +
                        "(6, 'Pooja Nair', 'pooja.nair@example.com', '9876543215', '2005-09-09', 'Female', 'OBC', 82.00, 85.00, 81.00, 82.40)");

                // Seed sample applications if empty
                stmt.execute("INSERT OR IGNORE INTO applications (application_id, student_id, course_id, preference_order, status, remarks) VALUES " +
                        "(1, 1, 'CS101', 1, 'PENDING', 'Applied for CSE'), " +
                        "(2, 2, 'CS101', 1, 'PENDING', 'Applied for CSE'), " +
                        "(3, 3, 'IT102', 1, 'PENDING', 'Applied for IT'), " +
                        "(4, 4, 'CS101', 1, 'PENDING', 'Applied for CSE'), " +
                        "(5, 5, 'ME104', 1, 'PENDING', 'Applied for ME'), " +
                        "(6, 6, 'EC103', 1, 'PENDING', 'Applied for ECE')");

            } else {
                // MySQL Table definitions
                stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                        "course_id VARCHAR(10) PRIMARY KEY, " +
                        "course_name VARCHAR(100) NOT NULL, " +
                        "total_seats INT NOT NULL, " +
                        "available_seats INT NOT NULL, " +
                        "cutoff_score DECIMAL(5, 2) NOT NULL, " +
                        "duration_years INT DEFAULT 4)");

                stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                        "student_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "full_name VARCHAR(100) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL UNIQUE, " +
                        "phone VARCHAR(20) NOT NULL, " +
                        "dob DATE NOT NULL, " +
                        "gender VARCHAR(10) NOT NULL, " +
                        "category VARCHAR(20) DEFAULT 'General', " +
                        "score_10th DECIMAL(5, 2) NOT NULL, " +
                        "score_12th DECIMAL(5, 2) NOT NULL, " +
                        "entrance_score DECIMAL(5, 2) NOT NULL, " +
                        "merit_score DECIMAL(5, 2) NOT NULL, " +
                        "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

                stmt.execute("CREATE TABLE IF NOT EXISTS applications (" +
                        "application_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "student_id INT NOT NULL, " +
                        "course_id VARCHAR(10) NOT NULL, " +
                        "preference_order INT NOT NULL DEFAULT 1, " +
                        "status VARCHAR(20) DEFAULT 'PENDING', " +
                        "remarks VARCHAR(255) DEFAULT 'Application under review', " +
                        "applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "decision_date TIMESTAMP NULL, " +
                        "FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (course_id) REFERENCES courses(course_id) ON DELETE CASCADE, " +
                        "UNIQUE KEY uq_student_course (student_id, course_id))");

                // Seed Courses if empty
                stmt.execute("INSERT INTO courses (course_id, course_name, total_seats, available_seats, cutoff_score, duration_years) VALUES " +
                        "('CS101', 'Computer Science & Engineering', 5, 5, 85.00, 4), " +
                        "('IT102', 'Information Technology', 5, 5, 80.00, 4), " +
                        "('EC103', 'Electronics & Communication', 4, 4, 75.00, 4), " +
                        "('ME104', 'Mechanical Engineering', 3, 3, 70.00, 4), " +
                        "('DS105', 'Data Science & Artificial Intelligence', 4, 4, 88.00, 4) " +
                        "ON DUPLICATE KEY UPDATE course_name = VALUES(course_name)");

                // Seed sample students
                stmt.execute("INSERT INTO students (student_id, full_name, email, phone, dob, gender, category, score_10th, score_12th, entrance_score, merit_score) VALUES " +
                        "(1, 'Aarav Sharma', 'aarav.sharma@example.com', '9876543210', '2005-04-12', 'Male', 'General', 92.50, 94.00, 91.00, 92.20), " +
                        "(2, 'Diya Patel', 'diya.patel@example.com', '9876543211', '2005-08-25', 'Female', 'OBC', 88.00, 89.50, 86.00, 87.45), " +
                        "(3, 'Rohan Verma', 'rohan.verma@example.com', '9876543212', '2004-11-15', 'Male', 'General', 85.00, 82.00, 78.00, 80.60), " +
                        "(4, 'Ananya Iyer', 'ananya.iyer@example.com', '9876543213', '2005-01-30', 'Female', 'General', 95.00, 96.00, 94.50, 95.05), " +
                        "(5, 'Vikram Singh', 'vikram.singh@example.com', '2004-06-18', '2004-06-18', 'Male', 'SC', 76.00, 74.50, 72.00, 73.55), " +
                        "(6, 'Pooja Nair', 'pooja.nair@example.com', '9876543215', '2005-09-09', 'Female', 'OBC', 82.00, 85.00, 81.00, 82.40) " +
                        "ON DUPLICATE KEY UPDATE email = VALUES(email)");

                // Seed sample applications
                stmt.execute("INSERT INTO applications (application_id, student_id, course_id, preference_order, status, remarks) VALUES " +
                        "(1, 1, 'CS101', 1, 'PENDING', 'Applied for CSE'), " +
                        "(2, 2, 'CS101', 1, 'PENDING', 'Applied for CSE'), " +
                        "(3, 3, 'IT102', 1, 'PENDING', 'Applied for IT'), " +
                        "(4, 4, 'CS101', 1, 'PENDING', 'Applied for CSE'), " +
                        "(5, 5, 'ME104', 1, 'PENDING', 'Applied for ME'), " +
                        "(6, 6, 'EC103', 1, 'PENDING', 'Applied for ECE') " +
                        "ON DUPLICATE KEY UPDATE status = VALUES(status)");
            }

            System.out.println("[DBConnection] Database schema initialized successfully (" + activeDbType + ")");
        } catch (SQLException e) {
            System.err.println("[DBConnection] Database initialization note: " + e.getMessage());
        }
    }

    public static String getActiveDbType() {
        return activeDbType;
    }

    public static String getActiveStatusMessage() {
        return activeStatusMessage;
    }

    public static boolean isSQLiteActive() {
        return activeDbType.toLowerCase().contains("sqlite");
    }
}
