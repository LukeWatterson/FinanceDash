package FinanceDash;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DBM {

    private static final String DEFAULT_DB_NAME = "finance.db";
    private static final String DEFAULT_URL = "jdbc:sqlite:" + DEFAULT_DB_NAME;

    /**
     * Connect to the default database (creates the database file if it does not exist).
     *
     * @return Connection object
     */
    public static Connection connect() {
        return connect(DEFAULT_DB_NAME);
    }

    /**
     * Connect to a specific database by file name / path (creates the database file if it does not exist).
     *
     * @param dbName database file name or path
     * @return Connection object
     */
    public static Connection connect(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            System.out.println("Connection to SQLite has been established: " + url);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
        }
        return conn;
    }

    /**
     * Creates a new database if one does not already exist.
     */
    public static void createNewDatabase() {
        createNewDatabase(DEFAULT_DB_NAME);
    }

    /**
     * Creates a new database with the specified file name if one does not already exist.
     *
     * @param fileName database file name or path
     */
    public static void createNewDatabase(String fileName) {
        String url = "jdbc:sqlite:" + fileName;
        try (Connection conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("Database has been created/connected: " + fileName);
            }
        } catch (SQLException e) {
            System.err.println("Error creating database: " + e.getMessage());
        }
    }

    /**
     * Creates the 'transactions' table if it does not already exist in the default database.
     */
    public static void createTransactionsTable() {
        createTransactionsTable(DEFAULT_DB_NAME);
    }

    /**
     * Creates the 'transactions' table if it does not already exist in the specified database.
     *
     * @param dbName database file name or path
     */
    public static void createTransactionsTable(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createTransactionsTable(conn);
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    /**
     * Creates the 'transactions' table using an existing Connection.
     * Also verifies that existing tables have all required columns and migrates older schemas if needed.
     *
     * @param conn existing database connection
     */
    public static void createTransactionsTable(Connection conn) {
        if (conn == null) {
            return;
        }
        String sql = "CREATE TABLE IF NOT EXISTS transactions (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    date TEXT,\n"
                + "    description TEXT,\n"
                + "    paid_out REAL,\n"
                + "    paid_in REAL,\n"
                + "    balance REAL,\n"
                + "    category TEXT\n"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'transactions' is ready (created if it did not exist).");

            // Migration / schema check: ensure all expected columns exist in an already existing table
            Set<String> columns = new HashSet<>();
            try (ResultSet rs = stmt.executeQuery("PRAGMA table_info(transactions);")) {
                while (rs.next()) {
                    columns.add(rs.getString("name").toLowerCase());
                }
            }

            if (!columns.contains("paid_out")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN paid_out REAL DEFAULT 0;");
            }
            if (!columns.contains("paid_in")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN paid_in REAL DEFAULT 0;");
            }
            if (!columns.contains("balance")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN balance REAL DEFAULT 0;");
            }
            if (!columns.contains("category")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN category TEXT;");
            }
            if (!columns.contains("date")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN date TEXT;");
            }
            if (!columns.contains("description")) {
                stmt.execute("ALTER TABLE transactions ADD COLUMN description TEXT;");
            }

            if (columns.contains("amount")) {
                stmt.execute("UPDATE transactions SET paid_out = -amount WHERE amount < 0 AND (paid_out IS NULL OR paid_out = 0) AND (paid_in IS NULL OR paid_in = 0);");
                stmt.execute("UPDATE transactions SET paid_in = amount WHERE amount > 0 AND (paid_out IS NULL OR paid_out = 0) AND (paid_in IS NULL OR paid_in = 0);");
            }
        } catch (SQLException e) {
            System.err.println("Error creating or updating table: " + e.getMessage());
        }
    }

    /**
     * Creates the 'categories' table if it does not already exist in the default database.
     */
    public static void createCategoriesTable() {
        createCategoriesTable(DEFAULT_DB_NAME);
    }

    /**
     * Creates the 'categories' table if it does not already exist in the specified database.
     *
     * @param dbName database file name or path
     */
    public static void createCategoriesTable(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            createCategoriesTable(conn);
        } catch (SQLException e) {
            System.err.println("Error creating categories table: " + e.getMessage());
        }
    }

    /**
     * Creates the 'categories' table using an existing Connection.
     *
     * @param conn existing database connection
     */
    public static void createCategoriesTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS categories (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    name TEXT UNIQUE NOT NULL,\n"
                + "    monthly_max REAL NOT NULL\n"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'categories' is ready (created if it did not exist).");

            // Ensure default categories exist
            String insertDefaultSql = "INSERT OR IGNORE INTO categories (name, monthly_max) VALUES (?, 0.0);";
            try (PreparedStatement pstmt = conn.prepareStatement(insertDefaultSql)) {
                for (Category defaultCat : Category.DEFAULT_CATEGORIES) {
                    pstmt.setString(1, defaultCat.getName());
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating categories table: " + e.getMessage());
        }
    }

    /**
     * Creates the 'settings' table if it does not already exist in the default database.
     */
    public static void createSettingsTable() {
        createSettingsTable(DEFAULT_DB_NAME);
    }

    /**
     * Creates the 'settings' table if it does not already exist in the specified database.
     *
     * @param dbName database file name or path
     */
    public static void createSettingsTable(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            createSettingsTable(conn);
        } catch (SQLException e) {
            System.err.println("Error creating settings table: " + e.getMessage());
        }
    }

    /**
     * Creates the 'settings' table using an existing Connection.
     *
     * @param conn existing database connection
     */
    public static void createSettingsTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS settings (\n"
                + "    key TEXT PRIMARY KEY,\n"
                + "    value TEXT\n"
                + ");";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'settings' is ready (created if it did not exist).");
        } catch (SQLException e) {
            System.err.println("Error creating settings table: " + e.getMessage());
        }
    }

    /**
     * Initializes the database: creates the database and all required tables if they do not exist.
     */
    public static void initializeDatabase() {
        initializeDatabase(DEFAULT_DB_NAME);
    }

    /**
     * Initializes the specified database: creates the database and required tables if they do not exist.
     *
     * @param dbName database file name or path
     */
    public static void initializeDatabase(String dbName) {
        createNewDatabase(dbName);
        createTransactionsTable(dbName);
        createCategoriesTable(dbName);
        createSettingsTable(dbName);
    }

    /**
     * Parses a single CSV line into a list of field values, correctly handling quoted values containing commas.
     *
     * @param line the CSV line to parse
     * @return a list of parsed field strings
     */
    public static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        if (line == null) {
            return values;
        }
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());
        return values;
    }

    /**
     * Reads financial transactions from the specified CSV file and parses each line into a {@link Transaction} object.
     * If a header row is detected at the beginning of the file, it is automatically skipped.
     *
     * @param filePath the path to the CSV file
     * @return a {@link List} of {@link Transaction} objects parsed from the CSV file
     */
    public static List<Transaction> readCSV(String filePath) {
        List<Transaction> transactions = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("CSV file does not exist: " + filePath);
            return transactions;
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                List<String> values = parseCsvLine(line);
                if (values.size() < 6) {
                    continue;
                }

                if (isFirstLine) {
                    isFirstLine = false;
                    String firstCol = values.get(0).toLowerCase();
                    if (firstCol.contains("date") || firstCol.contains("type") || firstCol.contains("transaction")) {
                        continue;
                    }
                }

                try {
                    String date = values.get(0);
                    String type = values.get(1);
                    String description = values.get(2);
                    String paidOut = values.get(3);
                    String paidIn = values.get(4);
                    String balance = values.get(5);

                    Transaction transaction = new Transaction(date, type, description, paidOut, paidIn, balance);
                    transactions.add(transaction);
                } catch (IllegalArgumentException e) {
                    System.err.println("Skipping invalid row in CSV (" + e.getMessage() + "): " + line);
                }
            }
            System.out.println("Read " + transactions.size() + " transactions from " + filePath);
        } catch (IOException e) {
            System.err.println("Error reading CSV file " + filePath + ": " + e.getMessage());
        }

        return transactions;
    }

    /**
     * Reads financial transactions from the specified CSV file.
     *
     * @param file the CSV file
     * @return a {@link List} of {@link Transaction} objects parsed from the CSV file
     */
    public static List<Transaction> readCSV(File file) {
        if (file == null) {
            return new ArrayList<>();
        }
        return readCSV(file.getPath());
    }

    /**
     * Inserts a single {@link Transaction} into the default database.
     *
     * @param transaction the transaction to insert
     */
    public static void insertTransaction(Transaction transaction) {
        insertTransaction(DEFAULT_DB_NAME, transaction);
    }

    /**
     * Inserts a single {@link Transaction} into the specified database.
     *
     * @param dbName      database file name or path
     * @param transaction the transaction to insert
     */
    public static void insertTransaction(String dbName, Transaction transaction) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createTransactionsTable(conn);
            insertTransaction(conn, transaction);
        } catch (SQLException e) {
            System.err.println("Error connecting or inserting transaction: " + e.getMessage());
        }
    }

    /**
     * Inserts a single {@link Transaction} into the database using an existing database connection.
     *
     * @param conn        existing database connection
     * @param transaction the transaction to insert
     */
    public static void insertTransaction(Connection conn, Transaction transaction) {
        if (conn == null || transaction == null) {
            return;
        }
        String sql = "INSERT INTO transactions (date, description, paid_out, paid_in, balance, category) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, transaction.getDate());
            pstmt.setString(2, transaction.getDescription());
            pstmt.setDouble(3, transaction.getPaidOut());
            pstmt.setDouble(4, transaction.getPaidIn());
            pstmt.setDouble(5, transaction.getBalance());
            String cat = transaction.getCategory() != null ? transaction.getCategory().getName() : (transaction.getType() != null ? transaction.getType().toString() : null);
            pstmt.setString(6, cat);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting transaction: " + e.getMessage());
        }
    }

    /**
     * Inserts a single transaction record with individual column values into the default database.
     *
     * @param date        the date of the transaction
     * @param description the description or narrative of the transaction
     * @param paidOut     the amount paid out
     * @param paidIn      the amount paid in
     * @param balance     the account balance
     * @param category    the category or type of the transaction
     */
    public static void insertTransaction(String date, String description, double paidOut, double paidIn, double balance, String category) {
        insertTransaction(DEFAULT_DB_NAME, date, description, paidOut, paidIn, balance, category);
    }

    /**
     * Inserts a single transaction record with individual column values into the specified database.
     *
     * @param dbName      database file name or path
     * @param date        the date of the transaction
     * @param description the description or narrative of the transaction
     * @param paidOut     the amount paid out
     * @param paidIn      the amount paid in
     * @param balance     the account balance
     * @param category    the category or type of the transaction
     */
    public static void insertTransaction(String dbName, String date, String description, double paidOut, double paidIn, double balance, String category) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createTransactionsTable(conn);
            insertTransaction(conn, date, description, paidOut, paidIn, balance, category);
        } catch (SQLException e) {
            System.err.println("Error connecting or inserting transaction: " + e.getMessage());
        }
    }

    /**
     * Inserts a single transaction record with individual column values using an existing database connection.
     *
     * @param conn        existing database connection
     * @param date        the date of the transaction
     * @param description the description or narrative of the transaction
     * @param paidOut     the amount paid out
     * @param paidIn      the amount paid in
     * @param balance     the account balance
     * @param category    the category or type of the transaction
     */
    public static void insertTransaction(Connection conn, String date, String description, double paidOut, double paidIn, double balance, String category) {
        if (conn == null) {
            return;
        }
        String sql = "INSERT INTO transactions (date, description, paid_out, paid_in, balance, category) VALUES (?, ?, ?, ?, ?, ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, date);
            pstmt.setString(2, description);
            pstmt.setDouble(3, paidOut);
            pstmt.setDouble(4, paidIn);
            pstmt.setDouble(5, balance);
            pstmt.setString(6, category);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting transaction: " + e.getMessage());
        }
    }

    /**
     * Inserts a list of {@link Transaction} objects into the default database.
     *
     * @param transactions the list of transactions to insert
     */
    public static void insertTransactions(List<Transaction> transactions) {
        insertTransactions(DEFAULT_DB_NAME, transactions);
    }

    /**
     * Inserts a list of {@link Transaction} objects into the specified database.
     *
     * @param dbName       database file name or path
     * @param transactions the list of transactions to insert
     */
    public static void insertTransactions(String dbName, List<Transaction> transactions) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createTransactionsTable(conn);
            insertTransactions(conn, transactions);
        } catch (SQLException e) {
            System.err.println("Error connecting or inserting transactions: " + e.getMessage());
        }
    }

    /**
     * Inserts a list of {@link Transaction} objects into the database using an existing database connection.
     * Performs batch execution within a transaction for high performance.
     *
     * @param conn         existing database connection
     * @param transactions the list of transactions to insert
     */
    public static void insertTransactions(Connection conn, List<Transaction> transactions) {
        if (conn == null || transactions == null || transactions.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO transactions (date, description, paid_out, paid_in, balance, category) VALUES (?, ?, ?, ?, ?, ?);";
        boolean autoCommitOriginal = true;
        try {
            autoCommitOriginal = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Transaction transaction : transactions) {
                    if (transaction == null) {
                        continue;
                    }
                    pstmt.setString(1, transaction.getDate());
                    pstmt.setString(2, transaction.getDescription());
                    pstmt.setDouble(3, transaction.getPaidOut());
                    pstmt.setDouble(4, transaction.getPaidIn());
                    pstmt.setDouble(5, transaction.getBalance());
                    String cat = transaction.getCategory() != null ? transaction.getCategory().getName() : (transaction.getType() != null ? transaction.getType().toString() : null);
                    pstmt.setString(6, cat);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
            conn.commit();
            System.out.println("Successfully inserted " + transactions.size() + " transactions.");
        } catch (SQLException e) {
            System.err.println("Error batch inserting transactions: " + e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("Error during rollback: " + ex.getMessage());
            }
        } finally {
            try {
                conn.setAutoCommit(autoCommitOriginal);
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    /**
     * Reads transactions from the specified CSV file and inserts them into the default database.
     *
     * @param csvFilePath the path to the CSV file
     */
    public static void insertFromCSV(String csvFilePath) {
        insertFromCSV(csvFilePath, DEFAULT_DB_NAME);
    }

    /**
     * Reads transactions from the specified CSV file and inserts them into the specified database.
     *
     * @param csvFilePath the path to the CSV file
     * @param dbName      database file name or path
     */
    public static void insertFromCSV(String csvFilePath, String dbName) {
        List<Transaction> transactions = readCSV(csvFilePath);
        if (!transactions.isEmpty()) {
            Categoriser.categorise(transactions);
            insertTransactions(dbName, transactions);
        }
    }

    /**
     * Reads transactions from the specified CSV file and inserts them using an existing database connection.
     *
     * @param conn        existing database connection
     * @param csvFilePath the path to the CSV file
     */
    public static void insertFromCSV(Connection conn, String csvFilePath) {
        List<Transaction> transactions = readCSV(csvFilePath);
        if (!transactions.isEmpty()) {
            Categoriser.categorise(transactions);
            insertTransactions(conn, transactions);
        }
    }

    /**
     * Reads transactions from the specified CSV file and inserts them into the default database.
     * Convenience alias for {@link #insertFromCSV(String)}.
     *
     * @param csvFilePath the path to the CSV file
     */
    public static void readAndInsertCSV(String csvFilePath) {
        insertFromCSV(csvFilePath);
    }

    /**
     * Reads transactions from the specified CSV file and inserts them into the specified database.
     * Convenience alias for {@link #insertFromCSV(String, String)}.
     *
     * @param csvFilePath the path to the CSV file
     * @param dbName      database file name or path
     */
    public static void readAndInsertCSV(String csvFilePath, String dbName) {
        insertFromCSV(csvFilePath, dbName);
    }

    /**
     * Retrieves all transactions from the default database.
     *
     * @return a list of all transactions
     */
    public static List<Transaction> getAllTransactions() {
        return getAllTransactions(DEFAULT_DB_NAME);
    }

    /**
     * Retrieves all transactions from the specified database.
     *
     * @param dbName database file name or path
     * @return a list of all transactions
     */
    public static List<Transaction> getAllTransactions(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createTransactionsTable(conn);
            return getAllTransactions(conn);
        } catch (SQLException e) {
            System.err.println("Error connecting or retrieving transactions: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all transactions using an existing database connection.
     *
     * @param conn existing database connection
     * @return a list of all transactions
     */
    public static List<Transaction> getAllTransactions(Connection conn) {
        List<Transaction> transactions = new ArrayList<>();
        if (conn == null) {
            return transactions;
        }
        createTransactionsTable(conn);
        String sql = "SELECT date, description, paid_out, paid_in, balance, category FROM transactions ORDER BY id ASC;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String date = rs.getString("date");
                String description = rs.getString("description");
                double paidOut = rs.getDouble("paid_out");
                double paidIn = rs.getDouble("paid_in");
                double balance = rs.getDouble("balance");
                String category = rs.getString("category");

                String paidOutStr = paidOut > 0 ? String.format(Locale.ENGLISH, "%.2f", paidOut) : "";
                String paidInStr = paidIn > 0 ? String.format(Locale.ENGLISH, "%.2f", paidIn) : "";
                String balanceStr = String.format(Locale.ENGLISH, "%.2f", balance);

                // Determine type from category or default
                String typeStr = "Contactless Payment";
                if (category != null && !category.isEmpty()) {
                    try {
                        Type parsedType = Type.fromString(category);
                        typeStr = parsedType.toString();
                    } catch (IllegalArgumentException ignored) {
                        typeStr = category;
                    }
                }

                Transaction tx;
                try {
                    tx = new Transaction(date, typeStr, description, paidOutStr, paidInStr, balanceStr, category);
                } catch (IllegalArgumentException e) {
                    tx = new Transaction(date, "Contactless Payment", description, paidOutStr, paidInStr, balanceStr, category);
                }
                transactions.add(tx);
            }
        } catch (SQLException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
        }
        return transactions;
    }

    /**
     * Inserts or updates a budget category in the default database.
     *
     * @param category the category to insert or update
     */
    public static void insertCategory(Category category) {
        insertCategory(DEFAULT_DB_NAME, category);
    }

    /**
     * Inserts or updates a budget category with the specified name and monthly maximum amount in the default database.
     *
     * @param name             the category name
     * @param monthlyMaxAmount the monthly maximum budget
     */
    public static void insertCategory(String name, double monthlyMaxAmount) {
        insertCategory(DEFAULT_DB_NAME, new Category(name, monthlyMaxAmount));
    }

    /**
     * Inserts or updates a budget category with the specified name and monthly maximum amount in the specified database.
     *
     * @param dbName           database file name or path
     * @param name             the category name
     * @param monthlyMaxAmount the monthly maximum budget
     */
    public static void insertCategory(String dbName, String name, double monthlyMaxAmount) {
        insertCategory(dbName, new Category(name, monthlyMaxAmount));
    }

    /**
     * Inserts or updates a budget category in the specified database.
     *
     * @param dbName   database file name or path
     * @param category the category to insert or update
     */
    public static void insertCategory(String dbName, Category category) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createCategoriesTable(conn);
            insertCategory(conn, category);
        } catch (SQLException e) {
            System.err.println("Error connecting or inserting category: " + e.getMessage());
        }
    }

    /**
     * Inserts or updates a budget category using an existing database connection.
     *
     * @param conn     existing database connection
     * @param category the category to insert or update
     */
    public static void insertCategory(Connection conn, Category category) {
        if (conn == null || category == null) {
            return;
        }
        String sql = "INSERT INTO categories (name, monthly_max) VALUES (?, ?) "
                + "ON CONFLICT(name) DO UPDATE SET monthly_max=excluded.monthly_max;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setDouble(2, category.getMonthlyMaxAmount());
            pstmt.executeUpdate();
            System.out.println("Category saved: " + category.getName());
        } catch (SQLException e) {
            System.err.println("Error inserting category: " + e.getMessage());
        }
    }

    /**
     * Retrieves all budget categories from the default database.
     *
     * @return a list of all budget categories
     */
    public static List<Category> getAllCategories() {
        return getAllCategories(DEFAULT_DB_NAME);
    }

    /**
     * Retrieves all budget categories from the specified database.
     *
     * @param dbName database file name or path
     * @return a list of all budget categories
     */
    public static List<Category> getAllCategories(String dbName) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createCategoriesTable(conn);
            return getAllCategories(conn);
        } catch (SQLException e) {
            System.err.println("Error connecting or retrieving categories: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Retrieves all budget categories using an existing database connection.
     *
     * @param conn existing database connection
     * @return a list of all budget categories
     */
    public static List<Category> getAllCategories(Connection conn) {
        List<Category> categories = new ArrayList<>();
        if (conn == null) {
            return categories;
        }
        String sql = "SELECT name, monthly_max FROM categories ORDER BY name ASC;";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                double monthlyMax = rs.getDouble("monthly_max");
                categories.add(new Category(name, monthlyMax));
            }
        } catch (SQLException e) {
            System.err.println("Error reading categories: " + e.getMessage());
        }
        return categories;
    }

    /**
     * Deletes a budget category by name from the default database.
     *
     * @param name the category name to delete
     */
    public static void deleteCategory(String name) {
        deleteCategory(DEFAULT_DB_NAME, name);
    }

    /**
     * Deletes a budget category by name from the specified database.
     *
     * @param dbName database file name or path
     * @param name   the category name to delete
     */
    public static void deleteCategory(String dbName, String name) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createCategoriesTable(conn);
            deleteCategory(conn, name);
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }

    /**
     * Deletes a budget category by name using an existing database connection.
     *
     * @param conn existing database connection
     * @param name the category name to delete
     */
    public static void deleteCategory(Connection conn, String name) {
        if (conn == null || name == null) {
            return;
        }
        String sql = "DELETE FROM categories WHERE LOWER(name) = LOWER(?);";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            System.out.println("Category deleted: " + name);
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
        }
    }

    /**
     * Retrieves a setting value by key from the default database.
     *
     * @param key setting key
     * @return setting value, or null if not found
     */
    public static String getSetting(String key) {
        return getSetting(DEFAULT_DB_NAME, key);
    }

    /**
     * Retrieves a setting value by key from the specified database.
     *
     * @param dbName database file name or path
     * @param key    setting key
     * @return setting value, or null if not found
     */
    public static String getSetting(String dbName, String key) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createSettingsTable(conn);
            return getSetting(conn, key);
        } catch (SQLException e) {
            System.err.println("Error getting setting: " + e.getMessage());
            return null;
        }
    }

    /**
     * Retrieves a setting value by key using an existing database connection.
     *
     * @param conn existing database connection
     * @param key  setting key
     * @return setting value, or null if not found
     */
    public static String getSetting(Connection conn, String key) {
        if (conn == null || key == null) {
            return null;
        }
        String sql = "SELECT value FROM settings WHERE key = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error reading setting: " + e.getMessage());
        }
        return null;
    }

    /**
     * Sets a setting key-value pair in the default database.
     *
     * @param key   setting key
     * @param value setting value
     */
    public static void setSetting(String key, String value) {
        setSetting(DEFAULT_DB_NAME, key, value);
    }

    /**
     * Sets a setting key-value pair in the specified database.
     *
     * @param dbName database file name or path
     * @param key    setting key
     * @param value  setting value
     */
    public static void setSetting(String dbName, String key, String value) {
        String url = "jdbc:sqlite:" + dbName;
        try (Connection conn = DriverManager.getConnection(url)) {
            createSettingsTable(conn);
            setSetting(conn, key, value);
        } catch (SQLException e) {
            System.err.println("Error setting value: " + e.getMessage());
        }
    }

    /**
     * Sets a setting key-value pair using an existing database connection.
     *
     * @param conn  existing database connection
     * @param key   setting key
     * @param value setting value
     */
    public static void setSetting(Connection conn, String key, String value) {
        if (conn == null || key == null) {
            return;
        }
        String sql = "INSERT INTO settings (key, value) VALUES (?, ?) "
                + "ON CONFLICT(key) DO UPDATE SET value=excluded.value;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error writing setting: " + e.getMessage());
        }
    }

    /**
     * Checks if this is the first launch of the program in the default database.
     *
     * @return true if first launch completed is not set or categories are empty, false otherwise
     */
    public static boolean isFirstLaunch() {
        return isFirstLaunch(DEFAULT_DB_NAME);
    }

    /**
     * Checks if this is the first launch of the program in the specified database.
     *
     * @param dbName database file name or path
     * @return true if first launch completed is not set or categories are empty, false otherwise
     */
    public static boolean isFirstLaunch(String dbName) {
        String val = getSetting(dbName, "first_launch_completed");
        return !"true".equalsIgnoreCase(val);
    }

    /**
     * Sets whether first launch has been completed in the default database.
     *
     * @param completed true if first launch is complete
     */
    public static void setFirstLaunchCompleted(boolean completed) {
        setFirstLaunchCompleted(DEFAULT_DB_NAME, completed);
    }

    /**
     * Sets whether first launch has been completed in the specified database.
     *
     * @param dbName    database file name or path
     * @param completed true if first launch is complete
     */
    public static void setFirstLaunchCompleted(String dbName, boolean completed) {
        setSetting(dbName, "first_launch_completed", String.valueOf(completed));
    }

    /**
     * Entry point for database initialization from command line.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        initializeDatabase();
    }
}
