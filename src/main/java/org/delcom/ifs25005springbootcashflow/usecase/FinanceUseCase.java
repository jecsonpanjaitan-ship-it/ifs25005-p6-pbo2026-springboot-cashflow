package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.entity.BalanceSummary;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionTypeFilter;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.EntityNotFoundException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import java.util.List;
import java.util.Locale;

/** Proses bisnis transaksi keuangan (tanpa komponen UI). */
public class FinanceUseCase {

    private final ITransactionRepository repository;

    public FinanceUseCase(ITransactionRepository repository) {
        this.repository = repository;
    }

    /** Validasi form (keterangan + jumlah teks) tanpa menyimpan; mengembalikan jumlah numerik. */
    public long validateInput(String description, String amountText) throws ValidationException {
        requireDescription(description);
        return parseAmount(amountText);
    }

    public long parseAmount(String text) throws ValidationException {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Jumlah wajib diisi");
        }
        long value;
        try {
            value = Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Jumlah harus berupa angka bulat");
        }
        requirePositive(value);
        return value;
    }

    public Transaction addTransaction(int userId, String description, String amountText,
                                      TransactionType type, String proofPath) throws DomainException {
        return addTransaction(userId, description, validateInput(description, amountText), type, proofPath);
    }

    public Transaction addTransaction(int userId, String description, long amount,
                                      TransactionType type, String proofPath) throws DomainException {
        requireValidId(userId);
        requireDescription(description);
        requirePositive(amount);
        if (type == null) {
            throw new ValidationException("Tipe transaksi wajib dipilih");
        }
        return repository.save(userId, description.trim(), amount, type, proofPath);
    }

    public Transaction getTransaction(int id) throws DomainException {
        requireValidId(id);
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Transaksi", id));
    }

    /** Hapus transaksi milik sendiri; mengembalikan transaksi yang dihapus. */
    public Transaction deleteTransaction(int requesterId, int transactionId) throws DomainException {
        Transaction transaction = getTransaction(transactionId);
        if (transaction.getUserId() != requesterId) {
            throw new ValidationException("Transaksi bukan milik Anda");
        }
        repository.deleteById(transactionId);
        return transaction;
    }

    public List<Transaction> listTransactions(int userId) {
        return listTransactions(userId, null, null, null);
    }

    /** Daftar transaksi pengguna; default: semua tipe, urutan terbaru di atas. */
    public List<Transaction> listTransactions(int userId, String keyword, TransactionTypeFilter filter,
                                              SortOption sort) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        TransactionTypeFilter typeFilter = filter == null ? TransactionTypeFilter.ALL : filter;
        SortOption order = sort == null ? SortOption.NEWEST_FIRST : sort;
        return repository.findByUserId(userId).stream()
                .filter(t -> t.getDescription().toLowerCase(Locale.ROOT).contains(needle))
                .filter(typeFilter::matches)
                .sorted(order.comparator())
                .toList();
    }

    public BalanceSummary getBalance(int userId) {
        List<Transaction> all = repository.findByUserId(userId);
        long income = all.stream().filter(TransactionTypeFilter.INCOME::matches).mapToLong(Transaction::getAmount).sum();
        long expense = all.stream().filter(TransactionTypeFilter.EXPENSE::matches).mapToLong(Transaction::getAmount).sum();
        return new BalanceSummary(all.size(), income, expense);
    }

    private static void requireValidId(int id) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException("ID tidak valid: " + id);
        }
    }

    private static void requireDescription(String description) throws ValidationException {
        if (description == null || description.isBlank()) {
            throw new ValidationException("Keterangan wajib diisi");
        }
    }

    private static void requirePositive(long amount) throws ValidationException {
        if (amount <= 0) {
            throw new ValidationException("Jumlah harus lebih dari nol");
        }
    }
}
