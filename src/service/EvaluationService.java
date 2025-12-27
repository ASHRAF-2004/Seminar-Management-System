package service;

import model.Evaluation;
import repository.EvaluationRepository;
import util.IdGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EvaluationService {
    private final EvaluationRepository repository;

    public EvaluationService(EvaluationRepository repository) {
        this.repository = repository;
    }

    public Evaluation saveEvaluation(String seminarId, String evaluatorId, int clarity, int methodology, int results,
                                     int presentation, String comments) {
        Evaluation evaluation = repository.findBySeminarAndEvaluator(seminarId, evaluatorId)
                .orElse(new Evaluation(IdGenerator.newId("EV-"), seminarId, evaluatorId, clarity, methodology, results,
                        presentation, comments));
        evaluation.setProblemClarity(clarity);
        evaluation.setMethodology(methodology);
        evaluation.setResults(results);
        evaluation.setPresentation(presentation);
        evaluation.setComments(comments);
        repository.addOrUpdate(evaluation);
        return evaluation;
    }

    public List<Evaluation> findBySeminar(String seminarId) {
        return repository.findBySeminar(seminarId);
    }

    public Optional<Evaluation> findBySeminarAndEvaluator(String seminarId, String evaluatorId) {
        return repository.findBySeminarAndEvaluator(seminarId, evaluatorId);
    }

    public Map<String, Double> averageScoresBySeminar() {
        Map<String, Double> averages = new HashMap<>();
        Map<String, List<Evaluation>> grouped = repository.findAllGroupedBySeminar();
        for (Map.Entry<String, List<Evaluation>> entry : grouped.entrySet()) {
            double total = entry.getValue().stream().mapToDouble(Evaluation::getAverageScore).sum();
            averages.put(entry.getKey(), total / entry.getValue().size());
        }
        return averages;
    }
}
