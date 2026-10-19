package FinanceDash;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window and entry point for the FinanceDash application.
 * Extends {@link JFrame} to provide the graphical user interface.
 */
public class FinanceDash extends JFrame {

    /**
     * Constructs a new {@code FinanceDash} frame, sets the window title,
     * and initializes the database.
     */
    public FinanceDash() {
        setTitle("FinanceDash");

        DBM.initializeDatabase();
    }

    /**
     * Entry point for running the FinanceDash application.
     * Initializes and displays the main application window sized to the full screen.
     */
    public static void main() {
        Toolkit tk = Toolkit.getDefaultToolkit();

        FinanceDash financeDash = new FinanceDash();
        financeDash.setSize(tk.getScreenSize());
        financeDash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        financeDash.setVisible(true);
    }
}
