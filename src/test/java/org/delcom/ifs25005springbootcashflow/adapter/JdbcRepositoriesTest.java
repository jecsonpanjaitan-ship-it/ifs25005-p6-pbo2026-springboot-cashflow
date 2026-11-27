package org.delcom.ifs25005springbootcashflow.adapter;

import static org.junit.jupiter.api.Assertions.*;

import org.delcom.ifs25005springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import java.util.List;
import java.util.UUID;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/** Tes JDBC memakai H2 in-memory (bukan PostgreSQL). */
class JdbcRepositoriesTest {

    private JdbcUserRepository users;
    private JdbcTransactionRepository transactions;
    private User owner;

    static DataSource newDataSource() {
        return new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
    }

    @BeforeEach
    void setUp() {
        DataSource ds = newDataSource();
        SchemaInitializer.initialize(ds);
        SchemaInitializer.initialize(ds); // idempoten
        users = new JdbcUserRepository(ds);
        transactions = new JdbcTransactionRepository(ds);
        owner = users.save("budi", "hash", "Budi", Role.USER);
    }

    @Test
    void userCrud() {
        assertTrue(owner.getId() > 0);
        User admin = users.save("admin", "hash2", "Admin", Role.ADMIN);

        User found = users.findByUsername("admin").orElseThrow();
        assertEquals(admin.getId(), found.getId());
        assertEquals(Role.ADMIN, found.getRole());
        assertNull(found.getPhotoPath());
        assertTrue(users.findByUsername("nope").isEmpty());

        assertEquals("Budi", users.findById(owner.getId()).orElseThrow().getFullName());
        assertTrue(users.findById(999).isEmpty());
        assertEquals(2, users.findAll().size());
        assertEquals(1, users.findBy(User::isAdmin).size());

        owner.setFullName("Budi Baru");
        owner.setPasswordHash("hash3");
        owner.setPhotoPath("foto.png");
        assertSame(owner, users.update(owner));
        User reloaded = users.findById(owner.getId()).orElseThrow();
        assertEquals("Budi Baru", reloaded.getFullName());
        assertEquals("hash3", reloaded.getPasswordHash());
        assertEquals("foto.png", reloaded.getPhotoPath());

        assertTrue(users.deleteById(admin.getId()));
        assertFalse(users.deleteById(admin.getId()));
    }

    @Test
    void transactionCrud() {
        Transaction a = transactions.save(owner.getId(), "Gaji", 5_000_000, TransactionType.INCOME, null);
        Transaction b = transactions.save(owner.getId(), "Makan", 25_000, TransactionType.EXPENSE, "bukti.png");
        assertTrue(b.getId() > a.getId());

        Transaction found = transactions.findById(b.getId()).orElseThrow();
        assertEquals("Makan", found.getDescription());
        assertEquals(25_000, found.getAmount());
        assertEquals(TransactionType.EXPENSE, found.getType());
        assertEquals("bukti.png", found.getProofPath());
        assertEquals(b.getCreatedAt(), found.getCreatedAt());
        assertNull(transactions.findById(a.getId()).orElseThrow().getProofPath());
        assertTrue(transactions.findById(999).isEmpty());

        // ORDER BY created_at DESC, id DESC -> terbaru di atas
        assertEquals(List.of(b.getId(), a.getId()),
                transactions.findByUserId(owner.getId()).stream().map(Transaction::getId).toList());
        assertTrue(transactions.findByUserId(999).isEmpty());
        assertEquals(2, transactions.findAll().size());
        assertEquals(1, transactions.findBy(t -> t.getType() == TransactionType.INCOME).size());

        Transaction edited = new Transaction(a.getId(), owner.getId(), "Gaji Naik", 6_000_000,
                TransactionType.INCOME, "baru.png", a.getCreatedAt());
        assertSame(edited, transactions.update(edited));
        Transaction reloaded = transactions.findById(a.getId()).orElseThrow();
        assertEquals("Gaji Naik", reloaded.getDescription());
        assertEquals(6_000_000, reloaded.getAmount());
        assertEquals("baru.png", reloaded.getProofPath());

        assertTrue(transactions.deleteById(a.getId()));
        assertFalse(transactions.deleteById(a.getId()));
    }
}
