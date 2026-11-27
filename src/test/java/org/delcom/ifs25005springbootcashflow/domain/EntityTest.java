package org.delcom.ifs25005springbootcashflow.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.domain.entity.*;
import org.delcom.ifs25005springbootcashflow.domain.exception.*;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityTest {

    private static final LocalDateTime T0 = LocalDateTime.of(2026, 1, 1, 10, 0);

    private static Transaction tx(int id, String desc, long amount, TransactionType type, LocalDateTime at) {
        return new Transaction(id, 1, desc, amount, type, null, at);
    }

    @Test
    void transactionTypeLabelAndSign() {
        assertEquals("Pemasukan", TransactionType.INCOME.label());
        assertEquals("Pengeluaran", TransactionType.EXPENSE.label());
        assertEquals(500, TransactionType.INCOME.signed(500));
        assertEquals(-500, TransactionType.EXPENSE.signed(500));
    }

    @Test
    void transactionTypeFromText() throws Exception {
        assertEquals(TransactionType.INCOME, TransactionType.fromText("income"));
        assertEquals(TransactionType.INCOME, TransactionType.fromText(" Pemasukan "));
        assertEquals(TransactionType.EXPENSE, TransactionType.fromText("EXPENSE"));
        assertEquals(TransactionType.EXPENSE, TransactionType.fromText("pengeluaran"));
        assertThrows(ValidationException.class, () -> TransactionType.fromText(null));
        assertThrows(ValidationException.class, () -> TransactionType.fromText("  "));
        ValidationException ex = assertThrows(ValidationException.class, () -> TransactionType.fromText("lainnya"));
        assertTrue(ex.getMessage().contains("lainnya"));
    }

    @Test
    void transactionGettersAndProof() {
        Transaction t = new Transaction(7, 3, "Gaji", 1000, TransactionType.INCOME, "a.png", T0);
        assertEquals(7, t.getId());
        assertEquals(3, t.getUserId());
        assertEquals("Gaji", t.getDescription());
        assertEquals(1000, t.getAmount());
        assertEquals(TransactionType.INCOME, t.getType());
        assertEquals("a.png", t.getProofPath());
        assertEquals(T0, t.getCreatedAt());
        assertTrue(t.hasProof());
        assertEquals(1000, t.signedAmount());
        assertFalse(tx(1, "x", 1, TransactionType.EXPENSE, T0).hasProof());
        assertFalse(new Transaction(1, 1, "x", 1, TransactionType.EXPENSE, " ", T0).hasProof());
        assertEquals(-1, tx(1, "x", 1, TransactionType.EXPENSE, T0).signedAmount());
    }

    @Test
    void userGettersSettersAndFlags() {
        User u = new User(1, "budi", "hash", "Budi", Role.USER, null);
        assertEquals(1, u.getId());
        assertEquals("budi", u.getUsername());
        assertEquals("hash", u.getPasswordHash());
        assertEquals("Budi", u.getFullName());
        assertEquals(Role.USER, u.getRole());
        assertNull(u.getPhotoPath());
        assertFalse(u.hasPhoto());
        assertFalse(u.isAdmin());
        u.setPhotoPath(" ");
        assertFalse(u.hasPhoto());
        u.setPhotoPath("p.png");
        assertTrue(u.hasPhoto());
        assertEquals("p.png", u.getPhotoPath());
        u.setFullName("Budi S");
        u.setPasswordHash("h2");
        assertEquals("Budi S", u.getFullName());
        assertEquals("h2", u.getPasswordHash());
        assertTrue(new User(2, "admin", "h", "A", Role.ADMIN, null).isAdmin());
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    void aiRecordAndBalanceSummary() {
        AiGeneratedTransaction ai = new AiGeneratedTransaction("Kopi", 15000, TransactionType.EXPENSE);
        assertEquals("Kopi", ai.description());
        assertEquals(15000, ai.amount());
        assertEquals(TransactionType.EXPENSE, ai.type());
        BalanceSummary s = new BalanceSummary(3, 5_000_000, 25_000);
        assertEquals(3, s.count());
        assertEquals(5_000_000, s.totalIncome());
        assertEquals(25_000, s.totalExpense());
        assertEquals(4_975_000, s.balance());
    }

    @Test
    void sortOptions() {
        Transaction old = tx(1, "beta", 100, TransactionType.INCOME, T0);
        Transaction recent = tx(2, "Alpha", 300, TransactionType.EXPENSE, T0.plusDays(1));
        Transaction tie = tx(3, "gamma", 100, TransactionType.INCOME, T0.plusDays(1));
        List<Transaction> all = List.of(old, recent, tie);

        assertEquals(List.of(tie, recent, old), all.stream().sorted(SortOption.NEWEST_FIRST.comparator()).toList());
        assertEquals(List.of(old, recent, tie), all.stream().sorted(SortOption.OLDEST_FIRST.comparator()).toList());
        assertEquals(List.of(recent, tie, old), all.stream().sorted(SortOption.AMOUNT_HIGHEST.comparator()).toList());
        assertEquals(List.of(old, tie, recent), all.stream().sorted(SortOption.AMOUNT_LOWEST.comparator()).toList());
        assertEquals(List.of(recent, old, tie), all.stream().sorted(SortOption.DESCRIPTION_AZ.comparator()).toList());
        for (SortOption option : SortOption.values()) {
            assertFalse(option.label().isBlank());
        }
        assertTrue(SortOption.values().length >= 4);
    }

    @Test
    void typeFilter() {
        Transaction in = tx(1, "a", 1, TransactionType.INCOME, T0);
        Transaction out = tx(2, "b", 1, TransactionType.EXPENSE, T0);
        assertTrue(TransactionTypeFilter.ALL.matches(in) && TransactionTypeFilter.ALL.matches(out));
        assertTrue(TransactionTypeFilter.INCOME.matches(in));
        assertFalse(TransactionTypeFilter.INCOME.matches(out));
        assertTrue(TransactionTypeFilter.EXPENSE.matches(out));
        assertFalse(TransactionTypeFilter.EXPENSE.matches(in));
        assertEquals("Semua", TransactionTypeFilter.ALL.label());
        assertEquals("Pemasukan", TransactionTypeFilter.INCOME.label());
        assertEquals("Pengeluaran", TransactionTypeFilter.EXPENSE.label());
    }

    @Test
    void exceptions() {
        assertEquals("Transaksi dengan ID 9 tidak ditemukan", new EntityNotFoundException("Transaksi", 9).getMessage());
        assertInstanceOf(DomainException.class, new ValidationException("x"));
        assertInstanceOf(DomainException.class, new EntityNotFoundException("x", 1));
        assertEquals("x", new DomainException("x").getMessage());
    }
}
