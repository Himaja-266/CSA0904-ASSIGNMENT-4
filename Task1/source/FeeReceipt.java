import java.math.BigDecimal;
import java.time.Instant;

public record FeeReceipt(String studentId, BigDecimal amount, Instant paidAt) {
}
