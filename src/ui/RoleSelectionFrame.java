package ui;

import model.Role;
import repository.UserRepository;
import service.AuthService;
import service.EnrollmentService;
import service.EvaluationService;
import service.SessionService;
import service.SubmissionService;
import service.SeminarService;
import service.AwardService;
import service.ReportService;

import javax.swing.*;
import java.awt.*;

public class RoleSelectionFrame extends JFrame {
    private final AuthService authService;
    private final SeminarService seminarService;
    private final EnrollmentService enrollmentService;
    private final EvaluationService evaluationService;
    private final SubmissionService submissionService;
    private final SessionService sessionService;
    private final AwardService awardService;
    private final ReportService reportService;
    private final UserRepository userRepository;

    public RoleSelectionFrame(AuthService authService, SeminarService seminarService, EnrollmentService enrollmentService,
                              EvaluationService evaluationService, SubmissionService submissionService,
                              SessionService sessionService, AwardService awardService, ReportService reportService, UserRepository userRepository) {
        super("Seminar Management System - Role Selection");
        this.authService = authService;
        this.seminarService = seminarService;
        this.enrollmentService = enrollmentService;
        this.evaluationService = evaluationService;
        this.submissionService = submissionService;
        this.sessionService = sessionService;
        this.awardService = awardService;
        this.reportService = reportService;
        this.userRepository = userRepository;
        buildUi();
    }

    private void buildUi() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel welcome = new JLabel("Select your role and login", SwingConstants.CENTER);
        welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 16f));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(welcome, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        panel.add(new JLabel("Role"), gbc);

        JComboBox<Role> roleCombo = new JComboBox<>(Role.values());
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("User ID"), gbc);
        JTextField idField = new JTextField();
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Password"), gbc);
        JPasswordField passwordField = new JPasswordField();
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        panel.add(loginButton, gbc);

        gbc.gridy++;
        JButton registerButton = new JButton("Register as Student");
        panel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            Role role = (Role) roleCombo.getSelectedItem();
            String id = idField.getText().trim();
            String pass = new String(passwordField.getPassword());
            if (id.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter both ID and password", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            authService.login(id, pass, role).ifPresentOrElse(user -> {
                JOptionPane.showMessageDialog(this, "Welcome, " + user.getName());
                openDashboard(role, user.getId());
            }, () -> JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE));
        });

        registerButton.addActionListener(e -> new StudentRegistrationFrame(userRepository).setVisible(true));

        add(panel);
    }

    private void openDashboard(Role role, String userId) {
        dispose();
        switch (role) {
            case STUDENT -> new StudentDashboard(seminarService, enrollmentService, submissionService, userId).setVisible(true);
            case EVALUATOR -> new EvaluatorDashboard(seminarService, evaluationService, submissionService, sessionService, userId).setVisible(true);
            case COORDINATOR -> new CoordinatorDashboard(seminarService, enrollmentService, submissionService, sessionService, awardService, reportService, userRepository).setVisible(true);
            default -> throw new IllegalStateException("Unexpected value: " + role);
        }
    }
}
