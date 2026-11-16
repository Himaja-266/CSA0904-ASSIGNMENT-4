import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InMemoryRegistrationDao implements RegistrationDao {
    private final Map<Long, EventInfo> events = new LinkedHashMap<>();
    private final List<Registration> registrations = new ArrayList<>();
    private long nextRegistrationId = 1;

    public InMemoryRegistrationDao() {
        events.put(1L, new EventInfo(1, "Java Conference"));
        events.put(2L, new EventInfo(2, "Data Science Workshop"));
        events.put(3L, new EventInfo(3, "Design Symposium"));
        addSample(1001, "Avery Patel", 1);
        addSample(1002, "Jordan Kim", 2);
    }

    private void addSample(long studentId, String studentName, long eventId) {
        registrations.add(new Registration(nextRegistrationId++, studentId, studentName,
                eventId, events.get(eventId).name(), Instant.now()));
    }

    @Override
    public synchronized List<EventInfo> listEvents() {
        return List.copyOf(events.values());
    }

    @Override
    public synchronized boolean eventExists(long eventId) {
        return events.containsKey(eventId);
    }

    @Override
    public synchronized boolean registrationExists(long studentId, long eventId) {
        return registrations.stream().anyMatch(registration ->
                registration.studentId() == studentId && registration.eventId() == eventId);
    }

    @Override
    public synchronized Registration register(long studentId, String studentName, long eventId)
            throws SQLException, DuplicateRegistrationException {
        EventInfo event = events.get(eventId);
        if (event == null) {
            throw new SQLException("Selected event no longer exists.");
        }
        if (registrationExists(studentId, eventId)) {
            throw new DuplicateRegistrationException("This student is already registered for the event.");
        }
        Registration registration = new Registration(nextRegistrationId++, studentId,
                studentName, eventId, event.name(), Instant.now());
        registrations.add(registration);
        return registration;
    }

    @Override
    public synchronized List<Registration> listRegistrations(Long eventId, int offset, int limit) {
        List<Registration> filtered = registrations.stream()
                .filter(registration -> eventId == null || registration.eventId() == eventId)
                .sorted(Comparator.comparingLong(Registration::id).reversed())
                .toList();
        if (offset >= filtered.size()) {
            return List.of();
        }
        return List.copyOf(filtered.subList(offset, Math.min(filtered.size(), offset + limit)));
    }
}
