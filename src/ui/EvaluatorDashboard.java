package ui;

import model.Evaluation;
import model.Seminar;
import service.EvaluationService;
import service.SeminarService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class EvaluatorDashboard extends JFrame {
    private final SeminarService seminarService;
    private final EvaluationService evaluationService;
    private final String evaluatorId;
    private final DefaultTableModel seminarModel;

    public EvaluatorDashboard(SeminarService seminarService, EvaluationService evaluationService, String evaluatorId) {
        super("Evaluator Dashboard");
        this.seminarService = seminarService;
        this.evaluationService = evaluationService;
        this.evaluatorId = evaluatorId;
        this.seminarModel = new DefaultTableModel(new Object[]{"ID", "Title", "Status"}, 0);
        buildUi();
        loadSeminars();
    }

    private void buildUi() {
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTable table = new JTable(seminarModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton evaluateButton = new JButton("Evaluate");
        add(evaluateButton, BorderLayout.SOUTH);

        evaluateButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a seminar to evaluate");
                return;
            }
            String id = (String) seminarModel.getValueAt(row, 0);
            seminarService.findById(id).ifPresent(this::openEvaluationDialog);
        });
    }

    private void loadSeminars() {
        seminarModel.setRowCount(0);
        List<Seminar> seminars = seminarService.getAll();
        for (Seminar s : seminars) {
            seminarModel.addRow(new Object[]{s.getId(), s.getTitle(), s.getStatus()});
        }
    }

    private void openEvaluationDialog(Seminar seminar) {
        JDialog dialog = new JDialog(this, "Evaluate Seminar", true);
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JSpinner clarity = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JSpinner methodology = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JSpinner results = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JSpinner presentation = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JTextArea comments = new JTextArea(5, 20);

        evaluationService.findBySeminarAndEvaluator(seminar.getId(), evaluatorId).ifPresent(existing -> {
            clarity.setValue(existing.getProblemClarity());
            methodology.setValue(existing.getMethodology());
            results.setValue(existing.getResults());
            presentation.setValue(existing.getPresentation());
            comments.setText(existing.getComments());
        });

        int y = 0;
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
            Evaluation eval = evaluationService.saveEvaluation(seminar.getId(), evaluatorId,
                    (int) clarity.getValue(), (int) methodology.getValue(), (int) results.getValue(),
                    (int) presentation.getValue(), comments.getText());
            JOptionPane.showMessageDialog(dialog, "Saved. Average score: " + eval.getAverageScore());
            dialog.dispose();
        });

        dialog.setVisible(true);
    }
}
