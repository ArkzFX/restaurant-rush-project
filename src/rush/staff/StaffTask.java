package rush.staff;

import java.util.ArrayList;
import java.util.List;
import rush.entity.Restaurant;

/**
 * One kind of job the manager can give a staff member, such as "seat a customer".
 * Each task knows which targets are currently valid, why it cannot be done when
 * there are none, and how to carry it out. The game controller and the UI only
 * work with this abstraction, so new tasks can be added without changing them.
 *
 * <p>A target is either a Customer or an Order depending on the task, so the
 * target methods take a plain Object and each subclass casts it to the type it
 * knows it works with.
 */
public abstract class StaffTask {

    private final String name;

    protected StaffTask(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Copies a list of customers or orders into a list of plain targets. */
    protected static List<Object> asTargets(List<?> items) {
        return new ArrayList<Object>(items);
    }

    /** Targets this task may be applied to right now. Empty means the task is unavailable. */
    public abstract List<Object> candidates(Restaurant restaurant);

    /** Explanation shown to the manager when {@link #candidates} is empty. */
    public abstract String whyUnavailable(Restaurant restaurant);

    /** Label for one candidate in a selection list. */
    public abstract String describe(Object candidate);

    /**
     * Carries out the task on a target taken from {@link #candidates}.
     *
     * @return a message describing what happened
     */
    public abstract String perform(Restaurant restaurant, Object target);
}
