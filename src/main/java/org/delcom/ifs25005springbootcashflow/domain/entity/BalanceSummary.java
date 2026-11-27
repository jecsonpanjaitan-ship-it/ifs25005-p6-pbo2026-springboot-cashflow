package org.delcom.ifs25005springbootcashflow.domain.entity;

/** Ringkasan saldo: jumlah transaksi, total pemasukan, dan total pengeluaran. */
public record BalanceSummary(int count, long totalIncome, long totalExpense) {

    public long balance() {
        return totalIncome - totalExpense;
    }
}
