package org.delcom.ifs25005springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.adapter.repository.TransactionRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.UserRepository;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryRepositoriesTest {

    @Test
    void transactionRepositoryCrud() {
        TransactionRepository repo = new TransactionRepository();
        Transaction a = repo.save(1, "A", 100, TransactionType.INCOME, null);
        Transaction b = repo.save(1, "B", 200, TransactionType.EXPENSE, "b.png");
        Transaction other = repo.save(2, "C", 300, TransactionType.INCOME, null);

        assertEquals(1, a.getId());
        assertEquals(2, b.getId());
        assertEquals(List.of(b, a), repo.findByUserId(1));
        assertEquals(List.of(a, b, other), repo.findAll());
        assertEquals(List.of(other), repo.findBy(t -> t.getUserId() == 2));
        assertSame(a, repo.findById(1).orElseThrow());
        assertTrue(repo.findById(99).isEmpty());

        Transaction edited = new Transaction(1, 1, "A2", 150, TransactionType.INCOME, null, a.getCreatedAt());
        assertSame(edited, repo.update(edited));
        assertEquals("A2", repo.findById(1).orElseThrow().getDescription());

        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
    }

    @Test
    void userRepositoryCrud() {
        UserRepository repo = new UserRepository();
        User budi = repo.save("budi", "h", "Budi", Role.USER);
        User admin = repo.save("admin", "h", "Admin", Role.ADMIN);

        assertEquals(1, budi.getId());
        assertSame(admin, repo.findByUsername("admin").orElseThrow());
        assertTrue(repo.findByUsername("nope").isEmpty());
        assertEquals(List.of(budi, admin), repo.findAll());
        assertEquals(List.of(admin), repo.findBy(User::isAdmin));
        assertSame(budi, repo.findById(1).orElseThrow());
        assertTrue(repo.findById(5).isEmpty());
        budi.setFullName("Budi Baru");
        assertSame(budi, repo.update(budi));
        assertTrue(repo.deleteById(1));
        assertFalse(repo.deleteById(1));
    }
}
