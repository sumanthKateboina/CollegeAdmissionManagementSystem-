package com.college.admission.model;

import java.sql.Timestamp;

/**
 * Model representing a Student Course Application in the College Admission System.
 */
public class Application {
    private int applicationId;
    private int studentId;
    private String courseId;
    private int preferenceOrder;
    private String status; // PENDING, APPROVED, REJECTED, ALLOCATED, WAITLISTED
    private String remarks;
    private Timestamp appliedAt;
    private Timestamp decisionDate;

    // Additional display fields (derived from joins)
    private String studentName;
    private String studentEmail;
    private String studentCategory;
    private double meritScore;
    private String courseName;
    private double courseCutoff;

    public Application() {}

    public Application(int studentId, String courseId, int preferenceOrder, String status, String remarks) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.preferenceOrder = preferenceOrder;
        this.status = status;
        this.remarks = remarks;
    }

    public Application(int applicationId, int studentId, String courseId, int preferenceOrder, 
                       String status, String remarks, Timestamp appliedAt, Timestamp decisionDate) {
        this(studentId, courseId, preferenceOrder, status, remarks);
        this.applicationId = applicationId;
        this.appliedAt = appliedAt;
        this.decisionDate = decisionDate;
    }

    // Getters and Setters
    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public int getPreferenceOrder() {
        return preferenceOrder;
    }

    public void setPreferenceOrder(int preferenceOrder) {
        this.preferenceOrder = preferenceOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Timestamp getDecisionDate() {
        return decisionDate;
    }

    public void setDecisionDate(Timestamp decisionDate) {
        this.decisionDate = decisionDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentCategory() {
        return studentCategory;
    }

    public void setStudentCategory(String studentCategory) {
        this.studentCategory = studentCategory;
    }

    public double getMeritScore() {
        return meritScore;
    }

    public void setMeritScore(double meritScore) {
        this.meritScore = meritScore;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public double getCourseCutoff() {
        return courseCutoff;
    }

    public void setCourseCutoff(double courseCutoff) {
        this.courseCutoff = courseCutoff;
    }

    @Override
    public String toString() {
        return String.format("App#%d [Student: %d, Course: %s, Status: %s, Score: %.2f]",
                applicationId, studentId, courseId, status, meritScore);
    }
}
