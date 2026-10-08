import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class CareerScopeServer {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        try {

            HttpServer server = HttpServer.create(
                    new InetSocketAddress(PORT),
                    0
            );

            // =========================
            // API ROUTES
            // =========================

            server.createContext("/api/test", CareerScopeServer::testHandler);

            server.createContext(
                    "/api/login",
                    LoginHandler::handle
            );

            server.createContext(
                    "/api/signup",
                    SignupHandler::handle
            );

            server.createContext(
                    "/api/profile/update",
                    ProfileUpdateHandler::handle
            );

            server.createContext(
                    "/api/dashboard",
                    DashboardHandler::handle
            );

            server.createContext(
                    "/api/ranking",
                    RankingHandler::handle
            );

            server.createContext(
                    "/api/score",
                    ScoreHandler::handle
            );

            server.createContext(
                    "/api/simulator",
                    SimulatorHandler::handle
            );

            server.createContext(
                    "/api/jobs",
                    JobHandler::handle
            );


            // =========================
            // SERVER EXECUTOR
            // =========================

            server.setExecutor(
                    Executors.newFixedThreadPool(20)
            );


            // =========================
            // START SERVER
            // =========================

            server.start();

            System.out.println();
            System.out.println("========================================");
            System.out.println("       CAREERSCOPE BACKEND SERVER");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Server Status : RUNNING");
            System.out.println("Port          : " + PORT);
            System.out.println("Base URL      : http://localhost:" + PORT);
            System.out.println();
            System.out.println("Available APIs:");
            System.out.println("----------------------------------------");
            System.out.println("GET  /api/test");
            System.out.println("POST /api/login");
            System.out.println("POST /api/signup");
            System.out.println("POST /api/profile/update");
            System.out.println("GET  /api/dashboard");
            System.out.println("GET  /api/ranking");
            System.out.println("GET  /api/score");
            System.out.println("POST /api/simulator");
            System.out.println("GET  /api/jobs");
            System.out.println("----------------------------------------");
            System.out.println();
            System.out.println("Database : MySQL");
            System.out.println("Status   : Ready");
            System.out.println();
            System.out.println("========================================");
            System.out.println();

        } catch (IOException e) {

            System.out.println();
            System.out.println("========================================");
            System.out.println("       SERVER STARTUP FAILED");
            System.out.println("========================================");
            System.out.println();

            if (e.getMessage() != null &&
                    e.getMessage().contains("Address already in use")) {

                System.out.println(
                        "Port " + PORT + " is already being used."
                );

                System.out.println();
                System.out.println(
                        "Close the previous CareerScope server"
                );

                System.out.println(
                        "and start the server again."
                );

            } else {

                e.printStackTrace();
            }
        }
    }


    // =========================
    // TEST API
    // =========================

    private static void testHandler(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if (!exchange.getRequestMethod()
                .equalsIgnoreCase("GET")) {

            sendResponse(
                    exchange,
                    405,
                    "Method Not Allowed"
            );

            return;
        }

        try {

            DatabaseConnection.getConnection().close();

            sendResponse(
                    exchange,
                    200,
                    "CareerScope Server Running | Database Connected Successfully!"
            );

        } catch (Exception e) {

            sendResponse(
                    exchange,
                    500,
                    "Server Running | Database Connection Failed"
            );
        }
    }


    // =========================
    // CORS
    // =========================

    public static void addCorsHeaders(
            HttpExchange exchange
    ) {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET, POST, PUT, DELETE, OPTIONS"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Max-Age",
                "3600"
        );
    }


    // =========================
    // RESPONSE
    // =========================

    public static void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        addCorsHeaders(exchange);

        byte[] responseBytes =
                response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "text/plain; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(responseBytes);
        }
    }


    // =========================
    // JSON RESPONSE
    // =========================

    public static void sendJsonResponse(
            HttpExchange exchange,
            int statusCode,
            String json
    ) throws IOException {

        addCorsHeaders(exchange);

        byte[] responseBytes =
                json.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        try (OutputStream output =
                     exchange.getResponseBody()) {

            output.write(responseBytes);
        }
    }
}