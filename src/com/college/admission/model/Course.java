package com.college.admission.model;

/**
 * Model representing an Academic Course/Branch offered by the College.
 */
public class Course {
    private String courseId;
    private String courseName;
    private int totalSeats;
    private int availableSeats;
    private double cutoffScore;
    private int durationYears;

    public Course() {}

    public Course(String courseId, String courseName, int totalSeats, int availableSeats, double cutoffScore, int durationYears) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.cutoffScore = cutoffScore;
        this.durationYears = durationYears;
    }

    public Course(String courseId, String courseName, int totalSeats, double cutoffScore, int durationYears) {
        this(courseId, courseName, totalSeats, totalSeats, cutoffScore, durationYears);
    }

    // Getters and Setters
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public double getCutoffScore() {
        return cutoffScore;
    }

    public void setCutoffScore(double cutoffScore) {
        this.cutoffScore = cutoffScore;
    }

    public int getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(int durationYears) {
        this.durationYears = durationYears;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (Cutoff: %.2f, Seats: %d/%d)", 
                courseId, courseName, cutoffScore, availableSeats, totalSeats);
    }
}
