package org.delcom.ifs25005springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileStorageTest {

    @TempDir
    Path tmp;

    @Test
    void storesFileWithUuidNameAndLowercaseExtension() throws Exception {
        FileStorage storage = new FileStorage(tmp.resolve("uploads").toString());
        String name = storage.store("Foto.PNG", new byte[] {1, 2, 3});
        assertTrue(name.matches("[0-9a-f-]{36}\\.png"));
        assertArrayEquals(new byte[] {1, 2, 3}, Files.readAllBytes(storage.root().resolve(name)));
        assertEquals("/uploads/" + name, storage.url(name));
        assertTrue(storage.root().endsWith("uploads"));
    }

    @Test
    void rejectsInvalidFiles() {
        FileStorage storage = new FileStorage(tmp.toString());
        assertThrows(ValidationException.class, () -> storage.store("a.png", null));
        assertThrows(ValidationException.class, () -> storage.store("a.png", new byte[0]));
        assertThrows(ValidationException.class,
                () -> storage.store("a.png", new byte[(int) FileStorage.MAX_BYTES + 1]));
        assertThrows(ValidationException.class, () -> storage.store("a.exe", new byte[] {1}));
        assertThrows(ValidationException.class, () -> storage.store("tanpaekstensi", new byte[] {1}));
        assertThrows(ValidationException.class, () -> storage.store(null, new byte[] {1}));
    }

    @Test
    void failsWhenFolderCannotBeCreated() throws Exception {
        Path regularFile = Files.createFile(tmp.resolve("bukan-folder"));
        FileStorage storage = new FileStorage(regularFile.resolve("sub").toString());
        ValidationException ex = assertThrows(ValidationException.class, () -> storage.store("a.png", new byte[] {1}));
        assertTrue(ex.getMessage().startsWith("Gagal menyimpan file"));
    }
}
