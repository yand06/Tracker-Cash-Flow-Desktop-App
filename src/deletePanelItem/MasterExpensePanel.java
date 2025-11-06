package deletePanelItem;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class MasterExpensePanel extends javax.swing.JPanel {

    public MasterExpensePanel() {
        initComponents();
        setBackground(new Color(0, 0, 0, 0));
        tabelExpenseCategory.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        tabelExpenseCategory.setDefaultRenderer(Object.class, new CustomCellRenderer());
        tabelExpenseCategory.getTableHeader().setReorderingAllowed(false);
        tabelExpenseCategory.getTableHeader().setResizingAllowed(false);
        tabelExpenseCategory.getTableHeader().setPreferredSize(new Dimension(0, 30));
        clear();
    }

    private void clear() {
        loadExpensecategory();
        addPlaceholder(txExpeseCategory, "Enter Text");
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

    private void loadExpensecategory() {
        String sql = "SELECT expense_category, created_date FROM master_expense";
        try {
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) tabelExpenseCategory.getModel();
            model.setRowCount(0);

            DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            while (rs.next()) {
                String a = rs.getString("expense_category");
                LocalDate date = rs.getDate("created_date").toLocalDate();

                String Date = date.format(formatterDate);

                model.addRow(new Object[]{
                    a,
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
        txExpeseCategory = new component.TextField();
        btnSave = new component.Button();
        btnCancel = new component.Button();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelExpenseCategory = new javax.swing.JTable();

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
        jLabel1.setText("Expense");

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
        jLabel11.setText("Expense Category :");

        txExpeseCategory.setBackground(new java.awt.Color(255, 255, 255, 0));
        txExpeseCategory.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        txExpeseCategory.setFont(new java.awt.Font("Sans Serif", 1, 16)); // NOI18N

        btnSave.setBackground(new java.awt.Color(16, 28, 76));
        btnSave.setForeground(new java.awt.Color(255, 255, 255));
        btnSave.setText("Save");
        btnSave.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnCancel.setBackground(new java.awt.Color(16, 28, 76));
        btnCancel.setForeground(new java.awt.Color(255, 255, 255));
        btnCancel.setText("Cancel");
        btnCancel.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });

        tabelExpenseCategory.setBackground(new java.awt.Color(255, 255, 255));
        tabelExpenseCategory.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        tabelExpenseCategory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Expense Category", "Created Date"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tabelExpenseCategory.setRowHeight(30);
        jScrollPane1.setViewportView(tabelExpenseCategory);
        if (tabelExpenseCategory.getColumnModel().getColumnCount() > 0) {
            tabelExpenseCategory.getColumnModel().getColumn(0).setResizable(false);
            tabelExpenseCategory.getColumnModel().getColumn(1).setResizable(false);
        }

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addComponent(jLabel11)
                        .addGap(30, 30, 30)
                        .addComponent(txExpeseCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                        .addGap(302, 302, 302)
                        .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(25, 25, 25))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txExpeseCategory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addGap(25, 25, 25))
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
        String incomeCategory = txExpeseCategory.getText().trim();

        if (incomeCategory.equals("") || incomeCategory.equals("Enter Text")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid income category", "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            try (Connection connection = DatabaseConnection.getConnection()) {
                InputStream iIcon = getClass().getResourceAsStream("/iconImage/berhasil.png");
                Image iicon = ImageIO.read(iIcon);
                ImageIcon icon = new ImageIcon(iicon);
                String query = "INSERT INTO master_expense (expense_category) VALUES (?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setString(1, incomeCategory);
                    statement.executeUpdate();
                    JOptionPane.showMessageDialog(null,
                            "Expense category saved successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE, icon);
                    clear();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error saving income category: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(MasterExpensePanel.class.getName()).log(Level.SEVERE, null, ex);
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

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Button btnCancel;
    private component.Button btnSave;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private component.Panel masterIncomePanel;
    private component.Panel panel1;
    private component.Panel panel2;
    private component.Panel panel3;
    private javax.swing.JTable tabelExpenseCategory;
    private component.TextField txExpeseCategory;
    // End of variables declaration//GEN-END:variables
}
