package repository;

import model.Award;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AwardRepository {
    private static final String FILE = "data/awards.csv";

    public AwardRepository() {
        try {
            new java.io.File(FILE).createNewFile();
        } catch (IOException ignored) {
        }
    }

    public synchronized List<Award> findAll() {
        List<Award> awards = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                awards.add(Award.fromCsv(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read awards", e);
        }
        return awards;
    }

    public synchronized void saveAll(List<Award> awards) {
        try (FileWriter fw = new FileWriter(FILE, false)) {
            for (Award a : awards) {
                fw.write(a.toCsv() + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save awards", e);
        }
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
