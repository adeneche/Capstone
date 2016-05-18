package com.adeneche.capstone.data;

import java.text.NumberFormat;

/**
 * contains a description and the amount of the expense
 */
public class Expense {
    private String description;
    private double amount;

    public Expense(final String description, final double amount) {
        this.description = description;
        this.amount = amount;
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

    public String getFormattedAmount() {
        return formatCurency(amount);
    }

    public static String formatCurency(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }

    public static Expense to(final String description, final double amount) {
        return new Expense(description, amount);
    }

    @Override
    public String toString() {
        return description + " " + amount;
    }
}
