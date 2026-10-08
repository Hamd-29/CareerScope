import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DashboardHandler {

    public static void handle(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed"
            );

            return;
        }

        String query =
                exchange.getRequestURI().getQuery();

        String studentIdText =
                getQueryParameter(query, "studentId");

        if (
                studentIdText == null ||
                studentIdText.isBlank()
        ) {

            sendResponse(
                    exchange,
                    400,
                    "Student ID is required."
            );

            return;
        }

        try {

            int studentId =
                    Integer.parseInt(
                            studentIdText
                    );

            String sql =
                    "SELECT " +
                    "s.student_id, " +
                    "s.name, " +
                    "s.prn, " +
                    "s.email, " +
                    "s.phone, " +
                    "s.branch, " +
                    "s.semester, " +
                    "s.cgpa, " +

                    "ss.parameter_score, " +
                    "ss.cgpa_score, " +
                    "ss.overall_score, " +

                    "sp.programming_level, " +
                    "sp.dsa_level, " +
                    "sp.communication_level, " +
                    "sp.aptitude_level, " +
                    "sp.project_level, " +
                    "sp.certification_level, " +
                    "sp.experience_level, " +
                    "sp.career_interest, " +
                    "sp.technical_skills " +

                    "FROM students s " +

                    "LEFT JOIN student_scores ss " +
                    "ON s.student_id = ss.student_id " +

                    "LEFT JOIN student_parameters sp " +
                    "ON s.student_id = sp.student_id " +

                    "WHERE s.student_id = ?";

            try (
                    Connection connection =
                            DatabaseConnection.getConnection();

                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {

                statement.setInt(
                        1,
                        studentId
                );

                try (
                        ResultSet result =
                                statement.executeQuery()
                ) {

                    if (!result.next()) {

                        sendResponse(
                                exchange,
                                404,
                                "Student not found."
                        );

                        return;
                    }

                    String response =
                            "{"

                            + "\"studentId\":"
                            + result.getInt("student_id")
                            + ","

                            + "\"name\":\""
                            + escape(
                                    result.getString("name")
                            )
                            + "\","

                            + "\"prn\":\""
                            + escape(
                                    result.getString("prn")
                            )
                            + "\","

                            + "\"email\":\""
                            + escape(
                                    result.getString("email")
                            )
                            + "\","

                            + "\"phone\":\""
                            + escape(
                                    result.getString("phone")
                            )
                            + "\","

                            + "\"branch\":\""
                            + escape(
                                    result.getString("branch")
                            )
                            + "\","

                            + "\"semester\":"
                            + result.getInt("semester")
                            + ","

                            + "\"cgpa\":"
                            + result.getDouble("cgpa")
                            + ","

                            + "\"parameterScore\":"
                            + result.getDouble("parameter_score")
                            + ","

                            + "\"cgpaScore\":"
                            + result.getDouble("cgpa_score")
                            + ","

                            + "\"overallScore\":"
                            + result.getDouble("overall_score")
                            + ","

                            + "\"programmingLevel\":\""
                            + escape(
                                    result.getString(
                                            "programming_level"
                                    )
                            )
                            + "\","

                            + "\"dsaLevel\":\""
                            + escape(
                                    result.getString(
                                            "dsa_level"
                                    )
                            )
                            + "\","

                            + "\"communicationLevel\":\""
                            + escape(
                                    result.getString(
                                            "communication_level"
                                    )
                            )
                            + "\","

                            + "\"aptitudeLevel\":\""
                            + escape(
                                    result.getString(
                                            "aptitude_level"
                                    )
                            )
                            + "\","

                            + "\"projectLevel\":\""
                            + escape(
                                    result.getString(
                                            "project_level"
                                    )
                            )
                            + "\","

                            + "\"certificationLevel\":\""
                            + escape(
                                    result.getString(
                                            "certification_level"
                                    )
                            )
                            + "\","

                            + "\"experienceLevel\":\""
                            + escape(
                                    result.getString(
                                            "experience_level"
                                    )
                            )
                            + "\","

                            + "\"careerInterest\":\""
                            + escape(
                                    result.getString(
                                            "career_interest"
                                    )
                            )
                            + "\","

                            + "\"technicalSkills\":\""
                            + escape(
                                    result.getString(
                                            "technical_skills"
                                    )
                            )
                            + "\""

                            + "}";

                    sendJsonResponse(
                            exchange,
                            200,
                            response
                    );
                }
            }

        } catch (NumberFormatException e) {

            sendResponse(
                    exchange,
                    400,
                    "Invalid student ID."
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to load dashboard data.\"}"
            );
        }
    }

    private static String getQueryParameter(
            String query,
            String parameter
    ) {

        if (query == null || query.isBlank()) {
            return null;
        }

        String[] parameters =
                query.split("&");

        for (String item : parameters) {

            String[] pair =
                    item.split("=", 2);

            if (
                    pair.length == 2 &&
                    pair[0].equals(parameter)
            ) {

                return URLDecoder.decode(
                        pair[1],
                        StandardCharsets.UTF_8
                );
            }
        }

        return null;
    }

    private static String escape(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    private static void sendJsonResponse(
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
                "application/json; charset=UTF-8"
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