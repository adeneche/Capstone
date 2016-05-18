package com.adeneche.capstone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.adeneche.capstone.data.Expense;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnExpenseEditedListener} interface
 * to handle interaction events.
 * Use the {@link ExpenseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExpenseFragment extends DialogFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DESCRIPTION = "description";
    private static final String ARG_AMOUNT = "amount";

    // TODO: Rename and change types of parameters
    private String mDescription;
    private double mAmount;

    @BindView(R.id.edit_expense_amount) EditText mAmountTxt;
    @BindView(R.id.edit_expense_description) EditText mDescriptionText;

    private OnExpenseEditedListener mListener;

    public ExpenseFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExpenseFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExpenseFragment newInstance(Expense expense) {
        ExpenseFragment fragment = new ExpenseFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DESCRIPTION, expense.getDescription());
        args.putDouble(ARG_AMOUNT, expense.getAmount());
        fragment.setArguments(args);
        return fragment;
    }

    public static ExpenseFragment newInstance() {
        return newInstance(Expense.to("", 0));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDescription = getArguments().getString(ARG_DESCRIPTION);
            mAmount = getArguments().getDouble(ARG_AMOUNT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.fragment_expense, null);

        ButterKnife.bind(this, view);

        mDescriptionText.setText(mDescription);
        mAmountTxt.setText(String.valueOf(mAmount));

        if (OnExpenseEditedListener.class.isInstance(getActivity())) {
            mListener = OnExpenseEditedListener.class.cast(getActivity());
        } else {
            throw new RuntimeException(getActivity().toString()
                    + " must implement OnExpenseEditedListener");
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because it's going in the dialog layout
        builder.setView(view)
        // add action buttons
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO handle invalid input
                    final double amount = Double.parseDouble(mAmountTxt.getText().toString());
                    final String description = mDescriptionText.getText().toString();

                    ((OnExpenseEditedListener) getActivity()).onDialogOk(amount, description);
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ExpenseFragment.this.getDialog().cancel();
                }
            });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnExpenseEditedListener) {
            mListener = (OnExpenseEditedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnExpenseEditedListener");
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
    public interface OnExpenseEditedListener {
        // TODO: Update argument type and name
        void onDialogOk(double amount, String description);
    }
}
