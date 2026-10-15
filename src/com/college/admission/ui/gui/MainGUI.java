package com.college.admission.ui.gui;

import com.college.admission.dao.DBConnection;
import com.college.admission.model.Application;
import com.college.admission.model.Course;
import com.college.admission.model.Student;
import com.college.admission.service.AdmissionService;
import com.college.admission.service.MeritCalculatorService;
import com.college.admission.service.ReportExporterService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Swing Desktop GUI for College Admission Management System.
 */
public class MainGUI extends JFrame {

    private final AdmissionService admissionService;
    private final ReportExporterService reportExporterService;

    // UI Components
    private JLabel dbStatusBadge;
    private JTabbedPane tabbedPane;

    // Student Registration Form Fields
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JTextField txtPhone;
    private JTextField txtDob;
    private JComboBox<String> cmbGender;
    private JComboBox<String> cmbCategory;
    private JTextField txtScore10th;
    private JTextField txtScore12th;
    private JTextField txtEntranceScore;
    private JLabel lblComputedMerit;
    private JComboBox<String> cmbCoursePref1;
    private JComboBox<String> cmbCoursePref2;

    // Tables & Models
    private JTable tblCourses;
    private DefaultTableModel modelCourses;
    private JTable tblApplications;
    private DefaultTableModel modelApplications;
    private JTable tblAdmissions;
    private DefaultTableModel modelAdmissions;

    // Filter controls
    private JComboBox<String> cmbFilterCourse;
    private JComboBox<String> cmbFilterStatus;

    public MainGUI() {
        this.admissionService = new AdmissionService();
        this.reportExporterService = new ReportExporterService();

        initLookAndFeel();
        initWindow();
        initMenuBar();
        initHeader();
        initTabs();

        // Initial data load
        refreshAllData();
    }

    private void initLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            // Fallback to system default
        }
    }

    private void initWindow() {
        setTitle("College Admission Management System - Java & JDBC");
        setSize(1100, 750);
        setMinimumSize(new Dimension(950, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
    }

    private void initMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu menuFile = new JMenu("File");
        JMenuItem itemDbSettings = new JMenuItem("Database Connection Settings...");
        itemDbSettings.addActionListener(e -> showDatabaseSettingsDialog());
        JMenuItem itemExit = new JMenuItem("Exit");
        itemExit.addActionListener(e -> System.exit(0));
        menuFile.add(itemDbSettings);
        menuFile.addSeparator();
        menuFile.add(itemExit);

        // Actions Menu
        JMenu menuActions = new JMenu("Actions");
        JMenuItem itemAutoAlloc = new JMenuItem("Run Auto Seat Allocation");
        itemAutoAlloc.addActionListener(e -> executeAutoAllocation());
        JMenuItem itemRefresh = new JMenuItem("Refresh All Data");
        itemRefresh.addActionListener(e -> refreshAllData());
        JMenuItem itemResetSchema = new JMenuItem("Re-initialize DB Schema & Seed Data");
        itemResetSchema.addActionListener(e -> {
            int conf = JOptionPane.showConfirmDialog(this, "Are you sure you want to re-initialize tables and seed data?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (conf == JOptionPane.YES_OPTION) {
                DBConnection.initializeDatabase();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Database initialized successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menuActions.add(itemAutoAlloc);
        menuActions.add(itemRefresh);
        menuActions.addSeparator();
        menuActions.add(itemResetSchema);

        // Help Menu
        JMenu menuHelp = new JMenu("Help");
        JMenuItem itemAbout = new JMenuItem("About");
        itemAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "College Admission Management System\nVersion 1.0\nBuilt with Java 17, JDBC, MySQL & SQLite Fallback\nOpenPDF Reporting Engine",
                "About", JOptionPane.INFORMATION_MESSAGE));
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuActions);
        menuBar.add(menuHelp);
        setJMenuBar(menuBar);
    }

    private void initHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 45, 80));
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel leftTitle = new JPanel(new GridLayout(2, 1));
        leftTitle.setOpaque(false);

        JLabel lblTitle = new JLabel("COLLEGE ADMISSION MANAGEMENT SYSTEM");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblSubtitle = new JLabel("Student Applications, Cut-off Enforcement, Course Allocation & Merit Lists");
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(210, 225, 245));

        leftTitle.add(lblTitle);
        leftTitle.add(lblSubtitle);

        // DB Status Badge
        JPanel rightBadge = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightBadge.setOpaque(false);

        dbStatusBadge = new JLabel("DB: " + DBConnection.getActiveStatusMessage());
        dbStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dbStatusBadge.setForeground(Color.WHITE);
        dbStatusBadge.setBackground(new Color(40, 140, 70));
        dbStatusBadge.setOpaque(true);
        dbStatusBadge.setBorder(new EmptyBorder(5, 12, 5, 12));

        JButton btnDbSettings = new JButton("DB Config");
        btnDbSettings.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDbSettings.addActionListener(e -> showDatabaseSettingsDialog());

        rightBadge.add(dbStatusBadge);
        rightBadge.add(btnDbSettings);

        headerPanel.add(leftTitle, BorderLayout.WEST);
        headerPanel.add(rightBadge, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);
    }

    private void initTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("  1. Student Registration  ", createStudentRegistrationPanel());
        tabbedPane.addTab("  2. Courses & Cut-offs  ", createCoursesPanel());
        tabbedPane.addTab("  3. Merit List & Allocation  ", createMeritAndAllocationPanel());
        tabbedPane.addTab("  4. Admin Decision Panel  ", createAdminDecisionPanel());
        tabbedPane.addTab("  5. Reports & Exports  ", createReportsPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // =========================================================================
    // TAB 1: STUDENT REGISTRATION FORM
    // =========================================================================
    private JPanel createStudentRegistrationPanel() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBorder(new EmptyBorder(20, 25, 20, 25));

        JPanel formGrid = new JPanel(new GridLayout(1, 2, 25, 0));

        // LEFT COLUMN: Personal Details
        JPanel pnlPersonal = new JPanel(new GridBagLayout());
        pnlPersonal.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                "Personal Details", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(25, 45, 80)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        txtFullName = new JTextField();
        txtEmail = new JTextField();
        txtPhone = new JTextField();
        txtDob = new JTextField(LocalDate.now().minusYears(18).toString());
        cmbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        cmbCategory = new JComboBox<>(new String[]{"General", "OBC", "SC", "ST", "EWS"});

        addFormField(pnlPersonal, gbc, 0, "Full Name *", txtFullName);
        addFormField(pnlPersonal, gbc, 1, "Email Address *", txtEmail);
        addFormField(pnlPersonal, gbc, 2, "Phone Number *", txtPhone);
        addFormField(pnlPersonal, gbc, 3, "Date of Birth (YYYY-MM-DD) *", txtDob);
        addFormField(pnlPersonal, gbc, 4, "Gender", cmbGender);
        addFormField(pnlPersonal, gbc, 5, "Category", cmbCategory);

        // RIGHT COLUMN: Academic Scores & Preferences
        JPanel pnlAcademic = new JPanel(new GridBagLayout());
        pnlAcademic.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                "Academic Qualifications & Course Preference", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(25, 45, 80)));

        txtScore10th = new JTextField("85.00");
        txtScore12th = new JTextField("88.00");
        txtEntranceScore = new JTextField("90.00");

        lblComputedMerit = new JLabel("87.40 %");
        lblComputedMerit.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblComputedMerit.setForeground(new Color(0, 120, 50));

        DocumentListener scoreListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateLiveMerit(); }
            public void removeUpdate(DocumentEvent e) { updateLiveMerit(); }
            public void changedUpdate(DocumentEvent e) { updateLiveMerit(); }
        };
        txtScore10th.getDocument().addDocumentListener(scoreListener);
        txtScore12th.getDocument().addDocumentListener(scoreListener);
        txtEntranceScore.getDocument().addDocumentListener(scoreListener);

        cmbCoursePref1 = new JComboBox<>();
        cmbCoursePref2 = new JComboBox<>();

        addFormField(pnlAcademic, gbc, 0, "10th Standard Marks % (Weight: 20%) *", txtScore10th);
        addFormField(pnlAcademic, gbc, 1, "12th Standard Marks % (Weight: 30%) *", txtScore12th);
        addFormField(pnlAcademic, gbc, 2, "Entrance Exam Score (Weight: 50%) *", txtEntranceScore);
        addFormField(pnlAcademic, gbc, 3, "Calculated Composite Merit Score:", lblComputedMerit);
        addFormField(pnlAcademic, gbc, 4, "Course 1st Preference *", cmbCoursePref1);
        addFormField(pnlAcademic, gbc, 5, "Course 2nd Preference (Optional)", cmbCoursePref2);

        formGrid.add(pnlPersonal);
        formGrid.add(pnlAcademic);
        main.add(formGrid, BorderLayout.CENTER);

        // BUTTONS PANEL
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSubmit = new JButton("Submit Application");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSubmit.setBackground(new Color(25, 110, 200));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setPreferredSize(new Dimension(200, 42));
        btnSubmit.addActionListener(e -> handleStudentRegistration());

        JButton btnClear = new JButton("Clear Form");
        btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnClear.setPreferredSize(new Dimension(130, 42));
        btnClear.addActionListener(e -> clearRegistrationForm());

        btnPanel.add(btnSubmit);
        btnPanel.add(btnClear);
        main.add(btnPanel, BorderLayout.SOUTH);

        return main;
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridy = row * 2;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(50, 50, 50));
        panel.add(lbl, gbc);

        gbc.gridy = (row * 2) + 1;
        panel.add(comp, gbc);
    }

    private void updateLiveMerit() {
        try {
            double s10 = Double.parseDouble(txtScore10th.getText().trim());
            double s12 = Double.parseDouble(txtScore12th.getText().trim());
            double ent = Double.parseDouble(txtEntranceScore.getText().trim());
            if (s10 >= 0 && s10 <= 100 && s12 >= 0 && s12 <= 100 && ent >= 0 && ent <= 100) {
                double merit = MeritCalculatorService.calculateMerit(s10, s12, ent);
                lblComputedMerit.setText(String.format("%.2f %%", merit));
            } else {
                lblComputedMerit.setText("Invalid range (0-100)");
            }
        } catch (Exception ex) {
            lblComputedMerit.setText("Enter valid numbers");
        }
    }

    private void handleStudentRegistration() {
        String name = txtFullName.getText().trim();
        String email = txtEmail.getText().trim();
        String phone = txtPhone.getText().trim();
        String dobStr = txtDob.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || dobStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all mandatory fields (*).", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Date format. Please use YYYY-MM-DD.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double s10, s12, ent;
        try {
            s10 = Double.parseDouble(txtScore10th.getText().trim());
            s12 = Double.parseDouble(txtScore12th.getText().trim());
            ent = Double.parseDouble(txtEntranceScore.getText().trim());
            if (s10 < 0 || s10 > 100 || s12 < 0 || s12 > 100 || ent < 0 || ent > 100) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Scores must be valid numbers between 0.00 and 100.00.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double merit = MeritCalculatorService.calculateMerit(s10, s12, ent);
        Student s = new Student(name, email, phone, dob, (String) cmbGender.getSelectedItem(),
                (String) cmbCategory.getSelectedItem(), s10, s12, ent, merit);

        List<String> prefs = new ArrayList<>();
        String pref1 = extractCourseId((String) cmbCoursePref1.getSelectedItem());
        if (pref1 != null && !pref1.isEmpty()) prefs.add(pref1);

        String pref2 = extractCourseId((String) cmbCoursePref2.getSelectedItem());
        if (pref2 != null && !pref2.isEmpty() && !pref2.equals(pref1) && !pref2.equals("None")) {
            prefs.add(pref2);
        }

        if (prefs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least 1 course preference.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int studentId = admissionService.registerStudentWithPreferences(s, prefs);
            JOptionPane.showMessageDialog(this,
                    String.format("Student Registration Successful!\nStudent ID: %d\nFull Name: %s\nComposite Merit Score: %.2f%%\nApplications submitted with status 'PENDING'.",
                            studentId, name, merit),
                    "Registration Complete", JOptionPane.INFORMATION_MESSAGE);

            clearRegistrationForm();
            refreshAllData();
            tabbedPane.setSelectedIndex(2); // Jump to Merit list tab
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearRegistrationForm() {
        txtFullName.setText("");
        txtEmail.setText("");
        txtPhone.setText("");
        txtDob.setText(LocalDate.now().minusYears(18).toString());
        txtScore10th.setText("85.00");
        txtScore12th.setText("88.00");
        txtEntranceScore.setText("90.00");
        updateLiveMerit();
    }

    private String extractCourseId(String text) {
        if (text == null || text.trim().isEmpty() || text.equalsIgnoreCase("None")) return null;
        int dashIdx = text.indexOf(" - ");
        return (dashIdx != -1) ? text.substring(0, dashIdx).trim() : text.trim();
    }

    // =========================================================================
    // TAB 2: COURSES & CUT-OFFS
    // =========================================================================
    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        modelCourses = new DefaultTableModel(new String[]{"Course ID", "Course Name", "Total Seats", "Available Seats", "Cut-off Score %", "Duration (Yrs)"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCourses = new JTable(modelCourses);
        tblCourses.setRowHeight(26);
        tblCourses.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        styleTableCenter(tblCourses, new int[]{0, 2, 3, 4, 5});

        panel.add(new JScrollPane(tblCourses), BorderLayout.CENTER);

        // Action Toolbar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));

        JButton btnUpdateCutoff = new JButton("Update Cut-off Score");
        btnUpdateCutoff.addActionListener(e -> updateSelectedCourseCutoff());

        JButton btnAddCourse = new JButton("Add New Course");
        btnAddCourse.addActionListener(e -> showAddCourseDialog());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadCoursesData());

        bar.add(btnUpdateCutoff);
        bar.add(btnAddCourse);
        bar.add(btnRefresh);

        panel.add(bar, BorderLayout.SOUTH);
        return panel;
    }

    private void updateSelectedCourseCutoff() {
        int sel = tblCourses.getSelectedRow();
        if (sel == -1) {
            JOptionPane.showMessageDialog(this, "Please select a course from the table first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String courseId = (String) modelCourses.getValueAt(sel, 0);
        String courseName = (String) modelCourses.getValueAt(sel, 1);
        double currentCutoff = (double) modelCourses.getValueAt(sel, 4);

        String input = JOptionPane.showInputDialog(this,
                String.format("Enter new cut-off score for %s (%s):", courseId, courseName),
                String.valueOf(currentCutoff));
        if (input != null && !input.trim().isEmpty()) {
            try {
                double newCutoff = Double.parseDouble(input.trim());
                if (newCutoff < 0 || newCutoff > 100) throw new NumberFormatException();

                admissionService.getCourseDAO().updateCutoff(courseId, newCutoff);
                JOptionPane.showMessageDialog(this, "Cut-off score updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadCoursesData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid score. Must be between 0 and 100.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddCourseDialog() {
        JTextField cId = new JTextField();
        JTextField cName = new JTextField();
        JTextField seats = new JTextField("5");
        JTextField cutoff = new JTextField("80.00");
        JTextField duration = new JTextField("4");

        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.add(new JLabel("Course ID (e.g. AI106):")); form.add(cId);
        form.add(new JLabel("Course Name:")); form.add(cName);
        form.add(new JLabel("Total Seats:")); form.add(seats);
        form.add(new JLabel("Cut-off Score %:")); form.add(cutoff);
        form.add(new JLabel("Duration (Years):")); form.add(duration);

        int res = JOptionPane.showConfirmDialog(this, form, "Add New Academic Course", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                String id = cId.getText().trim().toUpperCase();
                String name = cName.getText().trim();
                int s = Integer.parseInt(seats.getText().trim());
                double c = Double.parseDouble(cutoff.getText().trim());
                int d = Integer.parseInt(duration.getText().trim());

                if (id.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("Fields cannot be empty");

                Course course = new Course(id, name, s, s, c, d);
                admissionService.getCourseDAO().addCourse(course);
                loadCoursesData();
                JOptionPane.showMessageDialog(this, "Course added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to add course: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // TAB 3: MERIT LIST & APPLICATION REVIEW
    // =========================================================================
    private JPanel createMeritAndAllocationPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filter Bar
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        filterBar.add(new JLabel("Filter by Course:"));
        cmbFilterCourse = new JComboBox<>(new String[]{"ALL COURSES"});
        cmbFilterCourse.addActionListener(e -> filterApplications());
        filterBar.add(cmbFilterCourse);

        filterBar.add(new JLabel("Filter by Status:"));
        cmbFilterStatus = new JComboBox<>(new String[]{"ALL STATUSES", "PENDING", "ALLOCATED", "APPROVED", "WAITLISTED", "REJECTED"});
        cmbFilterStatus.addActionListener(e -> filterApplications());
        filterBar.add(cmbFilterStatus);

        panel.add(filterBar, BorderLayout.NORTH);

        // Applications Table
        modelApplications = new DefaultTableModel(new String[]{
                "App ID", "Student ID", "Student Name", "Category", "Merit %", "Course ID", "Course Name", "Cutoff %", "Status", "Remarks"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblApplications = new JTable(modelApplications);
        tblApplications.setRowHeight(25);
        tblApplications.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        styleTableCenter(tblApplications, new int[]{0, 1, 3, 4, 5, 7, 8});

        panel.add(new JScrollPane(tblApplications), BorderLayout.CENTER);

        // Allocation Action Bar
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnAutoAlloc = new JButton("Run Automated Seat Allocation");
        btnAutoAlloc.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAutoAlloc.setBackground(new Color(30, 130, 60));
        btnAutoAlloc.setForeground(Color.WHITE);
        btnAutoAlloc.addActionListener(e -> executeAutoAllocation());

        JButton btnReset = new JButton("Reset Allocations to Pending");
        btnReset.addActionListener(e -> resetAllocations());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.addActionListener(e -> loadApplicationsData());

        actions.add(btnReset);
        actions.add(btnRefresh);
        actions.add(btnAutoAlloc);

        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private void executeAutoAllocation() {
        int conf = JOptionPane.showConfirmDialog(this,
                "Run automated course allocation algorithm?\nThis will process all applications against course cut-offs and seat quotas.",
                "Confirm Auto Allocation", JOptionPane.YES_NO_OPTION);
        if (conf != JOptionPane.YES_OPTION) return;

        try {
            AdmissionService.AllocationResult res = admissionService.runAutoAllocation();
            refreshAllData();
            JOptionPane.showMessageDialog(this,
                    "Automated Allocation Completed!\n\n" +
                            "• Allocated Seats: " + res.allocated + "\n" +
                            "• Waitlisted (Seats Full): " + res.waitlisted + "\n" +
                            "• Rejected (Below Cut-off): " + res.rejected,
                    "Allocation Result", JOptionPane.INFORMATION_MESSAGE);
            tabbedPane.setSelectedIndex(4); // Jump to reports tab
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Allocation failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetAllocations() {
        int conf = JOptionPane.showConfirmDialog(this,
                "Reset all applications back to PENDING and restore seats?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            try {
                admissionService.getCourseDAO().resetAllSeatsToTotal();
                admissionService.getApplicationDAO().resetAllApplications();
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Applications reset to PENDING.", "Reset", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Reset failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // =========================================================================
    // TAB 4: ADMIN DECISION PANEL
    // =========================================================================
    private JPanel createAdminDecisionPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel infoCard = new JPanel(new GridLayout(6, 2, 10, 10));
        infoCard.setBorder(BorderFactory.createTitledBorder("Selected Application Review"));

        JLabel lblAppId = new JLabel("-");
        JLabel lblStudent = new JLabel("-");
        JLabel lblCourse = new JLabel("-");
        JLabel lblMeritVsCutoff = new JLabel("-");
        JLabel lblCurrentStatus = new JLabel("-");
        JTextField txtRemarks = new JTextField("Admin review");

        infoCard.add(new JLabel("Application ID:")); infoCard.add(lblAppId);
        infoCard.add(new JLabel("Student:")); infoCard.add(lblStudent);
        infoCard.add(new JLabel("Course Applied:")); infoCard.add(lblCourse);
        infoCard.add(new JLabel("Merit vs Cut-off:")); infoCard.add(lblMeritVsCutoff);
        infoCard.add(new JLabel("Current Status:")); infoCard.add(lblCurrentStatus);
        infoCard.add(new JLabel("Admin Decision Remarks:")); infoCard.add(txtRemarks);

        panel.add(infoCard, BorderLayout.NORTH);

        // Center table showing all applications for easy selection
        JTable tblSelect = new JTable(modelApplications);
        tblSelect.setRowHeight(24);
        tblSelect.getSelectionModel().addListSelectionListener(e -> {
            int row = tblSelect.getSelectedRow();
            if (row != -1) {
                lblAppId.setText(modelApplications.getValueAt(row, 0).toString());
                lblStudent.setText(modelApplications.getValueAt(row, 2) + " (" + modelApplications.getValueAt(row, 3) + ")");
                lblCourse.setText(modelApplications.getValueAt(row, 5) + " - " + modelApplications.getValueAt(row, 6));
                lblMeritVsCutoff.setText("Student Merit: " + modelApplications.getValueAt(row, 4) + "% | Cut-off: " + modelApplications.getValueAt(row, 7) + "%");
                lblCurrentStatus.setText(modelApplications.getValueAt(row, 8).toString());
                txtRemarks.setText(modelApplications.getValueAt(row, 9).toString());
            }
        });

        panel.add(new JScrollPane(tblSelect), BorderLayout.CENTER);

        // Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnApprove = new JButton("Approve & Allocate Seat");
        btnApprove.setBackground(new Color(30, 140, 60));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.addActionListener(e -> {
            int row = tblSelect.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an application first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int appId = (int) modelApplications.getValueAt(row, 0);
            try {
                admissionService.adminDecision(appId, "APPROVED", txtRemarks.getText().trim());
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Application #" + appId + " Approved & Seat Allocated!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Action failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnReject = new JButton("Reject Application");
        btnReject.setBackground(new Color(190, 40, 40));
        btnReject.setForeground(Color.WHITE);
        btnReject.addActionListener(e -> {
            int row = tblSelect.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an application first.", "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int appId = (int) modelApplications.getValueAt(row, 0);
            try {
                admissionService.adminDecision(appId, "REJECTED", txtRemarks.getText().trim());
                refreshAllData();
                JOptionPane.showMessageDialog(this, "Application #" + appId + " Marked as REJECTED.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Action failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnBar.add(btnApprove);
        btnBar.add(btnReject);
        panel.add(btnBar, BorderLayout.SOUTH);

        return panel;
    }

    // =========================================================================
    // TAB 5: REPORTS & EXPORTS
    // =========================================================================
    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Official Allocated Admission List (Ready for Export)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        panel.add(title, BorderLayout.NORTH);

        modelAdmissions = new DefaultTableModel(new String[]{
                "Rank", "App ID", "Student Name", "Category", "Course ID", "Course Name", "Merit Score %", "Status"
        }, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAdmissions = new JTable(modelAdmissions);
        tblAdmissions.setRowHeight(25);
        tblAdmissions.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        styleTableCenter(tblAdmissions, new int[]{0, 1, 3, 4, 6, 7});

        panel.add(new JScrollPane(tblAdmissions), BorderLayout.CENTER);

        // Export Toolbar
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));

        JButton btnExportCsv = new JButton("Export Admission List (CSV)");
        btnExportCsv.addActionListener(e -> {
            try {
                List<Application> list = admissionService.getApplicationDAO().getAllocatedApplications();
                File f = reportExporterService.exportAdmissionListToCSV(list);
                JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + f.getAbsolutePath(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "CSV Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnExportMeritCsv = new JButton("Export Merit List (CSV)");
        btnExportMeritCsv.addActionListener(e -> {
            try {
                List<Student> list = admissionService.getStudentDAO().getAllStudents();
                File f = reportExporterService.exportMeritListToCSV(list);
                JOptionPane.showMessageDialog(this, "Exported successfully to:\n" + f.getAbsolutePath(), "CSV Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "CSV Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnExportPdf = new JButton("Export Publication PDF");
        btnExportPdf.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExportPdf.setBackground(new Color(20, 60, 120));
        btnExportPdf.setForeground(Color.WHITE);
        btnExportPdf.addActionListener(e -> {
            try {
                List<Application> list = admissionService.getApplicationDAO().getAllocatedApplications();
                File f = reportExporterService.exportAdmissionListToPDF(list);
                JOptionPane.showMessageDialog(this, "Official PDF Admission List generated:\n" + f.getAbsolutePath(), "PDF Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "PDF Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnOpenFolder = new JButton("Open Output Folder");
        btnOpenFolder.addActionListener(e -> {
            try {
                Desktop.getDesktop().open(new File("output"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Could not open folder: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        bar.add(btnExportCsv);
        bar.add(btnExportMeritCsv);
        bar.add(btnExportPdf);
        bar.add(btnOpenFolder);

        panel.add(bar, BorderLayout.SOUTH);
        return panel;
    }

    // =========================================================================
    // DATA REFRESHING & LOGIC
    // =========================================================================
    public void refreshAllData() {
        dbStatusBadge.setText("DB: " + DBConnection.getActiveStatusMessage());
        loadCoursesData();
        loadApplicationsData();
        loadAdmissionsData();
        loadCourseDropdowns();
    }

    private void loadCoursesData() {
        modelCourses.setRowCount(0);
        try {
            List<Course> list = admissionService.getCourseDAO().getAllCourses();
            for (Course c : list) {
                modelCourses.addRow(new Object[]{
                        c.getCourseId(), c.getCourseName(), c.getTotalSeats(), c.getAvailableSeats(), c.getCutoffScore(), c.getDurationYears()
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading courses: " + e.getMessage());
        }
    }

    private void loadCourseDropdowns() {
        try {
            List<Course> list = admissionService.getCourseDAO().getAllCourses();
            cmbCoursePref1.removeAllItems();
            cmbCoursePref2.removeAllItems();
            cmbFilterCourse.removeAllItems();

            cmbCoursePref2.addItem("None");
            cmbFilterCourse.addItem("ALL COURSES");

            for (Course c : list) {
                String item = c.getCourseId() + " - " + c.getCourseName() + " (Cutoff: " + c.getCutoffScore() + "%)";
                cmbCoursePref1.addItem(item);
                cmbCoursePref2.addItem(item);
                cmbFilterCourse.addItem(c.getCourseId());
            }
        } catch (Exception e) {
            System.err.println("Error loading course dropdowns: " + e.getMessage());
        }
    }

    private void loadApplicationsData() {
        modelApplications.setRowCount(0);
        try {
            List<Application> list = admissionService.getApplicationDAO().getAllApplications();
            for (Application a : list) {
                modelApplications.addRow(new Object[]{
                        a.getApplicationId(), a.getStudentId(), a.getStudentName(), a.getStudentCategory(),
                        a.getMeritScore(), a.getCourseId(), a.getCourseName(), a.getCourseCutoff(),
                        a.getStatus(), a.getRemarks()
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading applications: " + e.getMessage());
        }
    }

    private void filterApplications() {
        String selCourse = (String) cmbFilterCourse.getSelectedItem();
        String selStatus = (String) cmbFilterStatus.getSelectedItem();

        modelApplications.setRowCount(0);
        try {
            List<Application> list = admissionService.getApplicationDAO().getAllApplications();
            for (Application a : list) {
                boolean matchCourse = (selCourse == null || selCourse.equals("ALL COURSES") || a.getCourseId().equalsIgnoreCase(selCourse));
                boolean matchStatus = (selStatus == null || selStatus.equals("ALL STATUSES") || a.getStatus().equalsIgnoreCase(selStatus));

                if (matchCourse && matchStatus) {
                    modelApplications.addRow(new Object[]{
                            a.getApplicationId(), a.getStudentId(), a.getStudentName(), a.getStudentCategory(),
                            a.getMeritScore(), a.getCourseId(), a.getCourseName(), a.getCourseCutoff(),
                            a.getStatus(), a.getRemarks()
                    });
                }
            }
        } catch (Exception e) {
            System.err.println("Error filtering applications: " + e.getMessage());
        }
    }

    private void loadAdmissionsData() {
        modelAdmissions.setRowCount(0);
        try {
            List<Application> list = admissionService.getApplicationDAO().getAllocatedApplications();
            int rank = 1;
            for (Application a : list) {
                modelAdmissions.addRow(new Object[]{
                        rank++, a.getApplicationId(), a.getStudentName(), a.getStudentCategory(),
                        a.getCourseId(), a.getCourseName(), a.getMeritScore(), a.getStatus()
                });
            }
        } catch (Exception e) {
            System.err.println("Error loading admissions: " + e.getMessage());
        }
    }

    private void showDatabaseSettingsDialog() {
        JTextField host = new JTextField(DBConnection.getProperty("db.mysql.host", "localhost"));
        JTextField port = new JTextField(DBConnection.getProperty("db.mysql.port", "3306"));
        JTextField dbName = new JTextField(DBConnection.getProperty("db.mysql.name", "college_admission_db"));
        JTextField user = new JTextField(DBConnection.getProperty("db.mysql.user", "root"));
        JPasswordField pass = new JPasswordField(DBConnection.getProperty("db.mysql.password", "123456"));
        JComboBox<String> dbType = new JComboBox<>(new String[]{"mysql", "sqlite"});
        dbType.setSelectedItem(DBConnection.getProperty("db.type", "mysql"));

        JPanel form = new JPanel(new GridLayout(6, 2, 8, 8));
        form.add(new JLabel("Database Engine:")); form.add(dbType);
        form.add(new JLabel("MySQL Host:")); form.add(host);
        form.add(new JLabel("MySQL Port:")); form.add(port);
        form.add(new JLabel("MySQL Database:")); form.add(dbName);
        form.add(new JLabel("MySQL Username:")); form.add(user);
        form.add(new JLabel("MySQL Password:")); form.add(pass);

        int res = JOptionPane.showConfirmDialog(this, form, "Database Connection Settings", JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            DBConnection.setProperty("db.type", (String) dbType.getSelectedItem());
            DBConnection.setProperty("db.mysql.host", host.getText().trim());
            DBConnection.setProperty("db.mysql.port", port.getText().trim());
            DBConnection.setProperty("db.mysql.name", dbName.getText().trim());
            DBConnection.setProperty("db.mysql.user", user.getText().trim());
            DBConnection.setProperty("db.mysql.password", new String(pass.getPassword()));
            DBConnection.saveProperties();

            DBConnection.initializeDatabase();
            refreshAllData();
            JOptionPane.showMessageDialog(this, "Settings updated!\nCurrent Status: " + DBConnection.getActiveStatusMessage(),
                    "Database Configured", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void styleTableCenter(JTable table, int[] centerCols) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int c : centerCols) {
            if (c < table.getColumnCount()) {
                table.getColumnModel().getColumn(c).setCellRenderer(center);
            }
        }
    }
}
