package org.delcom.ifs25005springbootcashflow.adapter.ai;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parser JSON minimal (tanpa dependensi): objek -> Map, array -> List, string -> String,
 * angka -> Double, true/false -> Boolean, null -> null. Gagal -> IllegalArgumentException.
 */
final class SimpleJson {

    private final String text;
    private int pos;

    private SimpleJson(String text) {
        this.text = text;
    }

    static Object parse(String text) {
        SimpleJson parser = new SimpleJson(text);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (parser.pos != text.length()) {
            throw parser.error("Karakter berlebih");
        }
        return value;
    }

    /** Membungkus teks sebagai string JSON (dengan tanda kutip). */
    static String quote(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : value.toCharArray()) {
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
        return sb.append('"').toString();
    }

    private Object readValue() {
        if (pos >= text.length()) {
            throw error("Akhir data tak terduga");
        }
        return switch (text.charAt(pos)) {
            case '{' -> readObject();
            case '[' -> readArray();
            case '"' -> readString();
            case 't' -> readLiteral("true", Boolean.TRUE);
            case 'f' -> readLiteral("false", Boolean.FALSE);
            case 'n' -> readLiteral("null", null);
            default -> readNumber();
        };
    }

    private Map<String, Object> readObject() {
        pos++;
        Map<String, Object> map = new LinkedHashMap<>();
        skipWhitespace();
        if (peek('}')) {
            pos++;
            return map;
        }
        while (true) {
            skipWhitespace();
            if (!peek('"')) {
                throw error("Kunci objek harus berupa string");
            }
            String key = readString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            map.put(key, readValue());
            skipWhitespace();
            if (peek(',')) {
                pos++;
            } else {
                expect('}');
                return map;
            }
        }
    }

    private List<Object> readArray() {
        pos++;
        List<Object> list = new ArrayList<>();
        skipWhitespace();
        if (peek(']')) {
            pos++;
            return list;
        }
        while (true) {
            skipWhitespace();
            list.add(readValue());
            skipWhitespace();
            if (peek(',')) {
                pos++;
            } else {
                expect(']');
                return list;
            }
        }
    }

    private String readString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (true) {
            if (pos >= text.length()) {
                throw error("String tidak ditutup");
            }
            char c = text.charAt(pos++);
            if (c == '"') {
                return sb.toString();
            }
            if (c != '\\') {
                sb.append(c);
                continue;
            }
            if (pos >= text.length()) {
                throw error("Escape tidak lengkap");
            }
            char escaped = text.charAt(pos++);
            switch (escaped) {
                case '"', '\\', '/' -> sb.append(escaped);
                case 'b' -> sb.append('\b');
                case 'f' -> sb.append('\f');
                case 'n' -> sb.append('\n');
                case 'r' -> sb.append('\r');
                case 't' -> sb.append('\t');
                case 'u' -> {
                    if (pos + 4 > text.length()) {
                        throw error("Escape unicode tidak lengkap");
                    }
                    sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                    pos += 4;
                }
                default -> throw error("Escape tidak dikenal");
            }
        }
    }

    private Object readLiteral(String word, Object value) {
        if (!text.startsWith(word, pos)) {
            throw error("Nilai tidak valid");
        }
        pos += word.length();
        return value;
    }

    private Double readNumber() {
        int start = pos;
        while (pos < text.length() && "+-.eE0123456789".indexOf(text.charAt(pos)) >= 0) {
            pos++;
        }
        if (start == pos) {
            throw error("Nilai tidak valid");
        }
        return Double.valueOf(text.substring(start, pos));
    }

    private void skipWhitespace() {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
    }

    private boolean peek(char c) {
        return pos < text.length() && text.charAt(pos) == c;
    }

    private void expect(char c) {
        if (!peek(c)) {
            throw error("Diharapkan '" + c + "'");
        }
        pos++;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " pada posisi " + pos);
    }
}
