package rush;

import java.util.Arrays;
import rush.boundary.ConsoleUI;
import rush.control.GameConfig;
import rush.control.GameController;

/**
 * Entry point. Usage: {@code java -cp out rush.Main [--stage1 | --stage2] [--echo]}.
 * Without a stage flag the full base setting is played (all Stage 3 features).
 * {@code --stage1} and {@code --stage2} replay the earlier stages' settings with the
 * same code. {@code --echo} prints each input line back, which makes transcripts
 * of scripted runs ({@code < test-runs/file.txt}) readable.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        boolean echo = Arrays.asList(args).contains("--echo");
        GameConfig config;
        if (Arrays.asList(args).contains("--stage1")) {
            config = GameConfig.stage1Setting();
        } else if (Arrays.asList(args).contains("--stage2")) {
            config = GameConfig.stage2Setting();
        } else {
            config = GameConfig.baseSetting();
        }
        ConsoleUI ui = new ConsoleUI(System.in, System.out, echo);
        GameController controller = new GameController(config, ui);
        controller.run();
    }
}
