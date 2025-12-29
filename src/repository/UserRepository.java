package repository;

import model.Role;
import model.User;
import util.AppConfig;
import util.DefaultData;
import util.FileUtils;
import util.PasswordUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepository {
    private final Path userFile;
    private final List<User> users;

    public UserRepository() {
        this(AppConfig.USERS_FILE);
    }

    public UserRepository(Path userFile) {
        this.userFile = userFile;
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
            users.addAll(DefaultData.defaultUsers());
            save();
        }
    }

    public Optional<User> authenticate(String id, String password) {
        return users.stream()
                .filter(u -> u.getId().equalsIgnoreCase(id) && PasswordUtils.matches(password, u.getPasswordHash()))
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

    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    public void saveUser(User user) {
        if (findById(user.getId()) != null) {
            throw new IllegalArgumentException("User ID already exists");
        }
        if (user.getRole() != Role.STUDENT) {
            throw new IllegalArgumentException("Only students can self-register");
        }
        users.add(user);
        save();
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            lines.add(String.join("|", u.getId(), u.getName(), u.getRole().name(), u.getPasswordHash()));
        }
        FileUtils.writeLines(userFile, lines);
    }
}
