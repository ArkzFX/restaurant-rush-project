#!/bin/sh
# Replays every scripted run in this folder and checks its "# EXPECT:" lines
# against the game's output. Usage: ./test-runs/run-all.sh   (from the project root)
cd "$(dirname "$0")/.." || exit 1
./build.sh > /dev/null || exit 1
mkdir -p test-runs/output
status=0
for script in test-runs/*.txt; do
    name=$(basename "$script" .txt)
    log="test-runs/output/$name.log"
    # optional "# ARGS: ..." line in a script selects the game setting, e.g. --stage1
    args=$(grep '^# ARGS: ' "$script" | sed 's/^# ARGS: //')
    java -cp out rush.Main --echo $args < "$script" > "$log" 2>&1
    failed=0
    grep '^# EXPECT: ' "$script" | sed 's/^# EXPECT: //' | while IFS= read -r expected; do
        # ignore echoed script comments so an EXPECT line cannot match itself
        if ! grep -v '^#' "$log" | grep -qF -- "$expected"; then
            echo "  missing: $expected"
            exit 1
        fi
    done || failed=1
    if [ "$failed" -eq 0 ]; then
        echo "PASS  $name"
    else
        echo "FAIL  $name   (see $log)"
        status=1
    fi
done
exit $status
