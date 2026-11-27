package org.delcom.ifs25005springbootcashflow.domain.entity;

/** Filter jenis transaksi pada daftar. */
public enum TransactionTypeFilter {
    ALL("Semua", null),
    INCOME("Pemasukan", TransactionType.INCOME),
    EXPENSE("Pengeluaran", TransactionType.EXPENSE);

    private final String label;
    private final TransactionType type;

    TransactionTypeFilter(String label, TransactionType type) {
        this.label = label;
        this.type = type;
    }

    public String label() {
        return label;
    }

    public boolean matches(Transaction transaction) {
        return type == null || type == transaction.getType();
    }
}
