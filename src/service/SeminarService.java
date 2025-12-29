package service;

import model.Seminar;
import model.SeminarStatus;
import repository.SeminarRepository;
import util.IdGenerator;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SeminarService {
    private final SeminarRepository repository;

    public SeminarService(SeminarRepository repository) {
        this.repository = repository;
    }

    public List<Seminar> getAll() {
        return repository.findAll();
    }

    public Optional<Seminar> findById(String id) {
        return repository.findById(id);
    }

    public Seminar create(String title, String presenterId, String abstractText, String supervisor, String presentationType,
                          String venue, LocalDate date, SeminarStatus status) {
        Seminar seminar = new Seminar(IdGenerator.newId("SEM-"), title, presenterId, abstractText, supervisor,
                presentationType, venue, date, status);
        repository.addSeminar(seminar);
        return seminar;
    }

    public Seminar createStudentProposal(String title, String presenterId, String venue, LocalDate date, String presentationType) {
        return create(title, presenterId, "", "", presentationType, venue, date, SeminarStatus.DRAFT);
    }

    public void update(Seminar seminar) {
        repository.update(seminar);
    }
}
