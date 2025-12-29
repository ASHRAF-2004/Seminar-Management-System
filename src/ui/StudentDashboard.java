package ui;

import model.Enrollment;
import model.Submission;
import service.EnrollmentService;
import service.SeminarService;
import service.SubmissionService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final SeminarService seminarService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final String studentId;
    private final DefaultTableModel seminarModel;
    private final DefaultTableModel enrollmentModel;
    private JTable seminarTable;

    private JTextField titleField;
    private JTextField abstractField;
    private JTextField supervisorField;
    private JComboBox<String> typeBox;
    private JTextField fileField;

    public StudentDashboard(SeminarService seminarService, EnrollmentService enrollmentService,
                             SubmissionService submissionService, String studentId) {
        super("Student Dashboard");
        this.seminarService = seminarService;
        this.enrollmentService = enrollmentService;
        this.submissionService = submissionService;
        this.studentId = studentId;
        this.seminarModel = new DefaultTableModel(new Object[]{"ID", "Title", "Date", "Venue", "Status"}, 0);
        this.enrollmentModel = new DefaultTableModel(new Object[]{"Enrollment ID", "Seminar", "Status"}, 0);
        buildUi();
        loadData();
        loadSubmission();
    }

    private void buildUi() {
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel heading = new JLabel("Available Seminars");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 16f));
        topPanel.add(heading);

        JButton enrollButton = new JButton("Enroll");
        topPanel.add(enrollButton);

        JButton refreshButton = new JButton("Refresh");
        topPanel.add(refreshButton);

        JButton proposeButton = new JButton("Propose Seminar" );
        topPanel.add(proposeButton);

        add(topPanel, BorderLayout.NORTH);

        seminarTable = new JTable(seminarModel);
        JScrollPane seminarScroll = new JScrollPane(seminarTable);

        JTable enrollmentTable = new JTable(enrollmentModel);
        JScrollPane enrollScroll = new JScrollPane(enrollmentTable);
        enrollScroll.setPreferredSize(new Dimension(250, 150));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, seminarScroll, enrollScroll);
        splitPane.setResizeWeight(0.5);

        JPanel submissionPanel = buildSubmissionPanel();

        JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPane, submissionPanel);
        outer.setResizeWeight(0.5);
        add(outer, BorderLayout.CENTER);

        enrollButton.addActionListener(e -> {
            int row = seminarTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a seminar to enroll", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            String seminarId = (String) seminarModel.getValueAt(row, 0);
            Enrollment enrollment = enrollmentService.enroll(seminarId, studentId);
            JOptionPane.showMessageDialog(this, "Enrollment saved. Status: " + enrollment.getStatus());
            loadEnrollments();
        });

        refreshButton.addActionListener(e -> {
            loadData();
            loadSubmission();
        });
        proposeButton.addActionListener(e -> openProposalDialog());
    }

    private JPanel buildSubmissionPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("My Presentation Submission"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        titleField = new JTextField();
        abstractField = new JTextField();
        supervisorField = new JTextField();
        typeBox = new JComboBox<>(new String[]{"ORAL", "POSTER"});
        fileField = new JTextField();
        JButton browse = new JButton("Browse...");

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                fileField.setText(file.getAbsolutePath());
            }
        });

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Research Title"), gbc);
        gbc.gridx = 1; panel.add(titleField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Abstract"), gbc);
        gbc.gridx = 1; panel.add(abstractField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Supervisor"), gbc);
        gbc.gridx = 1; panel.add(supervisorField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Presentation Type"), gbc);
        gbc.gridx = 1; panel.add(typeBox, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("File"), gbc);
        gbc.gridx = 1; panel.add(fileField, gbc);
        gbc.gridx = 2; panel.add(browse, gbc); y++;

        JButton save = new JButton("Save Submission");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 3; panel.add(save, gbc);

        save.addActionListener(e -> saveSubmission());

        return panel;
    }

    private void loadData() {
        seminarModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<model.Seminar> seminars = seminarService.getAll();
        for (model.Seminar s : seminars) {
            seminarModel.addRow(new Object[]{s.getId(), s.getTitle(), s.getDate().format(formatter), s.getVenue(), s.getStatus()});
        }
        loadEnrollments();
    }

    private void loadEnrollments() {
        enrollmentModel.setRowCount(0);
        List<Enrollment> enrollments = enrollmentService.findByStudent(studentId);
        for (Enrollment e : enrollments) {
            enrollmentModel.addRow(new Object[]{e.getId(), e.getSeminarId(), e.getStatus()});
        }
    }

    private void saveSubmission() {
        int row = seminarTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a seminar to register your research for", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (titleField.getText().isBlank() || abstractField.getText().isBlank() || supervisorField.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String seminarId = (String) seminarModel.getValueAt(row, 0);
        enrollmentService.enroll(seminarId, studentId);
        Submission submission = submissionService.saveOrUpdate(studentId, titleField.getText().trim(),
                abstractField.getText().trim(), supervisorField.getText().trim(), (String) typeBox.getSelectedItem(),
                fileField.getText().trim(), seminarId);
        JOptionPane.showMessageDialog(this, "Submission saved with ID " + submission.getId() + " and linked to seminar " + seminarId);
    }

    private void loadSubmission() {
        submissionService.findByStudent(studentId).ifPresent(sub -> {
            titleField.setText(sub.getResearchTitle());
            abstractField.setText(sub.getAbstractText());
            supervisorField.setText(sub.getSupervisorName());
            typeBox.setSelectedItem(sub.getPresentationType().toUpperCase());
            fileField.setText(sub.getFilePath());
            if (sub.getSeminarId() != null) {
                for (int i = 0; i < seminarModel.getRowCount(); i++) {
                    if (sub.getSeminarId().equals(seminarModel.getValueAt(i, 0))) {
                        seminarTable.setRowSelectionInterval(i, i);
                        break;
                    }
                }
            }
        });
    }

    private void openProposalDialog() {
        JTextField title = new JTextField();
        JTextField venue = new JTextField();
        JTextField dateField = new JTextField("2024-12-01");
        JComboBox<String> type = new JComboBox<>(new String[]{"ORAL", "POSTER"});

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Title"));
        panel.add(title);
        panel.add(new JLabel("Venue"));
        panel.add(venue);
        panel.add(new JLabel("Date (yyyy-mm-dd)"));
        panel.add(dateField);
        panel.add(new JLabel("Type"));
        panel.add(type);

        int result = JOptionPane.showConfirmDialog(this, panel, "Propose Seminar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                seminarService.createStudentProposal(title.getText().trim(), studentId, venue.getText().trim(), date, (String) type.getSelectedItem());
                JOptionPane.showMessageDialog(this, "Proposal submitted for coordinator approval (saved as draft)");
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid details: " + ex.getMessage());
            }
        }
    }
}
