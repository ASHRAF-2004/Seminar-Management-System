package ui;

import model.Evaluation;
import model.Session;
import model.Submission;
import service.EvaluationService;
import service.SessionService;
import service.SubmissionService;
import service.SeminarService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EvaluatorDashboard extends JFrame {
    private final SeminarService seminarService;
    private final EvaluationService evaluationService;
    private final SubmissionService submissionService;
    private final SessionService sessionService;
    private final String evaluatorId;
    private final DefaultTableModel submissionModel;

    public EvaluatorDashboard(SeminarService seminarService, EvaluationService evaluationService,
                              SubmissionService submissionService, SessionService sessionService, String evaluatorId) {
        super("Evaluator Dashboard");
        this.seminarService = seminarService;
        this.evaluationService = evaluationService;
        this.submissionService = submissionService;
        this.sessionService = sessionService;
        this.evaluatorId = evaluatorId;
        this.submissionModel = new DefaultTableModel(new Object[]{"Submission", "Title", "Session", "Type"}, 0);
        buildUi();
        loadSubmissions();
    }

    private void buildUi() {
        setSize(700, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTable table = new JTable(submissionModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton evaluateButton = new JButton("Evaluate");
        add(evaluateButton, BorderLayout.SOUTH);

        evaluateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a presentation to evaluate");
                return;
            }
            String submissionId = (String) submissionModel.getValueAt(row, 0);
            submissionService.findById(submissionId).ifPresent(this::openEvaluationDialog);
        });
    }

    private void loadSubmissions() {
        submissionModel.setRowCount(0);
        List<Submission> assigned = getAssignedSubmissions();
        for (Submission s : assigned) {
            submissionModel.addRow(new Object[]{s.getId(), s.getResearchTitle(), s.getSessionId(), s.getPresentationType()});
        }
    }

    private List<Submission> getAssignedSubmissions() {
        List<Submission> result = new ArrayList<>();
        List<Session> sessions = sessionService.findAll();
        for (Session session : sessions) {
            if (!session.getEvaluatorIds().contains(evaluatorId)) continue;
            for (String subId : session.getSubmissionIds()) {
                submissionService.findById(subId).ifPresent(sub -> {
                    sub.setSessionId(session.getId());
                    result.add(sub);
                });
            }
        }
        return result;
    }

    private void openEvaluationDialog(Submission submission) {
        JDialog dialog = new JDialog(this, "Evaluate Presentation", true);
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel sessionLabel = new JLabel("Session: " + Optional.ofNullable(submission.getSessionId()).orElse("Unassigned"));
        JLabel titleLabel = new JLabel("Title: " + submission.getResearchTitle());
        JLabel supervisorLabel = new JLabel("Supervisor: " + submission.getSupervisorName());
        JLabel typeLabel = new JLabel("Type: " + submission.getPresentationType());

        JSpinner clarity = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JSpinner methodology = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JSpinner results = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JSpinner presentation = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JTextArea comments = new JTextArea(5, 20);

        evaluationService.findBySeminarAndEvaluator(submission.getId(), evaluatorId).ifPresent(existing -> {
            clarity.setValue(existing.getProblemClarity());
            methodology.setValue(existing.getMethodology());
            results.setValue(existing.getResults());
            presentation.setValue(existing.getPresentation());
            comments.setText(existing.getComments());
        });

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(sessionLabel, gbc); y++;
        gbc.gridy = y; dialog.add(titleLabel, gbc); y++;
        gbc.gridy = y; dialog.add(supervisorLabel, gbc); y++;
        gbc.gridy = y; dialog.add(typeLabel, gbc); y++;

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Problem Clarity"), gbc);
        gbc.gridx = 1; dialog.add(clarity, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Methodology"), gbc);
        gbc.gridx = 1; dialog.add(methodology, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Results"), gbc);
        gbc.gridx = 1; dialog.add(results, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Presentation"), gbc);
        gbc.gridx = 1; dialog.add(presentation, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTH; dialog.add(new JLabel("Comments"), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(comments), gbc); y++;

        JButton save = new JButton("Save Evaluation");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(save, gbc);

        save.addActionListener(e -> {
            Evaluation eval = evaluationService.saveEvaluation(submission.getId(), evaluatorId,
                    (int) clarity.getValue(), (int) methodology.getValue(), (int) results.getValue(),
                    (int) presentation.getValue(), comments.getText());
            JOptionPane.showMessageDialog(dialog, "Saved. Average score: " + eval.getAverageScore());
            dialog.dispose();
        });

        dialog.setVisible(true);
    }
}
