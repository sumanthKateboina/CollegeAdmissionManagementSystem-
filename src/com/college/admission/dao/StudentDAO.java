package com.college.admission.dao;

import com.college.admission.model.Student;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student operations.
 */
public class StudentDAO {

    /**
     * Adds a new student record and populates its generated student_id.
     */
    public int addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (full_name, email, phone, dob, gender, category, " +
                "score_10th, score_12th, entrance_score, merit_score) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, student.getFullName());
            ps.setString(2, student.getEmail());
            ps.setString(3, student.getPhone());
            ps.setString(4, student.getDob() != null ? student.getDob().toString() : LocalDate.now().toString());
            ps.setString(5, student.getGender());
            ps.setString(6, student.getCategory());
            ps.setDouble(7, student.getScore10th());
            ps.setDouble(8, student.getScore12th());
            ps.setDouble(9, student.getEntranceScore());
            ps.setDouble(10, student.getMeritScore());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int generatedId = rs.getInt(1);
                        student.setStudentId(generatedId);
                        return generatedId;
                    }
                }
            }
        }
        return -1;
    }

    public Student getStudentById(int studentId) throws SQLException {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudent(rs);
                }
            }
        }
        return null;
    }

    public Student getStudentByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM students WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractStudent(rs);
                }
            }
        }
        return null;
    }

    public List<Student> getAllStudents() throws SQLException {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY merit_score DESC, student_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractStudent(rs));
            }
        }
        return list;
    }

    public boolean deleteStudent(int studentId) throws SQLException {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            return ps.executeUpdate() > 0;
        }
    }

    private Student extractStudent(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setStudentId(rs.getInt("student_id"));
        s.setFullName(rs.getString("full_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));

        String dobStr = rs.getString("dob");
        if (dobStr != null && !dobStr.isEmpty()) {
            try {
                s.setDob(LocalDate.parse(dobStr.substring(0, Math.min(10, dobStr.length()))));
            } catch (Exception e) {
                s.setDob(LocalDate.now());
            }
        }

        s.setGender(rs.getString("gender"));
        s.setCategory(rs.getString("category"));
        s.setScore10th(rs.getDouble("score_10th"));
        s.setScore12th(rs.getDouble("score_12th"));
        s.setEntranceScore(rs.getDouble("entrance_score"));
        s.setMeritScore(rs.getDouble("merit_score"));
        s.setRegisteredAt(rs.getTimestamp("registered_at"));
        return s;
    }
}
