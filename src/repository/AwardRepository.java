package repository;

import model.Award;
import util.AppConfig;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AwardRepository {
    private final Path awardsFile;

    public AwardRepository() {
        this(AppConfig.AWARDS_FILE);
    }

    public AwardRepository(Path awardsFile) {
        this.awardsFile = awardsFile;
    }

    public synchronized List<Award> findAll() {
        List<Award> awards = new ArrayList<>();
        for (String line : FileUtils.readAllLines(awardsFile)) {
            if (!line.isBlank()) {
                awards.add(Award.fromCsv(line));
            }
        }
        return awards;
    }

    public synchronized void saveAll(List<Award> awards) {
        List<String> lines = new ArrayList<>();
        for (Award a : awards) {
            lines.add(a.toCsv());
        }
        FileUtils.writeLines(awardsFile, lines);
    }

    public synchronized void upsert(Award award) {
        List<Award> all = findAll();
        Optional<Award> existing = all.stream().filter(a -> a.getId().equals(award.getId())).findFirst();
        if (existing.isPresent()) {
            all.set(all.indexOf(existing.get()), award);
        } else {
            all.add(award);
        }
        saveAll(all);
    }

    public Optional<Award> findById(String id) {
        return findAll().stream().filter(a -> a.getId().equals(id)).findFirst();
    }
}
