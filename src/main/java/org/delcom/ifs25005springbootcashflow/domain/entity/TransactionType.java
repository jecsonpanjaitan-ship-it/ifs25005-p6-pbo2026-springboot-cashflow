package org.delcom.ifs25005springbootcashflow.domain.entity;

import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;

/** Jenis transaksi: pemasukan menambah saldo, pengeluaran mengurangi saldo. */
public enum TransactionType {
    INCOME("Pemasukan", 1),
    EXPENSE("Pengeluaran", -1);

    private final String label;
    private final int sign;

    TransactionType(String label, int sign) {
        this.label = label;
        this.sign = sign;
    }

    public String label() {
        return label;
    }

    /** Jumlah bertanda: positif untuk pemasukan, negatif untuk pengeluaran. */
    public long signed(long amount) {
        return sign * amount;
    }

    /** Menerjemahkan teks (nama enum atau label Indonesia, tanpa peka huruf besar/kecil). */
    public static TransactionType fromText(String raw) throws ValidationException {
        if (raw == null || raw.isBlank()) {
            throw new ValidationException("Tipe transaksi wajib diisi");
        }
        String text = raw.trim();
        for (TransactionType type : values()) {
            if (type.name().equalsIgnoreCase(text) || type.label.equalsIgnoreCase(text)) {
                return type;
            }
        }
        throw new ValidationException("Tipe transaksi tidak dikenal: " + text);
    }
}
