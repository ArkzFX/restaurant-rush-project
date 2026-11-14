package rush.entity;

/** Life cycle of an order. */
public enum OrderStatus {
    /** Taken by the Waiter, no preparation done yet. */
    PLACED,
    /** At least one preparation unit done, but not finished. */
    PREPARING,
    /** All preparation units done; can be served from the next Waiter action on. */
    READY,
    /** Delivered to the table. */
    SERVED,
    /** Bill settled. */
    PAID,
    /** Customer left before being served. */
    CANCELLED;

    /** True while the order still needs work from the Chef. */
    public boolean isInKitchen() {
        return this == PLACED || this == PREPARING;
    }
}
