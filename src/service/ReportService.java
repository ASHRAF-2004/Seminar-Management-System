package service;

import model.Award;
import model.Evaluation;
import model.Session;
import model.Submission;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportService {
    private final SessionService sessionService;
    private final SubmissionService submissionService;
    private final EvaluationService evaluationService;
    private final AwardService awardService;

    public ReportService(SessionService sessionService, SubmissionService submissionService,
                         EvaluationService evaluationService, AwardService awardService) {
        this.sessionService = sessionService;
        this.submissionService = submissionService;
        this.evaluationService = evaluationService;
        this.awardService = awardService;
    }

    public void exportSchedule(String path) {
        List<Session> sessions = sessionService.findAll();
        Map<String, Submission> submissions = submissionService.getAll().stream()
                .collect(Collectors.toMap(Submission::getId, s -> s, (a, b) -> a));
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("Session ID,Date,Start,End,Venue,Type,Submissions\n");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
            for (Session session : sessions) {
                String names = session.getSubmissionIds().stream()
                        .map(submissions::get)
                        .filter(s -> s != null)
                        .map(Submission::getResearchTitle)
                        .collect(Collectors.joining("; "));
                writer.write(String.join(",",
                        session.getId(),
                        session.getDate().format(dateFormatter),
                        session.getStartTime().toString(),
                        session.getEndTime().toString(),
                        session.getVenueCode(),
                        session.getSessionType().name(),
                        names));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to export schedule", e);
        }
    }

    public void exportEvaluationReport(String path) {
        Map<String, Double> averages = evaluationService.averageScoresBySeminar();
        Map<String, Long> counts = evaluationService.findAll().stream()
                .collect(Collectors.groupingBy(Evaluation::getSeminarId, Collectors.counting()));
        Map<String, Submission> submissions = submissionService.getAll().stream()
                .collect(Collectors.toMap(Submission::getId, s -> s, (a, b) -> a));
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("Submission,Title,Average Score,Evaluations\n");
            for (Map.Entry<String, Double> entry : averages.entrySet()) {
                Submission submission = submissions.get(entry.getKey());
                String title = submission != null ? submission.getResearchTitle() : entry.getKey();
                long count = counts.getOrDefault(entry.getKey(), 0L);
                writer.write(String.join(",",
                        entry.getKey(),
                        title.replace(",", ";"),
                        String.format("%.2f", entry.getValue()),
                        String.valueOf(count)));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to export evaluation report", e);
        }
    }

    public void exportAwardAgenda(String path) {
        List<Award> awards = awardService.findAll();
        Map<String, Submission> submissions = submissionService.getAll().stream()
                .collect(Collectors.toMap(Submission::getId, s -> s, (a, b) -> a));
        try (FileWriter writer = new FileWriter(path)) {
            writer.write("Award Type,Submission,Title,Session\n");
            for (Award award : awards) {
                Submission sub = submissions.get(award.getSubmissionId());
                writer.write(String.join(",",
                        award.getAwardType().name(),
                        award.getSubmissionId(),
                        sub != null ? sub.getResearchTitle().replace(",", ";") : "",
                        award.getSessionId()));
                writer.write("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to export award agenda", e);
        }
    }
}
