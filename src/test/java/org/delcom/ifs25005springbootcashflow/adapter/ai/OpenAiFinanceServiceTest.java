package org.delcom.ifs25005springbootcashflow.adapter.ai;

import static org.junit.jupiter.api.Assertions.*;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Memakai server HTTP lokal palsu - TIDAK memanggil layanan AI sungguhan. */
class OpenAiFinanceServiceTest {

    private HttpServer server;
    private volatile int status = 200;
    private volatile String responseBody = "";
    private final AtomicReference<String> authHeader = new AtomicReference<>();
    private final AtomicReference<String> requestBody = new AtomicReference<>();

    @BeforeEach
    void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            authHeader.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static String chat(String content) {
        return "{\"choices\":[{\"message\":{\"content\":" + SimpleJson.quote(content) + "}}]}";
    }

    private OpenAiFinanceService service() {
        return new OpenAiFinanceService(url() + "/", "KUNCI", "model-x");
    }

    @Test
    void parsesFencedJsonAndSkipsInvalidItems() throws Exception {
        responseBody = chat("Berikut hasilnya:\n```json\n[1,"
                + "{\"description\":\"Gaji\",\"amount\":5000000,\"type\":\"INCOME\"},"
                + "{\"description\":\"Kos\",\"amount\":-1200000.4,\"type\":\"pengeluaran\"},"
                + "{\"amount\":10,\"type\":\"INCOME\"},"
                + "{\"description\":\"Tanpa jumlah\",\"type\":\"INCOME\"}]\n```");
        List<AiGeneratedTransaction> result = service().generateTransactions("gaji 5 juta, kos 1,2 juta");

        assertEquals(List.of(
                new AiGeneratedTransaction("Gaji", 5_000_000, TransactionType.INCOME),
                new AiGeneratedTransaction("Kos", 1_200_000, TransactionType.EXPENSE)), result);
        assertEquals("Bearer KUNCI", authHeader.get());
        assertTrue(requestBody.get().contains("\"model\":\"model-x\""));
        assertTrue(requestBody.get().contains("gaji 5 juta, kos 1,2 juta"));
    }

    @Test
    void endpointWithoutTrailingSlashAndPublicConstructor() throws Exception {
        responseBody = chat("[{\"description\":\"Kopi\",\"amount\":15000,\"type\":\"EXPENSE\"}]");
        List<AiGeneratedTransaction> result =
                new OpenAiFinanceService(url(), "k", "m").generateTransactions("kopi");
        assertEquals(1, result.size());
    }

    @Test
    void unknownTypeFails() {
        responseBody = chat("[{\"description\":\"X\",\"amount\":5}]");
        assertThrows(ValidationException.class, () -> service().generateTransactions("x"));
    }

    @Test
    void invalidResponses() {
        responseBody = "bukan json";
        assertEquals("Respons AI bukan JSON yang valid", message());
        responseBody = "[]";
        assertEquals("Respons AI tidak berisi jawaban", message());
        responseBody = "{\"choices\":[]}";
        assertEquals("Respons AI tidak berisi jawaban", message());
        responseBody = "{\"choices\":[1]}";
        assertEquals("Respons AI tidak berisi jawaban", message());
        responseBody = "{\"choices\":[{\"message\":{\"content\":5}}]}";
        assertEquals("Respons AI tidak berisi jawaban", message());
        responseBody = chat("tidak ada daftar");
        assertEquals("Jawaban AI tidak berisi daftar transaksi", message());
        responseBody = chat("] [");
        assertEquals("Jawaban AI tidak berisi daftar transaksi", message());
        responseBody = chat("[1,]");
        assertEquals("Daftar transaksi dari AI tidak valid", message());
    }

    @Test
    void httpErrorStatus() {
        status = 500;
        responseBody = "error";
        assertEquals("Layanan AI mengembalikan status 500", message());
    }

    @Test
    void connectionFailure() {
        OpenAiFinanceService offline = new OpenAiFinanceService("http://127.0.0.1:1", "k", "m");
        DomainException ex = assertThrows(DomainException.class, () -> offline.generateTransactions("x"));
        assertTrue(ex.getMessage().startsWith("Gagal menghubungi layanan AI"));
    }

    @Test
    void interruptedRequest() {
        Thread.currentThread().interrupt();
        try {
            DomainException ex = assertThrows(DomainException.class, () -> service().generateTransactions("x"));
            assertEquals("Permintaan ke layanan AI terputus", ex.getMessage());
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted(); // bersihkan flag agar tidak mengganggu test lain
        }
    }

    private String message() {
        return assertThrows(DomainException.class, () -> service().generateTransactions("x")).getMessage();
    }
}
