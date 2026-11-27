package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DotEnvTest {

    @TempDir
    Path tmp;

    @Test
    void loadsValuesIntoSystemProperties() throws Exception {
        Properties before = (Properties) System.getProperties().clone();
        try {
            Path env = tmp.resolve(".env");
            Files.writeString(env, String.join("\n",
                    "# komentar", "", "tanpa-sama-dengan", "=kosong",
                    "DOTENV_TEST_PLAIN=nilai", "  DOTENV_TEST_QUOTED = \"dalam kutip\" ",
                    "DOTENV_TEST_SINGLE='kutip tunggal'", "DOTENV_TEST_PRESET=baru", "PATH=jangan-timpa"));
            System.setProperty("DOTENV_TEST_PRESET", "lama");

            DotEnv.load(env);

            assertEquals("nilai", System.getProperty("DOTENV_TEST_PLAIN"));
            assertEquals("dalam kutip", System.getProperty("DOTENV_TEST_QUOTED"));
            assertEquals("kutip tunggal", System.getProperty("DOTENV_TEST_SINGLE"));
            assertEquals("lama", System.getProperty("DOTENV_TEST_PRESET"));
            assertNull(System.getProperty("PATH"), "environment OS tidak boleh ditimpa");
            assertNull(System.getProperty("tanpa-sama-dengan"));
        } finally {
            System.setProperties(before);
        }
    }

    @Test
    void missingFileIsIgnoredAndDefaultLoadWorks() throws Exception {
        Properties before = (Properties) System.getProperties().clone();
        try {
            DotEnv.load(tmp.resolve("tidak-ada.env"));
            DotEnv.load(); // membaca .env di direktori kerja bila ada; tidak boleh melempar
        } finally {
            System.setProperties(before);
        }
    }

    @Test
    void unquote() {
        assertEquals("a", DotEnv.unquote("\"a\""));
        assertEquals("a", DotEnv.unquote("'a'"));
        assertEquals("a", DotEnv.unquote("a"));
        assertEquals("", DotEnv.unquote(""));
        assertEquals("\"", DotEnv.unquote("\""));
        assertEquals("\"a'", DotEnv.unquote("\"a'"));
        assertEquals("'a\"", DotEnv.unquote("'a\""));
    }
}
