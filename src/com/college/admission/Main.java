package com.college.admission;

import com.college.admission.dao.DBConnection;
import com.college.admission.ui.console.ConsoleUI;
import com.college.admission.ui.gui.MainGUI;

import javax.swing.*;
import java.awt.*;

/**
 * Main Application Entry Point for the College Admission Management System.
 * Supports both modern Swing Desktop GUI and interactive Console CLI.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("            COLLEGE ADMISSION MANAGEMENT SYSTEM                   ");
        System.out.println("==================================================================");

        // 1. Initialize Database Schema & Seed Data
        DBConnection.initializeDatabase();

        // 2. Determine Launch Mode (GUI vs Console)
        boolean forceCli = false;
        boolean forceGui = false;

        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                forceCli = true;
            } else if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                forceGui = true;
            }
        }

        if (forceCli || GraphicsEnvironment.isHeadless()) {
            launchConsole();
        } else if (forceGui) {
            launchGUI();
        } else {
            // Default to GUI on desktop systems with a console prompt fallback
            System.out.println("Starting Desktop GUI Application...");
            System.out.println("Tip: Run with argument '--cli' to start in interactive console mode.");
            launchGUI();
        }
    }

    private static void launchGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                MainGUI gui = new MainGUI();
                gui.setVisible(true);
            } catch (Exception e) {
                System.err.println("Failed to launch GUI: " + e.getMessage());
                System.out.println("Falling back to Console CLI...");
                launchConsole();
            }
        });
    }

    private static void launchConsole() {
        ConsoleUI consoleUI = new ConsoleUI();
        consoleUI.start();
    }
}
