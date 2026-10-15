package com.college.admission.service;

import com.college.admission.dao.ApplicationDAO;
import com.college.admission.dao.CourseDAO;
import com.college.admission.dao.StudentDAO;
import com.college.admission.model.Application;
import com.college.admission.model.Course;
import com.college.admission.model.Student;

import java.sql.SQLException;
import java.util.*;

/**
 * Core business service orchestrating admissions, merit processing,
 * cut-off enforcement, and course allocation.
 */
public class AdmissionService {

    private final StudentDAO studentDAO;
    private final CourseDAO courseDAO;
    private final ApplicationDAO applicationDAO;

    public AdmissionService() {
        this.studentDAO = new StudentDAO();
        this.courseDAO = new CourseDAO();
        this.applicationDAO = new ApplicationDAO();
    }

    public StudentDAO getStudentDAO() {
        return studentDAO;
    }

    public CourseDAO getCourseDAO() {
        return courseDAO;
    }

    public ApplicationDAO getApplicationDAO() {
        return applicationDAO;
    }

    /**
     * Registers a new student applicant and submits their course preferences.
     */
    public int registerStudentWithPreferences(Student student, List<String> courseIds) throws SQLException {
        // Calculate merit score before saving
        double merit = MeritCalculatorService.calculateMerit(
                student.getScore10th(), student.getScore12th(), student.getEntranceScore());
        student.setMeritScore(merit);

        // Check if email already registered
        Student existing = studentDAO.getStudentByEmail(student.getEmail());
        int studentId;
        if (existing != null) {
            studentId = existing.getStudentId();
            student.setStudentId(studentId);
            // Update student scores
        } else {
            studentId = studentDAO.addStudent(student);
        }

        // Add application preferences
        int prefOrder = 1;
        for (String cId : courseIds) {
            Application app = new Application(studentId, cId, prefOrder++, "PENDING", "Applied by student");
            try {
                applicationDAO.apply(app);
            } catch (SQLException e) {
                // If duplicate preference already exists, skip or ignore
            }
        }
        return studentId;
    }

    /**
     * Runs automated seat allocation based on merit ranking and course cutoffs.
     * Logic:
     *   1. Resets available seats to course totals.
     *   2. Orders all applications by (Student Merit Score DESC, Preference Order ASC).
     *   3. A student is allocated at most 1 course (their highest qualifying preference).
     *   4. If Merit Score >= Cut-off and Seats > 0: ALLOCATED.
     *   5. If Merit Score >= Cut-off but Seats == 0: WAITLISTED.
     *   6. If Merit Score < Cut-off: REJECTED.
     */
    public AllocationResult runAutoAllocation() throws SQLException {
        // Step 1: Reset seats to total
        courseDAO.resetAllSeatsToTotal();
        Map<String, Course> courseMap = new HashMap<>();
        for (Course c : courseDAO.getAllCourses()) {
            courseMap.put(c.getCourseId().toUpperCase(), c);
        }

        // Step 2: Fetch all applications
        List<Application> allApps = applicationDAO.getAllApplications();

        Set<Integer> studentsAllocated = new HashSet<>();
        int allocatedCount = 0;
        int waitlistedCount = 0;
        int rejectedCount = 0;

        for (Application app : allApps) {
            int studentId = app.getStudentId();
            String courseId = app.getCourseId().toUpperCase();
            Course course = courseMap.get(courseId);

            if (course == null) {
                applicationDAO.updateStatus(app.getApplicationId(), "REJECTED", "Course not found");
                rejectedCount++;
                continue;
            }

            // If student has already been allocated a higher-preference course
            if (studentsAllocated.contains(studentId)) {
                applicationDAO.updateStatus(app.getApplicationId(), "CANCELLED",
                        "Already allocated to higher preference");
                continue;
            }

            double merit = app.getMeritScore();
            double cutoff = course.getCutoffScore();

            if (merit >= cutoff) {
                if (course.getAvailableSeats() > 0) {
                    // Allocate
                    course.setAvailableSeats(course.getAvailableSeats() - 1);
                    courseDAO.updateAvailableSeats(course.getCourseId(), -1);
                    applicationDAO.updateStatus(app.getApplicationId(), "ALLOCATED",
                            String.format("Allocated on Merit (Score: %.2f >= Cutoff: %.2f)", merit, cutoff));
                    studentsAllocated.add(studentId);
                    allocatedCount++;
                } else {
                    // Waitlist
                    applicationDAO.updateStatus(app.getApplicationId(), "WAITLISTED",
                            String.format("Cutoff met (%.2f >= %.2f) but seats are full", merit, cutoff));
                    waitlistedCount++;
                }
            } else {
                // Reject
                applicationDAO.updateStatus(app.getApplicationId(), "REJECTED",
                        String.format("Below Cutoff (Score: %.2f < Cutoff: %.2f)", merit, cutoff));
                rejectedCount++;
            }
        }

        return new AllocationResult(allocatedCount, waitlistedCount, rejectedCount);
    }

    /**
     * Manual administrator override for a single application.
     */
    public boolean adminDecision(int applicationId, String decision, String remarks) throws SQLException {
        Application app = applicationDAO.getApplicationById(applicationId);
        if (app == null) {
            throw new IllegalArgumentException("Application ID " + applicationId + " not found.");
        }

        String targetStatus = decision.toUpperCase().trim();
        String currentStatus = app.getStatus().toUpperCase().trim();

        // If approving/allocating, verify seat availability
        if ("APPROVED".equals(targetStatus) || "ALLOCATED".equals(targetStatus)) {
            Course course = courseDAO.getCourseById(app.getCourseId());
            if (course == null) {
                throw new IllegalStateException("Course " + app.getCourseId() + " does not exist.");
            }
            if (!("APPROVED".equals(currentStatus) || "ALLOCATED".equals(currentStatus))) {
                if (course.getAvailableSeats() <= 0) {
                    throw new IllegalStateException("Cannot approve: No available seats in " + course.getCourseName());
                }
                courseDAO.updateAvailableSeats(course.getCourseId(), -1);
            }
        } else if ("REJECTED".equals(targetStatus)) {
            // If it was previously approved/allocated, return the seat
            if ("APPROVED".equals(currentStatus) || "ALLOCATED".equals(currentStatus)) {
                courseDAO.updateAvailableSeats(app.getCourseId(), +1);
            }
        }

        return applicationDAO.updateStatus(applicationId, targetStatus, remarks);
    }

    public static class AllocationResult {
        public final int allocated;
        public final int waitlisted;
        public final int rejected;

        public AllocationResult(int allocated, int waitlisted, int rejected) {
            this.allocated = allocated;
            this.waitlisted = waitlisted;
            this.rejected = rejected;
        }

        @Override
        public String toString() {
            return String.format("Allocation Complete -> Allocated: %d, Waitlisted: %d, Below Cutoff / Rejected: %d",
                    allocated, waitlisted, rejected);
        }
    }
}
