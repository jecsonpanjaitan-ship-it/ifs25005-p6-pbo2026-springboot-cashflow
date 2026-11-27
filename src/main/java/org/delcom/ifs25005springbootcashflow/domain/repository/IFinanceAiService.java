package org.delcom.ifs25005springbootcashflow.domain.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.AiGeneratedTransaction;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import java.util.List;

/** Port AI: mengubah catatan teks bebas menjadi daftar transaksi. */
public interface IFinanceAiService {

    List<AiGeneratedTransaction> generateTransactions(String prompt) throws DomainException;
}
