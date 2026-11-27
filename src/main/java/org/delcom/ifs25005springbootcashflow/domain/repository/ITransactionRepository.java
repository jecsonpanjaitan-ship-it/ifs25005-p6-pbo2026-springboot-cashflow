package org.delcom.ifs25005springbootcashflow.domain.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import java.util.List;

public interface ITransactionRepository extends IRepository<Transaction, Integer> {

    Transaction save(int userId, String description, long amount, TransactionType type, String proofPath);

    /** Transaksi milik satu pengguna, terbaru di atas. */
    List<Transaction> findByUserId(int userId);
}
