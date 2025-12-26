package repository;

import model.Seminar;
import model.SeminarStatus;
import util.FileUtils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeminarRepository {
    private final Path seminarFile = Path.of("data/seminars.csv");
    private final List<Seminar> seminars;

    public SeminarRepository() {
        seminars = new ArrayList<>();
        load();
        seedDefaults();
    }

    private void load() {
        for (String line : FileUtils.readAllLines(seminarFile)) {
            if (!line.isBlank()) {
                seminars.add(Seminar.fromCsv(line));
            }
        }
    }

    private void seedDefaults() {
        if (seminars.isEmpty()) {
            seminars.add(new Seminar("SEM-001", "AI for Healthcare", "stu1", "Using ML for diagnosis", "Dr. Smith", "Oral",
                    "Auditorium", LocalDate.now().plusDays(10), SeminarStatus.OPEN));
            seminars.add(new Seminar("SEM-002", "Blockchain Security", "stu1", "Ledger analysis", "Dr. Tan", "Poster",
                    "Gallery", LocalDate.now().plusDays(15), SeminarStatus.OPEN));
            save();
        }
    }

    public List<Seminar> findAll() {
        return new ArrayList<>(seminars);
    }

    public Optional<Seminar> findById(String id) {
        return seminars.stream().filter(s -> s.getId().equals(id)).findFirst();
    }

    public void addSeminar(Seminar seminar) {
        seminars.add(seminar);
        save();
    }

    public void update(Seminar seminar) {
        save();
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Seminar s : seminars) {
            lines.add(s.toCsv());
        }
        FileUtils.writeLines(seminarFile, lines);
    }
}
