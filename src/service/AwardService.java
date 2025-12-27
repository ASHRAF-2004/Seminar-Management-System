package service;

import model.Award;
import model.AwardType;
import model.Submission;
import repository.AwardRepository;
import service.EvaluationService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AwardService {
    private final AwardRepository repository;
    private final EvaluationService evaluationService;

    public AwardService(AwardRepository repository, EvaluationService evaluationService) {
        this.repository = repository;
        this.evaluationService = evaluationService;
    }

    public List<Award> findAll() {
        return repository.findAll();
    }

    public void save(Award award) {
        repository.upsert(award);
    }

    public Map<AwardType, Optional<Submission>> computeWinners(List<Submission> submissions) {
        Map<String, Double> averages = evaluationService.averageScoresBySeminar();
        Map<String, Submission> byId = submissions.stream().collect(Collectors.toMap(Submission::getId, s -> s, (a, b) -> a));
        return Map.of(
                AwardType.BEST_ORAL, topSubmissionOfType(byId, averages, "ORAL"),
                AwardType.BEST_POSTER, topSubmissionOfType(byId, averages, "POSTER"),
                AwardType.PEOPLES_CHOICE, topSubmission(byId, averages)
        );
    }

    private Optional<Submission> topSubmissionOfType(Map<String, Submission> byId, Map<String, Double> averages, String type) {
        return averages.entrySet().stream()
                .filter(e -> byId.containsKey(e.getKey()))
                .filter(e -> type.equalsIgnoreCase(byId.get(e.getKey()).getPresentationType()))
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(e -> byId.get(e.getKey()));
    }

    private Optional<Submission> topSubmission(Map<String, Submission> byId, Map<String, Double> averages) {
        return averages.entrySet().stream()
                .filter(e -> byId.containsKey(e.getKey()))
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(e -> byId.get(e.getKey()));
    }
}
