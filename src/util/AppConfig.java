package util;

import java.nio.file.Path;
import java.time.LocalTime;

/**
 * Central place for configurable application values. Update these constants to
 * change behaviour without touching business logic.
 */
public final class AppConfig {
    private AppConfig() {
    }

    // CSV file locations
    public static final Path USERS_FILE = Path.of("data/users.csv");
    public static final Path SEMINARS_FILE = Path.of("data/seminars.csv");
    public static final Path SESSIONS_FILE = Path.of("data/sessions.csv");
    public static final Path SUBMISSIONS_FILE = Path.of("data/submissions.csv");
    public static final Path ENROLLMENTS_FILE = Path.of("data/enrollments.csv");
    public static final Path EVALUATIONS_FILE = Path.of("data/evaluations.csv");
    public static final Path AWARDS_FILE = Path.of("data/awards.csv");

    // Session defaults
    public static final LocalTime DEFAULT_SESSION_START = LocalTime.of(9, 0);
    public static final LocalTime DEFAULT_SESSION_END = LocalTime.of(17, 0);
}
