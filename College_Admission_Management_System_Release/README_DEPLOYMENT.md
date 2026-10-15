# College Admission Management System - Deployment Package

## Quick Launch:
1. **Desktop GUI Application**: Double-click `Launch_GUI.bat` (or double-click `CollegeAdmissionSystem.jar`).
2. **Terminal Console Interface**: Double-click `Launch_CLI.bat`.

## Database Setup:
- **Default Out-of-the-Box Mode**: Uses SQLite (`college_admission.db`) automatically with zero setup required.
- **MySQL Mode**: If you have MySQL 8.0 running on localhost, update your password in `db.properties` or inside the GUI under **File -> Database Connection Settings...**.
- **Database Schema**: Located in `sql/schema.sql` if you wish to run it in MySQL Workbench.

## Generated Reports:
Admissions lists and merit reports in CSV and PDF are saved in the `output/` folder.
