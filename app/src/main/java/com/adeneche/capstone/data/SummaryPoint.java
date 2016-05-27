package com.adeneche.capstone.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.adeneche.capstone.Utils;

/**
 * data point for summary graph
 */
public class SummaryPoint implements Parcelable {
    private final int month;
    private final double expenses;

    public SummaryPoint(int month, double expenses) {
        this.month = month;
        this.expenses = expenses;
    }

    public int getMonth() {
        return month;
    }

    public double getExpenses() {
        return expenses;
    }

    @Override
    public String toString() {
        return Utils.formatMonth(month) + ": " + Utils.formatCurrency(expenses);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(month);
        dest.writeDouble(expenses);
    }

    public static final Parcelable.Creator<SummaryPoint> CREATOR
        = new Parcelable.Creator<SummaryPoint>() {

        @Override
        public SummaryPoint createFromParcel(Parcel source) {
            int month = source.readInt();
            double expenses = source.readDouble();

            return new SummaryPoint(month, expenses);
        }

        @Override
        public SummaryPoint[] newArray(int size) {
            return new SummaryPoint[0];
        }
    };
}
