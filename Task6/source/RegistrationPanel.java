import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public final class RegistrationPanel extends JPanel {
    private static final int PAGE_SIZE = 50;
    private static final int EXPORT_PAGE_SIZE = 500;

    private record FilterOption(Long eventId, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    private record LoadResult(List<EventInfo> events, List<Registration> registrations) {
    }

    private RegistrationService service;
    private String connectionNotice;
    private final JTextField studentIdField = new JTextField(12);
    private final JTextField studentNameField = new JTextField(18);
    private final JComboBox<EventInfo> eventChoice = new JComboBox<>();
    private final JComboBox<FilterOption> filterChoice = new JComboBox<>();
    private final RegistrationTableModel tableModel = new RegistrationTableModel();
    private final JTable table = new JTable(tableModel);
    private final JButton registerButton = new JButton("Register");
    private final JButton previousButton = new JButton("Prev");
    private final JButton nextButton = new JButton("Next");
    private final JButton refreshButton = new JButton("Refresh");
    private final JButton exportButton = new JButton("Export CSV");
    private final JLabel status = new JLabel("Ready.");
    private final JProgressBar progress = new JProgressBar();
    private boolean updatingFilters;
    private boolean busy;
    private boolean reloadPending;
    private int pageOffset;

    public RegistrationPanel(RegistrationService service, String connectionNotice) {
        super(new BorderLayout(8, 8));
        this.service = service;
        this.connectionNotice = connectionNotice;

        JPanel form = new JPanel(new GridLayout(2, 4, 8, 4));
        form.add(new JLabel("Student ID"));
        form.add(new JLabel("Student name"));
        form.add(new JLabel("Event"));
        form.add(new JLabel());
        form.add(studentIdField);
        form.add(studentNameField);
        form.add(eventChoice);
        form.add(registerButton);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.add(new JLabel("Filter event:"));
        controls.add(filterChoice);
        controls.add(previousButton);
        controls.add(nextButton);
        controls.add(refreshButton);
        controls.add(exportButton);

        JPanel north = new JPanel(new BorderLayout());
        north.add(form, BorderLayout.NORTH);
        north.add(controls, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(8, 0));
        progress.setIndeterminate(true);
        progress.setVisible(false);
        footer.add(status, BorderLayout.CENTER);
        footer.add(progress, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        registerButton.addActionListener(event -> register());
        filterChoice.addActionListener(event -> {
            if (!updatingFilters && !busy) {
                pageOffset = 0;
                loadPage();
            }
        });
        previousButton.addActionListener(event -> changePage(-1));
        nextButton.addActionListener(event -> changePage(1));
        refreshButton.addActionListener(event -> loadPage());
        exportButton.addActionListener(event -> exportCsv());
        loadInitial();
    }

    public void setService(RegistrationService service, String connectionNotice) {
        this.service = service;
        this.connectionNotice = connectionNotice;
        pageOffset = 0;
        if (busy) {
            reloadPending = true;
        } else {
            loadInitial();
        }
    }

    public void refresh() {
        pageOffset = 0;
        loadPage();
    }

    public void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(Path.of("registrations.csv").toFile());
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destination = chooser.getSelectedFile().toPath();
        Long eventId = selectedFilterId();
        RegistrationService currentService = service;
        setBusy(true, "Exporting registrations...");
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                int exported = 0;
                try (BufferedWriter writer = Files.newBufferedWriter(destination, StandardCharsets.UTF_8)) {
                    writer.write("registration_id,student_id,student_name,event_id,event_name,registered_at");
                    writer.newLine();
                    int offset = 0;
                    while (true) {
                        List<Registration> rows = currentService.listRegistrations(eventId, offset, EXPORT_PAGE_SIZE);
                        for (Registration registration : rows) {
                            writer.write(csv(registration.id()) + ","
                                    + csv(registration.studentId()) + ","
                                    + csv(registration.studentName()) + ","
                                    + csv(registration.eventId()) + ","
                                    + csv(registration.eventName()) + ","
                                    + csv(registration.registeredAt()));
                            writer.newLine();
                        }
                        exported += rows.size();
                        if (rows.size() < EXPORT_PAGE_SIZE) {
                            break;
                        }
                        offset += rows.size();
                    }
                }
                return exported;
            }

            @Override
            protected void done() {
                setBusy(false, connectionNotice);
                try {
                    JOptionPane.showMessageDialog(RegistrationPanel.this,
                            get() + " registrations exported to " + destination,
                            "Export complete", JOptionPane.INFORMATION_MESSAGE);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("CSV export was interrupted.");
                } catch (ExecutionException exception) {
                    showFailure(exception.getCause());
                }
            }
        }.execute();
    }

    private void loadInitial() {
        if (busy) {
            return;
        }
        RegistrationService currentService = service;
        setBusy(true, "Loading events and registrations...");
        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws SQLException {
                List<EventInfo> events = currentService.listEvents();
                List<Registration> registrations = currentService.listRegistrations(null, 0, PAGE_SIZE);
                return new LoadResult(events, registrations);
            }

            @Override
            protected void done() {
                try {
                    LoadResult loaded = get();
                    updatingFilters = true;
                    eventChoice.removeAllItems();
                    filterChoice.removeAllItems();
                    filterChoice.addItem(new FilterOption(null, "All events"));
                    for (EventInfo event : loaded.events()) {
                        eventChoice.addItem(event);
                        filterChoice.addItem(new FilterOption(event.id(), event.name()));
                    }
                    updatingFilters = false;
                    pageOffset = 0;
                    tableModel.setRegistrations(loaded.registrations());
                    updatePaging(loaded.registrations().size());
                    setBusy(false, connectionNotice + " Loaded " + loaded.registrations().size() + " rows.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    setBusy(false, "Loading was interrupted.");
                    showError("Loading was interrupted.");
                } catch (ExecutionException exception) {
                    updatingFilters = false;
                    setBusy(false, "Could not load registrations.");
                    showFailure(exception.getCause());
                }
            }
        }.execute();
    }

    private void loadPage() {
        if (busy) {
            return;
        }
        Long eventId = selectedFilterId();
        RegistrationService currentService = service;
        setBusy(true, "Loading registrations...");
        new SwingWorker<List<Registration>, Void>() {
            @Override
            protected List<Registration> doInBackground() throws SQLException {
                return currentService.listRegistrations(eventId, pageOffset, PAGE_SIZE);
            }

            @Override
            protected void done() {
                try {
                    List<Registration> registrations = get();
                    tableModel.setRegistrations(registrations);
                    updatePaging(registrations.size());
                    setBusy(false, connectionNotice + " Showing " + registrations.size() + " rows.");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    setBusy(false, "Loading was interrupted.");
                    showError("Loading was interrupted.");
                } catch (ExecutionException exception) {
                    setBusy(false, "Could not load registrations.");
                    showFailure(exception.getCause());
                }
            }
        }.execute();
    }

    private void register() {
        String studentId = studentIdField.getText().trim();
        String studentName = studentNameField.getText().trim();
        EventInfo selectedEvent = (EventInfo) eventChoice.getSelectedItem();
        if (studentId.isEmpty() || studentName.isEmpty() || selectedEvent == null) {
            showError("Enter a student ID and name, then select an event.");
            return;
        }
        try {
            if (Long.parseLong(studentId) <= 0) {
                showError("Student ID must be a positive whole number.");
                return;
            }
        } catch (NumberFormatException exception) {
            showError("Student ID must be a positive whole number.");
            return;
        }

        RegistrationService currentService = service;
        setBusy(true, "Registering student...");
        new SwingWorker<Registration, Void>() {
            @Override
            protected Registration doInBackground()
                    throws SQLException, ValidationException, DuplicateRegistrationException {
                return currentService.register(studentId, studentName, selectedEvent.id());
            }

            @Override
            protected void done() {
                setBusy(false, connectionNotice);
                try {
                    Registration registration = get();
                    studentIdField.setText("");
                    studentNameField.setText("");
                    pageOffset = 0;
                    JOptionPane.showMessageDialog(RegistrationPanel.this,
                            "Registered " + registration.studentName() + " for " + registration.eventName(),
                            "Registration complete", JOptionPane.INFORMATION_MESSAGE);
                    loadPage();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    showError("Registration was interrupted.");
                } catch (ExecutionException exception) {
                    showFailure(exception.getCause());
                }
            }
        }.execute();
    }

    private void changePage(int direction) {
        pageOffset = Math.max(0, pageOffset + direction * PAGE_SIZE);
        loadPage();
    }

    private Long selectedFilterId() {
        FilterOption selected = (FilterOption) filterChoice.getSelectedItem();
        return selected == null ? null : selected.eventId();
    }

    private void updatePaging(int rowCount) {
        previousButton.setEnabled(pageOffset > 0);
        nextButton.setEnabled(rowCount == PAGE_SIZE);
    }

    private void setBusy(boolean busy, String message) {
        this.busy = busy;
        status.setText(message);
        progress.setVisible(busy);
        registerButton.setEnabled(!busy && eventChoice.getItemCount() > 0);
        filterChoice.setEnabled(!busy);
        previousButton.setEnabled(!busy && pageOffset > 0);
        nextButton.setEnabled(!busy && tableModel.getRowCount() == PAGE_SIZE);
        refreshButton.setEnabled(!busy);
        exportButton.setEnabled(!busy);
        if (!busy && reloadPending) {
            reloadPending = false;
            SwingUtilities.invokeLater(this::loadInitial);
        }
    }

    private void showFailure(Throwable cause) {
        String message = cause == null || cause.getMessage() == null
                ? "The operation failed."
                : cause.getMessage();
        showError(message);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Registration error", JOptionPane.ERROR_MESSAGE);
    }

    private static String csv(Object value) {
        String text = String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
