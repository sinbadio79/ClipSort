#!/usr/bin/env bash
set -euo pipefail
trap 'adb pull /sdcard/Pictures/ClipSortScreenshots screenshots || true' EXIT
./gradlew connectedDebugAndroidTest --stacktrace
