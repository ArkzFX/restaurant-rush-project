package rush.entity;

import java.util.List;

/**
 * Checks the worked examples and timing rules from the brief directly against the
 * domain classes. Plain Java (no test framework needed, so it runs offline).
 * Lives in rush.entity so it can drive the package-private life-cycle methods.
 */
public final class RulesTest {

    private static int failures;

    public static void main(String[] args) {
        comboPriceAndUnits();
        vipBurgerDuringHappyHour();
        happyHourLastsTwoTurnsOncePerGame();
        priceIsLockedWhenOrderIsTaken();
        sectionThreePointThreeTimeline();
        dishFinishedThisTurnCannotBeServedThisTurn();
        abandonmentCancelsOrderAndFreesTable();
        servedCustomersStopLosingSatisfaction();
        moneyFormatting();
        if (failures > 0) {
            System.out.println(failures + " check(s) FAILED");
            System.exit(1);
        }
        System.out.println("All rule checks passed");
    }

    private static void comboPriceAndUnits() {
        ComboMeal combo = new ComboMeal(new MenuItem("Salad", 900, 1), new MenuItem("Pasta", 1800, 2));
        check(combo.getPriceCents() == 2430, "Salad + Pasta combo costs $24.30");
        check(combo.getPreparationUnits() == 3, "Salad + Pasta combo takes 3 units");
    }

    private static void vipBurgerDuringHappyHour() {
        Restaurant r = restaurantWith(new MenuItem("Burger", 1500, 1), true);
        r.getHappyHour().activate();
        Customer vip = new VIPCustomer(1, 1);
        check(r.quoteBill(vip, r.getMenu().itemAt(0)) == 1080, "VIP Burger in Happy Hour = $10.80");
        check(r.quoteBill(new RegularCustomer(2, 1), r.getMenu().itemAt(0)) == 1200,
                "Regular Burger in Happy Hour = $12.00");
    }

    private static void happyHourLastsTwoTurnsOncePerGame() {
        HappyHour hh = new HappyHour(true);
        hh.activate();
        check(hh.isActive() && hh.getTurnsRemaining() == 2, "Happy Hour starts with 2 turns");
        hh.endTurn();
        check(hh.isActive() && hh.getTurnsRemaining() == 1, "Happy Hour still active on the next turn");
        hh.endTurn();
        check(!hh.isActive(), "Happy Hour ends after two turns");
        check(!hh.canActivate(), "Happy Hour cannot be activated twice");
        check(!new HappyHour(false).canActivate(), "Happy Hour unavailable when not offered");
    }

    private static void priceIsLockedWhenOrderIsTaken() {
        Restaurant r = restaurantWith(new MenuItem("Salad", 900, 1), true);
        Customer c = new RegularCustomer(1, 1);
        r.startTurn(1);
        r.admit(c);
        r.seat(c);
        r.getHappyHour().activate();
        Order order = r.takeOrder(c);
        r.endTurn();
        r.endTurn();
        check(!r.getHappyHour().isActive(), "Happy Hour over");
        check(order.getBillCents() == 720, "bill stays $7.20 after Happy Hour ends");
    }

    /** Section 3.3: one Regular ordering Pasta with the manager's full attention. */
    private static void sectionThreePointThreeTimeline() {
        Restaurant r = restaurantWith(new MenuItem("Pasta", 1800, 2), false);
        Customer c = new RegularCustomer(1, 1);

        r.startTurn(1);
        r.admit(c);
        r.seat(c);
        endTurn(r);
        check(c.getSatisfaction() == 92, "turn 1: seated, 92");

        r.startTurn(2);
        Order order = r.takeOrder(c);
        r.prepare(order);
        endTurn(r);
        check(c.getSatisfaction() == 84, "turn 2: ordered, unit 1/2, 84");

        r.startTurn(3);
        r.prepare(order);
        check(order.getStatus() == OrderStatus.READY, "turn 3: READY");
        check(r.servableOrders().isEmpty(), "turn 3: cannot serve a dish finished this turn");
        endTurn(r);
        check(c.getSatisfaction() == 76, "turn 3: 76");

        r.startTurn(4);
        r.serve(order);
        endTurn(r);
        check(order.getStatus() == OrderStatus.SERVED && c.getSatisfaction() == 76, "turn 4: SERVED, 76");

        r.startTurn(5);
        r.finishEating();
        check(c.getStatus() == CustomerStatus.READY_TO_PAY, "turn 5: READY_TO_PAY");
        check(r.nextCustomerToPay().isEmpty(), "turn 5: cannot pay on the turn they became ready");
        endTurn(r);

        r.startTurn(6);
        Customer payer = r.nextCustomerToPay().orElseThrow();
        r.collectPayment(payer);
        check(c.getStatus() == CustomerStatus.PAID && r.getRevenueCents() == 1800, "turn 6: PAID $18.00");
        check(c.getPaidSatisfaction() == 76, "turn 6: satisfaction 76 recorded");
        check(r.firstFreeTable().isPresent(), "turn 6: table freed");
    }

    private static void dishFinishedThisTurnCannotBeServedThisTurn() {
        Restaurant r = restaurantWith(new MenuItem("Salad", 900, 1), false);
        Customer c = new RegularCustomer(1, 1);
        r.startTurn(1);
        r.admit(c);
        r.seat(c);
        Order order = r.takeOrder(c);
        r.prepare(order);
        boolean refused = false;
        try {
            r.serve(order);
        } catch (IllegalStateException e) {
            refused = true;
        }
        check(refused, "serving a dish finished this turn is refused");
    }

    private static void abandonmentCancelsOrderAndFreesTable() {
        Restaurant r = restaurantWith(new MenuItem("Salad", 900, 1), false);
        Customer critic = new CriticCustomer(1, 1);
        r.startTurn(1);
        r.admit(critic);
        r.seat(critic);
        Order order = r.takeOrder(critic);
        List<Customer> left = List.of();
        for (int turn = 1; turn <= 9 && left.isEmpty(); turn++) {
            r.startTurn(turn);
            left = r.applyWaitingPenalties();
        }
        check(left.contains(critic) && critic.getStatus() == CustomerStatus.LEFT, "Critic leaves after 9 turns");
        check(order.getStatus() == OrderStatus.CANCELLED, "unfinished order cancelled");
        check(r.firstFreeTable().isPresent(), "table freed on departure");
        check(r.getRevenueCents() == 0, "no revenue from an abandoned order");
    }

    private static void servedCustomersStopLosingSatisfaction() {
        Customer vip = new VIPCustomer(1, 1);
        vip.sufferWaitingTurn();
        check(vip.getSatisfaction() == 95, "VIP loses 5 per turn");
        Customer critic = new CriticCustomer(2, 1);
        critic.sufferWaitingTurn();
        check(critic.getSatisfaction() == 88, "Critic loses 12 per turn");
    }

    private static void moneyFormatting() {
        check(Money.format(2430).equals("$24.30"), "format $24.30");
        check(Money.format(5).equals("$0.05"), "format $0.05");
        check(Money.percentOf(1944, 80) == 1555, "80% of $19.44 rounds to $15.55");
    }

    // ----- helpers -----

    private static Restaurant restaurantWith(MenuItem item, boolean happyHourOffered) {
        return new Restaurant(2, new Menu(List.of(item)), new HappyHour(happyHourOffered));
    }

    private static void endTurn(Restaurant r) {
        r.applyWaitingPenalties();
        r.endTurn();
    }

    private static void check(boolean condition, String description) {
        System.out.println((condition ? "  ok    " : "  FAIL  ") + description);
        if (!condition) {
            failures++;
        }
    }
}
