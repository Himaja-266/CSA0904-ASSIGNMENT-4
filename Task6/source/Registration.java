import java.time.Instant;

public record Registration(
        long id,
        long studentId,
        String studentName,
        long eventId,
        String eventName,
        Instant registeredAt) {
}
