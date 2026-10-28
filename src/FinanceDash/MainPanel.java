package FinanceDash;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application dashboard panel. Contains a Settings button to access the startup/settings page.
 */
public class MainPanel extends JPanel {

    private final FinanceDash parentFrame;
    private JButton settingsButton;

    /**
     * Constructs the main dashboard panel.
     *
     * @param parentFrame the main {@link FinanceDash} application frame
     */
    public MainPanel(FinanceDash parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // --- Top Bar with Title and Settings Button ---
        JPanel topBar = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("FinanceDash");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        topBar.add(titleLabel, BorderLayout.WEST);

        settingsButton = new JButton("Settings");
        settingsButton.setFont(new Font("SansSerif", Font.PLAIN, 14));
        settingsButton.addActionListener(e -> {
            if (parentFrame != null) {
                parentFrame.showStartupPanel();
            }
        });
        topBar.add(settingsButton, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // --- Center Content (Main Dashboard Placeholder) ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        JLabel welcomeLabel = new JLabel("Welcome to FinanceDash");
        welcomeLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        welcomeLabel.setForeground(Color.GRAY);
        centerPanel.add(welcomeLabel);

        add(centerPanel, BorderLayout.CENTER);
    }

    public JButton getSettingsButton() {
        return settingsButton;
    }
}
