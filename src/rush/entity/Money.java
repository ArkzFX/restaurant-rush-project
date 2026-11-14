package rush.entity;

/**
 * Helper for money. All amounts in the game are stored as integer cents
 * so that no floating-point rounding errors can creep into revenue.
 */
public final class Money {

    private Money() {
        // utility class, not instantiable
    }

    /** Formats an amount of cents as dollars with two decimals, e.g. 2430 -> "$24.30". */
    public static String format(int cents) {
        String sign = cents < 0 ? "-" : "";
        int abs = Math.abs(cents);
        return String.format("%s$%d.%02d", sign, abs / 100, abs % 100);
    }
}
