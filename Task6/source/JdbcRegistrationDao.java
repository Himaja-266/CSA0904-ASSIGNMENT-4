import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class JdbcRegistrationDao implements RegistrationDao {
    private final String url;
    private final String user;
    private final String password;

    public JdbcRegistrationDao(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void ping() throws SQLException {
        try (Connection connection = connect()) {
            connection.isValid(2);
        }
    }

    @Override
    public List<EventInfo> listEvents() throws SQLException {
        String sql = "SELECT event_id, event_name FROM events ORDER BY event_name";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            List<EventInfo> events = new ArrayList<>();
            while (results.next()) {
                events.add(new EventInfo(results.getLong("event_id"), results.getString("event_name")));
            }
            return events;
        }
    }

    @Override
    public boolean eventExists(long eventId) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM events WHERE event_id = ?")) {
            statement.setLong(1, eventId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    @Override
    public boolean registrationExists(long studentId, long eventId) throws SQLException {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT 1 FROM registrations WHERE student_id = ? AND event_id = ?")) {
            statement.setLong(1, studentId);
            statement.setLong(2, eventId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    @Override
    public Registration register(long studentId, String studentName, long eventId)
            throws SQLException, DuplicateRegistrationException {
        String upsertStudent = "INSERT INTO students (student_id, student_name) VALUES (?, ?) "
                + "ON DUPLICATE KEY UPDATE student_name = VALUES(student_name)";
        String insertRegistration = "INSERT INTO registrations (student_id, event_id) VALUES (?, ?)";
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement studentStatement = connection.prepareStatement(upsertStudent)) {
                    studentStatement.setLong(1, studentId);
                    studentStatement.setString(2, studentName);
                    studentStatement.executeUpdate();
                }
                long registrationId;
                try (PreparedStatement registrationStatement = connection.prepareStatement(
                        insertRegistration, Statement.RETURN_GENERATED_KEYS)) {
                    registrationStatement.setLong(1, studentId);
                    registrationStatement.setLong(2, eventId);
                    registrationStatement.executeUpdate();
                    try (ResultSet keys = registrationStatement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("The database did not return a registration ID.");
                        }
                        registrationId = keys.getLong(1);
                    }
                }
                EventInfo event = eventById(connection, eventId);
                connection.commit();
                return new Registration(registrationId, studentId, studentName,
                        eventId, event.name(), Instant.now());
            } catch (SQLException exception) {
                rollback(connection, exception);
                if (isDuplicateKey(exception)) {
                    throw new DuplicateRegistrationException(
                            "This student is already registered for the event.");
                }
                throw exception;
            }
        }
    }

    private EventInfo eventById(Connection connection, long eventId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT event_id, event_name FROM events WHERE event_id = ?")) {
            statement.setLong(1, eventId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    throw new SQLException("Event not found after registration.");
                }
                return new EventInfo(results.getLong("event_id"), results.getString("event_name"));
            }
        }
    }

    private static void rollback(Connection connection, Exception cause) {
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            cause.addSuppressed(rollbackFailure);
        }
    }

    private static boolean isDuplicateKey(SQLException exception) {
        return exception.getErrorCode() == 1062 || "23505".equals(exception.getSQLState());
    }

    @Override
    public List<Registration> listRegistrations(Long eventId, int offset, int limit) throws SQLException {
        String sql = "SELECT r.registration_id, r.student_id, s.student_name, "
                + "r.event_id, e.event_name, r.registered_at "
                + "FROM registrations r JOIN students s ON s.student_id = r.student_id "
                + "JOIN events e ON e.event_id = r.event_id "
                + "WHERE (? IS NULL OR r.event_id = ?) "
                + "ORDER BY r.registration_id DESC LIMIT ? OFFSET ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (eventId == null) {
                statement.setNull(1, java.sql.Types.BIGINT);
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(1, eventId);
                statement.setLong(2, eventId);
            }
            statement.setInt(3, limit);
            statement.setInt(4, offset);
            try (ResultSet results = statement.executeQuery()) {
                List<Registration> registrations = new ArrayList<>();
                while (results.next()) {
                    registrations.add(new Registration(
                            results.getLong("registration_id"),
                            results.getLong("student_id"),
                            results.getString("student_name"),
                            results.getLong("event_id"),
                            results.getString("event_name"),
                            results.getTimestamp("registered_at").toInstant()));
                }
                return registrations;
            }
        }
    }
}
