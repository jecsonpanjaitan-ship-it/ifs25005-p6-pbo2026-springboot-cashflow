package org.delcom.ifs25005springbootcashflow.domain.entity;

import java.time.LocalDateTime;

/** Transaksi keuangan milik satu pengguna, dengan bukti gambar opsional. */
public final class Transaction {

    private final int id;
    private final int userId;
    private final String description;
    private final long amount;
    private final TransactionType type;
    private final String proofPath;
    private final LocalDateTime createdAt;

    public Transaction(int id, int userId, String description, long amount,
                       TransactionType type, String proofPath, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.amount = amount;
        this.type = type;
        this.proofPath = proofPath;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public long getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public String getProofPath() {
        return proofPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean hasProof() {
        return proofPath != null && !proofPath.isBlank();
    }

    /** Pengaruh transaksi terhadap saldo (+ pemasukan, - pengeluaran). */
    public long signedAmount() {
        return type.signed(amount);
    }
}
