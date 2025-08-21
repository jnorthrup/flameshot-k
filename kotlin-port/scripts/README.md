Gap analysis TDD helper

This directory contains helper scripts to support a Gap-Analysis driven red-TDD workflow for the `kotlin-port` module.

gap_analysis_tdd.sh
- Purpose: Run tests that match `*Gap*` and enforce a red-first workflow.
- Behavior:
  - Executes `./gradlew -p kotlin-port test --tests "**/*Gap*"`.
  - If no gap tests execute or none fail, the script exits non-zero to enforce writing failing gap tests first.
  - If gap tests run and some fail, the script exits with the test runner's exit code (non-zero when failing).

Usage locally:

```bash
# from repo root
chmod +x kotlin-port/scripts/gap_analysis_tdd.sh
./kotlin-port/scripts/gap_analysis_tdd.sh
```

CI integration:
- The GitHub Actions workflow runs the green TDD job first (`tdd-green`) then the gap-analysis red TDD job (`tdd-gap-analysis`).

Notes and tips:
- Add tests by naming them or their classes/methods containing the token `Gap` to be picked up by the script.
- The script looks for test reports under `kotlin-port/build/test-results/test` to detect failures.
- If you prefer a different token or pattern, update the `--tests` filter in the script and workflow.
