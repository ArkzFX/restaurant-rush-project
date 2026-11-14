package rush;

import java.util.Arrays;
import rush.boundary.ConsoleUI;
import rush.control.GameConfig;
import rush.control.GameController;

/**
 * Entry point. Usage: {@code java -cp out rush.Main [--echo]}.
 * {@code --echo} prints each input line back, which makes transcripts of
 * scripted runs ({@code < test-runs/file.txt}) readable.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        boolean echo = Arrays.asList(args).contains("--echo");
        ConsoleUI ui = new ConsoleUI(System.in, System.out, echo);
        GameController controller = new GameController(GameConfig.baseSetting(), ui);
        controller.run();
    }
}
