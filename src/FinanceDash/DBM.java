package FinanceDash;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "CREATE TABLE IF NOT EXISTS transactions (\n"
                + "    id INTEGER PRIMARY KEY AUTOINCREMENT,\n"
                + "    date TEXT,\n"
                + "    description TEXT,\n"
                + "    paid_out REAL,\n"
                + "    paid_in REAL,\n"
                + "    balance REAL,\n"
                + "    category TEXT\n"
                + ");";

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Table 'transactions' is ready (created if it did not exist).");
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    /**
     * Creates the 'transactions' table using an existing Connection.
     *
     * @param conn existing database connection
     */
    public static void createTransactionsTable(Connection conn) {
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
        } catch (SQLException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }
    }

    /**
     * Initializes the database: creates the database and the 'transactions' table if they do not exist.
     */
    public static void initializeDatabase() {
        initializeDatabase(DEFAULT_DB_NAME);
    }

    /**
     * Initializes the specified database: creates the database and the 'transactions' table if they do not exist.
     *
     * @param dbName database file name or path
     */
    public static void initializeDatabase(String dbName) {
        createNewDatabase(dbName);
        createTransactionsTable(dbName);
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
            pstmt.setString(6, transaction.getType() != null ? transaction.getType().toString() : null);
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
                    pstmt.setString(6, transaction.getType() != null ? transaction.getType().toString() : null);
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
     * Entry point for database initialization from command line.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        initializeDatabase();
    }
}
