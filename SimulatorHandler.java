import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SimulatorHandler {

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

        // ========================================
        // CREATE MAX HEAP
        // ========================================

        MaxHeap maxHeap =
                new MaxHeap(10);


        // ========================================
        // GET STUDENTS FROM DATABASE
        // ========================================

        String sql =
                "SELECT " +
                "s.student_id, " +
                "s.name, " +
                "ss.overall_score " +
                "FROM students s " +
                "JOIN student_scores ss " +
                "ON s.student_id = ss.student_id " +
                "ORDER BY ss.overall_score DESC";


        try (
                Connection connection =
                        DatabaseConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {

            // ========================================
            // INSERT STUDENTS INTO MAX HEAP
            // ========================================

            while (result.next()) {

                int studentId =
                        result.getInt("student_id");

                String name =
                        result.getString("name");

                double score =
                        result.getDouble("overall_score");


                StudentScore student =
                        new StudentScore(
                                studentId,
                                name,
                                score
                        );


                maxHeap.insert(student);
            }


            // ========================================
            // BUILD JSON RESPONSE
            // ========================================

            StringBuilder json =
                    new StringBuilder();

            json.append("[");


            boolean first = true;


            // ========================================
            // REMOVE FROM MAX HEAP
            // ========================================

            while (maxHeap.size() > 0) {

                StudentScore student =
                        maxHeap.removeMax();


                if (!first) {
                    json.append(",");
                }

                first = false;


                json.append("{");

                json.append("\"studentId\":")
                        .append(student.getStudentId())
                        .append(",");

                json.append("\"name\":\"")
                        .append(escapeJson(student.getName()))
                        .append("\",");

                json.append("\"score\":")
                        .append(String.format(
                                "%.2f",
                                student.getScore()
                        ));

                json.append("}");
            }


            json.append("]");


            // ========================================
            // SEND RESPONSE
            // ========================================

            sendJsonResponse(
                    exchange,
                    200,
                    json.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Failed to generate Max Heap ranking."
            );
        }
    }


    // ========================================
    // ESCAPE JSON TEXT
    // ========================================

    private static String escapeJson(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }


    // ========================================
    // JSON RESPONSE
    // ========================================

    private static void sendJsonResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
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

        exchange.getResponseBody()
                .write(responseBytes);

        exchange.getResponseBody()
                .close();
    }


    // ========================================
    // TEXT RESPONSE
    // ========================================

    private static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        exchange.getResponseHeaders().add(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().add(
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

        exchange.getResponseBody()
                .write(responseBytes);

        exchange.getResponseBody()
                .close();
    }
}
