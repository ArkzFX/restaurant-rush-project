package rush.entity;

/**
 * The Happy Hour pricing policy. The manager may switch it on once per game at the
 * start of a turn; it lasts for that turn and the next. While active:
 * orders taken get 20% off (after any customer discount, locked when the order is
 * taken), and customers still waiting for a table or food recover 15 satisfaction
 * at the start of each active turn.
 */
public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int RECOVERY_POINTS = 15;
    private static final int PERCENT_PAYABLE = 80;

    private final boolean offered;
    private boolean used;
    private int turnsRemaining;

    /** @param offered whether this game setting includes Happy Hour at all */
    public HappyHour(boolean offered) {
        this.offered = offered;
    }

    public boolean isOffered() {
        return offered;
    }

    public boolean canActivate() {
        return offered && !used;
    }

    public void activate() {
        if (!canActivate()) {
            throw new IllegalStateException("Happy Hour can only be activated once per game");
        }
        used = true;
        turnsRemaining = DURATION_TURNS;
    }

    public boolean isActive() {
        return turnsRemaining > 0;
    }

    /** Active turns left, counting the current one. */
    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    /** Applies the Happy Hour discount to a price if it is active right now. */
    public int apply(int priceCents) {
        return isActive() ? Money.percentOf(priceCents, PERCENT_PAYABLE) : priceCents;
    }

    /** Called once at the end of every turn. */
    void endTurn() {
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
    }
}
