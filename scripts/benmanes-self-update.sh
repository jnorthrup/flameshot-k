#!/usr/bin/env bash
# Helper to self-update the ben-manes versions Gradle plugin
# - Queries the Gradle Plugin Portal for latest version
# - If newer than current, replaces the `id("com.github.ben-manes.versions") version "..."` line
#   in kotlin-port/build.gradle.kts and writes a sentinel file with details.
# - Always writes a sentinel proving the script ran.

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
BUILD_FILE="$ROOT_DIR/kotlin-port/build.gradle.kts"
SENTINEL="$ROOT_DIR/kotlin-port/benmanes-update-sentinel.txt"

# Fetch current version from build.gradle.kts
current_version=$(grep -oP 'id\("com.github.ben-manes.versions"\) version "\K[^"]+' "$BUILD_FILE" || true)
if [[ -z "$current_version" ]]; then
  echo "ERROR: couldn't detect current ben-manes version in $BUILD_FILE" >&2
  exit 2
fi

# Query Gradle Plugin Portal for latest version (uses plugins.gradle.org API)
# The API endpoint: https://plugins.gradle.org/api/plugins/com.github.ben-manes.versions
api_url="https://plugins.gradle.org/api/plugins/com.github.ben-manes.versions"

# Try to fetch JSON. Use curl or wget.
if command -v curl >/dev/null 2>&1; then
  json=$(curl -sSf "$api_url")
elif command -v wget >/dev/null 2>&1; then
  json=$(wget -qO- "$api_url")
else
  echo "ERROR: curl or wget required to query plugin portal" >&2
  exit 3
fi

# Extract latest version from JSON. Fallback to 'null' on parse failure.
latest_version=$(echo "$json" | grep -oP '"version":"\K[0-9][^" ]*' | head -n1 || true)

if [[ -z "$latest_version" ]]; then
  # try different key
  latest_version=$(echo "$json" | grep -oP '"latest_version":"\K[^"]+' | head -n1 || true)
fi

if [[ -z "$latest_version" ]]; then
  echo "WARNING: couldn't determine latest version from plugin portal; writing sentinel and exiting with code 4" >&2
  echo "ran_at: $(date --iso-8601=seconds 2>/dev/null || date)" > "$SENTINEL"
  echo "current_version: $current_version" >> "$SENTINEL"
  echo "latest_version: unknown" >> "$SENTINEL"
  exit 4
fi

# Compare versions using sort -V
if [[ "$current_version" == "$latest_version" ]]; then
  echo "ben-manes up-to-date: $current_version"
  echo "ran_at: $(date --iso-8601=seconds 2>/dev/null || date)" > "$SENTINEL"
  echo "current_version: $current_version" >> "$SENTINEL"
  echo "latest_version: $latest_version" >> "$SENTINEL"
  echo "updated: false" >> "$SENTINEL"
  exit 0
fi

# Determine if latest > current
if printf "%s\n%s\n" "$current_version" "$latest_version" | sort -V | tail -n1 | grep -qx "$latest_version"; then
  # Make a safe backup and update the line in the build file
  cp "$BUILD_FILE" "$BUILD_FILE.bak"
  # Use sed to replace the version only on the plugin line
  sed -E 's/(id\("com.github.ben-manes.versions"\) version ")([^"]+)("\))/\1'"$latest_version"'\3/' "$BUILD_FILE.bak" > "$BUILD_FILE"

  echo "Updated ben-manes from $current_version to $latest_version in $BUILD_FILE"
  echo "ran_at: $(date --iso-8601=seconds 2>/dev/null || date)" > "$SENTINEL"
  echo "current_version: $current_version" >> "$SENTINEL"
  echo "latest_version: $latest_version" >> "$SENTINEL"
  echo "updated: true" >> "$SENTINEL"
  echo "backup: $(basename "$BUILD_FILE.bak")" >> "$SENTINEL"
  exit 0
else
  echo "Latest version $latest_version is not greater than current $current_version; no change"
  echo "ran_at: $(date --iso-8601=seconds 2>/dev/null || date)" > "$SENTINEL"
  echo "current_version: $current_version" >> "$SENTINEL"
  echo "latest_version: $latest_version" >> "$SENTINEL"
  echo "updated: false" >> "$SENTINEL"
  exit 0
fi
