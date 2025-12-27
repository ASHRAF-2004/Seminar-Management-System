# Seminar Management System

A lightweight Java Swing application for managing postgraduate seminars. The system provides role-specific workflows for students, evaluators, and coordinators, storing data in simple CSV files for easy inspection and reset.

## Features
- **Authentication**: Role-based login for students, evaluators, and coordinators.
- **Student experience**: Browse available seminars and enroll in a session.
- **Coordinator console**: Create seminars, manage presenters/evaluators, and maintain the schedule.
- **Evaluation tools**: Capture rubric scores and comments for each enrolled presenter.
- **File-backed storage**: CSV files in `data/` hold seminars, enrollments, evaluations, and users; defaults are seeded on first run.

## Project structure
```
src/
  model/         // Domain entities (Seminar, Enrollment, Evaluation, User, etc.)
  repository/    // In-memory repositories with CSV persistence
  service/       // Business logic for auth, seminars, enrollment, and evaluations
  ui/            // Swing-based UI entry points and dashboards for each role
  util/          // File helpers and ID generation
```
Data files live in `data/` and can be cleared to reset the application state.

## Prerequisites
- Java 17 or later
- A terminal capable of running `javac` and `java`

## Build and run
Compile the sources into an `out` directory, then launch the Swing app. Use the commands that match your shell/OS:

**Linux/macOS (bash/zsh):**
  ```bash
  # From the repository root
  javac -d out $(find src -name "*.java")
  java -cp out ui.Main
  ```

- **Windows (PowerShell):**
  ```powershell
  Set-Location $PSScriptRoot
  if (-not (Test-Path out)) { New-Item -ItemType Directory out | Out-Null }
  Get-ChildItem -Recurse -Filter *.java -Path src | Select-Object -ExpandProperty FullName | Set-Content sources.txt
  javac -d out @sources.txt
  java -cp out ui.Main
  Remove-Item sources.txt -ErrorAction SilentlyContinue
  ```

- **Windows (Command Prompt):**
  ```cmd
  cd /d "%~dp0"
  if not exist out mkdir out
  dir /s /b "src\*.java" > sources.txt
  javac -d out @sources.txt
  java -cp out ui.Main
  del sources.txt
  ```

If you prefer a one-liner experience, use the OS-specific helper scripts in `scripts/` (see below).

On startup, use any of the seeded accounts (password `pass`):
- **Student**: `stu1`
- **Evaluator**: `eval1`
- **Coordinator**: `coord1`

## One-click run scripts
Run the app with the provided OS-specific helpers in `scripts/`:

- **Linux**: `./scripts/run-linux.sh`
- **macOS**: `./scripts/run-macos.sh`
- **Windows**: `scripts\run-windows.bat`

Each script compiles sources into `out/` and starts `ui.Main`. On first use, make the shell scripts executable (`chmod +x scripts/run-*.sh`).

## Resetting data
Delete the CSV files under `data/` to restore the seeded defaults on the next launch.
