package service;

import model.Enrollment;
import repository.EnrollmentRepository;
import util.IdGenerator;

import java.util.List;
import java.util.Optional;

public class EnrollmentService {
    private final EnrollmentRepository repository;

    public EnrollmentService(EnrollmentRepository repository) {
        this.repository = repository;
    }

    public Enrollment enroll(String seminarId, String studentId) {
        Optional<Enrollment> existing = repository.findBySeminarAndStudent(seminarId, studentId);
        if (existing.isPresent()) {
            return existing.get();
        }
        Enrollment enrollment = new Enrollment(IdGenerator.newId("ENR-"), seminarId, studentId, "Pending");
        repository.add(enrollment);
        return enrollment;
    }

    public List<Enrollment> findByStudent(String studentId) {
        return repository.findByStudent(studentId);
    }

    public List<Enrollment> findBySeminar(String seminarId) {
        return repository.findBySeminar(seminarId);
    }

    public List<Enrollment> findAll() {
        return repository.findAll();
    }
}
