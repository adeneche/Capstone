package com.adeneche.capstone.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public final class ExpensesContract {

    private ExpensesContract() {}

    public static abstract class ExpensesEntry implements BaseColumns {
        public static final String TABLE_NAME = "expenses";
        public static final String COLUMN_NAME_AMOUNT = "amount";
        public static final String COLUMN_NAME_DESC = "desc";
        public static final String COLUMN_NAME_MONTH = "month";
        public static final String COLUMN_NAME_YEAR = "year";
        public static final String COLUMN_NAME_EMAIL = "email";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String REAL_TYPE = " REAL";
    private static final String INT_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_EXPENSES =
        "CREATE TABLE " + ExpensesEntry.TABLE_NAME + " (" +
        ExpensesEntry._ID + " INTEGER PRIMARY KEY," +
        ExpensesEntry.COLUMN_NAME_AMOUNT + REAL_TYPE + COMMA_SEP +
        ExpensesEntry.COLUMN_NAME_DESC + TEXT_TYPE + COMMA_SEP +
        ExpensesEntry.COLUMN_NAME_MONTH + INT_TYPE + COMMA_SEP +
        ExpensesEntry.COLUMN_NAME_YEAR + INT_TYPE + COMMA_SEP +
        ExpensesEntry.COLUMN_NAME_EMAIL + TEXT_TYPE +
        " )";
    private static final String SQL_DELETE_EXPENSES =
        "DROP TABLE IF EXISTS " + ExpensesEntry.TABLE_NAME;

    public static class ExpensesDbHelper extends SQLiteOpenHelper {
        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "Expenses.db";

        private final String email;

        public ExpensesDbHelper(Context context, String email) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            this.email = email;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_EXPENSES);
            // insert some dummy data
            new DummyDataGen(db, email).generateExpenses();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // this is a simple upgrade policy that will simply discard the data and start over
            // if we really want to change the database schema we should have a "real" data migration
            // policy
            Log.w(ExpensesContract.class.getName(),
                "Upgrading database from version " + oldVersion + " to " + newVersion +
                ", which will destroy all old data");
            db.execSQL(SQL_DELETE_EXPENSES);
            onCreate(db);
        }
    }
}
