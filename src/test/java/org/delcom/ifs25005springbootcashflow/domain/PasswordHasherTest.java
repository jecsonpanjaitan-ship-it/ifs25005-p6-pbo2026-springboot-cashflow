package org.delcom.ifs25005springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordHasherTest {

    @Test
    void hashIsSha256Hex() {
        assertEquals("8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918",
                PasswordHasher.hash("admin"));
    }

    @Test
    void matches() {
        String hash = PasswordHasher.hash("rahasia");
        assertTrue(PasswordHasher.matches("rahasia", hash));
        assertFalse(PasswordHasher.matches("salah", hash));
    }

    @Test
    void unknownAlgorithmFails() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> PasswordHasher.digestHex("TIDAK-ADA", "x"));
        assertTrue(ex.getMessage().contains("TIDAK-ADA"));
    }
}
