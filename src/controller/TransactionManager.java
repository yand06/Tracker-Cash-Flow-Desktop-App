package controller;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

    private Connection connection;

    public TransactionManager(Connection connection) {
        this.connection = connection;
    }

    /**
     * Menyimpan transaksi income atau expense
     *
     * @param budgetId ID budget yang akan digunakan
     * @param incomeId ID income yang akan dikurangi (untuk expense) atau null
     * (untuk income)
     * @param type "income" atau "expense"
     * @param description Deskripsi transaksi
     * @param amount Jumlah uang
     * @return true jika berhasil, false jika gagal
     */
    public boolean saveTransaction(int budgetId, Integer incomeId, String type,
            String description, double amount) {
        try {
            connection.setAutoCommit(false);

            // Validasi input
            if (!type.equals("income") && !type.equals("expense")) {
                throw new IllegalArgumentException("Type harus 'income' atau 'expense'");
            }

            if (amount <= 0) {
                throw new IllegalArgumentException("Amount harus lebih dari 0");
            }

            // Jika expense, cek apakah income mencukupi
            if (type.equals("expense")) {
                if (incomeId == null) {
                    throw new IllegalArgumentException("Income ID harus diisi untuk transaksi expense");
                }

                if (!isIncomeAvailable(incomeId, amount)) {
                    throw new IllegalArgumentException("Saldo income tidak mencukupi untuk transaksi ini");
                }
            }

            // Insert ke tabel transaction
            String insertTransactionSQL = "INSERT INTO transaction (transaction_date, type, budget_id, "
                    + "income_id,description, amount, new_budget) VALUES (?, ?, ?, ?, ?, ?, ?)";

            // Hitung new_budget berdasarkan type
            double currentBudget = getCurrentBudget(budgetId);
            double newBudget = type.equals("income") ? currentBudget + amount : currentBudget - amount;

            PreparedStatement pstmt = connection.prepareStatement(insertTransactionSQL);
            pstmt.setDate(1, Date.valueOf(LocalDate.now()));
            pstmt.setString(2, type);
            pstmt.setInt(3, budgetId);
            pstmt.setObject(4, incomeId); // null untuk income, ada nilai untuk expense
            pstmt.setString(5, description);
            pstmt.setDouble(6, amount);
            pstmt.setDouble(7, newBudget);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Update budget di tabel master_budgeting jika perlu
                updateBudgetAmount(budgetId, newBudget);

                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }

        } catch (SQLException | IllegalArgumentException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            System.err.println("Error saving transaction: " + e.getMessage());
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cek apakah income masih tersedia untuk transaksi expense
     */
    private boolean isIncomeAvailable(int incomeId, double expenseAmount) throws SQLException {
        // Hitung total expense yang sudah digunakan dari income ini
        String sql = "SELECT COALESCE(SUM(amount), 0) as total_used FROM "
                + "transaction WHERE income_id = ? AND type = 'expense'";

        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, incomeId);
        ResultSet rs = pstmt.executeQuery();

        double totalUsed = 0;
        if (rs.next()) {
            totalUsed = rs.getDouble("total_used");
        }

        // Ambil total income
        double totalIncome = getIncomeAmount(incomeId);

        // Cek apakah sisa income mencukupi
        return (totalIncome - totalUsed) >= expenseAmount;
    }

    /**
     * Ambil jumlah income berdasarkan ID
     */
    private double getIncomeAmount(int incomeId) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM transaction WHERE income_id = ? AND type = 'income'";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, incomeId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("total");
        }
        return 0;
    }

    /**
     * Ambil budget saat ini
     */
    private double getCurrentBudget(int budgetId) throws SQLException {
        String sql = "SELECT amount FROM master_budgeting WHERE budget_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setInt(1, budgetId);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            return rs.getDouble("amount");
        }
        return 0;
    }

    /**
     * Update budget amount di master_budgeting
     */
    private void updateBudgetAmount(int budgetId, double newAmount) throws SQLException {
        String sql = "UPDATE master_budgeting SET amount = ? WHERE budget_id = ?";
        PreparedStatement pstmt = connection.prepareStatement(sql);
        pstmt.setDouble(1, newAmount);
        pstmt.setInt(2, budgetId);
        pstmt.executeUpdate();
    }

    /**
     * Ambil sisa saldo dari income tertentu
     *
     * @param incomeId
     * @return
     */
    public double getIncomeBalance(int incomeId) {
        try {
            String sql = "SELECT "
                    + "COALESCE(income_total.total, 0) - COALESCE(expense_total.total, 0) as balance "
                    + "FROM "
                    + "(SELECT SUM(amount) as total FROM transaction "
                    + "WHERE income_id = ? AND type = 'income') income_total "
                    + "CROSS JOIN "
                    + "(SELECT COALESCE(SUM(amount), 0) as total FROM transaction "
                    + "WHERE income_id = ? AND type = 'expense') expense_total";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setInt(1, incomeId);
            pstmt.setInt(2, incomeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("balance");
            }
            return 0;
        } catch (SQLException e) {
            System.err.println("Error getting income balance: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Ambil daftar income yang masih memiliki saldo
     *
     * @return
     */
    public List<IncomeInfo> getAvailableIncomes() {
        List<IncomeInfo> incomes = new ArrayList<>();
        try {
            String sql = "SELECT "
                    + "i.income_id, "
                    + "i.income_category, "
                    + "i.created_date, "
                    + "COALESCE(income_total.total, 0) as total_income, "
                    + "COALESCE(expense_total.total, 0) as total_expense, "
                    + "COALESCE(income_total.total, 0) - COALESCE(expense_total.total, 0) as balance "
                    + "FROM master_income i "
                    + "LEFT JOIN ( "
                    + "    SELECT income_id, SUM(amount) as total "
                    + "    FROM transaction "
                    + "    WHERE type = 'income' "
                    + "    GROUP BY income_id "
                    + ") income_total ON i.income_id = income_total.income_id "
                    + "LEFT JOIN ( "
                    + "    SELECT income_id, SUM(amount) as total "
                    + "    FROM transaction "
                    + "    WHERE type = 'expense' "
                    + "    GROUP BY income_id "
                    + ") expense_total ON i.income_id = expense_total.income_id "
                    + "HAVING balance > 0 "
                    + "ORDER BY i.created_date DESC";

            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                incomes.add(new IncomeInfo(
                        rs.getInt("income_id"),
                        rs.getString("income_category"),
                        rs.getDate("created_date").toLocalDate(),
                        rs.getDouble("total_income"),
                        rs.getDouble("total_expense"),
                        rs.getDouble("balance")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting available incomes: " + e.getMessage());
        }
        return incomes;
    }

    // Inner class untuk informasi income
    public static class IncomeInfo {

        public final int incomeId;
        public final String category;
        public final LocalDate createdDate;
        public final double totalIncome;
        public final double totalExpense;
        public final double balance;

        public IncomeInfo(int incomeId, String category, LocalDate createdDate,
                double totalIncome, double totalExpense, double balance) {
            this.incomeId = incomeId;
            this.category = category;
            this.createdDate = createdDate;
            this.totalIncome = totalIncome;
            this.totalExpense = totalExpense;
            this.balance = balance;
        }

        @Override
        public String toString() {
            return String.format("%s (Saldo: %.2f)", category, balance);
        }
    }
}

// Contoh penggunaan
class TransactionExample {

    public static void main(String[] args) {
        try {
            // Koneksi database (sesuaikan dengan database Anda)
            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/personal_expense_tracker",
                    "root", ""
            );

            TransactionManager tm = new TransactionManager(conn);

            // Contoh 1: Menyimpan transaksi income
            boolean incomeSuccess = tm.saveTransaction(
                    1, // budget_id
                    null, // income_id (null untuk income)
                    "income",
                    "Gaji Bulan Januari",
                    5000000.0
            );

            if (incomeSuccess) {
                System.out.println("Transaksi income berhasil disimpan!");
            }

            // Contoh 2: Menyimpan transaksi expense
            boolean expenseSuccess = tm.saveTransaction(
                    1, // budget_id
                    1, // income_id (dari income yang sudah ada)
                    "expense",
                    "Belanja Bulanan",
                    1500000.0
            );

            if (expenseSuccess) {
                System.out.println("Transaksi expense berhasil disimpan!");
            }

            // Cek saldo income
            double balance = tm.getIncomeBalance(1);
            System.out.println("Saldo income ID 1: " + balance);

            // Lihat daftar income yang masih ada saldo
            List<TransactionManager.IncomeInfo> availableIncomes = tm.getAvailableIncomes();
            System.out.println("Income yang tersedia:");
            for (TransactionManager.IncomeInfo income : availableIncomes) {
                System.out.println("- " + income.toString());
            }

            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
