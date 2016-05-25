package com.adeneche.capstone.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.adeneche.capstone.data.ExpensesContract.ExpensesEntry;

import java.util.Calendar;

/**
 * Utility class to generate 6 months worth of expenses
 */
public class DummyDataGen {

    private final SQLiteDatabase db;

    public DummyDataGen(SQLiteDatabase db) {
        this.db = db;
    }

    private void insertExpense(String desc, double amount, int month, int year) {
        ContentValues values = new ContentValues();
        values.put(ExpensesEntry.COLUMN_NAME_DESC, desc);
        values.put(ExpensesEntry.COLUMN_NAME_AMOUNT, amount);
        values.put(ExpensesEntry.COLUMN_NAME_MONTH, month);
        values.put(ExpensesEntry.COLUMN_NAME_YEAR, year);

        db.insert(ExpensesEntry.TABLE_NAME, null, values);
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
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        double minExpenses = 1000;
        double maxExpenses = 4000;

        for (int i = 0; i < 6; i++) {
            insertMonth(month-i, year, Math.random()*(maxExpenses-minExpenses)+minExpenses);
        }
    }
}
