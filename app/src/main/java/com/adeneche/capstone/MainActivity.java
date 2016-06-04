package com.adeneche.capstone;

import android.app.FragmentManager;
import android.app.LoaderManager;
import android.appwidget.AppWidgetManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.adeneche.capstone.data.ExpensesContract;
import com.adeneche.capstone.data.pojo.SummaryPoint;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity
        implements ExpenseFragment.ExpenseDialogListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "MainActivity";

    private static final String EXPENSE_DIALOG_TAG="EXPENSE_DIALOG";
    private static final String SUMMARY_DIALOG_TAG="SUMMARY_DIALOG";

    public static final String EXTRA_EMAIL = "email";
    public static final String ACTION_ADD_EXPENSE = "add_expense";

    private static final int EXPENSES_LOADER_ID = 1007;
    private static final int SPENT_LOADER_ID = 2008;

    private String mEmail;

    //TODO load this from app properties
    private final double budget = 4000.0;
    private double spent;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.list) ListView mListExpenses;
    @BindView(R.id.search_expense) SearchView mSearchView;

    @BindView(R.id.budget_progress) RoundCornerProgressBar mBudgetBar;
    @BindView(R.id.budget_spent) TextView mBudgetSpent;
    @BindView(R.id.budget_available) TextView mBudgetAvailable;

//    private ExpenseDataSource mDatasource;

    private SimpleCursorAdapter mExpensesAdapter;

    private long edited = -1;

    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mBudgetBar.setMax(5000);

        Intent intent = getIntent();
        boolean addExpense = false;
        if (ACTION_ADD_EXPENSE.equals(intent.getAction())) {
            //TODO fix this by requesting sign in at this point
            mEmail = "adeneche@gmail.com";
            addExpense = true;
        } else {
            mEmail = intent.getStringExtra(EXTRA_EMAIL);
            Log.i(TAG, "Signed in as " + mEmail);
        }

        getLoaderManager().initLoader(EXPENSES_LOADER_ID, null, this);
        getLoaderManager().initLoader(SPENT_LOADER_ID, null, this);

        setupListExpenses();
        initSearchView();

        // Obtain the shared Tracker instance.
        ExpensesApplication application = (ExpensesApplication) getApplication();
        mTracker = application.getDefaultTracker();

        if (addExpense) {
            showAddDialog();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader " + id);
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        if (id == EXPENSES_LOADER_ID) {
            return new CursorLoader(this,
                ExpensesContract.buildGetAllExpensesUri(mEmail, month, year), null, null, null, null);
        } else {
            return new CursorLoader(this,
                ExpensesContract.buildSpent(mEmail, month, year), null, null, null, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished " + loader.getId());
        if (loader.getId() == EXPENSES_LOADER_ID) {
            mExpensesAdapter.swapCursor(data);
        } else {
            if (data.moveToFirst()) {
                spent = data.getDouble(0);
            }
            //TODO should I explicitely close data cursor ?

            mBudgetBar.setProgress((float) spent);
            mBudgetSpent.setText(Utils.formatCurrency(spent));
            mBudgetAvailable.setText(Utils.formatCurrency(budget-spent));

            //TODO use loader to update widget automatically
            updateWidget();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset " + loader.getId());
        if (loader.getId() == EXPENSES_LOADER_ID) {
            mExpensesAdapter.swapCursor(null);
        } else {
            mBudgetBar.setProgress(0);
            mBudgetSpent.setText(Utils.formatCurrency(0));
            mBudgetAvailable.setText(Utils.formatCurrency(budget));
        }
    }

    private void updateWidget() {
        Intent intent = new Intent(this, ExpenseAppWidgetProvider.class);
        intent.setAction(ExpenseAppWidgetProvider.ACTION_UPDATE_TOTAL_SPENT);
        int[] ids = { 0 };
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        intent.putExtra(ExpenseAppWidgetProvider.EXTRA_AMOUNT, spent);
        sendBroadcast(intent);
    }

    private void initSearchView() {
        mSearchView.setOnQueryTextListener(
            new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    mSearchView.clearFocus();
                    return true;
                }
                @Override
                public boolean onQueryTextChange(String query) {
                    mExpensesAdapter.getFilter().filter(query);
                    return true;
                }
            }
        );
    }

    private void setupListExpenses() {
        mBudgetBar.setMax((float) budget);
        mBudgetBar.setProgress(0);
        mBudgetSpent.setText(Utils.formatCurrency(0));
        mBudgetAvailable.setText(Utils.formatCurrency(budget));

        final String[] from = {
                ExpensesContract.ExpensesEntry.COLUMN_DESC,
                ExpensesContract.ExpensesEntry.COLUMN_AMOUNT
        };
        final int[] to = {
                R.id.expense_description,
                R.id.expense_amount
        };

        mExpensesAdapter = new SimpleCursorAdapter(this, R.layout.expenselist_item, null, from, to, 0);
        mListExpenses.setAdapter(mExpensesAdapter);
        mListExpenses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FragmentManager fm = getFragmentManager();
                // TODO just store id and pass it to dialog
                edited = id;
                ExpenseFragment dialog = ExpenseFragment.newInstance(edited);
                dialog.show(fm, EXPENSE_DIALOG_TAG);
            }
        });
    }

    private List<SummaryPoint> getExpensesSummary() {

        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);

        Cursor results = getContentResolver().query(
            ExpensesContract.buildSummaryUri(mEmail, month, year), null, null, null, null);

        List<SummaryPoint> summary = new ArrayList<>();
        if (results != null) {
            results.moveToFirst();
            do {
                summary.add(SummaryPoint.of(results));
            } while (results.moveToNext());
            results.close();
        }

        return summary;
    }

    @OnClick(R.id.budget_progress)
    public void budgetBarClick() {
        FragmentManager fm = getFragmentManager();
        List<SummaryPoint> summary = getExpensesSummary();
        SummaryFragment dialog = SummaryFragment.newInstance(summary.toArray(new SummaryPoint[summary.size()]));
        dialog.show(fm, SUMMARY_DIALOG_TAG);

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Budget")
                .setAction("Click")
                .build());
    }

    @OnClick(R.id.fab)
    public void fabClick() {
        showAddDialog();
    }

    private void showAddDialog() {
        FragmentManager fm = getFragmentManager();
        ExpenseFragment dialog = ExpenseFragment.newInstance();
        dialog.show(fm, EXPENSE_DIALOG_TAG);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOk(double amount, String description) {
        if (edited == -1) {
            Log.i(TAG, "Added new Expense(" + amount + ", " + description + ")");

            Calendar cal = Calendar.getInstance();
            getContentResolver().insert(ExpensesContract.ExpensesEntry.CONTENT_URI,
                    Utils.expenseValues(mEmail, description, amount,
                        cal.get(Calendar.MONTH), cal.get(Calendar.YEAR)));

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Expense")
                    .setAction("Add")
                    .build());
        } else {
            Log.i(TAG, "Edited existing Expense(" + amount + ", " + description + ")");

            ContentValues values = new ContentValues();
            values.put(ExpensesContract.ExpensesEntry.COLUMN_DESC, description);
            values.put(ExpensesContract.ExpensesEntry.COLUMN_AMOUNT, amount);

            getContentResolver().update(ExpensesContract.buildExpenseUri(edited), values, null, null);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Expense")
                    .setAction("Edit")
                    .build());
        }

        edited = -1;
    }

    @Override
    public void onDelete() {
        if (BuildConfig.DEBUG) {
            if (edited == -1) throw new IllegalStateException("We are trying to delete a new expense");
        }

        Log.i(TAG, "Deleting expense");
        getContentResolver().delete(ExpensesContract.buildExpenseUri(edited), null, null);
        //TODO fix this
//        mExpensesAdapter.remove(edited);
        edited = -1;

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Expense")
                .setAction("Delete")
                .build());
    }

    static class ExpenseHolder {
        @BindView(R.id.expense_description) TextView description;
        @BindView(R.id.expense_amount) TextView amount;

        public ExpenseHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
