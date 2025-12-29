package ui;

import model.Role;
import model.User;
import repository.UserRepository;
import util.PasswordUtils;

import javax.swing.*;
import java.awt.*;

public class StudentRegistrationFrame extends JFrame {
    private final UserRepository userRepository;

    public StudentRegistrationFrame(UserRepository userRepository) {
        super("Register as Student");
        this.userRepository = userRepository;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField idField = addRow(panel, gbc, 0, "Student ID");
        JTextField nameField = addRow(panel, gbc, 1, "Full Name");
        JPasswordField passwordField = new JPasswordField();
        addRow(panel, gbc, 2, "Password", passwordField);
        JPasswordField confirmField = new JPasswordField();
        addRow(panel, gbc, 3, "Confirm Password", confirmField);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JButton registerButton = new JButton("Create Account");
        panel.add(registerButton, gbc);

        registerButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirm = new String(confirmField.getPassword());

            if (id.isEmpty() || name.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                User student = new User(id, name, Role.STUDENT, PasswordUtils.hashPassword(password));
                userRepository.saveUser(student);
                JOptionPane.showMessageDialog(this, "Account created. You can now log in as a student.");
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.WARNING_MESSAGE);
            }
        });

        add(panel);
    }

    private JTextField addRow(JPanel panel, GridBagConstraints gbc, int row, String label) {
        JTextField field = new JTextField();
        addRow(panel, gbc, row, label, field);
        return field;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }
}
