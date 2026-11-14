package rush.staff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A member of staff. Staff are composed of the tasks they are able to do;
 * the manager can give each staff member at most one of those tasks per turn.
 * Staff with no assignable tasks (the Cashier) work automatically.
 */
public abstract class Staff {

    private final String role;
    private final List<StaffTask> tasks = new ArrayList<>();

    protected Staff(String role) {
        this.role = role;
    }

    protected final void addTask(StaffTask task) {
        tasks.add(task);
    }

    public String getRole() {
        return role;
    }

    /** The tasks the manager may assign to this staff member, in menu order. */
    public List<StaffTask> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    /** Whether the manager chooses this staff member's task each turn. */
    public boolean isAssignable() {
        return !tasks.isEmpty();
    }

    @Override
    public String toString() {
        return role;
    }
}
