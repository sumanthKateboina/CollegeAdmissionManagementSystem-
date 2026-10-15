# College Admission Management System

A comprehensive Java & JDBC database application to manage student applications, course allocation, and merit lists.

## Features
- **Relational Database Model**: Tables for `students`, `courses`, and `applications` with foreign keys and constraints.
- **Dual Database Architecture**: Connects to **MySQL 8.0** with automatic **SQLite** fallback for zero-configuration out-of-the-box execution.
- **Student Registration**: Validated input forms with real-time composite merit score preview.
- **Merit Calculation Engine**: Composite formula combining 10th standard (20%), 12th standard (30%), and Entrance examination (50%).
- **Cut-Off & Seat Allocation**: Automated allocation engine that processes applications against cutoff thresholds and seat capacities.
- **Admin Review & Decision**: Manual approval/rejection panel with audit remarks and seat reservation management.
- **Reporting Engine**: Exports official reports to **CSV** and publication-ready **PDF** (powered by OpenPDF).
- **Dual User Interface**:
  - **Modern Swing Desktop GUI** with tabbed navigation, live calculations, and dialogs.
  - **Interactive Console CLI** with formatted ASCII tables and guided menus.

---

## Directory Structure
```
Task 6/
├── bin/                       # Compiled .class bytecode
├── lib/                       # External JAR dependencies
│   ├── mysql-connector-j-8.4.0.jar
│   ├── openpdf-1.3.39.jar
│   ├── sqlite-jdbc-3.45.1.0.jar
│   ├── slf4j-api-2.0.12.jar
│   └── slf4j-simple-2.0.12.jar
├── output/                    # Generated report files
│   ├── admission_list.csv
│   ├── merit_list.csv
│   └── admission_list.pdf
├── sql/
│   └── schema.sql             # MySQL schema & seed data
├── src/
│   └── com/college/admission/
│       ├── model/             # Student, Course, Application
│       ├── dao/               # DBConnection, StudentDAO, CourseDAO, ApplicationDAO
│       ├── service/           # MeritCalculatorService, AdmissionService, ReportExporterService
│       ├── ui/
│       │   ├── console/       # ConsoleUI.java
│       │   └── gui/           # MainGUI.java
│       ├── test/              # TestAdmissionSystem.java (E2E Test Suite)
│       └── Main.java          # Application Launcher
├── build.bat                  # Script to compile Java sources
├── run_gui.bat                # Script to launch GUI
├── run_cli.bat                # Script to launch Console CLI
├── db.properties              # Database configuration
├── USER_GUIDE.md              # Detailed user documentation
└── README.md                  # Project overview
```

---

## Quick Start

### 1. Run the Swing Desktop GUI:
```powershell
.\run_gui.bat
```
*(Or `java -cp "bin;lib/*" com.college.admission.Main --gui`)*

### 2. Run the Interactive Console CLI:
```powershell
.\run_cli.bat
```
*(Or `java -cp "bin;lib/*" com.college.admission.Main --cli`)*

### 3. Rebuild the Project:
```powershell
.\build.bat
```

### 4. Run Automated Test Suite:
```powershell
java -cp "bin;lib/*" com.college.admission.test.TestAdmissionSystem
```

---

## Deliverables Included
1. **DB Schema**: Located in [`sql/schema.sql`](file:///c:/Users/Asus/OneDrive/Documents/Desktop/Task%206/sql/schema.sql).
2. **User Guide**: Located in [`USER_GUIDE.md`](file:///c:/Users/Asus/OneDrive/Documents/Desktop/Task%206/USER_GUIDE.md).
3. **Output Files**: Generated in `output/` directory:
   - [`output/admission_list.csv`](file:///c:/Users/Asus/OneDrive/Documents/Desktop/Task%206/output/admission_list.csv)
   - [`output/merit_list.csv`](file:///c:/Users/Asus/OneDrive/Documents/Desktop/Task%206/output/merit_list.csv)
   - [`output/admission_list.pdf`](file:///c:/Users/Asus/OneDrive/Documents/Desktop/Task%206/output/admission_list.pdf)
