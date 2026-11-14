package rush.control;

import java.util.ArrayList;
import java.util.List;
import rush.entity.Money;
import rush.entity.Restaurant;

/** The three targets that must all be met at the end of the last turn. */
public class Objective {

    private final int minPaidCustomers;
    private final int minRevenueCents;
    private final double minAverageSatisfaction;

    public Objective(int minPaidCustomers, int minRevenueCents, double minAverageSatisfaction) {
        this.minPaidCustomers = minPaidCustomers;
        this.minRevenueCents = minRevenueCents;
        this.minAverageSatisfaction = minAverageSatisfaction;
    }

    public boolean isMetBy(Restaurant restaurant) {
        return paidMet(restaurant) && revenueMet(restaurant) && satisfactionMet(restaurant);
    }

    private boolean paidMet(Restaurant restaurant) {
        return restaurant.countPaid() >= minPaidCustomers;
    }

    private boolean revenueMet(Restaurant restaurant) {
        return restaurant.getRevenueCents() >= minRevenueCents;
    }

    private boolean satisfactionMet(Restaurant restaurant) {
        // with nobody paid there is no average, which counts as not met
        return restaurant.hasPaidCustomers()
                && restaurant.averagePaidSatisfaction() >= minAverageSatisfaction;
    }

    /** One line per target, marked [x] when met. */
    public List<String> checklist(Restaurant restaurant) {
        // show N/A instead of a number when no customer has paid yet
        String averageText = "N/A";
        if (restaurant.hasPaidCustomers()) {
            averageText = String.format("%.1f", restaurant.averagePaidSatisfaction());
        }
        List<String> lines = new ArrayList<>();
        lines.add(mark(paidMet(restaurant)) + " Paid customers: " + restaurant.countPaid()
                + " (need at least " + minPaidCustomers + ")");
        lines.add(mark(revenueMet(restaurant)) + " Revenue: " + Money.format(restaurant.getRevenueCents())
                + " (need at least " + Money.format(minRevenueCents) + ")");
        lines.add(mark(satisfactionMet(restaurant)) + " Average satisfaction of paid customers: " + averageText
                + " (need at least " + String.format("%.0f", minAverageSatisfaction) + ")");
        return lines;
    }

    /** Short description for the welcome screen. */
    public String describe() {
        return "at least " + minPaidCustomers + " paid customers AND revenue at least "
                + Money.format(minRevenueCents) + " AND average satisfaction of paid customers at least "
                + String.format("%.0f", minAverageSatisfaction);
    }

    private static String mark(boolean targetMet) {
        return targetMet ? "[x]" : "[ ]";
    }
}
