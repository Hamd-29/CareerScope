import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler {

    public static void handle(
            HttpExchange exchange
    ) throws IOException {

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed"
            );

            return;
        }

        String body =
                new String(
                        exchange.getRequestBody()
                                .readAllBytes(),
                        StandardCharsets.UTF_8
                );

        Map<String, String> data =
                parseFormData(body);

        String email =
                getValue(data, "email");

        String password =
                getValue(data, "password");

        // =========================
        // VALIDATION
        // =========================

        if (email.isEmpty() ||
                password.isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "Please enter email and password."
            );

            return;
        }

        // =========================
        // LOGIN QUERY
        // =========================

        String sql =
                "SELECT s.student_id, s.name " +
                "FROM students s " +
                "JOIN student_auth a " +
                "ON s.student_id = a.student_id " +
                "WHERE s.email = ? " +
                "AND a.password = ?";

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, email);
            statement.setString(2, password);

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {

                    sendResponse(
                            exchange,
                            401,
                            "Invalid email or password."
                    );

                    return;
                }

                int studentId =
                        result.getInt("student_id");

                String name =
                        result.getString("name");

                /*
                 * Frontend expects:
                 *
                 * LOGIN_SUCCESS|studentId|studentName
                 */

                String response =
                        "LOGIN_SUCCESS|" +
                        studentId +
                        "|" +
                        name;

                sendResponse(
                        exchange,
                        200,
                        response
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Login failed."
            );
        }
    }

    private static Map<String, String> parseFormData(
            String body
    ) {

        Map<String, String> data =
                new HashMap<>();

        if (body == null || body.isEmpty()) {
            return data;
        }

        String[] pairs =
                body.split("&");

        for (String pair : pairs) {

            String[] parts =
                    pair.split("=", 2);

            if (parts.length == 2) {

                String key =
                        java.net.URLDecoder.decode(
                                parts[0],
                                StandardCharsets.UTF_8
                        );

                String value =
                        java.net.URLDecoder.decode(
                                parts[1],
                                StandardCharsets.UTF_8
                        );

                data.put(key, value);
            }
        }

        return data;
    }

    private static String getValue(
            Map<String, String> data,
            String key
    ) {

        String value =
                data.get(key);

        return value == null
                ? ""
                : value.trim();
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        byte[] responseBytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (
                var output =
                        exchange.getResponseBody()
        ) {

            output.write(responseBytes);
        }
    }
}