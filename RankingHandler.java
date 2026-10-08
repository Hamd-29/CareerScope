import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RankingHandler {

    public static void handle(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String sql =
                "SELECT s.student_id, s.name, ss.overall_score " +
                "FROM students s " +
                "JOIN student_scores ss " +
                "ON s.student_id = ss.student_id " +
                "ORDER BY ss.overall_score DESC, s.student_id ASC";

        StringBuilder json = new StringBuilder();
        json.append("[");

        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {

            int position = 0;
            int rank = 0;
            double previousScore = -1;
            boolean first = true;

            while (result.next()) {

                position++;

                double score =
                        result.getDouble("overall_score");

                // Competition ranking:
                // 1, 2, 2, 4
                if (score != previousScore) {
                    rank = position;
                }

                if (!first) {
                    json.append(",");
                }

                json.append("{")
                        .append("\"rank\":")
                        .append(rank)
                        .append(",")

                        .append("\"studentId\":")
                        .append(result.getInt("student_id"))
                        .append(",")

                        .append("\"name\":\"")
                        .append(escape(result.getString("name")))
                        .append("\",")

                        .append("\"score\":")
                        .append(score)

                        .append("}");

                previousScore = score;
                first = false;
            }

            json.append("]");

            sendJsonResponse(
                    exchange,
                    200,
                    json.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendJsonResponse(
                    exchange,
                    500,
                    "{\"error\":\"Failed to load ranking.\"}"
            );
        }
    }

    private static String escape(String value) {

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
                response.getBytes(StandardCharsets.UTF_8);

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
                response.getBytes(StandardCharsets.UTF_8);

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