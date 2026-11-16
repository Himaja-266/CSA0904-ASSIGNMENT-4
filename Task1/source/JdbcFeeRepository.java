import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import javax.sql.DataSource;

public final class JdbcFeeRepository implements FeeRepository {
    private final DataSource dataSource;

    public JdbcFeeRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public FeeReceipt savePayment(String studentId, java.math.BigDecimal amount) throws SQLException {
        Instant paidAt = Instant.now();
        String sql = "INSERT INTO fees (student_id, amount, paid_at) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            statement.setBigDecimal(2, amount);
            statement.setTimestamp(3, Timestamp.from(paidAt));
            statement.executeUpdate();
        }
        return new FeeReceipt(studentId, amount, paidAt);
    }

    @Override
    public Optional<FeeReceipt> latestReceipt(String studentId) throws SQLException {
        String sql = "SELECT student_id, amount, paid_at FROM fees "
                + "WHERE student_id = ? ORDER BY paid_at DESC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, studentId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }
                return Optional.of(new FeeReceipt(
                        results.getString("student_id"),
                        results.getBigDecimal("amount"),
                        results.getTimestamp("paid_at").toInstant()));
            }
        }
    }
}
