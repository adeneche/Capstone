package com.adeneche.capstone.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.adeneche.capstone.data.ExpensesContract.ExpensesDbHelper;
import com.adeneche.capstone.data.ExpensesContract.ExpensesEntry;

import java.sql.Timestamp;
import java.util.ArrayList;
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
        ExpensesEntry.COLUMN_NAME_DATE
    };

    private static int COLUMN_IDX_ID = 0;
    private static int COLUMN_IDX_AMOUNT = 1;
    private static int COLUMN_IDX_DESC = 2;
    private static int COLUMN_IDX_DATE = 3;

    public ExpenseDataSource(Context context) {
        dbHelper = new ExpensesDbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Expense createExpense(String desc, double amount, Timestamp timestamp) {
        ContentValues values = new ContentValues();
        values.put(ExpensesEntry.COLUMN_NAME_DESC, desc);
        values.put(ExpensesEntry.COLUMN_NAME_AMOUNT, amount);
        values.put(ExpensesEntry.COLUMN_NAME_DATE, timestamp.getTime());

        long insertId = database.insert(ExpensesEntry.TABLE_NAME, null, values);
        return Expense.from(insertId, desc, amount, timestamp);
//        Cursor cursor = database.query(ExpensesEntry.TABLE_NAME, allColumns,
//            ExpensesEntry._ID + " = " + insertId, null, null, null, null);
//        cursor.moveToFirst();
//        Expense expense = cursorToExpense(cursor);
//        cursor.close();
//        return expense;
    }

    public void deleteExpense(Expense expense) {
        long id = expense.getId();
        database.delete(ExpensesEntry.TABLE_NAME, ExpensesEntry._ID + " = " + id, null);
    }

    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();

        Cursor cursor = database.query(ExpensesEntry.TABLE_NAME, allColumns,
            null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            expenses.add( cursorToExpense(cursor));
            cursor.moveToNext();
        }

        cursor.close();
        return expenses;
    }

    private Expense cursorToExpense(Cursor cursor) {
        Expense expense = new Expense();
        expense.setId(cursor.getInt(COLUMN_IDX_ID));
        expense.setAmount(cursor.getDouble(COLUMN_IDX_AMOUNT));
        expense.setDescription(cursor.getString(COLUMN_IDX_DESC));
        expense.setTimestamp(new Timestamp(cursor.getLong(COLUMN_IDX_DATE)));
        return expense;
    }
}
