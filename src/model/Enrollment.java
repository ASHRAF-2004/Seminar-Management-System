package model;

public class Enrollment {
    private final String id;
    private final String seminarId;
    private final String studentId;
    private String status;

    public Enrollment(String id, String seminarId, String studentId, String status) {
        this.id = id;
        this.seminarId = seminarId;
        this.studentId = studentId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getSeminarId() {
        return seminarId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toCsv() {
        return String.join("|", id, seminarId, studentId, status.replace("|", "/"));
    }

    public static Enrollment fromCsv(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 4) {
            throw new IllegalArgumentException("Invalid enrollment row");
        }
        return new Enrollment(parts[0], parts[1], parts[2], parts[3]);
    }
}
