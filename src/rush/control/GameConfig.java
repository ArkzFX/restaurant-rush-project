package rush.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import rush.entity.Menu;
import rush.entity.MenuItem;
import rush.staff.Cashier;
import rush.staff.Chef;
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

    public GameConfig(String name, int maxTurns, int tableCount, Menu menu,
                      List<Arrival> arrivals, Objective objective) {
        this.name = name;
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.menu = menu;
        this.arrivals = new ArrayList<>(arrivals);
        this.objective = objective;
    }

    /**
     * The required, reproducible base setting (Section 2.1) with the Stage 2 customer mix:
     * the 2nd arrival is a Critic and the 4th a VIP, the other four are Regular.
     */
    public static GameConfig baseSetting() {
        CustomerType[] mix = {CustomerType.REGULAR, CustomerType.CRITIC, CustomerType.REGULAR,
                CustomerType.VIP, CustomerType.REGULAR, CustomerType.REGULAR};
        return new GameConfig("Base setting (Stage 2: VIP and Critic)", 18, 2, standardMenu(),
                buildArrivals(mix), standardObjective());
    }

    /** The Stage 1 setting (six Regular customers), kept so earlier runs can be replayed. */
    public static GameConfig stage1Setting() {
        CustomerType[] allRegular = {CustomerType.REGULAR, CustomerType.REGULAR, CustomerType.REGULAR,
                CustomerType.REGULAR, CustomerType.REGULAR, CustomerType.REGULAR};
        return new GameConfig("Stage 1 setting (all Regular)", 18, 2, standardMenu(),
                buildArrivals(allRegular), standardObjective());
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

    private static Objective standardObjective() {
        // 4 paid customers, $45.00 (4500 cents), average satisfaction of 60
        return new Objective(4, 4500, 60);
    }

    /** Staff the manager directs each turn, in the order they act. */
    public List<Staff> createAssignableStaff() {
        List<Staff> actingOrder = new ArrayList<>();
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
}
