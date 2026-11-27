package org.delcom.ifs25005springbootcashflow.adapter.ai;

import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IFinanceAiService;
import java.util.List;

/** Stub AI deterministik untuk unit test (tanpa jaringan). */
public class InMemoryFinanceAiService implements IFinanceAiService {

    private final List<AiGeneratedTransaction> result;
    private final DomainException failure;

    public InMemoryFinanceAiService() {
        this(List.of(
                new AiGeneratedTransaction("Gaji bulanan", 5_000_000, TransactionType.INCOME),
                new AiGeneratedTransaction("Makan siang", 25_000, TransactionType.EXPENSE)), null);
    }

    private InMemoryFinanceAiService(List<AiGeneratedTransaction> result, DomainException failure) {
        this.result = result;
        this.failure = failure;
    }

    public static InMemoryFinanceAiService withResult(List<AiGeneratedTransaction> result) {
        return new InMemoryFinanceAiService(result, null);
    }

    public static InMemoryFinanceAiService failing(DomainException failure) {
        return new InMemoryFinanceAiService(List.of(), failure);
    }

    @Override
    public List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException {
        if (failure != null) {
            throw failure;
        }
        return result;
    }
}
