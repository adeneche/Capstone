package com.adeneche.capstone.data;

import java.sql.Timestamp;
import java.text.NumberFormat;

/**
 * contains a description and the amount of the expense
 */
public class Expense {
    private long id;
    private String description;
    private double amount;
    private Timestamp timestamp;

//    public Expense(final long id, final String description, final double amount) {
//        this.id = id;
//        this.description = description;
//        this.amount = amount;
//    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedAmount() {
        return formatCurency(amount);
    }

    public static String formatCurency(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    public static Expense from(final String description, final double amount) {
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        return expense;
    }

    public static Expense from(long id, String desc, double amount, Timestamp timestamp) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setAmount(amount);
        expense.setDescription(desc);
        expense.setTimestamp(timestamp);
        return expense;
    }

    @Override
    public String toString() {
        return description + " " + amount;
    }
}
