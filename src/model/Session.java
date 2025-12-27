package model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Session {
    private final String id;
    private LocalDate date;
    private String venueCode;
    private SessionType sessionType;
    private final List<String> submissionIds;
    private final List<String> evaluatorIds;

    public Session(String id, LocalDate date, String venueCode, SessionType sessionType, List<String> submissionIds,
                   List<String> evaluatorIds) {
        this.id = id;
        this.date = date;
        this.venueCode = venueCode;
        this.sessionType = sessionType;
        this.submissionIds = submissionIds != null ? submissionIds : new ArrayList<>();
        this.evaluatorIds = evaluatorIds != null ? evaluatorIds : new ArrayList<>();
    }

    public static Session createNew(LocalDate date, String venueCode, SessionType type) {
        return new Session(UUID.randomUUID().toString(), date, venueCode, type, new ArrayList<>(), new ArrayList<>());
    }

    public String getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getVenueCode() {
        return venueCode;
    }

    public void setVenueCode(String venueCode) {
        this.venueCode = venueCode;
    }

    public SessionType getSessionType() {
        return sessionType;
    }

    public void setSessionType(SessionType sessionType) {
        this.sessionType = sessionType;
    }

    public List<String> getSubmissionIds() {
        return submissionIds;
    }

    public List<String> getEvaluatorIds() {
        return evaluatorIds;
    }

    public void addSubmission(String submissionId) {
        if (!submissionIds.contains(submissionId)) {
            submissionIds.add(submissionId);
        }
    }

    public void removeSubmission(String submissionId) {
        submissionIds.remove(submissionId);
    }

    public void addEvaluator(String evaluatorId) {
        if (!evaluatorIds.contains(evaluatorId)) {
            evaluatorIds.add(evaluatorId);
        }
    }

    public void removeEvaluator(String evaluatorId) {
        evaluatorIds.remove(evaluatorId);
    }

    public String toCsv() {
        String submissions = String.join(";", submissionIds);
        String evaluators = String.join(";", evaluatorIds);
        return String.join("|", id, date.toString(), venueCode, sessionType.name(), submissions, evaluators);
    }

    public static Session fromCsv(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid session row");
        }
        List<String> submissions = new ArrayList<>();
        if (!parts[4].isBlank()) {
            for (String s : parts[4].split(";")) {
                submissions.add(s);
            }
        }
        List<String> evaluators = new ArrayList<>();
        if (!parts[5].isBlank()) {
            for (String e : parts[5].split(";")) {
                evaluators.add(e);
            }
        }
        return new Session(parts[0], LocalDate.parse(parts[1]), parts[2], SessionType.valueOf(parts[3]), submissions, evaluators);
    }
}
