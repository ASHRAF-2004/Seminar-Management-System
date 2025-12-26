package model;

public class User {
    private final String id;
    private final String name;
    private final Role role;
    private final String password;

    public User(String id, String name, Role role, String password) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.password = password;
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

    public String getPassword() {
        return password;
    }
}
