package rush.staff;

import rush.entity.Customer;
import rush.entity.Money;
import rush.entity.Restaurant;

/** Works automatically: takes at most one payment per turn, oldest READY_TO_PAY customer first. */
public class Cashier extends Staff {

    public Cashier() {
        super("Cashier");
    }

    /** @return a description of the payment taken, or null if nobody could pay */
    public String processPayment(Restaurant restaurant) {
        Customer next = restaurant.nextCustomerToPay();
        if (next == null) {
            return null;
        }
        int bill = restaurant.collectPayment(next);
        return getRole() + " collects " + Money.format(bill) + " from " + next.label()
                + ". PAID with satisfaction " + next.getPaidSatisfaction() + "; table freed.";
    }
}
