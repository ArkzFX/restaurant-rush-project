package rush.staff;

/** Seats customers, takes orders and serves READY dishes. One task per turn. */
public class Waiter extends Staff {

    public Waiter() {
        super("Waiter");
        addTask(new SeatCustomerTask());
        addTask(new TakeOrderTask());
        addTask(new ServeOrderTask());
    }
}
