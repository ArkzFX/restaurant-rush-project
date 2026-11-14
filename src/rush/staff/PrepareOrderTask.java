package rush.staff;

import java.util.List;
import rush.entity.Order;
import rush.entity.Restaurant;

/** Advance one order in the kitchen by one preparation unit. */
public class PrepareOrderTask extends StaffTask {

    public PrepareOrderTask() {
        super("Prepare an order (1 unit)");
    }

    @Override
    public List<Object> candidates(Restaurant restaurant) {
        return asTargets(restaurant.ordersInKitchen());
    }

    @Override
    public String whyUnavailable(Restaurant restaurant) {
        return "There is no order in the kitchen. The Waiter must take an order first.";
    }

    @Override
    public String describe(Object candidate) {
        Order order = (Order) candidate;
        return order.getItem().getName() + " for " + order.getCustomer().label()
                + " [" + order.progressLabel() + "], customer satisfaction "
                + order.getCustomer().getSatisfaction();
    }

    @Override
    public String perform(Restaurant restaurant, Object target) {
        Order order = (Order) target;
        boolean finished = restaurant.prepare(order);
        String result = "works on " + order.getItem().getName() + " for "
                + order.getCustomer().label() + ": " + order.progressLabel() + ".";
        if (finished) {
            result += " The dish can be served from next turn.";
        }
        return result;
    }
}
