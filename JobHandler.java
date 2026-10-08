import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JobHandler {

    public static void handle(HttpExchange exchange)
            throws IOException {

        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return;
        }

        String sql =
                "SELECT job_id, company_name, job_title, " +
                "job_description, location, minimum_cgpa, " +
                "required_branch, required_skills, salary, " +
                "application_link " +
                "FROM jobs " +
                "ORDER BY created_at DESC";

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

            boolean first = true;

            while (result.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"job_id\":")
                        .append(result.getInt("job_id"))
                        .append(",");

                json.append("\"company_name\":\"")
                        .append(escapeJson(
                                result.getString("company_name")
                        ))
                        .append("\",");

                json.append("\"job_title\":\"")
                        .append(escapeJson(
                                result.getString("job_title")
                        ))
                        .append("\",");

                json.append("\"job_description\":\"")
                        .append(escapeJson(
                                result.getString("job_description")
                        ))
                        .append("\",");

                json.append("\"location\":\"")
                        .append(escapeJson(
                                result.getString("location")
                        ))
                        .append("\",");

                json.append("\"minimum_cgpa\":")
                        .append(result.getBigDecimal("minimum_cgpa"))
                        .append(",");

                json.append("\"required_branch\":\"")
                        .append(escapeJson(
                                result.getString("required_branch")
                        ))
                        .append("\",");

                json.append("\"required_skills\":\"")
                        .append(escapeJson(
                                result.getString("required_skills")
                        ))
                        .append("\",");

                json.append("\"salary\":\"")
                        .append(escapeJson(
                                result.getString("salary")
                        ))
                        .append("\",");

                json.append("\"application_link\":\"")
                        .append(escapeJson(
                                result.getString("application_link")
                        ))
                        .append("\"");

                json.append("}");
            }

            json.append("]");

            sendResponse(
                    exchange,
                    200,
                    json.toString()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Failed to fetch jobs."
            );
        }
    }

    private static String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
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
                "application/json; charset=UTF-8"
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