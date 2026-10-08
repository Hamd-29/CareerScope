import java.sql.Connection;

public class TestDatabase {

    public static void main(String[] args) {

        try {

            Connection connection =
                    DatabaseConnection.getConnection();

            if (connection != null) {

                System.out.println(
                        "Database Connected Successfully!"
                );

                connection.close();

            } else {

                System.out.println(
                        "Database Connection Failed!"
                );
            }

        } catch (Exception e) {

            System.out.println(
                    "Database Connection Failed!"
            );

            e.printStackTrace();
        }
    }
}