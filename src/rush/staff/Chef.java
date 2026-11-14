package rush.staff;

/** Advances one order by one preparation unit per turn. */
public class Chef extends Staff {

    public Chef() {
        super("Chef");
        addTask(new PrepareOrderTask());
    }
}
