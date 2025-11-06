package view;

import component.CustomCellRenderer;
import component.CustomHeaderRenderer;
import config.DatabaseConnection;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class MasterBudgetingPanel extends javax.swing.JPanel {

    public MasterBudgetingPanel() {
        initComponents();
        setBackground(new Color(0, 0, 0, 0));
        clear();
    }

    private void clear() {
        loadMasterBudgeting();
        addPlaceholder(txExpense, "Enter Text");
        addPlaceholder(txDescription, "Enter Text");
        addPlaceholder(txAmount, "0");
        separatorInput();
        tabelMasterBudgeting.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        tabelMasterBudgeting.setDefaultRenderer(Object.class, new CustomCellRenderer());
        tabelMasterBudgeting.getTableHeader().setReorderingAllowed(false);
        tabelMasterBudgeting.getTableHeader().setResizingAllowed(false);
        tabelMasterBudgeting.getTableHeader().setPreferredSize(new Dimension(0, 30));
        invisibleButton();
    }

    private void invisibleButton() {
        int select = tabelMasterBudgeting.getSelectedRow();
        if (select == -1) {
            btnSave.setEnabled(true);
            btnUpdate.setEnabled(false);
        } else {
            btnSave.setEnabled(false);
            btnUpdate.setEnabled(true);
        }
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
                String input = textField.getText().replaceAll("[^0-9]", "");  // Remove non-numeric characters

                if (input.isEmpty()) {
                    return;
                }

                try {
                    long number = Long.parseLong(input);
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols();
                    symbols.setGroupingSeparator('.');
                    symbols.setDecimalSeparator(',');
                    DecimalFormat formatter = new DecimalFormat("#,###,###,###", symbols);

                    // Use SwingUtilities.invokeLater to update the text field outside the notification thread
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

    private void loadMasterBudgeting() {
        String sql = "SELECT budget_id, expense, expense_description, amount, created_date FROM master_budgeting";

        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            // Clear existing data in table
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tabelMasterBudgeting.getModel();
            model.setRowCount(0);

            // DecimalFormat untuk formatting amount
            DecimalFormat formatter = new DecimalFormat("#,###,###,###");
            DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            // Add data to table
            while (rs.next()) {
                int budgetId = rs.getInt("budget_id");
                String expense = rs.getString("expense");
                String description = rs.getString("expense_description");
                double amount = rs.getDouble("amount");
                LocalDate date = rs.getDate("created_date").toLocalDate();

                String formattedAmount = formatter.format(amount);
                String Date = date.format(formatterDate);

                model.addRow(new Object[]{
                    expense,
                    description,
                    "Rp" + formattedAmount,
                    Date,
                    budgetId
                });
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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

        masterIncomePanel = new component.Panel();
        panel10 = new component.Panel();
        panel11 = new component.Panel();
        panel12 = new component.Panel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        btnSave = new component.Button();
        btnCancel = new component.Button();
        jLabel12 = new javax.swing.JLabel();
        txDescription = new component.TextField();
        jLabel13 = new javax.swing.JLabel();
        txAmount = new component.TextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelMasterBudgeting = new javax.swing.JTable();
        btnDelete = new component.Button();
        btnUpdate = new component.Button();
        txExpense = new component.TextField();

        setBackground(new java.awt.Color(255, 255, 255));

        masterIncomePanel.setBackground(new java.awt.Color(163, 192, 255));
        masterIncomePanel.setRoundBottomRight(35);

        panel10.setBackground(new java.awt.Color(51, 51, 51));
        panel10.setRoundBottomLeft(35);
        panel10.setRoundBottomRight(35);
        panel10.setRoundTopLeft(35);
        panel10.setRoundTopRight(35);
        panel10.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panel10MouseDragged(evt);
            }
        });
        panel10.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panel10MousePressed(evt);
            }
        });

        panel11.setBackground(new java.awt.Color(163, 192, 255));
        panel11.setRoundBottomLeft(20);
        panel11.setRoundBottomRight(20);
        panel11.setRoundTopLeft(20);
        panel11.setRoundTopRight(20);

        panel12.setBackground(new java.awt.Color(16, 28, 76));
        panel12.setRoundTopLeft(20);
        panel12.setRoundTopRight(20);

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Budgeting");

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/Income.png"))); // NOI18N

        javax.swing.GroupLayout panel12Layout = new javax.swing.GroupLayout(panel12);
        panel12.setLayout(panel12Layout);
        panel12Layout.setHorizontalGroup(
            panel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel12Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel12Layout.setVerticalGroup(
            panel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel12Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(panel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Expense :");

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

        jLabel12.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("Description :");

        txDescription.setBackground(new java.awt.Color(255, 255, 255, 0));
        txDescription.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txDescription.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

        jLabel13.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 0));
        jLabel13.setText("Amount :");

        txAmount.setBackground(new java.awt.Color(255, 255, 255, 0));
        txAmount.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txAmount.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

        tabelMasterBudgeting.setBackground(new java.awt.Color(255, 255, 255));
        tabelMasterBudgeting.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Category", "Description", "Amount", "Date", "ID"
            }
        ));
        tabelMasterBudgeting.setRowHeight(30);
        tabelMasterBudgeting.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelMasterBudgetingMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabelMasterBudgeting);
        if (tabelMasterBudgeting.getColumnModel().getColumnCount() > 0) {
            tabelMasterBudgeting.getColumnModel().getColumn(3).setMinWidth(150);
            tabelMasterBudgeting.getColumnModel().getColumn(3).setMaxWidth(150);
            tabelMasterBudgeting.getColumnModel().getColumn(4).setMinWidth(0);
            tabelMasterBudgeting.getColumnModel().getColumn(4).setMaxWidth(0);
        }

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

        txExpense.setBackground(new java.awt.Color(255, 255, 255, 0));
        txExpense.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txExpense.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

        javax.swing.GroupLayout panel11Layout = new javax.swing.GroupLayout(panel11);
        panel11.setLayout(panel11Layout);
        panel11Layout.setHorizontalGroup(
            panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel11Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panel11Layout.createSequentialGroup()
                        .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11)
                            .addComponent(jLabel13))
                        .addGap(72, 72, 72)
                        .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(txExpense, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 718, Short.MAX_VALUE)
                    .addGroup(panel11Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panel11Layout.createSequentialGroup()
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(31, 31, 31))
        );
        panel11Layout.setVerticalGroup(
            panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 293, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
        );

        javax.swing.GroupLayout panel10Layout = new javax.swing.GroupLayout(panel10);
        panel10.setLayout(panel10Layout);
        panel10Layout.setHorizontalGroup(
            panel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel10Layout.setVerticalGroup(
            panel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout masterIncomePanelLayout = new javax.swing.GroupLayout(masterIncomePanel);
        masterIncomePanel.setLayout(masterIncomePanelLayout);
        masterIncomePanelLayout.setHorizontalGroup(
            masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(panel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(100, 100, 100))
        );
        masterIncomePanelLayout.setVerticalGroup(
            masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                .addGap(50, 50, 50)
                .addComponent(panel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(50, 50, 50))
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
        String expense = txExpense.getText();
        String description = txDescription.getText();
        String stringAmount = txAmount.getText().replace(".", "").replace(",", "");
        double amount = Double.parseDouble(stringAmount);

        try {
            Connection conn = DatabaseConnection.getConnection();

            String sql = "INSERT INTO master_budgeting (expense, expense_description, initial_amount, amount) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, expense);
            InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
            Image iicon = ImageIO.read(iIcon);
            ImageIcon icon = new ImageIcon(iicon);

            ps.setString(2, description);
            ps.setDouble(3, amount);
            ps.setDouble(4, amount);

            int result = ps.executeUpdate();
            System.out.println("Data budget berhasil disimpan.");
            if (result > 0) {
                JOptionPane.showMessageDialog(null,
                        "Budgeting Data saved successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE, icon);

                clear();
            } else {
                JOptionPane.showMessageDialog(this, "Error saving data.", "Error", JOptionPane.ERROR_MESSAGE);
            }

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            Logger.getLogger(MasterBudgetingPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        clear();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void panel10MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel10MouseDragged

    }//GEN-LAST:event_panel10MouseDragged

    private void panel10MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel10MousePressed

    }//GEN-LAST:event_panel10MousePressed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (tabelMasterBudgeting.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(this, "Please select the data to be deleted!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Confirm deletion
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this budgeting data?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                // Get the selected row index
                int selectedRow = tabelMasterBudgeting.getSelectedRow();

                // Get the budget_id from the hidden column (index 3)
                // Note: Make sure the ID column is properly populated in loadMasterBudgeting()
                Object budgetIdObj = tabelMasterBudgeting.getValueAt(selectedRow, 4);

                if (budgetIdObj == null) {
                    JOptionPane.showMessageDialog(this, "Unable to retrieve budget ID. Please refresh the table.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int budgetId = (Integer) budgetIdObj;

                // Delete from database (delete from both tables)
                try (Connection conn = DatabaseConnection.getConnection()) {

                    // Start transaction for data consistency
                    conn.setAutoCommit(false);

                    try {
                        // First, delete related transactions
                        String deleteTransactionSql = "DELETE FROM transaction WHERE budget_id = ?";
                        try (PreparedStatement psTransaction = conn.prepareStatement(deleteTransactionSql)) {
                            psTransaction.setInt(1, budgetId);
                            int transactionsDeleted = psTransaction.executeUpdate();

                            // Optional: Log how many related transactions were deleted
                            if (transactionsDeleted > 0) {
                                System.out.println("Deleted " + transactionsDeleted + " related transaction(s)");
                            }
                        }

                        // Then, delete the budget record
                        String deleteBudgetSql = "DELETE FROM master_budgeting WHERE budget_id = ?";
                        try (PreparedStatement psBudget = conn.prepareStatement(deleteBudgetSql)) {
                            psBudget.setInt(1, budgetId);
                            int budgetDeleted = psBudget.executeUpdate();

                            if (budgetDeleted > 0) {
                                // Commit the transaction
                                conn.commit();
                                int rowsDeleted = budgetDeleted; // For compatibility with existing code below

                                if (rowsDeleted > 0) {
                                    // Show success message with icon
                                    try {
                                        InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
                                        if (iIcon != null) {
                                            Image iicon = ImageIO.read(iIcon);
                                            ImageIcon icon = new ImageIcon(iicon);
                                            JOptionPane.showMessageDialog(this,
                                                    "Budgeting data and related transactions successfully deleted!",
                                                    "Success",
                                                    JOptionPane.INFORMATION_MESSAGE, icon);
                                        } else {
                                            JOptionPane.showMessageDialog(this,
                                                    "Budgeting data and related transactions successfully deleted!",
                                                    "Success",
                                                    JOptionPane.INFORMATION_MESSAGE);
                                        }
                                    } catch (IOException ex) {
                                        // If icon loading fails, show message without icon
                                        JOptionPane.showMessageDialog(this,
                                                "Budgeting data and related transactions successfully deleted!",
                                                "Success",
                                                JOptionPane.INFORMATION_MESSAGE);
                                    }

                                    // Refresh the table and form
                                    clear();
                                } else {
                                    // Rollback if budget deletion failed
                                    conn.rollback();
                                    JOptionPane.showMessageDialog(this,
                                            "No budgeting data was deleted. The record may have already been removed.",
                                            "Warning",
                                            JOptionPane.WARNING_MESSAGE);
                                }

                            } else {
                                // Rollback if budget deletion failed
                                conn.rollback();
                                JOptionPane.showMessageDialog(this,
                                        "No budgeting data was deleted. The record may have already been removed.",
                                        "Warning",
                                        JOptionPane.WARNING_MESSAGE);
                            }
                        }

                    } catch (SQLException transactionError) {
                        // Rollback transaction on error
                        try {
                            conn.rollback();
                        } catch (SQLException rollbackError) {
                            rollbackError.printStackTrace();
                        }
                        throw transactionError; // Re-throw to be caught by outer catch block
                    } finally {
                        // Reset auto-commit
                        conn.setAutoCommit(true);
                    }
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Database error occurred while deleting data: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (ClassCastException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Invalid data type for budget ID. Please refresh the table.",
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

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
//      Login update data master_budgeting
        int selectedRow = tabelMasterBudgeting.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a record to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String expense = txExpense.getText().trim();
        String description = txDescription.getText().trim();
        String stringAmount = txAmount.getText().replace(".", "").replace(",", "").trim();

        if (expense.isEmpty() || description.isEmpty() || stringAmount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields before updating.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int budgetId = (int) tabelMasterBudgeting.getValueAt(selectedRow, 4); // ID ada di kolom ke-4 (hidden)
            double amount = Double.parseDouble(stringAmount);

            Connection conn = DatabaseConnection.getConnection();
            String sql = "UPDATE master_budgeting SET expense = ?, expense_description = ?, amount = ? WHERE budget_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, expense);
            ps.setString(2, description);
            ps.setDouble(3, amount);
            ps.setInt(4, budgetId);

            int rowsUpdated = ps.executeUpdate();
            ps.close();
            conn.close();

            if (rowsUpdated > 0) {
                InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
                if (iIcon != null) {
                    Image iicon = ImageIO.read(iIcon);
                    ImageIcon icon = new ImageIcon(iicon);
                    JOptionPane.showMessageDialog(this,
                            "Budgeting data updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE,
                            icon);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Budgeting data updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                clear(); // Refresh table & input field
            } else {
                JOptionPane.showMessageDialog(this, "No records were updated.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Invalid number format for amount.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void tabelMasterBudgetingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelMasterBudgetingMouseClicked
        int selectedRow = tabelMasterBudgeting.getSelectedRow();

        if (selectedRow != -1) {
            String category = tabelMasterBudgeting.getValueAt(selectedRow, 0).toString(); // kategori
            String description = tabelMasterBudgeting.getValueAt(selectedRow, 1).toString(); // deskripsi
            String amountText = tabelMasterBudgeting.getValueAt(selectedRow, 2).toString(); // Rp1.500.000,00

            double amount = parseSafeAmount(amountText); // ✅ aman dari format
            System.out.println("Debug Budgeting -- Amount : " + amount);
            String formatted = formatAmountToRupiah(amount); // 1.500.000
            System.out.println("Debug Budgeting -- Formatted Amount : " + formatted);

            // Set ke form
            txExpense.setText(category);
            txExpense.setForeground(Color.BLACK);

            txDescription.setText(description);
            txDescription.setForeground(Color.BLACK);

            txAmount.setText(formatted);
            txAmount.setForeground(Color.BLACK);
        }
        invisibleButton();
    }//GEN-LAST:event_tabelMasterBudgetingMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Button btnCancel;
    private component.Button btnDelete;
    private component.Button btnSave;
    private component.Button btnUpdate;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private component.Panel masterIncomePanel;
    private component.Panel panel10;
    private component.Panel panel11;
    private component.Panel panel12;
    private javax.swing.JTable tabelMasterBudgeting;
    private component.TextField txAmount;
    private component.TextField txDescription;
    private component.TextField txExpense;
    // End of variables declaration//GEN-END:variables
}
