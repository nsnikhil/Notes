#!/bin/bash
# Build System Upgrade (ST-002) Verification Tests
# Runs independently of Gradle compilation to verify build config correctness.

set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
PASS=0
FAIL=0
FAILURES=""

assert_contains() {
    local file="$1" pattern="$2" msg="$3"
    if grep -q "$pattern" "$file"; then
        PASS=$((PASS + 1))
        echo "  ✅ $msg"
    else
        FAIL=$((FAIL + 1))
        FAILURES="${FAILURES}\n  ❌ $msg"
        echo "  ❌ $msg"
    fi
}

assert_not_contains() {
    local file="$1" pattern="$2" msg="$3"
    if grep -q "$pattern" "$file"; then
        FAIL=$((FAIL + 1))
        FAILURES="${FAILURES}\n  ❌ $msg"
        echo "  ❌ $msg"
    else
        PASS=$((PASS + 1))
        echo "  ✅ $msg"
    fi
}

echo "=== ST-002 Build System Upgrade Verification ==="
echo ""

WRAPPER="$PROJECT_ROOT/gradle/wrapper/gradle-wrapper.properties"
ROOT_BG="$PROJECT_ROOT/build.gradle"
APP_BG="$PROJECT_ROOT/app/build.gradle"
SETTINGS="$PROJECT_ROOT/settings.gradle"
PROPS="$PROJECT_ROOT/gradle.properties"

echo "--- Gradle Wrapper Version ---"
assert_contains "$WRAPPER" "gradle-8.9" "Gradle wrapper points to 8.9"

echo "--- AGP and Kotlin Versions ---"
assert_contains "$ROOT_BG" "com.android.tools.build:gradle:8.7.3" "AGP is 8.7.3"
assert_contains "$ROOT_BG" "ext.kotlin_version = '2.1.10'" "Kotlin is 2.1.10"

echo "--- Forbidden References Removed ---"
assert_not_contains "$ROOT_BG" "jcenter()" "No jcenter() in root build.gradle"
assert_not_contains "$APP_BG" "jcenter()" "No jcenter() in app/build.gradle"
assert_not_contains "$SETTINGS" "jcenter()" "No jcenter() in settings.gradle"
assert_not_contains "$ROOT_BG" "maven.fabric.io" "No maven.fabric.io in root build.gradle"
assert_not_contains "$APP_BG" "maven.fabric.io" "No maven.fabric.io in app/build.gradle"
assert_not_contains "$APP_BG" "hugo" "No Hugo plugin in app/build.gradle"
assert_not_contains "$ROOT_BG" "hugo" "No Hugo plugin in root build.gradle"
assert_not_contains "$APP_BG" "kotlin-android-extensions" "No kotlin-android-extensions in app/build.gradle"
assert_not_contains "$APP_BG" "androidExtensions" "No androidExtensions block in app/build.gradle"
assert_not_contains "$APP_BG" "firebase-perf" "No firebase-perf in app/build.gradle"
assert_not_contains "$ROOT_BG" "firebase-perf" "No firebase-perf in root build.gradle"
assert_not_contains "$APP_BG" "enableCrashlytics" "No enableCrashlytics in app/build.gradle"

echo "--- Required Features ---"
assert_contains "$APP_BG" "viewBinding true" "View binding enabled"
assert_contains "$APP_BG" "compileSdk 34" "compileSdk is 34"
assert_contains "$APP_BG" "targetSdk 34" "targetSdk is 34"
assert_contains "$APP_BG" "demo {" "Demo product flavor preserved"
assert_contains "$APP_BG" "full {" "Full product flavor preserved"
assert_contains "$APP_BG" "kotlin-parcelize" "kotlin-parcelize plugin applied"

echo "--- Settings and Properties ---"
assert_contains "$SETTINGS" "pluginManagement" "settings.gradle has pluginManagement block"
assert_not_contains "$PROPS" "MaxPermSize" "No MaxPermSize in gradle.properties"

echo "--- google-services plugin at bottom ---"
LAST_LINE=$(tail -n 5 "$APP_BG" | grep -v '^$' | tail -1)
if echo "$LAST_LINE" | grep -q "com.google.gms.google-services"; then
    PASS=$((PASS + 1))
    echo "  ✅ google-services plugin applied at bottom of app/build.gradle"
else
    FAIL=$((FAIL + 1))
    FAILURES="${FAILURES}\n  ❌ google-services plugin NOT at bottom of app/build.gradle (last line: $LAST_LINE)"
    echo "  ❌ google-services plugin NOT at bottom of app/build.gradle"
fi

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="
if [ "$FAIL" -gt 0 ]; then
    echo -e "\nFailures:$FAILURES"
    exit 1
else
    echo "All checks passed!"
    exit 0
fi
