package com.adeneche.capstone.data;

import com.adeneche.capstone.Utils;

import java.util.Calendar;

/**
 * contains a description and the amount of the expense
 */
public class Expense {
    private long id;
    private String description;
    private double amount;
    private int month;
    private int year;

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

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setTime(long theTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(theTime);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
    }

    public String getFormattedAmount() {
        return Utils.formatCurrency(amount);
    }

    public static Expense from(final String description, final double amount, long theTime) {
        Expense expense = new Expense();
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setTime(theTime);
        return expense;
    }

    public static Expense from(long id, String desc, double amount, long theTime) {
        Expense expense = new Expense();
        expense.setId(id);
        expense.setAmount(amount);
        expense.setDescription(desc);
        expense.setTime(theTime);
        return expense;
    }

    @Override
    public String toString() {
        return description + " " + amount;
    }
}
