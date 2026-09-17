#!/usr/bin/env bash
set -euo pipefail
trap 'adb pull /sdcard/Android/data/com.clipsort.app/files/screenshots screenshots || true' EXIT
./gradlew connectedDebugAndroidTest --stacktrace
