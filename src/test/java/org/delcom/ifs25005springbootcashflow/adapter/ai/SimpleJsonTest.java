package org.delcom.ifs25005springbootcashflow.adapter.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SimpleJsonTest {

    @Test
    void parsesAllValueTypes() {
        Object parsed = SimpleJson.parse(" { \"a\" : [1, -2.5e1, \"x\", true, false, null], \"b\": {}, \"c\": [] } ");
        Map<?, ?> map = assertInstanceOf(Map.class, parsed);
        assertEquals(List.of(1.0, -25.0, "x", true, false), ((List<?>) map.get("a")).subList(0, 5));
        assertNull(((List<?>) map.get("a")).get(5));
        assertEquals(Map.of(), map.get("b"));
        assertEquals(List.of(), map.get("c"));
    }

    @Test
    void parsesNumberAtEndOfInput() {
        assertEquals(5.0, SimpleJson.parse("5"));
    }

    @Test
    void parsesEscapes() {
        String json = "\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\"";
        assertEquals("\"\\/\b\f\n\r\tA", SimpleJson.parse(json));
    }

    @Test
    void rejectsMalformedInput() {
        String[] bad = {
                "", "{1:2}", "{\"a\" 1}", "{\"a\":1 \"b\":2}", "[1 2]", "[1,]", "\"abc", "\"\\", "\"\\x\"",
                "\"\\u12\"", "\"\\uZZZZ\"", "tru", "1 2", "@", "nul", "{", "[", "{\"a\":1", "[1"};
        for (String text : bad) {
            assertThrows(IllegalArgumentException.class, () -> SimpleJson.parse(text), text);
        }
    }

    @Test
    void quotesAndEscapes() {
        assertEquals("\"abc\"", SimpleJson.quote("abc"));
        assertEquals("\"a\\\"b\\\\c\\n\\r\\t\\u0001\"", SimpleJson.quote("a\"b\\c\n\r\t\u0001"));
        assertEquals("a\"b", SimpleJson.parse(SimpleJson.quote("a\"b")));
    }
}
