package repository;

import model.Seminar;
import util.AppConfig;
import util.DefaultData;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SeminarRepository {
    private final Path seminarFile;
    private final List<Seminar> seminars;

    public SeminarRepository() {
        this(AppConfig.SEMINARS_FILE);
    }

    public SeminarRepository(Path seminarFile) {
        this.seminarFile = seminarFile;
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
            seminars.addAll(DefaultData.defaultSeminars());
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
