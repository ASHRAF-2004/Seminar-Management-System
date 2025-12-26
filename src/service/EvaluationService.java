package service;

import model.Evaluation;
import repository.EvaluationRepository;
import util.IdGenerator;

import java.util.List;
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
}
