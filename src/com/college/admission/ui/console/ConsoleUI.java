package com.college.admission.ui.console;

import com.college.admission.dao.DBConnection;
import com.college.admission.model.Application;
import com.college.admission.model.Course;
import com.college.admission.model.Student;
import com.college.admission.service.AdmissionService;
import com.college.admission.service.MeritCalculatorService;
import com.college.admission.service.ReportExporterService;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

/**
 * Interactive Console CLI for College Admission Management System.
 */
public class ConsoleUI {

    private final AdmissionService admissionService;
    private final ReportExporterService reportExporterService;
    private final Scanner scanner;

    public ConsoleUI() {
        this.admissionService = new AdmissionService();
        this.reportExporterService = new ReportExporterService();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        System.out.println("======================================================================");
        System.out.println("              COLLEGE ADMISSION MANAGEMENT SYSTEM                     ");
        System.out.println("   Tools: Java 17 | JDBC | MySQL 8.0 & SQLite Graceful Fallback       ");
        System.out.println("======================================================================");
        System.out.println("Active DB: " + DBConnection.getActiveStatusMessage());
        System.out.println();

        while (running) {
            displayMenu();
            int choice = readInt("Enter your choice (1-10): ", 1, 10);
            System.out.println();

            try {
                switch (choice) {
                    case 1:
                        registerStudentFlow();
                        break;
                    case 2:
                        viewCourses();
                        break;
                    case 3:
                        updateCutoffFlow();
                        break;
                    case 4:
                        viewMeritList();
                        break;
                    case 5:
                        viewAllApplications();
                        break;
                    case 6:
                        runAutoAllocationFlow();
                        break;
                    case 7:
                        adminDecisionFlow();
                        break;
                    case 8:
                        exportReportsFlow();
                        break;
                    case 9:
                        dbConfigurationFlow();
                        break;
                    case 10:
                        running = false;
                        System.out.println("Thank you for using College Admission Management System. Goodbye!");
                        break;
                }
            } catch (Exception e) {
                System.out.println("[Error] Operation failed: " + e.getMessage());
            }
            System.out.println();
        }
    }

    private void displayMenu() {
        System.out.println("------------------------- MAIN MENU -------------------------");
        System.out.println("1.  Register Student & Apply for Course");
        System.out.println("2.  View Courses, Available Seats & Cut-offs");
        System.out.println("3.  Update Course Cut-off Score / Add Course (Admin)");
        System.out.println("4.  View Ranked Merit List");
        System.out.println("5.  View All Applications & Status");
        System.out.println("6.  Run Automated Course Allocation (Based on Merit & Cut-offs)");
        System.out.println("7.  Admin Manual Decision (Approve / Reject Application)");
        System.out.println("8.  Export Admission List & Merit List (CSV & PDF)");
        System.out.println("9.  Database Connection Settings & Status");
        System.out.println("10. Exit");
        System.out.println("-------------------------------------------------------------");
    }

    private void registerStudentFlow() {
        System.out.println("=== STUDENT REGISTRATION & APPLICATION FORM ===");
        String name = readNonBlankString("Enter Full Name: ");
        String email = readNonBlankString("Enter Email: ");
        String phone = readNonBlankString("Enter Phone Number: ");
        String dobStr = readNonBlankString("Enter Date of Birth (YYYY-MM-DD): ");
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
        } catch (Exception e) {
            System.out.println("Invalid date format, using today's date.");
            dob = LocalDate.now();
        }

        System.out.println("Select Gender: 1. Male  2. Female  3. Other");
        int gChoice = readInt("Choice: ", 1, 3);
        String gender = (gChoice == 1) ? "Male" : (gChoice == 2) ? "Female" : "Other";

        System.out.println("Select Category: 1. General  2. OBC  3. SC  4. ST  5. EWS");
        int cChoice = readInt("Choice: ", 1, 5);
        String[] cats = {"General", "OBC", "SC", "ST", "EWS"};
        String category = cats[cChoice - 1];

        double score10th = readDouble("Enter 10th Standard Marks Percentage (0-100): ", 0, 100);
        double score12th = readDouble("Enter 12th Standard Marks Percentage (0-100): ", 0, 100);
        double entranceScore = readDouble("Enter Entrance Exam Score (0-100): ", 0, 100);

        double meritScore = MeritCalculatorService.calculateMerit(score10th, score12th, entranceScore);
        System.out.printf("[Merit Calculation] %s\n", MeritCalculatorService.getFormulaDescription());
        System.out.printf("=> Computed Composite Merit Score: %.2f%%\n\n", meritScore);

        Student student = new Student(name, email, phone, dob, gender, category, score10th, score12th, entranceScore, meritScore);

        // Select Course Preferences
        viewCourses();
        String c1 = readNonBlankString("Enter 1st Preference Course ID (e.g. CS101): ").toUpperCase();
        List<String> prefs = new ArrayList<>();
        prefs.add(c1);

        System.out.print("Do you want to add a 2nd preference? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            String c2 = readNonBlankString("Enter 2nd Preference Course ID: ").toUpperCase();
            if (!c2.equalsIgnoreCase(c1)) prefs.add(c2);
        }

        try {
            int studentId = admissionService.registerStudentWithPreferences(student, prefs);
            System.out.printf("[SUCCESS] Student registered successfully! Assigned Student ID: %d\n", studentId);
            System.out.println("Course application(s) submitted with Status: PENDING.");
        } catch (Exception e) {
            System.out.println("[Error] Registration failed: " + e.getMessage());
        }
    }

    private void viewCourses() {
        System.out.println("-------------------------------- COURSES & CUT-OFFS --------------------------------");
        System.out.printf("%-10s %-38s %-12s %-15s %-10s\n", "Course ID", "Course Name", "Total Seats", "Available Seats", "Cut-off %");
        System.out.println("------------------------------------------------------------------------------------");
        try {
            List<Course> list = admissionService.getCourseDAO().getAllCourses();
            for (Course c : list) {
                System.out.printf("%-10s %-38s %-12d %-15d %-10.2f\n",
                        c.getCourseId(), c.getCourseName(), c.getTotalSeats(), c.getAvailableSeats(), c.getCutoffScore());
            }
        } catch (Exception e) {
            System.out.println("[Error] Could not load courses: " + e.getMessage());
        }
        System.out.println("------------------------------------------------------------------------------------");
    }

    private void updateCutoffFlow() {
        System.out.println("=== ADMIN: UPDATE COURSE CUT-OFF SCORE ===");
        viewCourses();
        String courseId = readNonBlankString("Enter Course ID to update (e.g., CS101): ").toUpperCase();
        double newCutoff = readDouble("Enter New Minimum Cut-off Score (0-100): ", 0, 100);

        try {
            boolean ok = admissionService.getCourseDAO().updateCutoff(courseId, newCutoff);
            if (ok) {
                System.out.printf("[SUCCESS] Cut-off score for %s updated to %.2f%%\n", courseId, newCutoff);
            } else {
                System.out.println("[Error] Course ID not found.");
            }
        } catch (Exception e) {
            System.out.println("[Error] Update failed: " + e.getMessage());
        }
    }

    private void viewMeritList() {
        System.out.println("================================= RANKED MERIT LIST =================================");
        System.out.printf("%-6s %-8s %-22s %-10s %-8s %-8s %-10s %-12s\n",
                "Rank", "ID", "Student Name", "Category", "10th %", "12th %", "Entrance", "Merit Score");
        System.out.println("-------------------------------------------------------------------------------------");
        try {
            List<Student> students = admissionService.getStudentDAO().getAllStudents();
            int rank = 1;
            for (Student s : students) {
                System.out.printf("%-6d %-8d %-22s %-10s %-8.2f %-8.2f %-10.2f %-12.2f\n",
                        rank++, s.getStudentId(), s.getFullName(), s.getCategory(),
                        s.getScore10th(), s.getScore12th(), s.getEntranceScore(), s.getMeritScore());
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to fetch merit list: " + e.getMessage());
        }
        System.out.println("-------------------------------------------------------------------------------------");
    }

    private void viewAllApplications() {
        System.out.println("================================ ALL APPLICATIONS ================================");
        System.out.printf("%-6s %-18s %-8s %-10s %-10s %-12s %-30s\n",
                "AppID", "Student Name", "Course", "Merit %", "Cutoff %", "Status", "Remarks");
        System.out.println("----------------------------------------------------------------------------------");
        try {
            List<Application> apps = admissionService.getApplicationDAO().getAllApplications();
            for (Application a : apps) {
                System.out.printf("%-6d %-18s %-8s %-10.2f %-10.2f %-12s %-30s\n",
                        a.getApplicationId(), a.getStudentName(), a.getCourseId(),
                        a.getMeritScore(), a.getCourseCutoff(), a.getStatus(), a.getRemarks());
            }
        } catch (Exception e) {
            System.out.println("[Error] Failed to fetch applications: " + e.getMessage());
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    private void runAutoAllocationFlow() {
        System.out.println("=== AUTOMATED COURSE ALLOCATION ENGINE ===");
        System.out.println("Evaluating all pending applications based on composite merit scores and course cut-offs...");
        try {
            AdmissionService.AllocationResult res = admissionService.runAutoAllocation();
            System.out.println();
            System.out.println("****************************************************************");
            System.out.println("             COURSE ALLOCATION PROCESS COMPLETED                ");
            System.out.println("****************************************************************");
            System.out.println(res.toString());
            System.out.println();
            viewCourses();
            System.out.println();
            viewAllApplications();
        } catch (Exception e) {
            System.out.println("[Error] Auto allocation failed: " + e.getMessage());
        }
    }

    private void adminDecisionFlow() {
        System.out.println("=== ADMIN MANUAL APPROVAL / REJECTION PANEL ===");
        viewAllApplications();
        int appId = readInt("Enter Application ID to review: ", 1, 999999);

        try {
            Application app = admissionService.getApplicationDAO().getApplicationById(appId);
            if (app == null) {
                System.out.println("[Error] Application not found.");
                return;
            }

            System.out.printf("\nSelected Application #%d:\n", app.getApplicationId());
            System.out.printf("Student: %s (%s, Category: %s)\n", app.getStudentName(), app.getStudentEmail(), app.getStudentCategory());
            System.out.printf("Course Applied: %s (%s)\n", app.getCourseId(), app.getCourseName());
            System.out.printf("Student Merit Score: %.2f%% | Course Cut-off: %.2f%%\n", app.getMeritScore(), app.getCourseCutoff());
            System.out.printf("Current Status: %s\n\n", app.getStatus());

            System.out.println("Choose Decision: 1. APPROVE / ALLOCATE   2. REJECT   3. Cancel");
            int dec = readInt("Choice: ", 1, 3);
            if (dec == 3) return;

            String status = (dec == 1) ? "APPROVED" : "REJECTED";
            String remarks = readNonBlankString("Enter Decision Remarks: ");

            boolean ok = admissionService.adminDecision(appId, status, remarks);
            if (ok) {
                System.out.printf("[SUCCESS] Application #%d updated to %s!\n", appId, status);
            } else {
                System.out.println("[Error] Failed to update application.");
            }
        } catch (Exception e) {
            System.out.println("[Error] Decision failed: " + e.getMessage());
        }
    }

    private void exportReportsFlow() {
        System.out.println("=== EXPORT ADMISSION & MERIT LISTS ===");
        System.out.println("1. Export Final Admission List to CSV");
        System.out.println("2. Export Ranked Merit List to CSV");
        System.out.println("3. Export Official Admission List to PDF (Publication Ready)");
        System.out.println("4. Export All (CSV + PDF)");
        System.out.println("5. Back");

        int choice = readInt("Enter choice (1-5): ", 1, 5);
        if (choice == 5) return;

        try {
            List<Application> allocated = admissionService.getApplicationDAO().getAllocatedApplications();
            List<Student> students = admissionService.getStudentDAO().getAllStudents();

            if (choice == 1 || choice == 4) {
                File csv = reportExporterService.exportAdmissionListToCSV(allocated);
                System.out.println("[SUCCESS] CSV Admission List exported: " + csv.getAbsolutePath());
            }
            if (choice == 2 || choice == 4) {
                File csvMerit = reportExporterService.exportMeritListToCSV(students);
                System.out.println("[SUCCESS] CSV Merit List exported: " + csvMerit.getAbsolutePath());
            }
            if (choice == 3 || choice == 4) {
                File pdf = reportExporterService.exportAdmissionListToPDF(allocated);
                System.out.println("[SUCCESS] PDF Admission List exported: " + pdf.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("[Error] Export failed: " + e.getMessage());
        }
    }

    private void dbConfigurationFlow() {
        System.out.println("=== DATABASE SETTINGS & STATUS ===");
        System.out.println("Active Engine: " + DBConnection.getActiveDbType());
        System.out.println("Status: " + DBConnection.getActiveStatusMessage());
        System.out.println("Configured db.type: " + DBConnection.getProperty("db.type", "mysql"));
        System.out.println("MySQL Host: " + DBConnection.getProperty("db.mysql.host", "localhost"));
        System.out.println("MySQL Port: " + DBConnection.getProperty("db.mysql.port", "3306"));
        System.out.println("MySQL Database: " + DBConnection.getProperty("db.mysql.name", "college_admission_db"));
        System.out.println("MySQL User: " + DBConnection.getProperty("db.mysql.user", "root"));
        System.out.println();
        System.out.print("Do you want to re-initialize schema / reload tables? (y/n): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            DBConnection.initializeDatabase();
            System.out.println("[SUCCESS] Database schema reloaded.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) return val;
                System.out.printf("Please enter a number between %d and %d.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Try again.");
            }
        }
    }

    private double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                double val = Double.parseDouble(input);
                if (val >= min && val <= max) return val;
                System.out.printf("Please enter a value between %.2f and %.2f.\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid numeric format. Try again.");
            }
        }
    }

    private String readNonBlankString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("Input cannot be empty. Try again.");
        }
    }
}
