package FinanceDash;

/**
 * Represents the type of a financial transaction.
 */
public enum Type {
    /**
     * A transaction made via contactless payment.
     */
    CONTACTLESS_PAYMENT,

    /**
     * A purchase transaction made using a Visa card.
     */
    VISA_PURCHASE,

    /**
     * A credit transaction credited to a Visa account.
     */
    VISA_CREDIT,

    /**
     * A credit transaction into the bank account.
     */
    BANK_CREDIT,

    /**
     * An automated direct debit payment.
     */
    DIRECT_DEBIT,

    /**
     * A payment transferred to another account or party.
     */
    PAYMENT_TO;

    /**
     * Parses a string and returns the corresponding {@code FinanceDash.Type} enum constant.
     * The input string is normalized by trimming leading and trailing whitespace,
     * removing double quotes, converting to uppercase, and replacing spaces with underscores.
     *
     * @param input the string representing the transaction type
     * @return the matching {@code FinanceDash.Type}
     * @throws IllegalArgumentException if {@code input} is {@code null} or does not match any known transaction type
     */
    public static Type fromString(String input) {
        if (input == null) {
            throw new IllegalArgumentException("FinanceDash.Type string cannot be null");
        }

        String normalized = input
                .trim()
                .replace("\"", "")
                .toUpperCase()
                .replaceAll("\\s+", "_");

        return switch (normalized) {
            case "CONTACTLESS_PAYMENT" -> CONTACTLESS_PAYMENT;
            case "VISA_PURCHASE" -> VISA_PURCHASE;
            case "VISA_CREDIT" -> VISA_CREDIT;
            case "BANK_CREDIT" -> BANK_CREDIT;
            case "DIRECT_DEBIT" -> DIRECT_DEBIT;
            case "PAYMENT_TO" -> PAYMENT_TO;
            default ->
                    throw new IllegalArgumentException("Unknown FinanceDash.Type: '" + input + "' (normalized as '" + normalized + "')");
        };
    }

    /**
     * Returns a human-readable, title-cased string representation of this transaction type
     * (for example, {@code "Contactless Payment"}).
     *
     * @return the formatted string representation of the transaction type
     */
    @Override
    public String toString() {
        String[] words = name().toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            result.append(word.substring(0, 1).toUpperCase())
                    .append(word.substring(1));
        }

        return result.toString();
    }
}