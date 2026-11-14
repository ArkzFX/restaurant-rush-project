package rush.staff;

import java.util.List;
import rush.entity.Customer;
import rush.entity.CustomerStatus;
import rush.entity.Money;
import rush.entity.Order;
import rush.entity.Restaurant;

/** Take the order of one seated customer. The customer chooses the food. */
public class TakeOrderTask extends StaffTask {

    public TakeOrderTask() {
        super("Take an order");
    }

    @Override
    public List<Object> candidates(Restaurant restaurant) {
        return asTargets(restaurant.customersWithStatus(CustomerStatus.SEATED));
    }

    @Override
    public String whyUnavailable(Restaurant restaurant) {
        return "No seated customer is waiting to order.";
    }

    @Override
    public String describe(Object candidate) {
        Customer customer = (Customer) candidate;
        return customer.label() + " at " + customer.getTable()
                + ", satisfaction " + customer.getSatisfaction();
    }

    @Override
    public String perform(Restaurant restaurant, Object target) {
        Customer customer = (Customer) target;
        Order order = restaurant.takeOrder(customer);
        return "takes the order of " + customer.label() + ": " + order.getItem().describe()
                + ", bill " + Money.format(order.getBillCents()) + ".";
    }
}
