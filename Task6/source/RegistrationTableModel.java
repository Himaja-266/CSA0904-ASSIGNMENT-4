import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.table.AbstractTableModel;

public final class RegistrationTableModel extends AbstractTableModel {
    private static final String[] COLUMNS = {"Registration ID", "Student ID", "Student", "Event", "Registered"};
    private List<Registration> registrations = List.of();

    public void setRegistrations(List<Registration> registrations) {
        this.registrations = List.copyOf(registrations);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return registrations.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Registration registration = registrations.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> registration.id();
            case 1 -> registration.studentId();
            case 2 -> registration.studentName();
            case 3 -> registration.eventName();
            case 4 -> DateTimeFormatter.ISO_INSTANT.format(registration.registeredAt());
            default -> throw new IndexOutOfBoundsException("Unknown column " + columnIndex);
        };
    }
}
