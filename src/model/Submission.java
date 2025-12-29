package model;

import java.util.UUID;

public class Submission {
    private final String id;
    private final String studentId;
    private String researchTitle;
    private String abstractText;
    private String supervisorName;
    private String presentationType;
    private String filePath;
    private String seminarId;
    private String sessionId;
    private String posterBoardId;

    public Submission(String id, String studentId, String researchTitle, String abstractText, String supervisorName,
                      String presentationType, String filePath, String seminarId, String sessionId, String posterBoardId) {
        this.id = id;
        this.studentId = studentId;
        this.researchTitle = researchTitle;
        this.abstractText = abstractText;
        this.supervisorName = supervisorName;
        this.presentationType = presentationType;
        this.filePath = filePath;
        this.seminarId = seminarId;
        this.sessionId = sessionId;
        this.posterBoardId = posterBoardId;
    }

    public static Submission createNew(String studentId, String researchTitle, String abstractText, String supervisorName,
                                       String presentationType, String filePath) {
        return new Submission(UUID.randomUUID().toString(), studentId, researchTitle, abstractText, supervisorName,
                presentationType, filePath, "", "", "");
    }

    public String getId() {
        return id;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getResearchTitle() {
        return researchTitle;
    }

    public void setResearchTitle(String researchTitle) {
        this.researchTitle = researchTitle;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getSeminarId() {
        return seminarId;
    }

    public void setSeminarId(String seminarId) {
        this.seminarId = seminarId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getPosterBoardId() {
        return posterBoardId;
    }

    public void setPosterBoardId(String posterBoardId) {
        this.posterBoardId = posterBoardId;
    }

    public String toCsv() {
        return String.join("|", id, studentId, safe(researchTitle), safe(abstractText), safe(supervisorName),
                presentationType, safe(filePath), seminarId == null ? "" : seminarId, sessionId == null ? "" : sessionId, posterBoardId == null ? "" : posterBoardId);
    }

    public static Submission fromCsv(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid submission row");
        }
        if (parts.length == 9) {
            return new Submission(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], "", parts[7], parts[8]);
        }
        return new Submission(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8], parts[9]);
    }

    private String safe(String value) {
        return value == null ? "" : value.replace("|", "/");
    }
}
