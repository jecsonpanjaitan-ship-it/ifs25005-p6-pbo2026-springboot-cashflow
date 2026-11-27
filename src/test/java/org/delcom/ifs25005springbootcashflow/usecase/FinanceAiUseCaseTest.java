package org.delcom.ifs25005springbootcashflow.usecase;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25005springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import java.util.List;
import org.junit.jupiter.api.Test;

class FinanceAiUseCaseTest {

    private final FinanceUseCase finance = new FinanceUseCase(new TransactionRepository());

    @Test
    void generateAndSave() throws Exception {
        FinanceAiUseCase ai = new FinanceAiUseCase(new InMemoryFinanceAiService(), finance);
        List<AiGeneratedTransaction> items = ai.generateTransactions("  gaji 5 juta ");
        assertEquals(2, items.size());
        List<Transaction> saved = ai.saveGeneratedTransactions(1, items);
        assertEquals(2, saved.size());
        assertEquals(4_975_000, finance.getBalance(1).balance());
    }

    @Test
    void generateValidation() {
        FinanceAiUseCase ai = new FinanceAiUseCase(InMemoryFinanceAiService.withResult(List.of()), finance);
        assertThrows(ValidationException.class, () -> ai.generateTransactions(null));
        assertThrows(ValidationException.class, () -> ai.generateTransactions("   "));
        ValidationException empty = assertThrows(ValidationException.class, () -> ai.generateTransactions("x"));
        assertEquals("AI tidak menghasilkan transaksi apa pun", empty.getMessage());
    }

    @Test
    void aiFailurePropagates() {
        FinanceAiUseCase ai = new FinanceAiUseCase(
                InMemoryFinanceAiService.failing(new DomainException("AI mati")), finance);
        DomainException ex = assertThrows(DomainException.class, () -> ai.generateTransactions("x"));
        assertEquals("AI mati", ex.getMessage());
    }

    @Test
    void saveValidation() {
        FinanceAiUseCase ai = new FinanceAiUseCase(new InMemoryFinanceAiService(), finance);
        assertThrows(ValidationException.class, () -> ai.saveGeneratedTransactions(1, null));
        assertThrows(ValidationException.class, () -> ai.saveGeneratedTransactions(1, List.of()));
        assertThrows(ValidationException.class, () -> ai.saveGeneratedTransactions(1,
                List.of(new AiGeneratedTransaction("Nol", 0, TransactionType.INCOME))));
    }
}
