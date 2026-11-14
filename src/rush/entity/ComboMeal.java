package rush.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A combo made of two existing menu items. It is itself a menu item, so customers,
 * orders and the kitchen handle it exactly like a single dish. Its price and
 * preparation effort are derived from its components:
 * units = sum of the components; price = 90% of their combined price, to the nearest cent.
 */
public class ComboMeal extends MenuItem {

    private static final int PERCENT_PAYABLE = 90;

    private final List<MenuItem> components = new ArrayList<>();

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Money.percentOf(first.getPriceCents() + second.getPriceCents(), PERCENT_PAYABLE),
                first.getPreparationUnits() + second.getPreparationUnits());
        components.add(first);
        components.add(second);
    }

    public List<MenuItem> getComponents() {
        return components;
    }

    @Override
    public String describe() {
        int fullPrice = 0;
        for (MenuItem component : components) {
            fullPrice += component.getPriceCents();
        }
        return super.describe() + " [combo: 10% off " + Money.format(fullPrice) + "]";
    }
}
