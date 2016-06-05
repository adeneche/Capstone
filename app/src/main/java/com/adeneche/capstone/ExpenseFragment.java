package com.adeneche.capstone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.adeneche.capstone.data.ExpensesContract;
import com.adeneche.capstone.data.pojo.Expense;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ExpenseDialogListener} interface
 * to handle interaction events.
 * Use the {@link ExpenseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseFragment extends DialogFragment {
    private static final String ARG_ID = "id";

    private long mId;

    @BindView(R.id.edit_expense_amount) EditText mAmountTxt;
    @BindView(R.id.edit_expense_description) EditText mDescriptionText;

    private ExpenseDialogListener mListener;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExpenseFragment.
     */
    public static ExpenseFragment newInstance(long id) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    public static ExpenseFragment newInstance() {
        return newInstance(-1);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mId = getArguments().getLong(ARG_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_expense, null);

        ButterKnife.bind(this, view);

        if (mId != -1) {
            Cursor cursor = getActivity().getContentResolver()
                    .query(ExpensesContract.buildExpenseUri(mId), null, null, null, null);
            cursor.moveToFirst();
            Expense expense = Expense.from(cursor);

            mDescriptionText.setText(expense.getDescription());
            mAmountTxt.setText(String.valueOf(expense.getAmount()));
        }

        if (ExpenseDialogListener.class.isInstance(getActivity())) {
            mListener = ExpenseDialogListener.class.cast(getActivity());
        } else {
            throw new RuntimeException(getActivity().toString()
                    + " must implement ExpenseDialogListener");
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because it's going in the dialog layout
        builder.setView(view)
        // add action buttons
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final double amount = Double.parseDouble(mAmountTxt.getText().toString());
                    final String description = mDescriptionText.getText().toString();

                    mListener.onOk(amount, description);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ExpenseFragment.this.getDialog().cancel();
                }
            })
            .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mListener.onDelete();
                }
            });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (ExpenseDialogListener.class.isInstance(context)) {
            mListener = ExpenseDialogListener.class.cast(context);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ExpenseDialogListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ExpenseDialogListener {
        void onOk(double amount, String description);
        void onDelete();
    }
}
