package com.college.admission.service;

import com.college.admission.model.Application;
import com.college.admission.model.Student;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates formatted CSV and PDF reports for Admission Lists and Merit Rankings.
 */
public class ReportExporterService {

    private static final String OUTPUT_DIR = "output";

    public ReportExporterService() {
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Exports allocated admission list to CSV.
     */
    public File exportAdmissionListToCSV(List<Application> admissions) throws IOException {
        String filename = OUTPUT_DIR + File.separator + "admission_list.csv";
        File file = new File(filename);

        try (FileWriter writer = new FileWriter(file)) {
            // CSV Header
            writer.write("Application ID,Student ID,Student Name,Email,Category,Course ID,Course Name,Merit Score,Cutoff,Status,Remarks\n");

            for (Application a : admissions) {
                writer.write(String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,\"%s\",\"%s\"\n",
                        a.getApplicationId(),
                        a.getStudentId(),
                        escapeCsv(a.getStudentName()),
                        escapeCsv(a.getStudentEmail()),
                        escapeCsv(a.getStudentCategory()),
                        escapeCsv(a.getCourseId()),
                        escapeCsv(a.getCourseName()),
                        a.getMeritScore(),
                        a.getCourseCutoff(),
                        a.getStatus(),
                        escapeCsv(a.getRemarks())
                ));
            }
        }
        return file;
    }

    /**
     * Exports all applicants merit list to CSV.
     */
    public File exportMeritListToCSV(List<Student> students) throws IOException {
        String filename = OUTPUT_DIR + File.separator + "merit_list.csv";
        File file = new File(filename);

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("Rank,Student ID,Full Name,Email,Phone,Category,10th Marks,12th Marks,Entrance Score,Composite Merit Score\n");

            int rank = 1;
            for (Student s : students) {
                writer.write(String.format("%d,%d,\"%s\",\"%s\",\"%s\",\"%s\",%.2f,%.2f,%.2f,%.2f\n",
                        rank++,
                        s.getStudentId(),
                        escapeCsv(s.getFullName()),
                        escapeCsv(s.getEmail()),
                        escapeCsv(s.getPhone()),
                        escapeCsv(s.getCategory()),
                        s.getScore10th(),
                        s.getScore12th(),
                        s.getEntranceScore(),
                        s.getMeritScore()
                ));
            }
        }
        return file;
    }

    /**
     * Generates a branded, publication-grade PDF Admission List using OpenPDF.
     */
    public File exportAdmissionListToPDF(List<Application> admissions) throws DocumentException, IOException {
        String filename = OUTPUT_DIR + File.separator + "admission_list.pdf";
        File file = new File(filename);

        Document document = new Document(PageSize.A4.rotate(), 36, 36, 40, 40);
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();

        // Title and Institution Header
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(20, 50, 90));
        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.DARK_GRAY);
        Font timestampFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, Color.GRAY);

        Paragraph title = new Paragraph("COLLEGE ADMISSION MANAGEMENT SYSTEM", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph subtitle = new Paragraph("Official Course Allocation & Final Admission List", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        document.add(subtitle);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss"));
        Paragraph timestamp = new Paragraph("Generated On: " + now + " | Total Admissions: " + admissions.size(), timestampFont);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        timestamp.setSpacingAfter(15);
        document.add(timestamp);

        // Table
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 1.2f, 2.8f, 1.5f, 1.5f, 3.2f, 1.4f, 1.6f});

        // Table Header
        String[] headers = {"Rank", "App ID", "Student Name", "Category", "Course ID", "Allocated Course", "Merit %", "Status"};
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        Color headerBg = new Color(28, 70, 120);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6);
            table.addCell(cell);
        }

        // Table Data
        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font boldDataFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new Color(0, 100, 0));
        Color altBg = new Color(245, 248, 252);

        int rank = 1;
        for (Application a : admissions) {
            Color rowColor = (rank % 2 == 0) ? altBg : Color.WHITE;

            addCell(table, String.valueOf(rank++), dataFont, Element.ALIGN_CENTER, rowColor);
            addCell(table, "#" + a.getApplicationId(), dataFont, Element.ALIGN_CENTER, rowColor);
            addCell(table, a.getStudentName(), dataFont, Element.ALIGN_LEFT, rowColor);
            addCell(table, a.getStudentCategory(), dataFont, Element.ALIGN_CENTER, rowColor);
            addCell(table, a.getCourseId(), dataFont, Element.ALIGN_CENTER, rowColor);
            addCell(table, a.getCourseName(), dataFont, Element.ALIGN_LEFT, rowColor);
            addCell(table, String.format("%.2f", a.getMeritScore()), dataFont, Element.ALIGN_RIGHT, rowColor);
            addCell(table, a.getStatus(), boldDataFont, Element.ALIGN_CENTER, rowColor);
        }

        document.add(table);

        // Signatures / Dean Approval Block
        Paragraph space = new Paragraph("\n\n");
        document.add(space);

        PdfPTable signTable = new PdfPTable(2);
        signTable.setWidthPercentage(100);
        PdfPCell c1 = new PdfPCell(new Phrase("Prepared By: Admissions Office\n(Automated Merit System)", subtitleFont));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell c2 = new PdfPCell(new Phrase("Approved By: Dean of Academic Affairs\nSignature: _______________________", subtitleFont));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);

        signTable.addCell(c1);
        signTable.addCell(c2);
        document.add(signTable);

        document.close();
        return file;
    }

    private void addCell(PdfPTable table, String text, Font font, int align, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(align);
        cell.setBackgroundColor(bgColor);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }
}
