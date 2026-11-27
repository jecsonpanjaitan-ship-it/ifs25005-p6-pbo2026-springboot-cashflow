package org.delcom.ifs25005springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25005springbootcashflow.domain.entity.*;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FinanceUseCaseTest {

    private FinanceUseCase finance;

    @BeforeEach
    void setUp() {
        finance = new FinanceUseCase(new TransactionRepository());
    }

    @Test
    void parseAndValidateInput() throws Exception {
        assertEquals(1500, finance.parseAmount(" 1500 "));
        assertThrows(ValidationException.class, () -> finance.parseAmount(null));
        assertThrows(ValidationException.class, () -> finance.parseAmount("  "));
        assertThrows(ValidationException.class, () -> finance.parseAmount("abc"));
        assertThrows(ValidationException.class, () -> finance.parseAmount("12.5"));
        assertThrows(ValidationException.class, () -> finance.parseAmount("0"));
        assertThrows(ValidationException.class, () -> finance.parseAmount("-10"));
        assertEquals(50, finance.validateInput("Kopi", "50"));
        assertThrows(ValidationException.class, () -> finance.validateInput(null, "50"));
        assertThrows(ValidationException.class, () -> finance.validateInput("  ", "50"));
        assertThrows(ValidationException.class, () -> finance.validateInput("Kopi", "x"));
    }

    @Test
    void addTransaction() throws Exception {
        Transaction t = finance.addTransaction(1, "  Gaji ", "5000000", TransactionType.INCOME, "b.png");
        assertEquals("Gaji", t.getDescription());
        assertEquals(5_000_000, t.getAmount());
        assertEquals("b.png", t.getProofPath());
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, "x", "bad", TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(0, "x", 5L, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, " ", 5L, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, "x", 0L, TransactionType.INCOME, null));
        assertThrows(ValidationException.class, () -> finance.addTransaction(1, "x", 5L, null, null));
    }

    @Test
    void getAndDelete() throws Exception {
        Transaction t = finance.addTransaction(1, "Makan", 25_000L, TransactionType.EXPENSE, null);
        assertSame(t, finance.getTransaction(t.getId()));
        assertThrows(ValidationException.class, () -> finance.getTransaction(0));
        assertThrows(EntityNotFoundException.class, () -> finance.getTransaction(999));
        ValidationException notOwner = assertThrows(ValidationException.class,
                () -> finance.deleteTransaction(2, t.getId()));
        assertEquals("Transaksi bukan milik Anda", notOwner.getMessage());
        assertSame(t, finance.deleteTransaction(1, t.getId()));
        assertThrows(EntityNotFoundException.class, () -> finance.deleteTransaction(1, t.getId()));
    }

    @Test
    void listFilterSearchSortAndDefaults() throws Exception {
        Transaction a = finance.addTransaction(1, "Gaji Bulanan", 5_000_000L, TransactionType.INCOME, null);
        Transaction b = finance.addTransaction(1, "Makan siang", 25_000L, TransactionType.EXPENSE, null);
        Transaction c = finance.addTransaction(1, "Bonus gaji", 100_000L, TransactionType.INCOME, null);
        finance.addTransaction(2, "Milik orang lain", 1L, TransactionType.INCOME, null);

        // default: semua tipe, urutan terbaru (id terbesar) di atas
        assertEquals(List.of(c, b, a), finance.listTransactions(1));
        assertEquals(List.of(c, b, a), finance.listTransactions(1, null, null, null));
        assertEquals(List.of(c, a), finance.listTransactions(1, "  GAJI ", TransactionTypeFilter.ALL, SortOption.NEWEST_FIRST));
        assertEquals(List.of(a, c), finance.listTransactions(1, "", TransactionTypeFilter.INCOME, SortOption.AMOUNT_HIGHEST));
        assertEquals(List.of(b), finance.listTransactions(1, null, TransactionTypeFilter.EXPENSE, null));
        assertEquals(List.of(), finance.listTransactions(1, "tidak ada", null, null));
    }

    @Test
    void balance() throws Exception {
        assertEquals(new BalanceSummary(0, 0, 0), finance.getBalance(1));
        finance.addTransaction(1, "Gaji", 5_000_000L, TransactionType.INCOME, null);
        finance.addTransaction(1, "Makan", 25_000L, TransactionType.EXPENSE, null);
        BalanceSummary s = finance.getBalance(1);
        assertEquals(2, s.count());
        assertEquals(4_975_000, s.balance());
        finance.deleteTransaction(1, 2);
        assertEquals(5_000_000, finance.getBalance(1).balance());
    }
}
