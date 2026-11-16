import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    public record DaoSelection(RegistrationDao dao, String notice) {
    }

    private Database() {
    }

    public static DaoSelection createDao() {
        Path config = Path.of("db.properties");
        if (!Files.exists(config)) {
            return new DaoSelection(new InMemoryRegistrationDao(),
                    "db.properties not found; using in-memory data.");
        }
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(config)) {
            properties.load(input);
        } catch (IOException exception) {
            return new DaoSelection(new InMemoryRegistrationDao(),
                    "Could not read db.properties; using in-memory data: " + exception.getMessage());
        }

        String url = properties.getProperty("url", "").trim();
        if (url.isEmpty()) {
            return new DaoSelection(new InMemoryRegistrationDao(),
                    "Database URL is missing; using in-memory data.");
        }
        JdbcRegistrationDao jdbcDao = new JdbcRegistrationDao(
                url, properties.getProperty("user", ""), properties.getProperty("password", ""));
        try {
            jdbcDao.ping();
            return new DaoSelection(jdbcDao, "Connected to MySQL.");
        } catch (SQLException exception) {
            return new DaoSelection(new InMemoryRegistrationDao(),
                    "MySQL unavailable; using in-memory data: " + exception.getMessage());
        }
    }
}
