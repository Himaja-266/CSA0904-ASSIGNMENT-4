import java.sql.SQLException;
import java.util.List;

public final class RegistrationService {
    private final RegistrationDao dao;

    public RegistrationService(RegistrationDao dao) {
        this.dao = dao;
    }

    public Registration register(String studentIdText, String studentName, long eventId)
            throws ValidationException, DuplicateRegistrationException, SQLException {
        String normalizedId = studentIdText == null ? "" : studentIdText.trim();
        String normalizedName = studentName == null ? "" : studentName.trim();
        long studentId;
        try {
            studentId = Long.parseLong(normalizedId);
        } catch (NumberFormatException exception) {
            throw new ValidationException("Student ID must be a positive whole number.");
        }
        if (studentId <= 0) {
            throw new ValidationException("Student ID must be a positive whole number.");
        }
        if (normalizedName.isEmpty()) {
            throw new ValidationException("Student name is required.");
        }
        if (eventId <= 0 || !dao.eventExists(eventId)) {
            throw new ValidationException("Select a valid event.");
        }
        if (dao.registrationExists(studentId, eventId)) {
            throw new DuplicateRegistrationException("This student is already registered for the event.");
        }
        return dao.register(studentId, normalizedName, eventId);
    }

    public List<EventInfo> listEvents() throws SQLException {
        return dao.listEvents();
    }

    public List<Registration> listRegistrations(Long eventId, int offset, int limit) throws SQLException {
        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException("Page offset must be non-negative and limit must be positive.");
        }
        return dao.listRegistrations(eventId, offset, limit);
    }
}
