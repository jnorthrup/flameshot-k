#!/usr/bin/env bash
# Gap-analysis driven red TDD helper
# - runs tests whose class/name contains 'Gap' (case-insensitive)
# - if no gap tests fail, exit non-zero to enforce red-first workflow
# - otherwise, propagate the test exit code

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Running gap-analysis tests in kotlin-port..."

# Gradle filter: run JVM tests with name or class matching .*Gap.* (case-insensitive)
./gradlew -p kotlin-port jvmTest --tests "**/*Gap*" --no-daemon || true
TEST_EXIT=$?

# Determine if any tests ran and failed by scanning test results XML for the jvmTest task
RESULT_DIR="kotlin-port/build/test-results/jvmTest"

if [ ! -d "$RESULT_DIR" ]; then
  echo "No test results found in $RESULT_DIR — assuming no gap tests executed. Enforcing red-first policy."
  exit 2
fi

# Count failed tests
FAILED_COUNT=$(grep -R "failures=\|errors=\"" -n "$RESULT_DIR" | grep -Eo "failures=\"[0-9]+\"|errors=\"[0-9]+\"" | sed -E 's/.*=\"([0-9]+)\".*/\1/' | awk '{s+=$1} END {print s+0}')

if [ "$FAILED_COUNT" -eq 0 ]; then
  echo "Gap-analysis found 0 failing tests. To follow red TDD, add a failing 'Gap' test first."
  exit 3
fi

# Otherwise, return the original test exit (non-zero if tests failed as expected)
exit $TEST_EXIT
