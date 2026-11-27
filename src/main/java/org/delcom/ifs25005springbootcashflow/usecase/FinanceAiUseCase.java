package org.delcom.ifs25005springbootcashflow.usecase;

import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IFinanceAiService;
import java.util.ArrayList;
import java.util.List;

/** Generate transaksi dari catatan teks via port AI, lalu simpan hasilnya. */
public class FinanceAiUseCase {

    private final IFinanceAiService aiService;
    private final FinanceUseCase financeUseCase;

    public FinanceAiUseCase(IFinanceAiService aiService, FinanceUseCase financeUseCase) {
        this.aiService = aiService;
        this.financeUseCase = financeUseCase;
    }

    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        if (prompt == null || prompt.isBlank()) {
            throw new ValidationException("Prompt wajib diisi");
        }
        List<AiGeneratedTransaction> result = aiService.generateTransactions(prompt.trim());
        if (result.isEmpty()) {
            throw new ValidationException("AI tidak menghasilkan transaksi apa pun");
        }
        return result;
    }

    public List<Transaction> saveGeneratedTransactions(int userId, List<AiGeneratedTransaction> items)
            throws DomainException {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("Tidak ada transaksi untuk disimpan");
        }
        List<Transaction> saved = new ArrayList<>();
        for (AiGeneratedTransaction item : items) {
            saved.add(financeUseCase.addTransaction(userId, item.description(), item.amount(), item.type(), null));
        }
        return saved;
    }
}
