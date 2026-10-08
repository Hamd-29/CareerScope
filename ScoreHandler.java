import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ScoreHandler {

    public static void handle(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String query = exchange.getRequestURI().getQuery();

        if (query == null || !query.startsWith("studentId=")) {
            sendResponse(exchange, 400, "Student ID is required.");
            return;
        }

        int studentId;

        try {
            studentId = Integer.parseInt(
                    query.substring("studentId=".length())
            );
        } catch (Exception e) {
            sendResponse(exchange, 400, "Invalid student ID.");
            return;
        }

        String sql =
                "SELECT " +
                "programming_level, " +
                "dsa_level, " +
                "communication_level, " +
                "aptitude_level, " +
                "project_level, " +
                "certification_level, " +
                "experience_level " +
                "FROM student_parameters " +
                "WHERE student_id = ?";

        try (
            Connection connection =
                    DatabaseConnection.getConnection();

            PreparedStatement statement =
                    connection.prepareStatement(sql)
        ) {

            statement.setInt(1, studentId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    sendResponse(
                            exchange,
                            404,
                            "Student parameters not found."
                    );
                    return;
                }

                String programming =
                        result.getString("programming_level");

                String dsa =
                        result.getString("dsa_level");

                String communication =
                        result.getString("communication_level");

                String aptitude =
                        result.getString("aptitude_level");

                String project =
                        result.getString("project_level");

                String certification =
                        result.getString("certification_level");

                String experience =
                        result.getString("experience_level");

                double score = 0;

                score += getLevelScore(programming);
                score += getLevelScore(dsa);
                score += getLevelScore(communication);
                score += getLevelScore(aptitude);

                score += getProjectScore(project);
                score += getCertificationScore(certification);
                score += getExperienceScore(experience);

                String updateSQL =
                        "INSERT INTO student_scores " +
                        "(student_id, parameter_score, cgpa_score, overall_score) " +
                        "VALUES (?, ?, 0, ?) " +
                        "ON DUPLICATE KEY UPDATE " +
                        "parameter_score = VALUES(parameter_score), " +
                        "overall_score = VALUES(overall_score)";

                try (PreparedStatement update =
                             connection.prepareStatement(updateSQL)) {

                    update.setInt(1, studentId);
                    update.setDouble(2, score);
                    update.setDouble(3, score);

                    update.executeUpdate();
                }

                sendResponse(
                        exchange,
                        200,
                        "Score saved successfully! Score = " + score
                );
            }

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Failed to save score."
            );
        }
    }

    private static double getLevelScore(String level) {

        if (level == null) {
            return 0;
        }

        switch (level.toLowerCase()) {

            case "basic":
                return 1;

            case "moderate":
                return 2;

            case "high":
                return 3;

            default:
                return 0;
        }
    }

    private static double getProjectScore(String value) {

        if (value == null) {
            return 0;
        }

        switch (value.toLowerCase()) {

            case "none":
                return 0;

            case "basic":
                return 1;

            case "good":
                return 2;

            case "strong":
                return 3;

            default:
                return 0;
        }
    }

    private static double getCertificationScore(String value) {

        if (value == null) {
            return 0;
        }

        switch (value.toLowerCase()) {

            case "none":
                return 0;

            case "1-2":
                return 1;

            case "3+":
                return 2;

            default:
                return 0;
        }
    }

    private static double getExperienceScore(String value) {

        if (value == null) {
            return 0;
        }

        switch (value.toLowerCase()) {

            case "none":
                return 0;

            case "basic":
                return 1;

            case "experienced":
                return 2;

            default:
                return 0;
        }
    }

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response)
            throws IOException {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }
}