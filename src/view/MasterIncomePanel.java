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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class MasterIncomePanel extends javax.swing.JPanel {

    public MasterIncomePanel() {
        initComponents();
        setBackground(new Color(0, 0, 0, 0));
        tabelIncomeCategory.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        tabelIncomeCategory.setDefaultRenderer(Object.class, new CustomCellRenderer());
        tabelIncomeCategory.getTableHeader().setReorderingAllowed(false);
        tabelIncomeCategory.getTableHeader().setResizingAllowed(false);
        tabelIncomeCategory.getTableHeader().setPreferredSize(new Dimension(0, 30));
        clear();
    }

    private void clear() {
        loadIncomecategory();
        addPlaceholder(txIncomeCategory, "Enter Text");
        invisibleButton();
    }

    private void invisibleButton() {
        int select = tabelIncomeCategory.getSelectedRow();
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

    private void loadIncomecategory() {
        String sql = "SELECT income_id, income_category, created_date FROM master_income";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tabelIncomeCategory.getModel();
            model.setRowCount(0);

            DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            while (rs.next()) {
                String id = rs.getString("income_id");
                String incomeCategory = rs.getString("income_category");
                LocalDate date = rs.getDate("created_date").toLocalDate();
                String Date = date.format(formatterDate);

                model.addRow(new Object[]{
                    id,
                    incomeCategory,
                    Date
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

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        masterIncomePanel = new component.Panel();
        panel3 = new component.Panel();
        panel1 = new component.Panel();
        panel2 = new component.Panel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        txIncomeCategory = new component.TextField();
        btnSave = new component.Button();
        btnCancel = new component.Button();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelIncomeCategory = new javax.swing.JTable();
        btnUpdate = new component.Button();
        btnDelete = new component.Button();

        setBackground(new java.awt.Color(255, 255, 255));

        masterIncomePanel.setBackground(new java.awt.Color(163, 192, 255));
        masterIncomePanel.setRoundBottomRight(35);

        panel3.setBackground(new java.awt.Color(51, 51, 51));
        panel3.setRoundBottomLeft(35);
        panel3.setRoundBottomRight(35);
        panel3.setRoundTopLeft(35);
        panel3.setRoundTopRight(35);
        panel3.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panel3MouseDragged(evt);
            }
        });
        panel3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panel3MousePressed(evt);
            }
        });

        panel1.setBackground(new java.awt.Color(163, 192, 255));
        panel1.setRoundBottomLeft(20);
        panel1.setRoundBottomRight(20);
        panel1.setRoundTopLeft(20);
        panel1.setRoundTopRight(20);

        panel2.setBackground(new java.awt.Color(16, 28, 76));
        panel2.setRoundTopLeft(20);
        panel2.setRoundTopRight(20);

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("Income");

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/Income.png"))); // NOI18N

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8))
        );

        jLabel11.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 0));
        jLabel11.setText("Income Category :");

        txIncomeCategory.setBackground(new java.awt.Color(255, 255, 255, 0));
        txIncomeCategory.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txIncomeCategory.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

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

        tabelIncomeCategory.setBackground(new java.awt.Color(255, 255, 255));
        tabelIncomeCategory.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        tabelIncomeCategory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Income Category", "Created Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                true, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelIncomeCategory.setRowHeight(30);
        tabelIncomeCategory.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabelIncomeCategoryMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabelIncomeCategory);
        if (tabelIncomeCategory.getColumnModel().getColumnCount() > 0) {
            tabelIncomeCategory.getColumnModel().getColumn(0).setMinWidth(0);
            tabelIncomeCategory.getColumnModel().getColumn(0).setMaxWidth(0);
            tabelIncomeCategory.getColumnModel().getColumn(1).setResizable(false);
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

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 891, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(panel1Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addGap(30, 30, 30)
                                .addComponent(txIncomeCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addGap(25, 25, 25))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txIncomeCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(btnDelete, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18))
        );

        javax.swing.GroupLayout panel3Layout = new javax.swing.GroupLayout(panel3);
        panel3.setLayout(panel3Layout);
        panel3Layout.setHorizontalGroup(
            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        panel3Layout.setVerticalGroup(
            panel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout masterIncomePanelLayout = new javax.swing.GroupLayout(masterIncomePanel);
        masterIncomePanel.setLayout(masterIncomePanelLayout);
        masterIncomePanelLayout.setHorizontalGroup(
            masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(panel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(100, 100, 100))
        );
        masterIncomePanelLayout.setVerticalGroup(
            masterIncomePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(masterIncomePanelLayout.createSequentialGroup()
                .addGap(100, 100, 100)
                .addComponent(panel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(100, 100, 100))
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
        String incomeCategory = txIncomeCategory.getText().trim();

        if (incomeCategory.equals("") || incomeCategory.equals("Enter Text")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid income category", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try (Connection connection = DatabaseConnection.getConnection()) {
                InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
                Image iicon = ImageIO.read(iIcon);
                ImageIcon icon = new ImageIcon(iicon);
                String query = "INSERT INTO master_income (income_category) VALUES (?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, incomeCategory);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(null,
                            "Income category saved successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE, icon);
                    clear();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving income category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(MasterIncomePanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        clear();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void panel3MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel3MouseDragged

    }//GEN-LAST:event_panel3MouseDragged

    private void panel3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel3MousePressed

    }//GEN-LAST:event_panel3MousePressed

    private void btnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateActionPerformed
        LocalDate localDate = LocalDate.now();
        Date sqlDate = Date.valueOf(localDate);
        int selected = tabelIncomeCategory.getSelectedRow();
        String income_id = tabelIncomeCategory.getValueAt(selected, 0).toString();
        int id = Integer.parseInt(income_id);
        String sql = "UPDATE master_income set income_category = ?, created_date = ? WHERE income_id = ?";
        String incomeCategory = txIncomeCategory.getText();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, incomeCategory);
            ps.setDate(2, sqlDate);
            ps.setInt(3, id);
            ps.executeUpdate();
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
            clear();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException ex) {
            Logger.getLogger(MasterIncomePanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnUpdateActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        int selected = tabelIncomeCategory.getSelectedRow();

        if (selected == -1) {
            JOptionPane.showMessageDialog(this, "Please select the income data to be deleted!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String income_id = tabelIncomeCategory.getValueAt(selected, 0).toString();
        int id = Integer.parseInt(income_id);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this transaction data?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {

            if (id == 0) {
                JOptionPane.showMessageDialog(this, "Unable to retrieve transaction ID. Please refresh the table.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String sql = "DELETE FROM master_income WHERE income_id = ?";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rowsDeleted = ps.executeUpdate();
                if (rowsDeleted > 0) {
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
                        JOptionPane.showMessageDialog(this,
                                "Transaction data successfully deleted!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    clear();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "No transaction data was deleted. The record may have already been removed.",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void tabelIncomeCategoryMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabelIncomeCategoryMouseClicked
        int selected = tabelIncomeCategory.getSelectedRow();

        // Ambil kategori
        String incomeCategory = tabelIncomeCategory.getValueAt(selected, 1).toString();
        txIncomeCategory.setText(incomeCategory);
        txIncomeCategory.setForeground(Color.BLACK);
        invisibleButton();
    }//GEN-LAST:event_tabelIncomeCategoryMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Button btnCancel;
    private component.Button btnDelete;
    private component.Button btnSave;
    private component.Button btnUpdate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private component.Panel masterIncomePanel;
    private component.Panel panel1;
    private component.Panel panel2;
    private component.Panel panel3;
    private javax.swing.JTable tabelIncomeCategory;
    private component.TextField txIncomeCategory;
    // End of variables declaration//GEN-END:variables
}
