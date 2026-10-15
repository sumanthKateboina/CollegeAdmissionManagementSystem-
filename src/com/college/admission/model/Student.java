package com.college.admission.model;

import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * Model representing a Student applicant in the College Admission System.
 */
public class Student {
    private int studentId;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate dob;
    private String gender;
    private String category; // General, OBC, SC, ST, EWS
    private double score10th;
    private double score12th;
    private double entranceScore;
    private double meritScore;
    private Timestamp registeredAt;

    public Student() {}

    public Student(String fullName, String email, String phone, LocalDate dob, 
                   String gender, String category, double score10th, double score12th, 
                   double entranceScore, double meritScore) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.dob = dob;
        this.gender = gender;
        this.category = category;
        this.score10th = score10th;
        this.score12th = score12th;
        this.entranceScore = entranceScore;
        this.meritScore = meritScore;
    }

    public Student(int studentId, String fullName, String email, String phone, LocalDate dob, 
                   String gender, String category, double score10th, double score12th, 
                   double entranceScore, double meritScore, Timestamp registeredAt) {
        this(fullName, email, phone, dob, gender, category, score10th, score12th, entranceScore, meritScore);
        this.studentId = studentId;
        this.registeredAt = registeredAt;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getScore10th() {
        return score10th;
    }

    public void setScore10th(double score10th) {
        this.score10th = score10th;
    }

    public double getScore12th() {
        return score12th;
    }

    public void setScore12th(double score12th) {
        this.score12th = score12th;
    }

    public double getEntranceScore() {
        return entranceScore;
    }

    public void setEntranceScore(double entranceScore) {
        this.entranceScore = entranceScore;
    }

    public double getMeritScore() {
        return meritScore;
    }

    public void setMeritScore(double meritScore) {
        this.meritScore = meritScore;
    }

    public Timestamp getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Timestamp registeredAt) {
        this.registeredAt = registeredAt;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Merit: %.2f", studentId, fullName, email, meritScore);
    }
}
