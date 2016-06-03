package com.adeneche.capstone;

import android.app.FragmentManager;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
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

import com.adeneche.capstone.data.DummyDataGen;
import com.adeneche.capstone.data.ExpenseDataSource;
import com.adeneche.capstone.data.ExpensesContract;
import com.adeneche.capstone.data.pojo.SummaryPoint;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ExpenseFragment.ExpenseDialogListener {
    private static final String TAG = "MainActivity";

    private static final String EXPENSE_DIALOG_TAG="EXPENSE_DIALOG";
    private static final String SUMMARY_DIALOG_TAG="SUMMARY_DIALOG";

    public static final String EXTRA_EMAIL = "email";
    public static final String ACTION_ADD_EXPENSE = "add_expense";

    //TODO load this from app properties
    private final double budget = 4000.0;
    private double spent;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.list) ListView mListExpenses;
    @BindView(R.id.search_expense) SearchView mSearchView;

    @BindView(R.id.budget_progress) RoundCornerProgressBar mBudgetBar;
    @BindView(R.id.budget_spent) TextView mBudgetSpent;
    @BindView(R.id.budget_available) TextView mBudgetAvailable;

    private ExpenseDataSource mDatasource;

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
        String email;
        boolean addExpense = false;
        if (ACTION_ADD_EXPENSE.equals(intent.getAction())) {
            //TODO fix this by requesting sign in at this point
            email = "adeneche@gmail.com";
            addExpense = true;
        } else {
            email = intent.getStringExtra(EXTRA_EMAIL);
            Log.i(TAG, "Signed in as " + email);
        }

        mDatasource = new ExpenseDataSource(this, email);
        mDatasource.open();

        initListExpenses(email);
        initSearchView();

        // Obtain the shared Tracker instance.
        ExpensesApplication application = (ExpensesApplication) getApplication();
        mTracker = application.getDefaultTracker();

        if (addExpense) {
            showAddDialog();
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
                    filterExpenses(query);
                    return true;
                }
            }
        );
    }

    private void filterExpenses(String query) {
        mExpensesAdapter.getFilter().filter(query);
    }

    private void initListExpenses(String email) {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);

        Cursor cursor = mDatasource.getAllExpenses(month, year);

        if (cursor.getCount() == 0) {
            Log.d(TAG, "Empty DB, adding some dummy data");
            // insert some dummy data
            new DummyDataGen(this, email).generateExpenses();

            cursor = mDatasource.getAllExpenses(month, year);
        }

        spent = mDatasource.getTotalSpent(month, year);

        mBudgetBar.setMax((float) budget);
        mBudgetBar.setProgress((float) spent);
        mBudgetSpent.setText(Utils.formatCurrency(spent));
        mBudgetAvailable.setText(Utils.formatCurrency(budget-spent));

        final String[] from = {
            ExpensesContract.ExpensesEntry.COLUMN_DESC,
            ExpensesContract.ExpensesEntry.COLUMN_AMOUNT
        };
        final int[] to = {
            R.id.expense_description,
            R.id.expense_amount
        };
        mExpensesAdapter = new SimpleCursorAdapter(this, R.layout.expenselist_item, cursor, from, to, 0);
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

    @OnClick(R.id.budget_progress)
    public void budgetBarClick() {
        FragmentManager fm = getFragmentManager();
        List<SummaryPoint> summary = mDatasource.getExpensesSummary();
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
            spent += amount;
            //TODO fix this to use mDatasource
//            Expense expense = mDatasource.createExpense(description, amount, System.currentTimeMillis());
//            mExpensesAdapter.add(expense);

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Expense")
                    .setAction("Add")
                    .build());
        } else {
            Log.i(TAG, "Edited existing Expense(" + amount + ", " + description + ")");
            //TODO update list and budget info
//            spent += amount - edited.getAmount();
//            edited.setAmount(amount);
//            edited.setDescription(description);
            mExpensesAdapter.notifyDataSetChanged();
            edited = -1;

            mTracker.send(new HitBuilders.EventBuilder()
                    .setCategory("Expense")
                    .setAction("Edit")
                    .build());
        }

        mBudgetBar.setProgress((float) spent);
        mBudgetSpent.setText(Utils.formatCurrency(spent));
        mBudgetAvailable.setText(Utils.formatCurrency(budget-spent));

        updateWidget();
    }

    @Override
    public void onDelete() {
        if (BuildConfig.DEBUG) {
            if (edited == -1) throw new IllegalStateException("We are trying to delete a new expense");
        }

        Log.i(TAG, "Deleting expense");
        mDatasource.deleteExpense(edited);
        //TODO fix this to use mDatasource
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
