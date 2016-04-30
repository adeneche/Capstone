package com.adeneche.capstone;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adeneche.capstone.data.Expense;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements ExpenseFragment.OnExpenseEditedListener {
    private static final String TAG = "MainActivity";

    private static final String EXPENSE_DIALOG_TAG="EXPENSE_DIALOG";
    private static final String SUMMARY_DIALOG_TAG="SUMMARY_DIALOG";

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.expenses_list) ListView mListExpenses;
    @BindView(R.id.search_expense) SearchView mSearchView;
    @BindView(R.id.budget_progress) RoundCornerProgressBar mBudgetBar;

    private final Expense[] data = {
            Expense.to("Amazon Card", 250),
            Expense.to("Car lease", 155),
            Expense.to("Rent", 2145)};
    private ArrayAdapter<Expense> mExpensesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        initListExpenses();
        initSearchView();
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
                });
    }

    private void filterExpenses(String query) {
        mExpensesAdapter.getFilter().filter(query);
    }

    private void initListExpenses() {

        mExpensesAdapter = new ExpenseAdapter(this, R.layout.expenselist_item, data);
        mListExpenses.setAdapter(mExpensesAdapter);
        mListExpenses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FragmentManager fm = getFragmentManager();
                ExpenseFragment dialog = ExpenseFragment.newInstance("", "");
                dialog.show(fm, EXPENSE_DIALOG_TAG);
            }
        });
    }

    @OnClick(R.id.budget_progress)
    public void budgetBarClick() {
        FragmentManager fm = getFragmentManager();
        SummaryFragment dialog = SummaryFragment.newInstance("", "");
        dialog.show(fm, SUMMARY_DIALOG_TAG);
    }

    @OnClick(R.id.fab)
    public void fabClick(FloatingActionButton fab) {
        Snackbar.make(fab, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
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
    public void onDialogOk(String amount, String description) {
        Log.i(TAG, "Expense edited");
    }

    static class ExpenseAdapter extends ArrayAdapter<Expense> {
        int layoutResourceId;

        public ExpenseAdapter(Context context, int resource, Expense[] data) {
            super(context, resource, data);
            layoutResourceId = resource;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            ExpenseHolder holder;

            if (view == null) {
                LayoutInflater inflater = ((Activity) getContext()).getLayoutInflater();
                view = inflater.inflate(layoutResourceId, parent, false);

                holder = new ExpenseHolder(view);
                view.setTag(holder);
            } else {
                holder = (ExpenseHolder) view.getTag();
            }

            final Expense expense = getItem(position);

            holder.description.setText(expense.getDescription());
            holder.amount.setText(expense.getFormattedAmount());
            return view;
        }
    }

    static class ExpenseHolder {
        @BindView(R.id.expense_description) TextView description;
        @BindView(R.id.expense_amount) TextView amount;

        public ExpenseHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
