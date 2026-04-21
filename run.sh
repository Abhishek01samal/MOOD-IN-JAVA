#!/bin/bash
# ============================================================
#  MoodTune — Build & Run Script (Linux / macOS)
# ============================================================
#
#  Prerequisites:
#    • JDK 11 or higher  (java / javac on PATH)
#    • mysql-connector-java-X.X.X.jar  placed in the lib/ folder
#    • MySQL running with schema.sql already imported
#
#  Usage:
#    chmod +x run.sh
#    ./run.sh
# ============================================================

set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR="$PROJECT_DIR/src"
BIN_DIR="$PROJECT_DIR/bin"
LIB_DIR="$PROJECT_DIR/lib"

# ── Find MySQL connector ───────────────────────────────────────────────────────
JDBC_JAR=$(find "$LIB_DIR" -name "mysql-connector*.jar" 2>/dev/null | head -1)
if [ -z "$JDBC_JAR" ]; then
    echo "⚠  No MySQL JDBC jar found in lib/"
    echo "   Download from: https://dev.mysql.com/downloads/connector/j/"
    echo "   Place the .jar in:  $LIB_DIR/"
    echo ""
    echo "   Continuing in DEMO MODE (no database required)."
    JDBC_JAR=""
fi

# ── Compile ────────────────────────────────────────────────────────────────────
mkdir -p "$BIN_DIR"
echo "→ Compiling sources..."

if [ -n "$JDBC_JAR" ]; then
    CP="$JDBC_JAR"
else
    CP="."
fi

find "$SRC_DIR" -name "*.java" > /tmp/sources.txt
javac -cp "$CP" -d "$BIN_DIR" @/tmp/sources.txt
echo "✓ Compilation complete."

# ── Run ────────────────────────────────────────────────────────────────────────
echo "→ Launching MoodTune..."
if [ -n "$JDBC_JAR" ]; then
    java -cp "$BIN_DIR:$JDBC_JAR" \
         -Dawt.useSystemAAFontSettings=on \
         -Dswing.aatext=true \
         com.moodtune.ui.MoodTuneApp
else
    java -cp "$BIN_DIR" \
         -Dawt.useSystemAAFontSettings=on \
         -Dswing.aatext=true \
         com.moodtune.ui.MoodTuneApp
fi
