#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd -- "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
REPO_ROOT="${SCRIPT_DIR}/.."
cd "${REPO_ROOT}"

if ! command -v javac >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1; then
  echo "JDK not found. Please install a Java Development Kit (JDK) and ensure 'java' and 'javac' are on your PATH." >&2
  exit 1
fi

cleanup() {
  rm -f sources.txt
}
trap cleanup EXIT

mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out ui.Main
