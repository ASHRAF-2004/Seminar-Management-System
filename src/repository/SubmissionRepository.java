package repository;

import model.Submission;
import util.AppConfig;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmissionRepository {
    private final Path submissionFile;

    public SubmissionRepository() {
        this(AppConfig.SUBMISSIONS_FILE);
    }

    public SubmissionRepository(Path submissionFile) {
        this.submissionFile = submissionFile;
    }

    public synchronized List<Submission> findAll() {
        List<Submission> submissions = new ArrayList<>();
        for (String line : FileUtils.readAllLines(submissionFile)) {
            if (!line.isBlank()) {
                submissions.add(Submission.fromCsv(line));
            }
        }
        return submissions;
    }

    public synchronized void saveAll(List<Submission> submissions) {
        List<String> lines = new ArrayList<>();
        for (Submission s : submissions) {
            lines.add(s.toCsv());
        }
        FileUtils.writeLines(submissionFile, lines);
    }

    public synchronized void upsert(Submission submission) {
        List<Submission> all = findAll();
        Optional<Submission> existing = all.stream().filter(s -> s.getId().equals(submission.getId())).findFirst();
        if (existing.isPresent()) {
            all.set(all.indexOf(existing.get()), submission);
        } else {
            all.add(submission);
        }
        saveAll(all);
    }

    public Optional<Submission> findById(String id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public Optional<Submission> findByStudent(String studentId) {
        return findAll().stream().filter(s -> s.getStudentId().equals(studentId)).findFirst();
    }
}
