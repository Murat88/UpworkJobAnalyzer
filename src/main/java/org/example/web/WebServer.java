package org.example.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import dev.langchain4j.model.chat.ChatModel;
import org.example.model.ApplicationDecision;
import org.example.service.JobApplicationDecisionService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Minimal embedded HTTP server (no extra framework/dependency needed) that
 * exposes:
 * <ul>
 *     <li>GET  /          -> the static single-page UI ({@code static/index.html})</li>
 *     <li>POST /evaluate  -> body = raw pasted Upwork job posting text,
 *         response = JSON describing the {@link ApplicationDecision}</li>
 * </ul>
 */
public class WebServer {

    private final JobApplicationDecisionService decisionService;
    private final HttpServer server;

    public WebServer(ChatModel model, int port) throws IOException {
        this.decisionService = new JobApplicationDecisionService(model);
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.setExecutor(Executors.newFixedThreadPool(4));
        registerRoutes();
    }

    private void registerRoutes() {
        server.createContext("/", this::handleIndex);
        server.createContext("/evaluate", this::handleEvaluate);
    }

    public void start() {
        server.start();
    }

    private void handleIndex(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }
        byte[] html = loadResource("static/index.html");
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, html.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(html);
        }
    }

    private void handleEvaluate(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        String rawJobPostText;
        try (InputStream is = exchange.getRequestBody()) {
            rawJobPostText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        if (rawJobPostText == null || rawJobPostText.isBlank()) {
            sendJson(exchange, 400, "{\"error\":\"Please paste the job post text.\"}");
            return;
        }

        try {
            ApplicationDecision decision = decisionService.decideFromRawText(rawJobPostText);
            sendJson(exchange, 200, toJson(decision));
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage() == null ? e.toString() : e.getMessage();
            sendJson(exchange, 500, "{\"error\":" + jsonString(message) + "}");
        }
    }

    private void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private byte[] loadResource(String path) throws IOException {
        try (InputStream is = WebServer.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) {
                throw new IOException("Resource not found on classpath: " + path);
            }
            return is.readAllBytes();
        }
    }

    private String toJson(ApplicationDecision decision) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"shouldApply\":").append(decision.shouldApply()).append(",");
        sb.append("\"summary\":").append(jsonString(decision.summary())).append(",");

        sb.append("\"clientEvaluation\":");
        if (decision.clientEvaluationResult() != null) {
            sb.append("{\"passed\":").append(decision.clientEvaluationResult().passed())
              .append(",\"failedReasons\":").append(jsonArray(decision.clientEvaluationResult().failedReasons()))
              .append("}");
        } else {
            sb.append("null");
        }
        sb.append(",");

        sb.append("\"capabilityMatch\":");
        if (decision.capabilityMatchResult() != null) {
            sb.append("{\"score\":").append(decision.capabilityMatchResult().score())
              .append(",\"matchedTechnologies\":").append(jsonArray(decision.capabilityMatchResult().matchedTechnologies()))
              .append(",\"missingTechnologies\":").append(jsonArray(decision.capabilityMatchResult().missingTechnologies()))
              .append(",\"reasoning\":").append(jsonString(decision.capabilityMatchResult().reasoning()))
              .append("}");
        } else {
            sb.append("null");
        }
        sb.append(",");

        sb.append("\"coverLetter\":").append(jsonString(decision.coverLetter()));
        sb.append("}");
        return sb.toString();
    }

    private String jsonArray(List<String> items) {
        if (items == null) {
            return "[]";
        }
        return items.stream().map(this::jsonString).collect(Collectors.joining(",", "[", "]"));
    }

    private String jsonString(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}

