package rush.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** The menu board: an ordered list of items. The order matters for the customers' choice rule. */
public class Menu {

    private final List<MenuItem> items = new ArrayList<>();

    public Menu(List<MenuItem> items) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("A menu needs at least one item");
        }
        this.items.addAll(items);
    }

    public List<MenuItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    /** Returns the item at the given board position, wrapping around the end of the board. */
    public MenuItem itemAt(int position) {
        return items.get(Math.floorMod(position, items.size()));
    }
}
