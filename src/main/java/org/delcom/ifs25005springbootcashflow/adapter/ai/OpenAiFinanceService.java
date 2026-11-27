package org.delcom.ifs25005springbootcashflow.adapter.ai;

import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IFinanceAiService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Adapter HTTP ke API OpenAI-compatible (OPENAI_ENDPOINT / OPENAI_KEY / OPENAI_MODEL). */
public class OpenAiFinanceService implements IFinanceAiService {

    static final String SYSTEM_PROMPT = "Kamu adalah asisten pencatat keuangan pribadi. "
            + "Ubah catatan pengguna menjadi JSON array berisi objek dengan field: "
            + "\"description\" (string singkat), \"amount\" (bilangan bulat rupiah positif, tanpa pemisah), "
            + "dan \"type\" (\"INCOME\" untuk pemasukan atau \"EXPENSE\" untuk pengeluaran). "
            + "Balas HANYA dengan JSON array, tanpa teks lain.";

    private final String endpoint;
    private final String apiKey;
    private final String model;
    private final HttpClient client;

    public OpenAiFinanceService(String endpoint, String apiKey, String model) {
        this(endpoint, apiKey, model, HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build());
    }

    OpenAiFinanceService(String endpoint, String apiKey, String model, HttpClient client) {
        this.endpoint = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        this.apiKey = apiKey;
        this.model = model;
        this.client = client;
    }

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        String body = "{\"model\":" + SimpleJson.quote(model) + ",\"temperature\":0.2,\"messages\":["
                + "{\"role\":\"system\",\"content\":" + SimpleJson.quote(SYSTEM_PROMPT) + "},"
                + "{\"role\":\"user\",\"content\":" + SimpleJson.quote(prompt) + "}]}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + "/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new DomainException("Gagal menghubungi layanan AI: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DomainException("Permintaan ke layanan AI terputus");
        }
        if (response.statusCode() / 100 != 2) {
            throw new DomainException("Layanan AI mengembalikan status " + response.statusCode());
        }
        return parseResponse(response.body());
    }

    private List<AiGeneratedTransaction> parseResponse(String json) throws DomainException {
        Object root;
        try {
            root = SimpleJson.parse(json);
        } catch (IllegalArgumentException e) {
            throw new DomainException("Respons AI bukan JSON yang valid");
        }
        String content = contentOf(root);
        if (content == null) {
            throw new DomainException("Respons AI tidak berisi jawaban");
        }
        return parseTransactions(content);
    }

    private static String contentOf(Object root) {
        if (!(member(root, "choices") instanceof List<?> choices) || choices.isEmpty()) {
            return null;
        }
        Object content = member(member(choices.get(0), "message"), "content");
        return content instanceof String text ? text : null;
    }

    private static Object member(Object node, String key) {
        return node instanceof Map<?, ?> map ? map.get(key) : null;
    }

    private List<AiGeneratedTransaction> parseTransactions(String content) throws DomainException {
        int start = content.indexOf('[');
        int end = content.lastIndexOf(']');
        if (start < 0 || end < start) {
            throw new DomainException("Jawaban AI tidak berisi daftar transaksi");
        }
        List<?> items;
        try {
            items = (List<?>) SimpleJson.parse(content.substring(start, end + 1));
        } catch (IllegalArgumentException e) {
            throw new DomainException("Daftar transaksi dari AI tidak valid");
        }
        List<AiGeneratedTransaction> result = new ArrayList<>();
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> map)) {
                continue;
            }
            String description = map.get("description") instanceof String text ? text.trim() : "";
            long amount = map.get("amount") instanceof Number number ? Math.abs(Math.round(number.doubleValue())) : 0;
            TransactionType type = TransactionType.fromText(map.get("type") instanceof String text ? text : null);
            if (!description.isEmpty() && amount > 0) {
                result.add(new AiGeneratedTransaction(description, amount, type));
            }
        }
        return result;
    }
}
