package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Seminar {
    private String id;
    private String title;
    private String presenterId;
    private String abstractText;
    private String supervisor;
    private String presentationType;
    private String venue;
    private LocalDate date;
    private SeminarStatus status;

    public Seminar(String id, String title, String presenterId, String abstractText, String supervisor, String presentationType,
                   String venue, LocalDate date, SeminarStatus status) {
        this.id = id;
        this.title = title;
        this.presenterId = presenterId;
        this.abstractText = abstractText;
        this.supervisor = supervisor;
        this.presentationType = presentationType;
        this.venue = venue;
        this.date = date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPresenterId() {
        return presenterId;
    }

    public void setPresenterId(String presenterId) {
        this.presenterId = presenterId;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(String supervisor) {
        this.supervisor = supervisor;
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = presentationType;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public SeminarStatus getStatus() {
        return status;
    }

    public void setStatus(SeminarStatus status) {
        this.status = status;
    }

    public String toCsv() {
        return String.join("|",
                id,
                title.replace("|", "/"),
                presenterId,
                abstractText.replace("|", "/"),
                supervisor.replace("|", "/"),
                presentationType,
                venue.replace("|", "/"),
                date.format(DateTimeFormatter.ISO_DATE),
                status.name());
    }

    public static Seminar fromCsv(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 9) {
            throw new IllegalArgumentException("Invalid seminar row");
        }
        return new Seminar(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6],
                LocalDate.parse(parts[7]), SeminarStatus.valueOf(parts[8]));
    }
}
