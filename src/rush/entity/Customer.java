package rush.entity;

public abstract class Customer {

    public static final int MAX_SATISFACTION = 100;

    private final int id;
    private final int arrivalTurn;
    private int satisfaction = MAX_SATISFACTION;
    private CustomerStatus status = CustomerStatus.WAITING;
    private Table table;
    private Order order;
    private int servedTurn;
    private int readyToPayTurn;
    private int paidSatisfaction;

    protected Customer(int id, int arrivalTurn) {
        this.id = id;
        this.arrivalTurn = arrivalTurn;
    }

    public MenuItem chooseItem(Menu menu) {
        return menu.itemAt(id - 1);
    }

    public abstract int getSatisfactionLoss();

    public int applyDiscount(int priceCents) {
        return priceCents;
    }

    public abstract String getTypeName();

    public abstract String describeTraits();

    void seatAt(Table newTable) {
        requireStatus(CustomerStatus.WAITING);
        newTable.assign(this);
        table = newTable;
        status = CustomerStatus.SEATED;
    }

    void placeOrder(Order newOrder) {
        requireStatus(CustomerStatus.SEATED);
        order = newOrder;
        status = CustomerStatus.ORDERED;
    }

    void receiveFood(int turn) {
        requireStatus(CustomerStatus.ORDERED);
        servedTurn = turn;
        status = CustomerStatus.EATING;
    }

    boolean finishEatingIfDone(int turn) {
        if (status == CustomerStatus.EATING && servedTurn < turn) {
            status = CustomerStatus.READY_TO_PAY;
            readyToPayTurn = turn;
            return true;
        }
        return false;
    }

    public boolean canPayOn(int turn) {
        return status == CustomerStatus.READY_TO_PAY && readyToPayTurn < turn;
    }

    void pay() {
        requireStatus(CustomerStatus.READY_TO_PAY);
        order.markPaid();
        paidSatisfaction = satisfaction;
        status = CustomerStatus.PAID;
        releaseTable();
    }

    /**
     * End-of-turn waiting penalty.
     *
     * @return true if the customer has run out of patience and must leave
     */
    boolean sufferWaitingTurn() {
        if (!status.isAwaitingService()) {
            return false;
        }
        satisfaction = Math.max(0, satisfaction - getSatisfactionLoss());
        return satisfaction == 0;
    }

    /** Happy Hour recovery for a customer still waiting for a table or food. */
    void recover(int points) {
        if (status.isAwaitingService()) {
            satisfaction = Math.min(MAX_SATISFACTION, satisfaction + points);
        }
    }

    void leave() {
        if (order != null) {
            order.cancel();
        }
        status = CustomerStatus.LEFT;
        releaseTable();
    }

    private void releaseTable() {
        if (table != null) {
            table.release();
        }
    }

    private void requireStatus(CustomerStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(label() + " is " + status + ", expected " + expected);
        }
    }

    // queries

    public int getId() {
        return id;
    }

    public int getArrivalTurn() {
        return arrivalTurn;
    }

    public int getSatisfaction() {
        return satisfaction;
    }

    public CustomerStatus getStatus() {
        return status;
    }

    public Table getTable() {
        return table;
    }

    public Order getOrder() {
        return order;
    }

    public int getReadyToPayTurn() {
        return readyToPayTurn;
    }

    /** Satisfaction recorded at the moment of payment. */
    public int getPaidSatisfaction() {
        return paidSatisfaction;
    }

    /** Short label such as "Customer #3 (Regular)". */
    public String label() {
        return "Customer #" + id + " (" + getTypeName() + ")";
    }

    @Override
    public String toString() {
        return label();
    }
}
