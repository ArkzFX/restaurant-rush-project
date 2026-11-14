# Restaurant Rush - Stage 2: customer behaviour

This is the **Stage 2** version. It extends Stage 1 with VIP and Critic customers.
Run `java -cp out rush.Main` for the Stage 2 game, or add `--stage1` to replay the Stage 1 setting.

## Build and run

Needs JDK 17 or newer. No other dependencies and no network access.

```powershell
# PowerShell, from this folder
javac --release 17 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out rush.Main
```

```sh
# Git Bash / Linux / macOS
./build.sh
./run.sh
```

In VS Code you can also open `src/rush/Main.java` and press Run.

## How to play

Every prompt is a numbered menu: type a number and press Enter. An invalid choice, or a task that
cannot be done right now, is explained and asked again, and the staff member's task is not used up.
Each turn runs the six steps from the brief: arrivals, manager choices, kitchen and serving, eating and
payment, waiting and abandonment, results. The goal after turn 18 is at least 4 paid customers,
revenue of at least $45.00 and an average satisfaction of paid customers of at least 60.

| Customer | Loss per waiting turn | Bill | Food choice |
|---|---|---|---|
| Regular | 8 | full price | n-th arrival orders the n-th item on the board, wrapping |
| VIP | 5 | 10% off | same rule as Regular |
| Critic | 12 | full price | always the most expensive item (Pasta) |

Arrivals: turn 1 Regular, 4 **Critic**, 7 Regular, 10 **VIP**, 13 Regular, 16 Regular.

## What changed from Stage 1

| File | Change | Reason |
|---|---|---|
| `entity/RegularCustomer.java` | new | Regular behaviour moved out of `Customer` |
| `entity/VIPCustomer.java` | new | loss 5, overrides `applyDiscount` (10% off) |
| `entity/CriticCustomer.java` | new | loss 12, overrides `chooseItem` (most expensive item) |
| `control/CustomerType.java` | new | names the kinds of customer a schedule can contain |
| `entity/Customer.java` | changed | made abstract; loss, type name and traits became hooks; added `applyDiscount` |
| `control/Arrival.java` | changed | each arrival now carries a type and builds the matching customer |
| `entity/Restaurant.java` | changed | the bill now asks the customer for its discount |
| `entity/Menu.java`, `entity/Money.java` | changed | added `mostExpensive()` and `percentOf()` |
| `control/GameConfig.java`, `Main.java` | changed | registration: new arrival mix; `--stage1` keeps the old setting selectable |
| `control/GameController.java` | changed | one message string only; the turn loop has no customer type checks |
| `test-runs/stage2-*.txt` | new | Stage 2 victory and defeat |
| `test-runs/stage1-*.txt` | changed | one `# ARGS: --stage1` line so they replay on this code |

The staff package and the rest of the turn loop did not change.

## Reproducible runs

Each script in `test-runs/` is plain game input with `# EXPECT:` lines. Replay one with
`java -cp out rush.Main --echo [flags] < test-runs/<file>.txt`, or run them all with `./test-runs/run-all.sh`.

| Script | Result |
|---|---|
| `stage1-victory.txt`, `stage1-defeat.txt` | same results as Stage 1 (earlier behaviour still works) |
| `stage2-victory.txt` | VICTORY: 5 paid, $68.10, average 75.4 |
| `stage2-defeat.txt` | DEFEAT: Critic served at satisfaction 4; 3 paid, $45.00, average 20.0 |

## Diagrams

`docs/class-diagram-stage1.puml` and `docs/class-diagram-stage2.puml` (sources and `.png` images).
