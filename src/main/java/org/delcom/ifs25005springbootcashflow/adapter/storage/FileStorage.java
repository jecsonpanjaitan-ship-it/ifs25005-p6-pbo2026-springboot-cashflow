package org.delcom.ifs25005springbootcashflow.adapter.storage;

import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/** Menyimpan upload (foto profil & bukti transaksi) ke folder UPLOAD_DIR dengan nama UUID. */
public class FileStorage {

    public static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED = Set.of("png", "jpg", "jpeg", "gif", "webp");

    private final Path root;

    public FileStorage(String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
        root.toFile().mkdirs();
    }

    public Path root() {
        return root;
    }

    /** Menyimpan file dan mengembalikan path relatif (nama file) untuk disimpan di database. */
    public String store(String originalName, byte[] data) throws ValidationException {
        if (data == null || data.length == 0) {
            throw new ValidationException("File kosong");
        }
        if (data.length > MAX_BYTES) {
            throw new ValidationException("Ukuran file maksimal 5 MB");
        }
        String extension = extensionOf(originalName);
        if (!ALLOWED.contains(extension)) {
            throw new ValidationException("Format gambar tidak didukung (png, jpg, jpeg, gif, webp)");
        }
        String name = UUID.randomUUID() + "." + extension;
        try {
            Files.createDirectories(root);
            Files.write(root.resolve(name), data);
        } catch (IOException e) {
            throw new ValidationException("Gagal menyimpan file: " + e.getMessage());
        }
        return name;
    }

    /** URL publik (dilayani lewat /uploads/**) dari path relatif. */
    public String url(String relativePath) {
        return "/uploads/" + relativePath;
    }

    private static String extensionOf(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        return dot < 0 ? "" : originalName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
