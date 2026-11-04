package FinanceDash;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A modal popup dialog that allows users to manually categorise un-categorised transactions
 * by clicking buttons representing each available budget category.
 */
public class ManualCategorisationDialog extends JDialog {

    private final List<Transaction> allTransactions;
    private final List<Transaction> uncategorisedQueue = new ArrayList<>();
    private final List<Category> categories;
    private int currentIndex = 0;
    private int categorisedCount = 0;

    private JLabel titleLabel;
    private JLabel progressLabel;
    private JLabel dateValueLabel;
    private JLabel descValueLabel;
    private JLabel amountValueLabel;
    private JLabel balanceValueLabel;
    private JPanel categoryButtonsPanel;
    private final List<JButton> categoryButtons = new ArrayList<>();
    private JButton skipButton;
    private JButton skipAllButton;

    /**
     * Displays the manual categorisation popup dialog for any uncategorised transactions in the list.
     *
     * @param parent       the parent component (frame or dialog)
     * @param transactions the list of transactions to process
     * @return the number of transactions manually categorised during this session
     */
    public static int showDialog(Component parent, List<Transaction> transactions) {
        List<Category> allCats = DBM.getAllCategories();
        if (allCats == null || allCats.isEmpty()) {
            allCats = Category.DEFAULT_CATEGORIES;
        }
        return showDialog(parent, transactions, allCats);
    }

    /**
     * Displays the manual categorisation popup dialog for uncategorised transactions with a specific category list.
     *
     * @param parent       the parent component
     * @param transactions the list of transactions
     * @param categories   the available categories
     * @return the number of transactions manually categorised
     */
    public static int showDialog(Component parent, List<Transaction> transactions, List<Category> categories) {
        if (transactions == null || transactions.isEmpty() || GraphicsEnvironment.isHeadless()) {
            return 0;
        }

        List<Transaction> uncategorised = new ArrayList<>();
        for (Transaction t : transactions) {
            if (t != null && (t.getCategory() == null || t.getCategory().getName() == null || t.getCategory().getName().trim().isEmpty())) {
                uncategorised.add(t);
            }
        }

        if (uncategorised.isEmpty()) {
            return 0;
        }

        Window owner = null;
        if (parent instanceof Window w) {
            owner = w;
        } else if (parent != null) {
            owner = SwingUtilities.getWindowAncestor(parent);
        }

        ManualCategorisationDialog dialog = new ManualCategorisationDialog(owner, transactions, uncategorised, categories);
        dialog.setVisible(true);
        return dialog.getCategorisedCount();
    }

    public ManualCategorisationDialog(Window owner, List<Transaction> allTransactions, List<Transaction> uncategorised, List<Category> categories) {
        super(owner, "Manual Categorisation", ModalityType.APPLICATION_MODAL);
        this.allTransactions = allTransactions;
        this.uncategorisedQueue.addAll(uncategorised);
        this.categories = (categories != null && !categories.isEmpty()) ? categories : Category.DEFAULT_CATEGORIES;

        initComponents();
        loadCurrentTransaction();

        setSize(520, 380);
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel) getContentPane()).setBorder(new EmptyBorder(15, 20, 15, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        titleLabel = new JLabel("Categorise Transaction", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        progressLabel = new JLabel("Transaction 1 of " + uncategorisedQueue.size(), SwingConstants.CENTER);
        progressLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        progressLabel.setForeground(Color.DARK_GRAY);

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(progressLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Center Panel (Transaction details card)
        JPanel detailsPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Transaction Details"),
                new EmptyBorder(8, 12, 8, 12)
        ));

        detailsPanel.add(new JLabel("Date:"));
        dateValueLabel = new JLabel("-");
        dateValueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        detailsPanel.add(dateValueLabel);

        detailsPanel.add(new JLabel("Description:"));
        descValueLabel = new JLabel("-");
        descValueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        detailsPanel.add(descValueLabel);

        detailsPanel.add(new JLabel("Amount:"));
        amountValueLabel = new JLabel("-");
        amountValueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        detailsPanel.add(amountValueLabel);

        detailsPanel.add(new JLabel("Balance:"));
        balanceValueLabel = new JLabel("-");
        balanceValueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detailsPanel.add(balanceValueLabel);

        add(detailsPanel, BorderLayout.CENTER);

        // Bottom Panel (Buttons: Categories + Actions)
        JPanel bottomContainer = new JPanel(new BorderLayout(10, 10));

        JLabel promptLabel = new JLabel("Assign a Category (its description will be remembered):", SwingConstants.CENTER);
        promptLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bottomContainer.add(promptLabel, BorderLayout.NORTH);

        // Category Buttons Panel
        categoryButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        categoryButtons.clear();
        for (Category cat : categories) {
            JButton catBtn = new JButton(cat.getName());
            catBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            catBtn.setFocusPainted(false);
            catBtn.addActionListener(e -> onCategorySelected(cat));
            categoryButtons.add(catBtn);
            categoryButtonsPanel.add(catBtn);
        }
        bottomContainer.add(categoryButtonsPanel, BorderLayout.CENTER);

        // Navigation Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        skipButton = new JButton("Skip");
        skipButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        skipButton.addActionListener(e -> onSkip());

        skipAllButton = new JButton("Skip All / Finish");
        skipAllButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        skipAllButton.addActionListener(e -> onSkipAll());

        actionsPanel.add(skipButton);
        actionsPanel.add(skipAllButton);
        bottomContainer.add(actionsPanel, BorderLayout.SOUTH);

        add(bottomContainer, BorderLayout.SOUTH);
    }

    private void loadCurrentTransaction() {
        // Skip any that might have been automatically categorised by newly added keywords
        while (currentIndex < uncategorisedQueue.size()) {
            Transaction tx = uncategorisedQueue.get(currentIndex);
            if (tx.getCategory() != null && tx.getCategory().getName() != null && !tx.getCategory().getName().trim().isEmpty()) {
                currentIndex++;
            } else {
                break;
            }
        }

        if (currentIndex >= uncategorisedQueue.size()) {
            dispose();
            return;
        }

        Transaction tx = uncategorisedQueue.get(currentIndex);
        progressLabel.setText("Transaction " + (currentIndex + 1) + " of " + uncategorisedQueue.size());
        dateValueLabel.setText(tx.getDate() != null ? tx.getDate() : "");
        descValueLabel.setText(tx.getDescription() != null ? tx.getDescription() : "");

        String amountText;
        if (tx.getPaidOut() > 0) {
            amountText = String.format(Locale.ENGLISH, "Paid Out: £%.2f", tx.getPaidOut());
        } else if (tx.getPaidIn() > 0) {
            amountText = String.format(Locale.ENGLISH, "Paid In: £%.2f", tx.getPaidIn());
        } else {
            amountText = "£0.00";
        }
        amountValueLabel.setText(amountText);
        balanceValueLabel.setText(String.format(Locale.ENGLISH, "£%.2f", tx.getBalance()));
    }

    public void onCategorySelected(Category category) {
        if (currentIndex >= uncategorisedQueue.size()) {
            dispose();
            return;
        }

        Transaction currentTx = uncategorisedQueue.get(currentIndex);
        Categoriser.categoriseManually(currentTx, category);
        categorisedCount++;

        // Re-check remaining transactions with the new keyword
        if (allTransactions != null) {
            for (Transaction t : allTransactions) {
                if (t != null && (t.getCategory() == null || t.getCategory().getName() == null || t.getCategory().getName().trim().isEmpty())) {
                    if (Categoriser.categorise(t)) {
                        categorisedCount++;
                    }
                }
            }
        }

        currentIndex++;
        loadCurrentTransaction();
    }

    public void onSkip() {
        currentIndex++;
        loadCurrentTransaction();
    }

    public void onSkipAll() {
        dispose();
    }

    public int getCategorisedCount() {
        return categorisedCount;
    }

    public List<JButton> getCategoryButtons() {
        return categoryButtons;
    }

    public JButton getSkipButton() {
        return skipButton;
    }

    public JButton getSkipAllButton() {
        return skipAllButton;
    }

    public JLabel getDescValueLabel() {
        return descValueLabel;
    }

    public JLabel getProgressLabel() {
        return progressLabel;
    }
}
