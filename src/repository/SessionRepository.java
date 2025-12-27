package repository;

import model.Session;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionRepository {
    private static final String FILE = "data/sessions.csv";

    public SessionRepository() {
        try {
            new java.io.File(FILE).createNewFile();
        } catch (IOException ignored) {
        }
    }

    public synchronized List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                sessions.add(Session.fromCsv(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to read sessions", e);
        }
        return sessions;
    }

    public synchronized void saveAll(List<Session> sessions) {
        try (FileWriter fw = new FileWriter(FILE, false)) {
            for (Session s : sessions) {
                fw.write(s.toCsv() + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new RuntimeException("Unable to save sessions", e);
        }
    }

    public synchronized void upsert(Session session) {
        List<Session> all = findAll();
        Optional<Session> existing = all.stream().filter(s -> s.getId().equals(session.getId())).findFirst();
        if (existing.isPresent()) {
            all.set(all.indexOf(existing.get()), session);
        } else {
            all.add(session);
        }
        saveAll(all);
    }

    public void delete(String id) {
        List<Session> all = findAll();
        all.removeIf(s -> s.getId().equals(id));
        saveAll(all);
    }

    public Optional<Session> findById(String id) {
        return findAll().stream().filter(s -> s.getId().equals(id)).findFirst();
    }
}
