package org.delcom.ifs25005springbootcashflow;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.UUID;
import javax.sql.DataSource;
import org.delcom.ifs25005springbootcashflow.adapter.ai.InMemoryFinanceAiService;
import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.config.AppConfig;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.ValidationException;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Uji integrasi end-to-end lintas layer lewat composition root (AppConfig) dengan H2 dan stub AI:
 * register/login -> tambah transaksi + bukti -> saldo -> Generate AI -> hapus -> isolasi data antar user.
 */
class AppTest {

    @TempDir
    Path tmp;

    @Test
    void endToEndCashflowFlow() throws Exception {
        AppConfig config = new AppConfig();
        DataSource ds = new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
        IUserRepository users = config.userRepository(ds);
        ITransactionRepository repo = config.transactionRepository(ds, users);
        AuthUseCase auth = config.authUseCase(users);
        FinanceUseCase finance = config.financeUseCase(repo);
        FinanceAiUseCase ai = config.financeAiUseCase(new InMemoryFinanceAiService(), finance);
        FinancePresenter presenter = config.financePresenter();
        FileStorage storage = config.fileStorage(tmp.resolve("uploads").toString());
        config.seedRootAdmin(auth, "admin", "admin").run(null);

        User budi = auth.register("budi", "rahasia", "Budi");
        User sari = auth.register("sari", "rahasia", "Sari");
        assertEquals(budi.getId(), auth.login("budi", "rahasia").getId());
        assertThrows(ValidationException.class, () -> auth.login("budi", "salah"));

        String proof = storage.store("bukti.png", new byte[] {1, 2, 3});
        Transaction gaji = finance.addTransaction(budi.getId(), "Gaji", "5000000", TransactionType.INCOME, proof);
        finance.addTransaction(budi.getId(), "Makan siang", "25000", TransactionType.EXPENSE, null);
        assertEquals("Saldo: Rp 4,975,000.00", presenter.formatBalance(finance.getBalance(budi.getId()).balance()));
        assertEquals("Makan siang", finance.listTransactions(budi.getId()).get(0).getDescription());
        assertTrue(finance.listTransactions(budi.getId()).get(1).hasProof());

        ai.saveGeneratedTransactions(budi.getId(), ai.generateTransactions("gaji 5 juta"));
        assertEquals(4, finance.getBalance(budi.getId()).count());

        assertTrue(finance.listTransactions(sari.getId()).isEmpty());
        assertThrows(ValidationException.class, () -> finance.deleteTransaction(sari.getId(), gaji.getId()));
        finance.deleteTransaction(budi.getId(), gaji.getId());
        assertEquals(3, finance.getBalance(budi.getId()).count());
    }
}
