package service;

import model.Submission;
import repository.SubmissionRepository;

import java.util.List;
import java.util.Optional;

public class SubmissionService {
    private final SubmissionRepository repository;

    public SubmissionService(SubmissionRepository repository) {
        this.repository = repository;
    }

    public Submission saveOrUpdate(String studentId, String title, String abstractText, String supervisor, String type,
                                   String filePath) {
        Optional<Submission> existing = repository.findByStudent(studentId);
        Submission submission = existing.orElseGet(() -> Submission.createNew(studentId, title, abstractText, supervisor, type, filePath));
        submission.setResearchTitle(title);
        submission.setAbstractText(abstractText);
        submission.setSupervisorName(supervisor);
        submission.setPresentationType(type);
        submission.setFilePath(filePath);
        repository.upsert(submission);
        return submission;
    }

    public void save(Submission submission) {
        repository.upsert(submission);
    }

    public List<Submission> getAll() {
        return repository.findAll();
    }

    public Optional<Submission> findByStudent(String studentId) {
        return repository.findByStudent(studentId);
    }

    public Optional<Submission> findById(String id) {
        return repository.findById(id);
    }
}
