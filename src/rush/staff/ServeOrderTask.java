package rush.staff;

import java.util.List;
import rush.entity.Order;
import rush.entity.Restaurant;

/** Serve one READY order to its table. */
public class ServeOrderTask extends StaffTask {

    public ServeOrderTask() {
        super("Serve a READY order");
    }

    @Override
    public List<Object> candidates(Restaurant restaurant) {
        return asTargets(restaurant.servableOrders());
    }

    @Override
    public String whyUnavailable(Restaurant restaurant) {
        return "No order is READY. A dish the Chef finishes this turn can be served next turn at the earliest.";
    }

    @Override
    public String describe(Object candidate) {
        Order order = (Order) candidate;
        return order.getItem().getName() + " for " + order.getCustomer().label()
                + " at " + order.getCustomer().getTable()
                + ", satisfaction " + order.getCustomer().getSatisfaction();
    }

    @Override
    public String perform(Restaurant restaurant, Object target) {
        Order order = (Order) target;
        restaurant.serve(order);
        return "serves " + order.getItem().getName() + " to " + order.getCustomer().label()
                + ". Order is SERVED.";
    }
}
