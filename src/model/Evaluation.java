package model;

public class Evaluation {
    private final String id;
    private final String seminarId;
    private final String evaluatorId;
    private int problemClarity;
    private int methodology;
    private int results;
    private int presentation;
    private String comments;

    public Evaluation(String id, String seminarId, String evaluatorId, int problemClarity, int methodology, int results,
                      int presentation, String comments) {
        this.id = id;
        this.seminarId = seminarId;
        this.evaluatorId = evaluatorId;
        this.problemClarity = problemClarity;
        this.methodology = methodology;
        this.results = results;
        this.presentation = presentation;
        this.comments = comments;
    }

    public String getId() {
        return id;
    }

    public String getSeminarId() {
        return seminarId;
    }

    public String getEvaluatorId() {
        return evaluatorId;
    }

    public int getProblemClarity() {
        return problemClarity;
    }

    public void setProblemClarity(int problemClarity) {
        this.problemClarity = problemClarity;
    }

    public int getMethodology() {
        return methodology;
    }

    public void setMethodology(int methodology) {
        this.methodology = methodology;
    }

    public int getResults() {
        return results;
    }

    public void setResults(int results) {
        this.results = results;
    }

    public int getPresentation() {
        return presentation;
    }

    public void setPresentation(int presentation) {
        this.presentation = presentation;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public double getAverageScore() {
        return (problemClarity + methodology + results + presentation) / 4.0;
    }

    public String toCsv() {
        return String.join("|", id, seminarId, evaluatorId,
                String.valueOf(problemClarity), String.valueOf(methodology),
                String.valueOf(results), String.valueOf(presentation), comments.replace("|", "/"));
    }

    public static Evaluation fromCsv(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length < 8) {
            throw new IllegalArgumentException("Invalid evaluation row");
        }
        return new Evaluation(parts[0], parts[1], parts[2],
                Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),
                Integer.parseInt(parts[6]), parts[7]);
    }
}
