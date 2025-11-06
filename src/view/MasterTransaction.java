package view;

import component.CustomCellRenderer;
import component.CustomHeaderRenderer;
import component.ExcelExporter;
import config.DatabaseConnection;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import model.TransactionData;

public class MasterTransaction extends javax.swing.JPanel {

    public static double totalAmount = 0;

    public MasterTransaction() {
        initComponents();
        setBackground(new Color(0, 0, 0, 0));
        clear();
        hapus();
    }

    private void hapus() {
        startDateExport.setEnabled(false);
        startDateExport.setVisible(false);
        endDateExport.setEnabled(false);
        endDateExport.setVisible(false);
        jLabel18.setVisible(false);
        jLabel19.setVisible(false);
    }

    private void clear() {
        separatorInput();
        excelExport.setCursor(new Cursor(Cursor.HAND_CURSOR) {
        });
        transactionTable.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        transactionTable.setDefaultRenderer(Object.class, new CustomCellRenderer());
        transactionTable.getTableHeader().setReorderingAllowed(false);
        transactionTable.getTableHeader().setResizingAllowed(false);
        transactionTable.getTableHeader().setPreferredSize(new Dimension(0, 30));
        transactionDate.setDate(new Date());
        startDateExport.setDate(new Date());
        endDateExport.setDate(new Date());
        addPlaceholder(txAmount, "0");
        cbbType.setSelectedIndex(0);
        cbbCategory.setSelectedIndex(0);
        cbbDescription.setSelectedIndex(0);
        txDescription.setVisible(false);
        loadTransactionData();
//        hitungTotalAmount();
        updateRemainingBudget();

        cbbType.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                loadCategories();
                if (selected.equals("Income")) {
                    loadDescriptionIncome();
                }
                System.out.println("Item terpilih:  " + selected);
            } else {
                System.out.println("Combo Box Type.");
            }
        });

        cbbCategory.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) e.getItem();
                if (selected.equals("Income")) {
                    loadDescriptionIncome();
                } else {
                    loadDescriptionExpense();
                }
            } else {
                System.out.println("Combo Box Category.");
            }
        });
        invisibleButton();
    }

    private void invisibleButton() {
        int select = transactionTable.getSelectedRow();
        if (select == -1) {
            btnSave.setEnabled(true);
            btnUpdate.setEnabled(false);
        } else {
            btnSave.setEnabled(false);
            btnUpdate.setEnabled(true);
        }
    }

    private void addPlaceholder(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setForeground(Color.GRAY);
                    textField.setText(placeholder);
                }
            }
        });
    }

    private void separatorInput() {
        txAmount.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                formatAmount(txAmount);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                formatAmount(txAmount);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                formatAmount(txAmount);
            }

            private void formatAmount(JTextField textField) {
                String input = textField.getText().replaceAll("[^0-9]", "");

                if (input.isEmpty()) {
                    return;
                }

                try {
                    long number = Long.parseLong(input);
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setGroupingSeparator('.');
                    symbols.setDecimalSeparator(',');
                    DecimalFormat formatter = new DecimalFormat("#,###,###,###", symbols);

                    SwingUtilities.invokeLater(() -> {
                        // Set text only if necessary
                        String formattedText = formatter.format(number);
                        if (!formattedText.equals(textField.getText())) {
                            textField.setText(formattedText);
                        }
                    });
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public int getBudgetId(String expense, String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT budget_id FROM master_budgeting WHERE expense = ? AND expense_description = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, expense);
            ps.setString(2, description);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("budget_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int getIncomeId(String categoryName) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String query = "SELECT income_id FROM master_income WHERE income_category = ?";
            PreparedStatement ps = conn.prepareStatement(query);
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("income_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadCategories() {
        try {
            // Membuka koneksi ke database
            Connection conn = DatabaseConnection.getConnection();
            String type = cbbType.getSelectedItem().toString();

            switch (type) {
                case "Income":
                    cbbCategory.removeAllItems();
                    cbbCategory.addItem("Select Category");
                    // Mengisi ComboBox category dari master_income
                    String incomeQuery = "SELECT DISTINCT income_category FROM master_income";
                    PreparedStatement psIncome = conn.prepareStatement(incomeQuery);
                    ResultSet rsIncome = psIncome.executeQuery();
                    while (rsIncome.next()) {
                        cbbCategory.addItem(rsIncome.getString("income_category"));
                    }
                    break;
                case "Expense":
                    cbbCategory.removeAllItems();
                    cbbCategory.addItem("Select Category");
                    String expenseQuery = "SELECT DISTINCT expense FROM master_budgeting";
                    PreparedStatement psExpense = conn.prepareStatement(expenseQuery);
                    ResultSet rsExpense = psExpense.executeQuery();
                    while (rsExpense.next()) {
                        cbbCategory.addItem(rsExpense.getString("expense"));
                    }
                    break;
                default:
                    cbbCategory.removeAllItems();
                    cbbCategory.addItem("Selected Category");
                    System.out.println("Not Selected Type.");
                    break;
            }
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDescriptionExpense() {
        try {
            // Clear previous items in description ComboBox
            txDescription.setVisible(false);
            cbbDescription.setVisible(true);
            cbbDescription.removeAllItems();
            cbbDescription.addItem("Select Description");

            // Get the selected category
            String categorySelected = cbbCategory.getSelectedItem().toString();

            // Check if the category is valid (i.e., not "Select Category")
            if (categorySelected.equals("Select Category")) {
                System.out.println("No category selected.");
                return; // Don't proceed if no valid category is selected
            }

            Connection conn = DatabaseConnection.getConnection();

            // Modified SQL query to get all descriptions for the selected category
            String categorySQL = "SELECT DISTINCT expense_description "
                    + "FROM master_budgeting "
                    + "WHERE expense = ?";

            PreparedStatement ps = conn.prepareStatement(categorySQL);
            ps.setString(1, categorySelected);
            ResultSet rs = ps.executeQuery();

            // Add all descriptions to the ComboBox
            while (rs.next()) {
                String description = rs.getString("expense_description");
                cbbDescription.addItem(description);
                System.out.println("Added description: " + description);
            }

            // Close connections
            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading descriptions: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDescriptionIncome() {
//      Tampilin TextField Description for income and Invisible cbbDescription for Expense
        cbbDescription.setVisible(false);
        txDescription.setVisible(true);
        addPlaceholder(txDescription, "Enter Text");
    }

    public void loadTransactionData() {
        String query = "SELECT "
                + "    t.transaction_id, "
                + "    t.transaction_date, "
                + "    t.type, "
                + "    COALESCE(mi.income_category, mb.expense) AS category, "
                + "    t.description, "
                + "    t.amount, "
                + "    t.new_budget "
                + "FROM transaction t "
                + "LEFT JOIN master_income mi ON t.income_id = mi.income_id "
                + "LEFT JOIN master_budgeting mb ON t.budget_id = mb.budget_id "
                + "ORDER BY t.transaction_date ASC";

        DecimalFormat rupiahFormat = new DecimalFormat("#,###,###,###");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        try {
            // Koneksi ke database
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            // Membuat model tabel untuk menampilkan data
            DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();

            // Menghapus baris lama
            model.setRowCount(0);

            int i = 0;
            while (rs.next()) {
                i++;
                LocalDate transactionDate = rs.getDate("transaction_date").toLocalDate();
                String formattedDate = transactionDate.format(formatter);
                String type = rs.getString("type");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String description = rs.getString("description");
                String transactionId = rs.getString("transaction_id");
                double newBudget = rs.getDouble("new_budget");

                String amountRupiahFormat = rupiahFormat.format(amount);
                String amountNewBudgetFormat = rupiahFormat.format(newBudget);

                model.addRow(new Object[]{
                    i,
                    formattedDate,
                    type,
                    category,
                    "Rp" + amountRupiahFormat,
                    description,
                    transactionId,
                    "Rp" + amountNewBudgetFormat
                });
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedTransactionId() {
        int row = transactionTable.getSelectedRow();
        String valueAtRow = (String) transactionTable.getValueAt(row, 5);

        try {
            return Integer.parseInt(valueAtRow);  // Mengonversi String menjadi Integer
        } catch (NumberFormatException e) {
            // Tangani kesalahan parsing, jika nilai tidak dapat diparse menjadi Integer
            JOptionPane.showMessageDialog(this, "Invalid transaction ID", "Error", JOptionPane.ERROR_MESSAGE);
            return -1;  // Kembalikan nilai default jika terjadi kesalahan
        }
    }

    private List<TransactionData> getTransactionData(Date fromDate, Date toDate) {
        List<TransactionData> transactionDataList = new ArrayList<>();

        String query = "SELECT "
                + "    t.transaction_date, "
                + "    t.type, "
                + "    CASE "
                + "        WHEN t.type = 'Income' THEN 'Income' "
                + "        ELSE mb.expense "
                + "    END as category, "
                + "    t.description, "
                + "    t.amount, "
                + "    mb.amount as budget_amount "
                + "FROM transaction t "
                + "LEFT JOIN master_budgeting mb ON t.budget_id = mb.budget_id "
                + "WHERE t.transaction_date BETWEEN ? AND ? "
                + "ORDER BY t.transaction_date DESC";

        try (
                Connection conn = DatabaseConnection.getConnection(); PreparedStatement pst = conn.prepareStatement(query)) {
            pst.setDate(1, new java.sql.Date(fromDate.getTime()));
            pst.setDate(2, new java.sql.Date(toDate.getTime()));

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    TransactionData data = new TransactionData(
                            rs.getDate("transaction_date"),
                            rs.getString("type"),
                            rs.getString("category"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getDouble("budget_amount")
                    );
                    transactionDataList.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error retrieving data from database: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        return transactionDataList;
    }

    private void updateRemainingBudget() {
        try {
            Connection conn = DatabaseConnection.getConnection();

            String incomeQuery = "SELECT COALESCE(SUM(amount), 0) AS total_income FROM transaction WHERE type = 'Income'";
            String expenseQuery = "SELECT COALESCE(SUM(amount), 0) AS total_expense FROM transaction WHERE type = 'Expense'";

            PreparedStatement psIncome = conn.prepareStatement(incomeQuery);
            ResultSet rsIncome = psIncome.executeQuery();
            double totalIncome = 0;
            if (rsIncome.next()) {
                totalIncome = rsIncome.getDouble("total_income");
            }

            PreparedStatement psExpense = conn.prepareStatement(expenseQuery);
            ResultSet rsExpense = psExpense.executeQuery();
            double totalExpense = 0;
            if (rsExpense.next()) {
                totalExpense = rsExpense.getDouble("total_expense");
            }

            double remainingBudget = totalIncome - totalExpense;

            DecimalFormat rupiahFormat = new DecimalFormat("#,###,###,###");
            String formattedBudget = rupiahFormat.format(remainingBudget);

            budgetRemaining.setText("Rp" + formattedBudget);

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error calculating remaining budget.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void hitungTotalAmount() {
        totalAmount = 0;
        int rowCount = transactionTable.getRowCount();
        System.out.println("Menghitung total dari " + rowCount + " baris");

        for (int i = 0; i < rowCount; i++) {
            Object val = transactionTable.getValueAt(i, 3);

            if (val == null) {
                System.out.println("Baris " + i + ": nilai null, dilewati.");
                continue; // skip ke baris berikutnya
            }

            System.out.println("Baris " + i + " nilai: " + val);

            try {
                if (val instanceof Number) {
                    totalAmount += ((Number) val).doubleValue();
                } else {
                    String cleaned = val.toString().replaceAll("[^0-9.,-]", "").replace(",", ".");
                    totalAmount += Double.parseDouble(cleaned);
                }
            } catch (NumberFormatException e) {
                System.out.println("Baris " + i + ": gagal parsing nilai: " + val);
            }
        }

        System.out.println("TOTAL: " + totalAmount);
    }

    // Format angka ke bentuk ribuan Indonesia, tanpa desimal
    private String formatAmountToRupiah(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        return formatter.format(value);
    }

    private double parseSafeAmount(String input) {
        if (input == null || input.trim().isEmpty()) {
            return 0.0;
        }

        try {
            // Hapus Rp dan spasi
            input = input.replace("Rp", "").replace(" ", "").trim();

            // Hapus SEMUA karakter selain angka
            input = input.replaceAll("[^\\d]", ""); // hanya angka

            return Double.parseDouble(input);
        } catch (NumberFormatException e) {
            System.err.println("❌ Format error saat parsing: " + input);
            e.printStackTrace();
            return 0.0;
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDayChooser1 = new com.toedter.calendar.JDayChooser();
        jDayChooser2 = new com.toedter.calendar.JDayChooser();
        jDateChooser1 = new com.toedter.calendar.JDateChooser();
        masterIncomePanel = new component.Panel();
        jLabel11 = new javax.swing.JLabel();
        transactionDate = new com.toedter.calendar.JDateChooser();
        jLabel12 = new javax.swing.JLabel();
        cbbType = new component.UnderlineComboBox();
        jLabel13 = new javax.swing.JLabel();
        cbbCategory = new component.UnderlineComboBox();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        txAmount = new component.TextField();
        btnSave = new component.Button();
        btnCancel = new component.Button();
        jScrollPane1 = new javax.swing.JScrollPane();
        transactionTable = new javax.swing.JTable();
        btnUpdate = new component.Button();
        underlineLabel1 = new component.UnderlineLabel();
        excelExport = new component.Panel();
        jLabel16 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        startDateExport = new com.toedter.calendar.JDateChooser();
        jLabel18 = new javax.swing.JLabel();
        endDateExport = new com.toedter.calendar.JDateChooser();
        jLabel19 = new javax.swing.JLabel();
        btnDelete = new component.Button();
        underlineLabel2 = new component.UnderlineLabel();
        budgetRemaining = new javax.swing.JLabel();
        cbbDescription = new component.UnderlineComboBox();
        txDescription = new component.TextField();

        setBackground(new java.awt.Color(255, 255, 255));

        masterIncomePanel.setBackground(new java.awt.Color(163, 192, 255));
        masterIncomePanel.setRoundBottomRight(35);

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Date :");

        transactionDate.setBackground(new java.awt.Color(163, 192, 255));
        transactionDate.setDateFormatString("dd MMM yyyy");
        transactionDate.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N

        jLabel12.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("Type :");

        cbbType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Type", "Income", "Expense" }));
        cbbType.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

        jLabel13.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setText("Category :");

        cbbCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Category" }));
        cbbCategory.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N
        cbbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbCategoryActionPerformed(evt);
            }
        });

        jLabel14.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(0, 0, 0));
        jLabel14.setText("Description :");

        jLabel15.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(0, 0, 0));
        jLabel15.setText("Amount :");

        txAmount.setBackground(new java.awt.Color(255, 255, 255, 0)
        );
        txAmount.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

        btnSave.setBackground(new java.awt.Color(16, 28, 76));
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/component/Add_New.png"))); // NOI18N
        btnSave.setText("Save");
        btnSave.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(16, 28, 76));
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/component/cancel.png"))); // NOI18N
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        transactionTable.setBackground(new java.awt.Color(255, 255, 255));
        transactionTable.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        transactionTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "No", "Date", "Type", "Category", "Amount", "Description", "ID", "Remaining Budget"
            }
        ));
        transactionTable.setRowHeight(30);
        transactionTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                transactionTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(transactionTable);
        if (transactionTable.getColumnModel().getColumnCount() > 0) {
            transactionTable.getColumnModel().getColumn(0).setMinWidth(50);
            transactionTable.getColumnModel().getColumn(0).setMaxWidth(50);
            transactionTable.getColumnModel().getColumn(5).setMinWidth(0);
            transactionTable.getColumnModel().getColumn(5).setMaxWidth(0);
            transactionTable.getColumnModel().getColumn(6).setMinWidth(0);
            transactionTable.getColumnModel().getColumn(6).setMaxWidth(0);
        }

        btnUpdate.setBackground(new java.awt.Color(16, 28, 76));
        btnUpdate.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/component/edit.png"))); // NOI18N
        btnUpdate.setText("Update");
        btnUpdate.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        btnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateActionPerformed(evt);
            }
        });

        underlineLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        underlineLabel1.setText("Transaction");
        underlineLabel1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N

        excelExport.setBackground(new java.awt.Color(0, 47, 142));
        excelExport.setRoundBottomLeft(15);
        excelExport.setRoundBottomRight(15);
        excelExport.setRoundTopLeft(15);
        excelExport.setRoundTopRight(15);
        excelExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                excelExportMouseClicked(evt);
            }
        });

        jLabel16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/excel.png"))); // NOI18N

        jLabel17.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(255, 255, 255));
        jLabel17.setText("Export to Excel");

        javax.swing.GroupLayout excelExportLayout = new javax.swing.GroupLayout(excelExport);
        excelExport.setLayout(excelExportLayout);
        excelExportLayout.setHorizontalGroup(
            excelExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(excelExportLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel16, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel17, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        excelExportLayout.setVerticalGroup(
            excelExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(excelExportLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(excelExportLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel16))
                .addGap(8, 8, 8))
        );

        startDateExport.setBackground(new java.awt.Color(163, 192, 255));
        startDateExport.setDateFormatString("dd MMM yyyy");
        startDateExport.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N

        jLabel18.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        jLabel18.setText("Start Date :");

        endDateExport.setBackground(new java.awt.Color(163, 192, 255));
        endDateExport.setDateFormatString("dd MMM yyyy");
        endDateExport.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N

        jLabel19.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setText("End Date :");

        btnDelete.setBackground(new java.awt.Color(16, 28, 76));
        btnDelete.setForeground(new java.awt.Color(255, 255, 255));
        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/component/delete.png"))); // NOI18N
        btnDelete.setText("Delete");
        btnDelete.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });

        underlineLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        underlineLabel2.setText("Total Remaining Budget :");
        underlineLabel2.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N

        budgetRemaining.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        budgetRemaining.setForeground(new java.awt.Color(0, 0, 0));
        budgetRemaining.setText("0");

        cbbDescription.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Select Description" }));
        cbbDescription.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N
        cbbDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbbDescriptionActionPerformed(evt);
            }
        });

        txDescription.setBackground(new java.awt.Color(255, 255, 255, 0));
        txDescription.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txDescription.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N

        javax.swing.GroupLayout masterIncomePanelLayout = new javax.swing.GroupLayout(masterIncomePanel);
        masterIncomePanel.setLayout(masterIncomePanelLayout);
        masterIncomePanelLayout.setHorizontalGroup(
            masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(underlineLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(masterIncomePanelLayout.createSequentialGroup()
                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, masterIncomePanelLayout.createSequentialGroup()
                                .addComponent(underlineLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(budgetRemaining, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(0, 0, 0)
                                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                .addComponent(jLabel18)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(startDateExport, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(25, 25, 25)
                                .addComponent(jLabel19)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(endDateExport, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(excelExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 886, Short.MAX_VALUE)
                            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
                                            .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(19, 19, 19)
                                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(cbbType, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(transactionDate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                        .addGap(91, 91, 91)
                                        .addComponent(cbbCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                    .addComponent(jLabel13))
                                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                        .addGap(100, 100, 100)
                                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                        .addGap(26, 26, 26)
                                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(txAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                                .addComponent(cbbDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(txDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, masterIncomePanelLayout.createSequentialGroup()
                                        .addGap(70, 70, 70)
                                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(50, 50, 50))))
        );
        masterIncomePanelLayout.setVerticalGroup(
            masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(underlineLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(masterIncomePanelLayout.createSequentialGroup()
                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(transactionDate, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(cbbType, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(cbbDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE))
                                .addGap(18, 18, 18)
                                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))))
                        .addGap(18, 18, 18)
                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(cbbCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16))
                    .addGroup(masterIncomePanelLayout.createSequentialGroup()
                        .addGap(134, 134, 134)
                        .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(40, 40, 40)
                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(startDateExport, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel19, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(endDateExport, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(excelExport, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 147, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(underlineLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(budgetRemaining, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(masterIncomePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(masterIncomePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        LocalDate transactionDateValue = transactionDate.getDate() != null
                ? transactionDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                : null;
        String type = cbbType.getSelectedItem().toString();
        String category = cbbCategory.getSelectedItem().toString();
        String amountText = txAmount.getText().replaceAll("[^0-9]", "");

        // Tentukan deskripsi berdasarkan tipe transaksi
        String description;
        if (null == type) {
            description = "";
        } else {
            switch (type) {
                case "Income":
                    description = txDescription.getText().trim();
                    break;
                case "Expense":
                    description = (String) cbbDescription.getSelectedItem();
                    break;
                default:
                    description = "";
                    break;
            }
        }

        // Validasi input
        if (transactionDateValue == null || type.equals("Select Type") || category.equals("Select Category")
                || description == null || description.trim().isEmpty() || amountText.isEmpty() || amountText.equals("0")) {
            JOptionPane.showMessageDialog(this, "Please complete all fields with valid values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            int budgetId = -1;
            int incomeId = -1;
            double currentBudget = 0;
            double newBudget;

            switch (type) {
                case "Expense":
                    budgetId = getBudgetId(category, description);
                    if (budgetId == -1) {
                        JOptionPane.showMessageDialog(this, "Budget category not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    String budgetQuery = "SELECT amount FROM master_budgeting WHERE budget_id = ?";
                    PreparedStatement ps = conn.prepareStatement(budgetQuery);
                    ps.setInt(1, budgetId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        currentBudget = rs.getDouble("amount");
                    }
                    if (currentBudget < amount) {
                        int confirm = JOptionPane.showConfirmDialog(this,
                                "Insufficient budget. Proceed anyway?",
                                "Warning", JOptionPane.YES_NO_OPTION);
                        if (confirm != JOptionPane.YES_OPTION) {
                            conn.rollback();
                            return;
                        }
                    }
                    newBudget = currentBudget - amount;
                    break;
                case "Income":
                    incomeId = getIncomeId(category);
                    if (incomeId == -1) {
                        JOptionPane.showMessageDialog(this, "Income category not found.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    newBudget = amount;
                    break;
                default:
                    JOptionPane.showMessageDialog(this, "Invalid type.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
            }

            String insertSQL = "INSERT INTO transaction (transaction_date, type, budget_id, income_id, description, amount, new_budget) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement psInsert = conn.prepareStatement(insertSQL);
            psInsert.setDate(1, java.sql.Date.valueOf(transactionDateValue));
            psInsert.setString(2, type);
            if (type.equals("Income")) {
                psInsert.setNull(3, java.sql.Types.INTEGER);
                psInsert.setInt(4, incomeId);
            } else {
                psInsert.setInt(3, budgetId);
                psInsert.setNull(4, java.sql.Types.INTEGER);
            }
            psInsert.setString(5, description);
            psInsert.setDouble(6, amount);
            psInsert.setDouble(7, newBudget);

            psInsert.executeUpdate();

            // Update budget if expense
            if (type.equals("Expense")) {
                String updateBudget = "UPDATE master_budgeting SET amount = ? WHERE budget_id = ?";
                PreparedStatement psUpdate = conn.prepareStatement(updateBudget);
                psUpdate.setDouble(1, newBudget);
                psUpdate.setInt(2, budgetId);
                psUpdate.executeUpdate();
            }

            conn.commit();

            updateRemainingBudget();
            loadTransactionData();
            clear();

            InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
            Image iicon = ImageIO.read(iIcon);
            ImageIcon icon = new ImageIcon(iicon);
            JOptionPane.showMessageDialog(null,
                    "Income category saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE, icon);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        clear();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        int transactionId = getSelectedTransactionId();
        if (transactionId == -1) {
            return;
        }

        LocalDate transactionDateValue = transactionDate.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        String type = cbbType.getSelectedItem().toString();
        String category = cbbCategory.getSelectedItem().toString();
        String description = cbbDescription.getSelectedItem().toString();
        String amountText = txAmount.getText().replaceAll("[^0-9]", "");

        if (transactionDateValue == null || type.equals("Select Type") || category.equals("Select Category")
                || description.isEmpty() || amountText.isEmpty()
                || description.equals("Enter Text") || amountText.equals("0")) {
            JOptionPane.showMessageDialog(this, "Please complete all fields with valid values.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            String getOld = "SELECT amount, type, budget_id, income_id FROM transaction WHERE transaction_id = ?";
            PreparedStatement psOld = conn.prepareStatement(getOld);
            psOld.setInt(1, transactionId);
            ResultSet rsOld = psOld.executeQuery();

            if (!rsOld.next()) {
                JOptionPane.showMessageDialog(this, "Transaction not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double oldAmount = rsOld.getDouble("amount");
            String oldType = rsOld.getString("type");
            int oldBudgetId = rsOld.getInt("budget_id");

            // Rollback old budget
            if (oldType.equals("Expense")) {
                String rollback = "UPDATE master_budgeting SET amount = amount + ? WHERE budget_id = ?";
                PreparedStatement ps = conn.prepareStatement(rollback);
                ps.setDouble(1, oldAmount);
                ps.setInt(2, oldBudgetId);
                ps.executeUpdate();
            }

            int budgetId = -1, incomeId = -1;
            double currentBudget = 0, newBudget = 0;

            if (type.equals("Expense")) {
                budgetId = getBudgetId(category, description);
                if (budgetId == -1) {
                    JOptionPane.showMessageDialog(this, "Budget not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String getBudget = "SELECT amount FROM master_budgeting WHERE budget_id = ?";
                PreparedStatement ps = conn.prepareStatement(getBudget);
                ps.setInt(1, budgetId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    currentBudget = rs.getDouble("amount");
                }

                if (currentBudget < amount) {
                    int confirm = JOptionPane.showConfirmDialog(this,
                            "Budget not enough. Continue?", "Warning", JOptionPane.YES_NO_OPTION);
                    if (confirm != JOptionPane.YES_OPTION) {
                        conn.rollback();
                        return;
                    }
                }

                newBudget = currentBudget - amount;

            } else if (type.equals("Income")) {
                incomeId = getIncomeId(category);
                if (incomeId == -1) {
                    JOptionPane.showMessageDialog(this, "Income not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                newBudget = amount;
            }

            String updateSQL = "UPDATE transaction SET transaction_date = ?, type = ?, budget_id = ?, income_id = ?, description = ?, amount = ?, new_budget = ? "
                    + "WHERE transaction_id = ?";
            PreparedStatement psUpdate = conn.prepareStatement(updateSQL);
            psUpdate.setDate(1, java.sql.Date.valueOf(transactionDateValue));
            psUpdate.setString(2, type);
            if (type.equals("Expense")) {
                psUpdate.setInt(3, budgetId);
                psUpdate.setNull(4, java.sql.Types.INTEGER);
            } else {
                psUpdate.setNull(3, java.sql.Types.INTEGER);
                psUpdate.setInt(4, incomeId);
            }
            psUpdate.setString(5, description);
            psUpdate.setDouble(6, amount);
            psUpdate.setDouble(7, newBudget);
            psUpdate.setInt(8, transactionId);

            psUpdate.executeUpdate();

            if (type.equals("Expense")) {
                String updateBudget = "UPDATE master_budgeting SET amount = ? WHERE budget_id = ?";
                PreparedStatement psBudget = conn.prepareStatement(updateBudget);
                psBudget.setDouble(1, newBudget);
                psBudget.setInt(2, budgetId);
                psBudget.executeUpdate();
            }

            conn.commit();
            updateRemainingBudget();
            loadTransactionData();
            clear();

            JOptionPane.showMessageDialog(this, "Transaction updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating transaction: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void transactionTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_transactionTableMouseClicked
        int row = transactionTable.getSelectedRow();
        if (row == -1) {
            return;
        }

        String transactionDateStr = transactionTable.getValueAt(row, 1).toString();
        String type = transactionTable.getValueAt(row, 2).toString();
        String category = transactionTable.getValueAt(row, 3).toString();
        String amountStr = transactionTable.getValueAt(row, 4).toString();
        String description = transactionTable.getValueAt(row, 5).toString();

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
            java.util.Date date = inputFormat.parse(transactionDateStr);
            transactionDate.setDate(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        System.out.println("=== DEBUH Mouse Click Table ===");
        System.out.println("Type: " + type);
        System.out.println("Category: " + category);

        cbbType.setSelectedItem(type);
        cbbCategory.setSelectedItem(category);

        if ("Income".equalsIgnoreCase(type)) {
            txDescription.setText(description);
            txDescription.setForeground(Color.BLACK);
            cbbDescription.setVisible(false);
            txDescription.setVisible(true);
            System.out.println("Description: " + description);
        } else if ("Expense".equalsIgnoreCase(type)) {
            cbbDescription.setSelectedItem(description);
            cbbDescription.setForeground(Color.BLACK);
            txDescription.setText("");
            txDescription.setVisible(false);
            cbbDescription.setVisible(true);
            System.out.println("Description: " + description);
        }

        try {
            double amount = parseSafeAmount(amountStr);
            txAmount.setText(formatAmountToRupiah(amount));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            txAmount.setText("0");
        }
        System.out.println("Amount: " + amountStr);
        System.out.println("=== Exit DEBUG Mouse Click Table ===" + amountStr);

        txAmount.setForeground(Color.BLACK);
        invisibleButton();
    }//GEN-LAST:event_transactionTableMouseClicked

    private void cbbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbCategoryActionPerformed

    }//GEN-LAST:event_cbbCategoryActionPerformed

    private void excelExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_excelExportMouseClicked
        Date fromDate = null;
        Date toDate = null;

        // Ambil tanggal maksimal dan set range 30 hari ke belakang
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT MAX(transaction_date) AS end_date FROM transaction"); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                toDate = rs.getDate("end_date");

                if (toDate != null) {
                    Date finalToDate = new Date(toDate.getTime());
                    LocalDate toLocal = finalToDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                    LocalDate fromLocal = toLocal.minusDays(29); // total 30 hari
                    fromDate = java.sql.Date.valueOf(fromLocal);
                    toDate = java.sql.Date.valueOf(toLocal);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "❌ Gagal mengambil tanggal dari database.",
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (fromDate == null || toDate == null) {
            JOptionPane.showMessageDialog(this, "📭 Tidak ada transaksi dalam 30 hari terakhir.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (fromDate.after(toDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date!", "Invalid Date Range", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil data
        List<TransactionData> dataToExport = getTransactionData(fromDate, toDate);
        if (dataToExport.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No transactions found in the selected date range.", "No Data", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Format nama file
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String fileName = "Transactions_" + sdf.format(fromDate) + "_to_" + sdf.format(toDate) + ".xlsx";

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Excel File");
        fileChooser.setSelectedFile(new File(fileName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        String filePath = selectedFile.getAbsolutePath();

        if (!filePath.toLowerCase().endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Export ke Excel
            ExcelExporter.exportToExcel(dataToExport, filePath);

            // 2. Simpan ke closing_history
            String insertSQL = "INSERT INTO closing_history (period_start, period_end) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertSQL)) {
                ps.setDate(1, new java.sql.Date(fromDate.getTime()));
                ps.setDate(2, new java.sql.Date(toDate.getTime()));
                ps.executeUpdate();
            }

            // 3. Hapus data transaksi
            String deleteSQL = "DELETE FROM transaction WHERE transaction_date BETWEEN ? AND ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSQL)) {
                ps.setDate(1, new java.sql.Date(fromDate.getTime()));
                ps.setDate(2, new java.sql.Date(toDate.getTime()));
                int deletedRows = ps.executeUpdate();
                System.out.println("Deleted transactions: " + deletedRows);
            }

            // 4 Reset budget ke initial_amount
            String resetSQL = "UPDATE master_budgeting SET amount = initial_amount";
            try (PreparedStatement ps = conn.prepareStatement(resetSQL)) {
                int updated = ps.executeUpdate();
                System.out.println("✅ Budget reset to initial amounts. Rows affected: " + updated);
            }

            conn.commit();

            JOptionPane.showMessageDialog(this,
                    "Transactions successfully exported and cleared.\nSaved to:\n" + filePath,
                    "Export & Clean Success", JOptionPane.INFORMATION_MESSAGE);

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(filePath));
            }

            // Refresh
            loadTransactionData();
            updateRemainingBudget();
            clear();

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error during export/cleanup: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);

            try {
                DatabaseConnection.getConnection().rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Rollback failed: " + rollbackEx.getMessage(),
                        "Critical DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_excelExportMouseClicked

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (transactionTable.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select the transaction data to be deleted!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this transaction data?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Get the selected row index
                int selectedRow = transactionTable.getSelectedRow();

                String transactionIdObj = transactionTable.getValueAt(selectedRow, 6).toString();

                if (transactionIdObj == null) {
                    JOptionPane.showMessageDialog(this, "Unable to retrieve transaction ID. Please refresh the table.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int transactionId = Integer.parseInt(transactionIdObj);

                // Delete from database
                String sql = "DELETE FROM transaction WHERE transaction_id = ?";

                try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, transactionId);

                    int rowsDeleted = ps.executeUpdate();

                    if (rowsDeleted > 0) {
                        // Show success message with icon
                        try {
                            InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
                            if (iIcon != null) {
                                Image iicon = ImageIO.read(iIcon);
                                ImageIcon icon = new ImageIcon(iicon);
                                JOptionPane.showMessageDialog(this,
                                        "Transaction data successfully deleted!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE, icon);
                            } else {
                                JOptionPane.showMessageDialog(this,
                                        "Transaction data successfully deleted!",
                                        "Success",
                                        JOptionPane.INFORMATION_MESSAGE);
                            }
                        } catch (IOException ex) {
                            // If icon loading fails, show message without icon
                            JOptionPane.showMessageDialog(this,
                                    "Transaction data successfully deleted!",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }

                        // Refresh the table and form
                        clear(); // or loadTransactionData() depending on your method name
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "No transaction data was deleted. The record may have already been removed.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Database error occurred while deleting transaction: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ClassCastException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Invalid data type for transaction ID. Please refresh the table.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "An unexpected error occurred: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void cbbDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbbDescriptionActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbbDescriptionActionPerformed

    public JTable getTransactionTable() {
        return transactionTable;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Button btnCancel;
    private component.Button btnDelete;
    private component.Button btnSave;
    private component.Button btnUpdate;
    private javax.swing.JLabel budgetRemaining;
    private component.UnderlineComboBox cbbCategory;
    private component.UnderlineComboBox cbbDescription;
    private component.UnderlineComboBox cbbType;
    private com.toedter.calendar.JDateChooser endDateExport;
    private component.Panel excelExport;
    private com.toedter.calendar.JDateChooser jDateChooser1;
    private com.toedter.calendar.JDayChooser jDayChooser1;
    private com.toedter.calendar.JDayChooser jDayChooser2;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JScrollPane jScrollPane1;
    private component.Panel masterIncomePanel;
    private com.toedter.calendar.JDateChooser startDateExport;
    private com.toedter.calendar.JDateChooser transactionDate;
    private javax.swing.JTable transactionTable;
    private component.TextField txAmount;
    private component.TextField txDescription;
    private component.UnderlineLabel underlineLabel1;
    private component.UnderlineLabel underlineLabel2;
    // End of variables declaration//GEN-END:variables
}
