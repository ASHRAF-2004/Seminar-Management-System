package ui;

import model.Enrollment;
import model.Seminar;
import service.EnrollmentService;
import service.SeminarService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDashboard extends JFrame {
    private final SeminarService seminarService;
    private final EnrollmentService enrollmentService;
    private final String studentId;
    private final DefaultTableModel seminarModel;
    private final DefaultTableModel enrollmentModel;

    public StudentDashboard(SeminarService seminarService, EnrollmentService enrollmentService, String studentId) {
        super("Student Dashboard");
        this.seminarService = seminarService;
        this.enrollmentService = enrollmentService;
        this.studentId = studentId;
        this.seminarModel = new DefaultTableModel(new Object[]{"ID", "Title", "Date", "Venue", "Status"}, 0);
        this.enrollmentModel = new DefaultTableModel(new Object[]{"Enrollment ID", "Seminar", "Status"}, 0);
        buildUi();
        loadData();
    }

    private void buildUi() {
        setSize(700, 500);
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

        add(topPanel, BorderLayout.NORTH);

        JTable seminarTable = new JTable(seminarModel);
        JScrollPane seminarScroll = new JScrollPane(seminarTable);

        JTable enrollmentTable = new JTable(enrollmentModel);
        JScrollPane enrollScroll = new JScrollPane(enrollmentTable);
        enrollScroll.setPreferredSize(new Dimension(250, 150));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, seminarScroll, enrollScroll);
        splitPane.setResizeWeight(0.7);
        add(splitPane, BorderLayout.CENTER);

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

        refreshButton.addActionListener(e -> loadData());
    }

    private void loadData() {
        seminarModel.setRowCount(0);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        List<Seminar> seminars = seminarService.getAll();
        for (Seminar s : seminars) {
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
}
