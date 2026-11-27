package org.delcom.ifs25005springbootcashflow.adapter.presenter;

import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Format uang, tanggal, dan pesan notifikasi (bukan tanggung jawab entity). */
public class FinancePresenter {

    private static final DateTimeFormatter DATE_TIME =
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID"));

    /** Contoh: Rp 1,000,000.00 (pemisah ribuan koma, dua digit desimal). */
    public String formatAmount(long amount) {
        String sign = amount < 0 ? "-" : "";
        return sign + String.format(Locale.US, "Rp %,.2f", BigDecimal.valueOf(amount).abs());
    }

    /** Contoh: [3] Gaji | Rp 1,000,000.00 | [Pemasukan] */
    public String formatTransaction(Transaction t) {
        return "[" + t.getId() + "] " + t.getDescription() + " | " + formatAmount(t.getAmount())
                + " | [" + t.getType().label() + "]";
    }

    /** Contoh: Saldo: Rp 4,975,000.00 */
    public String formatBalance(long balance) {
        return "Saldo: " + formatAmount(balance);
    }

    public String formatDateTime(LocalDateTime dateTime) {
        return DATE_TIME.format(dateTime);
    }

    public String addedMessage(Transaction t) {
        return "Transaksi ditambahkan: " + formatTransaction(t);
    }

    public String deletedMessage(Transaction t) {
        return "Transaksi dihapus: " + formatTransaction(t);
    }

    public String aiSavedMessage(int count) {
        return count + " transaksi dari AI berhasil ditambahkan";
    }
}
