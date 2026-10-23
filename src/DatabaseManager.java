import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {
    private static final String CONFIG_FILE = "config/db.properties";

    public static Connection getConnection() {
        Properties properties = new Properties();

        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);

            String url = properties.getProperty("db.url");
            String username = properties.getProperty("db.username");
            String password = properties.getProperty("db.password");

            if (url == null || url.trim().isEmpty()) {
                System.out.println("Database configuration error: Missing required property 'db.url' in " + CONFIG_FILE);
                return null;
            }

            if (username == null || username.trim().isEmpty()) {
                System.out.println("Database configuration error: Missing required property 'db.username' in " + CONFIG_FILE);
                return null;
            }

            if (password == null) {
                System.out.println("Database configuration error: Missing required property 'db.password' in " + CONFIG_FILE);
                return null;
            }

            return DriverManager.getConnection(url.trim(), username.trim(), password);
        } catch (IOException e) {
            System.out.println("Configuration file error: Unable to load " + CONFIG_FILE + " (" + e.getMessage() + ")");
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
        }
        return null;
    }
}