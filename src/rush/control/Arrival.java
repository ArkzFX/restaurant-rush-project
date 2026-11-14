package rush.control;

import rush.entity.CriticCustomer;
import rush.entity.Customer;
import rush.entity.RegularCustomer;
import rush.entity.VIPCustomer;

/**
 * A scheduled arrival: on a given turn one new customer of a given type joins the
 * waiting queue. This is the only place that turns a type into a customer object, so
 * the turn loop treats every customer the same way.
 */
public class Arrival {

    private final int turn;
    private final CustomerType type;

    public Arrival(int turn, CustomerType type) {
        this.turn = turn;
        this.type = type;
    }

    public int getTurn() {
        return turn;
    }

    /** Builds the customer for this arrival, passing along the id and the turn they arrive on. */
    public Customer createCustomer(int customerId) {
        switch (type) {
            case VIP:
                return new VIPCustomer(customerId, turn);
            case CRITIC:
                return new CriticCustomer(customerId, turn);
            default:
                return new RegularCustomer(customerId, turn);
        }
    }
}
