package repository;

import model.Session;
import util.AppConfig;
import util.FileUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SessionRepository {
    private final Path sessionFile;

    public SessionRepository() {
        this(AppConfig.SESSIONS_FILE);
    }

    public SessionRepository(Path sessionFile) {
        this.sessionFile = sessionFile;
    }

    public synchronized List<Session> findAll() {
        List<Session> sessions = new ArrayList<>();
        for (String line : FileUtils.readAllLines(sessionFile)) {
            if (!line.isBlank()) {
                sessions.add(Session.fromCsv(line));
            }
        }
        return sessions;
    }

    public synchronized void saveAll(List<Session> sessions) {
        List<String> lines = new ArrayList<>();
        for (Session s : sessions) {
            lines.add(s.toCsv());
        }
        FileUtils.writeLines(sessionFile, lines);
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
