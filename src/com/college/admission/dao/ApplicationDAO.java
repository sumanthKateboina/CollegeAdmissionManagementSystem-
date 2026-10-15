package com.college.admission.dao;

import com.college.admission.model.Application;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Application operations, including joins with Students and Courses.
 */
public class ApplicationDAO {

    public int apply(Application app) throws SQLException {
        String sql = "INSERT INTO applications (student_id, course_id, preference_order, status, remarks) " +
                "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, app.getStudentId());
            ps.setString(2, app.getCourseId().trim().toUpperCase());
            ps.setInt(3, app.getPreferenceOrder());
            ps.setString(4, app.getStatus() != null ? app.getStatus() : "PENDING");
            ps.setString(5, app.getRemarks() != null ? app.getRemarks() : "Application submitted");

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        app.setApplicationId(id);
                        return id;
                    }
                }
            }
        }
        return -1;
    }

    public Application getApplicationById(int applicationId) throws SQLException {
        String sql = getBaseJoinQuery() + " WHERE a.application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractApplication(rs);
                }
            }
        }
        return null;
    }

    public List<Application> getAllApplications() throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = getBaseJoinQuery() + " ORDER BY s.merit_score DESC, a.application_id ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractApplication(rs));
            }
        }
        return list;
    }

    public List<Application> getApplicationsByCourse(String courseId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = getBaseJoinQuery() + " WHERE UPPER(a.course_id) = UPPER(?) ORDER BY s.merit_score DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, courseId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractApplication(rs));
                }
            }
        }
        return list;
    }

    public List<Application> getApplicationsByStudent(int studentId) throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = getBaseJoinQuery() + " WHERE a.student_id = ? ORDER BY a.preference_order ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(extractApplication(rs));
                }
            }
        }
        return list;
    }

    public List<Application> getAllocatedApplications() throws SQLException {
        List<Application> list = new ArrayList<>();
        String sql = getBaseJoinQuery() + " WHERE a.status IN ('APPROVED', 'ALLOCATED') ORDER BY a.course_id ASC, s.merit_score DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractApplication(rs));
            }
        }
        return list;
    }

    public boolean updateStatus(int applicationId, String status, String remarks) throws SQLException {
        String sql = "UPDATE applications SET status = ?, remarks = ?, decision_date = CURRENT_TIMESTAMP WHERE application_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.toUpperCase().trim());
            ps.setString(2, remarks);
            ps.setInt(3, applicationId);
            return ps.executeUpdate() > 0;
        }
    }

    public void resetAllApplications() throws SQLException {
        String sql = "UPDATE applications SET status = 'PENDING', remarks = 'Reset to Pending review', decision_date = NULL";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    private String getBaseJoinQuery() {
        return "SELECT a.application_id, a.student_id, a.course_id, a.preference_order, a.status, a.remarks, " +
                "a.applied_at, a.decision_date, " +
                "s.full_name AS student_name, s.email AS student_email, s.category AS student_category, s.merit_score, " +
                "c.course_name, c.cutoff_score AS course_cutoff " +
                "FROM applications a " +
                "JOIN students s ON a.student_id = s.student_id " +
                "JOIN courses c ON a.course_id = c.course_id";
    }

    private Application extractApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setApplicationId(rs.getInt("application_id"));
        app.setStudentId(rs.getInt("student_id"));
        app.setCourseId(rs.getString("course_id"));
        app.setPreferenceOrder(rs.getInt("preference_order"));
        app.setStatus(rs.getString("status"));
        app.setRemarks(rs.getString("remarks"));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        app.setDecisionDate(rs.getTimestamp("decision_date"));

        app.setStudentName(rs.getString("student_name"));
        app.setStudentEmail(rs.getString("student_email"));
        app.setStudentCategory(rs.getString("student_category"));
        app.setMeritScore(rs.getDouble("merit_score"));
        app.setCourseName(rs.getString("course_name"));
        app.setCourseCutoff(rs.getDouble("course_cutoff"));
        return app;
    }
}
