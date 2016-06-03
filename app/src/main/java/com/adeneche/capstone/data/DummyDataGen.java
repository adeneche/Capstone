package com.adeneche.capstone.data;

import android.content.ContentValues;
import android.content.Context;

import com.adeneche.capstone.data.ExpensesContract.ExpensesEntry;

import java.util.Calendar;
import java.util.Random;

/**
 * Utility class to generate 6 months worth of expenses
 */
public class DummyDataGen {

    private final Context context;
    private final String email;

    public DummyDataGen(Context context, String email) {
        this.context = context;
        this.email = email;
    }

    private void insertExpense(String desc, double amount, int month, int year) {
        ContentValues values = new ContentValues();
        values.put(ExpensesEntry.COLUMN_DESC, desc);
        values.put(ExpensesEntry.COLUMN_AMOUNT, amount);
        values.put(ExpensesEntry.COLUMN_MONTH, month);
        values.put(ExpensesEntry.COLUMN_YEAR, year);
        values.put(ExpensesEntry.COLUMN_EMAIL, email);

        //TODO implement and use bulkInsert
        context.getContentResolver().insert(ExpensesEntry.CONTENT_URI, values);
    }

    private void insertMonth(int month, int year, double totalExpenses) {
        int num = 20;
        double expense = totalExpenses/num;

        for (int i = 0; i < num; i++) {
            insertExpense("expense #"+i, expense, month, year);
        }
    }

    public void generateExpenses() {
        Calendar calendar = Calendar.getInstance();
        Random rng = new Random();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int minExpenses = 1;
        int maxExpenses = 4;

        for (int i = 0; i < 6 && month-i >= 0; i++) {
            insertMonth(month-i, year, 1000*(rng.nextInt(maxExpenses-minExpenses)+minExpenses));
        }
    }
}
