# Restaurant Rush - Stage 3: Host, combo meals and Happy Hour

This is the **Stage 3** (final) version. `java -cp out rush.Main` plays the base setting with every
feature. Add `--stage2` or `--stage1` to replay the earlier settings on the same code.

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

**Host:** seats at most one queued customer per turn, before the Waiter acts. The Waiter can still seat customers.

**Combo meals:** two combos listed after the single dishes: Salad + Burger ($21.60, 2 units) and
Salad + Pasta ($24.30, 3 units). Price is 90% of the two components, to the nearest cent; units are the sum.
Because they come after the dishes, the 4th and 5th arrival of each cycle order a combo, and a Critic orders
the Salad + Pasta Combo.

**Happy Hour:** once per game, at the start of a turn. It lasts that turn and the next, and the remaining
duration is shown. Orders taken while it is on get 20% off, after any VIP discount, and the price is locked
when the order is taken. Customers still waiting for a table or food recover 15 satisfaction (max 100) at
the start of each active turn.

## What changed from Stage 1

Compare: https://github.com/ArkzFX/restaurant-rush-project/compare/stage-1...stage-2

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
| `test-runs/stage1-*.txt` | changed | one `y replay on this code |
| `test-runs/run-all.sh` | changed | reads each script's `# ARGS:` line so earlier stages can be replayed |
| `docs/class-diagram-stage2.puml` / `.png`  |

The staff package and the rest of the turn loop did not change.

## What changed from Stage 2

| File | Change | Reason |
|---|---|---|
| `staff/Host.java` | new | reuses the existing `SeatCustomerTask`, so seating rules are not duplicated |
| `entity/ComboMeal.java` | new | a `MenuItem` made of two `MenuItem`s; price and units are derived |
| `entity/HappyHour.java` | new | once-per-game, two-turn pricing policy with recovery |
| `test/rush/entity/RulesTest.java` | new | checks the brief's worked examples and timing rules |
| `entity/Restaurant.java` | changed | owns the `HappyHour`; the bill applies it after the customer discount; recovery and `endTurn()` |
| `entity/Customer.java` | changed | added `recover(points)`; no customer subclass changed |
| `control/GameController.java` | changed | `startOfTurnPolicies()` and an `endTurn()` call; the staff loop is unchanged because the Host is registered in the config |
| `control/GameConfig.java`, `Main.java` | changed | combo menu, Host and Happy Hour flags, `--stage2` setting |
| `boundary/ConsoleUI.java` | changed | Happy Hour status and prompt; numbered menu board |
| `test-runs/*`, `run-all.sh` | new / changed | Stage 3 runs; the script now also runs `RulesTest` |

Earlier behaviour still works: the Stage 1 and Stage 2 scripts replay on this code with the same results.

## Reproducible runs

Each script in `test-runs/` is plain game input with `# EXPECT:` lines. Replay one with
`java -cp out rush.Main --echo [flags] < test-runs/<file>.txt`, or run them all with `./test-runs/run-all.sh`.

| Script | Setting | Result |
|---|---|---|
| `stage1-victory.txt`, `stage1-defeat.txt` | `--stage1` | same as Stage 1 |
| `stage2-victory.txt`, `stage2-defeat.txt` | `--stage2` | same as Stage 2 |
| `stage3-victory.txt` | base | VICTORY: 5 paid, $95.04, average 82.4 (Happy Hour on turn 5 lifts the Critic) |
| `stage3-defeat.txt` | base | DEFEAT: Happy Hour wasted on turn 1, Host unused, Critic leaves; 2 paid, $25.20 |

`./test-runs/run-all.sh` also runs `RulesTest`: Salad + Pasta Combo = $24.30 and 3 units; a VIP Burger in
Happy Hour = $10.80; the Pasta timeline from the brief; a dish finished this turn cannot be served.

## Diagrams

- `docs/class-diagram-stage1.puml`, `class-diagram-stage2.puml`, `class-diagram-stage3.puml` (and `.png`)
- `docs/sequence-diagram-stage3.puml` (and `.png`): the Critic's combo order with Happy Hour, from arrival to payment and the result check

## Links and video timestamps

- Repository: https://github.com/ArkzFX/restaurant-rush-project
- Stages: 
  [stage-1](https://github.com/ArkzFX/restaurant-rush-project/tree/stage-1) ·
  [stage-2](https://github.com/ArkzFX/restaurant-rush-project/tree/stage-2) ·
  [stage-3](https://github.com/ArkzFX/restaurant-rush-project/tree/stage-3)
- Compare: 
  [stage-1...stage-2](https://github.com/ArkzFX/restaurant-rush-project/compare/stage-1...stage-2) ·
  [stage-2...stage-3](https://github.com/ArkzFX/restaurant-rush-project/compare/stage-2...stage-3)

- Video table: 

| # | Segment | Presenter | Timestamp |
|---|---|---|---|
| 1 | Game launching from the submitted project | | |
| 2 | Meaningful manager choices, including a constrained decision | | |
| 3 | A dish going READY -> SERVED -> payment | | |
| 4 | A VIP or Critic behaviour | | |
| 5 | A Stage 3 feature | | |
| 6 | One victory and one defeat | | |
| 7 | Stage tag comparisons and the diagrams | | |
