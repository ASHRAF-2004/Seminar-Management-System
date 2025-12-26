package ui;

import repository.EnrollmentRepository;
import repository.EvaluationRepository;
import repository.SeminarRepository;
import repository.UserRepository;
import service.AuthService;
import service.EnrollmentService;
import service.EvaluationService;
import service.SeminarService;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserRepository userRepository = new UserRepository();
            SeminarRepository seminarRepository = new SeminarRepository();
            EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
            EvaluationRepository evaluationRepository = new EvaluationRepository();

            AuthService authService = new AuthService(userRepository);
            SeminarService seminarService = new SeminarService(seminarRepository);
            EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepository);
            EvaluationService evaluationService = new EvaluationService(evaluationRepository);

            new RoleSelectionFrame(authService, seminarService, enrollmentService, evaluationService, userRepository)
                    .setVisible(true);
        });
    }
}
