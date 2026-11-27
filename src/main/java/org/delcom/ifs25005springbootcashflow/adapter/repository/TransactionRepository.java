package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.SortOption;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import java.time.LocalDateTime;
import java.util.List;

/** Implementasi in-memory ITransactionRepository (unit test). */
public class TransactionRepository extends InMemoryRepository<Transaction> implements ITransactionRepository {

    @Override
    protected int idOf(Transaction entity) {
        return entity.getId();
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        Transaction transaction = new Transaction(nextId(), userId, description, amount, type, proofPath,
                LocalDateTime.now());
        store.put(transaction.getId(), transaction);
        return transaction;
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return store.values().stream()
                .filter(t -> t.getUserId() == userId)
                .sorted(SortOption.NEWEST_FIRST.comparator())
                .toList();
    }
}
