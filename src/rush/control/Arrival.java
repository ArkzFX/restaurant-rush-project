package rush.control;

import rush.entity.Customer;

/**
 * A scheduled arrival: on a given turn one new customer joins the waiting queue.
 * In Stage 1 every customer is a Regular customer, so the arrival only needs the turn.
 */
public class Arrival {

    private final int turn;

    public Arrival(int turn) {
        this.turn = turn;
    }

    public int getTurn() {
        return turn;
    }

    /** Builds the customer for this arrival, passing along the id and the turn they arrive on. */
    public Customer createCustomer(int customerId) {
        return new Customer(customerId, turn);
    }
}
