package repository;

import model.Submission;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmissionRepository {
    private static final String FILE = "data/submissions.csv";

    public SubmissionRepository() {
        // ensure file exists
        try {
            new java.io.File(FILE).createNewFile();
        } catch (IOException ignored) {
        }
    }

    public synchronized List<Submission> findAll() {
        List<Submission> submissions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                submissions.add(Submission.fromCsv(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read submissions", e);
        }
        return submissions;
    }

    public synchronized void saveAll(List<Submission> submissions) {
        try (FileWriter fw = new FileWriter(FILE, false)) {
            for (Submission s : submissions) {
                fw.write(s.toCsv() + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save submissions", e);
        }
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
