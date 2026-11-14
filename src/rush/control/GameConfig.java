package rush.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rush.entity.ComboMeal;
import rush.entity.Menu;
import rush.entity.MenuItem;
import rush.staff.Cashier;
import rush.staff.Chef;
import rush.staff.Host;
import rush.staff.Staff;
import rush.staff.Waiter;

/**
 * Everything that defines one game set-up: turns, tables, menu, staff,
 * arrival schedule and objective. New content is registered here, so the
 * turn controller does not change when the restaurant grows.
 */
public class GameConfig {

    private final String name;
    private final int maxTurns;
    private final int tableCount;
    private final Menu menu;
    private final List<Arrival> arrivals;
    private final Objective objective;
    private final boolean hostOnDuty;
    private final boolean happyHourOffered;

    public GameConfig(String name, int maxTurns, int tableCount, Menu menu,
                      List<Arrival> arrivals, Objective objective,
                      boolean hostOnDuty, boolean happyHourOffered) {
        this.name = name;
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = menu;
        this.arrivals = new ArrayList<>(arrivals);
        this.objective = objective;
        this.hostOnDuty = hostOnDuty;
        this.happyHourOffered = happyHourOffered;
    }

    /**
     * The required, reproducible base setting (Section 2.1) with every feature:
     * the Stage 2 customer mix, plus the Host, combo meals and Happy Hour.
     */
    public static GameConfig baseSetting() {
        return new GameConfig("Base setting (Stage 3: Host, combos, Happy Hour)", 18, 2, comboMenu(),
                buildArrivals(mixedCustomers()), standardObjective(), true, true);
    }

    /** The Stage 2 setting (VIP and Critic, no Stage 3 features), kept so earlier runs can be replayed. */
    public static GameConfig stage2Setting() {
        return new GameConfig("Stage 2 setting (VIP and Critic)", 18, 2, standardMenu(),
                buildArrivals(mixedCustomers()), standardObjective(), false, false);
    }

    /** The Stage 1 setting (six Regular customers), kept so earlier runs can be replayed. */
    public static GameConfig stage1Setting() {
        CustomerType[] allRegular = {CustomerType.REGULAR, CustomerType.REGULAR, CustomerType.REGULAR,
                CustomerType.REGULAR, CustomerType.REGULAR, CustomerType.REGULAR};
        return new GameConfig("Stage 1 setting (all Regular)", 18, 2, standardMenu(),
                buildArrivals(allRegular), standardObjective(), false, false);
    }

    /** The Stage 2 customer mix: the 2nd arrival is a Critic and the 4th a VIP, the other four are Regular. */
    private static CustomerType[] mixedCustomers() {
        CustomerType[] mix = {CustomerType.REGULAR, CustomerType.CRITIC, CustomerType.REGULAR,
                CustomerType.VIP, CustomerType.REGULAR, CustomerType.REGULAR};
        return mix;
    }

    /** One customer at the start of turns 1, 4, 7, 10, 13 and 16, in the order given. */
    private static List<Arrival> buildArrivals(CustomerType[] types) {
        int[] arrivalTurns = {1, 4, 7, 10, 13, 16};
        List<Arrival> arrivals = new ArrayList<>();
        for (int i = 0; i < arrivalTurns.length; i++) {
            arrivals.add(new Arrival(arrivalTurns[i], types[i]));
        }
        return arrivals;
    }

    private static Menu standardMenu() {
        // prices are stored in cents, so $9.00 is 900
        List<MenuItem> dishes = new ArrayList<>();
        dishes.add(new MenuItem("Salad", 900, 1));
        dishes.add(new MenuItem("Burger", 1500, 1));
        dishes.add(new MenuItem("Pasta", 1800, 2));
        return new Menu(dishes);
    }

    /**
     * The single dishes followed by two combos. Combos are listed after the dishes, so
     * under the choice rule the 4th and 5th arrivals of each cycle order a combo, and a
     * Critic (who orders the most expensive item) orders the Salad + Pasta Combo.
     */
    private static Menu comboMenu() {
        List<MenuItem> menuBoard = new ArrayList<>(standardMenu().getItems());
        MenuItem salad = menuBoard.get(0);
        MenuItem burger = menuBoard.get(1);
        MenuItem pasta = menuBoard.get(2);
        menuBoard.add(new ComboMeal(salad, burger));
        menuBoard.add(new ComboMeal(salad, pasta));
        return new Menu(menuBoard);
    }

    private static Objective standardObjective() {
        // 4 paid customers, $45.00 (4500 cents), average satisfaction of 60
        return new Objective(4, 4500, 60);
    }

    /** Staff the manager directs each turn, in the order they act. */
    public List<Staff> createAssignableStaff() {
        List<Staff> actingOrder = new ArrayList<>();
        // the Host acts first so the Waiter can take the order of a guest seated this turn
        if (hostOnDuty) {
            actingOrder.add(new Host());
        }
        actingOrder.add(new Waiter());
        actingOrder.add(new Chef());
        return actingOrder;
    }

    public Cashier createCashier() {
        return new Cashier();
    }

    public String getName() {
        return name;
    }

    public int getMaxTurns() {
        return maxTurns;
    }

    public int getTableCount() {
        return tableCount;
    }

    public Menu getMenu() {
        return menu;
    }

    public List<Arrival> getArrivals() {
        return Collections.unmodifiableList(arrivals);
    }

    public Objective getObjective() {
        return objective;
    }

    public boolean isHappyHourOffered() {
        return happyHourOffered;
    }
}
