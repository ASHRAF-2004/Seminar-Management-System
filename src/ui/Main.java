package ui;

import repository.EnrollmentRepository;
import repository.EvaluationRepository;
import repository.SeminarRepository;
import repository.SubmissionRepository;
import repository.SessionRepository;
import repository.UserRepository;
import repository.AwardRepository;
import service.AuthService;
import service.EnrollmentService;
import service.EvaluationService;
import service.SessionService;
import service.SubmissionService;
import service.SeminarService;
import service.AwardService;
import service.ReportService;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UserRepository userRepository = new UserRepository();
            SeminarRepository seminarRepository = new SeminarRepository();
            EnrollmentRepository enrollmentRepository = new EnrollmentRepository();
            EvaluationRepository evaluationRepository = new EvaluationRepository();
            SubmissionRepository submissionRepository = new SubmissionRepository();
            SessionRepository sessionRepository = new SessionRepository();
            AwardRepository awardRepository = new AwardRepository();

            AuthService authService = new AuthService(userRepository);
            SeminarService seminarService = new SeminarService(seminarRepository);
            EnrollmentService enrollmentService = new EnrollmentService(enrollmentRepository);
            EvaluationService evaluationService = new EvaluationService(evaluationRepository);
            SubmissionService submissionService = new SubmissionService(submissionRepository);
            SessionService sessionService = new SessionService(sessionRepository);
            AwardService awardService = new AwardService(awardRepository, evaluationService);
            ReportService reportService = new ReportService(sessionService, submissionService, evaluationService, awardService);

            new RoleSelectionFrame(authService, seminarService, enrollmentService, evaluationService, submissionService,
                    sessionService, awardService, reportService, userRepository)
                    .setVisible(true);
        });
    }
}
