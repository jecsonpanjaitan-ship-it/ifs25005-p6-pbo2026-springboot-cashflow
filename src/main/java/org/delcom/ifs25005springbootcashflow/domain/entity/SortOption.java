package org.delcom.ifs25005springbootcashflow.domain.entity;

import java.util.Comparator;

/** Opsi pengurutan daftar transaksi. Default UI: {@link #NEWEST_FIRST}. */
public enum SortOption {
    NEWEST_FIRST("Terbaru",
            Comparator.comparing(Transaction::getCreatedAt).thenComparingInt(Transaction::getId).reversed()),
    OLDEST_FIRST("Terlama",
            Comparator.comparing(Transaction::getCreatedAt).thenComparingInt(Transaction::getId)),
    AMOUNT_HIGHEST("Jumlah terbesar",
            Comparator.comparingLong(Transaction::getAmount).thenComparingInt(Transaction::getId).reversed()),
    AMOUNT_LOWEST("Jumlah terkecil",
            Comparator.comparingLong(Transaction::getAmount).thenComparingInt(Transaction::getId)),
    DESCRIPTION_AZ("Keterangan A-Z",
            Comparator.comparing(Transaction::getDescription, String.CASE_INSENSITIVE_ORDER)
                    .thenComparingInt(Transaction::getId));

    private final String label;
    private final Comparator<Transaction> comparator;

    SortOption(String label, Comparator<Transaction> comparator) {
        this.label = label;
        this.comparator = comparator;
    }

    public String label() {
        return label;
    }

    public Comparator<Transaction> comparator() {
        return comparator;
    }
}
