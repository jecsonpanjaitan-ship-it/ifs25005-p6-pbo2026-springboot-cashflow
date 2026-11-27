package org.delcom.ifs25005springbootcashflow.adapter.repository;

import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

/** Implementasi JDBC (runtime) IUserRepository. */
public class JdbcUserRepository implements IUserRepository {

    private static final String COLUMNS = "id, username, password_hash, full_name, role, photo_path";

    private static final RowMapper<User> MAPPER = (rs, rowNum) -> new User(
            rs.getInt("id"),
            rs.getString("username"),
            rs.getString("password_hash"),
            rs.getString("full_name"),
            Role.valueOf(rs.getString("role")),
            rs.getString("photo_path"));

    private final JdbcTemplate jdbc;

    public JdbcUserRepository(DataSource dataSource) {
        this.jdbc = new JdbcTemplate(dataSource);
    }

    @Override
    public User save(String username, String passwordHash, String fullName, Role role) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            ps.setString(4, role.name());
            return ps;
        }, keyHolder);
        return new User(JdbcSupport.generatedId(keyHolder), username, passwordHash, fullName, role, null);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbc.query("SELECT " + COLUMNS + " FROM users WHERE username = ?", MAPPER, username)
                .stream().findFirst();
    }

    @Override
    public Optional<User> findById(Integer id) {
        return jdbc.query("SELECT " + COLUMNS + " FROM users WHERE id = ?", MAPPER, id).stream().findFirst();
    }

    @Override
    public List<User> findAll() {
        return jdbc.query("SELECT " + COLUMNS + " FROM users ORDER BY id", MAPPER);
    }

    @Override
    public List<User> findBy(Predicate<? super User> predicate) {
        return findAll().stream().filter(predicate).toList();
    }

    @Override
    public User update(User entity) {
        jdbc.update("UPDATE users SET password_hash = ?, full_name = ?, photo_path = ? WHERE id = ?",
                entity.getPasswordHash(), entity.getFullName(), entity.getPhotoPath(), entity.getId());
        return entity;
    }

    @Override
    public boolean deleteById(Integer id) {
        return jdbc.update("DELETE FROM users WHERE id = ?", id) > 0;
    }
}
