package model;

public class User {
    private final String id;
    private final String name;
    private final Role role;
    private final String passwordHash;

    public User(String id, String name, Role role, String passwordHash) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
}
