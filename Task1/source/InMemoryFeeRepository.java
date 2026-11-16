import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryFeeRepository implements FeeRepository {
    private final List<FeeReceipt> payments = new CopyOnWriteArrayList<>();

    @Override
    public FeeReceipt savePayment(String studentId, BigDecimal amount) {
        FeeReceipt receipt = new FeeReceipt(studentId, amount, Instant.now());
        payments.add(receipt);
        return receipt;
    }

    @Override
    public Optional<FeeReceipt> latestReceipt(String studentId) {
        for (int i = payments.size() - 1; i >= 0; i--) {
            FeeReceipt receipt = payments.get(i);
            if (receipt.studentId().equals(studentId)) {
                return Optional.of(receipt);
            }
        }
        return Optional.empty();
    }
}
