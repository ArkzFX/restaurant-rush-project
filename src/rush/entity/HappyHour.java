package rush.entity;


public class HappyHour {

    public static final int DURATION_TURNS = 2;
    public static final int RECOVERY_POINTS = 15;
    private static final int PERCENT_PAYABLE = 80;

    private final boolean offered;
    private boolean used;
    private int turnsRemaining;

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

    public int getTurnsRemaining() {
        return turnsRemaining;
    }

    public int apply(int priceCents) {
        return isActive() ? Money.percentOf(priceCents, PERCENT_PAYABLE) : priceCents;
    }

    void endTurn() {
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
    }
}
