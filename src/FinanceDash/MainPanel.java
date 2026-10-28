package FinanceDash;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Main application dashboard panel. Contains a scrollable transaction table,
 * search/filter controls, sorting controls, and a Settings button.
 */
public class MainPanel extends JPanel {

    private final FinanceDash parentFrame;
    private JButton settingsButton;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JTextField searchField;
    private JComboBox<String> sortColumnComboBox;
    private JComboBox<String> sortOrderComboBox;
    private JLabel countLabel;

    public static final String[] COLUMN_NAMES = {
            "Date", "Type / Category", "Description", "Paid Out (£)", "Paid In (£)", "Balance (£)"
    };

    private static final SimpleDateFormat[] DATE_FORMATS = new SimpleDateFormat[]{
            new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH),
            new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH),
            new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH),
            new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
    };

    /**
     * Constructs the main dashboard panel.
     *
     * @param parentFrame the main {@link FinanceDash} application frame
     */
    public MainPanel(FinanceDash parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 20, 15, 20));

        initComponents();
        loadTransactions();
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

        // --- Center Content Section (Controls & Table) ---
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Filter and Sort Control Bar
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createTitledBorder("Search & Sort Transactions"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search Bar
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        controlPanel.add(new JLabel("Search:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        searchField = new JTextField(20);
        searchField.setToolTipText("Search transactions by date, description, type, amount, etc.");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });
        controlPanel.add(searchField, gbc);

        JButton clearSearchButton = new JButton("Clear");
        clearSearchButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearSearchButton.addActionListener(e -> {
            searchField.setText("");
            applyFilter();
        });
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        controlPanel.add(clearSearchButton, gbc);

        // Sort Controls
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        controlPanel.add(new JLabel("Sort By:"), gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        sortColumnComboBox = new JComboBox<>(new String[]{"Date", "Paid In", "Paid Out", "Balance", "Description", "Type / Category"});
        sortColumnComboBox.addActionListener(e -> applySort());
        controlPanel.add(sortColumnComboBox, gbc);

        gbc.gridx = 5;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        sortOrderComboBox = new JComboBox<>(new String[]{"Increasing (Ascending)", "Decreasing (Descending)"});
        sortOrderComboBox.addActionListener(e -> applySort());
        controlPanel.add(sortOrderComboBox, gbc);

        centerPanel.add(controlPanel, BorderLayout.NORTH);

        // Table Setup
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return switch (columnIndex) {
                    case 0 -> String.class; // Date
                    case 1, 2 -> String.class;
                    case 3, 4, 5 -> Double.class; // Paid Out, Paid In, Balance
                    default -> Object.class;
                };
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(24);
        transactionTable.setFillsViewportHeight(true);
        transactionTable.setAutoCreateRowSorter(false);

        rowSorter = new TableRowSorter<>(tableModel);

        // Custom comparator for Date column (col 0)
        rowSorter.setComparator(0, new Comparator<String>() {
            @Override
            public int compare(String d1, String d2) {
                Date date1 = parseDate(d1);
                Date date2 = parseDate(d2);
                if (date1 == null && date2 == null) return 0;
                if (date1 == null) return -1;
                if (date2 == null) return 1;
                return date1.compareTo(date2);
            }
        });

        // Custom comparators for Numeric columns (col 3: Paid Out, col 4: Paid In, col 5: Balance)
        Comparator<Double> doubleComparator = Comparator.nullsFirst(Double::compareTo);
        rowSorter.setComparator(3, doubleComparator);
        rowSorter.setComparator(4, doubleComparator);
        rowSorter.setComparator(5, doubleComparator);

        transactionTable.setRowSorter(rowSorter);

        // Custom renderer for currency columns (3: Paid Out, 4: Paid In, 5: Balance)
        DefaultTableCellRenderer currencyRenderer = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number num) {
                    setText(String.format(Locale.ENGLISH, "£%.2f", num.doubleValue()));
                } else if (value == null) {
                    setText("");
                } else {
                    super.setValue(value);
                }
            }
        };
        currencyRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        transactionTable.getColumnModel().getColumn(3).setCellRenderer(currencyRenderer);
        transactionTable.getColumnModel().getColumn(4).setCellRenderer(currencyRenderer);
        transactionTable.getColumnModel().getColumn(5).setCellRenderer(currencyRenderer);

        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transactions"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Footer showing count of displayed / total transactions
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        countLabel = new JLabel("Showing 0 transactions");
        countLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerPanel.add(countLabel);
        centerPanel.add(footerPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Parses a date string against known formats.
     */
    private static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String clean = dateStr.trim();
        for (SimpleDateFormat format : DATE_FORMATS) {
            try {
                return format.parse(clean);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    /**
     * Loads transactions from the database and updates the table.
     */
    public void loadTransactions() {
        tableModel.setRowCount(0);
        List<Transaction> transactions = DBM.getAllTransactions();
        for (Transaction tx : transactions) {
            String typeOrCategory = "";
            if (tx.getCategory() != null && tx.getCategory().getName() != null && !tx.getCategory().getName().isEmpty()) {
                typeOrCategory = tx.getCategory().getName();
            } else if (tx.getType() != null) {
                typeOrCategory = tx.getType().toString();
            }

            Double paidOutVal = tx.getPaidOut() > 0 ? tx.getPaidOut() : null;
            Double paidInVal = tx.getPaidIn() > 0 ? tx.getPaidIn() : null;
            Double balanceVal = tx.getBalance();

            tableModel.addRow(new Object[]{
                    tx.getDate() != null ? tx.getDate() : "",
                    typeOrCategory,
                    tx.getDescription() != null ? tx.getDescription() : "",
                    paidOutVal,
                    paidInVal,
                    balanceVal
            });
        }
        updateCountLabel();
    }

    /**
     * Filters the table rows based on the search query.
     */
    public void applyFilter() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            rowSorter.setRowFilter(null);
        } else {
            // Case-insensitive regex filter matching any column
            try {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query)));
            } catch (Exception e) {
                rowSorter.setRowFilter(null);
            }
        }
        updateCountLabel();
    }

    /**
     * Sorts the table rows based on the selected column and direction from the UI controls.
     */
    public void applySort() {
        String columnChoice = (String) sortColumnComboBox.getSelectedItem();
        String orderChoice = (String) sortOrderComboBox.getSelectedItem();

        if (columnChoice == null || orderChoice == null) {
            return;
        }

        int columnIndex = switch (columnChoice) {
            case "Date" -> 0;
            case "Type / Category" -> 1;
            case "Description" -> 2;
            case "Paid Out" -> 3;
            case "Paid In" -> 4;
            case "Balance" -> 5;
            default -> 0;
        };

        SortOrder sortOrder = orderChoice.startsWith("Decreasing") ? SortOrder.DESCENDING : SortOrder.ASCENDING;

        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(columnIndex, sortOrder));
        rowSorter.setSortKeys(sortKeys);
        rowSorter.sort();
    }

    private void updateCountLabel() {
        int total = tableModel.getRowCount();
        int visible = transactionTable.getRowCount();
        if (visible == total) {
            countLabel.setText("Showing all " + total + " transaction" + (total == 1 ? "" : "s"));
        } else {
            countLabel.setText("Showing " + visible + " of " + total + " transaction" + (total == 1 ? "" : "s"));
        }
    }

    // --- Getters for testing and programmatic interaction ---

    public JButton getSettingsButton() {
        return settingsButton;
    }

    public JTable getTransactionTable() {
        return transactionTable;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    public TableRowSorter<DefaultTableModel> getRowSorter() {
        return rowSorter;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JComboBox<String> getSortColumnComboBox() {
        return sortColumnComboBox;
    }

    public JComboBox<String> getSortOrderComboBox() {
        return sortOrderComboBox;
    }

    public JLabel getCountLabel() {
        return countLabel;
    }
}
