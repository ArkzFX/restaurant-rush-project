package rush.entity;

/** The standard customer: loses 8 satisfaction per waiting turn and pays full price. */
public class RegularCustomer extends Customer {

    private static final int SATISFACTION_LOSS_PER_TURN = 8;

    public RegularCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS_PER_TURN;
    }

    @Override
    public String getTypeName() {
        return "Regular";
    }

    @Override
    public String describeTraits() {
        return "loses " + SATISFACTION_LOSS_PER_TURN + " satisfaction per waiting turn, pays full price";
    }
}
