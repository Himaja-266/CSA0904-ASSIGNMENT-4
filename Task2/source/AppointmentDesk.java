import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AppointmentDesk extends Frame {
    public record Doctor(String name, String specialty) {
    }

    private final Map<String, java.util.List<Doctor>> doctorsByDepartment;
    private final Map<String, java.util.List<String>> slotsByDoctor = Map.of(
            "Dr. Rao", java.util.List.of("09:00", "10:30"),
            "Dr. Shah", java.util.List.of("11:00", "14:00"),
            "Dr. Mehta", java.util.List.of("09:30", "15:00"));
    private final Choice departmentChoice = new Choice();
    private final Choice doctorChoice = new Choice();
    private final List slotList = new List(5);
    private final Label status = new Label("Choose a department, doctor, and slot.");

    public AppointmentDesk() {
        super("Hospital Appointment Desk");
        doctorsByDepartment = createDepartments();
        doctorsByDepartment.keySet().forEach(departmentChoice::add);

        Panel selectors = new Panel();
        selectors.add(new Label("Department:"));
        selectors.add(departmentChoice);
        selectors.add(new Label("Doctor:"));
        selectors.add(doctorChoice);

        Panel center = new Panel(new BorderLayout(8, 8));
        center.add(new Label("Available slots:"), BorderLayout.NORTH);
        center.add(slotList, BorderLayout.CENTER);

        Button bookButton = new Button("Book");
        Panel footer = new Panel(new BorderLayout());
        footer.add(bookButton, BorderLayout.WEST);
        footer.add(status, BorderLayout.CENTER);

        add(selectors, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        departmentChoice.addItemListener(event -> refreshDoctors());
        doctorChoice.addItemListener(event -> refreshSlots());
        slotList.addItemListener(event -> status.setText("Selected slot: " + slotList.getSelectedItem()));
        bookButton.addActionListener(event -> book());
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                dispose();
            }
        });

        setSize(560, 300);
        setLocationRelativeTo(null);
        refreshDoctors();
    }

    private static Map<String, java.util.List<Doctor>> createDepartments() {
        Map<String, java.util.List<Doctor>> departments = new LinkedHashMap<>();
        departments.put("Cardiology", java.util.List.of(
                new Doctor("Dr. Rao", "Cardiology"),
                new Doctor("Dr. Shah", "Cardiology")));
        departments.put("Neurology", java.util.List.of(
                new Doctor("Dr. Mehta", "Neurology")));
        return departments;
    }

    private void refreshDoctors() {
        doctorChoice.removeAll();
        java.util.List<Doctor> doctors = doctorsByDepartment.get(departmentChoice.getSelectedItem());
        if (doctors != null) {
            doctors.forEach(doctor -> doctorChoice.add(doctor.name()));
        }
        refreshSlots();
    }

    private void refreshSlots() {
        slotList.removeAll();
        java.util.List<String> slots = slotsByDoctor.get(doctorChoice.getSelectedItem());
        if (slots != null) {
            slots.forEach(slotList::add);
        }
    }

    private void book() {
        String doctor = doctorChoice.getSelectedItem();
        String slot = slotList.getSelectedItem();
        if (doctor == null || slot == null) {
            status.setText("Select a doctor and an available slot.");
            return;
        }
        status.setText("Booked " + doctor + " at " + slot + ".");
    }

    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(() -> new AppointmentDesk().setVisible(true));
    }
}
