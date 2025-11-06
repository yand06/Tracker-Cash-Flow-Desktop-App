package component;

import model.TransactionData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ExcelExporter {

    /**
     * Export transactions to Excel file with specific columns: Date, Type,
     * Category, Description, Amount, Budget Amount, Difference
     *
     * @param transactions List of TransactionData objects
     * @param fileName Output file name (.xlsx)
     */
    public static void exportToExcel(List<TransactionData> transactions, String fileName) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Transactions");

        // Format tanggal dan angka
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        DataFormat format = workbook.createDataFormat();
        CellStyle currencyStyle = workbook.createCellStyle();
        currencyStyle.setDataFormat(format.getFormat("#,##0.00"));

        // 1️⃣ Hitung Total Income dan Total Expense
        double totalIncome = 0;
        double totalExpense = 0;

        for (TransactionData t : transactions) {
            if ("Income".equalsIgnoreCase(t.getType())) {
                totalIncome += t.getAmount();
            } else if ("Expense".equalsIgnoreCase(t.getType())) {
                totalExpense += t.getAmount();
            }
        }

        double remainingBudget = totalIncome - totalExpense;

        // 2️⃣ Tulis Remaining Budget di Row 0
        Row topRow = sheet.createRow(0);
        Cell cell = topRow.createCell(0);
        cell.setCellValue("Total Remaining Budget:");
        Cell cell2 = topRow.createCell(1);
        cell2.setCellValue(remainingBudget);
        cell2.setCellStyle(currencyStyle);

        // 3️⃣ Tulis Header di Row 1
        String[] columns = {"Date", "Type", "Category", "Description", "Amount", "Budget Amount", "Difference"};
        Row headerRow = sheet.createRow(1);
        for (int i = 0; i < columns.length; i++) {
            Cell headerCell = headerRow.createCell(i);
            headerCell.setCellValue(columns[i]);
        }

        // 4️⃣ Tulis Data di Row 2 ke bawah
        int rowNum = 2;
        for (TransactionData t : transactions) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(t.getDate() != null ? sdf.format(t.getDate()) : "");
            row.createCell(1).setCellValue(nullToEmpty(t.getType()));
            row.createCell(2).setCellValue(nullToEmpty(t.getCategory()));
            row.createCell(3).setCellValue(nullToEmpty(t.getDescription()));

            Cell amountCell = row.createCell(4);
            amountCell.setCellValue(t.getAmount());
            amountCell.setCellStyle(currencyStyle);

            Cell budgetCell = row.createCell(5);
            budgetCell.setCellValue(t.getBudgetAmount());
            budgetCell.setCellStyle(currencyStyle);

            Cell diffCell = row.createCell(6);
            diffCell.setCellValue(Math.abs(t.getBudgetAmount() - t.getAmount()));
            diffCell.setCellStyle(currencyStyle);
        }

        // Auto-size
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream out = new FileOutputStream(fileName)) {
            workbook.write(out);
            System.out.println("✅ Excel exported to: " + fileName);
        } catch (IOException e) {
            System.err.println("❌ Error exporting to Excel: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                System.err.println("❌ Error closing workbook: " + e.getMessage());
            }
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
