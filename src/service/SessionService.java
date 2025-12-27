package service;

import model.Session;
import model.SessionType;
import repository.SessionRepository;
import util.VenueCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class SessionService {
    private final SessionRepository repository;

    public SessionService(SessionRepository repository) {
        this.repository = repository;
    }

    public Session create(LocalDate date, String venueCode, SessionType type) {
        validateVenue(venueCode);
        Session session = Session.createNew(date, venueCode, type);
        repository.upsert(session);
        return session;
    }

    public void update(Session session) {
        validateVenue(session.getVenueCode());
        repository.upsert(session);
    }

    public List<Session> findAll() {
        return repository.findAll();
    }

    public Optional<Session> findById(String id) {
        return repository.findById(id);
    }

    public void delete(String id) {
        repository.delete(id);
    }

    private void validateVenue(String code) {
        if (!VenueCode.isValid(code)) {
            throw new IllegalArgumentException("Invalid venue code");
        }
    }
}
