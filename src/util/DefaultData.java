package util;

import model.Role;
import model.Seminar;
import model.SeminarStatus;
import model.User;

import java.time.LocalDate;
import java.util.List;

public final class DefaultData {
    private DefaultData() {
    }

    public static List<User> defaultUsers() {
        return List.of(
                buildUser("stu1", "Alice Student", Role.STUDENT, "pass"),
                buildUser("eval1", "Dr. Eva Luator", Role.EVALUATOR, "pass"),
                buildUser("coord1", "Mr. C Oord", Role.COORDINATOR, "pass"),
                buildUser("admin1", "System Admin", Role.ADMIN, "admin123")
        );
    }

    public static List<Seminar> defaultSeminars() {
        return List.of(
                new Seminar("SEM-001", "AI for Healthcare", "stu1", "Using ML for diagnosis", "Dr. Smith", "Oral",
                        "Auditorium", LocalDate.now().plusDays(10), SeminarStatus.OPEN),
                new Seminar("SEM-002", "Blockchain Security", "stu1", "Ledger analysis", "Dr. Tan", "Poster",
                        "Gallery", LocalDate.now().plusDays(15), SeminarStatus.OPEN)
        );
    }

    private static User buildUser(String id, String name, Role role, String password) {
        return new User(id, name, role, PasswordUtils.hashPassword(password));
    }
}
