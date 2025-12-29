package repository;

import model.Enrollment;
import util.AppConfig;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EnrollmentRepository {
    private final Path enrollmentFile;
    private final List<Enrollment> enrollments;

    public EnrollmentRepository() {
        this(AppConfig.ENROLLMENTS_FILE);
    }

    public EnrollmentRepository(Path enrollmentFile) {
        this.enrollmentFile = enrollmentFile;
        enrollments = new ArrayList<>();
        load();
    }

    private void load() {
        for (String line : FileUtils.readAllLines(enrollmentFile)) {
            if (!line.isBlank()) {
                enrollments.add(Enrollment.fromCsv(line));
            }
        }
    }

    public void save() {
        List<String> lines = new ArrayList<>();
        for (Enrollment e : enrollments) {
            lines.add(e.toCsv());
        }
        FileUtils.writeLines(enrollmentFile, lines);
    }

    public void add(Enrollment enrollment) {
        enrollments.add(enrollment);
        save();
    }

    public List<Enrollment> findByStudent(String studentId) {
        return enrollments.stream().filter(e -> e.getStudentId().equals(studentId)).collect(Collectors.toList());
    }

    public List<Enrollment> findBySeminar(String seminarId) {
        return enrollments.stream().filter(e -> e.getSeminarId().equals(seminarId)).collect(Collectors.toList());
    }

    public List<Enrollment> findAll() {
        return new ArrayList<>(enrollments);
    }

    public Optional<Enrollment> findBySeminarAndStudent(String seminarId, String studentId) {
        return enrollments.stream()
                .filter(e -> e.getSeminarId().equals(seminarId) && e.getStudentId().equals(studentId))
                .findFirst();
    }
}
