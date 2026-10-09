
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminHandler {

    // Demo credentials: change these before using the project.
    private static final String ADMIN_USER = "admin";
    private static final String ADMIN_PASSWORD = "admin@123";
    private static final String ADMIN_TOKEN =
            "careerscope-admin-token-change-this";

    public static void login(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;

        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
            respond(ex, 405, "{\"error\":\"POST required\"}");
            return;
        }

        String body = new String(
                ex.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        String username = field(body, "username");
        String password = field(body, "password");

        if (ADMIN_USER.equals(username)
                && ADMIN_PASSWORD.equals(password)) {
            respond(ex, 200,
                    "{\"success\":true,\"token\":\""
                            + ADMIN_TOKEN + "\"}");
        } else {
            respond(ex, 401,
                    "{\"success\":false,\"error\":\"Invalid admin credentials\"}");
        }
    }

    public static void students(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!authorized(ex)) {
            respond(ex, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            respond(ex, 405, "{\"error\":\"GET required\"}");
            return;
        }

        StringBuilder json = new StringBuilder("{\"students\":[");
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT student_id,name,prn,email,phone,branch," +
                     "semester,cgpa FROM students ORDER BY name");
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"student_id\":").append(rs.getInt("student_id")).append(",")
                    .append("\"name\":").append(q(rs.getString("name"))).append(",")
                    .append("\"prn\":").append(q(rs.getString("prn"))).append(",")
                    .append("\"email\":").append(q(rs.getString("email"))).append(",")
                    .append("\"phone\":").append(q(rs.getString("phone"))).append(",")
                    .append("\"branch\":").append(q(rs.getString("branch"))).append(",")
                    .append("\"semester\":").append(rs.getString("semester") == null
                            ? "null" : rs.getInt("semester")).append(",")
                    .append("\"cgpa\":").append(rs.getString("cgpa") == null
                            ? "null" : rs.getBigDecimal("cgpa")).append("}");
            }

            json.append("]}");
            respond(ex, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            respond(ex, 500, "{\"error\":\"Could not load students\"}");
        }
    }

    public static void ranking(HttpExchange ex) throws IOException {
        if (preflight(ex)) return;
        if (!authorized(ex)) {
            respond(ex, 401, "{\"error\":\"Unauthorized\"}");
            return;
        }

        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            respond(ex, 405, "{\"error\":\"GET required\"}");
            return;
        }

        StringBuilder json = new StringBuilder("{\"ranking\":[");
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT s.student_id,s.name,s.branch,s.cgpa," +
                     "COALESCE(sc.overall_score,0) AS overall_score " +
                     "FROM students s LEFT JOIN student_scores sc " +
                     "ON s.student_id=sc.student_id " +
                     "ORDER BY overall_score DESC,s.name ASC");
             ResultSet rs = ps.executeQuery()) {

            boolean first = true;
            int rank = 0;

            while (rs.next()) {
                rank++;
                if (!first) json.append(",");
                first = false;

                json.append("{")
                    .append("\"rank\":").append(rank).append(",")
                    .append("\"student_id\":").append(rs.getInt("student_id")).append(",")
                    .append("\"name\":").append(q(rs.getString("name"))).append(",")
                    .append("\"branch\":").append(q(rs.getString("branch"))).append(",")
                    .append("\"cgpa\":").append(rs.getString("cgpa") == null
                            ? "null" : rs.getBigDecimal("cgpa")).append(",")
                    .append("\"overall_score\":")
                    .append(rs.getBigDecimal("overall_score")).append("}");
            }

            json.append("]}");
            respond(ex, 200, json.toString());

        } catch (Exception e) {
            e.printStackTrace();
            respond(ex, 500, "{\"error\":\"Could not load ranking\"}");
        }
    }

    private static boolean authorized(HttpExchange ex) {
        return ADMIN_TOKEN.equals(
                ex.getRequestHeaders().getFirst("X-Admin-Token"));
    }

    private static String field(String body, String name) {
        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 &&
                    parts[0].equals(name)) {
                return URLDecoder.decode(
                        parts[1], StandardCharsets.UTF_8);
            }
        }
        return "";
    }

    private static String q(String value) {
        if (value == null) return "null";

        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") + "\"";
    }

    private static void respond(
            HttpExchange ex, int status, String json
    ) throws IOException {
        CareerScopeServer.sendJsonResponse(ex, status, json);
    }

    private static boolean preflight(HttpExchange ex)
            throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            return false;
        }

        CareerScopeServer.addCorsHeaders(ex);
        ex.getResponseHeaders().set("Access-Control-Allow-Headers",
                "Content-Type, Authorization, X-Admin-Token");
        ex.sendResponseHeaders(204, -1);
        ex.close();
        return true;
    }
}