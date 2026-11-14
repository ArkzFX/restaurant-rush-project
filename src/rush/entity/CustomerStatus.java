package rush.entity;

/** Where a customer is in the restaurant flow. */
public enum CustomerStatus {
    /** In the waiting queue for a table. */
    WAITING,
    /** At a table, order not yet taken. */
    SEATED,
    /** Order taken, waiting for the food. */
    ORDERED,
    /** Served and eating (takes one full turn). */
    EATING,
    /** Finished eating; waiting for the Cashier. */
    READY_TO_PAY,
    /** Paid and gone. */
    PAID,
    /** Left unhappy without paying. */
    LEFT;

    /** Customers in these states have not been served yet and lose satisfaction every turn. */
    public boolean isAwaitingService() {
        return this == WAITING || this == SEATED || this == ORDERED;
    }

    /** True while the customer is still in the restaurant. */
    public boolean isActive() {
        return this != PAID && this != LEFT;
    }
}
