package model;

public class TransactionData {

    private java.util.Date date;           // dari transaction.transaction_date
    private String type;                   // dari transaction.type
    private String category;               // dari master_expense.expense_category atau master_income.income_category
    private String description;            // dari transaction.description
    private double amount;                 // dari transaction.amount
    private double budgetAmount;           // dari master_budgeting.amount
    private double difference;             // calculated: budgetAmount - amount

    // Constructor
    public TransactionData(java.util.Date date, String type, String category,
            String description, double amount, double budgetAmount) {
        this.date = date;
        this.type = type;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.budgetAmount = budgetAmount;
        this.difference = budgetAmount - amount;
    }

    // Getters
    public java.util.Date getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public double getDifference() {
        return difference;
    }

}
