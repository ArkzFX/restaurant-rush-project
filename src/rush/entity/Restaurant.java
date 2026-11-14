package rush.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The restaurant floor: tables, waiting queue, customers and takings.
 * Every change to game state goes through this class so the rules about
 * tables, orders and payment are enforced in one place.
 */
public class Restaurant {

    private final List<Table> tables = new ArrayList<>();
    private final List<Customer> waitingQueue = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final Menu menu;
    private int revenueCents;
    private int currentTurn;

    public Restaurant(int tableCount, Menu menu) {
        for (int i = 1; i <= tableCount; i++) {
            tables.add(new Table(i));
        }
        this.menu = menu;
    }

    public void startTurn(int turn) {
        currentTurn = turn;
    }

    // arrivals and seating

    public void admit(Customer customer) {
        customers.add(customer);
        waitingQueue.add(customer);
    }

    /** @return the first free table, or null if every table is occupied */
    public Table firstFreeTable() {
        for (Table table : tables) {
            if (table.isFree()) {
                return table;
            }
        }
        return null;
    }

    /** Seats a queued customer at the first free table. */
    public Table seat(Customer customer) {
        if (!waitingQueue.contains(customer)) {
            throw new IllegalStateException(customer.label() + " is not in the waiting queue");
        }
        Table table = firstFreeTable();
        if (table == null) {
            throw new IllegalStateException("No free table");
        }
        customer.seatAt(table);
        waitingQueue.remove(customer);
        return table;
    }

    // ordering, kitchen and serving

    /** The customer picks their own food; the bill is fixed now. */
    public Order takeOrder(Customer customer) {
        MenuItem item = customer.chooseItem(menu);
        Order order = new Order(customer, item, quoteBill(customer, item), currentTurn);
        customer.placeOrder(order);
        return order;
    }

    /** Price this customer will pay for an item ordered right now, after their own discount. */
    public int quoteBill(Customer customer, MenuItem item) {
        return customer.applyDiscount(item.getPriceCents());
    }

    /** @return true if this unit made the order READY */
    public boolean prepare(Order order) {
        return order.prepareOneUnit(currentTurn);
    }

    public void serve(Order order) {
        order.markServed(currentTurn);
        order.getCustomer().receiveFood(currentTurn);
    }

    // eating and payment

    /** Customers served on an earlier turn finish eating. Returns those who did. */
    public List<Customer> finishEating() {
        List<Customer> finished = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.finishEatingIfDone(currentTurn)) {
                finished.add(customer);
            }
        }
        return finished;
    }

    /**
     * Finds the customer who has been ready to pay the longest. If two became ready
     * on the same turn, the one who arrived first (lower id) goes first.
     *
     * @return the oldest customer allowed to pay this turn, or null if nobody can pay
     */
    public Customer nextCustomerToPay() {
        Customer oldest = null;
        for (Customer customer : customers) {
            if (!customer.canPayOn(currentTurn)) {
                continue;
            }
            if (oldest == null
                    || customer.getReadyToPayTurn() < oldest.getReadyToPayTurn()
                    || (customer.getReadyToPayTurn() == oldest.getReadyToPayTurn()
                            && customer.getId() < oldest.getId())) {
                oldest = customer;
            }
        }
        return oldest;
    }

    /** Settles the customer's bill and frees the table. Returns the amount collected. */
    public int collectPayment(Customer customer) {
        if (!customer.canPayOn(currentTurn)) {
            throw new IllegalStateException(customer.label() + " cannot pay yet");
        }
        int bill = customer.getOrder().getBillCents();
        customer.pay();
        revenueCents += bill;
        return bill;
    }

    // end of turn

    /**
     * Every customer who has not been served yet loses satisfaction.
     * Those who reach 0 leave without paying.
     *
     * @return the customers who left this turn
     */
    public List<Customer> applyWaitingPenalties() {
        List<Customer> departed = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.sufferWaitingTurn()) {
                customer.leave();
                waitingQueue.remove(customer);
                departed.add(customer);
            }
        }
        return departed;
    }

    // queries used by staff tasks and the UI

    public List<Customer> getWaitingQueue() {
        return Collections.unmodifiableList(waitingQueue);
    }

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(customers);
    }

    public List<Customer> customersWithStatus(CustomerStatus status) {
        List<Customer> matching = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getStatus() == status) {
                matching.add(customer);
            }
        }
        return matching;
    }

    /** Orders that still need work from the Chef. */
    public List<Order> ordersInKitchen() {
        List<Order> inKitchen = new ArrayList<>();
        for (Order order : activeOrders()) {
            if (order.getStatus().isInKitchen()) {
                inKitchen.add(order);
            }
        }
        return inKitchen;
    }

    /** Orders that were already READY before this turn, so the Waiter may serve them. */
    public List<Order> servableOrders() {
        List<Order> servable = new ArrayList<>();
        for (Order order : activeOrders()) {
            if (order.isServableOn(currentTurn)) {
                servable.add(order);
            }
        }
        return servable;
    }

    private List<Order> activeOrders() {
        List<Order> orders = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getStatus() == CustomerStatus.ORDERED) {
                orders.add(customer.getOrder());
            }
        }
        return orders;
    }

    public Menu getMenu() {
        return menu;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }

    public int getRevenueCents() {
        return revenueCents;
    }

    public int countPaid() {
        return customersWithStatus(CustomerStatus.PAID).size();
    }

    public int countUnhappyDepartures() {
        return customersWithStatus(CustomerStatus.LEFT).size();
    }

    /** How many customers can pay this turn (used by the display). */
    public int countReadyToPay() {
        int count = 0;
        for (Customer customer : customers) {
            if (customer.canPayOn(currentTurn)) {
                count++;
            }
        }
        return count;
    }

    /** Customers who have received their food (eating, ready to pay or paid). */
    public int countServed() {
        int count = 0;
        for (Customer customer : customers) {
            CustomerStatus status = customer.getStatus();
            if (status == CustomerStatus.EATING
                    || status == CustomerStatus.READY_TO_PAY
                    || status == CustomerStatus.PAID) {
                count++;
            }
        }
        return count;
    }

    /** True once at least one customer has paid, so an average exists. */
    public boolean hasPaidCustomers() {
        return countPaid() > 0;
    }

    /**
     * Average satisfaction recorded at payment. Only meaningful when
     * {@link #hasPaidCustomers()} is true; returns 0 if nobody has paid.
     */
    public double averagePaidSatisfaction() {
        List<Customer> paid = customersWithStatus(CustomerStatus.PAID);
        if (paid.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (Customer customer : paid) {
            total += customer.getPaidSatisfaction();
        }
        return (double) total / paid.size();
    }
}
