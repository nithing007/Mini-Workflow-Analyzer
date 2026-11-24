import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final String CONFIG_FILE = "config/db.properties";
    private static volatile Properties cachedProperties;

    public static Connection getConnection() {
        Properties properties = loadConfiguration();

        if (properties == null) {
            return null;
        }

        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");

        if (url == null || url.trim().isEmpty() || username == null || username.trim().isEmpty() || password == null) {
            System.out.println("Database configuration error: Missing or invalid required database properties.");
            return null;
        }

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url.trim(), username.trim(), password);

            if (connection == null || !connection.isValid(5)) {
                if (connection != null) {
                    connection.close();
                }
                System.out.println("Database connection validation failed.");
                return null;
            }

            return connection;
        } catch (SQLException e) {
            System.out.println("Database connection failed. SQLState: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    System.out.println("Failed to close invalid database connection.");
                }
            }
            return null;
        }
    }

    private static Properties loadConfiguration() {
        if (cachedProperties != null) {
            return cachedProperties;
        }

        synchronized (DatabaseManager.class) {
            if (cachedProperties != null) {
                return cachedProperties;
            }

            Properties properties = new Properties();

            try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
                properties.load(input);

                String url = properties.getProperty("db.url");
                String username = properties.getProperty("db.username");
                String password = properties.getProperty("db.password");

                if (url == null || url.trim().isEmpty() || username == null || username.trim().isEmpty() || password == null) {
                    System.out.println("Database configuration error: Missing or invalid required properties in " + CONFIG_FILE);
                    return null;
                }

                cachedProperties = properties;
                return cachedProperties;
            } catch (IOException e) {
                System.out.println("Configuration file error: Unable to load " + CONFIG_FILE);
                return null;
            }
        }
    }
}
