package FinanceDash;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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

    public static void main(String[] args) {
        initializeDatabase();
    }
}
