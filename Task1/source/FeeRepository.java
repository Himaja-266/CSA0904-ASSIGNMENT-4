import java.sql.SQLException;
import java.util.Optional;

public interface FeeRepository {
    FeeReceipt savePayment(String studentId, java.math.BigDecimal amount) throws SQLException;

    Optional<FeeReceipt> latestReceipt(String studentId) throws SQLException;
}
