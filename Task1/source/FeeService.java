import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Optional;

public final class FeeService {
    private final FeeRepository repository;

    public FeeService(FeeRepository repository) {
        this.repository = repository;
    }

    public FeeReceipt pay(String studentId, BigDecimal amount) throws SQLException {
        String normalizedStudentId = studentId == null ? "" : studentId.trim();
        if (normalizedStudentId.isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        if (amount == null || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Enter a positive amount with at most two decimal places.");
        }
        return repository.savePayment(normalizedStudentId, amount.setScale(2));
    }

    public Optional<FeeReceipt> latestReceipt(String studentId) throws SQLException {
        String normalizedStudentId = studentId == null ? "" : studentId.trim();
        if (normalizedStudentId.isEmpty()) {
            throw new IllegalArgumentException("Student ID is required.");
        }
        return repository.latestReceipt(normalizedStudentId);
    }
}
