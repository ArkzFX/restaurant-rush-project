package rush.entity;

/** A patient loyal guest: loses only 5 satisfaction per waiting turn and gets 10% off the bill. */
public class VIPCustomer extends Customer {

    private static final int SATISFACTION_LOSS_PER_TURN = 5;
    private static final int PERCENT_PAYABLE = 90;

    public VIPCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS_PER_TURN;
    }

    @Override
    public int applyDiscount(int priceCents) {
        return Money.percentOf(priceCents, PERCENT_PAYABLE);
    }

    @Override
    public String getTypeName() {
        return "VIP";
    }

    @Override
    public String describeTraits() {
        return "loses " + SATISFACTION_LOSS_PER_TURN + " satisfaction per waiting turn, gets 10% off the bill";
    }
}
