package FinanceDash;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window and entry point for the FinanceDash.FinanceDash application.
 * Extends {@link JFrame} to provide the graphical user interface.
 */
public class FinanceDash extends JFrame {

    private static final String STARTUP_PANEL_KEY = "STARTUP";
    private static final String MAIN_PANEL_KEY = "MAIN";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final StartupPanel startupPanel;
    private final MainPanel mainPanel;

    /**
     * Constructs a new {@code FinanceDash.FinanceDash} frame, sets the window title,
     * initializes the database, and loads the initial panel based on first-launch status.
     */
    public FinanceDash() {
        setTitle("FinanceDash.FinanceDash");

        DBM.initializeDatabase();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        startupPanel = new StartupPanel(this);
        mainPanel = new MainPanel(this);

        cardPanel.add(startupPanel, STARTUP_PANEL_KEY);
        cardPanel.add(mainPanel, MAIN_PANEL_KEY);

        add(cardPanel);

        if (DBM.isFirstLaunch()) {
            showStartupPanel();
        } else {
            showMainPanel();
        }
    }

    /**
     * Displays the startup / settings panel.
     */
    public void showStartupPanel() {
        startupPanel.loadCategories();
        cardLayout.show(cardPanel, STARTUP_PANEL_KEY);
    }

    /**
     * Displays the main application dashboard panel.
     */
    public void showMainPanel() {
        mainPanel.loadTransactions();
        cardLayout.show(cardPanel, MAIN_PANEL_KEY);
    }

    public StartupPanel getStartupPanel() {
        return startupPanel;
    }

    public MainPanel getMainPanel() {
        return mainPanel;
    }

    /**
     * Entry point for running the FinanceDash.FinanceDash application.
     * Initializes and displays the main application window sized to the full screen.
     */
    public static void main() {
        main(new String[0]);
    }

    /**
     * Standard entry point for running the FinanceDash.FinanceDash application.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Toolkit tk = Toolkit.getDefaultToolkit();
            FinanceDash financeDash = new FinanceDash();
            financeDash.setSize(tk.getScreenSize());
            financeDash.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            financeDash.setVisible(true);
        });
    }
}
