# Restaurant Rush - Stage 1: basic operation

A turn-based restaurant management game in basic Java with a command-line interface. You are the
manager: each turn you give the Waiter and the Chef one task each (or tell them to wait).

This is the **Stage 1** version: Regular customers only, three menu items, Waiter, Chef, Cashier,
two tables, the full six-step turn, abandonment, victory and defeat.

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

| Item | Value |
|---|---|
| Turns / tables | 18 / 2 |
| Staff | Waiter x1, Chef x1, Cashier x1 |
| Arrivals | one Regular customer on turns 1, 4, 7, 10, 13, 16 |
| Menu | Salad $9.00 (1 unit), Burger $15.00 (1), Pasta $18.00 (2) |

Food choice: the n-th customer to arrive orders the n-th item on the menu board, wrapping around.
Regular customers lose 8 satisfaction per turn while unserved. Money is stored as integer cents.

## Project structure

```
src/rush/Main.java                  entry point
src/rush/boundary/ConsoleUI.java    display and numeric input only, no game rules
src/rush/control/                   GameController (turn loop), GameConfig, Arrival, Objective
src/rush/entity/                    Restaurant, Customer, Order, Table, Menu, MenuItem, Money, two status enums
src/rush/staff/                     Staff, Waiter, Chef, Cashier, StaffTask and its four tasks
test-runs/                          scripted victory and defeat runs
docs/                               class diagram for this stage
```

## Reproducible runs

Each script in `test-runs/` is plain game input with `# EXPECT:` lines. Replay one with
`java -cp out rush.Main --echo [flags] < test-runs/<file>.txt`, or run them all with `./test-runs/run-all.sh`.

| Script | Result |
|---|---|
| `stage1-victory.txt` | VICTORY: 5 paid, $66.00, average 79.2 |
| `stage1-defeat.txt` | DEFEAT: kitchen idle until turn 9; 2 paid, $24.00, average 36.0 |

## Diagram

`docs/class-diagram-stage1.puml` (source) and `.png` (image).
