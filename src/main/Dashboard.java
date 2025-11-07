package main;

import chart.ModelChart;
import component.CustomCellRenderer;
import component.CustomHeaderRenderer;
import component.Panel;
import config.DatabaseConnection;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import view.MasterBudgetingPanel;
import view.MasterIncomePanel;
import view.MasterTransaction;

public class Dashboard extends javax.swing.JFrame {

    int x, y;
    MasterTransaction mt;
    private boolean isMaximized = false;
    private Rectangle originalBounds = new Rectangle(0, 0, 1251, 724);

    public Dashboard() {
        initComponents();
        setBackground(new Color(0, 0, 0, 0));
        mt = new MasterTransaction();
        mt.loadTransactionData();
        refresh();
        newChartViewAnimation();
        refreshChart();
        setSize(1251, 738);
        setLocationRelativeTo(null);
        tabelSummary.getTableHeader().setDefaultRenderer(new CustomHeaderRenderer());
        tabelSummary.setDefaultRenderer(Object.class, new CustomCellRenderer());
        tabelSummary.getTableHeader().setReorderingAllowed(false);
        tabelSummary.getTableHeader().setResizingAllowed(false);
        tabelSummary.getTableHeader().setPreferredSize(new Dimension(0, 30));
    }

    public void toggleMaximizeWindow() {
        if (isMaximized) {
            restoreWindow();
        } else {
            maximizeWindow();
        }
    }

    public void maximizeWindow() {
        if (!isMaximized) {
            originalBounds = getBounds();
        }

        GraphicsConfiguration gc = getGraphicsConfiguration();
        Rectangle screenBounds = gc.getBounds();
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
        Rectangle maxBounds = new Rectangle(
                screenBounds.x + screenInsets.left,
                screenBounds.y + screenInsets.top,
                screenBounds.width - screenInsets.left - screenInsets.right,
                screenBounds.height - screenInsets.top - screenInsets.bottom
        );

        setBounds(maxBounds);
        isMaximized = true;
    }

    public void restoreWindow() {
        setBounds(originalBounds);
        isMaximized = false;
    }

    public boolean isWindowMaximized() {
        return isMaximized;
    }

    private void refresh() {
        updateBudgetTotals();
        populateSummaryTable();
        setCursorPanel();
        updatePanelBackground(SideDashboard);
    }

    private void setCursorPanel() {
        component.Panel[] panels = {
            SideDashboard,
            SideMasterIncome,
            SideMasterBudgeting,
            SideTransaction,
            about
        };
        for (Panel panel : panels) {
            panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
    }

    private void updatePanelBackground(component.Panel clickPanel) {
        component.Panel[] panels = {
            SideDashboard,
            SideMasterIncome,
            SideMasterBudgeting,
            SideTransaction,
            about
        };
        for (component.Panel pnl : panels) {
            if (pnl == clickPanel) {
                pnl.setBackground(new java.awt.Color(16, 28, 76));
            } else if (pnl == SideDashboard) {
                pnl.setBackground(new java.awt.Color(0, 47, 142));
            } else {
                pnl.setBackground(new java.awt.Color(0, 47, 142));
            }
        }
    }

    private void newChartViewAnimation() {
        // Setup legends
        chart.addLegend("Income", new Color(139, 229, 222));
        chart.addLegend("Expense", new Color(255, 100, 100));
        chart.addLegend("Budgeting", new Color(245, 189, 135));
        loadDefaultChartData();
        // Load data from database
        loadChartDataFromDatabaseSimple();
    }

    /**
     * Method untuk refresh chart dengan data terbaru dari database
     */
    public void refreshChart() {
        chart.clear();
        loadChartDataFromDatabaseSimple();
//        loadDefaultChartData();
        chart.start();
    }

    /**
     * Method untuk mengambil data chart dari database
     */
    /**
     * Alternative method dengan query yang lebih sederhana jika query utama
     * tidak bekerja
     */
    private void loadChartDataFromDatabaseSimple() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            System.out.println("=== DEBUG: Loading chart data from database ===");
            conn = DatabaseConnection.getConnection();

            if (conn == null || conn.isClosed()) {
                System.out.println("ERROR: Database connection failed!");
                loadDefaultChartData();
                return;
            }

            String[] months = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
            double[][] monthlyData = new double[12][3];  // 0: income, 1: expense, 2: budgeting

            // === Income per Month ===
            String incomeQuery = "SELECT MONTH(transaction_date) AS month, SUM(amount) AS total "
                    + "FROM transaction WHERE type = 'Income' AND YEAR(transaction_date) = YEAR(CURDATE()) "
                    + "GROUP BY MONTH(transaction_date)";
            pstmt = conn.prepareStatement(incomeQuery);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("month") - 1;
                if (month >= 0 && month < 12) {
                    monthlyData[month][0] = rs.getDouble("total");
                }
            }
            rs.close();
            pstmt.close();

            // === Expense per Month ===
            String expenseQuery = "SELECT MONTH(transaction_date) AS month, SUM(amount) AS total "
                    + "FROM transaction WHERE type = 'Expense' AND YEAR(transaction_date) = YEAR(CURDATE()) "
                    + "GROUP BY MONTH(transaction_date)";
            pstmt = conn.prepareStatement(expenseQuery);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("month") - 1;
                if (month >= 0 && month < 12) {
                    monthlyData[month][1] = rs.getDouble("total");
                }
            }
            rs.close();
            pstmt.close();

            // === Budgeting per Month ===
            String budgetingQuery = "SELECT MONTH(created_date) AS month, SUM(amount) AS total "
                    + "FROM master_budgeting WHERE YEAR(created_date) = YEAR(CURDATE()) "
                    + "GROUP BY MONTH(created_date)";
            pstmt = conn.prepareStatement(budgetingQuery);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                int month = rs.getInt("month") - 1;
                if (month >= 0 && month < 12) {
                    monthlyData[month][2] = rs.getDouble("total");
                }
            }

            rs.close();
            pstmt.close();

            // === Add Data to Chart ===
            for (int i = 0; i < 12; i++) {
                chart.addData(new ModelChart(months[i], monthlyData[i]));
                System.out.printf("Chart Data - %s: Income=%.2f, Expense=%.2f, Budgeting=%.2f%n",
                        months[i], monthlyData[i][0], monthlyData[i][1], monthlyData[i][2]);
            }

            System.out.println("=== DEBUG: Chart data loaded successfully ===");

        } catch (SQLException e) {
            System.out.println("SQL Error: " + e.getMessage());
            e.printStackTrace();
            loadDefaultChartData();
        } catch (Exception e) {
            System.out.println("General Error: " + e.getMessage());
            e.printStackTrace();
            loadDefaultChartData();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Closing Error: " + e.getMessage());
            }
        }
    }

    /**
     * Method untuk load data default jika database error
     */
    private void loadDefaultChartData() {
        System.out.println("=== DEBUG: Loading default chart data ===");

        chart.addData(new ModelChart("January", new double[]{50, 0, 88}));
        chart.addData(new ModelChart("February", new double[]{60, 75, 15}));
        chart.addData(new ModelChart("March", new double[]{20, 30, 90}));
        chart.addData(new ModelChart("April", new double[]{48, 15, 70}));
        chart.addData(new ModelChart("May", new double[]{50, 40, 15}));
        chart.addData(new ModelChart("June", new double[]{190, 280, 200}));
        chart.addData(new ModelChart("July", new double[]{10, 28, 20}));
        chart.addData(new ModelChart("August", new double[]{1, 2, 2}));
        chart.addData(new ModelChart("September", new double[]{17, 28, 10}));
        chart.addData(new ModelChart("October", new double[]{10, 80, 20}));
        chart.addData(new ModelChart("November", new double[]{19, 28, 20}));
        chart.addData(new ModelChart("December", new double[]{10, 10, 20}));
    }

    /**
     * Method untuk mengisi data summary table dengan total income per category
     * dan persentase - FIXED VERSION
     */
    private void populateSummaryTable() {
        DefaultTableModel model = (DefaultTableModel) tabelSummary.getModel();
        model.setRowCount(0);

        DecimalFormat df = new DecimalFormat("#,###");
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            System.out.println("=== DEBUG: Mencoba koneksi database ===");
            conn = DatabaseConnection.getConnection();

            if (conn == null || conn.isClosed()) {
                System.out.println("ERROR: Koneksi database gagal!");
                model.addRow(new Object[]{"No Connection", "Database Error", "0%"});
                return;
            }

            System.out.println("SUCCESS: Koneksi database berhasil!");

            // Step 1: Hitung total income dari semua transaksi
            String totalIncomeQuery = "SELECT SUM(amount) AS total_income FROM transaction WHERE type = 'Income'";
            pstmt = conn.prepareStatement(totalIncomeQuery);
            rs = pstmt.executeQuery();

            double totalIncome = 0;
            if (rs.next()) {
                totalIncome = rs.getDouble("total_income");
            }

            rs.close();
            pstmt.close();

            // Step 2: Ambil total income per kategori
            String query
                    = "SELECT mi.income_category, COALESCE(SUM(t.amount), 0) AS total_amount "
                    + "FROM master_income mi "
                    + "LEFT JOIN transaction t ON mi.income_id = t.income_id AND t.type = 'Income' "
                    + "GROUP BY mi.income_category "
                    + "ORDER BY total_amount DESC";

            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            int rowCount = 0;
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String category = rs.getString("income_category");
                double totalAmount = rs.getDouble("total_amount");

                double percentage = (totalIncome > 0) ? (totalAmount * 100.0 / totalIncome) : 0.0;

                String formattedAmount = "Rp " + df.format(totalAmount);
                String formattedPercentage = df.format(percentage) + "%";

                model.addRow(new Object[]{formattedAmount, category, formattedPercentage});
                System.out.printf("DEBUG: Row %d => %s | %s | %s%n", ++rowCount, category, formattedAmount, formattedPercentage);
            }

            // Step 3: Jika tidak ada data transaksi
            if (!hasData) {
                rs.close();
                pstmt.close();

                String fallbackQuery = "SELECT income_category FROM master_income ORDER BY income_category";
                pstmt = conn.prepareStatement(fallbackQuery);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    String category = rs.getString("income_category");
                    model.addRow(new Object[]{"Rp 0.00", category, "0.00%"});
                    rowCount++;
                }

                if (rowCount == 0) {
                    model.addRow(new Object[]{"Rp 0.00", "No Categories Found", "0.00%"});
                }
            }

            System.out.println("=== DEBUG: Total baris ditambahkan: " + rowCount + " ===");

        } catch (SQLException e) {
            e.printStackTrace();
            model.addRow(new Object[]{"Database Error: " + e.getMessage(), "Database Error", "0%"});
        } catch (Exception e) {
            e.printStackTrace();
            model.addRow(new Object[]{"System Error: " + e.getMessage(), "System Error", "0%"});
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
                System.out.println("=== DEBUG: Koneksi database ditutup ===");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Method untuk update total budget income dan expense - DENGAN DEBUGGING
     */
    private void updateBudgetTotals() {
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

            DecimalFormat rupiahFormat = new DecimalFormat("###,###,###");
            String formattedBudgetIncome = rupiahFormat.format(remainingBudget);
            String formattedBudgetExpense = rupiahFormat.format(totalExpense);

            txTotalBudgetIncome.setText("Rp" + formattedBudgetIncome);
            txTotalBudgetExpense.setText("Rp" + formattedBudgetExpense);

            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error calculating remaining budget.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panel1 = new component.Panel();
        panel2 = new component.Panel();
        Navbar = new component.Panel();
        close = new javax.swing.JLabel();
        minimize = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        maximized = new javax.swing.JLabel();
        Sidebar = new component.Panel();
        SideDashboard = new component.Panel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        SideMasterIncome = new component.Panel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        SideMasterBudgeting = new component.Panel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        SideTransaction = new component.Panel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        about = new component.Panel();
        jLabel15 = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        MainPanel = new component.Panel();
        DashboardPanel = new component.Panel();
        jLabel12 = new javax.swing.JLabel();
        chart = new chart.Chart();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jLabel19 = new javax.swing.JLabel();
        underlineLabel1 = new component.UnderlineLabel();
        txTotalBudgetIncome = new javax.swing.JLabel();
        txTotalBudgetExpense = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tabelSummary = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        panel1.setBackground(new java.awt.Color(51, 51, 51));
        panel1.setForeground(new java.awt.Color(255, 255, 255));
        panel1.setRoundBottomLeft(35);
        panel1.setRoundBottomRight(35);
        panel1.setRoundTopLeft(35);
        panel1.setRoundTopRight(35);
        panel1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panel1MouseDragged(evt);
            }
        });
        panel1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panel1MousePressed(evt);
            }
        });

        panel2.setBackground(new java.awt.Color(255, 255, 255));
        panel2.setForeground(new java.awt.Color(255, 255, 255));
        panel2.setRoundBottomLeft(35);
        panel2.setRoundBottomRight(35);
        panel2.setRoundTopLeft(35);
        panel2.setRoundTopRight(35);
        panel2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                panel2MouseDragged(evt);
            }
        });
        panel2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                panel2MousePressed(evt);
            }
        });

        Navbar.setBackground(new java.awt.Color(16, 28, 76));
        Navbar.setRoundTopLeft(35);
        Navbar.setRoundTopRight(35);

        close.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/CloseKeren.png"))); // NOI18N
        close.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        close.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                closeMouseClicked(evt);
            }
        });

        minimize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/MinimizeWindow.png"))); // NOI18N
        minimize.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        minimize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                minimizeMouseClicked(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Cash Flow System");

        maximized.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/MaximizeWindow.png"))); // NOI18N
        maximized.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        maximized.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                maximizedMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout NavbarLayout = new javax.swing.GroupLayout(Navbar);
        Navbar.setLayout(NavbarLayout);
        NavbarLayout.setHorizontalGroup(
            NavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, NavbarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 239, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(minimize)
                .addGap(0, 0, 0)
                .addComponent(maximized)
                .addGap(0, 0, 0)
                .addComponent(close)
                .addGap(8, 8, 8))
        );
        NavbarLayout.setVerticalGroup(
            NavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NavbarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NavbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(close, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(minimize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maximized, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        Sidebar.setBackground(new java.awt.Color(0, 47, 142));
        Sidebar.setRoundBottomLeft(35);

        SideDashboard.setBackground(new java.awt.Color(16, 28, 76));
        SideDashboard.setRoundBottomLeft(15);
        SideDashboard.setRoundBottomRight(15);
        SideDashboard.setRoundTopLeft(15);
        SideDashboard.setRoundTopRight(15);
        SideDashboard.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SideDashboardMouseClicked(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/dashboard.png"))); // NOI18N

        jLabel3.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("Dashboard");

        javax.swing.GroupLayout SideDashboardLayout = new javax.swing.GroupLayout(SideDashboard);
        SideDashboard.setLayout(SideDashboardLayout);
        SideDashboardLayout.setHorizontalGroup(
            SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideDashboardLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        SideDashboardLayout.setVerticalGroup(
            SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideDashboardLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );

        SideMasterIncome.setBackground(new java.awt.Color(0, 47, 142));
        SideMasterIncome.setRoundBottomLeft(15);
        SideMasterIncome.setRoundBottomRight(15);
        SideMasterIncome.setRoundTopLeft(15);
        SideMasterIncome.setRoundTopRight(15);
        SideMasterIncome.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SideMasterIncomeMouseClicked(evt);
            }
        });

        jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/Income.png"))); // NOI18N

        jLabel14.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(255, 255, 255));
        jLabel14.setText("Income");

        javax.swing.GroupLayout SideMasterIncomeLayout = new javax.swing.GroupLayout(SideMasterIncome);
        SideMasterIncome.setLayout(SideMasterIncomeLayout);
        SideMasterIncomeLayout.setHorizontalGroup(
            SideMasterIncomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideMasterIncomeLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        SideMasterIncomeLayout.setVerticalGroup(
            SideMasterIncomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideMasterIncomeLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(SideMasterIncomeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );

        SideMasterBudgeting.setBackground(new java.awt.Color(0, 47, 142));
        SideMasterBudgeting.setRoundBottomLeft(15);
        SideMasterBudgeting.setRoundBottomRight(15);
        SideMasterBudgeting.setRoundTopLeft(15);
        SideMasterBudgeting.setRoundTopRight(15);
        SideMasterBudgeting.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SideMasterBudgetingMouseClicked(evt);
            }
        });

        jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/Estimate.png"))); // NOI18N

        jLabel7.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("Budgeting");

        javax.swing.GroupLayout SideMasterBudgetingLayout = new javax.swing.GroupLayout(SideMasterBudgeting);
        SideMasterBudgeting.setLayout(SideMasterBudgetingLayout);
        SideMasterBudgetingLayout.setHorizontalGroup(
            SideMasterBudgetingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideMasterBudgetingLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        SideMasterBudgetingLayout.setVerticalGroup(
            SideMasterBudgetingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideMasterBudgetingLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(SideMasterBudgetingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );

        SideTransaction.setBackground(new java.awt.Color(0, 47, 142));
        SideTransaction.setRoundBottomLeft(15);
        SideTransaction.setRoundBottomRight(15);
        SideTransaction.setRoundTopLeft(15);
        SideTransaction.setRoundTopRight(15);
        SideTransaction.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                SideTransactionMouseClicked(evt);
            }
        });

        jLabel8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/Transaction.png"))); // NOI18N

        jLabel9.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Transaction");

        javax.swing.GroupLayout SideTransactionLayout = new javax.swing.GroupLayout(SideTransaction);
        SideTransaction.setLayout(SideTransactionLayout);
        SideTransactionLayout.setHorizontalGroup(
            SideTransactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideTransactionLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        SideTransactionLayout.setVerticalGroup(
            SideTransactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideTransactionLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(SideTransactionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );

        about.setBackground(new java.awt.Color(0, 47, 142));
        about.setRoundBottomLeft(15);
        about.setRoundBottomRight(15);
        about.setRoundTopLeft(15);
        about.setRoundTopRight(15);
        about.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                aboutMouseClicked(evt);
            }
        });

        jLabel15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconImage/Dev.png"))); // NOI18N

        jLabel16.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(255, 255, 255));
        jLabel16.setText("About");

        javax.swing.GroupLayout aboutLayout = new javax.swing.GroupLayout(about);
        about.setLayout(aboutLayout);
        aboutLayout.setHorizontalGroup(
            aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel15, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE)
                .addContainerGap())
        );
        aboutLayout.setVerticalGroup(
            aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(aboutLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(8, 8, 8))
        );

        javax.swing.GroupLayout SidebarLayout = new javax.swing.GroupLayout(Sidebar);
        Sidebar.setLayout(SidebarLayout);
        SidebarLayout.setHorizontalGroup(
            SidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SidebarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(SidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SideDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SideMasterBudgeting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SideTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SideMasterIncome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(about, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20))
        );
        SidebarLayout.setVerticalGroup(
            SidebarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SidebarLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(SideDashboard, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(SideMasterIncome, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(SideMasterBudgeting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(20, 20, 20)
                .addComponent(SideTransaction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(about, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(25, 25, 25))
        );

        MainPanel.setBackground(new java.awt.Color(163, 192, 255));
        MainPanel.setRoundBottomLeft(35);
        MainPanel.setRoundBottomRight(35);
        MainPanel.setRoundTopLeft(35);
        MainPanel.setRoundTopRight(35);
        MainPanel.setLayout(new java.awt.CardLayout());

        DashboardPanel.setBackground(new java.awt.Color(163, 192, 255));
        DashboardPanel.setRoundBottomRight(35);

        jLabel12.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 0));
        jLabel12.setText("Total Budget Income ");

        chart.setBackground(new java.awt.Color(255,255,255,0)
        );
        chart.setForeground(new java.awt.Color(0, 0, 0));
        chart.setOpaque(false);

        jLabel17.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(0, 0, 0));
        jLabel17.setText(" :");

        jLabel18.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel18.setForeground(new java.awt.Color(0, 0, 0));
        jLabel18.setText("Total Budget Expense ");

        jLabel19.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        jLabel19.setForeground(new java.awt.Color(0, 0, 0));
        jLabel19.setText(" :");

        underlineLabel1.setText("Summary ");
        underlineLabel1.setFont(new java.awt.Font("SansSerif", 1, 18)); // NOI18N

        txTotalBudgetIncome.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        txTotalBudgetIncome.setForeground(new java.awt.Color(0, 0, 0));
        txTotalBudgetIncome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txTotalBudgetIncome.setText("0");

        txTotalBudgetExpense.setFont(new java.awt.Font("SansSerif", 1, 16)); // NOI18N
        txTotalBudgetExpense.setForeground(new java.awt.Color(0, 0, 0));
        txTotalBudgetExpense.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        txTotalBudgetExpense.setText("0");

        tabelSummary.setBackground(new java.awt.Color(255, 255, 255));
        tabelSummary.setFont(new java.awt.Font("SansSerif", 1, 14)); // NOI18N
        tabelSummary.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "Total Income", "Category Income", "Percentage"
            }
        ));
        tabelSummary.setRowHeight(30);
        jScrollPane1.setViewportView(tabelSummary);

        javax.swing.GroupLayout DashboardPanelLayout = new javax.swing.GroupLayout(DashboardPanel);
        DashboardPanel.setLayout(DashboardPanelLayout);
        DashboardPanelLayout.setHorizontalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(chart, javax.swing.GroupLayout.DEFAULT_SIZE, 941, Short.MAX_VALUE)
                    .addGroup(DashboardPanelLayout.createSequentialGroup()
                        .addGap(2, 2, 2)
                        .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(underlineLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(DashboardPanelLayout.createSequentialGroup()
                                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(DashboardPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel12)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel17))
                                    .addGroup(DashboardPanelLayout.createSequentialGroup()
                                        .addComponent(jLabel18)
                                        .addGap(0, 0, 0)
                                        .addComponent(jLabel19)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(txTotalBudgetExpense, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
                                    .addComponent(txTotalBudgetIncome, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGap(80, 80, 80)
                        .addComponent(jScrollPane1)))
                .addGap(23, 23, 23))
        );
        DashboardPanelLayout.setVerticalGroup(
            DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardPanelLayout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(DashboardPanelLayout.createSequentialGroup()
                        .addComponent(underlineLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)
                        .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel17, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txTotalBudgetIncome, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(DashboardPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel18, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel19, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txTotalBudgetExpense, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(chart, javax.swing.GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        MainPanel.add(DashboardPanel, "card2");

        javax.swing.GroupLayout panel2Layout = new javax.swing.GroupLayout(panel2);
        panel2.setLayout(panel2Layout);
        panel2Layout.setHorizontalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel2Layout.createSequentialGroup()
                        .addComponent(Sidebar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(3, 3, 3)
                        .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(Navbar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );
        panel2Layout.setVerticalGroup(
            panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel2Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(Navbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addGroup(panel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(Sidebar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(MainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(5, 5, 5))
        );

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(3, 3, 3))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panel1Layout.createSequentialGroup()
                .addGap(3, 3, 3)
                .addComponent(panel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(3, 3, 3))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void panel1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel1MousePressed

    }//GEN-LAST:event_panel1MousePressed

    private void panel1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel1MouseDragged

    }//GEN-LAST:event_panel1MouseDragged

    private void panel2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel2MouseDragged
        int X = evt.getXOnScreen();
        int Y = evt.getYOnScreen();
        this.setLocation(X - x, Y - y);
    }//GEN-LAST:event_panel2MouseDragged

    private void panel2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panel2MousePressed
        x = evt.getX();
        y = evt.getY();
    }//GEN-LAST:event_panel2MousePressed

    private void minimizeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minimizeMouseClicked
        setExtendedState(JFrame.ICONIFIED);
    }//GEN-LAST:event_minimizeMouseClicked

    private void closeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_closeMouseClicked
        System.exit(0);
    }//GEN-LAST:event_closeMouseClicked

    private void SideDashboardMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SideDashboardMouseClicked
        updatePanelBackground(SideDashboard);
        refreshChart();
        MainPanel.removeAll();
        MainPanel.add(DashboardPanel);
        MainPanel.revalidate();
        MainPanel.repaint();
        mt.loadTransactionData();
        populateSummaryTable();
        updateBudgetTotals();

    }//GEN-LAST:event_SideDashboardMouseClicked

    private void SideMasterBudgetingMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SideMasterBudgetingMouseClicked
        updatePanelBackground(SideMasterBudgeting);
//        new Budgeting().setVisible(true);
        MainPanel.removeAll();
        MainPanel.add(new MasterBudgetingPanel());
        MainPanel.revalidate();
        MainPanel.repaint();
    }//GEN-LAST:event_SideMasterBudgetingMouseClicked

    private void SideTransactionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SideTransactionMouseClicked
        updatePanelBackground(SideTransaction);
        MainPanel.removeAll();
        MainPanel.add(new MasterTransaction());
        MainPanel.revalidate();
        MainPanel.repaint();
    }//GEN-LAST:event_SideTransactionMouseClicked

    private void SideMasterIncomeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_SideMasterIncomeMouseClicked
        updatePanelBackground(SideMasterIncome);
//        new Income().setVisible(true);
        MainPanel.removeAll();
        MainPanel.add(new MasterIncomePanel());
        MainPanel.revalidate();
        MainPanel.repaint();
    }//GEN-LAST:event_SideMasterIncomeMouseClicked

    private void aboutMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_aboutMouseClicked
        updatePanelBackground(about);
        JOptionPane.showMessageDialog(this, "This is made using Java.");
    }//GEN-LAST:event_aboutMouseClicked

    private void maximizedMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maximizedMouseClicked
        toggleMaximizeWindow();
    }//GEN-LAST:event_maximizedMouseClicked

    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Dashboard.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Dashboard().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private component.Panel DashboardPanel;
    private component.Panel MainPanel;
    private component.Panel Navbar;
    private component.Panel SideDashboard;
    private component.Panel SideMasterBudgeting;
    private component.Panel SideMasterIncome;
    private component.Panel SideTransaction;
    private component.Panel Sidebar;
    private component.Panel about;
    private chart.Chart chart;
    private javax.swing.JLabel close;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel maximized;
    private javax.swing.JLabel minimize;
    private component.Panel panel1;
    private component.Panel panel2;
    private javax.swing.JTable tabelSummary;
    private javax.swing.JLabel txTotalBudgetExpense;
    private javax.swing.JLabel txTotalBudgetIncome;
    private component.UnderlineLabel underlineLabel1;
    // End of variables declaration//GEN-END:variables
}
