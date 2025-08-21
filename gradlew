#!/usr/bin/env bash
set -euo pipefail

# Simple launcher: prefer system 'gradle' if present. If you need a reproducible
# Gradle wrapper, run 'gradle wrapper' locally to generate wrapper files.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
else
  echo "Gradle not found. Please install Gradle or generate the Gradle wrapper (run: gradle wrapper)" >&2
  exit 1
fi
