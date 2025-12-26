package service;

import model.Role;
import model.User;
import repository.UserRepository;

import java.util.Optional;

public class AuthService {
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> login(String id, String password, Role expectedRole) {
        return userRepository.authenticate(id, password)
                .filter(u -> u.getRole() == expectedRole);
    }
}
