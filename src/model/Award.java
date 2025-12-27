package model;

import java.util.UUID;

public class Award {
    private final String id;
    private final String submissionId;
    private final String sessionId;
    private AwardType awardType;

    public Award(String id, String submissionId, String sessionId, AwardType awardType) {
        this.id = id;
        this.submissionId = submissionId;
        this.sessionId = sessionId;
        this.awardType = awardType;
    }

    public static Award createNew(String submissionId, String sessionId, AwardType type) {
        return new Award(UUID.randomUUID().toString(), submissionId, sessionId, type);
    }

    public String getId() {
        return id;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public AwardType getAwardType() {
        return awardType;
    }

    public void setAwardType(AwardType awardType) {
        this.awardType = awardType;
    }

    public String toCsv() {
        return String.join("|", id, submissionId, sessionId, awardType.name());
    }

    public static Award fromCsv(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid award row");
        }
        return new Award(parts[0], parts[1], parts[2], AwardType.valueOf(parts[3]));
    }
}
