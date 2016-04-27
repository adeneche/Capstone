package com.adeneche.capstone;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.adeneche.capstone.data.Expense;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.chart) BarChart mChart;
    @BindView(R.id.expenses_list) ListView mListExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        initChart();
        initListExpenses();
    }

    private void initListExpenses() {
        final Expense[] data= {
                Expense.to("Amazon Card", 250),
                Expense.to("Car lease", 155),
                Expense.to("Rent", 2145)};

        ArrayAdapter<Expense> adapter = new ExpenseAdapter(this, R.layout.expenselist_item, data);
        mListExpenses.setAdapter(adapter);
    }

    private void initChart() {
        mChart.setDescription("chart description");
        mChart.setData(generateChartData(1, 200, 12));
    }

    private BarData generateChartData(int dataSets, float range, int count) {
        final String[] labels = new String[] { "Company A", "Company B", "Company C", "Company D", "Company E", "Company F" };
        ArrayList<IBarDataSet> sets = new ArrayList<IBarDataSet>();

        for(int i = 0; i < dataSets; i++) {

            ArrayList<BarEntry> entries = new ArrayList<BarEntry>();

            for(int j = 0; j < count; j++) {
                entries.add(new BarEntry((float) (Math.random() * range) + range / 4, j));
            }

            BarDataSet ds = new BarDataSet(entries, labels[i]);
            ds.setColors(ColorTemplate.VORDIPLOM_COLORS);
            sets.add(ds);
        }

        BarData d = new BarData(ChartData.generateXVals(0, count), sets);
        return d;
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

    static class ExpenseAdapter extends ArrayAdapter<Expense> {
        int layoutResourceId;
        Expense[] expenses;

        public ExpenseAdapter(Context context, int resource, Expense[] data) {
            super(context, resource, data);
            layoutResourceId = resource;
            expenses = data;
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

            final Expense expense = expenses[position];

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
