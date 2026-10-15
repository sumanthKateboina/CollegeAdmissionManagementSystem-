package com.college.admission.dao;

import com.college.admission.model.Course;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Academic Course operations.
 */
public class CourseDAO {

    public boolean addCourse(Course course) throws SQLException {
        String sql = "INSERT INTO courses (course_id, course_name, total_seats, available_seats, cutoff_score, duration_years) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, course.getCourseId().trim().toUpperCase());
            ps.setString(2, course.getCourseName());
            ps.setInt(3, course.getTotalSeats());
            ps.setInt(4, course.getAvailableSeats());
            ps.setDouble(5, course.getCutoffScore());
            ps.setInt(6, course.getDurationYears());
            return ps.executeUpdate() > 0;
        }
    }

    public Course getCourseById(String courseId) throws SQLException {
        String sql = "SELECT * FROM courses WHERE UPPER(course_id) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractCourse(rs);
                }
            }
        }
        return null;
    }

    public List<Course> getAllCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM courses ORDER BY course_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractCourse(rs));
            }
        }
        return list;
    }

    public boolean updateCutoff(String courseId, double newCutoff) throws SQLException {
        String sql = "UPDATE courses SET cutoff_score = ? WHERE UPPER(course_id) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newCutoff);
            ps.setString(2, courseId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateAvailableSeats(String courseId, int delta) throws SQLException {
        String sql = "UPDATE courses SET available_seats = available_seats + ? " +
                "WHERE UPPER(course_id) = UPPER(?) AND (available_seats + ?) >= 0 AND (available_seats + ?) <= total_seats";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setString(2, courseId.trim());
            ps.setInt(3, delta);
            ps.setInt(4, delta);
            return ps.executeUpdate() > 0;
        }
    }

    public void resetAllSeatsToTotal() throws SQLException {
        String sql = "UPDATE courses SET available_seats = total_seats";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public boolean deleteCourse(String courseId) throws SQLException {
        String sql = "DELETE FROM courses WHERE UPPER(course_id) = UPPER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId.trim());
            return ps.executeUpdate() > 0;
        }
    }

    private Course extractCourse(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseId(rs.getString("course_id"));
        c.setCourseName(rs.getString("course_name"));
        c.setTotalSeats(rs.getInt("total_seats"));
        c.setAvailableSeats(rs.getInt("available_seats"));
        c.setCutoffScore(rs.getDouble("cutoff_score"));
        c.setDurationYears(rs.getInt("duration_years"));
        return c;
    }
}
