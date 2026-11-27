package org.delcom.ifs25005springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.junit.jupiter.api.Test;

class FinancePresenterTest {

    private final FinancePresenter presenter = new FinancePresenter();
    private final Transaction salary = new Transaction(3, 1, "Gaji", 1_000_000, TransactionType.INCOME, null,
            LocalDateTime.of(2026, 11, 27, 8, 12));

    @Test
    void formatsAmounts() {
        assertEquals("Rp 1,000,000.00", presenter.formatAmount(1_000_000));
        assertEquals("Rp 0.00", presenter.formatAmount(0));
        assertEquals("Rp 25,000.00", presenter.formatAmount(25_000));
        assertEquals("-Rp 1,500.00", presenter.formatAmount(-1_500));
    }

    @Test
    void formatsTransactionAndBalance() {
        assertEquals("[3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.formatTransaction(salary));
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(4_975_000));
    }

    @Test
    void formatsDateAndMessages() {
        String date = presenter.formatDateTime(salary.getCreatedAt());
        assertTrue(date.startsWith("27 ") && date.endsWith("2026 08:12"), date);
        assertEquals("Transaksi ditambahkan: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.addedMessage(salary));
        assertEquals("Transaksi dihapus: [3] Gaji | Rp 1,000,000.00 | [Pemasukan]", presenter.deletedMessage(salary));
        assertEquals("2 transaksi dari AI berhasil ditambahkan", presenter.aiSavedMessage(2));
    }
}
