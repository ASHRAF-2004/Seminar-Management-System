package ui;

import model.Enrollment;
import model.Role;
import model.Session;
import model.SessionType;
import model.Submission;
import model.User;
import repository.UserRepository;
import service.EnrollmentService;
import service.SessionService;
import service.SubmissionService;
import service.SeminarService;
import service.AwardService;
import service.ReportService;
import util.VenueCode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CoordinatorDashboard extends JFrame {
    private final SeminarService seminarService;
    private final EnrollmentService enrollmentService;
    private final SubmissionService submissionService;
    private final SessionService sessionService;
    private final AwardService awardService;
    private final ReportService reportService;
    private final UserRepository userRepository;
    private final DefaultTableModel seminarModel;
    private final DefaultTableModel participantModel;
    private final DefaultTableModel sessionModel;

    public CoordinatorDashboard(SeminarService seminarService, EnrollmentService enrollmentService,
                                SubmissionService submissionService, SessionService sessionService,
                                AwardService awardService, ReportService reportService,
                                UserRepository userRepository) {
        super("Coordinator Dashboard");
        this.seminarService = seminarService;
        this.enrollmentService = enrollmentService;
        this.submissionService = submissionService;
        this.sessionService = sessionService;
        this.awardService = awardService;
        this.reportService = reportService;
        this.userRepository = userRepository;
        this.seminarModel = new DefaultTableModel(new Object[]{"ID", "Title", "Date", "Venue", "Status"}, 0);
        this.participantModel = new DefaultTableModel(new Object[]{"Enrollment", "Student", "Seminar", "Status"}, 0);
        this.sessionModel = new DefaultTableModel(new Object[]{"ID", "Date", "Start", "End", "Venue", "Type", "Submissions", "Evaluators"}, 0);
        buildUi();
        loadSeminars();
        loadSessions();
    }

    private void buildUi() {
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Seminars", buildSeminarPanel());
        tabs.addTab("Sessions", buildSessionPanel());
        tabs.addTab("Reports", buildReportPanel());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildSeminarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Create Seminar");
        JButton editButton = new JButton("Edit Seminar");
        JButton refresh = new JButton("Refresh");
        top.add(addButton);
        top.add(editButton);
        top.add(refresh);
        panel.add(top, BorderLayout.NORTH);

        JTable seminarTable = new JTable(seminarModel);
        JScrollPane seminarScroll = new JScrollPane(seminarTable);

        JTable participantTable = new JTable(participantModel);
        JScrollPane participantScroll = new JScrollPane(participantTable);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, seminarScroll, participantScroll);
        splitPane.setResizeWeight(0.5);
        panel.add(splitPane, BorderLayout.CENTER);

        addButton.addActionListener(e -> openSeminarDialog(null));
        editButton.addActionListener(e -> {
            int row = seminarTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a seminar to edit");
                return;
            }
            String id = (String) seminarModel.getValueAt(row, 0);
            seminarService.findById(id).ifPresent(this::openSeminarDialog);
        });

        refresh.addActionListener(e -> loadSeminars());
        return panel;
    }

    private JPanel buildSessionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Create Session");
        JButton edit = new JButton("Edit Session");
        JButton delete = new JButton("Delete");
        JButton assign = new JButton("Assign presenters/evaluators");
        JButton exportSchedule = new JButton("Export Schedule");
        top.add(add);
        top.add(edit);
        top.add(delete);
        top.add(assign);
        top.add(exportSchedule);
        panel.add(top, BorderLayout.NORTH);

        JTable sessionTable = new JTable(sessionModel);
        panel.add(new JScrollPane(sessionTable), BorderLayout.CENTER);

        add.addActionListener(e -> openSessionDialog(null));
        edit.addActionListener(e -> {
            int row = sessionTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a session to edit");
                return;
            }
            String id = (String) sessionModel.getValueAt(row, 0);
            sessionService.findById(id).ifPresent(this::openSessionDialog);
        });
        delete.addActionListener(e -> {
            int row = sessionTable.getSelectedRow();
            if (row == -1) return;
            String id = (String) sessionModel.getValueAt(row, 0);
            sessionService.delete(id);
            loadSessions();
        });
        assign.addActionListener(e -> {
            int row = sessionTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a session first");
                return;
            }
            String id = (String) sessionModel.getValueAt(row, 0);
            sessionService.findById(id).ifPresent(this::openAssignmentDialog);
        });
        exportSchedule.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export schedule CSV");
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                reportService.exportSchedule(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Schedule exported");
            }
        });
        return panel;
    }

    private JPanel buildReportPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton exportEvaluations = new JButton("Export Evaluation Report");
        JButton exportAwards = new JButton("Export Award Agenda");
        panel.add(exportEvaluations);
        panel.add(exportAwards);

        exportEvaluations.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                reportService.exportEvaluationReport(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Evaluation report exported");
            }
        });

        exportAwards.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                reportService.exportAwardAgenda(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Award agenda exported");
            }
        });
        return panel;
    }

    private void loadSeminars() {
        seminarModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<model.Seminar> seminars = seminarService.getAll();
        for (model.Seminar s : seminars) {
            seminarModel.addRow(new Object[]{s.getId(), s.getTitle(), s.getDate().format(formatter), s.getVenue(), s.getStatus()});
        }
        loadParticipants();
    }

    private void loadParticipants() {
        participantModel.setRowCount(0);
        List<Enrollment> enrollments = enrollmentService.findAll();
        for (Enrollment enrollment : enrollments) {
            User student = userRepository.findById(enrollment.getStudentId());
            participantModel.addRow(new Object[]{
                    enrollment.getId(),
                    student != null ? student.getName() : enrollment.getStudentId(),
                    enrollment.getSeminarId(),
                    enrollment.getStatus()
            });
        }
    }

    private void loadSessions() {
        sessionModel.setRowCount(0);
        List<Session> sessions = sessionService.findAll();
        for (Session session : sessions) {
            sessionModel.addRow(new Object[]{
                    session.getId(),
                    session.getDate().toString(),
                    session.getStartTime().toString(),
                    session.getEndTime().toString(),
                    session.getVenueCode(),
                    session.getSessionType(),
                    session.getSubmissionIds().size(),
                    session.getEvaluatorIds().size()
            });
        }
    }

    private void openSeminarDialog(model.Seminar seminar) {
        JDialog dialog = new JDialog(this, seminar == null ? "Create Seminar" : "Edit Seminar", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(seminar != null ? seminar.getTitle() : "");
        JTextField presenterField = new JTextField(seminar != null ? seminar.getPresenterId() : "");
        JTextField abstractField = new JTextField(seminar != null ? seminar.getAbstractText() : "");
        JTextField supervisorField = new JTextField(seminar != null ? seminar.getSupervisor() : "");
        JTextField typeField = new JTextField(seminar != null ? seminar.getPresentationType() : "Oral");
        JTextField venueField = new JTextField(seminar != null ? seminar.getVenue() : "");
        JTextField dateField = new JTextField(seminar != null ? seminar.getDate().toString() : LocalDate.now().plusDays(7).toString());
        JComboBox<model.SeminarStatus> statusBox = new JComboBox<>(model.SeminarStatus.values());
        if (seminar != null) {
            statusBox.setSelectedItem(seminar.getStatus());
        }

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Title"), gbc);
        gbc.gridx = 1; dialog.add(titleField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Presenter ID"), gbc);
        gbc.gridx = 1; dialog.add(presenterField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Abstract"), gbc);
        gbc.gridx = 1; dialog.add(abstractField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Supervisor"), gbc);
        gbc.gridx = 1; dialog.add(supervisorField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Type"), gbc);
        gbc.gridx = 1; dialog.add(typeField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Venue"), gbc);
        gbc.gridx = 1; dialog.add(venueField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Date (yyyy-MM-dd)"), gbc);
        gbc.gridx = 1; dialog.add(dateField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Status"), gbc);
        gbc.gridx = 1; dialog.add(statusBox, gbc); y++;

        JButton save = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(save, gbc);

        save.addActionListener(ev -> {
            if (titleField.getText().isBlank() || presenterField.getText().isBlank()) {
                JOptionPane.showMessageDialog(dialog, "Title and presenter are required", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                if (seminar == null) {
                    seminarService.create(titleField.getText(), presenterField.getText(), abstractField.getText(),
                            supervisorField.getText(), typeField.getText(), venueField.getText(), date,
                            (model.SeminarStatus) statusBox.getSelectedItem());
                } else {
                    seminar.setTitle(titleField.getText());
                    seminar.setPresenterId(presenterField.getText());
                    seminar.setAbstractText(abstractField.getText());
                    seminar.setSupervisor(supervisorField.getText());
                    seminar.setPresentationType(typeField.getText());
                    seminar.setVenue(venueField.getText());
                    seminar.setDate(date);
                    seminar.setStatus((model.SeminarStatus) statusBox.getSelectedItem());
                    seminarService.update(seminar);
                }
                dialog.dispose();
                loadSeminars();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.setVisible(true);
    }

    private void openSessionDialog(Session session) {
        JDialog dialog = new JDialog(this, session == null ? "Create Session" : "Edit Session", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField dateField = new JTextField(session != null ? session.getDate().toString() : LocalDate.now().plusDays(1).toString());
        JTextField startField = new JTextField(session != null ? session.getStartTime().toString() : "09:00");
        JTextField endField = new JTextField(session != null ? session.getEndTime().toString() : "10:00");
        JTextField venueField = new JTextField(session != null ? session.getVenueCode() : "CQCR1024");
        JComboBox<SessionType> typeBox = new JComboBox<>(SessionType.values());
        if (session != null) {
            typeBox.setSelectedItem(session.getSessionType());
        }
        JLabel hint = new JLabel("Venue follows MMU code e.g. CQCR1024");

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Date (yyyy-MM-dd)"), gbc);
        gbc.gridx = 1; dialog.add(dateField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Start (HH:mm)"), gbc);
        gbc.gridx = 1; dialog.add(startField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("End (HH:mm)"), gbc);
        gbc.gridx = 1; dialog.add(endField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Venue Code"), gbc);
        gbc.gridx = 1; dialog.add(venueField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Session Type"), gbc);
        gbc.gridx = 1; dialog.add(typeBox, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(hint, gbc); y++;

        JButton save = new JButton("Save Session");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(save, gbc);

        save.addActionListener(ev -> {
            try {
                LocalDate date = LocalDate.parse(dateField.getText().trim());
                java.time.LocalTime start = java.time.LocalTime.parse(startField.getText().trim());
                java.time.LocalTime end = java.time.LocalTime.parse(endField.getText().trim());
                if (!VenueCode.isValid(venueField.getText().trim())) {
                    JOptionPane.showMessageDialog(dialog, "Invalid venue code", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (session == null) {
                    Session created = sessionService.create(date, venueField.getText().trim(), (SessionType) typeBox.getSelectedItem());
                    created.setStartTime(start);
                    created.setEndTime(end);
                    sessionService.update(created);
                } else {
                    session.setDate(date);
                    session.setStartTime(start);
                    session.setEndTime(end);
                    session.setVenueCode(venueField.getText().trim());
                    session.setSessionType((SessionType) typeBox.getSelectedItem());
                    sessionService.update(session);
                }
                dialog.dispose();
                loadSessions();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid input: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void openAssignmentDialog(Session session) {
        JDialog dialog = new JDialog(this, "Assign presenters & evaluators", true);
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        List<Submission> submissions = submissionService.getAll();
        JList<String> submissionList = new JList<>(submissions.stream()
                .map(s -> s.getId() + " - " + s.getResearchTitle()).toArray(String[]::new));
        submissionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        for (int i = 0; i < submissions.size(); i++) {
            if (session.getSubmissionIds().contains(submissions.get(i).getId())) {
                submissionList.addSelectionInterval(i, i);
            }
        }

        List<User> evaluators = userRepository.findAll().stream().filter(u -> u.getRole() == Role.EVALUATOR).collect(Collectors.toList());
        JList<String> evaluatorList = new JList<>(evaluators.stream()
                .map(u -> u.getId() + " - " + u.getName()).toArray(String[]::new));
        evaluatorList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        for (int i = 0; i < evaluators.size(); i++) {
            if (session.getEvaluatorIds().contains(evaluators.get(i).getId())) {
                evaluatorList.addSelectionInterval(i, i);
            }
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5; gbc.weighty = 1; dialog.add(new JScrollPane(submissionList), gbc);
        gbc.gridx = 1; dialog.add(new JScrollPane(evaluatorList), gbc);

        JButton save = new JButton("Save Assignments");
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.weighty = 0; dialog.add(save, gbc);

        save.addActionListener(ev -> {
            session.getSubmissionIds().clear();
            for (int idx : submissionList.getSelectedIndices()) {
                Submission sub = submissions.get(idx);
                session.addSubmission(sub.getId());
                if (session.getSessionType() == SessionType.POSTER && (sub.getPosterBoardId() == null || sub.getPosterBoardId().isBlank())) {
                    String boardId = JOptionPane.showInputDialog(dialog, "Board ID for " + sub.getResearchTitle());
                    if (boardId != null) {
                        sub.setPosterBoardId(boardId);
                        submissionService.save(sub);
                    }
                }
                sub.setSessionId(session.getId());
                submissionService.save(sub);
            }
            session.getEvaluatorIds().clear();
            for (int idx : evaluatorList.getSelectedIndices()) {
                session.addEvaluator(evaluators.get(idx).getId());
            }
            sessionService.update(session);
            dialog.dispose();
            loadSessions();
        });

        dialog.setVisible(true);
    }
}
