package rush.entity;

/**
 * Helper for money. All amounts in the game are stored as integer cents
 * so that no floating-point rounding errors can creep into revenue.
 */
public final class Money {

    private Money() {
        // utility class, not instantiable
    }

    /**
     * Returns {@code percent}% of a non-negative amount, rounded to the nearest cent
     * (half a cent rounds up). Integer arithmetic only, e.g. 90% of 2700 = 2430.
     */
    public static int percentOf(int cents, int percent) {
        return (cents * percent + 50) / 100;
    }

    /** Formats an amount of cents as dollars with two decimals, e.g. 2430 -> "$24.30". */
    public static String format(int cents) {
        String sign = cents < 0 ? "-" : "";
        int abs = Math.abs(cents);
        return String.format("%s$%d.%02d", sign, abs / 100, abs % 100);
    }
}
