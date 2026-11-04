package FinanceDash;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

/**
 * Startup and Settings panel allowing users to configure budget categories with monthly maximum limits
 * and import transaction data from CSV files.
 */
public class StartupPanel extends JPanel {

    private final FinanceDash parentFrame;
    private JTextField categoryNameField;
    private JTextField monthlyMaxField;
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    /**
     * Constructs the startup and settings panel.
     *
     * @param parentFrame the main {@link FinanceDash} application frame
     */
    public StartupPanel(FinanceDash parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(25, 30, 25, 30));

        initComponents();
        loadCategories();
    }

    private void initComponents() {
        // --- Header Section ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("FinanceDash.FinanceDash Setup & Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel subtitleLabel = new JLabel("Configure budget categories and import initial transaction data.", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // --- Center Content Section (FinanceDash.Category Management & Table) ---
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));

        // Form to add budget category
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Add Budget FinanceDash.Category"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // FinanceDash.Category Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("FinanceDash.Category Name:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        categoryNameField = new JTextField(15);
        categoryNameField.setToolTipText("e.g. Transport, Groceries, Entertainment");
        formPanel.add(categoryNameField, gbc);

        // Monthly Max Amount
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        formPanel.add(new JLabel("Monthly Max Spend (£):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        monthlyMaxField = new JTextField(10);
        monthlyMaxField.setToolTipText("e.g. 60.00");
        formPanel.add(monthlyMaxField, gbc);

        // Add FinanceDash.Category Button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.anchor = GridBagConstraints.EAST;
        JButton addCategoryButton = new JButton("Add / Update FinanceDash.Category");
        addCategoryButton.addActionListener(e -> onAddCategory());
        formPanel.add(addCategoryButton, gbc);

        centerPanel.add(formPanel, BorderLayout.NORTH);

        // Table for displaying existing categories
        String[] columnNames = {"FinanceDash.Category Name", "Monthly Max Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        categoryTable = new JTable(tableModel);
        categoryTable.setRowHeight(24);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(categoryTable);
        tableScrollPane.setBorder(BorderFactory.createTitledBorder("Budget Categories"));

        // Delete button below table
        JPanel tableActionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteCategoryButton = new JButton("Delete Selected FinanceDash.Category");
        deleteCategoryButton.addActionListener(e -> onDeleteCategory());
        tableActionsPanel.add(deleteCategoryButton);

        JPanel tableContainer = new JPanel(new BorderLayout());
        tableContainer.add(tableScrollPane, BorderLayout.CENTER);
        tableContainer.add(tableActionsPanel, BorderLayout.SOUTH);

        centerPanel.add(tableContainer, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Section (CSV Import, Status & Continue Button) ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));

        // CSV Import Box
        JPanel csvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        csvPanel.setBorder(BorderFactory.createTitledBorder("Import Transactions"));
        JButton importCsvButton = new JButton("Import CSV File...");
        importCsvButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
        importCsvButton.addActionListener(e -> onImportCsv());
        csvPanel.add(importCsvButton);

        JLabel csvHintLabel = new JLabel("Load transaction history from a bank statement CSV into the database.");
        csvHintLabel.setForeground(Color.GRAY);
        csvPanel.add(csvHintLabel);

        bottomPanel.add(csvPanel, BorderLayout.NORTH);

        // Status & Navigation
        JPanel navPanel = new JPanel(new BorderLayout());
        statusLabel = new JLabel(" ", SwingConstants.LEFT);
        statusLabel.setForeground(new Color(0, 128, 0));
        navPanel.add(statusLabel, BorderLayout.WEST);

        JButton continueButton = new JButton("Continue to Dashboard");
        continueButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        continueButton.addActionListener(e -> onContinue());
        navPanel.add(continueButton, BorderLayout.EAST);

        bottomPanel.add(navPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Refreshes the category table by loading all categories from the database.
     */
    public void loadCategories() {
        tableModel.setRowCount(0);
        List<Category> categories = DBM.getAllCategories();
        for (Category category : categories) {
            tableModel.addRow(new Object[]{
                    category.getName(),
                    String.format("£%.2f", category.getMonthlyMaxAmount())
            });
        }
    }

    private void onAddCategory() {
        String name = categoryNameField.getText().trim();
        String maxStr = monthlyMaxField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a category name.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (maxStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a monthly maximum amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double maxAmount;
        try {
            String cleaned = maxStr.replace("£", "").replace("$", "").replace("€", "").trim();
            maxAmount = Double.parseDouble(cleaned);
            if (maxAmount < 0) {
                JOptionPane.showMessageDialog(this, "Monthly max amount must be non-negative.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number for monthly max amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Category category = new Category(name, maxAmount);
        DBM.insertCategory(category);
        loadCategories();

        categoryNameField.setText("");
        monthlyMaxField.setText("");
        statusLabel.setText("FinanceDash.Category '" + name + "' added/updated with monthly max £" + String.format("%.2f", maxAmount) + ".");
    }

    private void onDeleteCategory() {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a category from the table to delete.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String categoryName = (String) tableModel.getValueAt(selectedRow, 0);
        if (Category.isDefaultCategory(categoryName)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Default category '" + categoryName + "' cannot be deleted.",
                    "Delete FinanceDash.Category",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete category '" + categoryName + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            DBM.deleteCategory(categoryName);
            loadCategories();
            statusLabel.setText("FinanceDash.Category '" + categoryName + "' deleted.");
        }
    }

    private void onImportCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select FinanceDash.Transaction CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        // Default to current working directory
        fileChooser.setCurrentDirectory(new File("."));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            List<Transaction> transactions = DBM.readCSV(selectedFile.getAbsolutePath());
            if (transactions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No valid transactions found in selected file.", "Import CSV", JOptionPane.WARNING_MESSAGE);
            } else {
                int autoCategorizedCount = Categoriser.categorise(transactions);
                ManualCategorisationDialog.showDialog(this, transactions);

                int totalCategorizedCount = 0;
                for (Transaction t : transactions) {
                    if (t != null && t.getCategory() != null && t.getCategory().getName() != null && !t.getCategory().getName().trim().isEmpty()) {
                        totalCategorizedCount++;
                    }
                }

                DBM.insertTransactions(transactions);
                statusLabel.setText("Successfully imported " + transactions.size() + " transactions from " + selectedFile.getName() + ".");
                JOptionPane.showMessageDialog(
                        this,
                        "Successfully imported " + transactions.size() + " transactions into the database. And Categorised " + totalCategorizedCount + " transactions.",
                        "Import Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    private void onContinue() {
        DBM.setFirstLaunchCompleted(true);
        if (parentFrame != null) {
            parentFrame.showMainPanel();
        }
    }

    // Getters for testing / programmatic access
    public JTextField getCategoryNameField() {
        return categoryNameField;
    }

    public JTextField getMonthlyMaxField() {
        return monthlyMaxField;
    }

    public JTable getCategoryTable() {
        return categoryTable;
    }

    public JLabel getStatusLabel() {
        return statusLabel;
    }
}
