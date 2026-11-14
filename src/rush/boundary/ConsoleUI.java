package rush.boundary;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import rush.control.Arrival;
import rush.control.GameConfig;
import rush.entity.Customer;
import rush.entity.CustomerStatus;
import rush.entity.MenuItem;
import rush.entity.Money;
import rush.entity.Restaurant;
import rush.entity.Table;
import rush.staff.Staff;
import rush.staff.StaffTask;

/**
 * Command-line boundary. It only displays state and collects the manager's
 * decisions as menu numbers; it never decides whether a move is legal.
 *
 * <p>Input robustness: anything that is not a number in range is rejected with
 * an explanation and asked again. Lines starting with '#' are treated as
 * comments (useful in scripted test runs). If input ends, every remaining
 * decision defaults to option 0 (Wait / Back) so the game still finishes.
 */
public class ConsoleUI {

    private static final String RULE = "=".repeat(72);

    private final Scanner in;
    private final PrintStream out;
    private final boolean echoInput;
    private boolean inputEnded;

    public ConsoleUI(InputStream in, PrintStream out, boolean echoInput) {
        this.in = new Scanner(in);
        this.out = out;
        this.echoInput = echoInput;
    }

    // display

    public void showWelcome(GameConfig config) {
        out.println(RULE);
        out.println(" RESTAURANT RUSH  -  " + config.getName());
        out.println(RULE);
        out.println("You are the manager. Each turn you give the Waiter and the Chef one task each.");
        out.println("Turns: " + config.getMaxTurns() + "   Tables: " + config.getTableCount()
                + "   Arrivals on turns: " + arrivalTurns(config));
        out.println("Goal after turn " + config.getMaxTurns() + ": " + config.getObjective().describe() + ".");
        out.println("Menu (customers choose their own food):");
        for (MenuItem item : config.getMenu().getItems()) {
            out.println("  " + item.describe());
        }
    }

    private static String arrivalTurns(GameConfig config) {
        StringBuilder turns = new StringBuilder();
        for (Arrival arrival : config.getArrivals()) {
            if (turns.length() > 0) {
                turns.append(", ");
            }
            turns.append(arrival.getTurn());
        }
        return turns.toString();
    }

    public void showTurnHeader(int turn, int maxTurns) {
        out.println();
        out.println(RULE);
        out.println(" TURN " + turn + " of " + maxTurns);
        out.println(RULE);
    }

    /** Start-of-turn display: totals, queue, tables, customers and available staff tasks. */
    public void showState(Restaurant restaurant, List<Staff> staff) {
        out.println("Revenue: " + Money.format(restaurant.getRevenueCents())
                + "   Avg satisfaction (paid): " + formatAverage(restaurant)
                + "   Paid: " + restaurant.countPaid()
                + "   Left unhappy: " + restaurant.countUnhappyDepartures());

        List<Customer> queue = restaurant.getWaitingQueue();
        out.println("Waiting queue:" + (queue.isEmpty() ? " (empty)" : ""));
        for (Customer customer : queue) {
            out.println("  " + customer.label() + "  satisfaction " + customer.getSatisfaction());
        }

        out.println("Tables:");
        for (Table table : restaurant.getTables()) {
            Customer occupant = table.getOccupant();
            out.println("  " + table + ": " + (occupant == null ? "free" : describeCustomer(occupant)));
        }

        out.println("Staff tasks available:");
        for (Staff member : staff) {
            StringBuilder line = new StringBuilder(String.format("  %-8s", member.getRole() + ":"));
            for (StaffTask task : member.getTasks()) {
                int count = task.candidates(restaurant).size();
                line.append(" [").append(task.getName()).append(": ")
                        .append(count == 0 ? "none" : count).append("]");
            }
            out.println(line);
        }
        int readyToPay = restaurant.countReadyToPay();
        out.println(String.format("  %-8s", "Cashier:") + " automatic, at most one payment per turn ("
                + readyToPay + " can pay this turn)");
    }

    private static String describeCustomer(Customer customer) {
        String text = customer.label() + "  satisfaction " + customer.getSatisfaction() + "  " + customer.getStatus();
        if (customer.getOrder() != null) {
            text += "  | order: " + customer.getOrder().getItem().getName() + " [" + customer.getOrder().progressLabel()
                    + "] bill " + Money.format(customer.getOrder().getBillCents());
        }
        return text;
    }

    public void showEvent(String message) {
        out.println("  > " + message);
    }

    public void showError(String message) {
        out.println("  ! " + message);
    }

    public void showEndOfTurn(int turn, int maxTurns, Restaurant restaurant) {
        out.println("-- End of turn " + turn + ": served " + restaurant.countServed()
                + " | paid " + restaurant.countPaid()
                + " | left unhappy " + restaurant.countUnhappyDepartures()
                + " | revenue " + Money.format(restaurant.getRevenueCents())
                + " | avg satisfaction " + formatAverage(restaurant)
                + " | turns used " + turn + "/" + maxTurns);
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.getStatus().isActive()) {
                out.println("   " + customer.label() + ": satisfaction " + customer.getSatisfaction() + ", " + customer.getStatus());
            }
        }
    }

    public void showFinalResult(boolean victory, List<String> checklist, Restaurant restaurant, int turnsUsed) {
        out.println();
        out.println(RULE);
        out.println(victory ? " VICTORY! The restaurant hit every target." : " DEFEAT. Not every target was met.");
        out.println(RULE);
        for (String line : checklist) {
            out.println("  " + line);
        }
        out.println("  Served: " + restaurant.countServed() + "   Left unhappy: "
                + restaurant.countUnhappyDepartures() + "   Turns used: " + turnsUsed);
        out.println("  Paid customers:");
        for (Customer customer : restaurant.getCustomers()) {
            if (customer.getStatus() == CustomerStatus.PAID) {
                out.println("    " + customer.label() + ": " + customer.getOrder().getItem().getName() + " "
                        + Money.format(customer.getOrder().getBillCents()) + ", satisfaction " + customer.getPaidSatisfaction());
            }
        }
    }

    /** Shows the average to one decimal place, or N/A if nobody has paid yet. */
    private static String formatAverage(Restaurant restaurant) {
        if (!restaurant.hasPaidCustomers()) {
            return "N/A";
        }
        return String.format("%.1f", restaurant.averagePaidSatisfaction());
    }

    // input

    /** @return 0 for Wait, otherwise the 1-based task number */
    public int chooseTask(Staff staff) {
        out.println(staff.getRole() + " - choose a task:");
        List<StaffTask> tasks = staff.getTasks();
        for (int i = 0; i < tasks.size(); i++) {
            out.println("  " + (i + 1) + ") " + tasks.get(i).getName());
        }
        out.println("  0) Wait");
        return readChoice(tasks.size());
    }

    /** @return 0 to go back, otherwise the 1-based position in {@code labels} */
    public int chooseTarget(String taskName, List<String> labels) {
        out.println(taskName + " - choose one:");
        for (int i = 0; i < labels.size(); i++) {
            out.println("  " + (i + 1) + ") " + labels.get(i));
        }
        out.println("  0) Back");
        return readChoice(labels.size());
    }

    private int readChoice(int max) {
        while (true) {
            String prompt = "Enter 0-" + max + ": ";
            out.print(prompt);
            String line = nextLine(prompt);
            if (line == null) {
                out.println("(no more input - choosing 0)");
                return 0;
            }
            try {
                int value = Integer.parseInt(line);
                if (value >= 0 && value <= max) {
                    return value;
                }
            } catch (NumberFormatException e) {
                // fall through to the message below
            }
            showError("'" + line + "' is not a valid choice. Please enter a number from 0 to " + max + ".");
        }
    }

    /** Next non-comment line, or null once input has ended. */
    private String nextLine(String prompt) {
        boolean echoedComment = false;
        while (!inputEnded && in.hasNextLine()) {
            String line = in.nextLine().trim();
            if (line.startsWith("#")) {
                if (echoInput) {
                    out.print(echoedComment ? "" : System.lineSeparator());
                    out.println(line);
                    echoedComment = true;
                }
                continue;
            }
            if (echoInput) {
                out.println((echoedComment ? prompt : "") + line);
            }
            return line;
        }
        inputEnded = true;
        return null;
    }
}
