package org.cyclops.colossalchests2.client.gui;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Formats counts for display.
 * @author rubensworks
 */
public final class CountFormat {

    private static final String[] SUFFIXES = {"", "K", "M", "B", "T"};
    private static final BigDecimal THOUSAND = BigDecimal.valueOf(1000);

    private CountFormat() {
    }

    /**
     * @return The count with grouping separators, such as "16,777,216".
     */
    public static String full(long count) {
        return NumberFormat.getIntegerInstance(Locale.ROOT).format(count);
    }

    /**
     * @return The count rounded to three significant digits with a suffix, such as "16.8M", or as is below 1000.
     */
    public static String compact(long count) {
        if (Math.abs(count) < 1000) {
            return Long.toString(count);
        }
        BigDecimal value = BigDecimal.valueOf(count);
        int suffix = 0;
        // Divide again when rounding gives 1000, such as 999,999 becoming 1M instead of 1000K.
        while (suffix < SUFFIXES.length - 1 && value.abs().round(new MathContext(3)).compareTo(THOUSAND) >= 0) {
            value = value.divide(THOUSAND);
            suffix++;
        }
        return value.round(new MathContext(3)).stripTrailingZeros().toPlainString() + SUFFIXES[suffix];
    }

}
