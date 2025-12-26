package ui;

import model.Enrollment;
import model.Seminar;
import model.SeminarStatus;
import model.User;
import repository.UserRepository;
import service.EnrollmentService;
import service.SeminarService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CoordinatorDashboard extends JFrame {
    private final SeminarService seminarService;
    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;
    private final DefaultTableModel seminarModel;
    private final DefaultTableModel participantModel;

    public CoordinatorDashboard(SeminarService seminarService, EnrollmentService enrollmentService, UserRepository userRepository) {
        super("Coordinator Dashboard");
        this.seminarService = seminarService;
        this.enrollmentService = enrollmentService;
        this.userRepository = userRepository;
        this.seminarModel = new DefaultTableModel(new Object[]{"ID", "Title", "Date", "Venue", "Status"}, 0);
        this.participantModel = new DefaultTableModel(new Object[]{"Enrollment", "Student", "Seminar", "Status"}, 0);
        buildUi();
        loadSeminars();
    }

    private void buildUi() {
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Create Seminar");
        JButton editButton = new JButton("Edit Seminar");
        JButton refresh = new JButton("Refresh");
        top.add(addButton);
        top.add(editButton);
        top.add(refresh);
        add(top, BorderLayout.NORTH);

        JTable seminarTable = new JTable(seminarModel);
        JScrollPane seminarScroll = new JScrollPane(seminarTable);

        JTable participantTable = new JTable(participantModel);
        JScrollPane participantScroll = new JScrollPane(participantTable);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, seminarScroll, participantScroll);
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

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
    }

    private void loadSeminars() {
        seminarModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<Seminar> seminars = seminarService.getAll();
        for (Seminar s : seminars) {
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

    private void openSeminarDialog(Seminar seminar) {
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
        JComboBox<SeminarStatus> statusBox = new JComboBox<>(SeminarStatus.values());
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
                            (SeminarStatus) statusBox.getSelectedItem());
                } else {
                    seminar.setTitle(titleField.getText());
                    seminar.setPresenterId(presenterField.getText());
                    seminar.setAbstractText(abstractField.getText());
                    seminar.setSupervisor(supervisorField.getText());
                    seminar.setPresentationType(typeField.getText());
                    seminar.setVenue(venueField.getText());
                    seminar.setDate(date);
                    seminar.setStatus((SeminarStatus) statusBox.getSelectedItem());
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
}
