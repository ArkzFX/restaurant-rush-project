package rush.entity;

/**
 * One customer's order. The bill is locked when the order is taken,
 * so later changes to the menu or pricing never affect it.
 */
public class Order {

    private final Customer customer;
    private final MenuItem item;
    private final int billCents;
    private final int turnTaken;
    private int unitsDone;
    private int turnReady;
    private OrderStatus status = OrderStatus.PLACED;

    Order(Customer customer, MenuItem item, int billCents, int turnTaken) {
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.turnTaken = turnTaken;
    }

    public Customer getCustomer() {
        return customer;
    }

    public MenuItem getItem() {
        return item;
    }

    public int getBillCents() {
        return billCents;
    }

    public int getTurnTaken() {
        return turnTaken;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public int getUnitsDone() {
        return unitsDone;
    }

    /**
     * Whether the Waiter may serve this order now. A dish only counts as ready
     * if it was finished on an earlier turn than the current one.
     */
    public boolean isServableOn(int turn) {
        return status == OrderStatus.READY && turnReady < turn;
    }

    /** Status plus progress, e.g. "PREPARING 1/2". */
    public String progressLabel() {
        if (status.isInKitchen()) {
            return status + " " + unitsDone + "/" + item.getPreparationUnits();
        }
        return status.toString();
    }

    /**
     * The Chef completes one preparation unit.
     *
     * @return true if this unit finished the dish
     */
    boolean prepareOneUnit(int turn) {
        if (!status.isInKitchen()) {
            throw new IllegalStateException("Order is not in the kitchen: " + status);
        }
        unitsDone++;
        if (unitsDone >= item.getPreparationUnits()) {
            status = OrderStatus.READY;
            turnReady = turn;
            return true;
        }
        status = OrderStatus.PREPARING;
        return false;
    }

    void markServed(int turn) {
        if (!isServableOn(turn)) {
            throw new IllegalStateException("Only a dish that was already READY can be served");
        }
        status = OrderStatus.SERVED;
    }

    void markPaid() {
        if (status != OrderStatus.SERVED) {
            throw new IllegalStateException("Only a served order can be paid");
        }
        status = OrderStatus.PAID;
    }

    void cancel() {
        if (status == OrderStatus.SERVED || status == OrderStatus.PAID) {
            throw new IllegalStateException("A served order cannot be cancelled");
        }
        status = OrderStatus.CANCELLED;
    }
}
