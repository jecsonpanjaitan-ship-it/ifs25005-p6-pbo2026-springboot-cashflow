package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** Implementasi JDBC (runtime) ITransactionRepository. */
public class JdbcTransactionRepository implements ITransactionRepository {

    private static final String SELECT_ALL =
            "SELECT id, user_id, description, amount, type, proof_path, created_at FROM transactions";

    private static final RowMapper<Transaction> MAPPER = (rs, rowNum) -> new Transaction(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("description"),
            rs.getLong("amount"),
            TransactionType.valueOf(rs.getString("type")),
            rs.getString("proof_path"),
            rs.getTimestamp("created_at").toLocalDateTime());

    private final JdbcTemplate jdbc;

    public JdbcTransactionRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public Transaction save(int userId, String description, long amount, TransactionType type, String proofPath) {
        LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO transactions (user_id, description, amount, type, proof_path, created_at) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, userId);
            ps.setString(2, description);
            ps.setLong(3, amount);
            ps.setString(4, type.name());
            ps.setString(5, proofPath);
            ps.setTimestamp(6, Timestamp.valueOf(createdAt));
            return ps;
        }, keyHolder);
        return new Transaction(JdbcSupport.generatedId(keyHolder), userId, description, amount, type, proofPath,
                createdAt);
    }

    @Override
    public List<Transaction> findByUserId(int userId) {
        return jdbc.query(SELECT_ALL + " WHERE user_id = ? "
                + "ORDER BY created_at DESC, id DESC", MAPPER, userId);
    }

    @Override
    public Optional<Transaction> findById(Integer id) {
        return jdbc.query(SELECT_ALL + " WHERE id = ?", MAPPER, id)
                .stream().findFirst();
    }

    @Override
    public List<Transaction> findAll() {
        return jdbc.query(SELECT_ALL + " ORDER BY created_at DESC, id DESC", MAPPER);
    }

    @Override
    public List<Transaction> findBy(Predicate<? super Transaction> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public Transaction update(Transaction entity) {
        jdbc.update("UPDATE transactions SET description = ?, amount = ?, type = ?, proof_path = ? WHERE id = ?",
                entity.getDescription(), entity.getAmount(), entity.getType().name(), entity.getProofPath(),
                entity.getId());
        return entity;
    }

    @Override
    public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM transactions WHERE id = ?", id) > 0;
    }
}