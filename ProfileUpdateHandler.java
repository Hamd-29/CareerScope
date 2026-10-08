import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ProfileUpdateHandler {

    public static void handle(HttpExchange exchange)
            throws IOException {

        // ==================================================
        // ONLY POST REQUEST ALLOWED
        // ==================================================

        if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {

            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed"
            );

            return;
        }

        Connection connection = null;

        try {

            // ==================================================
            // READ REQUEST BODY
            // ==================================================

            String body =
                    new String(
                            exchange.getRequestBody().readAllBytes(),
                            StandardCharsets.UTF_8
                    );

            System.out.println(
                    "Profile Update Request: " + body
            );

            // ==================================================
            // PARSE FORM DATA
            // ==================================================

            Map<String, String> data =
                    parseFormData(body);

            String studentIdValue =
                    data.get("studentId");

            if (
                    studentIdValue == null ||
                    studentIdValue.trim().isEmpty()
            ) {

                sendResponse(
                        exchange,
                        400,
                        "Student ID is missing."
                );

                return;
            }

            int studentId =
                    Integer.parseInt(
                            studentIdValue
                    );

            // ==================================================
            // GET STUDENT DATA
            // ==================================================

            String name =
                    getValue(data, "name");

            String email =
                    getValue(data, "email");

            String phone =
                    getValue(data, "phone");

            // ==================================================
            // GET PARAMETERS
            // ==================================================

            String programming =
                    getValue(data, "programming");

            String dsa =
                    getValue(data, "dsa");

            String communication =
                    getValue(data, "communication");

            String aptitude =
                    getValue(data, "aptitude");

            String project =
                    getValue(data, "project");

            String certification =
                    getValue(data, "certification");

            String experience =
                    getValue(data, "experience");

            String careerInterest =
                    getValue(data, "careerInterest");

            String technicalSkills =
                    getValue(data, "technicalSkills");

            // ==================================================
            // BASIC VALIDATION
            // ==================================================

            if (name.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "Name cannot be empty."
                );

                return;
            }

            if (email.isEmpty()) {

                sendResponse(
                        exchange,
                        400,
                        "Email cannot be empty."
                );

                return;
            }

            // ==================================================
            // DATABASE CONNECTION
            // ==================================================

            connection =
                    DatabaseConnection.getConnection();

            if (connection == null) {

                sendResponse(
                        exchange,
                        500,
                        "Database connection failed."
                );

                return;
            }

            connection.setAutoCommit(false);

            // ==================================================
            // CHECK STUDENT EXISTS
            // ==================================================

            String checkStudentSql =
                    "SELECT student_id " +
                    "FROM students " +
                    "WHERE student_id = ?";

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    checkStudentSql
                            )
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

                        connection.rollback();

                        sendResponse(
                                exchange,
                                404,
                                "Student account not found."
                        );

                        return;
                    }
                }
            }

            // ==================================================
            // CHECK EMAIL
            // ==================================================

            String emailCheckSql =
                    "SELECT student_id " +
                    "FROM students " +
                    "WHERE email = ? " +
                    "AND student_id <> ?";

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    emailCheckSql
                            )
            ) {

                statement.setString(
                        1,
                        email
                );

                statement.setInt(
                        2,
                        studentId
                );

                try (
                        ResultSet result =
                                statement.executeQuery()
                ) {

                    if (result.next()) {

                        connection.rollback();

                        sendResponse(
                                exchange,
                                409,
                                "This email is already registered with another student account."
                        );

                        return;
                    }
                }
            }

            // ==================================================
            // UPDATE STUDENT INFORMATION
            // ==================================================

            String studentSql =
                    "UPDATE students " +
                    "SET name = ?, " +
                    "email = ?, " +
                    "phone = ? " +
                    "WHERE student_id = ?";

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    studentSql
                            )
            ) {

                statement.setString(
                        1,
                        name
                );

                statement.setString(
                        2,
                        email
                );

                statement.setString(
                        3,
                        phone
                );

                statement.setInt(
                        4,
                        studentId
                );

                int rowsUpdated =
                        statement.executeUpdate();

                System.out.println(
                        "Student rows updated: " +
                        rowsUpdated
                );
            }

            // ==================================================
            // CHECK PARAMETER ROW
            // ==================================================

            boolean parameterRowExists =
                    false;

            String checkParameterSql =
                    "SELECT student_id " +
                    "FROM student_parameters " +
                    "WHERE student_id = ?";

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    checkParameterSql
                            )
            ) {

                statement.setInt(
                        1,
                        studentId
                );

                try (
                        ResultSet result =
                                statement.executeQuery()
                ) {

                    if (result.next()) {

                        parameterRowExists = true;
                    }
                }
            }

            // ==================================================
            // UPDATE OR INSERT PARAMETERS
            // ==================================================

            if (parameterRowExists) {

                String parameterSql =
                        "UPDATE student_parameters " +
                        "SET programming_level = ?, " +
                        "dsa_level = ?, " +
                        "communication_level = ?, " +
                        "aptitude_level = ?, " +
                        "project_level = ?, " +
                        "certification_level = ?, " +
                        "experience_level = ?, " +
                        "career_interest = ?, " +
                        "technical_skills = ? " +
                        "WHERE student_id = ?";

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        parameterSql
                                )
                ) {

                    statement.setString(
                            1,
                            programming
                    );

                    statement.setString(
                            2,
                            dsa
                    );

                    statement.setString(
                            3,
                            communication
                    );

                    statement.setString(
                            4,
                            aptitude
                    );

                    statement.setString(
                            5,
                            project
                    );

                    statement.setString(
                            6,
                            certification
                    );

                    statement.setString(
                            7,
                            experience
                    );

                    statement.setString(
                            8,
                            careerInterest
                    );

                    statement.setString(
                            9,
                            technicalSkills
                    );

                    statement.setInt(
                            10,
                            studentId
                    );

                    int rowsUpdated =
                            statement.executeUpdate();

                    System.out.println(
                            "Parameter rows updated: " +
                            rowsUpdated
                    );
                }

            } else {

                String parameterInsertSql =
                        "INSERT INTO student_parameters " +
                        "(student_id, " +
                        "programming_level, " +
                        "dsa_level, " +
                        "communication_level, " +
                        "aptitude_level, " +
                        "project_level, " +
                        "certification_level, " +
                        "experience_level, " +
                        "career_interest, " +
                        "technical_skills) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (
                        PreparedStatement statement =
                                connection.prepareStatement(
                                        parameterInsertSql
                                )
                ) {

                    statement.setInt(
                            1,
                            studentId
                    );

                    statement.setString(
                            2,
                            programming
                    );

                    statement.setString(
                            3,
                            dsa
                    );

                    statement.setString(
                            4,
                            communication
                    );

                    statement.setString(
                            5,
                            aptitude
                    );

                    statement.setString(
                            6,
                            project
                    );

                    statement.setString(
                            7,
                            certification
                    );

                    statement.setString(
                            8,
                            experience
                    );

                    statement.setString(
                            9,
                            careerInterest
                    );

                    statement.setString(
                            10,
                            technicalSkills
                    );

                    statement.executeUpdate();

                    System.out.println(
                            "Parameter row created for student: " +
                            studentId
                    );
                }
            }

            // ==================================================
            // CALCULATE PARAMETER SCORE
            // ==================================================

            double parameterScore = 0;

            parameterScore +=
                    getLevelScore(programming);

            parameterScore +=
                    getLevelScore(dsa);

            parameterScore +=
                    getLevelScore(communication);

            parameterScore +=
                    getLevelScore(aptitude);

            parameterScore +=
                    getProjectScore(project);

            parameterScore +=
                    getCertificationScore(certification);

            parameterScore +=
                    getExperienceScore(experience);

            System.out.println(
                    "Raw Parameter Score: " +
                    parameterScore
            );

            // ==================================================
            // CONVERT PARAMETER SCORE TO 5
            // ==================================================
            //
            // Maximum raw parameter score = 19
            //
            // Parameter score shown in dashboard = maximum 5
            //
            // ==================================================

            double parameterScoreOutOfFive =
                    (parameterScore / 19.0) * 5.0;

            // Safety limit

            if (parameterScoreOutOfFive > 5) {
                parameterScoreOutOfFive = 5;
            }

            if (parameterScoreOutOfFive < 0) {
                parameterScoreOutOfFive = 0;
            }

            System.out.println(
                    "Parameter Score / 5: " +
                    parameterScoreOutOfFive
            );

            // ==================================================
            // GET CGPA
            // ==================================================

            double cgpa = 0;

            String cgpaSql =
                    "SELECT cgpa " +
                    "FROM students " +
                    "WHERE student_id = ?";

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    cgpaSql
                            )
            ) {

                statement.setInt(
                        1,
                        studentId
                );

                try (
                        ResultSet result =
                                statement.executeQuery()
                ) {

                    if (result.next()) {

                        cgpa =
                                result.getDouble(
                                        "cgpa"
                                );
                    }
                }
            }

            // ==================================================
            // CGPA SCORE
            // ==================================================
            //
            // CGPA maximum = 10
            // Convert to maximum 5
            //
            // ==================================================

            double cgpaScore =
                    (cgpa / 10.0) * 5.0;

            if (cgpaScore > 5) {
                cgpaScore = 5;
            }

            if (cgpaScore < 0) {
                cgpaScore = 0;
            }

            // ==================================================
            // FINAL SCORE
            // ==================================================
            //
            // CGPA Score       = 5 maximum
            // Parameter Score  = 5 maximum
            //
            // Overall Score    = 10 maximum
            //
            // ==================================================

            double overallScore =
                    cgpaScore +
                    parameterScoreOutOfFive;

            if (overallScore > 10) {
                overallScore = 10;
            }

            if (overallScore < 0) {
                overallScore = 0;
            }

            System.out.println(
                    "CGPA Score / 5: " +
                    cgpaScore
            );

            System.out.println(
                    "Parameter Score / 5: " +
                    parameterScoreOutOfFive
            );

            System.out.println(
                    "Overall Score / 10: " +
                    overallScore
            );

            // ==================================================
            // UPDATE STUDENT SCORE
            // ==================================================

            String scoreSql =
                    "INSERT INTO student_scores " +
                    "(student_id, parameter_score, cgpa_score, overall_score) " +
                    "VALUES (?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "parameter_score = VALUES(parameter_score), " +
                    "cgpa_score = VALUES(cgpa_score), " +
                    "overall_score = VALUES(overall_score)";

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    scoreSql
                            )
            ) {

                statement.setInt(
                        1,
                        studentId
                );

                statement.setDouble(
                        2,
                        parameterScoreOutOfFive
                );

                statement.setDouble(
                        3,
                        cgpaScore
                );

                statement.setDouble(
                        4,
                        overallScore
                );

                statement.executeUpdate();
            }

            // ==================================================
            // COMMIT EVERYTHING
            // ==================================================

            connection.commit();

            connection.setAutoCommit(true);

            System.out.println(
                    "Profile updated successfully for student: " +
                    studentId
            );

            // ==================================================
            // SUCCESS
            // ==================================================

            sendResponse(
                    exchange,
                    200,
                    "PROFILE_UPDATED"
            );

        } catch (NumberFormatException e) {

            if (connection != null) {
                rollbackQuietly(connection);
            }

            e.printStackTrace();

            sendResponse(
                    exchange,
                    400,
                    "Invalid student ID."
            );

        } catch (SQLException e) {

            if (connection != null) {
                rollbackQuietly(connection);
            }

            e.printStackTrace();

            String message =
                    e.getMessage();

            if (message == null) {
                message =
                        "Database error occurred.";
            }

            if (
                    message.toLowerCase()
                            .contains("duplicate")
                    &&
                    message.toLowerCase()
                            .contains("email")
            ) {

                message =
                        "This email is already registered.";
            }

            sendResponse(
                    exchange,
                    500,
                    "Database Error: " +
                    message
            );

        } catch (Exception e) {

            if (connection != null) {
                rollbackQuietly(connection);
            }

            e.printStackTrace();

            sendResponse(
                    exchange,
                    500,
                    "Profile update failed: " +
                    e.getMessage()
            );

        } finally {

            if (connection != null) {

                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    // ==================================================
    // GET VALUE
    // ==================================================

    private static String getValue(
            Map<String, String> data,
            String key
    ) {

        String value =
                data.get(key);

        if (value == null) {
            return "";
        }

        return value.trim();
    }

    // ==================================================
    // LEVEL SCORE
    // ==================================================

    private static double getLevelScore(
            String level
    ) {

        if (level == null) {
            return 0;
        }

        switch (
                level.trim()
                        .toLowerCase()
        ) {

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

    // ==================================================
    // PROJECT SCORE
    // ==================================================

    private static double getProjectScore(
            String value
    ) {

        if (value == null) {
            return 0;
        }

        switch (
                value.trim()
                        .toLowerCase()
        ) {

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

    // ==================================================
    // CERTIFICATION SCORE
    // ==================================================

    private static double getCertificationScore(
            String value
    ) {

        if (value == null) {
            return 0;
        }

        switch (
                value.trim()
                        .toLowerCase()
        ) {

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

    // ==================================================
    // EXPERIENCE SCORE
    // ==================================================

    private static double getExperienceScore(
            String value
    ) {

        if (value == null) {
            return 0;
        }

        switch (
                value.trim()
                        .toLowerCase()
        ) {

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

    // ==================================================
    // PARSE FORM DATA
    // ==================================================

    private static Map<String, String> parseFormData(
            String body
    ) {

        Map<String, String> data =
                new HashMap<>();

        if (
                body == null ||
                body.isEmpty()
        ) {
            return data;
        }

        String[] pairs =
                body.split("&");

        for (String pair : pairs) {

            String[] parts =
                    pair.split(
                            "=",
                            2
                    );

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

                data.put(
                        key,
                        value
                );
            }
        }

        return data;
    }

    // ==================================================
    // ROLLBACK
    // ==================================================

    private static void rollbackQuietly(
            Connection connection
    ) {

        try {

            if (
                    connection != null &&
                    !connection.isClosed()
            ) {

                connection.rollback();
            }

        } catch (SQLException ignored) {
        }
    }

    // ==================================================
    // SEND RESPONSE
    // ==================================================

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

        exchange.getResponseBody()
                .write(responseBytes);

        exchange.getResponseBody()
                .close();
    }
}