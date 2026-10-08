import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/placement_ranking" +
            "?useSSL=false" +
            "&serverTimezone=Asia/Kolkata" +
            "&allowPublicKeyRetrieval=true";

    private static final String USER = "root";

    private static final String PASSWORD = "5432";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "MySQL JDBC Driver not found.",
                    e
            );
        }
    }

    private DatabaseConnection() {
        // Prevent object creation
    }

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
                URL,
                USER,
                PASSWORD
        );
    }
}