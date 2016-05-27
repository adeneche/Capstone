package com.adeneche.capstone;

import java.text.DateFormatSymbols;
import java.text.NumberFormat;

/**
 * set of helpful utility methods
 */
public class Utils {
    private static String[] months = new DateFormatSymbols().getMonths();

    public static String formatMonth(int month) {
        return months[month];
    }

    public static String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance().format(amount);
    }
}
