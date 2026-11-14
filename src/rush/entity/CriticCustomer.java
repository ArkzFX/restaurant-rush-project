package rush.entity;

/**
 * A food critic: impatient (loses 12 satisfaction per waiting turn), pays full price,
 * and ignores the usual choice rule to test the kitchen with the most expensive
 * item on the menu. That dish is usually also the slowest to prepare, so a Critic
 * forces the manager to decide whether to prioritise them in the kitchen.
 */
public class CriticCustomer extends Customer {

    private static final int SATISFACTION_LOSS_PER_TURN = 12;

    public CriticCustomer(int id, int arrivalTurn) {
        super(id, arrivalTurn);
    }

    @Override
    public MenuItem chooseItem(Menu menu) {
        return menu.mostExpensive();
    }

    @Override
    public int getSatisfactionLoss() {
        return SATISFACTION_LOSS_PER_TURN;
    }

    @Override
    public String getTypeName() {
        return "Critic";
    }

    @Override
    public String describeTraits() {
        return "loses " + SATISFACTION_LOSS_PER_TURN
                + " satisfaction per waiting turn, pays full price, always orders the most expensive item";
    }
}
