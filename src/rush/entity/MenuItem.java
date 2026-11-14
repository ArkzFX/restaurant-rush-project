package rush.entity;

/**
 * A dish that customers can order. Immutable: price and preparation
 * effort never change once the item is on the menu.
 */
public class MenuItem {

    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceCents, int preparationUnits) {
        if (priceCents < 0 || preparationUnits <= 0) {
            throw new IllegalArgumentException("Invalid menu item: " + name);
        }
        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public String getName() {
        return name;
    }

    public int getPriceCents() {
        return priceCents;
    }

    public int getPreparationUnits() {
        return preparationUnits;
    }

    /** One-line description used on the menu board. */
    public String describe() {
        return String.format("%s %s (%d prep unit%s)", name, Money.format(priceCents),
                preparationUnits, preparationUnits == 1 ? "" : "s");
    }

    @Override
    public String toString() {
        return name;
    }
}
