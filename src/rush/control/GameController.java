package rush.control;

import java.util.ArrayList;
import java.util.List;
import rush.boundary.ConsoleUI;
import rush.entity.Customer;
import rush.entity.HappyHour;
import rush.entity.Restaurant;
import rush.staff.Cashier;
import rush.staff.Staff;
import rush.staff.StaffTask;

/**
 * Runs the game: the fixed number of turns, each made of the six steps from the
 * brief. It asks the UI for the manager's decisions and delegates the rules to
 * the restaurant, the staff tasks and the customers themselves.
 */
public class GameController {

    private final GameConfig config;
    private final ConsoleUI ui;
    private final Restaurant restaurant;
    private final List<Staff> assignableStaff;
    private final Cashier cashier;
    private int nextCustomerId = 1;

    public GameController(GameConfig config, ConsoleUI ui) {
        this.config = config;
        this.ui = ui;
        this.restaurant = new Restaurant(config.getTableCount(), config.getMenu(),
                new HappyHour(config.isHappyHourOffered()));
        this.assignableStaff = config.createAssignableStaff();
        this.cashier = config.createCashier();
    }

    /** Plays every turn (there is no early ending) and reports the result. */
    public boolean run() {
        ui.showWelcome(config);
        for (int turn = 1; turn <= config.getMaxTurns(); turn++) {
            playTurn(turn);
        }
        // the targets are only checked once, after the last turn has finished
        boolean playerWon = config.getObjective().isMetBy(restaurant);
        ui.showFinalResult(playerWon, config.getObjective().checklist(restaurant), restaurant, config.getMaxTurns());
        return playerWon;
    }

    private void playTurn(int turn) {
        restaurant.startTurn(turn);
        ui.showTurnHeader(turn, config.getMaxTurns());

        // Step 1: whoever is scheduled for this turn joins the waiting queue
        for (Customer newCustomer : arrivingOn(turn)) {
            restaurant.admit(newCustomer);
            ui.showEvent("Arrival: " + newCustomer.label() + " joins the waiting queue with satisfaction "
                    + newCustomer.getSatisfaction() + " (" + newCustomer.describeTraits() + ").");
        }
        ui.showState(restaurant, assignableStaff);
        startOfTurnPolicies();

        // Steps 2 and 3: manager choices, executed in staff order (Host, Waiter, then Chef)
        for (Staff staffMember : assignableStaff) {
            directStaff(staffMember);
        }

        // Step 4: eating and payment
        for (Customer finishedEater : restaurant.finishEating()) {
            ui.showEvent(finishedEater.label() + " finishes eating and is READY_TO_PAY (pays next turn at the earliest).");
        }
        // the Cashier takes at most one payment per turn, oldest first
        String paymentMessage = cashier.processPayment(restaurant);
        if (paymentMessage != null) {
            ui.showEvent(paymentMessage);
        }

        // Step 5: waiting and abandonment
        for (Customer departedCustomer : restaurant.applyWaitingPenalties()) {
            ui.showEvent(departedCustomer.label() + " ran out of patience and LEFT without paying.");
        }

        // Step 6: end-of-turn results, after Happy Hour has counted down one turn
        restaurant.endTurn();
        ui.showEndOfTurn(turn, config.getMaxTurns(), restaurant);
    }

    /** Happy Hour may be switched on at the start of a turn; while active, waiting customers recover. */
    private void startOfTurnPolicies() {
        HappyHour happyHourPolicy = restaurant.getHappyHour();
        // canActivate is false when the setting has no Happy Hour and once the one use is spent
        if (happyHourPolicy.canActivate() && ui.offerHappyHour()) {
            happyHourPolicy.activate();
            ui.showEvent("Happy Hour is ON for this turn and the next: orders taken get 20% off, and waiting"
                    + " customers recover " + HappyHour.RECOVERY_POINTS + " satisfaction at the start of each turn.");
        }
        for (Customer recoveredCustomer : restaurant.applyHappyHourRecovery()) {
            ui.showEvent("Happy Hour: " + recoveredCustomer.label() + " cheers up, satisfaction now "
                    + recoveredCustomer.getSatisfaction() + ".");
        }
    }

    private List<Customer> arrivingOn(int turn) {
        List<Customer> customersArriving = new ArrayList<>();
        for (Arrival scheduledArrival : config.getArrivals()) {
            if (scheduledArrival.getTurn() == turn) {
                // ids are handed out in arrival order, starting from 1
                customersArriving.add(scheduledArrival.createCustomer(nextCustomerId++));
            }
        }
        return customersArriving;
    }

    /** Lets the manager give one task (or Wait) to a staff member. Invalid choices are re-asked. */
    private void directStaff(Staff staff) {
        while (true) {
            int menuNumber = ui.chooseTask(staff);
            // 0 means wait, so the staff member does nothing this turn
            if (menuNumber == 0) {
                ui.showEvent(staff.getRole() + " waits.");
                return;
            }
            // menu numbers start at 1 but the list starts at 0, hence the minus one
            if (tryTask(staff, staff.getTasks().get(menuNumber - 1))) {
                return;
            }
            // the task could not be done, so ask again without using up the turn
        }
    }

    /** @return true if the task was carried out, false if the manager must choose again */
    private boolean tryTask(Staff staff, StaffTask task) {
        List<Object> possibleTargets = task.candidates(restaurant);
        if (possibleTargets.isEmpty()) {
            ui.showError("Cannot " + task.getName().toLowerCase() + ": " + task.whyUnavailable(restaurant)
                    + " Choose again.");
            return false;
        }
        List<String> targetLabels = new ArrayList<>();
        for (Object target : possibleTargets) {
            targetLabels.add(task.describe(target));
        }
        int targetNumber = ui.chooseTarget(task.getName(), targetLabels);
        // 0 here means the manager backed out of this task
        if (targetNumber == 0) {
            return false;
        }
        ui.showEvent(staff.getRole() + " " + task.perform(restaurant, possibleTargets.get(targetNumber - 1)));
        return true;
    }
}
