package com.adeneche.capstone.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.adeneche.capstone.data.ExpensesContract.ExpensesDbHelper;
import com.adeneche.capstone.data.ExpensesContract.ExpensesEntry;
import com.adeneche.capstone.data.pojo.Expense;
import com.adeneche.capstone.data.pojo.SummaryPoint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * This class is our DAO. It maintains the database connection and supports adding new expenses
 * and fetching all expenses
 */
public class ExpenseDataSource {
    // database fields
    private SQLiteDatabase database;
    private ExpensesDbHelper dbHelper;
    private String[] allColumns = {
        ExpensesEntry._ID,
        ExpensesEntry.COLUMN_AMOUNT,
        ExpensesEntry.COLUMN_DESC,
        ExpensesEntry.COLUMN_MONTH,
        ExpensesEntry.COLUMN_YEAR,
        ExpensesEntry.COLUMN_EMAIL
    };

    private final String email;

    public ExpenseDataSource(Context context, String email) {
        dbHelper = new ExpensesDbHelper(context, email);
        this.email = email;
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Expense createExpense(String desc, double amount, long theTime) {
        final Expense expense = Expense.from(desc, amount, theTime);

        ContentValues values = new ContentValues();
        values.put(ExpensesEntry.COLUMN_DESC, expense.getDescription());
        values.put(ExpensesEntry.COLUMN_AMOUNT, expense.getAmount());
        values.put(ExpensesEntry.COLUMN_MONTH, expense.getMonth());
        values.put(ExpensesEntry.COLUMN_YEAR, expense.getYear());
        values.put(ExpensesEntry.COLUMN_EMAIL, email);

        long insertId = database.insert(ExpensesEntry.TABLE_NAME, null, values);
        expense.setId(insertId);
        return expense;
    }

    public void deleteExpense(long id) {
        database.delete(ExpensesEntry.TABLE_NAME, ExpensesEntry._ID + " = " + id, null);
    }

    public Expense getExpense(long id) {
        return Expense.from(database.query(ExpensesEntry.TABLE_NAME, allColumns,
            "_ID = ?", new String[] { String.valueOf(id) },
            null, null, null));
    }

    public Cursor getAllExpenses(int month, int year) {
        return database.query(ExpensesEntry.TABLE_NAME, allColumns,
            "email=? and month=? and year=?",
            new String[]{ email, String.valueOf(month), String.valueOf(year) },
            null, null, null);
    }

    public double getTotalSpent(int month, int year) {
        double total = 0;

        // SELECT SUM(amount)
        // FROM TABLE_NAME
        // GROUP BY email, year, month
        // HAVING email=<email> and year=<year> and month=<month>
        Cursor cursor = database.query(ExpensesEntry.TABLE_NAME,
                new String[]{ "sum(amount)"}, null, null,
                "email, year, month",
                "email=\""+email+"\" AND year="+year+" AND  month="+month,
                null, null);

        cursor.moveToFirst();
        if (!cursor.isAfterLast()) {
            total = cursor.getDouble(0);
        }
        cursor.close();

        return total;
    }

    public List<SummaryPoint> getExpensesSummary() {
        final List<SummaryPoint> summary = new ArrayList<>();

        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);

        // SELECT month, SUM(amount)
        // FROM expenses
        // GROUP BY month
        // HAVING email=<email> and year=<year> and month > <current_month>-6
        // ORDER BY month
        Cursor cursor = database.query(ExpensesEntry.TABLE_NAME,
                new String[]{ "month", "SUM(amount)"}, // select clause
                null, null, // where clause
                "month", // group by clause
                "email=\"" + email + "\"AND year=" + year + " AND month > " + (month-6), // having clause
                "year, month", // order by
                null
                );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            summary.add(new SummaryPoint(cursor.getInt(0), cursor.getDouble(1)));
            cursor.moveToNext();
        }
        cursor.close();

        return summary;
    }
}
