package app;

import afds.AFD;
import afds.Estado;
import afds.Simbolo;
import afds.TransicaoD;
import java.util.ArrayList;
import java.util.List;

/** Adapta o AFD fornecido para análise de texto com rastreamento de estados. */
public final class Lexer {
    private final AFD afd;

    public Lexer(AFD afd) { this.afd = afd; }

    public String analyze(String source) {
        List<String> tokens = new ArrayList<>();
        List<String> steps = new ArrayList<>();
        Estado initial = afd.getEstadoInicial();
        Estado current = initial;
        int start = -1, startLine = 1, startColumn = 1;
        int line = 1, column = 1;
        String error = null;

        for (int i = 0; i < source.length(); i++) {
            char raw = source.charAt(i);
            // O exemplo original converte LF em espaço. CRLF é tratado como uma quebra só.
            if (raw == '\r' && i + 1 < source.length() && source.charAt(i + 1) == '\n') {
                continue;
            }
            char symbol = raw == '\n' || raw == '\r' ? ' ' : raw;
            boolean separator = symbol == ' ';
            if (start < 0 && separator) {
                steps.add(step(line, column, display(raw), "1", "1", "Separador ignorado entre tokens"));
            } else {
                if (start < 0) { start = i; startLine = line; startColumn = column; }
                String from = current.getNome();
                Estado next = afd.p(current, new Simbolo(symbol));
                if (next == null) {
                    String reason = separator ? "Número incompleto antes do separador" : "Não existe transição para este símbolo";
                    steps.add(step(line, column, display(raw), from, "—", reason));
                    error = "{\"line\":" + line + ",\"column\":" + column + ",\"index\":" + i
                            + ",\"symbol\":" + q(display(raw)) + ",\"message\":" + q(reason) + "}";
                    break;
                }
                String action = next.getNome().equals("F") ? "Token reconhecido" : description(next.getNome());
                steps.add(step(line, column, display(raw), from, next.getNome(), action));
                if (afd.getEstadosFinais().pertence(next)) {
                    int end = raw == '\n' && i > 0 && source.charAt(i - 1) == '\r' ? i - 1 : i;
                    String lexeme = source.substring(start, end);
                    String type = from.equals("4") ? "Decimal" : "Inteiro";
                    tokens.add(token(lexeme, type, startLine, startColumn, start));
                    current = initial;
                    start = -1;
                } else {
                    current = next;
                }
            }
            if (raw == '\n' || raw == '\r') { line++; column = 1; }
            else column++;
        }

        if (error == null && start >= 0) {
            String from = current.getNome();
            Estado next = afd.p(current, new Simbolo(' '));
            if (next != null && afd.getEstadosFinais().pertence(next)) {
                String lexeme = source.substring(start);
                tokens.add(token(lexeme, from.equals("4") ? "Decimal" : "Inteiro", startLine, startColumn, start));
                steps.add(step(line, column, "EOF", from, "F", "Fim do texto: token reconhecido"));
            } else {
                steps.add(step(line, column, "EOF", from, "—", "Número incompleto no fim do texto"));
                error = "{\"line\":" + line + ",\"column\":" + column + ",\"index\":" + source.length()
                        + ",\"symbol\":\"EOF\",\"message\":\"Número incompleto no fim do texto\"}";
            }
        }
        return "{\"tokens\":[" + String.join(",", tokens) + "],\"steps\":[" + String.join(",", steps)
                + "],\"error\":" + (error == null ? "null" : error) + "}";
    }

    public String transitions() {
        List<String> rows = new ArrayList<>();
        for (Object item : afd.getFuncaoPrograma().getElementos()) {
            TransicaoD t = (TransicaoD) item;
            rows.add("{\"from\":" + q(t.getOrigem().getNome()) + ",\"symbol\":" + q(display(t.getSimbolo().getSimbolo()))
                    + ",\"to\":" + q(t.getDestino().getNome()) + "}");
        }
        return "[" + String.join(",", rows) + "]";
    }

    private static String token(String value, String type, int line, int column, int index) {
        return "{\"lexeme\":" + q(value) + ",\"type\":" + q(type) + ",\"line\":" + line
                + ",\"column\":" + column + ",\"index\":" + index + "}";
    }

    private static String step(int line, int column, String symbol, String from, String to, String action) {
        return "{\"line\":" + line + ",\"column\":" + column + ",\"symbol\":" + q(symbol)
                + ",\"from\":" + q(from) + ",\"to\":" + q(to) + ",\"action\":" + q(action) + "}";
    }

    private static String description(String state) {
        switch (state) {
            case "2": return "Zero inicial lido";
            case "3": return "Ponto lido; falta um dígito";
            case "4": return "Parte decimal válida";
            case "5": return "Parte inteira válida";
            default: return "Transição realizada";
        }
    }

    private static String display(char c) {
        if (c == '\\') return "\\n (XML)";
        if (c == '\n') return "\\n";
        if (c == '\r') return "\\r";
        if (c == ' ') return "espaço";
        if (c == '\t') return "\\t";
        return String.valueOf(c);
    }

    private static String q(String s) {
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int)c));
                    else out.append(c);
            }
        }
        return out.append('"').toString();
    }
}
