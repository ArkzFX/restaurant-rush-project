package rush.entity;

import java.util.ArrayList;
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
        Restaurant restaurant = restaurantWith(new MenuItem("Burger", 1500, 1), true);
        restaurant.getHappyHour().activate();
        Customer vip = new VIPCustomer(1, 1);
        check(restaurant.quoteBill(vip, restaurant.getMenu().itemAt(0)) == 1080, "VIP Burger in Happy Hour = $10.80");
        check(restaurant.quoteBill(new RegularCustomer(2, 1), restaurant.getMenu().itemAt(0)) == 1200,
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
        Restaurant restaurant = restaurantWith(new MenuItem("Salad", 900, 1), true);
        Customer customer = new RegularCustomer(1, 1);
        restaurant.startTurn(1);
        restaurant.admit(customer);
        restaurant.seat(customer);
        restaurant.getHappyHour().activate();
        Order order = restaurant.takeOrder(customer);
        restaurant.endTurn();
        restaurant.endTurn();
        check(!restaurant.getHappyHour().isActive(), "Happy Hour over");
        check(order.getBillCents() == 720, "bill stays $7.20 after Happy Hour ends");
    }

    /** Section 3.3: one Regular ordering Pasta with the manager's full attention. */
    private static void sectionThreePointThreeTimeline() {
        Restaurant restaurant = restaurantWith(new MenuItem("Pasta", 1800, 2), false);
        Customer customer = new RegularCustomer(1, 1);

        restaurant.startTurn(1);
        restaurant.admit(customer);
        restaurant.seat(customer);
        endTurn(restaurant);
        check(customer.getSatisfaction() == 92, "turn 1: seated, 92");

        restaurant.startTurn(2);
        Order order = restaurant.takeOrder(customer);
        restaurant.prepare(order);
        endTurn(restaurant);
        check(customer.getSatisfaction() == 84, "turn 2: ordered, unit 1/2, 84");

        restaurant.startTurn(3);
        restaurant.prepare(order);
        check(order.getStatus() == OrderStatus.READY, "turn 3: READY");
        check(restaurant.servableOrders().isEmpty(), "turn 3: cannot serve a dish finished this turn");
        endTurn(restaurant);
        check(customer.getSatisfaction() == 76, "turn 3: 76");

        restaurant.startTurn(4);
        restaurant.serve(order);
        endTurn(restaurant);
        check(order.getStatus() == OrderStatus.SERVED && customer.getSatisfaction() == 76, "turn 4: SERVED, 76");

        restaurant.startTurn(5);
        restaurant.finishEating();
        check(customer.getStatus() == CustomerStatus.READY_TO_PAY, "turn 5: READY_TO_PAY");
        check(restaurant.nextCustomerToPay() == null, "turn 5: cannot pay on the turn they became ready");
        endTurn(restaurant);

        restaurant.startTurn(6);
        Customer payer = restaurant.nextCustomerToPay();
        restaurant.collectPayment(payer);
        check(customer.getStatus() == CustomerStatus.PAID && restaurant.getRevenueCents() == 1800, "turn 6: PAID $18.00");
        check(customer.getPaidSatisfaction() == 76, "turn 6: satisfaction 76 recorded");
        check(restaurant.firstFreeTable() != null, "turn 6: table freed");
    }

    private static void dishFinishedThisTurnCannotBeServedThisTurn() {
        Restaurant restaurant = restaurantWith(new MenuItem("Salad", 900, 1), false);
        Customer customer = new RegularCustomer(1, 1);
        restaurant.startTurn(1);
        restaurant.admit(customer);
        restaurant.seat(customer);
        Order order = restaurant.takeOrder(customer);
        restaurant.prepare(order);
        boolean refused = false;
        try {
            restaurant.serve(order);
        } catch (IllegalStateException e) {
            refused = true;
        }
        check(refused, "serving a dish finished this turn is refused");
    }

    private static void abandonmentCancelsOrderAndFreesTable() {
        Restaurant restaurant = restaurantWith(new MenuItem("Salad", 900, 1), false);
        Customer critic = new CriticCustomer(1, 1);
        restaurant.startTurn(1);
        restaurant.admit(critic);
        restaurant.seat(critic);
        Order order = restaurant.takeOrder(critic);
        List<Customer> left = new ArrayList<>();
        for (int turn = 1; turn <= 9 && left.isEmpty(); turn++) {
            restaurant.startTurn(turn);
            left = restaurant.applyWaitingPenalties();
        }
        check(left.contains(critic) && critic.getStatus() == CustomerStatus.LEFT, "Critic leaves after 9 turns");
        check(order.getStatus() == OrderStatus.CANCELLED, "unfinished order cancelled");
        check(restaurant.firstFreeTable() != null, "table freed on departure");
        check(restaurant.getRevenueCents() == 0, "no revenue from an abandoned order");
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

    // helper methods

    private static List<MenuItem> listOf(MenuItem item) {
        List<MenuItem> items = new ArrayList<>();
        items.add(item);
        return items;
    }

    private static Restaurant restaurantWith(MenuItem item, boolean happyHourOffered) {
        return new Restaurant(2, new Menu(listOf(item)), new HappyHour(happyHourOffered));
    }

    private static void endTurn(Restaurant restaurant) {
        restaurant.applyWaitingPenalties();
        restaurant.endTurn();
    }

    private static void check(boolean condition, String description) {
        System.out.println((condition ? "  ok    " : "  FAIL  ") + description);
        if (!condition) {
            failures++;
        }
    }
}
