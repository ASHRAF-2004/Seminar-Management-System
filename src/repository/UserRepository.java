package repository;

import model.Role;
import model.User;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final Path userFile = Path.of("data/users.csv");
    private final List<User> users;

    public UserRepository() {
        users = new ArrayList<>();
        load();
        seedDefaults();
    }

    private void load() {
        for (String line : FileUtils.readAllLines(userFile)) {
            String[] parts = line.split("\\|", -1);
            if (parts.length >= 4) {
                users.add(new User(parts[0], parts[1], Role.valueOf(parts[2]), parts[3]));
            }
        }
    }

    private void seedDefaults() {
        if (users.isEmpty()) {
            users.add(new User("stu1", "Alice Student", Role.STUDENT, "pass"));
            users.add(new User("eval1", "Dr. Eva Luator", Role.EVALUATOR, "pass"));
            users.add(new User("coord1", "Mr. C Oord", Role.COORDINATOR, "pass"));
            save();
        }
    }

    public Optional<User> authenticate(String id, String password) {
        return users.stream()
                .filter(u -> u.getId().equalsIgnoreCase(id) && u.getPassword().equals(password))
                .findFirst();
    }

    public User findById(String id) {
        return users.stream().filter(u -> u.getId().equals(id)).findFirst().orElse(null);
    }

    public List<User> findByRole(Role role) {
        List<User> result = new ArrayList<>();
        for (User user : users) {
            if (user.getRole() == role) {
                result.add(user);
            }
        }
        return result;
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join("|", u.getId(), u.getName(), u.getRole().name(), u.getPassword()));
        }
        FileUtils.writeLines(userFile, lines);
    }
}
