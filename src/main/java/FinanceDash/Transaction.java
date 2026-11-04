package FinanceDash;

/**
 * Represents a financial transaction containing details such as date, type, description,
 * amounts paid in or out, account balance, and category.
 */
public class Transaction {
    private String date;
    private Type type;
    private String description;
    private double paidOut;
    private double paidIn;
    private double balance;
    private Category category;

    /**
     * Constructs a new {@code FinanceDash.Transaction} with the specified details.
     * Numeric strings (paidOut, paidIn, balance) may include currency symbols (e.g. '£')
     * or be empty, in which case they default to 0.
     *
     * @param date        the date of the transaction
     * @param type        the string representation of the transaction type
     * @param description the description or narrative of the transaction
     * @param paidOut     the amount paid out, formatted as a string (e.g., "£10.50" or "")
     * @param paidIn      the amount paid in, formatted as a string (e.g., "£100.00" or "")
     * @param balance     the account balance after the transaction, formatted as a string
     */
    public Transaction(String date, String type, String description, String paidOut, String paidIn, String balance) {
        this(date, type, description, paidOut, paidIn, balance, (Category) null);
    }

    /**
     * Constructs a new {@code FinanceDash.Transaction} with the specified details including a budget category.
     *
     * @param date        the date of the transaction
     * @param type        the string representation of the transaction type
     * @param description the description or narrative of the transaction
     * @param paidOut     the amount paid out, formatted as a string (e.g., "£10.50" or "")
     * @param paidIn      the amount paid in, formatted as a string (e.g., "£100.00" or "")
     * @param balance     the account balance after the transaction, formatted as a string
     * @param category    the budget category assigned to this transaction
     */
    public Transaction(String date, String type, String description, String paidOut, String paidIn, String balance, Category category) {
        this.date = date;
        this.type = Type.fromString(type);
        this.description = description;
        this.category = category;

        if (paidOut == null || paidOut.trim().isEmpty()){
            this.paidOut = 0;
        } else {
            paidOut = paidOut.replace("£", "").trim();
            this.paidOut = Double.parseDouble(paidOut);
        }
        if(paidIn == null || paidIn.trim().isEmpty()){
            this.paidIn = 0;
        } else {
            paidIn = paidIn.replace("£", "").trim();
            this.paidIn = Double.parseDouble(paidIn);
        }
        if(balance == null || balance.trim().isEmpty()){
            this.balance = 0;
        } else{
            balance = balance.replace("£", "").trim();
            this.balance = Double.parseDouble(balance);
        }
    }

    /**
     * Constructs a new {@code FinanceDash.Transaction} with the specified details including a budget category name.
     *
     * @param date         the date of the transaction
     * @param type         the string representation of the transaction type
     * @param description  the description or narrative of the transaction
     * @param paidOut      the amount paid out, formatted as a string (e.g., "£10.50" or "")
     * @param paidIn       the amount paid in, formatted as a string (e.g., "£100.00" or "")
     * @param balance      the account balance after the transaction, formatted as a string
     * @param categoryName the name of the budget category
     */
    public Transaction(String date, String type, String description, String paidOut, String paidIn, String balance, String categoryName) {
        this(date, type, description, paidOut, paidIn, balance, categoryName != null ? new Category(categoryName, 0.0) : null);
    }

    /**
     * Gets the date of the transaction.
     *
     * @return the transaction date
     */
    public String getDate() {
        return date;
    }

    /**
     * Gets the type of the transaction.
     *
     * @return the transaction type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the description of the transaction.
     *
     * @return the transaction description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the amount paid out in the transaction.
     *
     * @return the paid out amount
     */
    public double getPaidOut() {
        return paidOut;
    }

    /**
     * Gets the amount paid in during the transaction.
     *
     * @return the paid in amount
     */
    public double getPaidIn() {
        return paidIn;
    }

    /**
     * Gets the account balance after the transaction.
     *
     * @return the account balance
     */
    public double getBalance() {
        return balance;
    }

    /**
     * Gets the budget category of the transaction.
     *
     * @return the transaction category
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Sets the budget category of the transaction.
     *
     * @param category the budget category
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Sets the budget category of the transaction by name.
     *
     * @param categoryName the name of the budget category
     */
    public void setCategory(String categoryName) {
        this.category = categoryName != null ? new Category(categoryName, 0.0) : null;
    }
}

