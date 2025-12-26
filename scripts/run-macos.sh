#!/usr/bin/env bash
set -euo pipefail

cleanup() {
  rm -f sources.txt
}
trap cleanup EXIT

mkdir -p out
find src -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out ui.Main
