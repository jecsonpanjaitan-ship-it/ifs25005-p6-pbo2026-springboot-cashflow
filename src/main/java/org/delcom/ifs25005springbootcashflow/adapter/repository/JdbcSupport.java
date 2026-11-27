package org.delcom.ifs25005springbootcashflow.adapter.repository;

import java.util.Map;
import org.springframework.jdbc.support.KeyHolder;

/** Util kecil JDBC bersama. */
final class JdbcSupport {

    private JdbcSupport() {
    }

    /**
     * Mengambil kolom id dari generated keys. PostgreSQL mengembalikan "id", H2 "ID",
     * dan PostgreSQL dapat mengembalikan seluruh kolom, sehingga dicari tanpa peka huruf besar/kecil.
     */
    static int generatedId(KeyHolder keyHolder) {
        Map<String, Object> keys = keyHolder.getKeys();
        return keys.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase("id"))
                .map(e -> ((Number) e.getValue()).intValue())
                .findFirst()
                .orElseThrow();
    }
}
