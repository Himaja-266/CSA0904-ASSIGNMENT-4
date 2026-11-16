import java.sql.SQLException;
import java.util.List;

public interface RegistrationDao {
    List<EventInfo> listEvents() throws SQLException;

    boolean eventExists(long eventId) throws SQLException;

    boolean registrationExists(long studentId, long eventId) throws SQLException;

    Registration register(long studentId, String studentName, long eventId)
            throws SQLException, DuplicateRegistrationException;

    List<Registration> listRegistrations(Long eventId, int offset, int limit) throws SQLException;
}
