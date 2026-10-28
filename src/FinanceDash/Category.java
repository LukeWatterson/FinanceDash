package FinanceDash;

import java.util.Objects;

/**
 * Represents a budget category with a name and a monthly maximum budget amount.
 */
public class Category {
    private String name;
    private double monthlyMaxAmount;

    /**
     * Constructs a new {@code Category} with the specified name and monthly maximum amount.
     *
     * @param name             the name of the category (e.g. "Transport")
     * @param monthlyMaxAmount the monthly maximum budget amount (e.g. 60.0)
     */
    public Category(String name, double monthlyMaxAmount) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name.trim();
        this.monthlyMaxAmount = monthlyMaxAmount;
    }

    /**
     * Constructs a new {@code Category} with the specified name and string representation of the monthly amount.
     * Strips currency symbols (such as '£', '$', '€') and whitespace.
     *
     * @param name                  the name of the category
     * @param monthlyMaxAmountStr   the string representation of the monthly maximum amount (e.g. "£60" or "60.00")
     */
    public Category(String name, String monthlyMaxAmountStr) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name.trim();
        this.monthlyMaxAmount = parseAmount(monthlyMaxAmountStr);
    }

    private static double parseAmount(String amountStr) {
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return 0.0;
        }
        String cleaned = amountStr.replace("£", "").replace("$", "").replace("€", "").trim();
        return Double.parseDouble(cleaned);
    }

    /**
     * Gets the name of the budget category.
     *
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the budget category.
     *
     * @param name the new category name
     */
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be null or empty");
        }
        this.name = name.trim();
    }

    /**
     * Gets the monthly maximum budget amount for this category.
     *
     * @return monthly max amount
     */
    public double getMonthlyMaxAmount() {
        return monthlyMaxAmount;
    }

    /**
     * Sets the monthly maximum budget amount for this category.
     *
     * @param monthlyMaxAmount the new monthly max amount
     */
    public void setMonthlyMaxAmount(double monthlyMaxAmount) {
        this.monthlyMaxAmount = monthlyMaxAmount;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(name.toLowerCase(), category.name.toLowerCase());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name.toLowerCase());
    }
}
