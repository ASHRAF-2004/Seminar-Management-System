package repository;

import model.Evaluation;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EvaluationRepository {
    private final Path evaluationFile = Path.of("data/evaluations.csv");
    private final List<Evaluation> evaluations;

    public EvaluationRepository() {
        evaluations = new ArrayList<>();
        load();
    }

    private void load() {
        for (String line : FileUtils.readAllLines(evaluationFile)) {
            if (!line.isBlank()) {
                evaluations.add(Evaluation.fromCsv(line));
            }
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (Evaluation e : evaluations) {
            lines.add(e.toCsv());
        }
        FileUtils.writeLines(evaluationFile, lines);
    }

    public void addOrUpdate(Evaluation evaluation) {
        findBySeminarAndEvaluator(evaluation.getSeminarId(), evaluation.getEvaluatorId())
                .ifPresentOrElse(existing -> {
                    existing.setProblemClarity(evaluation.getProblemClarity());
                    existing.setMethodology(evaluation.getMethodology());
                    existing.setResults(evaluation.getResults());
                    existing.setPresentation(evaluation.getPresentation());
                    existing.setComments(evaluation.getComments());
                }, () -> evaluations.add(evaluation));
        save();
    }

    public List<Evaluation> findBySeminar(String seminarId) {
        return evaluations.stream().filter(e -> e.getSeminarId().equals(seminarId)).collect(Collectors.toList());
    }

    public Optional<Evaluation> findBySeminarAndEvaluator(String seminarId, String evaluatorId) {
        return evaluations.stream()
                .filter(e -> e.getSeminarId().equals(seminarId) && e.getEvaluatorId().equals(evaluatorId))
                .findFirst();
    }
}
