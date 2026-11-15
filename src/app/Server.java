package app;

import afds.AFD;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public final class Server {
    private static final int MAX_INPUT = 1024 * 1024;

    public static void main(String[] args) throws Exception {
        Path root = Path.of("").toAbsolutePath();
        AFD afd = new AFD();
        afd.ler(root.resolve("config/AFD.XML").toString());
        Lexer lexer = new Lexer(afd);
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        server.createContext("/", exchange -> route(exchange, root, lexer));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        System.out.println("Analisador Léxico Web: http://127.0.0.1:" + port);
    }

    private static void route(HttpExchange exchange, Path root, Lexer lexer) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            if (path.equals("/api/analisar") && method.equals("POST")) {
                byte[] bytes = exchange.getRequestBody().readNBytes(MAX_INPUT + 1);
                if (bytes.length > MAX_INPUT) { send(exchange, 413, "application/json", "{\"error\":\"Texto acima de 1 MB\"}"); return; }
                send(exchange, 200, "application/json", lexer.analyze(new String(bytes, StandardCharsets.UTF_8)));
                return;
            }
            if (path.equals("/api/transicoes") && method.equals("GET")) {
                send(exchange, 200, "application/json", lexer.transitions()); return;
            }
            if (method.equals("GET") && (path.equals("/") || path.equals("/index.html") || path.equals("/app.js") || path.equals("/style.css"))) {
                String file = path.equals("/") ? "index.html" : path.substring(1);
                String mime = file.endsWith(".js") ? "application/javascript" : file.endsWith(".css") ? "text/css" : "text/html";
                send(exchange, 200, mime, Files.readString(root.resolve("web").resolve(file), StandardCharsets.UTF_8));
                return;
            }
            send(exchange, 404, "text/plain", "Não encontrado");
        } catch (Exception e) {
            send(exchange, 500, "text/plain", "Erro interno: " + e.getMessage());
        }
    }

    private static void send(HttpExchange exchange, int status, String mime, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", mime + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, data.length);
        try (var output = exchange.getResponseBody()) { output.write(data); }
    }
}
