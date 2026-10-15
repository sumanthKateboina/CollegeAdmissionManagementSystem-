package com.college.admission.test;

import com.college.admission.dao.DBConnection;
import com.college.admission.model.Application;
import com.college.admission.model.Course;
import com.college.admission.model.Student;
import com.college.admission.service.AdmissionService;
import com.college.admission.service.MeritCalculatorService;
import com.college.admission.service.ReportExporterService;

import java.io.File;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * Automated end-to-end test suite for College Admission Management System.
 */
public class TestAdmissionSystem {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("   RUNNING AUTOMATED TEST SUITE: COLLEGE ADMISSION SYSTEM         ");
        System.out.println("==================================================================");

        try {
            // Test 1: Database Initialization
            System.out.println("\n[TEST 1] Initializing Database Schema...");
            DBConnection.initializeDatabase();
            System.out.println("  -> Active DB: " + DBConnection.getActiveStatusMessage());
            assertNotNull(DBConnection.getActiveDbType(), "DB Type should not be null");
            System.out.println("  [PASS] Database initialized successfully.");

            AdmissionService admissionService = new AdmissionService();
            ReportExporterService reportExporter = new ReportExporterService();

            // Test 2: Merit Score Calculation
            System.out.println("\n[TEST 2] Testing Merit Calculation Logic...");
            // Formula: 20% of 90 (18) + 30% of 90 (27) + 50% of 90 (45) = 90.00
            double merit1 = MeritCalculatorService.calculateMerit(90.0, 90.0, 90.0);
            assertEquals(90.00, merit1, "Merit for 90, 90, 90 should be exactly 90.00");

            // Formula: 20% of 100 (20) + 30% of 80 (24) + 50% of 90 (45) = 89.00
            double merit2 = MeritCalculatorService.calculateMerit(100.0, 80.0, 90.0);
            assertEquals(89.00, merit2, "Merit for 100, 80, 90 should be exactly 89.00");
            System.out.println("  [PASS] Merit score calculation logic verified.");

            // Test 3: Course Listing & Cut-offs
            System.out.println("\n[TEST 3] Verifying Course Catalogue and Cut-offs...");
            List<Course> courses = admissionService.getCourseDAO().getAllCourses();
            assertTrue(courses.size() >= 5, "Should have at least 5 courses seeded");
            for (Course c : courses) {
                System.out.printf("  Course: %s - %s | Cut-off: %.2f%% | Seats: %d/%d\n",
                        c.getCourseId(), c.getCourseName(), c.getCutoffScore(), c.getAvailableSeats(), c.getTotalSeats());
            }
            System.out.println("  [PASS] Courses catalogue verified.");

            // Test 4: Student Registration and Application
            System.out.println("\n[TEST 4] Testing Student Registration & Application Flow...");
            String testEmail = "test.applicant." + System.currentTimeMillis() + "@campus.edu";
            Student student = new Student("Kavya Reddy", testEmail, "9988776655",
                    LocalDate.of(2005, 5, 20), "Female", "General",
                    95.0, 92.0, 96.0, 0.0);

            int newStudentId = admissionService.registerStudentWithPreferences(student, Arrays.asList("CS101", "DS105"));
            assertTrue(newStudentId > 0, "Student ID should be positive integer");
            System.out.println("  Registered Student ID: " + newStudentId + " with email: " + testEmail);
            System.out.printf("  Calculated Merit Score: %.2f%%\n", student.getMeritScore());
            System.out.println("  [PASS] Student registration & preferences recorded.");

            // Test 5: Automated Seat Allocation Logic
            System.out.println("\n[TEST 5] Running Automated Seat Allocation Engine...");
            AdmissionService.AllocationResult allocResult = admissionService.runAutoAllocation();
            System.out.println("  " + allocResult.toString());
            assertTrue(allocResult.allocated > 0, "Should have allocated at least 1 student");
            System.out.println("  [PASS] Cut-off based allocation verified.");

            // Test 6: Admin Manual Approval / Rejection
            System.out.println("\n[TEST 6] Testing Admin Manual Decision Override...");
            List<Application> apps = admissionService.getApplicationDAO().getAllApplications();
            assertTrue(!apps.isEmpty(), "Applications list should not be empty");
            Application firstApp = apps.get(0);
            boolean decisionOk = admissionService.adminDecision(firstApp.getApplicationId(), "APPROVED", "Manual Dean approval override");
            assertTrue(decisionOk, "Admin decision update should return true");
            System.out.println("  Updated Application #" + firstApp.getApplicationId() + " to APPROVED.");
            System.out.println("  [PASS] Admin decision workflow verified.");

            // Test 7: Export Admission List & Merit List (CSV & PDF)
            System.out.println("\n[TEST 7] Generating CSV and PDF Reports...");
            List<Application> allocated = admissionService.getApplicationDAO().getAllocatedApplications();
            List<Student> allStudents = admissionService.getStudentDAO().getAllStudents();

            File csvFile = reportExporter.exportAdmissionListToCSV(allocated);
            assertTrue(csvFile.exists() && csvFile.length() > 0, "Admission CSV file should be created and non-empty");
            System.out.println("  [CREATED] CSV Admission List: " + csvFile.getAbsolutePath() + " (" + csvFile.length() + " bytes)");

            File csvMeritFile = reportExporter.exportMeritListToCSV(allStudents);
            assertTrue(csvMeritFile.exists() && csvMeritFile.length() > 0, "Merit CSV file should be created and non-empty");
            System.out.println("  [CREATED] CSV Merit List: " + csvMeritFile.getAbsolutePath() + " (" + csvMeritFile.length() + " bytes)");

            File pdfFile = reportExporter.exportAdmissionListToPDF(allocated);
            assertTrue(pdfFile.exists() && pdfFile.length() > 0, "Admission PDF file should be created and non-empty");
            System.out.println("  [CREATED] PDF Admission List: " + pdfFile.getAbsolutePath() + " (" + pdfFile.length() + " bytes)");
            System.out.println("  [PASS] Reporting engine (CSV and PDF) verified.");

            System.out.println("\n==================================================================");
            System.out.println("       >>> ALL 7 TEST CASES PASSED SUCCESSFULLY! <<<              ");
            System.out.println("==================================================================");

        } catch (Throwable t) {
            System.err.println("\n[TEST FAILURE] An unexpected error occurred:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertEquals(double expected, double actual, String msg) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError(msg + " [Expected: " + expected + ", Actual: " + actual + "]");
        }
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) {
            throw new AssertionError(msg);
        }
    }

    private static void assertNotNull(Object obj, String msg) {
        if (obj == null) {
            throw new AssertionError(msg);
        }
    }
}
