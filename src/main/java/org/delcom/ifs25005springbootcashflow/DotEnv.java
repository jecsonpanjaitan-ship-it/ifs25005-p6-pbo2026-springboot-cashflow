package org.delcom.ifs25005springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Memuat berkas .env ke system properties sebelum Spring Boot start, sehingga placeholder
 * ${DB_HOST}, ${OPENAI_KEY}, dst. di application.yaml terisi. Environment server (cloud) selalu menang.
 */
public final class DotEnv {

    private DotEnv() {
    }

    public static void load() throws IOException {
        load(Path.of(".env"));
    }

    public static void load(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            return;
        }
        for (String raw : Files.readAllLines(file)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = unquote(line.substring(eq + 1).trim());
            if (System.getenv(key) == null && System.getProperty(key) == null) {
                System.setProperty(key, value);
            }
        }
    }

    static String unquote(String value) {
        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
        if (value.length() >= 2 && (doubleQuoted || singleQuoted)) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
