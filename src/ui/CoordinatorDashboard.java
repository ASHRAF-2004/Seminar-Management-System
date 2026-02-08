package ui;

import model.Award;
import model.AwardType;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
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
    private final DefaultTableModel awardModel;

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
        this.seminarModel = buildReadOnlyModel(new Object[]{"ID", "Title", "Date", "Venue", "Status"});
        this.participantModel = buildReadOnlyModel(new Object[]{"Enrollment", "Student", "Seminar", "Status"});
        this.sessionModel = buildReadOnlyModel(new Object[]{"ID", "Date", "Start", "End", "Venue", "Type", "Submissions", "Evaluators"});
        this.awardModel = buildReadOnlyModel(new Object[]{"ID", "Type", "Submission", "Session"});
        buildUi();
        loadSeminars();
        loadSessions();
        loadAwards();
    }

    private void buildUi() {
        setResizable(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Seminars", buildSeminarPanel());
        tabs.addTab("Sessions", buildSessionPanel());
        tabs.addTab("Awards", buildAwardPanel());
        tabs.addTab("Reports", buildReportPanel());
        add(tabs, BorderLayout.CENTER);
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
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
        JButton exportSummary = new JButton("Export Summary");
        panel.add(exportEvaluations);
        panel.add(exportAwards);
        panel.add(exportSummary);

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
        exportSummary.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                reportService.exportSummary(chooser.getSelectedFile().getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Summary exported");
            }
        });
        return panel;
    }

    private JPanel buildAwardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton add = new JButton("Nominate Winner");
        JButton edit = new JButton("Edit");
        JButton refresh = new JButton("Refresh");
        top.add(add);
        top.add(edit);
        top.add(refresh);
        panel.add(top, BorderLayout.NORTH);

        JTable table = new JTable(awardModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> openAwardDialog(null));
        edit.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select an award to edit");
                return;
            }
            String id = (String) awardModel.getValueAt(row, 0);
            awardService.findAll().stream().filter(a -> a.getId().equals(id)).findFirst().ifPresent(this::openAwardDialog);
        });
        refresh.addActionListener(e -> loadAwards());
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

    private void loadAwards() {
        awardModel.setRowCount(0);
        for (model.Award award : awardService.findAll()) {
            awardModel.addRow(new Object[]{award.getId(), award.getAwardType(), award.getSubmissionId(), award.getSessionId()});
        }
    }

    private void openSeminarDialog(model.Seminar seminar) {
        JDialog dialog = new JDialog(this, seminar == null ? "Create Seminar" : "Edit Seminar", true);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(seminar != null ? seminar.getTitle() : "");
        titleField.setColumns(30);
        JTextField presenterField = new JTextField(seminar != null ? seminar.getPresenterId() : "");
        presenterField.setColumns(30);
        JTextField abstractField = new JTextField(seminar != null ? seminar.getAbstractText() : "");
        abstractField.setColumns(30);
        JTextField supervisorField = new JTextField(seminar != null ? seminar.getSupervisor() : "");
        supervisorField.setColumns(30);
        JTextField typeField = new JTextField(seminar != null ? seminar.getPresentationType() : "Oral");
        typeField.setColumns(30);
        JTextField venueField = new JTextField(seminar != null ? seminar.getVenue() : "");
        venueField.setColumns(30);
        LocalDate today = LocalDate.now();
        LocalDate seminarDate = seminar != null ? seminar.getDate() : today.plusDays(7);
        if (seminarDate.isBefore(today)) {
            seminarDate = today;
        }
        JSpinner dateSpinner = buildDateSpinner(seminarDate, today);
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
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Date"), gbc);
        gbc.gridx = 1; dialog.add(dateSpinner, gbc); y++;
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
                LocalDate date = toLocalDate((Date) dateSpinner.getValue());
                if (date.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Seminar date cannot be in the past", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
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

        dialog.pack();
        dialog.setVisible(true);
    }

    private void openSessionDialog(Session session) {
        JDialog dialog = new JDialog(this, session == null ? "Create Session" : "Edit Session", true);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        LocalDate today = LocalDate.now();
        LocalDate sessionDate = session != null ? session.getDate() : today.plusDays(1);
        if (sessionDate.isBefore(today)) {
            sessionDate = today;
        }
        JSpinner dateSpinner = buildDateSpinner(sessionDate, today);
        LocalTime startTime = session != null ? session.getStartTime() : LocalTime.of(9, 0);
        LocalTime endTime = session != null ? session.getEndTime() : LocalTime.of(10, 0);
        JSpinner startSpinner = buildTimeSpinner(startTime);
        JSpinner endSpinner = buildTimeSpinner(endTime);
        JTextField venueField = new JTextField(session != null ? session.getVenueCode() : "CQCR1024");
        venueField.setColumns(24);
        JComboBox<SessionType> typeBox = new JComboBox<>(SessionType.values());
        if (session != null) {
            typeBox.setSelectedItem(session.getSessionType());
        }
        JLabel hint = new JLabel("Venue follows MMU code e.g. CQCR1024");

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Date"), gbc);
        gbc.gridx = 1; dialog.add(dateSpinner, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Start time"), gbc);
        gbc.gridx = 1; dialog.add(startSpinner, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("End time"), gbc);
        gbc.gridx = 1; dialog.add(endSpinner, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Venue Code"), gbc);
        gbc.gridx = 1; dialog.add(venueField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Session Type"), gbc);
        gbc.gridx = 1; dialog.add(typeBox, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(hint, gbc); y++;

        JButton save = new JButton("Save Session");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(save, gbc);

        save.addActionListener(ev -> {
            try {
                LocalDate date = toLocalDate((Date) dateSpinner.getValue());
                LocalTime start = toLocalTime((Date) startSpinner.getValue());
                LocalTime end = toLocalTime((Date) endSpinner.getValue());
                if (date.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(dialog, "Session date cannot be in the past", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!end.isAfter(start)) {
                    JOptionPane.showMessageDialog(dialog, "End time must be after the start time", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (date.isEqual(LocalDate.now()) && LocalDateTime.of(date, start).isBefore(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(dialog, "Session start time cannot be in the past", "Validation", JOptionPane.WARNING_MESSAGE);
                    return;
                }
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

        dialog.pack();
        dialog.setVisible(true);
    }

    private void openAssignmentDialog(Session session) {
        JDialog dialog = new JDialog(this, "Assign presenters & evaluators", true);
        dialog.setResizable(true);
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

        dialog.pack();
        dialog.setVisible(true);
    }

    private void openAwardDialog(Award award) {
        JDialog dialog = new JDialog(this, award == null ? "Nominate Award" : "Edit Award", true);
        dialog.setResizable(true);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<AwardType> typeBox = new JComboBox<>(AwardType.values());
        List<Submission> submissions = submissionService.getAll();
        JComboBox<String> submissionBox = new JComboBox<>(submissions.stream()
                .map(s -> s.getId() + " - " + s.getResearchTitle())
                .toArray(String[]::new));
        List<Session> sessions = sessionService.findAll();
        JComboBox<String> sessionBox = new JComboBox<>(sessions.stream()
                .map(s -> s.getId() + " (" + s.getSessionType() + ")")
                .toArray(String[]::new));

        if (award != null) {
            typeBox.setSelectedItem(award.getAwardType());
            for (int i = 0; i < submissionBox.getItemCount(); i++) {
                if (submissionBox.getItemAt(i).startsWith(award.getSubmissionId())) {
                    submissionBox.setSelectedIndex(i);
                    break;
                }
            }
            for (int i = 0; i < sessionBox.getItemCount(); i++) {
                if (sessionBox.getItemAt(i).startsWith(award.getSessionId())) {
                    sessionBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Award Type"), gbc);
        gbc.gridx = 1; dialog.add(typeBox, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Submission"), gbc);
        gbc.gridx = 1; dialog.add(submissionBox, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; dialog.add(new JLabel("Session"), gbc);
        gbc.gridx = 1; dialog.add(sessionBox, gbc); y++;

        JButton save = new JButton("Save");
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 2; dialog.add(save, gbc);

        save.addActionListener(ev -> {
            if (submissionBox.getSelectedIndex() == -1 || sessionBox.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(dialog, "Please select submission and session");
                return;
            }
            String submissionId = submissions.get(submissionBox.getSelectedIndex()).getId();
            String sessionId = sessions.get(sessionBox.getSelectedIndex()).getId();
            AwardType type = (AwardType) typeBox.getSelectedItem();
            Award updated = award != null ? award : Award.createNew(submissionId, sessionId, type);
            updated.setAwardType(type);
            awardService.save(updated);
            dialog.dispose();
            loadAwards();
        });

        dialog.pack();
        dialog.setVisible(true);
    }

    private DefaultTableModel buildReadOnlyModel(Object[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JSpinner buildDateSpinner(LocalDate date, LocalDate minDate) {
        Date value = toDate(date);
        Date min = minDate != null ? toDate(minDate) : null;
        SpinnerDateModel model = new SpinnerDateModel(value, min, null, Calendar.DAY_OF_MONTH);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        return spinner;
    }

    private JSpinner buildTimeSpinner(LocalTime time) {
        Date value = toTimeDate(time);
        SpinnerDateModel model = new SpinnerDateModel(value, null, null, Calendar.MINUTE);
        JSpinner spinner = new JSpinner(model);
        spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
        return spinner;
    }

    private LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }

    private Date toDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date toTimeDate(LocalTime time) {
        return Date.from(time.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant());
    }
}
