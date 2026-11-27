package org.delcom.ifs25005springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.delcom.ifs25005springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.junit.jupiter.api.Test;

class InMemoryFinanceAiServiceTest {

    @Test
    void defaultIsDeterministic() throws Exception {
        List<AiGeneratedTransaction> result = new InMemoryFinanceAiService().generateTransactions("apa saja");
        assertEquals(2, result.size());
        assertEquals(TransactionType.INCOME, result.get(0).type());
        assertEquals(result, new InMemoryFinanceAiService().generateTransactions("lain"));
    }

    @Test
    void customResultAndFailure() throws Exception {
        List<AiGeneratedTransaction> custom = List.of(new AiGeneratedTransaction("A", 1, TransactionType.INCOME));
        assertEquals(custom, InMemoryFinanceAiService.withResult(custom).generateTransactions("x"));
        DomainException boom = new DomainException("boom");
        assertSame(boom, assertThrows(DomainException.class,
                () -> InMemoryFinanceAiService.failing(boom).generateTransactions("x")));
    }
}
