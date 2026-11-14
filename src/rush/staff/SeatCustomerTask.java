package rush.staff;

import java.util.ArrayList;
import java.util.List;
import rush.entity.Customer;
import rush.entity.Restaurant;
import rush.entity.Table;

/** Seat one queued customer at the first free table. */
public class SeatCustomerTask extends StaffTask {

    public SeatCustomerTask() {
        super("Seat a queued customer");
    }

    @Override
    public List<Object> candidates(Restaurant restaurant) {
        if (restaurant.firstFreeTable() == null) {
            return new ArrayList<Object>();
        }
        return asTargets(restaurant.getWaitingQueue());
    }

    @Override
    public String whyUnavailable(Restaurant restaurant) {
        if (restaurant.getWaitingQueue().isEmpty()) {
            return "Nobody is waiting in the queue.";
        }
        return "Every table is occupied. A table is freed only when its customer pays or leaves.";
    }

    @Override
    public String describe(Object candidate) {
        Customer customer = (Customer) candidate;
        return customer.label() + ", satisfaction " + customer.getSatisfaction()
                + ", waiting since turn " + customer.getArrivalTurn();
    }

    @Override
    public String perform(Restaurant restaurant, Object target) {
        Customer customer = (Customer) target;
        Table table = restaurant.seat(customer);
        return "seats " + customer.label() + " at " + table + ".";
    }
}
