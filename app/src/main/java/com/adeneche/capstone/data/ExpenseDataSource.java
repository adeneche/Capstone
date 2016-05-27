package com.adeneche.capstone.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.adeneche.capstone.data.ExpensesContract.ExpensesDbHelper;
import com.adeneche.capstone.data.ExpensesContract.ExpensesEntry;

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
        ExpensesEntry.COLUMN_NAME_AMOUNT,
        ExpensesEntry.COLUMN_NAME_DESC,
        ExpensesEntry.COLUMN_NAME_MONTH,
        ExpensesEntry.COLUMN_NAME_YEAR
    };

    private static int COLUMN_IDX_ID = 0;
    private static int COLUMN_IDX_AMOUNT = 1;
    private static int COLUMN_IDX_DESC = 2;
    private static int COLUMN_IDX_MONTH = 3;
    private static int COLUMN_IDX_YEAR = 4;

    public ExpenseDataSource(Context context) {
        dbHelper = new ExpensesDbHelper(context);
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
        values.put(ExpensesEntry.COLUMN_NAME_DESC, expense.getDescription());
        values.put(ExpensesEntry.COLUMN_NAME_AMOUNT, expense.getAmount());
        values.put(ExpensesEntry.COLUMN_NAME_MONTH, expense.getMonth());
        values.put(ExpensesEntry.COLUMN_NAME_YEAR, expense.getYear());

        long insertId = database.insert(ExpensesEntry.TABLE_NAME, null, values);
        expense.setId(insertId);
        return expense;
    }

    public void deleteExpense(Expense expense) {
        long id = expense.getId();
        database.delete(ExpensesEntry.TABLE_NAME, ExpensesEntry._ID + " = " + id, null);
    }

    public List<Expense> getAllExpenses(int month, int year) {
        List<Expense> expenses = new ArrayList<>();

        Cursor cursor = database.query(ExpensesEntry.TABLE_NAME, allColumns,
            "month=? and year=?", new String[]{ String.valueOf(month), String.valueOf(year) },
            null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            expenses.add( cursorToExpense(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return expenses;
    }

    public List<SummaryPoint> getExpensesSummary() {
        final List<SummaryPoint> summary = new ArrayList<>();

        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);

        // SELECT month, SUM(amount)
        // FROM expenses
        // GROUP BY month
        // HAVING month > current_month-6
        // ORDER BY month
        Cursor cursor = database.query(ExpensesEntry.TABLE_NAME,
                new String[]{ "month", "SUM(amount)"}, // select clause
                null, null, // where clause
                "month", // group by clause
                "year = " + year + " AND month > " + (month-6), // having clause
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

    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();
        expense.setId(cursor.getInt(COLUMN_IDX_ID));
        expense.setAmount(cursor.getDouble(COLUMN_IDX_AMOUNT));
        expense.setDescription(cursor.getString(COLUMN_IDX_DESC));
        expense.setMonth(cursor.getInt(COLUMN_IDX_MONTH));
        expense.setYear(cursor.getInt(COLUMN_IDX_YEAR));
        return expense;
    }
}
