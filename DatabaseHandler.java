package utils;
import model.Transaction;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {
    private static final String DB_URL = "jdbc:sqlite:expense_tracker.db";

    // Create table if not exists
    static {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "type TEXT," +
                    "category TEXT," +
                    "amount REAL," +
                    "date TEXT)";
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Insert a transaction
    public static void saveTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(type, category, amount, date) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getType());
            pstmt.setString(2, t.getCategory());
            pstmt.setDouble(3, t.getAmount());
            pstmt.setString(4, t.getDate().toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Load all transactions
    public static List<Transaction> loadTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                transactions.add(new Transaction(
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        LocalDate.parse(rs.getString("date"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
