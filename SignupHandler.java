import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class SignupHandler {

    public static void handle(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String requestBody = readRequestBody(exchange);

        Map<String, String> data =
                parseFormData(requestBody);

        String name = getValue(data, "name");
        String prn = getValue(data, "prn");
        String email = getValue(data, "email");
        String phone = getValue(data, "phone");
        String branch = getValue(data, "branch");
        String semesterText = getValue(data, "semester");
        String cgpaText = getValue(data, "cgpa");
        String password = getValue(data, "password");

        // =========================
        // VALIDATION
        // =========================

        if (name.isEmpty() ||
                prn.isEmpty() ||
                email.isEmpty() ||
                phone.isEmpty() ||
                branch.isEmpty() ||
                semesterText.isEmpty() ||
                cgpaText.isEmpty() ||
                password.isEmpty()) {

            sendResponse(
                    exchange,
                    400,
                    "Please fill all required fields."
            );
            return;
        }

        if (!email.contains("@") ||
                !email.contains(".")) {

            sendResponse(
                    exchange,
                    400,
                    "Please enter a valid email address."
            );
            return;
        }

        if (password.length() < 6) {

            sendResponse(
                    exchange,
                    400,
                    "Password must contain at least 6 characters."
            );
            return;
        }

        try {

            int semester =
                    Integer.parseInt(semesterText);

            double cgpa =
                    Double.parseDouble(cgpaText);

            if (semester < 1 || semester > 8) {

                sendResponse(
                        exchange,
                        400,
                        "Semester must be between 1 and 8."
                );
                return;
            }

            if (cgpa < 0 || cgpa > 10) {

                sendResponse(
                        exchange,
                        400,
                        "CGPA must be between 0 and 10."
                );
                return;
            }

            // =========================
            // SCORE
            // =========================

            // CGPA contributes maximum 5 points.
            double cgpaScore =
                    cgpa / 2.0;

            double parameterScore = 0.0;

            // New student has no career parameters yet.
            double overallScore =
                    cgpaScore + parameterScore;

            // =========================
            // SQL
            // =========================

            String studentSQL =
                    "INSERT INTO students " +
                    "(name, prn, email, phone, branch, semester, cgpa) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            String authSQL =
                    "INSERT INTO student_auth " +
                    "(student_id, password) " +
                    "VALUES (?, ?)";

            String parameterSQL =
                    "INSERT INTO student_parameters " +
                    "(student_id, programming_level, " +
                    "dsa_level, communication_level, " +
                    "aptitude_level, project_level, " +
                    "certification_level, experience_level, " +
                    "career_interest, technical_skills) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            String scoreSQL =
                    "INSERT INTO student_scores " +
                    "(student_id, parameter_score, " +
                    "cgpa_score, overall_score) " +
                    "VALUES (?, ?, ?, ?)";

            // =========================
            // DATABASE TRANSACTION
            // =========================

            try (Connection connection =
                         DatabaseConnection.getConnection()) {

                connection.setAutoCommit(false);

                try {

                    int studentId;

                    // =========================
                    // CREATE STUDENT
                    // =========================

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            studentSQL,
                                            Statement.RETURN_GENERATED_KEYS
                                    )
                    ) {

                        statement.setString(1, name);
                        statement.setString(2, prn);
                        statement.setString(3, email);
                        statement.setString(4, phone);
                        statement.setString(5, branch);
                        statement.setInt(6, semester);
                        statement.setDouble(7, cgpa);

                        statement.executeUpdate();

                        try (
                                var keys =
                                        statement.getGeneratedKeys()
                        ) {

                            if (!keys.next()) {
                                throw new Exception(
                                        "Student ID could not be generated."
                                );
                            }

                            studentId =
                                    keys.getInt(1);
                        }
                    }

                    // =========================
                    // CREATE AUTH
                    // =========================

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(authSQL)
                    ) {

                        statement.setInt(
                                1,
                                studentId
                        );

                        /*
                         * Keeping the current password format so your
                         * existing login database remains compatible.
                         *
                         * Password hashing can be added next without
                         * changing the frontend.
                         */
                        statement.setString(
                                2,
                                password
                        );

                        statement.executeUpdate();
                    }

                    // =========================
                    // DEFAULT PARAMETERS
                    // =========================

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(
                                            parameterSQL
                                    )
                    ) {

                        statement.setInt(1, studentId);

                        statement.setString(2, "None");
                        statement.setString(3, "None");
                        statement.setString(4, "None");
                        statement.setString(5, "None");
                        statement.setString(6, "None");
                        statement.setString(7, "None");
                        statement.setString(8, "None");
                        statement.setString(9, "Not Set");
                        statement.setString(10, "None");

                        statement.executeUpdate();
                    }

                    // =========================
                    // INITIAL SCORE
                    // =========================

                    try (
                            PreparedStatement statement =
                                    connection.prepareStatement(scoreSQL)
                    ) {

                        statement.setInt(1, studentId);
                        statement.setDouble(2, parameterScore);
                        statement.setDouble(3, cgpaScore);
                        statement.setDouble(4, overallScore);

                        statement.executeUpdate();
                    }

                    // =========================
                    // COMMIT
                    // =========================

                    connection.commit();

                    System.out.println();
                    System.out.println(
                            "New CareerScope Student Created"
                    );
                    System.out.println(
                            "Student ID     : " + studentId
                    );
                    System.out.println(
                            "Name           : " + name
                    );
                    System.out.println(
                            "CGPA Score / 5 : " +
                            String.format("%.2f", cgpaScore)
                    );
                    System.out.println(
                            "Parameter / 5  : " +
                            String.format("%.2f", parameterScore)
                    );
                    System.out.println(
                            "Overall / 10   : " +
                            String.format("%.2f", overallScore)
                    );
                    System.out.println();

                    sendResponse(
                            exchange,
                            200,
                            "SUCCESS:" + studentId
                    );

                } catch (Exception e) {

                    try {
                        connection.rollback();
                    } catch (Exception ignored) {
                    }

                    e.printStackTrace();

                    String message =
                            e.getMessage();

                    if (message != null &&
                            message.toLowerCase()
                                    .contains("duplicate")) {

                        sendResponse(
                                exchange,
                                409,
                                "PRN or Email already exists."
                        );

                    } else {

                        sendResponse(
                                exchange,
                                500,
                                "Account creation failed."
                        );
                    }
                }
            }

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "Invalid semester or CGPA."
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Something went wrong."
            );
        }
    }

    private static String readRequestBody(
            HttpExchange exchange
    ) throws IOException {

        InputStream inputStream =
                exchange.getRequestBody();

        return new String(
                inputStream.readAllBytes(),
                StandardCharsets.UTF_8
        );
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