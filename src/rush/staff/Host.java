package rush.staff;

/**
 * Greets the queue: seats at most one queued customer per turn, in addition to
 * the Waiter's task. Reuses the same seating task as the Waiter, so the seating
 * rules exist in one place only.
 */
public class Host extends Staff {

    public Host() {
        super("Host");
        addTask(new SeatCustomerTask());
    }
}
